package swaiotos.channel.iot.ss.device;

import android.os.Parcel;

/**
 * @ClassName: TVDeviceInfo
 * @Author: lu
 * @CreateDate: 2020/4/22 2:44 PM
 * @Description:z
 */
public class TVDeviceInfo extends DeviceInfo {
    public String activeId;
    public  String deviceName;
    public  String mMovieSource;
    public  String mChip, mModel, mSize;
    public  String cHomepageVersion;
    public  String  MAC;
    public  String cFMode;
    public  String cTcVersion;
    public  String cPattern;
    public  String Resolution;
    public  String aSdk;
    public  String cEmmcCID;
    public  String cBrand;
    public String mNickName;        //电视的昵称


    public TVDeviceInfo(String activeId,
                        String deviceName,
                        String movieSource,
                        String chip,
                        String model,
                        String size,
                        String cHomepageVersion,
                        String MAC,
                        String cFMode,
                        String cTcVersion,
                        String cPattern,
                        String Resolution,
                        String aSdk,
                        String cEmmcCID,
                        String cBrand,
                        String nickName) {
        this.activeId = activeId;
        this.deviceName = deviceName;
        this.mMovieSource = movieSource;
        this.mChip = chip;
        this.mModel = model;
        this.mSize = size;
        this.cHomepageVersion = cHomepageVersion;
        this.MAC = MAC;
        this.cFMode = cFMode;
        this.cTcVersion = cTcVersion;
        this.cPattern = cPattern;
        this.Resolution = Resolution;
        this.aSdk = aSdk;
        this.cEmmcCID = cEmmcCID;
        this.cBrand = cBrand;
        this.mNickName = nickName;
    }

    public TVDeviceInfo() {
    }

    TVDeviceInfo(Parcel source) {
        this.activeId = source.readString();
        this.deviceName = source.readString();
        this.mMovieSource = source.readString();
        this.mChip = source.readString();
        this.mModel = source.readString();
        this.mSize = source.readString();
        this.cHomepageVersion = source.readString();
        this.MAC = source.readString();
        this.cFMode = source.readString();
        this.cTcVersion = source.readString();
        this.cPattern = source.readString();
        this.Resolution = source.readString();
        this.aSdk = source.readString();
        this.cEmmcCID = source.readString();
        this.cBrand = source.readString();
        this.mNickName = source.readString();
    }

    @Override
    public TYPE type() {
        return TYPE.TV;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(activeId);
        dest.writeString(deviceName);
        dest.writeString(mMovieSource);
        dest.writeString(mChip);
        dest.writeString(mModel);
        dest.writeString(mSize);
        dest.writeString(cHomepageVersion);
        dest.writeString(MAC);
        dest.writeString(cFMode);
        dest.writeString(cTcVersion);
        dest.writeString(cPattern);
        dest.writeString(Resolution);
        dest.writeString(aSdk);
        dest.writeString(cEmmcCID);
        dest.writeString(cBrand);
        dest.writeString(mNickName);
    }

    @Override
    public String encode() {
        return "TVDeviceInfo[\n"
                + "activeId:" + activeId + "\n"
                + "deviceName:" + deviceName + "\n"
                + "moviesource:" + mMovieSource + "\n"
                + "chip:" + mChip + "\n"
                + "model:" + mModel + "\n"
                + "size:" + mSize + "\n"
                + "cHomepageVersion:" + cHomepageVersion + "\n"
                + "MAC:" + MAC + "\n"
                + "cFMode:" + cFMode + "\n"
                + "cTcVersion:" + cTcVersion + "\n"
                + "cPattern:" + cPattern + "\n"
                + "Resolution:" + Resolution + "\n"
                + "aSdk:" + aSdk + "\n"
                + "cEmmcCID:" + cEmmcCID + "\n"
                + "cBrand:" + cBrand + "\n"
                + "nickName:" + mNickName + "]";
    }

    public static final Creator<TVDeviceInfo> CREATOR = new Creator<TVDeviceInfo>() {
        @Override
        public TVDeviceInfo createFromParcel(Parcel source) {
            return new TVDeviceInfo(source);
        }

        @Override
        public TVDeviceInfo[] newArray(int size) {
            return new TVDeviceInfo[size];
        }
    };
}
