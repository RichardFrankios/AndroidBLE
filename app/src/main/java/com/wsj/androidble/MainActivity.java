package com.wsj.androidble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.wsj.blesdk.BleListener;
import com.wsj.blesdk.BleManager;
import com.wsj.blesdk.utils.LogUtil;

import java.util.List;

public class MainActivity extends AppCompatActivity implements BleListener {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BleManager.getInstance()
                .setListener(this)
                .initializeBle(this);
    }

    public void closeBle(View view) {
        LogUtil.i(TAG,"关闭蓝牙");
        BleManager.getInstance().closeBluetooth();
    }

    public void openBle(View view) {
        LogUtil.d(TAG,"打开蓝牙");
        BleManager.getInstance().openBluetooth();
    }

    public void startLeScan(View view) {
        LogUtil.d(TAG,"开始扫描");
        BleManager.getInstance().startBleScan();
    }

    public void stopLeScan(View view) {
        LogUtil.d(TAG,"停止扫描");
        BleManager.getInstance().stopBleScan();
    }
    // 测试机
    final String address = "87:83:01:15:98:25";
    public void connectBle(View view) {
        LogUtil.d(TAG,"连接设备 : " + address);
        BleManager.getInstance().connectBleDevice(address);
    }

    public void disconnectBle(View view) {
        LogUtil.d(TAG,"断开设备 : " + address);
        BleManager.getInstance().disconnectBleDevice();
    }
    public void discoverServices(View view) {
        LogUtil.d(TAG,"查找服务");
        BleManager.getInstance().discoverServices();
    }






    @Override
    public void onBleDiscover(String name, String address) {
        LogUtil.d(TAG,"发现设备 : " + address);
    }

    @Override
    public void onBleConnected(String address) {
        LogUtil.d(TAG,"设备已连接 : " + address);
    }

    @Override
    public void onBleDisconnected(String address) {
        LogUtil.d(TAG,"设备已断开 : " + address);
    }

    @Override
    public void onBleDiscoverServices(String address) {
        LogUtil.d(TAG,"查找服务成功");

        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtil.d(TAG,"查询服务");
                List<BluetoothGattService> bleServices = BleManager.getInstance().getBleServices();
                if (bleServices == null)
                    LogUtil.e(TAG,"服务列表为空");
                else
                    for (BluetoothGattService service :bleServices) {
                        LogUtil.d(TAG,"服务 : " + service.getUuid().toString());
                        List<BluetoothGattCharacteristic> bleCharacteristics =
                                BleManager.getInstance()
                                        .getBleCharacteristics(service.getUuid().toString());
                        if (bleCharacteristics == null){
                            LogUtil.e(TAG,"没有特征值");
                        }else {
                            for (BluetoothGattCharacteristic ch : bleCharacteristics) {
                                LogUtil.d(TAG,"CH : " + ch.getUuid().toString());
                            }
                        }
                    }
            }
        }).start();
    }

    @Override
    public void onBleError(int code) {
        LogUtil.e(TAG,"发生错误 : " + String.format("%x",code));
    }



}
