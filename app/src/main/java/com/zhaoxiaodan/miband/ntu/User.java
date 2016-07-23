package com.zhaoxiaodan.miband.ntu;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Chongkai on 2016/3/12.
 */
public class User {
    String name = "";
    String batteryCapacity = "";
    String myHeartRate = "";
    String imageicon = "";
    String mac = "";
    String responseTime = "";
    String deviceName = "";
    int imageId;
    BluetoothDevice device;
    boolean connect;
    int flag;
    int id;
    String heartRateAVG = "";
    int range, range1;
    int dpmStatus;  //0:斷線;1:正常;2:異常
    int avgStatus;  //0:斷線;1:正常;2:異常


    public User(int id, String name, String myHeartRate, String batteryCapacity, String mac, String responseTime, int imageId, int flag, int range, int range1, String deviceName,int dpmStatus,int avgStatus) {
        setId(id);
        setName(name);
        setMac(mac);
        setMyHeartRate(myHeartRate);
        setResponseTime(responseTime);
        setImageId(imageId);
        setBatteryCapacity(batteryCapacity);
        setFlag(flag);
        setRange(range);
        setRange1(range1);
        setDeviceName(deviceName);
        setDpmStatus(dpmStatus);
        setAvgStatus(avgStatus);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBatteryCapacity() {
        return batteryCapacity;
    }

    public void setBatteryCapacity(String batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }

    public String getMyHeartRate() {
        return myHeartRate;
    }

    public void setMyHeartRate(String myHeartRate) {
        this.myHeartRate = myHeartRate;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }


    public String getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(String responseTime) {
        this.responseTime = responseTime;
    }


    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public boolean isConnect() {
        return connect;
    }

    public void setConnect(boolean connect) {
        this.connect = connect;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getHeartRateAVG() {
        return heartRateAVG;
    }

    public void setHeartRateAVG(String heartRateAVG) {
        this.heartRateAVG = heartRateAVG;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public int getRange1() {
        return range1;
    }

    public void setRange1(int range1) {
        this.range1 = range1;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getDpmStatus() {
        return dpmStatus;
    }

    public void setDpmStatus(int dpmStatus) {
        this.dpmStatus = dpmStatus;
    }

    public int getAvgStatus() {
        return avgStatus;
    }

    public void setAvgStatus(int avgStatus) {
        this.avgStatus = avgStatus;
    }

}
