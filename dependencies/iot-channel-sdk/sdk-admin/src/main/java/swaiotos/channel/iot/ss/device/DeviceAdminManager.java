package swaiotos.channel.iot.ss.device;

import android.os.RemoteException;

import java.util.List;

/**
 * @ClassName: Devices
 * @Author: colin
 * @CreateDate: 2020/4/15 19:48 PM
 * @Description:
 */
public interface DeviceAdminManager extends DeviceManager {
    interface OnBindResultListener {
        /**
         * 绑定过程成功回调
         */
        void onSuccess(String bindCode, Device device);

        /**
         * 绑定过程失败回调
         */
        void onFail(String bindCode, String errorType, String msg);
    }

    interface unBindResultListener {
        /**
         * 解绑成功回调
         */
        void onSuccess(String lsid);

        /**
         * 解绑失败回调
         */
        void onFail(String lsid, String errorType, String msg);
    }

    interface OnDeviceChangedListener {
        void onDeviceOffLine(Device device);

        void onDeviceOnLine(Device device);

        void onDeviceUpdate(Device device);
    }

    interface OnDeviceBindListener {
        void onDeviceBind(String lsid);

        void onDeviceUnBind(String lsid);
    }

    interface OnDeviceInfoUpdateListener {
        void onDeviceInfoUpdate(List<Device> devices);
    }

    interface OnDeviceInfoUpdateSDK {
        void addDeviceInfoUpdateListener(OnDeviceInfoUpdateListener listener) throws RemoteException;

        void removeDeviceInfoUpdateListener(OnDeviceInfoUpdateListener listener) throws RemoteException;
    }

    interface OnDeviceInfoUpdateCore {
        void onDeviceInfoUpdateListener(OnDeviceInfoUpdateListener listener);

        void onDeviceInfoUpdateList();
    }

    void startBind(String accessToken, String bindCode, OnBindResultListener listener, long time) throws Exception;
    void unBindDevice(String accessToken, String lsid, int type, unBindResultListener listener) throws Exception;

    void addOnDeviceChangedListener(OnDeviceChangedListener listener) throws RemoteException;

    void removeOnDeviceChangedListener(OnDeviceChangedListener listener) throws RemoteException;

    void addDeviceBindListener(OnDeviceBindListener listener) throws RemoteException;

    void removeDeviceBindListener(OnDeviceBindListener listener) throws RemoteException;



    List<Device> updateDeviceList();
}
