package swaiotos.channel.iot.ss.session;

import android.os.RemoteException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import swaiotos.channel.iot.utils.ipc.ParcelableObject;

/**
 * @ClassName: SessionManagerImpl
 * @Author: lu
 * @CreateDate: 2020/4/13 2:49 PM
 * @Description:
 */
public class SessionManagerImpl implements SessionManagerClient {
    private ISessionManagerService mService;
    private Map<OnMySessionUpdateListener, IOnMySessionUpdateListener> mOnMySessionUpdateListeners = new LinkedHashMap<>();
    private Map<OnSessionUpdateListener, IOnSessionUpdateListener> mServerSessionOnUpdateListeners = new LinkedHashMap<>();
    private Map<OnSessionUpdateListener, IOnSessionUpdateListener> mConnectedSessionOnUpdateListeners = new LinkedHashMap<>();

    public SessionManagerImpl() {
    }

    @Override
    public void setService(ISessionManagerService service) {
        mService = service;
    }

    @Override
    public Session getMySession() throws Exception {
        ParcelableObject<Session> session = mService.getMySession();
        if (session.code == 0 && session.object != null) {
            return session.object;
        }
        throw new Exception(session.extra);
    }

    @Override
    public Session getConnectedSession() throws Exception {
        ParcelableObject<Session> session = mService.getConnectedSession();
        if (session.code == 0 && session.object != null) {
            return session.object;
        }
        throw new Exception(session.extra);
    }

    @Override
    public void addConnectedSessionOnUpdateListener(final OnSessionUpdateListener listener) throws Exception {
        synchronized (mConnectedSessionOnUpdateListeners) {
            if (!mConnectedSessionOnUpdateListeners.containsKey(listener)) {
                IOnSessionUpdateListener l = new IOnSessionUpdateListener.Stub() {
                    @Override
                    public void onSessionConnect(Session session) throws RemoteException {
                        listener.onSessionConnect(session);
                    }

                    @Override
                    public void onSessionUpdate(Session session) throws RemoteException {
                        listener.onSessionUpdate(session);
                    }

                    @Override
                    public void onSessionDisconnect(Session session) throws RemoteException {
                        listener.onSessionDisconnect(session);
                    }
                };
                mConnectedSessionOnUpdateListeners.put(listener, l);
                mService.addConnectedSessionOnUpdateListener(l);
            }
        }
    }

    @Override
    public void removeConnectedSessionOnUpdateListener(OnSessionUpdateListener listener) throws Exception {
        synchronized (mConnectedSessionOnUpdateListeners) {
            IOnSessionUpdateListener l = mConnectedSessionOnUpdateListeners.get(listener);
            if (l != null) {
                mService.removeConnectedSessionOnUpdateListener(l);
                mConnectedSessionOnUpdateListeners.remove(listener);
            }
        }
    }

    @Override
    public void addOnMySessionUpdateListener(final OnMySessionUpdateListener listener) throws Exception {
        synchronized (mOnMySessionUpdateListeners) {
            if (!mOnMySessionUpdateListeners.containsKey(listener)) {
                IOnMySessionUpdateListener l = new IOnMySessionUpdateListener.Stub() {
                    @Override
                    public void onMySessionUpdate(Session mySession) throws RemoteException {
                        listener.onMySessionUpdate(mySession);
                    }
                };
                mOnMySessionUpdateListeners.put(listener, l);
                mService.addOnMySessionUpdateListener(l);
            }
        }
    }

    @Override
    public void removeOnMySessionUpdateListener(OnMySessionUpdateListener listener) throws Exception {
        synchronized (mOnMySessionUpdateListeners) {
            IOnMySessionUpdateListener l = mOnMySessionUpdateListeners.get(listener);
            if (l != null) {
                mService.removeOnMySessionUpdateListener(l);
                mOnMySessionUpdateListeners.remove(listener);
            }
        }
    }

    @Override
    public void addServerSessionOnUpdateListener(final OnSessionUpdateListener listener) throws Exception {
        synchronized (mServerSessionOnUpdateListeners) {
            if (!mServerSessionOnUpdateListeners.containsKey(listener)) {
                IOnSessionUpdateListener l = new IOnSessionUpdateListener.Stub() {
                    @Override
                    public void onSessionConnect(Session session) throws RemoteException {
                        listener.onSessionConnect(session);
                    }

                    @Override
                    public void onSessionUpdate(Session session) throws RemoteException {
                        listener.onSessionUpdate(session);
                    }

                    @Override
                    public void onSessionDisconnect(Session session) throws RemoteException {
                        listener.onSessionDisconnect(session);
                    }
                };
                mServerSessionOnUpdateListeners.put(listener, l);
                mService.addServerSessionOnUpdateListener(l);
            }
        }
    }

    @Override
    public void removeServerSessionOnUpdateListener(OnSessionUpdateListener listener) throws Exception {
        synchronized (mServerSessionOnUpdateListeners) {
            IOnSessionUpdateListener l = mServerSessionOnUpdateListeners.get(listener);
            if (l != null) {
                mService.removeServerSessionOnUpdateListener(l);
                mServerSessionOnUpdateListeners.remove(listener);
            }
        }
    }

    @Override
    public void close() {
        synchronized (mOnMySessionUpdateListeners) {
            Collection<IOnMySessionUpdateListener> values = mOnMySessionUpdateListeners.values();
            for (IOnMySessionUpdateListener listener : values) {
                try {
                    mService.removeOnMySessionUpdateListener(listener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            mOnMySessionUpdateListeners.clear();
        }
        synchronized (mServerSessionOnUpdateListeners) {
            Collection<IOnSessionUpdateListener> values = mServerSessionOnUpdateListeners.values();
            for (IOnSessionUpdateListener listener : values) {
                try {
                    mService.removeServerSessionOnUpdateListener(listener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            mServerSessionOnUpdateListeners.clear();
        }
        synchronized (mConnectedSessionOnUpdateListeners) {
            Collection<IOnSessionUpdateListener> values = mConnectedSessionOnUpdateListeners.values();
            for (IOnSessionUpdateListener listener : values) {
                try {
                    mService.removeConnectedSessionOnUpdateListener(listener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            mConnectedSessionOnUpdateListeners.clear();
        }
    }

    @Override
    public List<Session> getServerSessions() throws Exception {
        return mService.getServerSessions();
    }

    @Override
    public boolean available(Session session, String channel) throws Exception  {
        return mService.available(session, channel);
    }
}
