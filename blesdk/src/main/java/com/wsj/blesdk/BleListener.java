package com.wsj.blesdk;

/**
 * Ble 监听.<br>
 * Created by WSJ on 2017/2/14.
 */

public interface BleListener {
    /**
     * 扫描到一台设备.
     * @param name     设备名称
     * @param address  设备地址
     */
    public void onDiscoverDevice(final String name, final String address);
}
