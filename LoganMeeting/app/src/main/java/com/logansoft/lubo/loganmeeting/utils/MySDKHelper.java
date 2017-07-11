package com.logansoft.lubo.loganmeeting.utils;

import android.os.Handler;
import android.os.Message;

import com.cloudroom.cloudroomvideosdk.CloudroomVideoMeeting;
import com.cloudroom.cloudroomvideosdk.model.CRVIDEOSDK_ERR_DEF;
import com.logansoft.lubo.loganmeeting.MgrCallback;
import com.logansoft.lubo.loganmeeting.VideoCallback;

/**
 * Created by logansoft on 2017/7/11.
 */

public class MySDKHelper implements Handler.Callback {

    private static final String TAG = "test";
    private static MySDKHelper mInstance = null;
    private long mEnterTime = 0;
    private String mLoginUserID = null;

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case VideoCallback.MSG_ENTERMEETING_RSLT:
                enterMeetingRslt((CRVIDEOSDK_ERR_DEF) msg.obj);
                break;
            case MgrCallback.MSG_LOGIN_SUCCESS:
                mLoginUserID = (String) msg.obj;
                break;
            case MgrCallback.MSG_LINEOFF:
            case MgrCallback.MSG_LOGIN_FAIL:
                mLoginUserID = null;
                break;
            case MgrCallback.MSG_NOTIFYCALL_HUNGUP:
            case MgrCallback.MSG_HANGUPCALL_SUCCESS:
                mEnterTime = 0;
                break;
            default:
                break;
        }
        return false;
    }

    public static MySDKHelper getInstance() {
        synchronized (TAG) {
            if (mInstance == null) {
                mInstance = new MySDKHelper();
            }
        }
        return mInstance;
    }

    private void enterMeetingRslt(CRVIDEOSDK_ERR_DEF code) {
        if (code == CRVIDEOSDK_ERR_DEF.CRVIDEOSDK_NOERR) {
            mEnterTime = System.currentTimeMillis();
        } else {

        }
    }

    public void enterMeeting(int meetId, String meetPsw) {
        // Video回调对象
        CloudroomVideoMeeting.getInstance().setMeetingCallBack(
                VideoCallback.getInstance());
        CloudroomVideoMeeting.getInstance().enterMeeting(meetId, meetPsw,
                mLoginUserID, mLoginUserID);
    }

    public long getEnterTime() {
        return mEnterTime;
    }

    public String getErrStr(CRVIDEOSDK_ERR_DEF errCode) {
        switch (errCode) {
            case CRVIDEOSDK_OUTOF_MEM:
                return "内存不足";
            case CRVIDEOSDK_INNER_ERR:
                return "SDK内部错误";
            case CRVIDEOSDK_MISMATCHCLIENTVER:
                return "不支持的sdk版本";
            case CRVIDEOSDK_MEETPARAM_ERR:
                return "参数错误";
            case CRVIDEOSDK_ERR_DATA:
                return "无效数据";
            case CRVIDEOSDK_ANCTPSWD_ERR:
                return "帐号密码不正确";
            case CRVIDEOSDK_LOGINSTATE_ERROR:
                return "状态错误";
            case CRVIDEOSDK_USER_BEEN_KICKOUT:
                return "被挤下线（帐号在别处登录）";
            case CRVIDEOSDK_SERVER_EXCEPTION:
                return "服务异常";
            case CRVIDEOSDK_NETWORK_INITFAILED:
                return "网络初始化失败";
            case CRVIDEOSDK_NO_SERVERINFO:
                return "没有服务器信息";
            case CRVIDEOSDK_NOSERVER_RSP:
                return "服务器无响应";
            case CRVIDEOSDK_CREATE_CONN_FAILED:
                return "创建连接失败";
            case CRVIDEOSDK_SOCKETEXCEPTION:
                return "socket异常";
            case CRVIDEOSDK_SOCKETTIMEOUT:
                return "网络超时";
            case CRVIDEOSDK_FORCEDCLOSECONNECTION:
                return "连接被关闭";
            case CRVIDEOSDK_CONNECTIONLOST:
                return "连接丢失";
            case CRVIDEOSDK_QUE_ID_INVALID:
                return "队列ID错误";
            case CRVIDEOSDK_QUE_NOUSER:
                return "没有用户在排队";
            case CRVIDEOSDK_QUE_USER_CANCELLED:
                return "排队用户已取消";
            case CRVIDEOSDK_QUE_SERVICE_NOT_START:
                return "队列服务没有初始化";
            case CRVIDEOSDK_ALREADY_OTHERQUE:
                return "已在其它队列排队(客户只能在一个队列排队)";
            case CRVIDEOSDK_INVALID_CALLID:
                return "无效的呼叫ID";
            case CRVIDEOSDK_ERR_CALL_EXIST:
                return "已在呼叫中";
            case CRVIDEOSDK_ERR_BUSY:
                return "对方忙";
            case CRVIDEOSDK_ERR_OFFLINE:
                return "对方不在线";
            case CRVIDEOSDK_ERR_NOANSWER:
                return "对方无应答";
            case CRVIDEOSDK_ERR_USER_NOT_FOUND:
                return "用户不存在";
            case CRVIDEOSDK_ERR_REFUSE:
                return "对方拒接";
            case CRVIDEOSDK_MEETNOTEXIST:
                return "会议不存在或已结束";
            case CRVIDEOSDK_AUTHERROR:
                return "会议密码不正确";
            case CRVIDEOSDK_MEMBEROVERFLOWERROR:
                return "会议终端数量已满（购买的license不够)";
            case CRVIDEOSDK_RESOURCEALLOCATEERROR:
                return "分配会议资源失败";
            default:
                break;
        }
        return "未知错误";
    }

}
