package com.wsj.blesdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;

import com.wsj.blesdk.utils.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wsj.blesdk.BleConstants.BLE_ERROR_CONNECT;
import static com.wsj.blesdk.BleConstants.BLE_ERROR_DISCONNECT;
import static com.wsj.blesdk.BleConstants.BLE_ERROR_DISCOVER_SERVICES;
import static com.wsj.blesdk.BleConstants.BLE_SUCCESS;

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

    // 相关 UUID.
    private String mCurrentGattServiceUuid ;
    private String mCurrentGattWriteCharacteristicUuid ;
    private String mCurrentGattReadCharacteristicUuid ;
    private List<BluetoothGattService> mGattServices = new ArrayList<>();

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
                mListener.onBleDiscover(device.getName(),device.getAddress());
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

    /**
     * 连接指定地址的BLE设备.
     * @param address   设备地址
     * @return
     *      是否成功发送连接请求.
     */
    public boolean connectBleDevice(final String address){
        LogUtil.logFunc(TAG);
        if (!mIsInitialized || !isBluetoothNormal()){
            return false;
        }
        if (address == null || mDevices.get(address) == null){
            return false;
        }
        if (mCurentBleutoothState == STATE_DISCONNECTING
                || mCurentBleutoothState == STATE_CONNECTING){
            return false;
        }
        // close gatt
        if (mCurDeviceAddress != null){
            if (mCurBluetoothGatt != null){
                if (mCurDeviceAddress.equals(address)){
                    return true;
                }else {
                    mCurBluetoothGatt.disconnect();
                }
            }
            mCurDeviceAddress = null;
        }
        if (mCurBluetoothGatt != null){
            mCurBluetoothGatt.close();
            mCurBluetoothGatt = null;
            SystemClock.sleep(50);
        }

        // clear data
        mGattServices = new ArrayList<>();

        // connect device
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null){
            LogUtil.e(TAG,"Device not found . Unable to connect");
            return false;
        }
        // We want to directly connect to the device , so wo are setting the autoConnect
        // Parameter false.
        mCurBluetoothGatt = device.connectGatt(mContext,false,mGattCallback);
        mCurDeviceAddress = address;
        mCurentBleutoothState = STATE_CONNECTING;
        return true;
    }

    /**
     * 断开设备连接
     */
    public boolean disconnectBleDevice(){
        LogUtil.logFunc(TAG);
        if (!mIsInitialized || !isBluetoothNormal()){
            return false;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                mCurentBleutoothState = STATE_DISCONNECTING;
                mCurBluetoothGatt.disconnect();
            }
        }).start();
        return true;
    }

    /**
     * 开始查找服务.
     */
    public boolean discoverServices(){
        if (!mIsInitialized || !isBluetoothNormal()
                || (mCurentBleutoothState != STATE_CONNECTED)){
            return false;
        }
        return mCurBluetoothGatt.discoverServices();
    }

    /**
     * 获取服务列表
     * @return
     */
    public List<BluetoothGattService> getBleServices(){
        LogUtil.logFunc(TAG);
        return mGattServices;
    }

    /**
     * 获取特征值.
     * @param serviceUuid
     * @return
     */
    public List<BluetoothGattCharacteristic> getBleCharacteristics(String serviceUuid){
        if (mGattServices == null)
            return null;
        BluetoothGattService service = getService(serviceUuid);
        if (service == null)
            return null;
        return service.getCharacteristics();
    }

    /**
     * 设置服务 UUID
     * @param uuid uuid 字符串.
     * @return
     */
    public BleManager setServiceUuid(String uuid){
        mCurrentGattServiceUuid = uuid;
        return this;
    }

    /**
     * 设置写特征值.
     * @param uuid uuid string
     * @return
     */
    public BleManager setWriteCharacteristicUuid(String uuid){
        mCurrentGattWriteCharacteristicUuid = uuid;
        return this;
    }

    /**
     * 设置读特征值.
     * @param uuid uuid string
     * @return
     */
    public BleManager setReadCharacteristicUuid(String uuid){
        mCurrentGattReadCharacteristicUuid = uuid;
        return this;
    }

//    public boolean transmitData2Device(final byte[] data) {
//
//    }

    private BluetoothGattService getService(String serviceUuid) {
        for (BluetoothGattService service : mGattServices) {
            if (service.getUuid().toString().equals(serviceUuid))
                return service;
        }
        return null;
    }
    // BLE GATT回调.
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            final String name = gatt.getDevice().getName();
            final String address = gatt.getDevice().getAddress();

            if (status == BluetoothGatt.GATT_SUCCESS){
                // operation success
                connectionStateChangeSuccess(newState, address);
            }else {
                connectionStateChangeFailed();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            servicesDiscoveredProcess(gatt, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }
    };

    /**
     * 发现服务回调.
     * @param gatt    GATT
     * @param status  状态
     */
    private void servicesDiscoveredProcess(BluetoothGatt gatt, int status) {
        LogUtil.logFunc(TAG);
        if (status == BluetoothGatt.GATT_SUCCESS){
            mGattServices = gatt.getServices();
            if (mListener != null)
                mListener.onBleDiscoverServices(mCurDeviceAddress);
        }else {
            if (mListener != null)
                mListener.onBleError(BLE_ERROR_DISCOVER_SERVICES);
        }
    }

    /**
     * 设备连接/断开失败
     */
    private void connectionStateChangeFailed() {
        int code = BLE_SUCCESS;
        switch (mCurentBleutoothState){
            case STATE_CONNECTING:
                code = BLE_ERROR_CONNECT;
                mCurentBleutoothState = STATE_CONNECTED;
                break;
            case STATE_DISCONNECTING:
                code = BLE_ERROR_DISCONNECT;
                mCurentBleutoothState = STATE_DISCONNECTED;
                break;
        }
        if (code != BLE_SUCCESS && mListener != null)
            mListener.onBleError(code);
    }

    /**
     * 设备连接/断开成功
     * @param newState  新状态
     * @param address   设备地址
     */
    private void connectionStateChangeSuccess(int newState, String address) {
        switch (newState){
            case BluetoothGatt.STATE_CONNECTED:
                mCurentBleutoothState = STATE_CONNECTED;
                mCurDeviceAddress = address;
                if (mListener != null)
                    mListener.onBleConnected(address);
                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                mCurentBleutoothState = STATE_DISCONNECTED;
                mCurDeviceAddress = null;
                mCurBluetoothGatt.close();
                mCurBluetoothGatt = null;
                if (mListener != null)
                    mListener.onBleDisconnected(address);
                break;
        }
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
                    mListener.onBleDiscover(device.getName(),device.getAddress());
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
    private boolean shouldSaveDevice(final BluetoothDevice device) {
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













