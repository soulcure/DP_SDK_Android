package com.swaiotos.skymirror.sdk.reverse;

import android.app.Service;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import com.skyworth.dpclientsdk.WebSocketServer;
import com.swaiotos.skymirror.sdk.Command.CheckVer;
import com.swaiotos.skymirror.sdk.Command.ClientIpCodec;
import com.swaiotos.skymirror.sdk.Command.Command;
import com.swaiotos.skymirror.sdk.Command.FrameWH;
import com.swaiotos.skymirror.sdk.data.FrameInfo;
import com.swaiotos.skymirror.sdk.data.PortKey;
import com.skyworth.dpclientsdk.ConnectState;
import com.skyworth.dpclientsdk.RequestCallback;
import com.skyworth.dpclientsdk.StreamChannelSink;
import com.skyworth.dpclientsdk.StreamSinkCallback;
import com.swaiotos.skymirror.sdk.util.DLNACommonUtil;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import static com.swaiotos.skymirror.sdk.reverse.IPlayerListener.CODE_DECODER_ERROR;
import static com.swaiotos.skymirror.sdk.reverse.IPlayerListener.CODE_SOCKET_ERROR;


/**
 * 同屏控制的控制方
 * 接收投屏端发送过来的视频流，解码播放
 */
public class PlayerDecoder {

    //常量定义--start---
    private static final String TAG = PlayerDecoder.class.getSimpleName();
    private static final String MIR_SERVER_VERSION = "3.0";

    private static final int HEART_BEAT_INTERVAL = 5; //心跳间隔30秒
    //常量定义--end---

    interface DecoderListener {
        void setStatus(String Status, PlayerDecoder decoder);

        void setHW(int w, int h, int rotate, PlayerDecoder decoder);
    }


    private DecoderListener mListener;

    private int mEncoderCodecType; //接受到对方的编码类型

    private int mEncoderCodecSupportType;  //硬件编解码器信息

    private IPlayerListener playerListener;

    private Context mContext;

    private WebSocketServer mWebSocketServer;
    private Set<Integer> mWebSocketClients = new HashSet<>();

    //video socket 传输
    private StreamChannelSink mSocketServer;
    private LinkedBlockingQueue<FrameInfo> videoList = null;

    private boolean videoDecoderConfigured = false;

    private MediaCodec mVideoDecoder = null;

    private int mWatchDogStatus = 0;
    private volatile boolean isExit = false;

    private int mFrameWidth = 1080;
    private int mFrameHeight = 1920;


    private Surface mSurface;
    private boolean isFirst;


    public PlayerDecoder(Context context) {
        this(context, null);
    }

    public PlayerDecoder(Context context, Surface surface) {
        mContext = context;
        mSurface = surface;

        //控制端建立 webSocketServer 端，发送控制事件
        initWebSocketServer();
        //视频流接收方（接收方：socket server 端接收解码播放 ，socket client 端为录制编码发送方）
        initSocketServer();
        Log.d("playerDecoder", "onStartCommand: PlayerDecoder init success");
    }


    private void closeSocketServer() {
        Log.d(TAG, " --- Socket Server is close --- ");
        if (mSocketServer != null) {
            mSocketServer.close();
            mSocketServer = null;
        }

        if (mWebSocketServer != null) {
            mWebSocketServer.close();
            mWebSocketServer = null;
        }
    }


    /**
     * server send data to client
     *
     * @param socketId
     * @param data
     */
    private void sendData(int socketId, String data) {
        if (mWebSocketServer != null) {
            mWebSocketServer.sendData(socketId, data);
        }
    }


    private void ping(int socketId, String data) {
        if (mWebSocketServer != null) {
            Log.d(TAG, "web socket server ping---" + data);
            mWebSocketServer.ping(socketId, data);
        }
    }


    public void setDecoderListener(DecoderListener listener) {
        mListener = listener;
    }

    public void setStatusListener(IPlayerListener listener) {
        playerListener = listener;
    }

