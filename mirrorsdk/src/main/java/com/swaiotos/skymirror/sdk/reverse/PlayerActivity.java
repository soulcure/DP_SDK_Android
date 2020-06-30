package com.swaiotos.skymirror.sdk.reverse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Parcel;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.swaiotos.skymirror.sdk.R;
import com.skyworth.dpclientsdk.WebSocketClient;
import com.swaiotos.skymirror.sdk.manager.DeviceControllerManager;
import com.swaiotos.skymirror.sdk.util.DLNACommonUtil;

/**
 * @ClassName: PlayerActivity
 * @Description: java类作用描述
 * @Author: lfz
 * @Date: 2020/4/15 9:38
 */
public class PlayerActivity extends Activity implements TextureView.SurfaceTextureListener {

    private String TAG = PlayerActivity.class.getSimpleName();
    private final int STATUS_PORTAIT = 1;
    private final int STATUS_LANDSCAPE = 0;

    private TextureView textureView;
    private static PlayerDecoder mDecoder;
    private boolean mStopFlag = false;
    private int deviceWidth;
    private int deviceHeight;
    private int mViewWidth = 1080;
    private int mViewHeight = 1920;
    private int screenWidth = 1080;
    private int screenHeight = 1920;
    private int mAngle = 0;
    private int mLastRotion = Surface.ROTATION_0 % 2;
    private int orientation = Configuration.ORIENTATION_LANDSCAPE;
    private static String remoteIp;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        DeviceControllerManager.getInstance().init();

