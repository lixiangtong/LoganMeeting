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
import com.logansoft.lubo.loganmeeting.utils.MySDKHelper;

import cn.finalteam.toolsfinal.CrashHandler;

/**
 * Created by logansoft on 2017/7/10.
 */

public class MyApplication extends Application {

    private static final String TAG = "test";
    private static MyApplication mInstance;
    public static CloudroomVideoSDK sdkInstance;
    public static CloudroomVideoMeeting meetingInstance;
    public static CloudroomVideoMgr mgrInstance;
    public static CloudroomQueue queueInstance;
    private Toast mToast;
    private Handler mMainHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        CrashHandler.getInstance().init(getApplicationContext());

        sdkInstance = CloudroomVideoSDK.getInstance();


        SdkInitDat initDat = new SdkInitDat();

        initDat.oemID = "CLOUDROOM";
        //配置文件路径
        initDat.cfgPathFileName = "/sdcard/LoganMeeting/UserCfg.ini";
        //日志文件路径
        initDat.loggerPathFileName = "/sdcard/LoganMeeting/log/Log.log";
        //是否输出日志到控制台
        initDat.showSDKLogConsole = true;
        //初始化SDK
        sdkInstance.init(getApplicationContext(), initDat);
        //CloudroomVideoSDK初始化完成之后CloudroomVideoMeeting才能使用
        if (sdkInstance.isInitSuccess()) {
//            MyApplication.getInstance().showToast("初始化成功");
            meetingInstance = CloudroomVideoMeeting.getInstance();
            mgrInstance = CloudroomVideoMgr.getInstance();
            queueInstance = CloudroomQueue.getInstance();
        }
        //打开日志
        sdkInstance.setLogOpen(true);

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
        String text = String.format("%s ( %s )", txt, MySDKHelper
                .getInstance().getErrStr(err));
        showToast(text);
    }

    public void showToast(final int id, final CRVIDEOSDK_ERR_DEF err) {
        showToast(getString(id), err);
    }

}
