package com.skyworth.dpclient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_websocket).setOnClickListener(this);
        findViewById(R.id.btn_tcp).setOnClickListener(this);
        findViewById(R.id.btn_udp).setOnClickListener(this);
        findViewById(R.id.btn_ble).setOnClickListener(this);
        findViewById(R.id.btn_fast_ble).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_websocket) {
            startActivity(new Intent(this, WebSocketServerActivity.class));
        } else if (id == R.id.btn_tcp) {
            startActivity(new Intent(this, TcpServerActivity.class));
        } else if (id == R.id.btn_udp) {
            startActivity(new Intent(this, UdpServerActivity.class));
        } else if (id == R.id.btn_ble) {
            startActivity(new Intent(this, BleServerActivity.class));
        } else if (id == R.id.btn_fast_ble) {
            startActivity(new Intent(this, FastBleServerActivity.class));
        }
    }
}
