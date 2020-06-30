package swaiotos.channel.iot.ss.channel.im;

import android.os.RemoteException;

import java.util.concurrent.TimeoutException;

import swaiotos.channel.iot.utils.ipc.ParcelableObject;

/**
 * @ClassName: IIMChannelImpl
 * @Author: lu
 * @CreateDate: 2020/4/1 8:36 PM
 * @Description:
 */
public class IIMChannelImpl implements IIMChannelClient {
    IIMChannelService mService;
    private IMMessageCallbackHandler mHandler;

    public IIMChannelImpl() {
        mHandler = new IMMessageCallbackHandler();
    }

    @Override
    public void setService(IIMChannelService service) {
        mService = service;
    }

    @Override
    public void send(IMMessage message, IMMessageCallback callback) throws Exception {
        if (callback != null) {
            mHandler.add(message, callback);
        }
        try {
            mService.send(message, mHandler.getMessenger());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(IMMessage message) throws Exception {
        try {
            send(message, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IMMessage sendSync(IMMessage message, IMMessageCallback callback, long timeout) throws Exception {
        if (callback != null) {
            mHandler.add(message, callback);
        }
        try {
            ParcelableObject<IMMessage> reply = mService.sendSync(message, mHandler.getMessenger(), timeout);
            if (reply.code == 0) {
                return reply.object;
            }
            throw new TimeoutException(reply.extra);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public IMMessage sendSync(IMMessage message, long timeout) throws Exception {
        return sendSync(message, IMMessageCallback.defaultIMMessageCallback, timeout);
    }
}
