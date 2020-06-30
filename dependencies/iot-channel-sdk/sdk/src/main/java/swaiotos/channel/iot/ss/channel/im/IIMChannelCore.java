package swaiotos.channel.iot.ss.channel.im;

import swaiotos.channel.iot.ss.session.Session;

/**
 * @ClassName: IIMChannelCore
 * @Author: lu
 * @CreateDate: 2020/4/13 2:25 PM
 * @Description:
 */
public interface IIMChannelCore {
    void send(IMMessage message, IMMessageCallback callback) throws Exception;

    void send(IMMessage message) throws Exception;
}
