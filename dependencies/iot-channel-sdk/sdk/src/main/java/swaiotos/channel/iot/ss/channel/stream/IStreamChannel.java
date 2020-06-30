package swaiotos.channel.iot.ss.channel.stream;

import swaiotos.channel.iot.ss.session.Session;

public interface IStreamChannel {
    interface Receiver {
        void onReceive(byte[] data);
    }

    interface SenderMonitor {
        void onAvailableChanged(boolean available);
    }

    interface Sender {
        void setSenderMonitor(SenderMonitor monitor);

        boolean available();

        void send(byte[] data) throws Exception;
    }

    int openReceiver(Receiver receiver);

    void closeReceiver(int channelId);

    Sender openSender(Session session, int channelId);

    void closeSender(Sender sender);
}
