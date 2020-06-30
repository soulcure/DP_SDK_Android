package swaiotos.channel.iot.mqtt;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;

import java.util.HashMap;
import java.util.Map;


/**
 * @ClassName: MQTTHelper
 * @Author: lu
 * @CreateDate: 2020/3/14 6:13 PM
 * @Description: MQTT服务接口
 */
public class MQTTHelper {
    public interface Callback {
        void onResult(int code, String extra);
    }

    private static final Handler HANDLER = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            MQTTHelper.Callback callback;
            int index = msg.arg1;
            synchronized (callbacks) {
                callback = callbacks.get(index);
                callbacks.remove(index);
            }
            if (callback != null) {
                callback.onResult(0, "");
            }
        }
    };

    private static final String SERVICE_ACTION = "swaiotos.intent.action.channel.iot.service.MQTT";
    private static final String PACKAGE_NAME = "swaiotos.channel.iot";
    static final String CMD_REGISTER = "register";
    static final String CMD_UNREGISTER = "unregister";
    static final String KEY_CMD = "cmd";
    static final String KEY_CLIENT = "client";


    private static int index = Integer.MIN_VALUE;
    private static Map<Integer, Callback> callbacks = new HashMap<>();

    public static void register(Context context, Bundle params, Class<? extends MQTTClientService> service, final Callback callback) {
        Bundle data = new Bundle();
        data.putParcelable(KEY_CLIENT, new ComponentName(context, service));
        if (params != null) {
            data.putAll(params);
        }
        send(context, params, CMD_REGISTER, callback);
    }

    public static void unregister(Context context, Bundle params, Class<? extends MQTTClientService> service, Callback callback) {
        Bundle data = new Bundle();
        data.putParcelable(KEY_CLIENT, new ComponentName(context, service));
        if (params != null) {
            data.putAll(params);
        }
        send(context, params, CMD_UNREGISTER, callback);
    }

    static void send(Context context, Bundle data, String cmd, Callback callback) {
        data.putString(KEY_CMD, cmd);
        send(context, data, callback);
    }

    static synchronized void send(Context context, Bundle data, final Callback callback) {
        if (index > Integer.MAX_VALUE) {
            index = Integer.MIN_VALUE;
        }
        Message message = new Message();
        message.arg1 = index++;
        message.replyTo = new Messenger(HANDLER);
        message.setData(data);
        synchronized (callbacks) {
            callbacks.put(message.arg1, callback);
        }
        Intent intent = new Intent(SERVICE_ACTION);
        intent.setPackage(PACKAGE_NAME);
        intent.putExtra("message", message);
        context.startService(intent);
    }
}
