package swaiotos.channel.iot.ss.controller;

import java.util.concurrent.TimeoutException;

import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.session.Session;

/**
 * The interface Controller.
 *
 * @ClassName: IController
 * @Author: lu
 * @CreateDate: 2020 /4/13 2:42 PM
 * @Description:
 */
public interface Controller {

    /**
     * 打开指定sid的Session
     *
     * @param lsid    the target screenid
     * @param timeout the timeout
     * @return the session
     * @throws Exception the exception
     */
    Session connect(String lsid, long timeout) throws Exception;

    Session connect(Device device, long timeout) throws Exception;


    /**
     * Close session.
     *
     * @param target the session
     * @throws Exception the exception
     */
    void disconnect(Session target) throws Exception;

    /**
     * 获取指定session设备中指定client的版本号
     *
     * @param target  the target
     * @param client  对应client的clientID
     * @param timeout
     * @return the client version
     * @throws TimeoutException the timeout exception
     */
    int getClientVersion(Session target, String client, long timeout) throws Exception;

    DeviceInfo getDeviceInfo() throws Exception;

}
