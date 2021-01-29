// IbindDevices.aidl
package swaiotos.channel.iot.ss.device;

import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.IBindResult;
import swaiotos.channel.iot.ss.device.IUnBindResult;
import swaiotos.channel.iot.ss.device.IOnDeviceChangedListener;
import swaiotos.channel.iot.ss.device.IDeviceBindListener;
import swaiotos.channel.iot.ss.device.IDeviceInfoUpdateListener;

interface IDeviceAdminManagerService {
    void startBind(String accessToken, String bindCode,in IBindResult result, long time);
    void addOnDeviceChangedListener(in IOnDeviceChangedListener listener);
    void removeOnDeviceChangedListener(in IOnDeviceChangedListener listener);
    void unBindDevice(String accessToken, String lsid, int type, in IUnBindResult result);
    void addDeviceBindListener(in IDeviceBindListener listener);
    void removeDeviceBindListener(in IDeviceBindListener listener);
    void addDeviceInfoUpdateListener(in IDeviceInfoUpdateListener listener);
    void removeDeviceInfoUpdateListener(in IDeviceInfoUpdateListener listener);

    List<Device> updateDeviceList();
}
