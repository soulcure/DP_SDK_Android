package swaiotos.channel.iot.ss.channel.stream;

import android.os.RemoteException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import swaiotos.channel.iot.ss.session.Session;

/**
 * @ClassName: StreamChannelImpl
 * @Author: lu
 * @CreateDate: 2020/4/9 7:10 PM
 * @Description:
 */
public class StreamChannelImpl implements IStreamChannelClient {
    private static class ReceiverProxy extends IStreamChannelReceiver.Stub {
        private Receiver mReceiver;

        ReceiverProxy(Receiver receiver) {
            this.mReceiver = receiver;
        }

        @Override
        public void onReceive(byte[] data) throws RemoteException {
            mReceiver.onReceive(data);
        }
    }

    private static class SenderProxy implements Sender {
        private IStreamChannelSender mSender;
        private SenderMonitor mSenderMonitor;
        public final Session mSession;
        public final int mChannelId;

        SenderProxy(Session session, int channelId, IStreamChannelSender sender) {
            mSender = sender;
            mSession = session;
            mChannelId = channelId;
            try {
                mSender.setSenderMonitor(monitor);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void setSenderMonitor(SenderMonitor monitor) {
            mSenderMonitor = monitor;
        }

        @Override
        public boolean available() {
            try {
                return mSender.available();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public void send(byte[] data) throws Exception {
            mSender.send(data);
        }

        private IStreamChannelSenderMonitor monitor = new IStreamChannelSenderMonitor.Stub() {
            @Override
            public void onAvailableChanged(boolean available) throws RemoteException {
                if (mSenderMonitor != null) {
                    mSenderMonitor.onAvailableChanged(available);
                }
            }
        };

        static String getId(Session session, int channelId) {
            return session.getId() + "-" + channelId;
        }

        static String getId(SenderProxy proxy) {
            return getId(proxy.mSession, proxy.mChannelId);
        }
    }

    private Map<Receiver, Integer> receivers = new LinkedHashMap<>();
    private Map<String, Sender> senders = new LinkedHashMap<>();

    private IStreamChannelService mService;

    public StreamChannelImpl() {
    }

    @Override
    public void setService(IStreamChannelService service) {
        mService = service;
    }

    @Override
    public int openReceiver(final Receiver receiver) {
        synchronized (receivers) {
            if (receivers.containsKey(receiver)) {
                return receivers.get(receiver);
            }
            int id = -1;
            try {
                ReceiverProxy proxy = new ReceiverProxy(receiver);
                id = mService.open(proxy);
                receivers.put(receiver, Integer.valueOf(id));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return id;
        }
    }

    @Override
    public void closeReceiver(int channelId) {
        synchronized (receivers) {
            Integer v = Integer.valueOf(channelId);
            if (receivers.containsValue(v)) {
                Set<Map.Entry<Receiver, Integer>> entries = receivers.entrySet();
                for (Map.Entry<Receiver, Integer> entry : entries) {
                    if (entry.getValue().equals(v)) {
                        receivers.remove(entry.getKey());
                        break;
                    }
                }
                try {
                    mService.close(channelId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Sender openSender(Session session, int channelId) {
        String id = SenderProxy.getId(session, channelId);
        synchronized (senders) {
            if (senders.containsKey(id)) {
                return senders.get(id);
            }
            try {
                IStreamChannelSender sender = mService.openSender(session, channelId);
                SenderProxy proxy = new SenderProxy(session, channelId, sender);
                senders.put(id, proxy);
                return proxy;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void closeSender(Sender sender) {
        try {
            SenderProxy proxy = (SenderProxy) sender;
            String id = SenderProxy.getId(proxy);
            synchronized (senders) {
                if (senders.containsKey(id)) {
                    senders.remove(id);
                }
            }
            mService.closeSender(proxy.mSender);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
