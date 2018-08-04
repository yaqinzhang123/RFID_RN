package com.rfidreader;

import android.app.Activity;
import android.app.Application;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
    private boolean isMulti = true;// multi mode flag
    private SharedPreferences mSharedPreferences;

    public RNToastModule(final ReactApplicationContext reactContext) {
        super(reactContext);
        DyEvent.myContext = reactContext;
        Util.initSoundPool(reactContext);
        mSharedPreferences = reactContext.getSharedPreferences("UHF",MODE_PRIVATE);
        this.mUhfrManager = UHFRManager.getIntance();
        mUhfrManager.setPower(mSharedPreferences.getInt("readPower",30), mSharedPreferences.getInt("writePower",30));//set uhf module power
        mUhfrManager.setRegion(Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion",1)));
     //   Toast.makeText(getReactApplicationContext(),"FreRegion:"+Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion",1))+
       //         "\n"+"Read Power:"+mSharedPreferences.getInt("readPower",30)+
         //       "\n"+"Write Power:"+mSharedPreferences.getInt("writePower",30),Toast.LENGTH_LONG).show();
        try{
            } catch (Exception e) {
            Toast.makeText(getReactApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();

        }

    }


    @Override
    public String getName() {
        return "RNToastAndroid";
    }
//读取时间太久，List一个值。
    @ReactMethod
    public void readrfid(){
       // mSharedPreferences = reactContext.getSharedPreferences("UHF",MODE_PRIVATE);
        if (mUhfrManager == null) {
            mUhfrManager = UHFRManager.getIntance();
         }
         if(mUhfrManager != null){
         Log.e("eee","init success!");
         mUhfrManager.setPower(mSharedPreferences.getInt("readPower",20), mSharedPreferences.getInt("writePower",20));//t uhf module power
         mUhfrManager.setRegion(Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion",1)));
         mUhfrManager.setCancleFastMode();
         int iCount = 0;
         //String m_Rfid = "";
         List<Reader.TAGINFO> epcList;
         Tag_Data tag_data=new Tag_Data("","","");
         ArrayList<Tag_Data> strList=new ArrayList<Tag_Data>();

            while(iCount<200){
                Log.e("eee","running!"+iCount);
                 try {
                     Thread.sleep(250);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
                    epcList = mUhfrManager.tagInventoryByTimer((short)50);
                    if (epcList != null&& epcList.size()>0) {
                        for (Reader.TAGINFO tfs:epcList) {
                            byte[] epcdata = tfs.EpcId;
                            String epc = Tools.Bytes2HexString(epcdata, epcdata.length);
                            int rssi = tfs.RSSI;
                            tag_data.epc=epc;
                            tag_data.rssi=String.valueOf(rssi);
                            if(strList.contains(tag_data)){
                                continue;
                            }
                            strList.add(tag_data);
                            iCount=200;
                            break;
                        }
                       // Util.play(1, 0);
                        JSONArray arr = (JSONArray) JSONArray.toJSON(strList);
                        WritableMap wm = new WritableNativeMap();
                        wm.putString("RFID", arr.toJSONString());
                        new DyEvent().sendEvent(getReactApplicationContext(), "EventName", wm);
                    }
                iCount = iCount+1;
                }
                mUhfrManager.stopTagInventory();
                mUhfrManager.close();
                mUhfrManager = null;
                Log.e("eee","close!");
                //OperRegister();
            }else{
                //ShowDialog("错误信息","初始化签标（RFID）读取器失败。",2);
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
                 if (isStart) {
                     List<Reader.TAGINFO> list1;
                    if (isMulti) { // multi mode
                        list1 = mUhfrManager.tagInventoryRealTime();
                    } else {
                        list1 = mUhfrManager.tagInventoryByTimer((short) 50);
                    }
                    if (list1 != null && list1.size() > 0) {
                        JSONArray arr = (JSONArray) JSONArray.toJSON(list1);
                        WritableMap wm = new WritableNativeMap();
                        wm.putString("RFID", arr.toJSONString());
                        new DyEvent().sendEvent(getReactApplicationContext(), "EventName", wm);
                    }
                }

            }
        }
    };
}




