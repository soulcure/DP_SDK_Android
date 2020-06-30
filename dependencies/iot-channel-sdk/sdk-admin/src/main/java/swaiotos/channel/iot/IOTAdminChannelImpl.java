package swaiotos.channel.iot;

import android.content.Context;
import android.os.IBinder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import swaiotos.channel.iot.ss.IMainService;
import swaiotos.channel.iot.ss.SSAdminChannel;
import swaiotos.channel.iot.ss.SSAdminChannelImpl;

import static swaiotos.channel.iot.IOTChannelImpl.SS_ACTION;

/**
 * @ClassName: IOTChannelImpl
 * @Author: lu
 * @CreateDate: 2020/4/14 11:23 AM
 * @Description:
 */
public class IOTAdminChannelImpl implements IOTAdminChannel {
    private SSAdminChannel mChannel = new SSAdminChannelImpl();
    private IOTChannelImpl.S<IMainService> mService;

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
    public SSAdminChannel getSSAdminChannel() {
        return mChannel;
    }

    @Override
    public void close() {
        mChannel.close();
        mService.unbind();
        IOTChannel.mananger.close();
    }

    private void performOpen(final Context context, String packageName, final OpenCallback callback) throws Exception {
        final CountDownLatch latch = callback == null ? new CountDownLatch(1) : null;
        mService = new IOTChannelImpl.S<IMainService>(context, packageName, SS_ACTION) {
            @Override
            protected IMainService transform(IBinder service) {
                return IMainService.Stub.asInterface(service);
            }

            @Override
            protected void onConntected(IMainService service) {
                try {
                    IOTChannel.mananger.open(context, service);
                    mChannel.open(context, service);
                    if (callback != null) {
                        callback.onConntected(mChannel);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
}