        deviceWidth = dm.widthPixels;
        deviceHeight = dm.heightPixels;
        Log.d(TAG, "deviceWidth :" + deviceWidth + ",deviceHeight:" + deviceHeight);
        hideSystemUI();
        setContentView(R.layout.activity_player);
        textureView = findViewById(R.id.playerView);
        textureView.setSurfaceTextureListener(this);
        textureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e(TAG, "onTouch: 11111111 --- " + event.getAction());
                return sendMotionEvent(v, event);
            }
        });
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (deviceWidth > deviceHeight) {
            Log.d(TAG, "STATUS_LANDSCAPE");
            orientation = Configuration.ORIENTATION_LANDSCAPE;
            //setConfiguration(Configuration.ORIENTATION_LANDSCAPE);
        } else {
            Log.d(TAG, "STATUS_PORTAIT");
            int oldWidth = deviceWidth;
            //deviceWidth = deviceHeight;
            //deviceHeight = oldWidth;
            orientation = Configuration.ORIENTATION_PORTRAIT;
            //setConfiguration(Configuration.ORIENTATION_PORTRAIT);
        }

    }

    private boolean sendMotionEvent(View v, MotionEvent motionEvent) {

        DeviceControllerManager.getInstance().sendMotionEvent(motionEvent);//send

        double xpos = motionEvent.getX();
        double ypos = motionEvent.getY();

        Parcel motionEventParcel = Parcel.obtain();
        String jsonTxt = "{";
        jsonTxt += "\"action\":";
        jsonTxt += motionEvent.getAction();
        jsonTxt += ",";

        jsonTxt += "\"touch\":[";
        int cnt = motionEvent.getPointerCount();
        Log.d(TAG, "original " + motionEvent.toString());
        double Xrate = ((float) deviceWidth) / 1080.0;
        double Yrate = ((float) deviceHeight) / 1920.0;
        double XViewRate = (((float) mViewWidth) / ((float) 1080.0));
        double YViewRate = (((float) mViewHeight) / ((float) 1920.0));
        MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[cnt];
        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[cnt];

        for (int index = 0; index < cnt; index++) {
            MotionEvent.PointerCoords pointerCoord = new MotionEvent.PointerCoords();
            pointerCoord.pressure = 1;

            Log.e(TAG, "TXT --- onTouch: x --- " + motionEvent.getX(index) + " --- y ---" + motionEvent.getY(index));

            if (mViewWidth > mViewHeight) {
                mAngle = 0;
            } else {
                mAngle = 1;
            }

            Log.e(TAG, "sendMotionEvent: 1111111111 --- " + mAngle);

            if (mAngle == 0) {
                xpos = motionEvent.getX(index) / (mViewWidth / screenWidth);
                ypos = motionEvent.getY(index) / (mViewHeight / screenHeight);
            } else {
                xpos = motionEvent.getX(index) / (mViewWidth / screenWidth);
                ypos = motionEvent.getY(index) / (mViewHeight / screenWidth);
            }

            Log.d(TAG, "sendMotionEvent: 1111111112 start ---- " + xpos + " ---- " + ypos);
            Log.d(TAG, "sendMotionEvent: 1111111112 view ---- " + mViewWidth + " ---- " + mViewHeight);


            jsonTxt += "{";
            jsonTxt += "\"x\":" + (int) xpos + ",";
            jsonTxt += "\"y\":" + (int) ypos + ",";
            jsonTxt += "\"id\":" + motionEvent.getPointerId(index) + "";
            pointerCoord.x = (float) xpos;//x坐标
            pointerCoord.y = (float) ypos;//yx坐标
            pointerCoords[index] = pointerCoord;
            jsonTxt += "}";
            if (index < (cnt - 1))
                jsonTxt += ",";
            MotionEvent.PointerProperties pointerPropertie = new MotionEvent.PointerProperties();
            pointerPropertie.id = motionEvent.getPointerId(index);
            pointerPropertie.toolType = motionEvent.getToolType(index);
            pointerProperties[index] = pointerPropertie;
        }
        jsonTxt += "]";
        jsonTxt += "}";
        Log.d(TAG, "JSON TXT:" + jsonTxt);
        long when = SystemClock.uptimeMillis();
        int actionPoint = motionEvent.getAction();
        MotionEvent newMotionEvent = MotionEvent.obtain(when, when, actionPoint, cnt, pointerProperties, pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0);

        motionEventParcel.setDataPosition(0);
        newMotionEvent.writeToParcel(motionEventParcel, 0);
        //motionEventParcel.setDataPosition(0);

        Log.d(TAG, "actionPoint :" + actionPoint);
        Log.d(TAG, newMotionEvent.toString());
        Log.d(TAG, "Parcel:" + motionEventParcel.toString());

        Log.e(TAG, "sendMotionEvent: 11111111111 ---- " + motionEvent.toString());
        Log.e(TAG, "sendMotionEvent: 11111111111 ---- " + MotionEventUtil.formatTouchEvent(motionEvent, 1));
        motionEventParcel.recycle();
        return true;
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public static void obtainPlayer(Context context, PlayerDecoder decoder, String ip) {
        remoteIp = ip;
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        mDecoder = decoder;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureAvailable: " + width + " ----- " + height);

        mViewWidth = width;
        mViewHeight = height;
        mDecoder.start(new Surface(surface));
        mDecoder.setDecoderListener(listener);
        Log.e(TAG, "onSurfaceTextureAvailable: " + remoteIp);

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureSizeChanged: " + width + " ----- " + height);
        mViewWidth = width;
        mViewHeight = height;
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }


    PlayerDecoder.DecoderListener listener = new PlayerDecoder.DecoderListener() {
        @Override
        public void setStatus(String Status, PlayerDecoder decoder) {
            Log.e(TAG, "setStatus: 111");
            if (Status.equals("delete") || Status.equals("close")) {
                finish();
            }
        }

        @Override
        public void setHW(final int w, final int h, int rotate, PlayerDecoder decoder) {
            Log.d(TAG, "setUIHw w " + w + " h " + h + " angle " + rotate);
            Log.e(TAG, "SetUIHw: cccccccccccc ");
            mAngle = rotate;

            //电视端分辨率最高1920*1080，假设：电视屏幕宽dw,高dh
            // if( w > h ) ---- 手机横屏  电视需横屏显示，//理论全屏充满
            //                                         check(w < 1080 )
            // if( w < h)  ---- 手机竖屏  电视需竖屏显示，取 1920 和 1080 中最小值 1080 做为高，宽需等比例缩放
            //                                         高1080 宽 （w）

            if (h > w) { //设备竖屏,竖屏显示
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //取电视宽高最小值
                        if (deviceWidth > deviceHeight) {
                            //电视画面高 = （电视屏幕宽和电视屏幕高的最小值）
                            //电视画面宽 = 传入宽 * （传入高 / 电视屏幕高和电视屏幕高的最小值）
                            //换算成16/9
                            float desWH = (deviceHeight * 1.0f / h);
                            int newWidth = (int) (w * desWH - w * desWH % 16);
                            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(newWidth, deviceHeight);
                            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                            textureView.setLayoutParams(layoutParams);
                            textureView.requestLayout();
                        } else {//反之
                            float desWH = (deviceWidth * 1.0f / w);
                            int newHeight = (int) (h * desWH - w * desWH % 16);
                            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(deviceWidth, newHeight);
                            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                            textureView.setLayoutParams(layoutParams);
                            textureView.requestLayout();
                        }
                    }
                });
            }

            if (w > h) {//设备横屏，横屏显示
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //取电视宽高最小值
                        if (DLNACommonUtil.checkPermission(PlayerActivity.this)) {
                            if (deviceWidth > deviceHeight) {
                                float desWH = (deviceWidth * 1.0f / w);
                                int newHeight = (int) (h * desWH - w * desWH % 16);
                                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(deviceWidth, newHeight);
                                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                                textureView.setLayoutParams(layoutParams);
                                textureView.requestLayout();
                            } else {//反之
                                float desWH = (deviceHeight * 1.0f / h);
                                int newWidth = (int) (w * desWH - w * desWH % 16);
                                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(newWidth, deviceHeight);
                                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                                textureView.setLayoutParams(layoutParams);
                                textureView.requestLayout();
                            }
                        } else {
                            if (deviceWidth > deviceHeight) {
                                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(deviceWidth, deviceHeight - deviceHeight % 16);
                                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                                textureView.setLayoutParams(layoutParams);
                                textureView.requestLayout();
                            } else {
                                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(deviceHeight, deviceWidth);
                                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                                textureView.setLayoutParams(layoutParams);
                                textureView.setRotation(270);
                                textureView.requestLayout();
                            }
                        }

                    }
                });
            }


            /*if (w > h) {
                mAngle = 0;
                screenWidth = w;
                screenHeight = h;
                Log.e(TAG, "SetUIHw: aaaaaaaaaaaaa ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //textureView.setLayoutParams(new RelativeLayout.LayoutParams(1920, 1080));
                        textureView.setScaleX((float) mViewHeight / (float) mViewWidth);//1080/1920            1920 resize to 1080 right
                        textureView.setScaleY((float) mViewWidth / (float) mViewHeight);//1920/886
                        textureView.setRotation(270);
                        textureView.requestLayout();
                    }
                });

            } else {
                mAngle = 1;
                screenWidth = h;
                screenHeight = w;
                Log.e(TAG, "SetUIHw: bbbbbbbbbbbbbb ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //textureView.setLayoutParams(new RelativeLayout.LayoutParams(1080, 1920));
                        textureView.setRotation(0);
                        int aa = 0;
                        if (h > 1080) {
                            float desWH = 1080f / 1920;
                            int baseWidth = (int) (1080 * desWH);
                            aa = baseWidth - (baseWidth % 16);
                        }
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(608, deviceWidth);
                        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                        textureView.setLayoutParams(layoutParams);
                        //textureView.setScaleX((float)mViewHeight/(float)mViewWidth);//1080/1920            1920 resize to 1080 right
                        //textureView.setScaleY((float)mViewWidth/(float)mViewHeight);//1920/886
                        textureView.setScaleX(1);
                        textureView.setScaleY(1);
                        textureView.requestLayout();
                    }
                });

            }*/

            //textureView.setLayoutParams(new RelativeLayout.LayoutParams(w, h));


            //textureView.setLayoutParams(new RelativeLayout.LayoutParams(h, w));

            Log.e(TAG, "SetUIHw: hw ---- 1 --- " + textureView.getWidth() + " --- " + textureView.getHeight());

            Log.e(TAG, "SetUIHw: deviceWidth --- " + deviceWidth + " --- deviceHeight --- " + deviceHeight);


            //textureView.setScaleX(deviceWidth / h);
            //textureView.setScaleY(deviceHeight / w);


