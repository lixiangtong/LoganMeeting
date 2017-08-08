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
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cloudroom.cloudroomvideosdk.CloudroomVideoMeeting;
import com.cloudroom.cloudroomvideosdk.CloudroomVideoMgr;
import com.cloudroom.cloudroomvideosdk.model.ASTATUS;
import com.cloudroom.cloudroomvideosdk.model.CRVIDEOSDK_ERR_DEF;
import com.cloudroom.cloudroomvideosdk.model.MemberInfo;
import com.cloudroom.cloudroomvideosdk.model.RawFrame;
import com.cloudroom.cloudroomvideosdk.model.ScreenShareImg;
import com.cloudroom.cloudroomvideosdk.model.UsrVideoId;
import com.cloudroom.cloudroomvideosdk.model.UsrVideoInfo;
import com.cloudroom.cloudroomvideosdk.model.VIDEO_WALL_MODE;
import com.cloudroom.cloudroomvideosdk.model.VSTATUS;
import com.cloudroom.tool.AndroidTool;
import com.logansoft.lubo.loganmeeting.utils.UITool;
import com.logansoft.lubo.loganmeeting.utils.VideoSDKHelper;
import com.logansoft.lubo.loganmeeting.utils.YUVVideoView;


import java.util.ArrayList;

import cn.finalteam.toolsfinal.CrashHandler;

@SuppressLint({"NewApi", "HandlerLeak", "ClickableViewAccessibility",
        "DefaultLocale"})

/**
 * 会议界面
 * @author admin
 *
 */
public class MeetingActivity extends Activity implements OnTouchListener {

    private static final String TAG = "MeetingActivity";

    private ImageView mScreenshareIV = null;
    private YUVVideoView mPeerGLSV2 = null;
    private YUVVideoView mPeerGLSV1 = null;
    private View mVideos = null;

    private CheckBox mCbSwitchCamera = null;
    private CheckBox mCbCamera = null;
    private CheckBox mCbMicphone = null;
    private ProgressBar mMicPB = null;

    private View mOptionsView = null;

    private String[] mVideoModes = null;
    private String[] mVideoSizes = null;

    private boolean mBScreenShareStarted = false;
    private HandlerThread mVideoThread = new HandlerThread("VideoThread");
    private Handler mVideoHandler = null;

    private static final int MSG_CHECK_BACKGROUND = 1001;
    private static final int MSG_HIDE_OPTION = 1002;

    private long mBackgroundTime = 0;
    private View mTopOptions;
    private CheckBox mCbVolume;
    private TextView mTvRightSetting;
    private LinearLayout mLiRightSettings;
    private TextView mTvClose;
    private View mRlKeyboardAll;
    private TextView tvShowState;
    private TextView tvHideState;
    private RadioButton rbFirst;
    private RadioButton rbSecond;
    private RadioButton rbThird;
    private RadioButton rbFourth;
    private RadioButton rbFifth;
    private RadioGroup rgKeyboard;
    private YUVVideoView mPeerGLSV3;
    private YUVVideoView mPeerGLSV4;
    private YUVVideoView mPeerGLSV5;
    private ArrayList<YUVVideoView> yuvVideoViews;
    private ArrayList<UsrVideoId> videos;
    private int i = 1;
    ;
    private Callback mMainCallback = new Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case VideoCallback.MSG_VIDEODATA_UPDATED:
                    UsrVideoId userVideoId = (UsrVideoId) msg.obj;
                    videoDataUpdated(userVideoId);
                    break;
                case VideoCallback.MSG_ENTERMEETING_RSLT:
                    enterMeetingRslt((CRVIDEOSDK_ERR_DEF) msg.obj);
                    break;
                case VideoCallback.MSG_MICENERGY_UPDATED: {
                    String myUserID = CloudroomVideoMeeting.getInstance()
                            .getMyUserID();
                    String userId = (String) msg.obj;
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
                    Log.d(TAG, "handleMessage: " + "视频状态改变");
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
                case VideoCallback.MSG_USER_LEFTMEETING:
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
                case VideoCallback.MSG_NOTIFY_VIDEOWALL_MODE:
                    wallMode = (int) msg.obj;
                    Log.d(TAG, "handleMessage: wallMode=" + wallMode);
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
    private int wallMode;
    private View mRlKeyboard;
    private VIDEO_WALL_MODE videoWallMode;
    private TextView tvVideoID1;
    private TextView tvVideoID2;
    private TextView tvVideoID3;
    private TextView tvVideoID4;
    private TextView tvVideoID5;
    private ArrayList<TextView> textViews;
    private ArrayList<MemberInfo> members;

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
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_meeting);
        mScreenshareIV = (ImageView) findViewById(R.id.iv_screenshare);
        DisplayMetrics dm = getResources().getDisplayMetrics();

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
        mPeerGLSV1 = (YUVVideoView) findViewById(R.id.yuv_self);
        mPeerGLSV2 = (YUVVideoView) findViewById(R.id.yuv_peer2);
        mPeerGLSV3 = ((YUVVideoView) findViewById(R.id.yuv_peer3));
        mPeerGLSV4 = ((YUVVideoView) findViewById(R.id.yuv_peer4));
        mPeerGLSV5 = ((YUVVideoView) findViewById(R.id.yuv_peer5));
        yuvVideoViews = new ArrayList<>();
        yuvVideoViews.add(mPeerGLSV1);
        yuvVideoViews.add(mPeerGLSV2);
        yuvVideoViews.add(mPeerGLSV3);
        yuvVideoViews.add(mPeerGLSV4);
        yuvVideoViews.add(mPeerGLSV5);
        mVideos = findViewById(R.id.videos);

