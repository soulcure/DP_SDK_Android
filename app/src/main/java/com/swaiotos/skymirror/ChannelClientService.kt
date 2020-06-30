package com.swaiotos.skymirror

import android.content.Intent
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.swaiotos.skymirror.sdk.manager.DeviceControllerManager
import com.swaiotos.skymirror.sdk.reverse.IPlayerInitListener
import swaiotos.channel.iot.ss.SSChannel
import swaiotos.channel.iot.ss.SSChannelClient
import swaiotos.channel.iot.ss.channel.im.IMMessage
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback

/**
 * @ClassName: ChannelClientService
 * @Description: java类作用描述
 * @Author: lfz
 * @Date: 2020/5/11 11:17
 */
class ChannelClientService : SSChannelClient.SSChannelClientService("ChannelClientService") {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun handleIMMessage(message: IMMessage?, channel: SSChannel?): Boolean {

        Log.e("lfzzzz", "handleIMMessage: message --- $message")
        val content = message!!.content
        Log.e("lfzzzz", "handleIMMessage: content --- $content")
        val gson = Gson()
        val msg = gson.fromJson(content, CmdData::class.java)
        //接收到接收端开始消息
        if (msg.cmd == ReverseScreenParams.CMD.START_REVERSE.toString()) {
            if (TextUtils.isEmpty(msg.param)) {
                Log.e("MainActivity", "iot-channel param is null")
                return false
            }

            val serverIp = gson.fromJson(msg.param, ServerIpData::class.java).ip

            if (TextUtils.isEmpty(serverIp)) {
                Log.e("MainActivity", "iot-channel ip is null")
                return false
            }
            //开始镜像
            val intent = Intent(this, MainActivity::class.java)
            intent.setAction("action_startCapture")
            intent.putExtra("serverIp", serverIp)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent)

            return false
        }

        //接收到接收端结束消息
        if (msg.cmd == ReverseScreenParams.CMD.STOP_REVERSE.toString()) {
            //结束镜像
            DeviceControllerManager.getInstance()
                .stopScreenCapture(this, DefaultCaptureService::class.java)
            return false
        }
        //接收到发送端开始消息
        if (msg.cmd == MirrorScreenParams.CMD.START_MIRROR.toString()) {

            //开始前调用下stop,防止adress in used
            DeviceControllerManager.getInstance()
                .stopReverseScreenPlayer(this, DefaultPlayerService::class.java)
            DeviceControllerManager.getInstance()
                .stopScreenCapture(this, DefaultCaptureService::class.java)

            DeviceControllerManager.getInstance()
                .startReverseScreenPlayer(this, DefaultPlayerService::class.java)
            DeviceControllerManager.getInstance()
                .setReverseInitListener(object : IPlayerInitListener {
                    override fun onInitStatus(isInit: Boolean) {
                        Log.e("lfzzz", "isInit --- " + isInit)
                        if (channel != null) {
                            sendChannelMessage(message, channel, isInit)
                        } else {
                            Log.e("lfzzz", "iot-channel reSend failed be channel is null")
                        }
                    }
                })
            return false
        }
        //接收到发送端结束消息
        if (msg.cmd == MirrorScreenParams.CMD.STOP_MIRROR.toString()) {
            //DeviceControllerManager.getInstance()
                //.stopReverseScreenPlayer(this, DefaultPlayerService::class.java)
            return false
        }
        return false
    }

    fun sendChannelMessage(message: IMMessage, channel: SSChannel, result: Boolean): Unit {

        try {
            val mirror = MirrorScreenParams()
            mirror.result = result

            val cmdData = CmdData(
                MirrorScreenParams.CMD.START_MIRROR.toString(),
                CmdData.CMD_TYPE.MIRROR_SCREEN.toString(),
                mirror.toJson()
            )

            Log.e("lfzzz", "target --- " + message.target)
            Log.e("lfzzz", "source --- " + message.source)
            Log.e("lfzzz", "clientTarget --- " + message.clientTarget)
            Log.e("lfzzz", "clientSource --- " + message.clientSource)
            Log.e("lfzzz", "cmdData --- " + cmdData.toJson())

            val imMessage =
                IMMessage.Builder.createTextMessage(
                    /*message.source,*/  message.target,
                    /*message.target,*/ message.source,
                    /*message.clientSource,*/message.clientTarget,
                    /*message.clientTarget,*/ message.clientSource,
                    cmdData.toJson()
                )

            imMessage.putExtra("response", message.content.toString())

            channel.imChannel.send(imMessage, object : IMMessageCallback {
                override fun onProgress(message: IMMessage?, progress: Int) {
                    Log.e("lfzzz", "onProgress --- " + progress)
                }

                override fun onEnd(message: IMMessage?, code: Int, info: String?) {
                    Log.e("lfzzz", "onEnd --- " + code)
                }

                override fun onStart(message: IMMessage?) {
                    Log.e("lfzzz", "onStart --- ")
                }
            })
        } catch (e: Exception) {
            Log.e("lfzzz", "iot-channel reSend failed --- " + e.message)
        }

    }

}