package com.logansoft.lubo.loganmeeting;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;

import com.cloudroom.cloudroomvideosdk.CloudroomVideoMgr.CloudroomVideoMgrCallback;
import com.cloudroom.cloudroomvideosdk.model.CRVIDEOSDK_ERR_DEF;
import com.cloudroom.cloudroomvideosdk.model.MeetInfo;

import java.util.LinkedList;

public class MgrCallback implements CloudroomVideoMgrCallback {
	private static final String TAG = "MgrCallback";

	public static final int MSG_LINEOFF = 11;
	public static final int MSG_LOGIN_SUCCESS = 12;
	public static final int MSG_LOGIN_FAIL = 13;
	public static final int MSG_CREATEMEETING_SUCCESS = 14;
	public static final int MSG_CREATEMEETING_FAIL = 15;

	public static final int MSG_ACCEPTCALL_SUCCESS = 16;
	public static final int MSG_HANGUPCALL_SUCCESS = 17;
	public static final int MSG_NOTIFYCALL_HUNGUP = 18;
	public static final int MSG_NOTIFYCALL_ACCEPTED = 19;
	public static final int MSG_NOTIFYCALL_IN = 20;

	private LinkedList<Callback> mVideoCallbacks = new LinkedList<Callback>();

	public void registerMgrCallback(Callback callback) {
		if (mVideoCallbacks.contains(callback)) {
			return;
		}
		mVideoCallbacks.add(callback);
	}

	public void unregisterMgrCallback(Callback callback) {
		mVideoCallbacks.remove(callback);
	}

