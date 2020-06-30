package com.swaiotos.skymirror

import android.app.Application
import com.swaiotos.skymirror.sdk.manager.DeviceControllerManager

/**
 * @ClassName: MyApplication
 * @Description: java类作用描述
 * @Author: lfz
 * @Date: 2020/4/14 16:37
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DeviceControllerManager.getInstance().init()
    }
}