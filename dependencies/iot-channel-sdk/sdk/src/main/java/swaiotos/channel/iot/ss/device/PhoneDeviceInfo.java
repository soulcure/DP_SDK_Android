package swaiotos.channel.iot.ss.device;

import android.os.Parcel;

/**
 * @ClassName: PhoneDeviceInfo
 * @Author: lu
 * @CreateDate: 2020/4/22 2:46 PM
 * @Description:
 */
public class PhoneDeviceInfo extends DeviceInfo {
    public String imei;             //串号 例如：*#06#
    public String mAccount;         //酷开账号
    public String mNickName;        //酷开账号昵称
    public String mModel;           //型号
    public String mChip;            //机芯
    public String mSize;            //大小

    public PhoneDeviceInfo(String imei,
                           String account,
                           String nickName,
                           String model,
                           String chip,
                           String size) {
        this.imei = imei;
        this.mAccount = account;
        this.mNickName = nickName;
        this.mModel = model;
        this.mChip = chip;
        this.mSize = size;
    }

    public PhoneDeviceInfo() {
    }

    PhoneDeviceInfo(Parcel source) {
        this.imei = source.readString();
        this.mAccount = source.readString();
        this.mNickName = source.readString();
        this.mModel = source.readString();
        this.mChip = source.readString();
        this.mSize = source.readString();
    }

    @Override
    public TYPE type() {
        return TYPE.PHONE;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imei);
        dest.writeString(mAccount);
        dest.writeString(mNickName);
        dest.writeString(mModel);
        dest.writeString(mChip);
        dest.writeString(mSize);
    }

    @Override
    public String encode() {
        return "PhoneDeviceInfo[\n"
                + "imei:" + imei + "\n"
                + "account:" + mAccount + "\n"
                + "nickName:" + mNickName + "\n"
                + "model:" + mModel + "\n"
                + "chip:" + mChip + "\n"
                + "size:" + mSize
                + "]";
    }

    public static final Creator<PhoneDeviceInfo> CREATOR = new Creator<PhoneDeviceInfo>() {
        @Override
        public PhoneDeviceInfo createFromParcel(Parcel source) {
            return new PhoneDeviceInfo(source);
        }

        @Override
        public PhoneDeviceInfo[] newArray(int size) {
            return new PhoneDeviceInfo[size];
        }
    };
}
