package com.wsj.androidble;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.wsj.blesdk.BleManager;
import com.wsj.blesdk.utils.LogUtil;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BleManager.getInstance().initializeBle(this);
    }

    public void closeBle(View view) {
        LogUtil.i(TAG,"关闭蓝牙");
        BleManager.getInstance().closeBluetooth();
    }

    public void openBle(View view) {
        LogUtil.d(TAG,"打开蓝牙");
        BleManager.getInstance().openBluetooth();
    }
}