//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//                            h, w);
//                    params.addRule(RelativeLayout.CENTER_IN_PARENT);
//                    textureView.setLayoutParams(params);
//                    textureView.requestLayout();
//                    //textureView.setRotation(0);
//                    //textureView.setScaleX(deviceWidth / h);
//                    //textureView.setScaleY(deviceHeight / w);
//                }
//            });


            Log.e(TAG, "SetUIHw: hw ---- 2 --- " + textureView.getWidth() + " --- " + textureView.getHeight());

            //mViewWidth = w;
            //mViewHeight = h;
            //ChangeLayoutOnBroadCast();
        }
    };

    private void ChangeLayoutOnBroadCast() {
        runOnUiThread(new Runnable() {
            public void run() {
                int w = mViewWidth;
                int h = mViewHeight;
                textureView.setRotation(270);
                textureView.setScaleX((float) mViewHeight / (float) mViewWidth);//1080/1920            1920 resize to 1080 right
                textureView.setScaleY((float) mViewWidth / (float) mViewHeight);//1920/886
                Log.d(TAG, "ChangeLayoutOnBroadCast initial w " + mViewWidth + " h " + mViewHeight);
                setUILayout(w, h);
            }
        });
    }

    public void setUILayout(int w, int h) {
        int width;
        int height;
        Log.d(TAG, "setUILayout mLastRotion " + mLastRotion);
        Log.d(TAG, "setUILayout view width:" + textureView.getWidth() + ",Height:" + textureView.getHeight());
        int SCREEN_WIDTH = 1080;
        int SCREEN_HEIGHT = 1920;


        int or = getResources().getConfiguration().orientation;

        if (mAngle == 0) {

        } else {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    SCREEN_HEIGHT, SCREEN_WIDTH);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            textureView.setLayoutParams(params);
        }
        if (true) {
            return;
        }

        if (or == Configuration.ORIENTATION_LANDSCAPE) {

        }

        if (or == Configuration.ORIENTATION_PORTRAIT) {

        }

        if (mLastRotion == STATUS_LANDSCAPE) {//tv landscape 0
            if (h > w)//phone portait
            {
                height = SCREEN_HEIGHT;
                width = (SCREEN_HEIGHT * w) / h;
                Log.d(TAG, "setUILayout new w " + width + " h " + height);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        width, height);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                textureView.setLayoutParams(params);
//                textureView.setRotation(0);
//                textureView.setScaleX(1.0f);
//                textureView.setScaleY(1.0f);
                Log.d(TAG, "setUILayout setUILayout 0");
            } else {//phone landscape,tv landscape
                width = SCREEN_WIDTH;
                height = (SCREEN_WIDTH * h) / w;
                Log.d(TAG, "setUILayout new w " + width + " h " + height);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        width, height);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                textureView.setLayoutParams(params);
