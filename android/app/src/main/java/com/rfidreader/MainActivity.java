package com.rfidreader;

import android.content.SharedPreferences;
import android.widget.Toast;

import com.facebook.react.ReactActivity;
import com.handheld.uhfr.UHFRManager;
import com.uhf.api.cls.Reader;

public class MainActivity extends ReactActivity {

    public static UHFRManager mUhfrManager;//uhf
    private SharedPreferences mSharedPreferences;
    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "RFIDReader";
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        mUhfrManager = UHFRManager.getIntance();// Init Uhf module
//        if(mUhfrManager!=null){
//            mUhfrManager.setPower(mSharedPreferences.getInt("readPower",30), mSharedPreferences.getInt("writePower",30));//set uhf module power
//            mUhfrManager.setRegion(Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion",1)));
//            Toast.makeText(getApplicationContext(),"FreRegion:"+Reader.Region_Conf.valueOf(mSharedPreferences.getInt("freRegion",1))+
//                    "\n"+"Read Power:"+mSharedPreferences.getInt("readPower",30)+
//                    "\n"+"Write Power:"+mSharedPreferences.getInt("writePower",30), Toast.LENGTH_LONG).show();
//            showToast("UHF初始化成功！");
//        }else {
//            showToast("UHF初始化失败！");
//        }
////        getRfidTest();
//    }
//    private Toast mToast;
//    //show toast
//    private void showToast(String info) {
//        if (mToast == null)
//            mToast = Toast.makeText(this, info, Toast.LENGTH_SHORT);
//        else
//            mToast.setText(info);
//        mToast.show();
//    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
//        Log.e("main","destroy");
        if (mUhfrManager != null) {//close uhf module
            mUhfrManager.close();
            mUhfrManager = null;
        }
    }
}
