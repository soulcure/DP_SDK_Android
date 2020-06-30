package swaiotos.channel.iot;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import swaiotos.channel.iot.ss.IMainService;
import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSChannelImpl;

/**
 * @ClassName: IOTChannelImpl
 * @Author: lu
 * @CreateDate: 2020/4/14 11:23 AM
 * @Description:
 */
public class IOTChannelImpl implements IOTChannel {
    static final String SS_ACTION = "swaiotos.intent.action.channel.iot.service.SS";

    private SSChannel mSSChannel = new SSChannelImpl();
    private S<IMainService> mService;

    @Override
    public void open(Context context, IMainService service) throws Exception {
        mSSChannel.open(context, service);
    }

    @Override
    public void open(Context context, OpenCallback callback) {
        open(context, context.getPackageName(), callback);
    }

    @Override
    public void open(Context context, String packageName, OpenCallback callback) {
        try {
            performOpen(context, packageName, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public SSChannel getSSChannel() {
        return mSSChannel;
    }

    @Override
    public void close() {
        if (mSSChannel != null) {
            try {
                mSSChannel.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mService != null) {
            try {
                mService.unbind();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void performOpen(final Context context, String packageName, final OpenCallback callback) throws Exception {
        final CountDownLatch latch = callback == null ? new CountDownLatch(1) : null;
        mService = new S<IMainService>(context, packageName, SS_ACTION) {
            @Override
            protected IMainService transform(IBinder service) {
                return IMainService.Stub.asInterface(service);
            }

            @Override
            protected void onConntected(IMainService service) {
                try {
                    open(context, service);
                    if (callback != null) {
                        callback.onConntected(mSSChannel);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mService.unbind();
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                }
                if (latch != null) {
                    latch.countDown();
                }
            }
        };
        mService.bind();
        if (latch != null) {
            latch.await(10, TimeUnit.SECONDS);
        }
    }


    static abstract class S<T> implements ServiceConnection {
        private Context mContext;
        private Intent mIntent;
        private T mService;

        public S(Context context, String packageName, String action) {
            this.mContext = context;
            mIntent = new Intent(action);
            mIntent.setPackage(packageName);
        }

        public void bind() {
            mContext.bindService(mIntent, this, Context.BIND_AUTO_CREATE);
        }

        public void unbind() {
            mContext.unbindService(this);
        }

        public T getService() {
            return this.mService;
        }

        protected abstract T transform(IBinder service);

        protected abstract void onConntected(T service);

        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mService = transform(service);
                    onConntected(mService);
                }
            }).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("S", "onServiceDisconnected@" + name);
            bind();
        }
    }
}
