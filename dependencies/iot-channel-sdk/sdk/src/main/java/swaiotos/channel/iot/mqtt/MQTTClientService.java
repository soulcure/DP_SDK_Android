package swaiotos.channel.iot.mqtt;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

/**
 * @ClassName: MQTTClientService
 * @Author: lu
 * @CreateDate: 2020/3/14 6:13 PM
 * @Description: MQTT设备端服务
 */
public abstract class MQTTClientService extends IntentService {
    public static final String KEY_MESSAGE = "message";

    public MQTTClientService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String message = intent.getStringExtra(KEY_MESSAGE);
        onReceive(message);
    }

    protected abstract void onReceive(String message);

    protected final void send(String message) {
        Bundle data = new Bundle();
        data.putString("", message);
        MQTTHelper.send(this, data, "", null);
    }
}

