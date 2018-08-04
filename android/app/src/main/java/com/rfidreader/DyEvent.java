package com.rfidreader;

import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

/**
 * Created by jeffddjt on 2017/12/5.
 */

public class DyEvent {
    public static ReactContext myContext;

    public  void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params)
    {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName,params);
    }

}
