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
    public void onBleDiscover(final String name, final String address);

    /**
     * 设备连接.
     * @param address  设备地址
     */
    public void onBleConnected(final String address);

    /**
     * 断开设备.
     * @param address  设备地址
     */
    public void onBleDisconnected(final String address);

    /**
     * 发现服务.
     * @param address  设备地址.
     */
    public void onBleDiscoverServices(final String address);

    /**
     * 发生错误
     * @param code 错误代码.
     */
    public void onBleError(final int code);
}
