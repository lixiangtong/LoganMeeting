package com.logansoft.lubo.loganmeeting;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;

import com.cloudroom.cloudroomvideosdk.CloudroomVideoMeeting.CloudroomVideoCallback;
import com.cloudroom.cloudroomvideosdk.model.ASTATUS;
import com.cloudroom.cloudroomvideosdk.model.CRVIDEOSDK_ERR_DEF;
import com.cloudroom.cloudroomvideosdk.model.ElementID;
import com.cloudroom.cloudroomvideosdk.model.MEDIA_STOP_REASON;
import com.cloudroom.cloudroomvideosdk.model.MainPage;
import com.cloudroom.cloudroomvideosdk.model.RECORD_FILE_STATE;
import com.cloudroom.cloudroomvideosdk.model.Size;
import com.cloudroom.cloudroomvideosdk.model.SubPage;
import com.cloudroom.cloudroomvideosdk.model.SubPageInfo;
import com.cloudroom.cloudroomvideosdk.model.UsrVideoId;
import com.cloudroom.cloudroomvideosdk.model.VSTATUS;

import java.util.ArrayList;
import java.util.LinkedList;

public class VideoCallback implements CloudroomVideoCallback {

	private static final String TAG = "VideoCallback";

	public static final int MSG_VIDEODATA_UPDATED = 100;
	public static final int MSG_ENTERMEETING_RSLT = 101;
	public static final int MSG_MICENERGY_UPDATED = 102;
	public static final int MSG_VIDEODEV_CHANGED = 103;
	public static final int MSG_NETSTATE_CHANGED = 104;
	public static final int MSG_AUDIOSTATUS_CHANGED = 105;
	public static final int MSG_VIDEOSTATUS_CHANGED = 106;

	public static final int MSG_SYSTEM_DROPPED = 107;
	public static final int MSG_USER_ENTERMEETING = 108;

	public static final int MSG_SYSTEM_STOPED = 109;
	public static final int MSG_DEFVIDEO_CHANGED = 110;

	public static final int MSG_NOTIFY_SCREENSHARE_DATA = 111;
	public static final int MSG_NOTIFY_SCREENSHARE_STARTRD = 112;
	public static final int MSG_NOTIFY_SCREENSHARE_STOPPED = 113;

	public static final int MSG_NOTIFY_MEDIA_DATA = 114;
	public static final int MSG_NOTIFY_MEDIA_STARTRD = 115;
	public static final int MSG_NOTIFY_MEDIA_PAUSED = 116;
	public static final int MSG_NOTIFY_MEDIA_STOPPED = 117;

	private LinkedList<Callback> mVideoCallbacks = new LinkedList<Callback>();

	public void registerVideoCallback(Callback callback) {
		if (mVideoCallbacks.contains(callback)) {
			return;
		}
		mVideoCallbacks.add(callback);
	}