	private Handler mMainHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			for (Callback callback : mVideoCallbacks) {
				callback.handleMessage(msg);
			}
			super.handleMessage(msg);
		}

	};

	private MgrCallback() {

	}

	private static MgrCallback mInstance = null;

	public static MgrCallback getInstance() {
		synchronized (TAG) {
			if (mInstance == null) {
				mInstance = new MgrCallback();
			}
		}
		return mInstance;
	}

	@Override
	public void createMeetingFail(final CRVIDEOSDK_ERR_DEF sdkErr, String cookie) {
		// TODO Auto-generated method stub
		Log.d(TAG, "createMeetingFail");
		Message msg = mMainHandler.obtainMessage(MSG_CREATEMEETING_SUCCESS);
		msg.obj = sdkErr;
		msg.sendToTarget();
	}

	@Override
	public void createMeetingSuccess(final MeetInfo meetInfo, String cookie) {
		// TODO Auto-generated method stub
		Log.d(TAG, "createMeetingSuccess");
		Message msg = mMainHandler.obtainMessage(MSG_CREATEMEETING_SUCCESS);
		msg.obj = meetInfo;
		Bundle data = msg.getData();
		data.putString("cookie", cookie);
		msg.setData(data);
		msg.sendToTarget();
	}

	@Override
	public void lineOff(final CRVIDEOSDK_ERR_DEF sdkErr) {
		// TODO Auto-generated method stub
		Log.d(TAG, "lineOff");
		mMainHandler.sendEmptyMessage(MSG_LINEOFF);
		MyApplication.getInstance().showToast("掉线",sdkErr);
	}

	@Override
	public void loginFail(final CRVIDEOSDK_ERR_DEF sdkErr, String cookie) {
		// TODO Auto-generated method stub
		Log.d(TAG, "loginFail");
		Message msg = mMainHandler.obtainMessage(MSG_LOGIN_FAIL);
		Bundle data = new Bundle();
		data.putString("cookie", cookie);
		msg.setData(data);
		msg.obj = sdkErr;
		msg.sendToTarget();
	}

	@Override
	public void loginSuccess(final String usrID, final String cookie) {
		// TODO Auto-generated method stub
		Log.d(TAG, "loginSuccess");
		Message msg = mMainHandler.obtainMessage(MSG_LOGIN_SUCCESS);
		msg.obj = usrID;
		Bundle data = new Bundle();
		data.putString("userID", usrID);
		data.putString("cookie", cookie);
		msg.setData(data);
		msg.sendToTarget();
	}

	@Override
	public void setDNDStatusFail(final CRVIDEOSDK_ERR_DEF sdkErr, String cookie) {
		// TODO Auto-generated method stub
		Log.d(TAG, "setDNDStatusFail");
		MyApplication.getInstance().showToast("设置状态失败",sdkErr);
	}

	@Override
	public void setDNDStatusSuccess(String cookie) {
		// TODO Auto-generated method stub
		Log.d(TAG, "setDNDStatusSuccess");
		MyApplication.getInstance().showToast("设置状态成功");
	}

	@Override
	public void acceptCallFail(String callID, final CRVIDEOSDK_ERR_DEF sdkErr,
			String cookie) {
		// TODO Auto-generated method stub
		Log.d(TAG, "acceptCallFail");
	}

	@Override
	public void acceptCallSuccess(String callID, String cookie) {
		// TODO Auto-generated method stub
		Log.d(TAG, "acceptCallSuccess");
	}

	@Override
	public void callFail(String callID, final CRVIDEOSDK_ERR_DEF sdkErr,
			String cookie) {
		// TODO Auto-generated method stub
		Log.d(TAG, "callFail");
	}

	@Override
	public void callSuccess(String callID, int meetID, String meetPswd,
			String cookie) {
		// TODO Auto-generated method stub
		Log.d(TAG, "callSuccess");
	}

	@Override
	public void hangupCallFail(String callID, final CRVIDEOSDK_ERR_DEF sdkErr,
			String cookie) {
		// TODO Auto-generated method stub
		Log.d(TAG, "hangupCallFail");
	}

	@Override
	public void hangupCallSuccess(String callID, String cookie) {
		// TODO Auto-generated method stub
		Log.d(TAG, "hangupCallSuccess");
	}

	@Override
	public void notifyCallAccepted(final String callID, final MeetInfo meetInfo) {
		// TODO Auto-generated method stub
		Log.d(TAG, "notifyCallAccepted");
	}

	@Override
	public void notifyCallHungup(String callID) {
		// TODO Auto-generated method stub
		Log.d(TAG, "notifyCallHungup");
	}

	@Override
	public void notifyCallIn(final String callID, final MeetInfo meetInfo,
			final String callerID, final String param) {
		// TODO Auto-generated method stub
		Log.d(TAG, "notifyCallIn");
	}

	@Override
	public void notifyCallRejected(String callID,
			final CRVIDEOSDK_ERR_DEF reason) {
		// TODO Auto-generated method stub
		Log.d(TAG, "notifyCallRejected");
	}

	@Override
	public void rejectCallFail(String callID, final CRVIDEOSDK_ERR_DEF sdkErr,
			String cookie) {
		// TODO Auto-generated method stub
		Log.d(TAG, "rejectCallFail");
	}

	@Override
	public void rejectCallSuccess(String callID, String cookie) {
		// TODO Auto-generated method stub
		Log.d(TAG, "rejectCallSuccess");

	}

	@Override
	public void sendCmdRlst(String sendId, CRVIDEOSDK_ERR_DEF sdkErr,
			String cookie) {
		// TODO Auto-generated method stub
		Log.d(TAG, "sendCmdRlst");
	}

	@Override
	public void sendBufferRlst(String sendId, CRVIDEOSDK_ERR_DEF sdkErr,
			String cookie) {
		// TODO Auto-generated method stub
		Log.d(TAG, "sendBufferRlst");
	}

	@Override
	public void sendFileRlst(String sendId, String fileName,
                             CRVIDEOSDK_ERR_DEF sdkErr, String cookie) {
		// TODO Auto-generated method stub
		Log.d(TAG, "sendFileRlst");
	}

	@Override
	public void sendProgress(String sendId, int sendedLen, int totalLen,
			String cookie) {
		// TODO Auto-generated method stub
		Log.d(TAG, "sendProgress");
	}

	@Override
	public void cancelSendRlst(String sendId, CRVIDEOSDK_ERR_DEF sdkErr,
			String cookie) {
		// TODO Auto-generated method stub
		Log.d(TAG, "cancelSendRlst");
	}

	@Override
	public void notifyCmdData(String sourceUserId, byte[] data) {
		// TODO Auto-generated method stub
		Log.d(TAG, "notifyCmdData");
	}

	@Override
	public void notifyBufferData(String sourceUserId, byte[] data) {
		// TODO Auto-generated method stub
		Log.d(TAG, "notifyBufferData");
	}

	@Override
	public void notifyFileData(String sourceUserId, String tmpFile,
			String orgFileName) {
		// TODO Auto-generated method stub
		Log.d(TAG, "notifyFileData");
	}

	@Override
	public void notifyCancelSend(String sendId) {
		// TODO Auto-generated method stub
		Log.d(TAG, "notifyCancelSend");
	}

}
