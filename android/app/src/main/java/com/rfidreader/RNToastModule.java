package com.rfidreader;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.BRMicro.Tools;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
//import com.alibaba.fastjson.asm.Type;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.handheld.uhfr.UHFRManager;
import com.uhf.api.cls.Reader;
import com.uhf.api.cls.Reader.TAGINFO;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by yaqin on 2018/07/16.
 */

public class RNToastModule extends ReactContextBaseJavaModule {

    public UHFRManager mUhfrManager;// uhf
    private boolean keyControl = true;
    private boolean isStart = false;
    private boolean isRunning = false;
    private boolean isMulti = false;// multi mode flag
    private SharedPreferences mSharedPreferences;

    public RNToastModule(final ReactApplicationContext reactContext) {
        super(reactContext);
        DyEvent.myContext = reactContext;
        Util.initSoundPool(reactContext);
        mSharedPreferences = reactContext.getSharedPreferences("UHF",MODE_PRIVATE);
        this.mUhfrManager = UHFRManager.getIntance();
        if(mUhfrManager != null) {
            mUhfrManager.setPower(mSharedPreferences.getInt("readPower", 30), mSharedPreferences.getInt("writePower", 30));//set uhf module power
            mUhfrManager.setRegion(Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion", 1)));
        }
     //   Toast.makeText(getReactApplicationContext(),"FreRegion:"+Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion",1))+
       //         "\n"+"Read Power:"+mSharedPreferences.getInt("readPower",30)+
         //       "\n"+"Write Power:"+mSharedPreferences.getInt("writePower",30),Toast.LENGTH_LONG).show();
        try{
            } catch (Exception e) {
            Toast.makeText(getReactApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();

        }
        IntentFilter filter = new IntentFilter() ;
        filter.addAction("android.rfid.FUN_KEY");
        reactContext.registerReceiver(keyReceiver, filter) ;
    }


    @Override
    public String getName() {
        return "RNToastAndroid";
    }
//读取时间太久，List一个值。
    @ReactMethod
    public void readrfid() {
        // mSharedPreferences = reactContext.getSharedPreferences("UHF",MODE_PRIVATE);\
        if (mUhfrManager == null) {
            mUhfrManager = UHFRManager.getIntance();
        }
        if (mUhfrManager != null) {
//             Log.e("eee", "init success!");
//             mUhfrManager.setPower(mSharedPreferences.getInt("readPower", 30), mSharedPreferences.getInt("writePower", 30));//t uhf module power
//             mUhfrManager.setRegion(Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion", 1)));
            mUhfrManager.setCancleFastMode();
            isRunning = true;

            //  mUhfrManager.setFastMode();
            int iCount = 0;
            // new Thread(inventoryTask).start();
            int i = 0;
            List<Reader.TAGINFO> epcList;
            Map<String, Tag_Data> strList = new HashMap<String, Tag_Data>();
            if (iCount < 50) {
                while (isRunning) {
//                    Log.e("eee", "running!" + iCount);
//                    try {
//                        Thread.sleep(250);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    //epcList = mUhfrManager.tagInventoryRealTime();
                    epcList = mUhfrManager.tagInventoryByTimer((short) 50);
                    if (epcList != null && epcList.size() > 0) {
                        for (Reader.TAGINFO tfs : epcList) {
                            i++;
                            byte[] epcdata = tfs.EpcId;
                            String epc = Tools.Bytes2HexString(epcdata, epcdata.length);
                            int rssi = tfs.RSSI;
                            Tag_Data tag_data = new Tag_Data(String.valueOf(i), epc, String.valueOf(rssi));
                            if (strList.containsKey(epc)) {
                                continue;
                            }
                            strList.put(epc, tag_data);
                        }
                        Util.play(1, 0);
//                     JSONObject arr = (JSONObject) JSONObject.toJSON(strList);
//                     WritableMap wm = new WritableNativeMap();
//                     wm.putString("RFID", arr.toJSONString());
//                     new DyEvent().sendEvent(getReactApplicationContext(), "EventName", wm);
                        // iCount=200;
                        iCount++;
                        if (iCount == 50) {
                            isRunning = false;
                            break;
                        }
                    }
                }
                JSONObject arr = (JSONObject) JSONObject.toJSON(strList);
                WritableMap wm = new WritableNativeMap();
                wm.putString("RFID", arr.toJSONString());
                new DyEvent().sendEvent(getReactApplicationContext(), "EventName", wm);
                // iCount++;
            }
            mUhfrManager.stopTagInventory();
            mUhfrManager.close();
            mUhfrManager = null;
            Log.e("eee", "close!");
            //OperRegister();
        }
    }
    @ReactMethod
    public void read() {
        if (keyControl) {
            keyControl = false;
            if (!isStart) {
                this.mUhfrManager.setCancleInventoryFilter();
                isRunning = true;
                if (isMulti) {
                    //1200模块支持快速模式，启用快速模式后盘点标签速度大大提高。但是也需要注意模块温度
                    this.mUhfrManager.setFastMode();
                    this.mUhfrManager.asyncStartReading();
                    //Toast.makeText(getReactApplicationContext(), "开始盘点", Toast.LENGTH_LONG).show();
                } else {
                    this.mUhfrManager.setCancleFastMode();
                }
                //Toast.makeText(getReactApplicationContext(), "run", Toast.LENGTH_LONG).show();
                new Thread(){
                    public  void run(){
                        Looper.prepare();
                        new Handler().post(inventoryTask);
                        Looper.loop();
                    }
                }.start();
                isStart = true;
          }else{
                if (isMulti) {
                    mUhfrManager.asyncStopReading();
                } else {
                    mUhfrManager.stopTagInventory();
                    mUhfrManager.close();
                }
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isRunning = false;
                isStart = false;
            }
            keyControl = true;
        }
    }


    // inventory epc
    private Runnable inventoryTask = new Runnable() {
        @Override
        public void run() {
            while (isRunning) {
                     int i=0;
                     List<Reader.TAGINFO> list1;
                     Map<String,Tag_Data> strList=new HashMap<String,Tag_Data>();
                    //if (isMulti) { // multi mode
                     //   list1 = mUhfrManager.tagInventoryRealTime();
                  //  } else {
                        list1 = mUhfrManager.tagInventoryByTimer((short) 50);
                  //  }
                    if (list1 != null && list1.size() > 0) {
                        for (Reader.TAGINFO tfs:list1) {
                            i++;
                            byte[] epcdata = tfs.EpcId;
                            String epc = Tools.Bytes2HexString(epcdata, epcdata.length);
                            int rssi = tfs.RSSI;
                            Tag_Data tag_data=new Tag_Data(String.valueOf(i),epc,String.valueOf(rssi));
                            if(strList.containsKey(epc)){
                                continue;
                            }
                            strList.put(epc,tag_data);
                    }
                        Util.play(1, 0);
                            JSONObject obj = (JSONObject) JSONObject.toJSON(strList);
                            WritableMap wm = new WritableNativeMap();
                            wm.putString("RFID", obj.toJSONString());
                            new DyEvent().sendEvent(getReactApplicationContext(), "EventName", wm);

                        }
            }
            }


    };
    //private boolean f1hidden = false;
    //key receiver
    private  long startTime = 0 ;
    private boolean keyUpFalg= true;
    private BroadcastReceiver keyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        //    if (f1hidden) return;
            int keyCode = intent.getIntExtra("keyCode", 0) ;
            if(keyCode == 0){//H941
                keyCode = intent.getIntExtra("keycode", 0) ;
            }
//            Log.e("key ","keyCode = " + keyCode) ;
            boolean keyDown = intent.getBooleanExtra("keydown", false) ;
//			Log.e("key ", "down = " + keyDown);
            if(keyUpFalg&&keyDown && System.currentTimeMillis() - startTime > 500){
                keyUpFalg = false;
                startTime = System.currentTimeMillis() ;
                if ( (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F2
                        || keyCode == KeyEvent.KEYCODE_F3 || keyCode == KeyEvent.KEYCODE_F4 ||
                        keyCode == KeyEvent.KEYCODE_F5)) {
//                Log.e("key ","inventory.... " ) ;
                    readrfid();
                }
                return ;
            }else if (keyDown){
                startTime = System.currentTimeMillis() ;
            }else {
                keyUpFalg = true;
            }

        }
    } ;


}




