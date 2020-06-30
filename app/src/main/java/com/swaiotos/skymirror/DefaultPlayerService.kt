package com.swaiotos.skymirror

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.swaiotos.skymirror.sdk.reverse.ReverseCaptureService

/**
 * @ClassName: DefaultPlayerService
 * @Description: java类作用描述
 * @Author: lfz
 * @Date: 2020/4/15 18:17
 */
class DefaultPlayerService : ReverseCaptureService() {

    override fun initNotification() {
        createNotification()
    }

    var CHANNEL_TWO_ID = "CHANNEL_TWO_ID"
    var CHANNEL_TWO_NAME = "ReverseCaptureService"

    fun createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_TWO_ID,
                CHANNEL_TWO_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            val manager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            startForeground(
                1024, Notification.Builder(this)
                    .setChannelId(CHANNEL_TWO_ID).build()
            )
        } else {
            startForeground(1024, Notification())
        }
    }
}