    public void start(Surface surface) {
        Log.d(TAG, "player decoder start...");
        videoList = new LinkedBlockingQueue<>();
        isExit = false;
        mSurface = surface;
        videoDecoderConfigured = false;
        isFirst = true;

        checkDecoderSupportCodec();
        new Thread(new Runnable() {
            @Override
            public void run() {
                videoDecoderInput();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                videoDecoderOutput();
            }
        }).start();


        //开始发送数据
        for (int clientPort : mWebSocketClients) {
            sendData(clientPort, Command.setDecoderStatus(true));
            sendData(clientPort, Command.setSendData(true));
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setVideoData(MediaCodec.BufferInfo info, ByteBuffer encodedFrame) {

        Log.e(TAG, "setVideoData:  check surface start");
        if (mSurface == null) {
            Log.e(TAG, "setVideoData: surface is null");
            return;
        }
        Log.e(TAG, "setVideoData:  check surface success");

        String mimeType;  //解码器类型
        if (mEncoderCodecType == Command.CODEC_HEVC_FLAG) {
            mimeType = MediaFormat.MIMETYPE_VIDEO_HEVC; //H265
        } else {
            mimeType = MediaFormat.MIMETYPE_VIDEO_AVC; //H264
        }

        if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {//配置数据

            Log.d(TAG, "mimeType is " + mimeType + ",mFrameWidth:" + mFrameWidth + ",Height:" + mFrameHeight);
            MediaFormat format = MediaFormat.createVideoFormat(mimeType, mFrameWidth, mFrameHeight);

//            byte[] bytes = new byte[encodedFrame.remaining()];  //sps pps config no need
//            encodedFrame.get(bytes, 0, bytes.length);
//            if (info.size < 7) {
//                Log.d(TAG, "BAD info");
//                return;
//            }
//            encodedFrame.flip();
//            boolean ppsFound = false;
//            int startPos;
//            //here we default assume the first is sps(csd-0) and the following is pps
//            for (startPos = 3; startPos < (info.size - 5); startPos++) {
//
//                if ((bytes[startPos] == 0x0)
//                        && (bytes[startPos + 1] == 0x0)
//                        && (bytes[startPos + 2] == 0x0)
//                        && (bytes[startPos + 3] == 0x1)
//                        && ((bytes[startPos + 4] & 0x1f) == 0x8)
//                ) {
//
//                    //if encodedFrame.get(i+4) & 0x1f is 8 , find pps
//                    Log.d(TAG, "pps found");
//                    ppsFound = true;
//                    break;
//                }
//            }
//            Log.d(TAG, "now b12.length " + startPos);
//            /*temp remove to save in buffer by Huazhu Sun*/
//            if (ppsFound) {
//                byte[] b12 = new byte[(bytes.length - startPos)];
//                System.arraycopy(bytes, startPos, b12, 0, b12.length);
//
//                ByteBuffer sps = ByteBuffer.wrap(bytes, 0, startPos);
//                ByteBuffer pps = ByteBuffer.wrap(b12, 0, info.size - startPos);
//
//                format.setByteBuffer("csd-0", sps);
//
//                format.setByteBuffer("csd-1", pps);
//
//                if (BuildConfig.DEBUG) {
//                    byte[] data = new byte[sps.remaining()];
//                    sps.get(data);
//                    Log.d("yao", "sps---:" + bytes2HexString(data));
//                    sps.flip();
//
//
//                    byte[] data2 = new byte[pps.remaining()];
//                    pps.get(data2);
//                    Log.d("yao", "pps---:" + bytes2HexString(data2));
//                    pps.flip();
//                }
//
//
//            } else {
//                format.setByteBuffer("csd-0", encodedFrame);
//            }

            try {
                format.setByteBuffer("csd-0", encodedFrame);
                format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, mFrameWidth * mFrameHeight);

                if (mVideoDecoder == null) {
                    mVideoDecoder = MediaCodec.createDecoderByType(mimeType);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mVideoDecoder.reset();
                }
                mVideoDecoder.configure(format, mSurface, null, 0);
                mVideoDecoder.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);//VIDEO_SCALING_MODE_SCALE_TO_FIT
                mVideoDecoder.start();
                videoDecoderConfigured = true;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "VideoDecoder init error" + e.toString());
            }

            Log.d(TAG, "video decoder configured (" + info.size + " bytes)");

            Log.e("colin", "colin start time05 --- pad start VideoDecoder configure finish");
            return;
        }