        tvVideoID1 = ((TextView) findViewById(R.id.tvVideoID));
        tvVideoID2 = ((TextView) findViewById(R.id.tvVideoID2));
        tvVideoID3 = ((TextView) findViewById(R.id.tvVideoID3));
        tvVideoID4 = ((TextView) findViewById(R.id.tvVideoID4));
        tvVideoID5 = ((TextView) findViewById(R.id.tvVideoID5));
        textViews = new ArrayList<>();
        textViews.add(tvVideoID1);
        textViews.add(tvVideoID2);
        textViews.add(tvVideoID3);
        textViews.add(tvVideoID4);
        textViews.add(tvVideoID5);

        mScreenshareIV.setVisibility(View.GONE);

        mPeerGLSV1.setOnTouchListener(mDragListener);

        mOptionsView = findViewById(R.id.view_options);
        mTopOptions = findViewById(R.id.rlTopOptions);
        mRlKeyboardAll = findViewById(R.id.rlKeyboardAll);
        mRlKeyboard = findViewById(R.id.rlKeyboard);
        tvShowState = ((TextView) findViewById(R.id.tvShowState));
        tvHideState = ((TextView) findViewById(R.id.tvHideState));
        rgKeyboard = ((RadioGroup) findViewById(R.id.rgKeyboard));
        rbFirst = ((RadioButton) findViewById(R.id.rbFirst));
        rbSecond = ((RadioButton) findViewById(R.id.rbSecond));
        rbThird = ((RadioButton) findViewById(R.id.rbThird));
        rbFourth = ((RadioButton) findViewById(R.id.rbFourth));
        rbFifth = ((RadioButton) findViewById(R.id.rbFifth));

        mCbSwitchCamera = (CheckBox) findViewById(R.id.cbSwitchCamera);
        mCbCamera = (CheckBox) findViewById(R.id.cbCamera);
        mCbMicphone = (CheckBox) findViewById(R.id.cbMicphone);
        mCbVolume = ((CheckBox) findViewById(R.id.cbVolume));
        mTvRightSetting = ((TextView) findViewById(R.id.right_setting));
        mLiRightSettings = ((LinearLayout) findViewById(R.id.llRightSettings));
        mTvClose = ((TextView) mLiRightSettings.findViewById(R.id.tvClose));


        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        ViewGroup.LayoutParams selfLayoutParams = mPeerGLSV1.getLayoutParams();
        ViewGroup.LayoutParams peerLayoutParams = mPeerGLSV2.getLayoutParams();
//        selfLayoutParams.width = display.getWidth()/2;
//        selfLayoutParams.height = display.getHeight()/2;
//        peerLayoutParams.width = display.getWidth()/2;
//        peerLayoutParams.height = display.getHeight()/2;


//		mMicPB = (ProgressBar) findViewById(R.id.pb_mic);

        VideoCallback.getInstance().registerVideoCallback(mMainCallback);
        VideoCallback.getInstance().registerVideoCallback(mVideoCallback);
//         VideoSDKHelper.getInstance().getVideoCallback()
//         .setMediaHandler(mVideoHandler);

        mMainHandler.sendEmptyMessageDelayed(MSG_CHECK_BACKGROUND, 10 * 1000);

        updateCameraBtn();
//        updateMicBtn();


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
        TextView tvMeetInfo = (TextView) findViewById(R.id.tvMeetInfo);
        tvMeetInfo.setText(getString(R.string.meet_prompt, meetID));
        Log.d(TAG, "onCreate 5");

