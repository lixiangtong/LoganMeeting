package com.logansoft.lubo.loganmeeting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.cloudroom.cloudroomvideosdk.CloudroomVideoMeeting;
import com.cloudroom.cloudroomvideosdk.model.ASTATUS;
import com.cloudroom.cloudroomvideosdk.model.CRVIDEOSDK_ERR_DEF;
import com.cloudroom.cloudroomvideosdk.model.MemberInfo;
import com.cloudroom.cloudroomvideosdk.model.RawFrame;
import com.cloudroom.cloudroomvideosdk.model.ScreenShareImg;
import com.cloudroom.cloudroomvideosdk.model.UsrVideoId;
import com.cloudroom.cloudroomvideosdk.model.UsrVideoInfo;
import com.cloudroom.cloudroomvideosdk.model.VIDEO_SIZE_TYPE;
import com.cloudroom.cloudroomvideosdk.model.VSTATUS;
import com.cloudroom.cloudroomvideosdk.model.VideoCfg;
import com.cloudroom.tool.AndroidTool;
import com.logansoft.lubo.loganmeeting.utils.UITool;
import com.logansoft.lubo.loganmeeting.utils.VideoSDKHelper;
import com.logansoft.lubo.loganmeeting.utils.YUVVideoView;


import java.util.ArrayList;

import cn.finalteam.toolsfinal.CrashHandler;

@SuppressLint({ "NewApi", "HandlerLeak", "ClickableViewAccessibility",
		"DefaultLocale" })

/**
 * 会议界面
 * @author admin
 *
 */
public class MeetingActivity extends Activity implements OnTouchListener {

	private static final String TAG = "MeetingActivity";

	private ImageView mScreenshareIV = null;
	private YUVVideoView mPeerGLSV = null;
	private YUVVideoView mSelfGLSV = null;
	private View mVideos = null;

	private Button mVideoSizeBtn = null;
	private Button mVideoModeBtn = null;
	private Button mCameraSwitchBtn = null;
	private Button mCameraBtn = null;
	private Button mMicBtn = null;
	private ProgressBar mMicPB = null;

	private View mOptionsView = null;

	private String[] mVideoModes = null;
	private String[] mVideoSizes = null;

	private boolean mBScreenShareStarted = false;
	private HandlerThread mVideoThread = new HandlerThread("VideoThread");
	private Handler mVideoHandler = null;

