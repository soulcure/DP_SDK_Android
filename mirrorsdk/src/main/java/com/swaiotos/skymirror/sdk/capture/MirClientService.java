/*
 * 创建VirtualDisplay进行抓取屏幕数据，将数据进行编码，再通过socket将编码后的数据发送至接收端
 * 发送数据完整流程：
 * 发送数据时通道有3种：
 * 1.iot-channel用来发送开启、关闭消息（替代设备发现）
 * 2.httpServer用来传输指令 （用于验证版本、传递分辨率、触控事件及心跳包）
 * 3.socket用来传输数据流 （传输编码后的数据）
 * if(iot-channel) ：
 * 1.接收端通过iot-channel（携带接收端IP,创建通道所需要port）发起传递数据请求，并创建接收端指令、数据服务端，
 * 发送端接收到消息后开启抓屏服务，并创建client连接至服务端（使用iot-channel携带的ip,port），至此指令、传输数据通道建立
 * 2.经过一系列的指令交互验证，验证通过后创建编码器及绘制所需画布（surface）
 * 3.获取到surface后创建VirtualDisplay抓取屏幕数据并绘制到surface，由于电视端系统缘故，抓屏方式分两种：
 * ① 电视端通过DisplayManager创建VirtualDisplay(需要system权限)
 * ② 手机端通过MediaProjection创建VirtualDisplay（需要手动获取显示最上层权限）
 * 4.将编码后的数据通过socket发送至接收端
 * 关于分辨率：理论上：取发送端编码器和接收端编解码器共同支持的最大分辨率进行编解码操作。
 * 目前优化分辨率为业务层开通接口将分辨率传入
 * <p>
 * if(本地设备发现)
 * //暂定
 */
package com.swaiotos.skymirror.sdk.capture;


import android.annotation.TargetApi;
import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.skyworth.dpclientsdk.StreamSourceCallback;
import com.swaiotos.skymirror.sdk.Command.Command;
import com.swaiotos.skymirror.sdk.Command.DecoderStatus;
import com.swaiotos.skymirror.sdk.Command.Dog;
import com.swaiotos.skymirror.sdk.Command.SendData;
import com.swaiotos.skymirror.sdk.Command.ServerVersionCodec;
import com.swaiotos.skymirror.sdk.data.PortKey;
import com.swaiotos.skymirror.sdk.data.TouchData;
import com.swaiotos.skymirror.sdk.reverse.MotionEventUtil;
import com.swaiotos.skymirror.sdk.util.DLNACommonUtil;
import com.swaiotos.skymirror.sdk.util.NetUtils;
import com.skyworth.dpclientsdk.WebSocketClient;
import com.skyworth.dpclientsdk.ConnectState;
import com.skyworth.dpclientsdk.ResponseCallback;
import com.skyworth.dpclientsdk.StreamChannelSource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: MirClientService
 * @Description: 抓屏服务类，提供抓屏、编码、传流功能（即发送数据端，亦为client端）
 * @Author: lfz
 * @Date: 2020/4/15 9:38
 */
public abstract class MirClientService extends Service {


    //常量定义--start---
    private static final String TAG = MirClientService.class.getSimpleName();

    private static final String MIR_CLIENT_VERSION = "3.0";

    private static final int MIN_BITRATE_THRESHOLD = 4 * 1024 * 1024;
    private static final int DEFAULT_BITRATE = 6 * 1024 * 1024;
    private static final int MAX_BITRATE_THRESHOLD = 8 * 1024 * 1024;

    private static final int MAX_VIDEO_FPS = 60;


    private static final int HEART_BEAT_INTERVAL = 5; //心跳间隔30秒
    //常量定义--end---

    private ScheduledExecutorService heartBeatScheduled;


    private MediaCodec encoder = null;

    private VirtualDisplay virtualDisplay = null;

    private boolean isExit;

    private int mWatchDog = 0;

    public int mScreenDisplayWidth;
    public int mScreenDisplayHeight;

    public int mWidth;
    public int mHeight;


    private int mBitrate = DEFAULT_BITRATE;
    private long consumedUs;

