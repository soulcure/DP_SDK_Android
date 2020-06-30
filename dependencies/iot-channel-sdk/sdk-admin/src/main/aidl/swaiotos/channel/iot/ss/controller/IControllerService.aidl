// IControllerService.aidl
package swaiotos.channel.iot.ss.controller;

// Declare any non-default types here with import statements

import swaiotos.channel.iot.utils.ipc.ParcelableObject;
import swaiotos.channel.iot.ss.session.Session;

interface IControllerService {
     ParcelableObject connect(String lsid,long timeout);

     void disconnect(in Session session);

     ParcelableObject getClientVersion(in Session target, String client,long timeout);

     ParcelableObject getDeviceInfo();

}