	private static final int MSG_CHECK_BACKGROUND = 1001;
	private static final int MSG_HIDE_OPTION = 1002;
	private Callback mMainCallback = new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case VideoCallback.MSG_VIDEODATA_UPDATED:
				videoDataUpdated((UsrVideoId) msg.obj);
				break;
			case VideoCallback.MSG_ENTERMEETING_RSLT:
				enterMeetingRslt((CRVIDEOSDK_ERR_DEF) msg.obj);
				break;
			case VideoCallback.MSG_MICENERGY_UPDATED: {
				String myUserID = CloudroomVideoMeeting.getInstance()
						.getMyUserID();
				String userId = (String) msg.obj;
//				if (myUserID.equals(userId)) {
//					mMicPB.setProgress(msg.arg2 % mMicPB.getMax());
//				}
			}
				break;
			case VideoCallback.MSG_VIDEODEV_CHANGED:
				updateCameraSwitchBtn();
				watchVideos();
				break;
			case VideoCallback.MSG_NETSTATE_CHANGED:
				break;
			case VideoCallback.MSG_AUDIOSTATUS_CHANGED:
				updateMicBtn();
				break;
			case VideoCallback.MSG_VIDEOSTATUS_CHANGED: {
				String userId = (String) msg.obj;
				VSTATUS newStatus = VSTATUS.values()[msg.arg2];
				VSTATUS oldStatus = VSTATUS.values()[msg.arg1];
				videoStatusChanged(userId, newStatus, oldStatus);
				watchVideos();
			}
				break;
			case VideoCallback.MSG_SYSTEM_DROPPED:
				MyApplication.getInstance().showToast(R.string.meet_dropped);
				exitMeeting();
				break;
			case VideoCallback.MSG_SYSTEM_STOPED:
				MyApplication.getInstance().showToast(R.string.meet_stopped);
				exitMeeting();
				break;
			case VideoCallback.MSG_USER_ENTERMEETING:
				break;
			case VideoCallback.MSG_NOTIFY_SCREENSHARE_DATA: {
				String userID = msg.getData().getString("userId");
				Rect changeRect = (Rect) msg.obj;
				notifyScreenShareData(userID, changeRect);
				break;
			}
			case VideoCallback.MSG_NOTIFY_SCREENSHARE_STOPPED:
				synchronized (TAG) {
					mBScreenShareStarted = false;
				}
				screenShareStateChanged();
				break;
			case VideoCallback.MSG_NOTIFY_SCREENSHARE_STARTRD:
				synchronized (TAG) {
					mBScreenShareStarted = true;
				}
				screenShareStateChanged();
				break;
			case VideoCallback.MSG_DEFVIDEO_CHANGED: {
				String userID = (String) msg.obj;
				short videoID = (short) msg.arg1;
				if (mPeerUsrVideoId != null
						&& mPeerUsrVideoId.userId.equals(userID)) {
					mPeerUsrVideoId.videoID = videoID;
				}
			}
				break;
			case MSG_CHECK_BACKGROUND:
				checkBackground();
				break;
			case MSG_HIDE_OPTION:
				hideOption();
				break;
			default:
				break;
			}
			return false;
		}
	};

	public Handler mMainHandler = new Handler(mMainCallback);

	private Callback mVideoCallback = new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case VideoCallback.MSG_VIDEODATA_UPDATED:
				videoDataUpdated((UsrVideoId) msg.obj);
				return true;

			default:
				break;
			}
			return false;
		}
	};

	private long mBackgroundTime = 0;
	private View mOptionsViewRight;

	private void checkBackground() {
		mMainHandler.removeMessages(MSG_CHECK_BACKGROUND);
		mMainHandler.sendEmptyMessageDelayed(MSG_CHECK_BACKGROUND, 10 * 1000);
		Context context = MyApplication.getInstance().getApplicationContext();
		boolean forground = AndroidTool.isAppForground(context,
				context.getPackageName());
		Log.d(TAG, "checkBackground forground:" + forground);
		if (forground) {
			mBackgroundTime = 0;
		} else {
			if (mBackgroundTime == 0) {
				mBackgroundTime = System.currentTimeMillis();
			} else if (System.currentTimeMillis() - mBackgroundTime > 3 * 60 * 1000) {
				Log.d(TAG, "checkBackground exitMeeting");
				mBackgroundTime = 0;
				mMainHandler.removeMessages(MSG_CHECK_BACKGROUND);
				exitMeeting();
			}
		}
	}

	private void exitMeeting() {
		Log.d(TAG, "exitMeeting 1");
		CloudroomVideoMeeting.getInstance().exitMeeting();
		Log.d(TAG, "exitMeeting 2");
		finish();
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate 1");
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		Log.d(TAG, "onCreate 2");
		setContentView(R.layout.activity_meeting_perfor);
		Log.d(TAG, "onCreate 3");
		mScreenshareIV = (ImageView) findViewById(R.id.iv_screenshare);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		Log.d(TAG, "onCreate 4");
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;

		mVideoThread.start();
		mVideoHandler = new Handler(mVideoThread.getLooper(), mVideoCallback);
		mVideoHandler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				CrashHandler.getInstance().init(getApplicationContext());
			}
		});
		mPeerGLSV = (YUVVideoView) findViewById(R.id.yuv_peer);
		mSelfGLSV = (YUVVideoView) findViewById(R.id.yuv_self);
		mVideos = findViewById(R.id.videos);

		mScreenshareIV.setVisibility(View.GONE);

		mSelfGLSV.setOnTouchListener(mDragListener);

		mOptionsView = findViewById(R.id.view_options);
		mOptionsViewRight = findViewById(R.id.view_options_right);

		mCameraSwitchBtn = (Button) findViewById(R.id.btn_switchcamera);
		mCameraBtn = (Button) findViewById(R.id.btn_camera);
		mMicBtn = (Button) findViewById(R.id.btn_mic);
		mVideoSizeBtn = (Button) findViewById(R.id.btn_videosize);
		mVideoModeBtn = (Button) findViewById(R.id.btn_videomode);

