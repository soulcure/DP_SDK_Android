package swaiotos.channel.iot.ss.channel.stream;

import swaiotos.channel.iot.ss.SSChannel;

/**
 * @ClassName: IStreamChannelClient
 * @Author: lu
 * @CreateDate: 2020/4/18 5:13 PM
 * @Description:
 */
public interface IStreamChannelClient extends IStreamChannel, SSChannel.IClient<IStreamChannelService> {
}
