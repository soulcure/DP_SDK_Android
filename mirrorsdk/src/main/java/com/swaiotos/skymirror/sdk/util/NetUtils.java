/*
 * Copyright © 2018 Yan Zhenjie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.swaiotos.skymirror.sdk.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.swaiotos.skymirror.sdk.capture.MirClientService;

import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

import java.util.List;
import java.util.regex.Pattern;


import static android.content.Context.WIFI_SERVICE;

/**
 * Created by YanZhenjie on 2018/6/9.
 */
public class NetUtils {

    /**
     * Ipv4 address check.
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(" + "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}" +
                    "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

    /**
     * Check if valid IPV4 address.
     *
     * @param input the address string to check for validity.
     * @return True if the input parameter is a valid IPv4 address.
     */
    public static boolean isIPv4Address(String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }

    /**
     * Get local Ip address.
     */
    public static InetAddress getLocalIPAddress(int netType) {
        Enumeration<NetworkInterface> enumeration = null;
        try {
            enumeration = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        String netName = "";

        if (netType == ConnectivityManager.TYPE_ETHERNET)
            netName = "eth0";
        else if (netType == ConnectivityManager.TYPE_WIFI)
            netName = "wlan0";

        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                NetworkInterface nif = enumeration.nextElement();
                if (netName.equals(nif.getName())) {
                    Enumeration<InetAddress> inetAddresses = nif.getInetAddresses();
                    if (inetAddresses != null) {
                        while (inetAddresses.hasMoreElements()) {
                            InetAddress inetAddress = inetAddresses.nextElement();

                            if (!inetAddress.isLoopbackAddress() && isIPv4Address(inetAddress.getHostAddress())) {
                                Log.d("cuixiyuan", "ip = " + inetAddress.toString());
                                return inetAddress;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static NetworkInterface getNetInterface() {
        try {
            Enumeration<NetworkInterface> en;
            en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface ni = en.nextElement();
                if (ni.getName().startsWith("eth")) {
                    return ni;
                }
            }
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            Enumeration<NetworkInterface> en;
            en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface ni = en.nextElement();
                if (ni.getName().startsWith("wlan")) {
                    return ni;
                }
            }
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public static int getLocalIpV4IntLEByNetInterface(NetworkInterface ni, Context context) throws SocketException {

        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());

        if (networkInfo == null || !networkInfo.isConnected()) {
            return 0;//如果未连接网络状态为未连接
        }

        Log.d("MirClientService", "getLocalIpV4IntLEByNetInterface: networkInfo type " + networkInfo.getType());

        if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
            for (NetworkInterface intf : interfaces) {
                //只索引WiFi网络的IP，一般手机只会有一个wlan，即wlan0（少数手机WiFi Ap模式下的接口为“ap0”）
                if (intf.getDisplayName().startsWith("eth")) {
                    List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                    for (InetAddress addr : addrs) {
                        if (!addr.isLoopbackAddress()) {
                            String sAddr = addr.getHostAddress().toUpperCase();
                            boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                            if (isIPv4) {

                                return Conv.ipv4StrToIntLE(addr.getHostAddress());

                            }
                        }
                    }
                }
            }
        }

        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return getLocalIp(context);
        }

        return 0;

    }

    private static int getLocalIp(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        // 判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return ipAddress;
    }

    private static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    public static String getIP(Context context) {
//        WifiManager wifiService = (WifiManager)context.getSystemService(WIFI_SERVICE);
//        WifiInfo wifiinfo = wifiService.getConnectionInfo();
//        return intToIp(wifiinfo.getIpAddress());
        int ipNumber = 0;
        try {
            ipNumber = getLocalIpV4IntLEByNetInterface(getNetInterface(), context);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return intToIp(ipNumber);
    }

    public static String intToIp(int i) {

        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
                + "." + (i >> 24 & 0xFF);
    }

}