//		mMicPB = (ProgressBar) findViewById(R.id.pb_mic);

		VideoCallback.getInstance().registerVideoCallback(mMainCallback);
		// VideoSDKHelper.getInstance().getVideoCallback()
		// .setMediaHandler(mVideoHandler);

		mMainHandler.sendEmptyMessageDelayed(MSG_CHECK_BACKGROUND, 10 * 1000);

		updateCameraBtn();
		updateMicBtn();


		int meetID = getIntent().getIntExtra("meetID", 0);
		String password = getIntent().getStringExtra("password");
		if (meetID > 0) {
			VideoSDKHelper.getInstance().enterMeeting(meetID, password);
			mMainHandler.post(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					showEntering();
				}
			});
		}
//		TextView promptTV = (TextView) findViewById(R.id.tv_prompt);
//		promptTV.setText(getString(R.string.meet_prompt, meetID));
		Log.d(TAG, "onCreate 5");

		String[] videoModes = { getString(R.string.mode_fluency),
				getString(R.string.mode_quality) };
		mVideoModes = videoModes;

		String[] videoSizes = { "144*80", "224*128", "288*160", "336*192",
				"448*256", "512*288", "576*320", "640*360", "720*400",
				"848*480", "1024*576", "1280*720", "1920*1080" };
		mVideoSizes = videoSizes;

		int index = 0;
		mVideoModeBtn.setTag(index);
		mVideoModeBtn.setText(mVideoModes[index]);
		index = VIDEO_SIZE_TYPE.VSIZE_SZ_360.ordinal();
		mVideoSizeBtn.setTag(index);
		mVideoSizeBtn.setText(mVideoSizes[index]);

		setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
		View view = getWindow().getDecorView();
		view.setOnTouchListener(this);

		// Vide
	}

	private void resetVideoCfg() {
		VideoCfg cfg = CloudroomVideoMeeting.getInstance().getVideoCfg();
		int index = (Integer) mVideoModeBtn.getTag();
		if (index <= 0) {
			cfg.maxQuality = 36;
			cfg.minQuality = 22;
		} else {
			cfg.maxQuality = 25;
			cfg.minQuality = 22;
		}

		index = (Integer) mVideoSizeBtn.getTag();
		VIDEO_SIZE_TYPE size = VIDEO_SIZE_TYPE.values()[index];
		cfg.sizeType = size;

		cfg.fps = 12;
		cfg.maxbps = -1;

		String log = String.format(
				"resetVideoCfg sizeType:%s(%d) Quality:%d-%d", cfg.sizeType,
				cfg.sizeType.ordinal(), cfg.minQuality, cfg.maxQuality);
		Log.d(TAG, log);
		CloudroomVideoMeeting.getInstance().setVideoCfg(cfg);

		cfg = CloudroomVideoMeeting.getInstance().getVideoCfg();
		Log.d(TAG, "resetVideoCfg rslt sizeType:" + cfg.sizeType
				+ " minQuality:" + cfg.minQuality + " maxQuality:"
				+ cfg.maxQuality);
	}

	private void showEntering() {
		Log.d(TAG, "showEntering 2");
		UITool.showProcessDialog(this, getString(R.string.entering));
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart 2");

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume 2");

	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause 2");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop 2");

	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(TAG, "onRestart 2");

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d(TAG, "onDestroy 2");
		mVideoThread.quit();
		unwatchHeadset();
		CloudroomVideoMeeting.getInstance().exitMeeting();
		VideoCallback.getInstance().unregisterVideoCallback(mMainCallback);
