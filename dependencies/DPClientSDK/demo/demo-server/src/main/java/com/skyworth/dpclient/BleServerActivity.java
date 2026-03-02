package com.skyworth.dpclient;


import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.skyworth.dpclientsdk.ble.BlePdu;
import com.skyworth.dpclientsdk.ble.BluetoothServer;
import com.skyworth.dpclientsdk.ble.BluetoothServerCallBack;
import com.skyworth.dpclientsdk.MACUtils;

import java.util.ArrayList;
import java.util.List;

public class BleServerActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "server";
    /** Android 12+ 蓝牙运行时权限请求码 */
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1001;

    private BluetoothServer bleServer;

    private TextView tv_msg;
    private TextView tv_info;
    private TextView input;
    private String mac;
    private BluetoothDevice mBluetoothDevice;

    BluetoothServerCallBack callBack = new BluetoothServerCallBack() {

        @Override
        public void onMessageShow(BlePdu blePdu, BluetoothDevice device) {
            mBluetoothDevice = device;
            tv_msg.setText(new String(blePdu.body));
        }

        @Override
        public void onStartSuccess(String deviceName) {
            tv_info.setText("开启ble广播成功:" + mac);
            input.setText(deviceName);
        }

        @Override
        public void onStartFail(String info) {
            tv_info.setText(info + "&mac:" + mac);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_server);
        mac = MACUtils.getMac(this).replace(":", "");

        input = findViewById(R.id.ip_edit);
        input.setText(mac);

        tv_msg = findViewById(R.id.tv_msg);
        tv_info = findViewById(R.id.tv_info);

        findViewById(R.id.btn_server_open).setOnClickListener(this);
        findViewById(R.id.btn_server_close).setOnClickListener(this);
        findViewById(R.id.btn_response).setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_server_open) {
            if (ensureBluetoothPermissions()) {
                openBleServer();
            }
        } else if (id == R.id.btn_server_close) {
            if (bleServer != null) {
                bleServer.removeService();
            }
        } else if (id == R.id.btn_response) {
            responseMsg();
        }
    }

    /**
     * 确保已具备蓝牙所需权限（Android 12+ 需 BLUETOOTH_CONNECT、BLUETOOTH_ADVERTISE）。
     * 若未授权则请求权限，授权后再执行 openBleServer。
     */
    private boolean ensureBluetoothPermissions() {
        // 使用字符串避免 compileSdkVersion < 31 时找不到常量
        String connect = "android.permission.BLUETOOTH_CONNECT";
        String advertise = "android.permission.BLUETOOTH_ADVERTISE";
        List<String> missing = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, connect) != PackageManager.PERMISSION_GRANTED) {
            missing.add(connect);
        }
        if (ContextCompat.checkSelfPermission(this, advertise) != PackageManager.PERMISSION_GRANTED) {
            missing.add(advertise);
        }
        if (missing.isEmpty()) {
            return true;
        }
        ActivityCompat.requestPermissions(this, missing.toArray(new String[0]), REQUEST_BLUETOOTH_PERMISSIONS);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_BLUETOOTH_PERMISSIONS) return;
        for (int r : grantResults) {
            if (r != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "需要蓝牙权限才能开启 BLE 服务", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            openBleServer();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openBleServer() {
        bleServer = new BluetoothServer(this, callBack);
        bleServer.openBle();
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void responseMsg() {
        String msg = "{\n" +
                "    \"id\":\"EFC91A36-F545-4053-A91C-C367C0155D57\",\n" +
                "    \"client-source\":\"ss-clientID-mobile-iphone\",\n" +
                "    \"client-target\":\"ss-clientID-appstore_12345\",\n" +
                "    \"type\":\"TEXT\",\n" +
                "    \"source\":\"{\"id\":\"47377d7249a042889545d8421249f308\",\"extra\":{\"im-cloud\":\"47377d7249a042889545d8421249f308\",\"stream-local\":\"10.136.108.230\",\"address-local\":\"10.136.108.230\",\"im-local\":\"10.136.108.230:34000\"}}\",\n" +
                "    \"extra\":{\n" +
                "        \"force-sse\":\"true\"\n" +
                "    },\n" +
                "    \"content\":\"{\"cmd\":\"\",\"type\":\"SCREEN_SHOT\",\"param\":\"\"}\",\n" +
                "    \"reply\":false,\n" +
                "    \"target\":\"{\"id\":\"de17ec2f155f4687b78052ea65943004\",\"extra\":{}}\"\n" +
                "}";
        if (mBluetoothDevice != null)
            bleServer.sendMessage(msg, BlePdu.TEMP_CMD, mBluetoothDevice);
    }


}