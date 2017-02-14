package com.wsj.blesdk;

/**
 * 蓝牙设备信息类 <br>
 * Created by WSJ on 2017/2/14.
 */

public class BluetoothInfo {
    private String address;
    private String name;

    public BluetoothInfo(String address, String name) {
        this.address = address;
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
