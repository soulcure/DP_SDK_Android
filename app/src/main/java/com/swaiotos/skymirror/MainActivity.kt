package com.swaiotos.skymirror

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.swaiotos.skymirror.sdk.manager.DeviceControllerManager
import com.swaiotos.skymirror.sdk.util.DLNACommonUtil
import com.swaiotos.skymirror.sdk.util.NetUtils
import kotlinx.android.synthetic.main.activity_main.*
import swaiotos.channel.iot.ss.SSChannel
import swaiotos.channel.iot.ss.channel.im.IMMessage
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback


//1.验证是否有系统权限
//              if(has)
//                  使用 DisplayManager 创建 VirtualDisplay 进行抓屏
//              else
//                  获取悬浮窗权限，再使用 MediaProjection 创建 VirtualDisplay 进行抓屏
//2.是否是iot-channel传输
//              if(isIotChannel)
//                  通过 handleIMMessage 获取 iot-channel 发送的IP，开始镜像
//              else（通过点击事件）
//                  需要在editText中输入正确IP，点击按钮将IP传入，开始镜像

class MainActivity : Activity()/* : SSChannelClient.SSChannelClientActivity()*/ {

    var REQUEST_CODE_CHANNEL: Int = 1;
    var REQUEST_CODE_CLICK: Int = 2;

    var mServerIp: String = null.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*if (Build.VERSION.SDK_INT >= 21) {
            val decorView = getWindow().getDecorView()
            //使背景图与状态栏融合到一起，这里需要在setcontentview前执行
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }*/

        setContentView(R.layout.activity_main)

        //startService(Intent(this, ClientChannelService::class.java))

        /*val window = getWindow()
        window.setGravity(Gravity.LEFT or Gravity.TOP)
        val params = window.getAttributes()
        params.x = 0
        params.y = 0
        params.height = 1
        params.width = 1
        window.setAttributes(params)*/

        //etInputCaptureIp.setText(NetUtils.getIP(this))
        //etInputCaptureIp.setText("172.20.130.169")
        etInputCaptureIp.setText("192.168.50.202")
        //etInputCaptureIp.setText("192.168.137.115")

        versionName.text = BuildConfig.VERSION_NAME
    }

    override fun onResume() {
        super.onResume()
        if (intent != null && intent.action != null) {
            if (intent.action.equals("action_startCapture")) {
                val serverIp = intent.getStringExtra("serverIp")
                prepareScreen(serverIp, REQUEST_CODE_CHANNEL)
            }
        }
    }

    /**
     * 开启发送镜像服务
     */
    fun startScreen(view: View) {
        prepareScreen(etInputCaptureIp.text.toString(), REQUEST_CODE_CLICK)
    }

    /**
     * 关闭发送镜像服务
     */
    fun stopScreen(view: View) {
        DeviceControllerManager.getInstance()
            .stopScreenCapture(this, DefaultCaptureService::class.java)
    }

    /**
     * 结束接收投屏数据
     */
    fun stopReverseScreen(view: View) {
        DeviceControllerManager.getInstance()
            .stopReverseScreenPlayer(this, DefaultPlayerService::class.java)
    }

    /**
     * 开始接收投屏数据（使用sdkUI）
     */
    fun startReverseScreen(view: View) {
        Log.e("colin", "colin start time01 --- pad start ReverseCaptureService by click")
        DeviceControllerManager.getInstance()
            .startReverseScreenPlayer(this, DefaultPlayerService::class.java)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_CHANNEL -> {
                startScreen(resultCode, data)
                finish()//启动后关闭
            }

            REQUEST_CODE_CLICK -> {
                if (TextUtils.isEmpty(etInputCaptureIp.text)) {
                    Toast.makeText(this, "请输入IP", Toast.LENGTH_LONG).show()
                    return
                }
                startScreen(resultCode, data)
            }
        }

    }

    /**
     * iot-channel 消息处理
     */
    /*override fun handleIMMessage(message: IMMessage?, channel: SSChannel?): Boolean {

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
            prepareScreen(serverIp, REQUEST_CODE_CHANNEL)//开始镜像
            return false
        }
        //接收到接收端结束消息
        if (msg.cmd == ReverseScreenParams.CMD.STOP_REVERSE.toString()) {
            stopScreen()//结束镜像
            return false
        }
        //接收到发送端开始消息
        if (msg.cmd == MirrorScreenParams.CMD.START_MIRROR.toString()) {
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
            DeviceControllerManager.getInstance()
                .stopReverseScreenPlayer(this, DefaultPlayerService::class.java)
            return false
        }
        return false
    }*/

    /**
     * 验证是否有系统权限
     */
    fun hasPermission(): Boolean {
        return DLNACommonUtil.checkPermission(this)
    }

    /**
     * 开始镜像
     */
    fun prepareScreen(ip: String, type: Int) {
        Log.e("colin", "colin start time01 --- tv start MirClientService by iot-channel")
        mServerIp = ip;

        if (hasPermission()) {
            DeviceControllerManager.getInstance()
                .startScreenCapture(
                    this,
                    ip,
                    DefaultCaptureService::class.java
                )
            finish()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val projectionManager =
                getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            startActivityForResult(
                projectionManager.createScreenCaptureIntent(),
                type
            )
            return
        }
    }

    /**
     * 结束镜像
     */
    fun stopScreen() {
        DeviceControllerManager.getInstance()
            .stopScreenCapture(this, DefaultCaptureService::class.java)
    }

    fun startScreen(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "权限被拒绝", Toast.LENGTH_LONG).show()
        } else {
            DeviceControllerManager.getInstance()
                .startScreenCapture(
                    this,
                    mServerIp,
                    1080,
                    1920,
                    resultCode,
                    data,
                    DefaultCaptureService::class.java
                )
        }

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
