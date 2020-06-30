package com.swaiotos.skymirror

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.swaiotos.skymirror.sdk.capture.MirClientService

/**
 * @ClassName: DefaultService
 * @Description: java类作用描述
 * @Author: lfz
 * @Date: 2020/4/14 14:06
 */
class DefaultCaptureService : MirClientService() {
    override fun initNotification() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        createNotification()
    }

    var CHANNEL_ONE_ID = "CHANNEL_ONE_ID"
    var CHANNEL_ONE_NAME = "MirClientService"

    fun createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ONE_ID,
                CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            val manager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            startForeground(
                1024, Notification.Builder(this)
                    .setChannelId(CHANNEL_ONE_ID).build()
            )
        } else {
            startForeground(1024, Notification())
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return null
    }
}