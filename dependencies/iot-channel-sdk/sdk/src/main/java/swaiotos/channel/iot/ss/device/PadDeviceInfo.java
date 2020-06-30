package swaiotos.channel.iot.ss.device;

import android.os.Parcel;

/**
 * @ClassName: PadDeviceInfo
 * @Author: lu
 * @CreateDate: 2020/4/22 2:46 PM
 * @Description:
 */
public class PadDeviceInfo extends DeviceInfo {
    public String activeId;             //激活ID
    public String deviceName;           //设备名称
    public String mChip, mModel, mSize; //机芯、机型、大小
    public String mAccount;             //酷开账号
    public String mNickName;            //酷开账号昵称

    public PadDeviceInfo() {
    }

    public PadDeviceInfo(String activeId,
                         String deviceName,
                         String chip,
                         String model,
                         String size,
                         String account,
                         String nickName) {
        this.activeId = activeId;
        this.deviceName = deviceName;
        this.mChip = chip;
        this.mModel = model;
        this.mSize = size;
        this.mAccount = account;
        this.mNickName = nickName;
    }

    PadDeviceInfo(Parcel source) {
        this.activeId = source.readString();
        this.deviceName = source.readString();
        this.mChip = source.readString();
        this.mModel = source.readString();
        this.mSize = source.readString();
        this.mAccount = source.readString();
        this.mNickName = source.readString();
    }

    @Override
    public TYPE type() {
        return TYPE.PAD;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(activeId);
        dest.writeString(deviceName);
        dest.writeString(mChip);
        dest.writeString(mModel);
        dest.writeString(mSize);
        dest.writeString(mAccount);
        dest.writeString(mNickName);
    }

    @Override
    public String encode() {
        return "PadDeviceInfo[\n"
                + "activeId:" + activeId + "\n"
                + "deviceName:" + deviceName + "\n"
                + "chip:" + mChip + "\n"
                + "model:" + mModel + "\n"
                + "size:" + mSize + "\n"
                + "account:" + mAccount + "\n"
                + "nickName:" + mNickName
                + "]";
    }

    public static final Creator<PadDeviceInfo> CREATOR = new Creator<PadDeviceInfo>() {
        @Override
        public PadDeviceInfo createFromParcel(Parcel source) {
            return new PadDeviceInfo(source);
        }

        @Override
        public PadDeviceInfo[] newArray(int size) {
            return new PadDeviceInfo[size];
        }
    };
}
