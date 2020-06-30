package com.swaiotos.skymirror.sdk.manager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.Surface;

import com.swaiotos.skymirror.sdk.capture.DLNASocketManager;
import com.swaiotos.skymirror.sdk.reverse.IPlayerInitListener;
import com.swaiotos.skymirror.sdk.reverse.IPlayerListener;

/**
 * @ClassName: DeviceControllerManager
 * @Description: java类作用描述
 * @Author: lfz
 * @Date: 2020/3/18 13:42
 */
public class DeviceControllerManager {

    public static final int ORIENTATION_PORTRAIT = 2;
    public static final int ORIENTATION_HORIZONTAL = 1;
    private DLNASocketManager manager;

    private DeviceControllerManager() {}

    public static DeviceControllerManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public void init() {
        manager = DLNASocketManager.getInstance();
    }

    public void destory() {
        manager.destroy();
    }

    public void startScreenCapture(Context context, String ip, int resultCode, Intent data, Class<? extends  Service> service) {
        manager.startForegroundScreenCapture(context, ip, resultCode, data, service);
    }

    public void startScreenCapture(Context context, String ip, int width, int height, int resultCode, Intent data, Class<? extends  Service> service) {
        manager.startForegroundScreenCapture(context, width, height, ip, resultCode, data, service);
    }

    public void startScreenCapture(Context context, String ip, Class<? extends  Service> service) {
        manager.startForegroundScreenCapture(context, ip, service);
    }

    public void startScreenCapture(Context context, String ip, int width, int height, Class<? extends  Service> service) {
        manager.startForegroundScreenCapture(context, width, height, ip, service);
    }

    public void stopScreenCapture(Context context, Class<? extends Service> service) {
        manager.stopForegroundScreenCapture(context, service);
    }

    public void startReverseScreenPlayer(Context context, Class<? extends Service> service) {
        manager.startReverseScreenPlayer(context, service);
    }

    public void setReverseInitListener(IPlayerInitListener listener) {
        manager.setReverseInitListener(listener);
    }

    public void startReverseScreenPlayer(Context context, Surface surface, Class<? extends Service> service) {
        manager.startReverseScreenPlayer(context, service, surface);
    }

    public void stopReverseScreenPlayer(Context context, Class<? extends Service> service) {
        manager.stopReverseScreenPlayer(context, service);
    }

    public void setReverseScreePlayerListener(IPlayerListener listener) {
        manager.setReverseScreePlayerListener(listener);
    }

    public void sendMotionEvent(MotionEvent motionEvent) {
        manager.sendMotionEvent(motionEvent);
    }


    public static class InstanceHolder {
        private static DeviceControllerManager INSTANCE = new DeviceControllerManager();
    }

}
