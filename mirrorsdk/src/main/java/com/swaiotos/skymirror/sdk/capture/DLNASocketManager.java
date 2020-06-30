package com.swaiotos.skymirror.sdk.capture;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;

import com.skyworth.dpclientsdk.WebSocketClient;
import com.skyworth.dpclientsdk.WebSocketServer;
import com.swaiotos.skymirror.sdk.constant.Constants;
import com.swaiotos.skymirror.sdk.data.DeviceInfo;
import com.swaiotos.skymirror.sdk.reverse.IPlayerInitListener;
import com.swaiotos.skymirror.sdk.reverse.IPlayerListener;
import com.swaiotos.skymirror.sdk.reverse.MotionEventUtil;
import com.swaiotos.skymirror.sdk.reverse.PlayerDecoder;
import com.swaiotos.skymirror.sdk.reverse.ReverseCaptureService;
import com.swaiotos.skymirror.sdk.util.SpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DLNASocketManager {

    public static final String STATUS_START = "START";
    public static final String STATUS_STOP = "STOP";

    private static DLNASocketManager instance = null;
    private WebSocketClient request;

    private DLNASocketManager() {
    }

    private DeviceInfo mConnectDevice;
    private List<DeviceInfo> mDevices = new ArrayList<>();
    private String connectIp = "";
    private boolean isScreenCastMode = false;

    public static DLNASocketManager getInstance() {
        if (null == instance) {
            instance = new DLNASocketManager();
        }
        return instance;
    }

    /**
     * 开始镜像
     *
     * @param mContext
     * @param remoteip
     */
    public void startScreenCapture(Context mContext, String remoteip) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent startServerIntent = new Intent(mContext, MirClientService.class);
            startServerIntent.setAction("START");
            startServerIntent.putExtra(Constants.SERVER_IP, remoteip);
            mContext.startService(startServerIntent);
        }
    }

    public void startScreenCapture(Context mContext, String remoteip, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent startServerIntent = new Intent(mContext, MirClientService.class);
            startServerIntent.setAction("START");
            startServerIntent.putExtra(Constants.REQUEST_CODE, resultCode);
            startServerIntent.putExtra(Constants.INTENT, data);
            startServerIntent.putExtra(Constants.SERVER_IP, remoteip);
            mContext.startService(startServerIntent);
        }
    }

    public void unBindNfc(Context context, String nfcId) {
        SpUtil.putString(context, nfcId, "");
    }

    public void destroy() {
        destroySocket();
    }

    private void destroySocket() {
        instance = null;
    }

    public void stopForegroundScreenCapture(Context context, Class<? extends Service> service) {
        try {
            Intent startServerIntent = new Intent(context, service);
            startServerIntent.setAction("STOP");
            context.stopService(startServerIntent);
        } catch (Exception | Error e) {
            Log.e("STOP_SERVICE", "stopForegroundScreenCapture: " + e.getMessage());
        }
    }

    public void startForegroundScreenCapture(Context context, String ip, int resultCode, Intent data, Class<? extends Service> service) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent startServerIntent = new Intent(context, service);
            startServerIntent.setAction("START");
            startServerIntent.putExtra("resultCode", resultCode);
            startServerIntent.putExtra("intent", data);
            startServerIntent.putExtra("serverip", ip);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startServerIntent);
            } else {
                context.startService(startServerIntent);
            }
        }

    }

    public void startForegroundScreenCapture(Context context, int width, int height, String ip, int resultCode, Intent data, Class<? extends Service> service) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent startServerIntent = new Intent(context, service);
            startServerIntent.setAction("START");
            startServerIntent.putExtra("resultCode", resultCode);
            startServerIntent.putExtra("intent", data);
            startServerIntent.putExtra("width", width);
            startServerIntent.putExtra("height", height);
            startServerIntent.putExtra("serverip", ip);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startServerIntent);
            } else {
                context.startService(startServerIntent);
            }
        }

    }

    public void startForegroundScreenCapture(Context context, int width, int height, String ip, Class<? extends Service> service) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent startServerIntent = new Intent(context, service);
            startServerIntent.setAction("START");
            startServerIntent.putExtra(Constants.SERVER_IP, ip);
            startServerIntent.putExtra("width", width);
            startServerIntent.putExtra("height", height);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startServerIntent);
            } else {
                context.startService(startServerIntent);
            }
        }
    }

    public void startForegroundScreenCapture(Context context, String ip, Class<? extends Service> service) {
        if (/*Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP*/true) {
            Intent startServerIntent = new Intent(context, service);
            startServerIntent.setAction("START");
            startServerIntent.putExtra(Constants.SERVER_IP, ip);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startServerIntent);
            } else {
                context.startService(startServerIntent);
            }
        }
    }


    private DeviceInfo formatDevice(DeviceInfo info, String recv) {
        if (TextUtils.isEmpty(recv)) return info;
        try {
            JSONObject jsonObject = new JSONObject(recv);
            if (jsonObject == null) return info;
            String param = jsonObject.getString("param");
            if (TextUtils.isEmpty(param)) return info;
            JSONObject paramObject = new JSONObject(param);
            String deviceModel = paramObject.getString("model");
            if (!TextUtils.isEmpty(deviceModel))
                info.setDeviceModel(deviceModel);
            String deviceMac = paramObject.getString("mac");
            if (!TextUtils.isEmpty(deviceMac))
                info.setDeviceMac(deviceMac);
            String deviceChip = paramObject.getString("chip");
            if (!TextUtils.isEmpty(deviceChip))
                info.setDeviceChip(deviceChip);
            String deviceSkymid = paramObject.getString("skymid");
            if (!TextUtils.isEmpty(deviceSkymid))
                info.setDeviceSkymid(deviceSkymid);
            String deviceVersion = paramObject.getString("version");
            if (!TextUtils.isEmpty(deviceVersion))
                info.setDeviceVersion(deviceVersion);
            String deviceId = paramObject.getString("activeId");
            if (!TextUtils.isEmpty(deviceId))
                info.setDeviceID(deviceId);
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        return info;
    }

    public void startReverseScreenPlayer(Context context, Class<? extends Service> service) {
        Intent intent = new Intent(context, service);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public void stopReverseScreenPlayer(Context context, Class<? extends Service> service) {
        Intent intent = new Intent(context, service);
        context.stopService(intent);
    }

    public void startReverseScreenPlayer(Context context, Class<? extends Service> service, Surface surface) {
        Intent intent = new Intent(context, service);
        intent.putExtra("surface", surface);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public void setReverseScreePlayerListener(IPlayerListener listener) {
        //PlayerDecoder.setStatusListener(listener);
        PlayerDecoder decoder = ReverseCaptureService.getDecoder();
        if (decoder == null)
            return;
        decoder.setStatusListener(listener);
    }


    public void sendMotionEvent(MotionEvent motionEvent) {
        PlayerDecoder decoder = ReverseCaptureService.getDecoder();
        if (decoder == null) {
            Log.d("PlayerDecoder", "sendMotionEvent: decoder is null");
            return;
        }
        //CommandChannelRequest channelRequest = decoder.getTouch();
        WebSocketServer touchServer = decoder.getTouch();
        Set<Integer> touchClients = decoder.getTouchClients();
        if (touchServer != null) {
            for (int client : touchClients) {
                String json = MotionEventUtil.formatTouchEvent(motionEvent, 1);
                Log.d("PlayerDecoder", "sendMotionEvent json---:" + json);
                //使用send byte[] 接口专门发送触控事件
                touchServer.sendData(client, json.getBytes());
            }

        } else {
            Log.d("PlayerDecoder", "sendMotionEvent: channelRequest is null");
        }
    }

    public void setReverseInitListener(IPlayerInitListener listener) {
        ReverseCaptureService.setReverseInitListener(listener);
    }
}
