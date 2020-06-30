package swaiotos.channel.iot.ss.device;

import swaiotos.channel.iot.ss.SSChannel;

/**
 * @ClassName: IDeviceManagerClient
 * @Author: lu
 * @CreateDate: 2020/4/18 5:14 PM
 * @Description:
 */
public interface DeviceManagerClient extends DeviceManager, SSChannel.IClient<IDeviceManagerService> {
}
