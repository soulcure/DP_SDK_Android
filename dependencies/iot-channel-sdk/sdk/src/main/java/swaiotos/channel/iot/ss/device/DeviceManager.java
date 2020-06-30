package swaiotos.channel.iot.ss.device;

import java.util.List;

/**
 * @ClassName: Devices
 * @Author: colin
 * @CreateDate: 2020/4/15 19:48 PM
 * @Description:
 */
public interface DeviceManager {
    /**
     * 设备列表查询
     *
     * @return List<Device> 返回所有绑定的设备
     * @throws Exception the exception
     */
    List<Device> getDevices() throws Exception;

    Device getCurrentDevice() throws Exception;
}