        //系统音量控制
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        View view = getWindow().getDecorView();
        view.setOnTouchListener(this);

    }

    private void showEntering() {
        Log.d(TAG, "showEntering 2");
        UITool.showProcessDialog(this, getString(R.string.entering));
    }

    private void enterMeetingRslt(CRVIDEOSDK_ERR_DEF code) {
        UITool.hideProcessDialog(this);
        if (code != CRVIDEOSDK_ERR_DEF.CRVIDEOSDK_NOERR) {
            MyApplication.getInstance().showToast(R.string.enter_fail, code);
            exitMeeting();
            return;
        }

        //获取视频分屏模式
//        videoWallMode = CloudroomVideoMeeting.getInstance().getVideoWallMode();

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
        mCbCamera.setChecked(true);

        //获取所有的参会者信息
//        ArrayList<MemberInfo> members = CloudroomVideoMeeting.getInstance()
//                .getAllMembers();
//        for (int i = 0; i < members.size(); i++) {
//            Log.d(TAG, "enterMeetingRslt: members=" + members.get(i).nickName);
//        }
//        for (MemberInfo info : members) {
//            String nickname = CloudroomVideoMeeting.getInstance().getNickName(
//                    info.userId);
//            MemberInfo memInfo = CloudroomVideoMeeting.getInstance()
//                    .getMemberInfo(info.userId);
//            VSTATUS vStatus = CloudroomVideoMeeting.getInstance()
//                    .getVideoStatus(info.userId);
//            ASTATUS aStatus = CloudroomVideoMeeting.getInstance()
//                    .getAudioStatus(info.userId);
//            Log.d(TAG, "userId:" + memInfo.userId + "  nickname:" + nickname
//                    + " audioStatus:" + aStatus + "  videoStatus:" + vStatus);
//            CloudroomVideoMeeting.getInstance().sendIMmsg("test", info.userId);
//        }

//		resetVideoCfg();

        // 开启外放
        CloudroomVideoMeeting.getInstance().setSpeakerOut(true);
        boolean speakerOut = CloudroomVideoMeeting.getInstance()
                .getSpeakerOut();
        if (speakerOut) {
            mCbVolume.setChecked(true);
        }
        Log.d(TAG, "setSpeakerOut:" + speakerOut);

        showOption();
    }

    private void watchVideos() {
        //获取所有的参会者信息
        members = CloudroomVideoMeeting.getInstance()
                .getAllMembers();
        // 订阅可订阅的视频
        videos = CloudroomVideoMeeting.getInstance()
                .getWatchableVideos();
        Log.d(TAG, "getWatchableVideos videos.size()=" + videos.size());
        for (UsrVideoId id : videos) {
            Log.d(TAG, "getWatchableVideos " + id);
        }
        if (videos.size() > 0) {
            CloudroomVideoMeeting.getInstance().watchVideos(videos);
        }
        if (mPeerUsrVideoId != null) {
            Log.d(TAG, "getWatchableVideos --------------" + videos.size());
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
            mCbCamera.setChecked(true);
        } else {
            mCbCamera.setChecked(false);
        }
        updateCameraSwitchBtn();
    }

    private void updateMicBtn() {
        String userId = CloudroomVideoMeeting.getInstance().getMyUserID();
        ASTATUS status = CloudroomVideoMeeting.getInstance().getAudioStatus(
                userId);
        if (status == ASTATUS.AOPEN || status == ASTATUS.AOPENING) {
            mCbMicphone.setChecked(true);
        } else {
            mCbMicphone.setChecked(false);
        }
//        boolean showMicPB = status == ASTATUS.AOPEN
//                || status == ASTATUS.AOPENING;
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
        mCbSwitchCamera.setVisibility(showSwitch ? View.VISIBLE : View.GONE);
    }

    private void videoStatusChanged(String userID, VSTATUS newStatus,
                                    VSTATUS oldStatus) {
        String myUserID = CloudroomVideoMeeting.getInstance().getMyUserID();
        boolean open = newStatus == VSTATUS.VOPEN;
//        Log.d(TAG, "videoStatusChanged: +mPeerUsrVideoId.userId="+mPeerUsrVideoId.userId);
        if (myUserID.equals(userID)) {
            if (!open) {
                mPeerGLSV1.getYUVRender().update(null, 0, 0);
            }
        } else if (mPeerUsrVideoId != null
                && userID.equals(mPeerUsrVideoId.userId)) {
            if (!open) {
                mPeerGLSV2.getYUVRender().update(null, 0, 0);
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
        //获取我的ID
        String myUserID = CloudroomVideoMeeting.getInstance().getMyUserID();
        Log.d(TAG, "videoDataUpdated: userId.userId=" + userId.userId + "-----myUserID=" + myUserID);
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
                YUVVideoView view = null;
                for (int i = 0; i < videos.size(); i++) {
                    if (userId.userId.equals(videos.get(i).userId)) {
                        view = yuvVideoViews.get(i);
                        final int finalI = i;
                        view.getYUVRender().update(frame.dat, frame.frameWidth, frame.frameHeight);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textViews.get(finalI).setText(members.get(finalI).nickName);
                            }
                        });
                        return;
                    }
                }
