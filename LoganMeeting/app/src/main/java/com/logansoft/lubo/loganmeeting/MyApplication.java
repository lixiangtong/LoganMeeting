package com.logansoft.lubo.loganmeeting;

import android.app.Application;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.cloudroom.cloudroomvideosdk.CloudroomQueue;
import com.cloudroom.cloudroomvideosdk.CloudroomVideoMeeting;
import com.cloudroom.cloudroomvideosdk.CloudroomVideoMgr;
import com.cloudroom.cloudroomvideosdk.CloudroomVideoSDK;
import com.cloudroom.cloudroomvideosdk.model.CRVIDEOSDK_ERR_DEF;
import com.cloudroom.cloudroomvideosdk.model.SdkInitDat;
import com.cloudroom.tool.MeetingPackageHelper;
import com.logansoft.lubo.loganmeeting.utils.VideoSDKHelper;

import cn.finalteam.toolsfinal.CrashHandler;

/**
 * Created by logansoft on 2017/7/10.
 */

public class MyApplication extends Application {

    private static final String TAG = "test";
    private static MyApplication mInstance;
    private Toast mToast;
    private Handler mMainHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        CrashHandler.getInstance().init(getApplicationContext());


        SdkInitDat initDat = new SdkInitDat();

        initDat.oemID = "CLOUDROOM";
        //配置文件路径
        initDat.cfgPathFileName = "/sdcard/LoganMeeting/UserCfg.ini";
        //日志文件路径
        initDat.loggerPathFileName = "/sdcard/LoganMeeting/log/Log.log";
        //是否输出日志到控制台
        initDat.showSDKLogConsole = true;
        //初始化SDK
        CloudroomVideoSDK.getInstance().init(getApplicationContext(), initDat);


        //打开日志
        CloudroomVideoSDK.getInstance().setLogOpen(true);

        CloudroomVideoMgr.getInstance().setMgrCallBack(MgrCallback.getInstance());

        //输出SDK版本号
        Log.d(TAG, "CloudroomVideoMgrVer:"
                + CloudroomVideoSDK.getInstance().GetCloudroomVideoSDKVer());

        //输出SDK包名
        Log.d(TAG, "CloudroomVideoMgrVer:"
                + MeetingPackageHelper.getMeetingMgrPackageName(initDat.oemID) + "/" + MeetingPackageHelper.getMeetingPackageName(initDat.oemID));

    }

    public static MyApplication getInstance() {
        return mInstance;
    }

    public void showToast(final String txt) {
        mMainHandler.post(new Runnable() {

            @Override
            public void run() {
                if (mToast != null) {
                    try {
                        mToast.cancel();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mToast = Toast.makeText(getApplicationContext(), txt,
                        Toast.LENGTH_LONG);
                mToast.show();
            }
        });
    }

    public void showToast(final int id) {
        showToast(getString(id));
    }

    public void showToast(String txt, CRVIDEOSDK_ERR_DEF err) {
        String text = String.format("%s ( %s )", txt, VideoSDKHelper
                .getInstance().getErrStr(err));
        showToast(text);
    }

    public void showToast(final int id, final CRVIDEOSDK_ERR_DEF err) {
        showToast(getString(id), err);
    }

}
