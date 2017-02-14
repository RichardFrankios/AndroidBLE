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