    private String mimeType;   //编码器类型


    private MediaCodec.BufferInfo mBufferInfo;

    private int mEncoderCodecSupportType;  //硬件编解码器信息


    private WebSocketClient mWebSocketClient;

    private LinkedBlockingQueue<MotionEvent> touchEventQueue;

    private StreamChannelSource mVideoDataClient;
    private MediaProjection mediaProjection;


    private StreamSourceCallback mStreamSourceCallback = new StreamSourceCallback() {
        @Override
        public void onConnectState(ConnectState state) {
            Log.d(TAG, "web socket client onConnectState: --- " + state);
            if (state == ConnectState.ERROR) {
                stopSelf();
            }
        }
    };


    private ResponseCallback mResponseCallback = new ResponseCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onCommand(String s) {
            clientOnRead(s);
        }

        //使用send byte[] 接口专门发送触控事件
        @Override
        public void onCommand(byte[] bytes) {
            String json = new String(bytes);
            Gson gson = new Gson();
            try {
                MotionEvent event = MotionEventUtil.formatMotionEvent(gson.fromJson(json, TouchData.class));
                touchEventQueue.add(event);
                Log.d(TAG, "addInputEvent2 onCommand:event --- " + event.toString());
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "addInputEvent2 onCommand event error--- " + e.toString());
            }
        }


        @Override
        public void ping(String cmd) {
            Dog dog = Command.getDogData(cmd);
            if (dog != null) {
                consumedUs = dog.dog;
            }
        }

        @Override
        public void pong(String cmd) {
            Log.d(TAG, "web socket client pong---" + cmd);
            mWatchDog--;
        }


        @Override
        public void onConnectState(ConnectState connectState) {
            Log.e(TAG, "web socket client onConnectState -----" + connectState);
            if (connectState == ConnectState.CONNECT) {
                sendMsg(Command.setCheckVersion(MIR_CLIENT_VERSION));
                touchEventQueue.clear();
                new Thread(new InputWorkerTouch(), "Input Thread Touch").start();//bsp

                startHeartBeat();    //开启心跳
            } else { // ConnectState.ERROR ,ConnectState.DISCONNECT
                stopSelf();
            }
        }
    };


    /**
     * 子类实现的抽象方法
     */
    protected abstract void initNotification();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MirClientService onCreate");
        isExit = false;
        touchEventQueue = new LinkedBlockingQueue<>();
        mBufferInfo = new MediaCodec.BufferInfo();
    }

    /**
     * Main Entry Point of the server code. Create a WebSocket server and start
     * the encoder.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        Log.d(TAG, "MirClientService onStartCommand---" + action);

        if (action != null && action.equals("START")) {
            initNotification();
            String ip = intent.getStringExtra("serverip");
            int resultCode = intent.getIntExtra("resultCode", -10);
            Intent data = intent.getParcelableExtra("intent");

            initMediaProjection(resultCode, data);

            DisplayMetrics dm = new DisplayMetrics();
            Display mDisplay = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay();
            mDisplay.getMetrics(dm);
            int deviceWidth = dm.widthPixels;
            int deviceHeight = dm.heightPixels;

            Log.d(TAG, "deviceWidth:" + deviceWidth + ",deviceHeight:" + deviceHeight);

            if (!DLNACommonUtil.checkPermission(this)) {
                if (deviceWidth > 1080) {
                    float desWH = 1080f / deviceWidth;
                    int baseHeight = (int) (deviceHeight * desWH);
                    mWidth = (int) (deviceWidth * desWH);
                    mHeight = baseHeight - (baseHeight % 16);
                } else if (deviceHeight > 1920) {
                    float desWH = 1920F / deviceHeight;
                    int baseWidth = (int) (deviceWidth * desWH);
                    mWidth = baseWidth - (baseWidth % 16);
                    mHeight = (int) (deviceHeight * desWH);
                } else {
                    mWidth = deviceWidth;
                    mHeight = deviceHeight;
                }
            } else {
                mWidth = deviceWidth;
                mHeight = deviceHeight;
            }

            Log.e(TAG, "onStartCommand: " + mWidth + " --- " + mHeight);
            Log.d(TAG, "mScreenDisplayWidth:" + mScreenDisplayWidth + ",mScreenDisplayHeight:" + mScreenDisplayHeight);

            checkEncoderSupportCodec();

            Log.d(TAG, "onStartCommand: client(PHONE) ip ----- " + ip);
            Log.d(TAG, "onStartCommand: server(TV) ip ----- " + NetUtils.getIP(this));

            mWebSocketClient = new WebSocketClient(ip, PortKey.PORT_HTTP_DATA, mResponseCallback);
            mWebSocketClient.open();

            mVideoDataClient = new StreamChannelSource(ip, PortKey.PORT_SOCKET_VIDEO,
                    mStreamSourceCallback);

        } else if (action != null && action.equals("STOP")) {
            Log.d(TAG, "onStartCommand: stop flag");
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    public void ping(String msg) {
        mWatchDog++;
        if (mWebSocketClient != null) {
            Log.d(TAG, "web socket client ping---" + msg);
            mWebSocketClient.ping(msg);
        }
    }


    public void sendMsg(String msg) {
        if (mWebSocketClient != null) {
            mWebSocketClient.send(msg);
        }
    }


    //check解码器
    private void checkEncoderSupportCodec() {
        //获取所有编解码器个数
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            //获取所有支持的编解码器信息
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            // 判断是否为编码器，否则直接进入下一次循环
            if (!codecInfo.isEncoder()) {
                continue;
            }
            // 如果是解码器，判断是否支持Mime类型
            String[] types = codecInfo.getSupportedTypes();
            for (String type : types) {
                if (type.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_AVC)) {
                    mEncoderCodecSupportType |= Command.CODEC_AVC_FLAG;
                    Log.d(TAG, "AVC Supported");
                    continue;
                }

                if (type.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_HEVC)) {
                    mEncoderCodecSupportType |= Command.CODEC_HEVC_FLAG;
                    Log.d(TAG, "HEVC Supported");
                }
            }
        }
        Log.d(TAG, "encoderCodecSupportType " + mEncoderCodecSupportType);
    }


    private void initMediaProjection(int resultCode, Intent data) {
        if (resultCode == -10 || data == null)
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                    getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            if (mediaProjection == null) {
                mediaProjection = mediaProjectionManager.getMediaProjection(
                        resultCode, data);
            }
        }
    }


    //clint onRead
    private void clientOnRead(String s) {
        if (s.startsWith(Command.ServerVersion)) {   // first cmd
            ServerVersionCodec versionCodec = Command.getServerVersionCodec(s);

            if (versionCodec.serverVersion.compareTo(MIR_CLIENT_VERSION) >= 0) {
                Log.d(TAG, "ServerVersion fitted with " + MIR_CLIENT_VERSION);
            }
            int codeSupport = versionCodec.codecSupport; //对方的编码类型
            int encoderCodecType; //自己的编码类型
            //高分辨率手机使用h265
            if (((codeSupport & Command.CODEC_HEVC_FLAG) == Command.CODEC_HEVC_FLAG)  //对方支持H265
                    && ((mEncoderCodecSupportType & Command.CODEC_HEVC_FLAG) == Command.CODEC_HEVC_FLAG)) { //自己支持H265
                mimeType = MediaFormat.MIMETYPE_VIDEO_HEVC;  //H265
                encoderCodecType = Command.CODEC_HEVC_FLAG;
            } else {
                mimeType = MediaFormat.MIMETYPE_VIDEO_AVC;  //H264
                encoderCodecType = Command.CODEC_AVC_FLAG;
            }

            Log.d(TAG, "CodecSupport is remote---" + codeSupport + "  ---self---" + encoderCodecType);

            String localIp = NetUtils.getIP(MirClientService.this);

            Log.d(TAG, "onCommand: getLocalIp ---- " + localIp);

            String ipCodec = Command.setClientIpCodec(true, localIp, encoderCodecType); //发送给对方

            sendMsg(ipCodec);

            Log.e("colin", "colin start time02 --- tv check version and codeSupport");
        } else if (s.startsWith(Command.DecoderStatus)) {  //second cmd
            DecoderStatus status = Command.getDecoderStatus(s);
            if (status.decoderStatus) {
                // 处理屏幕旋转，通知解码端分辨率，动态设置，如果屏幕旋转，需要重新设置
                String hw = Command.setFrameWH(true, mWidth, mHeight);
                sendMsg(hw);
                Log.e("colin", "colin start time03 --- tv check DecoderStatus and set hw");
            }
        } else if (s.startsWith(Command.SendData)) { //third cmd
            SendData data = Command.getSendData(s);
            if (data.sendData) {
                Log.d(TAG, "start Encode....");
                new Thread(new EncoderWorker(), "Encoder Thread").start();
                Log.e("colin", "colin start time04 --- tv start Encoder and SendData");
            }
        } else if (s.startsWith(Command.Bye)) { //exit cmd
            stopSelf();
        }
    }


    private void adjustBitRate(MediaCodec.BufferInfo info) {
        long ts = (info.presentationTimeUs - consumedUs) / 1000;  //纳秒转换为毫秒
        Log.d(TAG, "adjustBitRate ts---" + ts);

        int bitrate;
        if (ts > 300) { //延时大于300ms
            bitrate = MIN_BITRATE_THRESHOLD;
        } else if (ts < 100) { //延时小于100ms
            bitrate = MAX_BITRATE_THRESHOLD;
        } else {
            bitrate = DEFAULT_BITRATE;
        }

        if (bitrate == mBitrate) {
            Log.d(TAG, "adjustBitRate no need");
            return;
        }

        Log.d(TAG, "adjustBitRate increase bit rate---" + bitrate);
        Bundle param = new Bundle();
        param.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, bitrate);
        encoder.setParameters(param);
        mBitrate = bitrate;
    }


    private void doEncodeWork() {
        try {
            int index = encoder.dequeueOutputBuffer(mBufferInfo, -1);
            if (index >= 0) {
                adjustBitRate(mBufferInfo);
                ByteBuffer encodeData;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    ByteBuffer[] byteBuffers = encoder.getOutputBuffers();
                    encodeData = byteBuffers[index];
                } else {
                    encodeData = encoder.getOutputBuffer(index);
                }
                if (encodeData != null
                        && mVideoDataClient != null
                        && mVideoDataClient.isOpen()
                        && mBufferInfo.size != 0) {
                    mVideoDataClient.sendData(StreamChannelSource.PduType.VideoFrame, mBufferInfo, encodeData);

                    Log.e("colin", "colin start time05 --- tv start Encoder finish will send by socket");
                }
                encoder.releaseOutputBuffer(index, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "doEncodeWork error---" + e.getMessage());
        }
    }


    @TargetApi(19)
    private Surface createDisplaySurface() throws IOException {
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(mimeType, mWidth, mHeight);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, mBitrate); //设置比特率

        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, MAX_VIDEO_FPS);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);
        encoder = MediaCodec.createEncoderByType(mimeType);
        encoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        Log.i(TAG, "createDisplaySurface: " + mWidth + " ----- " + mHeight + "--- "
                + mimeType + " --- " + mBitrate);

        return encoder.createInputSurface();
    }


    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startDisplayManager() {
        try {
            Surface encoderInputSurface = createDisplaySurface();//系统Size

            if (DLNACommonUtil.checkPermission(this)) { //for tv
                Log.d(TAG, "startDisplayManager: create virtualDisplay by DisplayManager");
                DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
                virtualDisplay = displayManager.createVirtualDisplay(
                        "TV Screen Mirror", mWidth, mHeight,
                        50,
                        encoderInputSurface,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
                                | DisplayManager.VIRTUAL_DISPLAY_FLAG_SECURE);
            } else if (mediaProjection != null) { //for mobile
                Log.d(TAG, "startDisplayManager: create virtualDisplay by mediaProjection");
                virtualDisplay = mediaProjection
                        .createVirtualDisplay(
                                "TV Screen Mirror", mWidth, mHeight,
                                50,
                                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                                encoderInputSurface,
                                null, null);// bsp
            }
            encoder.start();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "startDisplayManager: create virtualDisplay error");
            stopSelf();
        }
    }

    private void mySleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 开始心跳
     */
    private void startHeartBeat() {
        if (heartBeatScheduled == null) {
            heartBeatScheduled = Executors.newScheduledThreadPool(5);
            heartBeatScheduled.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    heatBeat();
                }
            }, HEART_BEAT_INTERVAL, HEART_BEAT_INTERVAL, TimeUnit.SECONDS);
        }
    }


    /**
     * 停止心跳
     */
    private void stopHeartBeat() {
        if (heartBeatScheduled != null
                && !heartBeatScheduled.isShutdown()) {
            heartBeatScheduled.shutdown();
        }
    }


    /**
     * 心跳协议请求
     */
    private void heatBeat() {
        if (mWatchDog > 3) {
            Log.e(TAG, "fetch watchdog timeout");
            stopSelf();
        }
        ping(Command.setLiveData(System.currentTimeMillis()));
    }


    private class InputWorkerTouch implements Runnable {
        @Override
        public void run() {
            Log.d(TAG, "InputWorkerTouch enter");
            while (!isExit) {
                MotionEvent motionEvent = touchEventQueue.poll();
                if (motionEvent == null) {
                    mySleep(10);
                    continue;
                }

                Log.d(TAG, "run: 111111111111111111111111111111111111111111111111111111111111");

                long downtime = SystemClock.uptimeMillis();
                long eventTime = SystemClock.uptimeMillis();
                int count = motionEvent.getPointerCount();
                int action = motionEvent.getAction();
                MotionEvent.PointerProperties props[] = new MotionEvent.PointerProperties[count];
                MotionEvent.PointerCoords coords[] = new MotionEvent.PointerCoords[count];
                //int meta = motionEvent.getMetaState();
                //int bstat = motionEvent.getButtonState();
                //float xprec = motionEvent.getXPrecision();
                //float yprec = motionEvent.getYPrecision();
                //int edgef = motionEvent.getEdgeFlags();
                //int flag = motionEvent.getFlags();

                for (int i = 0; i < count; i++) {
                    props[i] = new MotionEvent.PointerProperties();
                    motionEvent.getPointerProperties(i, props[i]);
                }
                for (int i = 0; i < count; i++) {
                    coords[i] = new MotionEvent.PointerCoords();
                    motionEvent.getPointerCoords(i, coords[i]);
                }

                MotionEvent motionEvent2 = MotionEvent.obtain(downtime, eventTime, action, count, props, coords, 0, 0, 0, 0, 0, 0, 0, 0);

                Log.d(TAG, "touch obtain:" + motionEvent2.toString());

                try {
                    Instrumentation mInst = new Instrumentation();
                    mInst.sendPointerSync(motionEvent2);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

            }
            Log.d(TAG, "InputWorkerTouch exit");
        }
    }

    @TargetApi(19)
    private class EncoderWorker implements Runnable {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            Log.d(TAG, "EncoderWorker:start WatchDogThread");
            startDisplayManager();//it will be jammed for loop
            // 创建BufferedOutputStream对象
            while (!isExit) {
                doEncodeWork();
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MirClientService onDestroy...");
        stopClient();
        stopEncoder();
        stopForeground(true);
    }

    private void stopClient() {
        Log.d(TAG, "stopClient ");

        sendMsg("bye");

        stopHeartBeat();//停止心跳

        if (mWebSocketClient != null) {
            Log.d(TAG, "=====> Close WebSocketClient");
            mWebSocketClient.close();
            mWebSocketClient = null;
        }


        if (mVideoDataClient != null) {
            Log.d(TAG, "=====> Close VideoDataClient");
            mVideoDataClient.close();
            mVideoDataClient = null;
        }

    }

    private void stopEncoder() {
        isExit = true;

        try {
            if (encoder != null) {
                Log.d(TAG, "encoder stop.................");
                encoder.signalEndOfInputStream();
                encoder.stop();
                encoder.release();
                encoder = null;

            }

            if (virtualDisplay != null) {
                virtualDisplay.release();
                virtualDisplay = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