	public void unregisterVideoCallback(Callback callback) {
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

	private VideoCallback() {

	}

	private static VideoCallback mInstance = null;

	public static VideoCallback getInstance() {
		synchronized (TAG) {
			if (mInstance == null) {
				mInstance = new VideoCallback();
			}
		}
		return mInstance;
	}

	@Override
	public void audioDevChanged() {
		// TODO Auto-generated method stub
		Log.d(TAG, "audioDevChanged");
	}

	@Override
	public void audioStatusChanged(final String userID,
                                   final ASTATUS oldStatus, final ASTATUS newStatus) {
		// TODO Auto-generated method stub
		Log.d(TAG, "audioStatusChanged");
		Message msg = mMainHandler.obtainMessage(MSG_AUDIOSTATUS_CHANGED);
		msg.obj = userID;
		msg.arg1 = oldStatus.ordinal();
		msg.arg2 = newStatus.ordinal();
		msg.sendToTarget();
	}

	@Override
	public void defVideoChanged(String userID, short videoID) {
		// TODO Auto-generated method stub
		Log.d(TAG, "defVideoChanged userID:" + userID + "  videoID:" + videoID);
		Message msg = mMainHandler.obtainMessage(MSG_DEFVIDEO_CHANGED);
		msg.arg1 = videoID;
		msg.obj = userID;
		msg.sendToTarget();
	}

	@Override
	public void recordErr(int i) {

	}

	@Override
	public void recordStateChanged(int i) {

	}

	@Override
	public void cancelUploadRecordFileErr(int i) {

	}

	@Override
	public void endMeetingRslt(CRVIDEOSDK_ERR_DEF code) {
		// TODO Auto-generated method stub
		Log.d(TAG, "endMeetingRslt");
	}

	@Override
	public void enterMeetingRslt(CRVIDEOSDK_ERR_DEF code) {
		// TODO Auto-generated method stub
		Log.d(TAG, "enterMeetingRslt");
		Message msg = mMainHandler.obtainMessage(MSG_ENTERMEETING_RSLT);
		msg.obj = code;
		msg.sendToTarget();
	}

	@Override
	public void meetingDropped() {
		// TODO Auto-generated method stub
		Log.d(TAG, "meetingDropped");
		mMainHandler.sendEmptyMessage(MSG_SYSTEM_DROPPED);
	}

	@Override
	public void meetingStopped() {
		// TODO Auto-generated method stub
		Log.d(TAG, "meetingStopped");
		mMainHandler.sendEmptyMessage(MSG_SYSTEM_STOPED);
	}

	@Override
	public void micEnergyUpdate(String userID, int oldLevel, int newLevel) {
		// TODO Auto-generated method stub
		// Log.d(TAG, "micEnergyUpdate");
		Message msg = mMainHandler.obtainMessage(MSG_MICENERGY_UPDATED);
		msg.arg1 = oldLevel;
		msg.arg2 = newLevel;
		msg.obj = userID;
		msg.sendToTarget();
	}

	@Override
	public void netStateChanged(int level) {
		// TODO Auto-generated method stub
		Log.d(TAG, "netStateChanged");
		mMainHandler.sendEmptyMessage(MSG_NETSTATE_CHANGED);
	}

	@Override
	public void notifyScreenShareData(final String userID,
			final Rect changedRect, final Size frameSize) {
		// TODO Auto-generated method stub
		// Log.d(TAG, "notifyScreenShareData");
		Message msg = mMainHandler.obtainMessage(MSG_NOTIFY_SCREENSHARE_DATA);
		msg.obj = changedRect;
		Bundle data = msg.getData();
		data.putString("userID", userID);
		msg.sendToTarget();
	}

	@Override
	public void notifyScreenShareStarted() {
		// TODO Auto-generated method stub
		Log.d(TAG, "notifyScreenShareStarted");
		mMainHandler.sendEmptyMessage(MSG_NOTIFY_SCREENSHARE_STARTRD);
	}

	@Override
	public void notifyScreenShareStopped() {
		// TODO Auto-generated method stub
		Log.d(TAG, "notifyScreenShareStopped");
		mMainHandler.sendEmptyMessage(MSG_NOTIFY_SCREENSHARE_STOPPED);
	}

	@Override
	public void notifyVideoData(final UsrVideoId usrVideoID, long frameTime) {
		// TODO Auto-generated method stub
		// Log.d(TAG, "notifyVideoData:" + usrVideoID.userId);
		Message msg = mMainHandler.obtainMessage(MSG_VIDEODATA_UPDATED);
		msg.obj = usrVideoID;
		msg.sendToTarget();
	}

	@Override
	public void userEnterMeeting(final String userID) {
		// TODO Auto-generated method stub
		Log.d(TAG, "userEnterMeeting");
		Message msg = mMainHandler.obtainMessage(MSG_USER_ENTERMEETING);
		msg.obj = userID;
		msg.sendToTarget();
		MyApplication.getInstance().showToast(userID + "进入会议");
	}

	@Override
	public void userLeftMeeting(String userID) {
		// TODO Auto-generated method stub
		Log.d(TAG, "userLeftMeeting");
	}

	@Override
	public void videoDevChanged(String userID) {
		// TODO Auto-generated method stub
		Log.d(TAG, "videoDevChanged");
		Message msg = mMainHandler.obtainMessage(MSG_VIDEODEV_CHANGED);
		msg.obj = userID;
		msg.sendToTarget();
	}

	@Override
	public void videoStatusChanged(final String userID,
                                   final VSTATUS oldStatus, final VSTATUS newStatus) {
		// TODO Auto-generated method stub
		Log.d(TAG, "videoStatusChanged");
		Message msg = mMainHandler.obtainMessage(MSG_VIDEOSTATUS_CHANGED);
		msg.obj = userID;
		msg.arg1 = oldStatus.ordinal();
		msg.arg2 = newStatus.ordinal();
		msg.sendToTarget();
	}

	@Override
	public void notifyMediaPause(String userid, boolean bPause) {
		// TODO Auto-generated method stub
		Log.d(TAG, "notifyMediaPause");
		Message msg = mMainHandler.obtainMessage(MSG_NOTIFY_MEDIA_PAUSED);
		msg.obj = userid;
		msg.arg1 = bPause ? 1 : 0;
		msg.sendToTarget();
	}

	@Override
	public void notifyPlayPosSetted(int i) {

	}

	@Override
	public void notifyMediaData(String s, int i) {

	}

	@Override
	public void notifyMediaStart(String userid) {
		// TODO Auto-generated method stub
		Log.d(TAG, "notifyMediaStart");
		Message msg = mMainHandler.obtainMessage(MSG_NOTIFY_MEDIA_STARTRD);
		msg.obj = userid;
		msg.sendToTarget();
	}

	@Override
	public void notifyMediaStop(String userid, MEDIA_STOP_REASON reason) {
		// TODO Auto-generated method stub
		Log.d(TAG, "notifyMediaStop");
		Message msg = mMainHandler.obtainMessage(MSG_NOTIFY_MEDIA_STOPPED);
		msg.obj = userid;
		msg.sendToTarget();
	}



	@Override
	public void notifyIMmsg(String fromUserID, String text) {
		// TODO Auto-generated method stub
		Log.d(TAG, "notifyIMmsg");
	}

	@Override
	public void sendIMmsgRlst(String taskID, CRVIDEOSDK_ERR_DEF sdkErr,
			String cookie) {
		// TODO Auto-generated method stub
		Log.d(TAG, "sendIMmsgRlst");
	}

	@Override
	public void notifySwitchToPage(MainPage main, SubPage sub) {
		Log.d(TAG, "notifySwitchToPage");
	}

	@Override
	public void notifyVideoWallMode(int wallMode) {
		Log.d(TAG, "notifyVideoWallMode");
	}

	@Override
	public void notifyMainVideo(String userID) {
		Log.d(TAG, "notifyMainVideo");
	}

	@Override
	public void notifyInitBoards(ArrayList<SubPageInfo> boards) {
		Log.d(TAG, "notifyInitBoards");
	}

	@Override
	public void notifyInitBoardElements(SubPage subPage,
			ArrayList<String> elementDatas, String bkImgID) {
		Log.d(TAG, "notifyInitBoardElements");
	}

	@Override
	public void notifyCreateBoard(SubPage boardID, String title, int width,
                                  int height, String operatorID) {
		Log.d(TAG, "notifyCreateBoard");
	}

	@Override
	public void notifyCloseBoard(SubPage boardID, String operatorID) {
		Log.d(TAG, "notifyCloseBoard");
	}

	@Override
	public void notifyBoardBkImage(SubPage boardID, String imgFileID,
                                   String operatorID) {
		Log.d(TAG, "notifyBoardBkImage");
	}

	@Override
	public void notifyAddBoardElement(SubPage boardID, byte[] elementData,
                                      String operatorID) {
		Log.d(TAG, "notifyAddBoardElement");
	}

	@Override
	public void notifyDelBoardElement(SubPage boardID,
                                      ArrayList<ElementID> elementIDs, String operatorID) {
		Log.d(TAG, "notifyDelBoardElement");
	}

	@Override
	public void notifyMouseHotSpot(SubPage boardID, int x, int y,
                                   String operatorID) {
		Log.d(TAG, "notifyMouseHotSpot");
	}

	@Override
	public void notifyNetDiskFileDeleteRslt(String fileID, Boolean isSucceed) {
		Log.d(TAG, "notifyNetDiskFileDeleteRslt");
	}

	@Override
	public void notifyNetDiskTransforProgress(String fileID, int percent,
			Boolean isUpload) {
		Log.d(TAG, "notifyNetDiskTransforProgress");
	}

	@Override
	public void notifyRecordFileStateChanged(String s, RECORD_FILE_STATE record_file_state) {

	}

	@Override
	public void notifyRecordFileUploadProgress(String s, int i) {

	}

	@Override
	public void uploadRecordFileErr(String s, int i) {

	}

	@Override
	public void notifyMediaOpened(int i, Size size) {

	}
}