//                YUVVideoView view = isSelf ? mPeerGLSV1 : mPeerGLSV3;
//                view.getYUVRender().update(frame.dat, frame.frameWidth,
//                        frame.frameHeight);
//                Log.d(TAG, "run: isSelf="+isSelf);
//                Log.d(TAG, "run: "+isSelf+"/"+frame.frameWidth+"/"+frame.frameHeight);
            }
        });
    }

    public void onViewClick(View v) {
        String userId = CloudroomVideoMeeting.getInstance().getMyUserID();
        switch (v.getId()) {
            case R.id.left_button:
                exitMeeting();
                break;
            case R.id.cbMicphone: {
                ASTATUS status = CloudroomVideoMeeting.getInstance()
                        .getAudioStatus(userId);
                if (status == ASTATUS.AOPEN || status == ASTATUS.AOPENING) {
                    CloudroomVideoMeeting.getInstance().closeMic(userId);
                    mCbMicphone.setChecked(false);
                } else {
                    CloudroomVideoMeeting.getInstance().openMic(userId);
                    mCbMicphone.setChecked(true);
                }
            }
            break;
            case R.id.cbCamera: {
                VSTATUS status = CloudroomVideoMeeting.getInstance()
                        .getVideoStatus(userId);
                if (status == VSTATUS.VOPEN || status == VSTATUS.VOPENING) {
                    CloudroomVideoMeeting.getInstance().closeVideo(userId);
                    mCbCamera.setChecked(false);

                } else {
                    CloudroomVideoMeeting.getInstance().openVideo(userId);
                    mCbCamera.setChecked(true);
                }
            }
            break;
            case R.id.cbSwitchCamera:
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
            case R.id.cbVolume:
                boolean speakerOut = CloudroomVideoMeeting.getInstance().getSpeakerOut();
                if (speakerOut) {
                    mCbVolume.setChecked(false);
                    CloudroomVideoMeeting.getInstance().setSpeakerOut(false);
                } else {
                    mCbVolume.setChecked(true);
                    CloudroomVideoMeeting.getInstance().setSpeakerOut(true);
                }
                break;
            case R.id.right_setting:
                mLiRightSettings.setVisibility(View.VISIBLE);
                break;
            case R.id.tvClose:
                mLiRightSettings.setVisibility(View.GONE);
                break;
//		case R.id.btn_videomode:
//			showVideoCfgDialog((Button) v, mVideoModes);
//			break;
//		case R.id.btn_videosize:
//			showVideoCfgDialog((Button) v, mVideoSizes);
//			break;
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
     * 视频view拖动监听
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
        String myUserID = CloudroomVideoMeeting.getInstance().getMyUserID();
        String mainVideoID = CloudroomVideoMeeting.getInstance().getMainVideo();
        mMainHandler.removeMessages(MSG_HIDE_OPTION);
        mOptionsView.setVisibility(View.VISIBLE);
        mTopOptions.setVisibility(View.VISIBLE);
        Log.d(TAG, "showOption -----" + mainVideoID + "/" + myUserID);
        if (mainVideoID.equals(myUserID)) {
            Log.d(TAG, "showOption -----");
            mRlKeyboard.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "showOption =====");
            mRlKeyboard.setVisibility(View.GONE);
        }
        mMainHandler.sendEmptyMessageDelayed(MSG_HIDE_OPTION, 3 * 1000);
    }

    private void hideOption() {
        Log.d(TAG, "hideOption");
        mMainHandler.removeMessages(MSG_HIDE_OPTION);
        mOptionsView.setVisibility(View.GONE);
        mTopOptions.setVisibility(View.GONE);
        mRlKeyboard.setVisibility(View.GONE);

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
//							resetVideoCfg();
                        }
                        dlg.dismiss();
                    }
                });
        alertBuilder.create().show();
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
//        CloudroomVideoMeeting.getInstance().exitMeeting();
        VideoCallback.getInstance().unregisterVideoCallback(mMainCallback);
        VideoCallback.getInstance().unregisterVideoCallback(mVideoCallback);
        CloudroomVideoMgr.getInstance().logout();
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
}
