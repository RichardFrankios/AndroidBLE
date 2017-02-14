# 1 AndroidBLE(Android 低功耗蓝牙)

> Android BLE封装了BLE相关的操作 . 


# 2 接口

### 2.1 初始化 BleManager

	// 失败返回false
	boolean res = BleManager.getInstance().initializeBle(this);

### 2.2 打开蓝牙 openBluetooth

	boolean res = BleManager.getInstance().openBluetooth();

### 2.3 关闭蓝牙 closeBluetooth

	boolean res = BleManager.getInstance().closeBluetooth();

### 2.4 开始扫描

	boolean res = BleManager.getInstance().startBleScan();

**扫描结果回调 : onDiscoverDevice**
	
	public void onDiscoverDevice(final String name, final String address);

### 2.5 停止扫描

	boolean res = BleManager.getInstance().stopBleScan();

### 2.6 连接设备

	boolean res = BleManager.getInstance().connectBleDevice(address);
**连接成功 : onBleConnected**

	public void onBleConnected(final String address);

### 2.7 断开设备

	boolean res = BleManager.getInstance().disconnectBleDevice();

**设备断开 : onBleDisconnected**

	public void onBleDisconnected(final String address);

### 2.8 发生错误

	public void onBleError(final int code);

### 2.9 服务和特征

	// 发现服务
	boolean res = BleManager.getInstance().discoverServices();
	// 获取服务列表
	List<BluetoothGattService> bleServices = BleManager.getInstance().getBleServices();
	// 获取特征列表
	List<BluetoothGattCharacteristic> bleCharacteristics =
                                BleManager.getInstance()
                                        .getBleCharacteristics(service.getUuid().toString());
	// 设置服务UUID
	BleManager.getInstance.setServiceUuid(uuidStr);
	// 设置写特征UUID
	BleManager.getInstance.setWriteCharacteristicUuid(uuidStr);
	// 设置读特征UUID
	BleManager.getInstance.setReadCharacteristicUuid(uuidStr);