//        CloudroomVideoSDK.getInstance().uninit();
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	private void enterMeetingRslt(CRVIDEOSDK_ERR_DEF code) {
		UITool.hideProcessDialog(this);
		if (code != CRVIDEOSDK_ERR_DEF.CRVIDEOSDK_NOERR) {
			MyApplication.getInstance().showToast(R.string.enter_fail, code);
			exitMeeting();
			return;
		}

		watchHeadset();
		MyApplication.getInstance().showToast(R.string.enter_success);
		updateCameraBtn();
		updateMicBtn();

		ArrayList<String> mics = new ArrayList<String>();
		ArrayList<String> spearks = new ArrayList<String>();
		CloudroomVideoMeeting.getInstance().getAudioDeviceName(mics, spearks);
		Log.d(TAG, "enterSuccess  mics:" + mics.toString() + "  spearks:"
				+ spearks.toString());

		String myUserID = CloudroomVideoMeeting.getInstance().getMyUserID();

		// 默认使用前置摄像头ͷ
		ArrayList<UsrVideoInfo> myVideos = CloudroomVideoMeeting.getInstance()
				.getAllVideoInfo(myUserID);
		for (UsrVideoInfo vInfo : myVideos) {
			if (vInfo.videoName.contains("FRONT")) {
				CloudroomVideoMeeting.getInstance().setDefaultVideo(myUserID,
						vInfo.videoID);
				break;
			}
		}
		// 打开麦克风
		CloudroomVideoMeeting.getInstance().openMic(myUserID);
		// 打开摄像头
		CloudroomVideoMeeting.getInstance().openVideo(myUserID);

		ArrayList<MemberInfo> members = CloudroomVideoMeeting.getInstance()
				.getAllMembers();
		for (int i=0;i<members.size();i++) {
			Log.d(TAG, "enterMeetingRslt: members="+members.get(i).nickName);
		}
		for (MemberInfo info : members) {
			String nickname = CloudroomVideoMeeting.getInstance().getNickName(
					info.userId);
			MemberInfo memInfo = CloudroomVideoMeeting.getInstance()
					.getMemberInfo(info.userId);
			VSTATUS vStatus = CloudroomVideoMeeting.getInstance()
					.getVideoStatus(info.userId);
			ASTATUS aStatus = CloudroomVideoMeeting.getInstance()
					.getAudioStatus(info.userId);
			Log.d(TAG, "userId:" + memInfo.userId + "  nickname:" + nickname
					+ " audioStatus:" + aStatus + "  videoStatus:" + vStatus);
			CloudroomVideoMeeting.getInstance().sendIMmsg("test", info.userId);
		}

		resetVideoCfg();

		// 开启外放
		CloudroomVideoMeeting.getInstance().setSpeakerOut(true);
		boolean speakerOut = CloudroomVideoMeeting.getInstance()
				.getSpeakerOut();
		Log.d(TAG, "setSpeakerOut:" + speakerOut);

		showOption();
	}

	private void watchVideos() {
		// 订阅可订阅的视频
		ArrayList<UsrVideoId> videos = CloudroomVideoMeeting.getInstance()
				.getWatchableVideos();
		Log.d(TAG, "getWatchableVideos " + videos.size());
		if (videos.size() > 0) {
			CloudroomVideoMeeting.getInstance().watchVideos(videos);
		}
		if (mPeerUsrVideoId != null) {
			for (UsrVideoId id : videos) {
				if (id.equals(mPeerUsrVideoId)) {
					return;
				}
			}
			mPeerUsrVideoId = null;
		}
	}

	private void updateCameraBtn() {
		String userId = CloudroomVideoMeeting.getInstance().getMyUserID();
		VSTATUS status = CloudroomVideoMeeting.getInstance().getVideoStatus(
				userId);
		if (status == VSTATUS.VOPEN || status == VSTATUS.VOPENING) {
			mCameraBtn.setText(R.string.close_camera);
		} else {
			mCameraBtn.setText(R.string.open_camera);
		}
		updateCameraSwitchBtn();
	}

	private void updateMicBtn() {
		String userId = CloudroomVideoMeeting.getInstance().getMyUserID();
		ASTATUS status = CloudroomVideoMeeting.getInstance().getAudioStatus(
				userId);
		if (status == ASTATUS.AOPEN || status == ASTATUS.AOPENING) {
			mMicBtn.setText(R.string.close_mic);
		} else {
			mMicBtn.setText(R.string.open_mic);
		}
		boolean showMicPB = status == ASTATUS.AOPEN
				|| status == ASTATUS.AOPENING;
//		mMicPB.setVisibility(showMicPB ? View.VISIBLE : View.GONE);
	}

	private void updateCameraSwitchBtn() {
		ArrayList<UsrVideoId> videos = CloudroomVideoMeeting.getInstance()
				.getWatchableVideos();
		if (videos.size() > 0) {
			CloudroomVideoMeeting.getInstance().watchVideos(videos);
		}

		String userId = CloudroomVideoMeeting.getInstance().getMyUserID();
		ArrayList<UsrVideoInfo> videoInfos = CloudroomVideoMeeting
				.getInstance().getAllVideoInfo(userId);
		VSTATUS status = CloudroomVideoMeeting.getInstance().getVideoStatus(
				userId);
		boolean showSwitch = status == VSTATUS.VOPEN
				|| status == VSTATUS.VOPENING && videoInfos.size() > 1;
		mCameraSwitchBtn.setVisibility(showSwitch ? View.VISIBLE : View.GONE);
	}

	private void videoStatusChanged(String userID, VSTATUS newStatus,
			VSTATUS oldStatus) {
		String myUserID = CloudroomVideoMeeting.getInstance().getMyUserID();
		boolean open = newStatus == VSTATUS.VOPEN;
		if (myUserID.equals(userID)) {
			if (!open) {
				mSelfGLSV.getYUVRender().update(null, 0, 0);
			}
		} else if (mPeerUsrVideoId != null
				&& userID.equals(mPeerUsrVideoId.userId)) {
			if (!open) {
				mPeerGLSV.getYUVRender().update(null, 0, 0);
				mPeerUsrVideoId = null;
			}
		}

		updateCameraBtn();
	}

	private void screenShareStateChanged() {
		MyApplication.getInstance().showToast(
				mBScreenShareStarted ? R.string.screenshare_started
						: R.string.screenshare_stopped);
		mScreenshareIV.setVisibility(mBScreenShareStarted ? View.VISIBLE
				: View.GONE);
		mVideos.setVisibility(mBScreenShareStarted ? View.GONE : View.VISIBLE);
	}

	private UsrVideoId mPeerUsrVideoId = null;

	private void videoDataUpdated(final UsrVideoId userId) {
		if (mBScreenShareStarted) {
			return;
		}
		String myUserID = CloudroomVideoMeeting.getInstance().getMyUserID();
		final boolean isSelf = myUserID.equals(userId.userId);
		mVideoHandler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				RawFrame frame = CloudroomVideoMeeting.getInstance()
						.getVideoImg(userId);
				boolean showFrame = frame != null && frame.dat != null
						&& frame.dat.length > 0;
				if (!showFrame) {
					return;
				}
				YUVVideoView view = isSelf ? mSelfGLSV : mPeerGLSV;
				view.getYUVRender().update(frame.dat, frame.frameWidth,
						frame.frameHeight);
			}
		});
	}

	public void onViewClick(View v) {
		String userId = CloudroomVideoMeeting.getInstance().getMyUserID();
		switch (v.getId()) {
		case R.id.btn_leftmeet:
			exitMeeting();
			break;
		case R.id.btn_mic: {
			ASTATUS status = CloudroomVideoMeeting.getInstance()
					.getAudioStatus(userId);
			if (status == ASTATUS.AOPEN || status == ASTATUS.AOPENING) {
				CloudroomVideoMeeting.getInstance().closeMic(userId);
			} else {
				CloudroomVideoMeeting.getInstance().openMic(userId);
			}
		}
			break;
		case R.id.btn_camera: {
			VSTATUS status = CloudroomVideoMeeting.getInstance()
					.getVideoStatus(userId);
			if (status == VSTATUS.VOPEN || status == VSTATUS.VOPENING) {
				CloudroomVideoMeeting.getInstance().closeVideo(userId);
			} else {
				CloudroomVideoMeeting.getInstance().openVideo(userId);
			}
		}
			break;
		case R.id.btn_switchcamera:
			short curDev = CloudroomVideoMeeting.getInstance().getDefaultVideo(
					userId);
			ArrayList<UsrVideoInfo> devs = CloudroomVideoMeeting.getInstance()
					.getAllVideoInfo(userId);

			if (devs.size() > 1) {
				UsrVideoInfo info = devs.get(0);
				boolean find = false;
				for (UsrVideoInfo dev : devs) {
					if (find) {
						info = dev;
					} else if (dev.videoID == curDev) {
						find = true;
					}
				}
				CloudroomVideoMeeting.getInstance().setDefaultVideo(
						info.userId, info.videoID);
			}
			break;
		case R.id.btn_videomode:
			showVideoCfgDialog((Button) v, mVideoModes);
			break;
		case R.id.btn_videosize:
			showVideoCfgDialog((Button) v, mVideoSizes);
			break;
		default:
			break;
		}

	}

	private void notifyScreenShareData(String userID, Rect changeRect) {
		ScreenShareImg img = CloudroomVideoMeeting.getInstance()
				.getShareScreenDecodeImg();
		if (img == null) {
			return;
		}

		// Log.d(TAG, "notifyScreenShareData rgbWidth:" + img.rgbWidth
		// + "  rgbHeight:" + img.rgbHeight + " dataLength:"
		// + img.rgbDat.length);
		if (img.rgbDat.length <= 0
				|| img.rgbDat.length != img.rgbWidth * img.rgbHeight) {
			return;
		}
		Bitmap screenBitmap = Bitmap.createBitmap(img.rgbDat, img.rgbWidth,
				img.rgbHeight, Bitmap.Config.ARGB_8888);
		mScreenshareIV.setImageBitmap(screenBitmap);
	}

	private HeadsetReceiver mHeadsetReceiver = null;

	private void watchHeadset() {
		if (mHeadsetReceiver != null) {
			return;
		}
		mHeadsetReceiver = new HeadsetReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		registerReceiver(mHeadsetReceiver, filter);
	}

	private void unwatchHeadset() {
		if (mHeadsetReceiver == null) {
			return;
		}
		unregisterReceiver(mHeadsetReceiver);
		mHeadsetReceiver = null;
	}

	private class HeadsetReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			Log.d(TAG, "HeadsetReceiver : " + action);
			if (intent.hasExtra("state")) {
				int state = intent.getIntExtra("state", 0);
				Log.d(TAG, "HeadsetReceiver state:" + state);
				CloudroomVideoMeeting.getInstance()
						.setSpeakerOut(!(state == 1));
				boolean speakerOut = CloudroomVideoMeeting.getInstance()
						.getSpeakerOut();
				Log.d(TAG, "setSpeakerOut:" + speakerOut);
			}
		}
	}

	private int screenWidth;
	private int screenHeight;

	/**
	 * 	视频view拖到监听
	 */
	private OnTouchListener mDragListener = new OnTouchListener() {

		private int lastX;
		private int lastY;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			int action = event.getAction();

			switch (action) {
			case MotionEvent.ACTION_DOWN:
				lastX = (int) event.getRawX();
				lastY = (int) event.getRawY();
				break;

			case MotionEvent.ACTION_MOVE:
				int dx = (int) event.getRawX() - lastX;
				int dy = (int) event.getRawY() - lastY;

				int left = v.getLeft() + dx;
				int top = v.getTop() + dy;
				int right = v.getRight() + dx;
				int bottom = v.getBottom() + dy;
				if (left < 0) {
					left = 0;
					right = left + v.getWidth();
				}
				if (right > screenWidth) {
					right = screenWidth;
					left = right - v.getWidth();
				}
				if (top < 0) {
					top = 0;
					bottom = top + v.getHeight();
				}
				if (bottom > screenHeight) {
					bottom = screenHeight;
					top = bottom - v.getHeight();
				}
				v.layout(left, top, right, bottom);
				lastX = (int) event.getRawX();
				lastY = (int) event.getRawY();
				break;
			case MotionEvent.ACTION_UP:
				break;
			}
			return true;
		}
	};

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_UP:
			showOption();
			break;
		}
		return true;
	}


	private void showOption() {
		Log.d(TAG, "showOption");
		mMainHandler.removeMessages(MSG_HIDE_OPTION);
		mOptionsView.setVisibility(View.VISIBLE);
		mOptionsViewRight.setVisibility(View.VISIBLE);
		mMainHandler.sendEmptyMessageDelayed(MSG_HIDE_OPTION, 3 * 1000);
	}

	private void hideOption() {
		Log.d(TAG, "hideOption");
		mMainHandler.removeMessages(MSG_HIDE_OPTION);
		mOptionsView.setVisibility(View.GONE);
		mOptionsViewRight.setVisibility(View.GONE);
	}

	public void showVideoCfgDialog(final Button view, final String[] items) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		int index = (Integer) view.getTag();
		alertBuilder.setSingleChoiceItems(items, index,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dlg, int index) {
						int oldIndex = (Integer) view.getTag();
						if (oldIndex != index) {
							view.setTag(index);
							view.setText(items[index]);
							resetVideoCfg();
						}
						dlg.dismiss();
					}
				});
		alertBuilder.create().show();
	}
}
