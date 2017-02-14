package com.wsj.blesdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;

import com.wsj.blesdk.utils.LogUtil;

import java.util.HashMap;
import java.util.Map;

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

    // 设备集合.
    private Map<String , BluetoothDevice> mDevices = new HashMap<>();
    // 当前设备
    private String mCurDeviceAddress;

    private BleListener mListener;

    private boolean mIsScaning = false;

    // 设备名称过滤
    private String mNamePrefixFilter = null;

    private int mCurentBleutoothState = STATE_DISCONNECTED;

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

    public BleManager setListener(BleListener listener) {
        mListener = listener;
        return this;
    }
    public BleManager setNameFilter(final String namePrefix){
        mNamePrefixFilter = namePrefix;
        return this;
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

    /**
     * 开始扫描设备.
     */
    public boolean startBleScan() {
        LogUtil.logFunc(TAG);
        if (!mIsInitialized || !isBluetoothNormal())
            return false;
        BluetoothDevice device = null;
        // 判断是否存在当前设备.
        if (mCurentBleutoothState == STATE_CONNECTED){
            if (mCurDeviceAddress != null && mDevices.get(mCurDeviceAddress) != null){
                device = mDevices.get(mCurDeviceAddress);
            }else {
                mCurDeviceAddress = null;
                mCurentBleutoothState = STATE_DISCONNECTED;
            }
        }
        // clear
        mDevices.clear();
        if (device != null){
            mDevices.put(mCurDeviceAddress,device);
            if (mListener != null){
                mListener.onDiscoverDevice(device.getName(),device.getAddress());
            }
        }
        if (mIsScaning){
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            SystemClock.sleep(50);
        }

        mBluetoothAdapter.startLeScan(mLeScanCallback);
        mIsScaning = true;
        return true;
    }

    /**
     * 停止扫描设备.
     */
    public boolean stopBleScan() {
        LogUtil.logFunc(TAG);
        if (!mIsInitialized || !isBluetoothNormal())
            return false;
        if (mIsScaning)
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        mIsScaning = false;
        return true;
    }
    // BLE 扫描回调.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            // LogUtil.d(TAG,"发现设备 : " + device.getName());
            if (shouldSaveDevice(device)){
                mDevices.put(device.getAddress(),device);
                if (mListener != null)
                    mListener.onDiscoverDevice(device.getName(),device.getAddress());
            }

        }
    };

    /**
     * 是否保存该设备.<br>
     *     (1) 对设备进行条件过滤.
     *     (2) 去除重复设备.
     * @param device
     * @return
     */
    private boolean shouldSaveDevice(BluetoothDevice device) {
        final String name = device.getName();
        final String address = device.getAddress();

        if (!mDevices.containsKey(address)){
            if (mNamePrefixFilter != null){
                if (name != null && name.startsWith(mNamePrefixFilter))
                    return true;
            }else {
                return true;
            }
        }
        return false;
    }


    /**
     * 蓝牙是否正常,
     */
    private boolean isBluetoothNormal() {
        return mIsInitialized
                && (mBluetoothAdapter != null)
                && (mBluetoothAdapter.isEnabled());
    }
    /**
     * 本机是否支持 BLE
     */
    private boolean isSupportBle() {
        return mContext.getPackageManager().
                hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }



}













