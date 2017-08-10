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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
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
import com.logansoft.lubo.loganmeeting.utils.PinyinComparator;
import com.logansoft.lubo.loganmeeting.utils.UITool;
import com.logansoft.lubo.loganmeeting.utils.VideoSDKHelper;
import com.logansoft.lubo.loganmeeting.utils.YUVVideoView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
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
    @BindView(R.id.rlMode1v1)
    RelativeLayout rlMode1v1;
    @BindView(R.id.llMode1)
    LinearLayout llMode1;
    @BindView(R.id.llMode2)
    LinearLayout llMode2;
    @BindView(R.id.llMode4)
    LinearLayout llMode4;
    @BindView(R.id.llMode5)
    LinearLayout llMode5;

    private ImageView mScreenshareIV = null;
    private YUVVideoView mPeerGLSV52 = null;
    private YUVVideoView mPeerGLSV51 = null;
    private CheckBox mCbSwitchCamera = null;
    private CheckBox mCbCamera = null;
    private CheckBox mCbMicphone = null;
    private ProgressBar mMicPB = null;
    private View mOptionsView = null;
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
    private YUVVideoView mPeerGLSV53;
    private YUVVideoView mPeerGLSV54;
    private YUVVideoView mPeerGLSV55;
    private ArrayList<YUVVideoView> yuvVideoViews5;
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
                    String myUserID = CloudroomVideoMeeting.getInstance().getMyUserID();
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
    private View mRlKeyboard;
    private VIDEO_WALL_MODE videoWallMode;
    private TextView tvVideoID1;
    private TextView tvVideoID2;
    private TextView tvVideoID3;
    private TextView tvVideoID4;
    private TextView tvVideoID5;
    private ArrayList<TextView> textViews;
    private ArrayList<MemberInfo> members;
    private YUVVideoView yuv_peer11;
    private YUVVideoView yuv_peer21;
    private YUVVideoView yuv_peer22;
    private YUVVideoView yuv_peer1v1;
    private YUVVideoView yuv_self1V1;
    private YUVVideoView yuv_peer41;
    private YUVVideoView yuv_peer42;
    private YUVVideoView yuv_peer43;
    private YUVVideoView yuv_peer44;
    private int wallMode;
    private ArrayList<YUVVideoView> yuvVideoViews2;
    private ArrayList<YUVVideoView> yuvVideoViews1v1;
    private ArrayList<YUVVideoView> yuvVideoViews4;
    private ArrayList<YUVVideoView> yuvVideoViews1;
    private ArrayList<String> userIDs;
    private boolean isLogout;

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
        ButterKnife.bind(this);
        mScreenshareIV = (ImageView) findViewById(R.id.iv_screenshare);
        DisplayMetrics dm = getResources().getDisplayMetrics();

        mVideoThread.start();
        mVideoHandler = new Handler(mVideoThread.getLooper(), mVideoCallback);
        mVideoHandler.post(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                CrashHandler.getInstance().init(getApplicationContext());
            }
        });
        //1分屏
        yuv_peer11 = ((YUVVideoView) findViewById(R.id.yuv_peer11));
        yuvVideoViews1 = new ArrayList<>();
        yuvVideoViews1.add(yuv_peer11);
        //2分屏
        yuv_peer21 = ((YUVVideoView) findViewById(R.id.yuv_peer21));
        yuv_peer22 = ((YUVVideoView) findViewById(R.id.yuv_peer22));
        yuvVideoViews2 = new ArrayList<>();
        yuvVideoViews2.add(yuv_peer21);
        yuvVideoViews2.add(yuv_peer22);
        //1v1分屏
        yuv_peer1v1 = ((YUVVideoView) findViewById(R.id.yuv_peer1V1));
        yuv_self1V1 = ((YUVVideoView) findViewById(R.id.yuv_self1V1));
        yuvVideoViews1v1 = new ArrayList<>();
        yuvVideoViews1v1.add(yuv_peer1v1);
        yuvVideoViews1v1.add(yuv_self1V1);
        //4分屏
        yuv_peer41 = ((YUVVideoView) findViewById(R.id.yuv_peer41));
        yuv_peer42 = ((YUVVideoView) findViewById(R.id.yuv_peer42));
        yuv_peer43 = ((YUVVideoView) findViewById(R.id.yuv_peer43));
        yuv_peer44 = ((YUVVideoView) findViewById(R.id.yuv_peer44));
        yuvVideoViews4 = new ArrayList<>();
        yuvVideoViews4.add(yuv_peer41);
        yuvVideoViews4.add(yuv_peer42);
        yuvVideoViews4.add(yuv_peer43);
        yuvVideoViews4.add(yuv_peer44);
        //五分屏
        mPeerGLSV51 = (YUVVideoView) findViewById(R.id.yuv_peer51);
        mPeerGLSV52 = (YUVVideoView) findViewById(R.id.yuv_peer52);
        mPeerGLSV53 = ((YUVVideoView) findViewById(R.id.yuv_peer53));
        mPeerGLSV54 = ((YUVVideoView) findViewById(R.id.yuv_peer54));
        mPeerGLSV55 = ((YUVVideoView) findViewById(R.id.yuv_peer55));
        yuvVideoViews5 = new ArrayList<>();
        yuvVideoViews5.add(mPeerGLSV51);
        yuvVideoViews5.add(mPeerGLSV52);
        yuvVideoViews5.add(mPeerGLSV53);
        yuvVideoViews5.add(mPeerGLSV54);
        yuvVideoViews5.add(mPeerGLSV55);

        mScreenshareIV.setVisibility(View.GONE);

