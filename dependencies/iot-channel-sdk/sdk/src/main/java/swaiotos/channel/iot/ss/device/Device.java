package swaiotos.channel.iot.ss.device;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;

import swaiotos.channel.iot.ss.controller.DeviceState;

public class Device<T extends DeviceInfo> implements Parcelable {
    private final String mLsid;
    private T mInfo = null;
    private DeviceState mDeviceState;
    private int status;    // 上下线状态 0:下线 1:上线

    public Device(String lsid, T info, DeviceState deviceState, int status) {
        this.mLsid = lsid;
        this.mInfo = info;
        this.mDeviceState = deviceState;
        this.status = status;
    }

    public Device(Parcel in) {
        mLsid = in.readString();
        int info = in.readInt();
        if (info > 0) {
            String infoJson = in.readString();
            if (!TextUtils.isEmpty(infoJson)) {
//                DeviceInfo deviceInfo = JSONObject.parseObject(infoJson).getString("clazzName");//.parseObject(infoJson, DeviceInfo.class);
                String clazzName =  JSONObject.parseObject(infoJson).getString("clazzName");
                try {
                    Class<T> clazz = (Class<T>) Class.forName(clazzName);
                    mInfo = JSONObject.parseObject(infoJson, clazz);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mInfo = null;
            }
        } else {
            mInfo = null;
        }
//        mInfo = in.readParcelable(getClass().getClassLoader());
        int state = in.readInt();
        if (state > 0) {
            String stateJson = in.readString();
            if (!TextUtils.isEmpty(stateJson))
                mDeviceState = DeviceState.parse(stateJson);//JSONObject.parseObject(stateJson, DeviceState.class);
//        mDeviceState = in.readParcelable(DeviceState.class.getClassLoader());
        }
        status = in.readInt();
    }

    public final T getInfo() {
        return mInfo;
    }

    public final String getLsid() {
        return mLsid;
    }

    public DeviceState getDeviceState() {
        return mDeviceState;
    }

    public void setDeviceState(DeviceState state) {
        mDeviceState = state;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int s) {
        this.status = s;
    }

    public final String encode() {
        JSONObject object = new JSONObject();
        object.put("id", mLsid);
        if (mInfo != null)
            object.put("info", mInfo.encode());
        if (mDeviceState != null)
            object.put("state", mDeviceState.encode());
        object.put("status", status);
        return object.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Device device = (Device) o;
        return mLsid.equals(device.mLsid);
    }

    @Override
    public int hashCode() {
        return mLsid.hashCode();
    }

    @Override
    public String toString() {
        return encode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mLsid);
        dest.writeInt(mInfo != null ? 1 : 0);
        if (mInfo != null) {
            dest.writeString(mInfo.encode());
        }
        dest.writeInt(mDeviceState != null ? 1 : 0);
        if (mDeviceState != null) {
            dest.writeString(mDeviceState.encode());
        }
        dest.writeInt(status);
    }

    public static final Creator<Device> CREATOR = new Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel source) {
            return new Device(source);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };
}
