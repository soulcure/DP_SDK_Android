// IbindDevices.aidl
package swaiotos.channel.iot.ss.device;

import swaiotos.channel.iot.ss.device.Device;

interface IDeviceManagerService {
    List<Device> getDevices();
    Device getCurrentDevice();
}