//        mPeerGLSV51.setOnTouchListener(mDragListener);

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

        VideoCallback.getInstance().registerVideoCallback(mMainCallback);
        VideoCallback.getInstance().registerVideoCallback(mVideoCallback);
        mMainHandler.sendEmptyMessageDelayed(MSG_CHECK_BACKGROUND, 10 * 1000);

        updateCameraBtn();

        int meetID = getIntent().getIntExtra("meetID", 0);
        String password = getIntent().getStringExtra("password");
        isLogout = getIntent().getBooleanExtra("isLogout",false);
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
        watchHeadset();
        MyApplication.getInstance().showToast(R.string.enter_success);
        updateCameraBtn();
        updateMicBtn();

        VIDEO_WALL_MODE videoWallMode = CloudroomVideoMeeting.getInstance().getVideoWallMode();
        wallMode = videoWallMode.ordinal();
        Log.d(TAG, "videoDataUpdated wallMode111=" + wallMode);

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

    private UsrVideoId mPeerUsrVideoId = null;

    private void videoDataUpdated(final UsrVideoId userId) {
        if (mBScreenShareStarted) {
            return;
        }
        //获取我的ID
        String myUserID = CloudroomVideoMeeting.getInstance().getMyUserID();
        Log.d(TAG, "videoDataUpdated: userId.userId=" + userId.userId + "-----myUserID=" + myUserID);
        Log.d(TAG, "videoDataUpdated wallMode=" + wallMode);
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
//                getYuvView(frame,userId,yuvVideoViews5,5);
                switch (wallMode){
                    case 0:
                        stopOthersViewUpdate(yuvVideoViews1,yuvVideoViews2,yuvVideoViews4,yuvVideoViews5);
                        showOrHideView(rlMode1v1,llMode1,llMode2,llMode4,llMode5);
                        getYuvView(frame, userId,yuvVideoViews1v1,2);
                        break;
                    case 1:
                        stopOthersViewUpdate(yuvVideoViews1v1,yuvVideoViews2,yuvVideoViews4,yuvVideoViews5);
                        showOrHideView(llMode1,rlMode1v1,llMode2,llMode4,llMode5);
                        getYuvView(frame, userId,yuvVideoViews1,1);
                        break;
                    case 2:
                        stopOthersViewUpdate(yuvVideoViews1v1,yuvVideoViews1,yuvVideoViews4,yuvVideoViews5);
                        showOrHideView(llMode2,rlMode1v1,llMode1,llMode4,llMode5);
                        getYuvView(frame, userId,yuvVideoViews2,2);
                        break;
                    case 3:
                        stopOthersViewUpdate(yuvVideoViews1v1,yuvVideoViews1,yuvVideoViews2,yuvVideoViews5);
                        showOrHideView(llMode4,rlMode1v1,llMode1,llMode2,llMode5);
                        getYuvView(frame, userId,yuvVideoViews4,4);
                        break;
                    case 4:
                        stopOthersViewUpdate(yuvVideoViews1v1,yuvVideoViews1,yuvVideoViews2,yuvVideoViews4);
                        showOrHideView(llMode5,rlMode1v1,llMode1,llMode2,llMode4);
                        getYuvView(frame, userId,yuvVideoViews5,5);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void stopOthersViewUpdate(ArrayList<YUVVideoView> yuvVideoViews1,ArrayList<YUVVideoView> yuvVideoViews2,ArrayList<YUVVideoView> yuvVideoViews3,ArrayList<YUVVideoView> yuvVideoViews4) {
        for (int i = 0; i < yuvVideoViews1.size(); i++) {
            YUVVideoView yuvVideoView = yuvVideoViews1.get(i);
            yuvVideoView.getYUVRender().update(null,0,0);
//            yuvVideoView.setBackground(null);
        }
        for (int i = 0; i < yuvVideoViews2.size(); i++) {
            YUVVideoView yuvVideoView = yuvVideoViews2.get(i);
            yuvVideoView.getYUVRender().update(null,0,0);
//            yuvVideoView.setBackground(null);
        }
        for (int i = 0; i < yuvVideoViews3.size(); i++) {
            YUVVideoView yuvVideoView = yuvVideoViews3.get(i);
            yuvVideoView.getYUVRender().update(null,0,0);
//            yuvVideoView.setBackground(null);
        }
        for (int i = 0; i < yuvVideoViews4.size(); i++) {
            YUVVideoView yuvVideoView = yuvVideoViews4.get(i);
            yuvVideoView.getYUVRender().update(null,0,0);
//            yuvVideoView.setBackground(null);
        }
    }

    private void showOrHideView(final View showView, final View hideView1, final View hideView2, final View hideView3, final View hideView4) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showView.setVisibility(View.VISIBLE);
                hideView1.setVisibility(View.GONE);
                hideView2.setVisibility(View.GONE);
                hideView3.setVisibility(View.GONE);
                hideView4.setVisibility(View.GONE);
            }
        });
    }

    private void getYuvView(RawFrame frame, UsrVideoId userId,ArrayList<YUVVideoView> yuvVideoViews,int mode) {
        YUVVideoView view;
        for (int i = 0; i < mode; i++) {
            if (userIDs.get(i).equals(userId.userId)) {
                view = yuvVideoViews.get(i);
                final int finalI = i;
                view.getYUVRender().update(frame.dat, frame.frameWidth, frame.frameHeight);
                final YUVVideoView finalView = view;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                                textViews.get(finalI).setText(members.get(finalI).nickName);
                        finalView.setBackground(null);
                    }
                });
                return;
            }
        }
    }

    private void videoStatusChanged(String userID, VSTATUS newStatus,
                                    VSTATUS oldStatus) {
        String myUserID = CloudroomVideoMeeting.getInstance().getMyUserID();
        boolean open = newStatus == VSTATUS.VOPEN;
//        Log.d(TAG, "videoStatusChanged: +mPeerUsrVideoId.userId="+mPeerUsrVideoId.userId);
        if (myUserID.equals(userID)) {
            if (!open) {
                mPeerGLSV51.getYUVRender().update(null, 0, 0);
            }
        } else if (mPeerUsrVideoId != null
                && userID.equals(mPeerUsrVideoId.userId)) {
            if (!open) {
                mPeerGLSV52.getYUVRender().update(null, 0, 0);
                mPeerUsrVideoId = null;
            }
        }
        updateCameraBtn();
    }

    private void watchVideos() {
        //获取所有的参会者信息
        members = CloudroomVideoMeeting.getInstance()
                .getAllMembers();
        // 订阅可订阅的视频
        videos = CloudroomVideoMeeting.getInstance()
                .getWatchableVideos();
        userIDs = new ArrayList<>();
        for (int i = 0; i < videos.size(); i++) {
            userIDs.add(videos.get(i).userId);
            Log.d(TAG, "watchVideos: videos.get(i).userId="+videos.get(i).userId);
        }
        PinyinComparator pinyinComparator = new PinyinComparator();
        Collections.sort(userIDs,pinyinComparator);
        for (int i = 0; i < videos.size(); i++) {
            Log.d(TAG, "watchVideos: userIDs.get(i)=" + userIDs.get(i));
        }

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

    private void screenShareStateChanged() {
        MyApplication.getInstance().showToast(
                mBScreenShareStarted ? R.string.screenshare_started
                        : R.string.screenshare_stopped);
        mScreenshareIV.setVisibility(mBScreenShareStarted ? View.VISIBLE
                : View.GONE);
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
        if (isLogout) {
            CloudroomVideoMgr.getInstance().logout();
        }
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
