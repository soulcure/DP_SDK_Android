package com.swaiotos.skymirror.sdk.reverse;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @ClassName: IPlayerListener
 * @Description: java类作用描述
 * @Author: lfz
 * @Date: 2020/4/15 11:27
 */
public interface IPlayerListener{

    int CODE_ENCODER_ERROR = 10000;//编码器异常
    int CODE_DECODER_ERROR = 10001;//解码器异常
    int CODE_SOCKET_ERROR  = 10002;//网络异常

    void onStart();
    void onStop();
    void onError(int code, String errorMessage);
}
