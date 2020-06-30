package com.swaiotos.skymirror.sdk.reverse;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;


/**
 * 设备发现功能替换为云端push ,将ip、port回传，用于连接httpserver和socket
 * 反向投屏功能：
 * 此为server端，接收client端发送过来的屏幕数据、解码、播放
 * 开启反向投屏功能后service需要保证为存活状态去初始化 httpServer 等待着 httpclient 连接
 * 优化空间：
 * 1.目前开始的时候会开启多个 httpserver, 后期可以换为1个固定server通道实现连接操作
 * PS：
 */
public abstract class ReverseCaptureService extends Service {

    private final static String TAG = ReverseCaptureService.class.getSimpleName();

    private static PlayerDecoder decoder;

    protected abstract void initNotification();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static PlayerDecoder getDecoder() {
        return decoder;
    }

    private static IPlayerInitListener mListener;

    public static void setReverseInitListener(IPlayerInitListener listener) {
        mListener = listener;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initNotification();
        if (intent != null && intent.getExtras() != null) {
            Surface surface = intent.getExtras().getParcelable("surface");
            if (surface != null) {
                Log.e(TAG, "onStartCommand custom surface");
                decoder = new PlayerDecoder(this, surface);
            }
        }

        if (decoder == null) {
            decoder = new PlayerDecoder(this);
        }

        if (mListener != null) {
            mListener.onInitStatus(true);
        }

        Log.d("playerDecoder", "onStartCommand: reverseCaptureService init success");

        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (decoder != null) {
            decoder.mirServerStop(10);
            decoder = null;
        }
    }


}
