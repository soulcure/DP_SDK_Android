package swaiotos.channel.iot.ss.device;

import swaiotos.channel.iot.ss.SSChannel;

/**
 * @ClassName: IDeviceAdminManagerClient
 * @Author: lu
 * @CreateDate: 2020/4/18 5:27 PM
 * @Description:
 */
public interface DeviceAdminManagerClient extends DeviceAdminManager, SSChannel.IClient<IDeviceAdminManagerService> {
    void setDeviceManager(DeviceManager manager);
}
