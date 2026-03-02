package com.skyworth.dpclient;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.skyworth.dpclientsdk.ConnectState;
import com.skyworth.dpclientsdk.StreamSourceCallback;
import com.skyworth.dpclientsdk.UdpClient;
import com.skyworth.dpclientsdk.local.DeviceUtil;


public class UdpClientActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "client";

    private UdpClient udpClient;
    private EditText input;

    private int count;

    private StreamSourceCallback callback = new StreamSourceCallback() {
        @Override
        public void onConnectState(ConnectState state) {
            Log.d(TAG, "StreamSourceState: " + state);
        }

        @Override
        public void onData(String data) {

        }

        @Override
        public void onData(byte[] data) {

        }

        @Override
        public void ping(String msg) {

        }

        @Override
        public void pong(String msg) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udp_client);

        input = findViewById(R.id.editText);
        input.setText(DeviceUtil.getLocalIPAddress(this));
        //input.setText("192.168.0.103");

        findViewById(R.id.btn_open).setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);
        findViewById(R.id.btn_bytes).setOnClickListener(this);
        findViewById(R.id.btn_string).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_open) {
            String ip = input.getText().toString();
            udpClient = new UdpClient(ip, 39999, callback);
            udpClient.open();
        } else if (id == R.id.btn_close) {
            if (udpClient != null) {
                udpClient.close();
                udpClient = null;
            }
        } else if (id == R.id.btn_bytes) {
            if (udpClient != null) {
                byte[] bytes = new byte[]{'a', 'b', 'c'};
                udpClient.sendData(bytes);
            }
        } else if (id == R.id.btn_string) {
            if (udpClient != null) {
                String str = "我是一个小宝宝" + count++;
                udpClient.sendData(str);
            }
        }
    }


}
