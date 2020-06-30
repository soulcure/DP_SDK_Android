package com.swaiotos.skymirror;

import android.util.Log;

import com.google.gson.Gson;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;

/**
 * @ClassName DeviceParams
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/4/8
 * @Version TODO (write something)
 */
public class MirrorScreenParams {
    /**
     * 用于给CmdData cmd字段赋值
     */
    public enum CMD{
        START_MIRROR,
        STOP_MIRROR,
    }

    public boolean result;

    public String toJson() {
        return new Gson().toJson(this);
    }
}
