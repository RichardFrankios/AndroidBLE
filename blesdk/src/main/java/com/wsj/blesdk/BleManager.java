package com.wsj.blesdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.wsj.blesdk.utils.LogUtil;

/**
 * BLE Manager <br>
 * Created by WSJ on 2017/2/14.
 */

public class BleManager {
    // TAG
    private static final String TAG = "BleManager";

    /**
     * 单例对象 .
     */
    private static BleManager ourInstance = new BleManager();
    private Context mContext;

    /**
     * BleManager 是否初始化.
     */
    private boolean mIsInitialized = false;

    /**
     * 本地蓝牙适配器 .
     */
    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothManager mBluetoothManager;

    private BluetoothGatt mCurBluetoothGatt;

    /* 蓝牙连接状态. */
    private static final int STATE_DISCONNECTED  = 0x00;
    private static final int STATE_CONNECTING    = 0x01;
    private static final int STATE_CONNECTED     = 0x02;
    private static final int STATE_DISCONNECTING = 0x03;

    /**
     * 获取 BleManager 实例.
     */
    public static BleManager getInstance() {
        return ourInstance;
    }

    private BleManager() {
    }

    /**
     * 初始化BleManager.
     */
    public boolean initializeBle(Context context) {
        LogUtil.logFunc(TAG);
        if (mIsInitialized) {
            return true;
        }
        if (context == null) {
            LogUtil.e(TAG, "Argument bad !!!");
            return false;
        }
        mContext = context.getApplicationContext();
        // 初始化本地蓝牙适配器
        mBluetoothManager = (BluetoothManager) mContext
                .getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            LogUtil.e(TAG, "bluetooth not supported !!!");
            return false;
        }
        // 是否支持BLE
        if (!isSupportBle()) {
            LogUtil.e(TAG, "BLE not supported !!!");
            return false;
        }
        mIsInitialized = true;
        return true;
    }

    /**
     * 打开蓝牙.
     */
    public boolean openBluetooth(){
        if (!mIsInitialized) {
            LogUtil.e(TAG,"sdk not initializeed !!!");
            return false;
        }
        if (!mBluetoothAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
        return true;
    }

    /**
     * 关闭蓝牙.
     */
    public boolean closeBluetooth() {
        if (!mIsInitialized) {
            LogUtil.e(TAG,"sdk not initializeed !!!");
            return false;
        }
        if (mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.disable();
        }
        return true;
    }
//    private boolean isBluetoothOpened() {
//
//        if (mBluetoothAdapter == null) {
//            return false;
//        }
//    }


    /**
     * 本机是否支持 BLE
     */
    private boolean isSupportBle() {
        return mContext.getPackageManager().
                hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }



}













