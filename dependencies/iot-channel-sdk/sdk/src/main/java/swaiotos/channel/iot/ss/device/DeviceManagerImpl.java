package swaiotos.channel.iot.ss.device;

import java.util.List;

public class DeviceManagerImpl implements DeviceManagerClient {

    private IDeviceManagerService mService;

    public DeviceManagerImpl() {
    }

    @Override
    public void setService(IDeviceManagerService service) {
        mService = service;
    }

    @Override
    public List<Device> getDevices() throws Exception {
        return mService.getDevices();
    }

    @Override
    public List<Device> getDeviceOnlineStatus() throws Exception {
        return mService.getDeviceOnlineStatus();
    }

    @Override
    public Device getCurrentDevice() throws Exception {
        return mService.getCurrentDevice();
    }
}