        FrameInfo videoFrame = new FrameInfo(info, encodedFrame);
        if (videoList != null) {
            videoList.add(videoFrame);
        }
    }


    /**
     * 解码器 input
     */
    private void videoDecoderInput() {
        while (!videoDecoderConfigured) {
            waitTimes(10);
        }

        int inputBufIndex = 0;
        while (!isExit) {
            FrameInfo videoFrame = videoList.poll();
            if (videoFrame == null) {
                waitTimes(1);
                continue;
            }

            ByteBuffer encodedFrames = videoFrame.encodedFrame;
            MediaCodec.BufferInfo info = videoFrame.bufferInfo;

            try {
                //解码 请求一个输入缓存
                inputBufIndex = mVideoDecoder.dequeueInputBuffer(-1);
                if (inputBufIndex < 0) {
                    Log.e(TAG, "dequeueInputBuffer result error---" + inputBufIndex);
                    waitTimes(1);
                    continue;
                }

                ByteBuffer[] inputBuf = mVideoDecoder.getInputBuffers();
                inputBuf[inputBufIndex].clear();
                inputBuf[inputBufIndex].put(encodedFrames);

                //解码数据添加到输入缓存中
                mVideoDecoder.queueInputBuffer(inputBufIndex, 0, info.size, info.presentationTimeUs, 0);

                Log.d(TAG, "end queue input buffer with ts " + info.presentationTimeUs + ",info.size :" + info.size);
                heatBeat(info.presentationTimeUs);

                Log.e("colin", "colin start time06 --- pad start VideoDecoder queueInputBuffer");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "videoDecoderInput error---" + e.getMessage());
            }


            if (playerListener != null & isFirst) {
                Log.d(TAG, "doVideoDecoderFeed: player is on start");
                playerListener.onStart();
                isFirst = false;
            }
        }
    }


    /**
     * 解码器 output
     */
    private void videoDecoderOutput() {
        while (!videoDecoderConfigured) {
            waitTimes(10);
        }

        Log.d(TAG, "videoDecoderOutput Thing enter");

        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        while (!isExit) {
            try {
                int decoderIndex = mVideoDecoder.dequeueOutputBuffer(info, -1);
                if (decoderIndex > 0) {
                    mVideoDecoder.releaseOutputBuffer(decoderIndex, true);
                    Log.e("colin", "colin start time07 --- pad start VideoDecoder dequeueOutputBuffer finish");
                } else {
                    Log.e(TAG, "videoDecoderOutput dequeueOutputBuffer error---" + decoderIndex);
                    if (playerListener != null) {
                        playerListener.onError(CODE_DECODER_ERROR, "decodedFrames error");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "videoDecoderOutput error---" + e.getMessage());
            }
        }
    }


    /**
     * 心跳协议请求
     */
    private void heatBeat(long consumeUs) {
        if (mWatchDogStatus > 30) {
            Log.e(TAG, "Disconnect Not Data");
            mirServerStop(DOG_ERR);//stop
        }
        sendDogMsg(consumeUs);
    }


    private void sendDogMsg(long us) {
        mWatchDogStatus++;
        for (int clientPort : mWebSocketClients) {
            ping(clientPort, Command.setDogData(us));
        }
    }


    private void waitTimes(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static final int DOG_ERR = 5;

    /**
     * 停止镜像服务
     *
     * @param errCode 错误类型
     */
    void mirServerStop(int errCode) {
        Log.e(TAG, "mirServerStop---" + errCode);

        isExit = true;  //关闭解码线程

        for (int clientPort : mWebSocketClients) {
            sendData(clientPort, Command.setByeData(true));
        }

        closeSocketServer();  //关闭 socket server

        if (mVideoDecoder != null) {
            Log.d(TAG, "unhappy decoder release");
            mVideoDecoder.stop();
            mVideoDecoder.release();
            mVideoDecoder = null;
        }

        if (playerListener != null) {
            Log.d(TAG, "mirServerStop: player is onStop");
            playerListener.onStop();
        }

        if (mListener != null) {
            mListener.setStatus("close", this);
        }
    }

    private void setUiHw() {
        Log.d(TAG, "setUiHw");
        if (mListener != null)
            mListener.setHW(mFrameWidth, mFrameHeight, 0, this);
    }


    public WebSocketServer getTouch() {
        return mWebSocketServer;
    }

    public Set<Integer> getTouchClients() {
        return mWebSocketClients;
    }


    private void serverOnRead(int socketId, String data) {
        Log.d(TAG, "web socket server Receive client String:" + data);

        if (data.startsWith(Command.CheckVersion)) {  // first cmd
            CheckVer clientVersion = Command.getCheckVersion(data);

            Log.d(TAG, "Client Version:" + clientVersion); //need be back

            String verCodec = Command.setServerVersionCodec(MIR_SERVER_VERSION, mEncoderCodecSupportType);

            sendData(socketId, verCodec);
            Log.e("colin", "colin start time02 --- pad start PlayerDecoder check version");
        } else if (data.startsWith(Command.Start)) {
            ClientIpCodec ipCodec = Command.getClientIpCodec(data);
            if (ipCodec.start) {
                String remoteIp = ipCodec.remoteIp;
                Log.d(TAG, "onCommand: connect touch remote ip:" + remoteIp);
                mEncoderCodecType = ipCodec.encoderCodecType;
                Log.d(TAG, "Encoder Codec Type:" + mEncoderCodecType);
                //开启播放页面，创建播放surface
                prepare(remoteIp);
                Log.e("colin", "colin start time03 --- pad start PlayerDecoder prepare:" + remoteIp);
            }

        } else if (data.startsWith(Command.SetWH)) {
            FrameWH frameWH = Command.getFrameWH(data);
            if (frameWH.setWH) {
                //设置分辨率
                mFrameWidth = frameWH.frameWidth;
                mFrameHeight = frameWH.frameHeight;
                setUiHw();
                Log.e("colin", "colin start time04 --- pad start PlayerDecoder setUiHw:"
                        + mFrameWidth + " X " + mFrameHeight);
            }

        } else if (data.startsWith(Command.Bye)) {
            if (mContext instanceof Service) {
                ((Service) mContext).stopSelf();
            }
        }
    }


    /**
     * 初始化并创建web socket server
     * 用于控制，和传输触控事件
     */
    private void initWebSocketServer() {
        mWebSocketServer = new WebSocketServer(PortKey.PORT_HTTP_DATA, new RequestCallback() {
            @Override
            public void onRead(int socketId, String s) {
                serverOnRead(socketId, s);
            }

            @Override
            public void onRead(int socketId, byte[] bytes) {

            }

            @Override
            public void ping(int socketId, String cmd) {
                //Log.d(TAG, "web socket server receive ping--- " + cmd);
            }

            @Override
            public void pong(int socketId, String cmd) {
                Log.d(TAG, "web socket server pong---" + cmd);
                mWatchDogStatus--;
            }

            @Override
            public void onConnectState(int socketId, ConnectState connectState) {
                Log.d(TAG, "create WebSocketServer onConnectState --- " + connectState);
                if (connectState == ConnectState.CONNECT) {
                    mWebSocketClients.add(socketId);

                    //startHeartBeat();//开始心跳
                } else if (connectState == ConnectState.DISCONNECT) {
                    mWebSocketClients.remove(socketId);
                } else if (connectState == ConnectState.ERROR) {
                    if (playerListener != null)
                        playerListener.onError(CODE_SOCKET_ERROR, "get socket error");
                }
            }
        });

        mWebSocketServer.open();
    }


    /**
     * 初始化视频数据 socketServer 端
     * socketServer 建立在同屏控制的控制方
     */
    private void initSocketServer() {
        //just client send pdu to server
        mSocketServer = new StreamChannelSink(PortKey.PORT_SOCKET_VIDEO, new StreamSinkCallback() {
            @Override
            public void onData(byte[] data) {

            }

            @Override
            public void onAudioFrame(MediaCodec.BufferInfo bufferInfo, ByteBuffer byteBuffer) {

            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onVideoFrame(MediaCodec.BufferInfo bufferInfo, ByteBuffer byteBuffer) {
                Log.d(TAG, "onData: onVideoFrame bufferInfo:" + bufferInfo.size);
                Log.d(TAG, "onData: onVideoFrame byteBuffer size:" + byteBuffer.remaining());
                setVideoData(bufferInfo, byteBuffer);
            }

            @Override
            public void onConnectState(ConnectState connectState) {
                Log.d(TAG, "create  videoSocket onConnectState --- " + connectState);
                if (connectState != ConnectState.CONNECT) {
                    mirServerStop(6);
                    if (playerListener != null)
                        playerListener.onError(CODE_SOCKET_ERROR, "get socket error");
                }
            }
        });
    }


    private void prepare(String ip) {
        if (DLNACommonUtil.checkPermission(mContext) || mSurface == null) {
            Log.d(TAG, "prepare: start type with mySelf UI");
            PlayerActivity.obtainPlayer(mContext, this, ip);
        } else {
            Log.d(TAG, "prepare: start type with custom UI");
            start(mSurface);
        }
    }

    void onDestroySurface() {
        mirServerStop(9);
    }

    private void checkDecoderSupportCodec() {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            // 判断是否为编码器，是则直接进入下一次循环
            if (codecInfo.isEncoder()) {
                continue;
            }

            // 如果是解码器，判断是否支持Mime类型
            String[] types = codecInfo.getSupportedTypes();
            for (String type : types) {
                if (type.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_AVC)) {
                    mEncoderCodecSupportType |= Command.CODEC_AVC_FLAG;
                    continue;
                }
                if (type.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_HEVC)) {
                    mEncoderCodecSupportType |= Command.CODEC_HEVC_FLAG;
                }
            }
        }
    }


    public static String bytes2HexString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        int length = b.length;
        for (int i = 0; i < length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append("0x").append(hex.toUpperCase());
            if (i < length - 1) {
                sb.append(',');
            }
        }
        return sb.toString();
    }

}