//                textureView.setRotation(0);
//                textureView.setScaleX(1.0f);
//                textureView.setScaleY(1.0f);
//                mTextureView.setScaleX((float)h/(float)w);//1080/1920            1920 resize to 1080 right
//                mTextureView.setScaleY((float)h/(float)w);//1920/886            886 rize to 886?
                Log.d(TAG, "setUILayout setUILayout 1");
            }
        } else {
            if (h > w) {//tv portait,,phone portait
                height = SCREEN_WIDTH;
                width = (SCREEN_WIDTH * w) / h;

                Log.d(TAG, "setUILayout new w " + width + " h " + height);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        width, height);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                textureView.setLayoutParams(params);
//                mTextureView.setRotation(270);
//                textureView.setScaleX(1.0f);
//                //mTextureView.setScaleY((float)height/(float)width);//1080/1920            1920 resize to 1080 right
//                //mTextureView.setScaleY((float)SCREEN_WIDTH/(float)SCREEN_HEIGHT);//1080/1920            1920 resize to 1080 right
//                textureView.setScaleX(1.0f);//new modified
                Log.d(TAG, "setUILayout setUILayout 2");
            } else {//tv portait,,phone landscape
//                width = 1920;
//                height = (1920 * h) / w;
                width = SCREEN_HEIGHT;
                height = (SCREEN_HEIGHT * h) / w;
                Log.d(TAG, "setUILayout new w " + width + " h " + height);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        width, height);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                textureView.setLayoutParams(params);
//                mTextureView.setRotation(270);
//                textureView.setScaleX(1.0f);
//                textureView.setScaleY(1.0f);

                Log.d(TAG, "setUILayout setUILayout 3");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDecoder.onDestroySurface();
        mStopFlag = true;
    }
}
