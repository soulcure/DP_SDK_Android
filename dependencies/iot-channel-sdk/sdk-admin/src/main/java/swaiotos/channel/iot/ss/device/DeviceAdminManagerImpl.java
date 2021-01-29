package swaiotos.channel.iot.ss.device;

import android.os.RemoteException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeviceAdminManagerImpl implements DeviceAdminManagerClient,DeviceAdminManager.OnDeviceInfoUpdateSDK {
    private IDeviceAdminManagerService mService;
    private DeviceManager mDeviceManager;
    private Map<OnDeviceChangedListener, IOnDeviceChangedListener> mOnDeviceChangedListeners = new LinkedHashMap<>();
    private Map<OnDeviceBindListener, IDeviceBindListener> mOnDeviceBindListeners = new LinkedHashMap<>();
    private Map<OnDeviceInfoUpdateListener, IDeviceInfoUpdateListener> mOnDeviceInfoUpdateListeners = new LinkedHashMap<>();

    public DeviceAdminManagerImpl() {
    }

    @Override
    public void setService(IDeviceAdminManagerService service) {
        mService = service;
    }

    @Override
    public void setDeviceManager(DeviceManager manager) {
        mDeviceManager = manager;
    }

    @Override
    public void startBind(String accessToken, String bindCode, final OnBindResultListener listener, long time) throws Exception {
        mService.startBind(accessToken, bindCode, new IBindResult.Stub() {
            @Override
            public void onSuccess(String bindCode, Device device) throws RemoteException {
                listener.onSuccess(bindCode, device);
            }

            @Override
            public void onFail(String bindCode, String errorType, String msg) throws RemoteException {
                listener.onFail(bindCode, errorType, msg);
            }
        }, time);
    }

    @Override
    public void unBindDevice(String accessToken, String lsid, int type, final unBindResultListener listener) throws Exception {
        mService.unBindDevice(accessToken, lsid, type, new IUnBindResult.Stub() {
            @Override
            public void onSuccess(String lsid) throws RemoteException {
                listener.onSuccess(lsid);
            }

            @Override
            public void onFail(String lsid, String errorType, String msg) throws RemoteException {
                listener.onFail(lsid, errorType, msg);
            }
        });
    }

    @Override
    public void addOnDeviceChangedListener(final OnDeviceChangedListener listener) throws RemoteException {
        synchronized (mOnDeviceChangedListeners) {
            if (!mOnDeviceChangedListeners.containsKey(listener)) {
                IOnDeviceChangedListener l = new IOnDeviceChangedListener.Stub() {
                    @Override
                    public void onDeviceOffLine(Device device) throws RemoteException {
                        listener.onDeviceOffLine(device);
                    }

                    @Override
                    public void onDeviceOnLine(Device device) throws RemoteException {
                        listener.onDeviceOnLine(device);
                    }

                    @Override
                    public void onDeviceUpdate(Device device) throws RemoteException {
                        listener.onDeviceUpdate(device);
                    }
                };
                mOnDeviceChangedListeners.put(listener, l);
                mService.addOnDeviceChangedListener(l);
            }
        }
    }

    @Override
    public void removeOnDeviceChangedListener(OnDeviceChangedListener listener) throws RemoteException {
        synchronized (mOnDeviceChangedListeners) {
            IOnDeviceChangedListener l = mOnDeviceChangedListeners.get(listener);
            if (l != null) {
                mService.removeOnDeviceChangedListener(l);
                mOnDeviceChangedListeners.remove(listener);
            }
        }
    }

    @Override
    public void addDeviceBindListener(final OnDeviceBindListener listener) throws RemoteException {
        synchronized (mOnDeviceBindListeners) {
            if (!mOnDeviceBindListeners.containsKey(listener)) {
                final IDeviceBindListener l = new IDeviceBindListener.Stub() {
                    @Override
                    public void onDeviceBind(String lsid) throws RemoteException {
                        listener.onDeviceBind(lsid);
                    }

                    @Override
                    public void onDeviceUnbind(String lsid) throws RemoteException {
                        listener.onDeviceUnBind(lsid);
                    }
                };
                mOnDeviceBindListeners.put(listener, l);
                mService.addDeviceBindListener(l);
            }
        }
    }

    @Override
    public void removeDeviceBindListener(OnDeviceBindListener listener) throws RemoteException {
        synchronized (mOnDeviceBindListeners) {
            IDeviceBindListener l = mOnDeviceBindListeners.get(listener);
            if (l != null) {
                mService.removeDeviceBindListener(l);
                mOnDeviceBindListeners.remove(listener);
            }
        }
    }

    @Override
    public void addDeviceInfoUpdateListener(final OnDeviceInfoUpdateListener listener) throws RemoteException {
        synchronized (mOnDeviceInfoUpdateListeners) {
            if (!mOnDeviceInfoUpdateListeners.containsKey(listener)) {
                final IDeviceInfoUpdateListener l = new IDeviceInfoUpdateListener.Stub() {

                    @Override
                    public void onDeviceInfoUpdate(List<Device> devices) throws RemoteException {
                        listener.onDeviceInfoUpdate(devices);
                    }
                };
                mOnDeviceInfoUpdateListeners.put(listener, l);
                mService.addDeviceInfoUpdateListener(l);
            }
        }
    }

    @Override
    public void removeDeviceInfoUpdateListener(OnDeviceInfoUpdateListener listener) throws RemoteException {
        synchronized (mOnDeviceInfoUpdateListeners) {
            IDeviceInfoUpdateListener l = mOnDeviceInfoUpdateListeners.get(listener);
            if (l != null) {
                mService.removeDeviceInfoUpdateListener(l);
                mOnDeviceInfoUpdateListeners.remove(listener);
            }
        }
    }

    @Override
    public List<Device> getDevices() throws Exception {
        return mDeviceManager.getDevices();
    }

    @Override
    public List<Device> getDeviceOnlineStatus() throws Exception {
        return mDeviceManager.getDeviceOnlineStatus();
    }

    @Override
    public Device getCurrentDevice() throws Exception {
        return mDeviceManager.getCurrentDevice();
    }


    @Override
    public List<Device> updateDeviceList() {
        try {
            return mService.updateDeviceList();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }


    interface DeviceListCallBack {
        void onDevices(List<Device> list);
    }


    public void updateDeviceList(final DeviceListCallBack callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Device> list = updateDeviceList();
                if (callBack != null) {
                    callBack.onDevices(list);
                }
            }
        }).start();
    }
}
