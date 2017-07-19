package com.logansoft.lubo.loganmeeting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudroom.cloudroomvideosdk.CloudroomVideoMgr;
import com.cloudroom.cloudroomvideosdk.CloudroomVideoSDK;
import com.cloudroom.cloudroomvideosdk.model.CRVIDEOSDK_ERR_DEF;
import com.cloudroom.cloudroomvideosdk.model.LoginDat;
import com.logansoft.lubo.loganmeeting.utils.MD5Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MeetingLoginActivity extends Activity {
    private static final String TAG = "MeetingLoginActivity";

    @BindView(R.id.ivLogo)
    ImageView ivLogo;
    @BindView(R.id.ctl)
    CollapsingToolbarLayout ctl;
    @BindView(R.id.abl)
    AppBarLayout abl;
    @BindView(R.id.tvMeeting)
    TextView tvMeeting;
    @BindView(R.id.etMeeting)
    EditText etMeeting;
    @BindView(R.id.tvMeetingName)
    TextView tvMeetingName;
    @BindView(R.id.etMeetingNick)
    EditText etMeetingNick;
    @BindView(R.id.llMeetting)
    LinearLayout llMeetting;
    @BindView(R.id.btnMeetingLogin)
    Button btnMeetingLogin;
    @BindView(R.id.btnAccount)
    Button btnAccount;
    @BindView(R.id.tvNetworkSetting)
    TextView tvNetworkSetting;
    private int meetID = -1;

    public Callback mLoginCallback = new Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MgrCallback.MSG_LOGIN_SUCCESS:
                    Log.d(TAG, "MSG_LOGIN_SUCCESS");
                    btnMeetingLogin.setClickable(true);
                    btnMeetingLogin.setEnabled(true);

                    Intent intent = new Intent(MeetingLoginActivity.this, MeetingActivity.class);
                    intent.putExtra("meetID", meetID);
                    intent.putExtra("password","");
                    startActivity(intent);

                    MyApplication.getInstance().showToast(R.string.meeting_login_success);
                    break;
                case MgrCallback.MSG_LOGIN_FAIL:
                    btnMeetingLogin.setClickable(true);
                    btnMeetingLogin.setEnabled(true);

                    CRVIDEOSDK_ERR_DEF sdkErr = (CRVIDEOSDK_ERR_DEF) msg.obj;
                    MyApplication.getInstance().showToast(R.string.login_fail, sdkErr);
                    if (sdkErr == CRVIDEOSDK_ERR_DEF.CRVIDEOSDK_LOGINSTATE_ERROR) {
                        CloudroomVideoMgr.getInstance().logout();
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_login);
        ButterKnife.bind(this);

        etMeeting.setText("41782832");
        etMeetingNick.setText("哈哈");
        MgrCallback.getInstance().registerMgrCallback(mLoginCallback);
    }

    //会议号登录
    @OnClick(R.id.btnMeetingLogin)
    public void onBtnMeetingLoginClick(View v) {
        String meetingNumber = etMeeting.getText().toString();
        String meetingNick = etMeetingNick.getText().toString();
        if (TextUtils.isEmpty(meetingNumber)) {
            MyApplication.getInstance().showToast(R.string.null_meeting_number);
            return;
        }

        if (TextUtils.isEmpty(meetingNick)) {
            MyApplication.getInstance().showToast(R.string.null_nickname);
            return;
        }
        CloudroomVideoSDK.getInstance().setServerAddr("www.cloudroom.com");
        try {
            meetID = Integer.parseInt(meetingNumber);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (meetID < 0) {
            MyApplication.getInstance().showToast(R.string.err_meetid_prompt);
            return;
        }

        CloudroomVideoSDK.getInstance().setServerAddr("www.cloudroom.com");

        LoginDat loginDat = new LoginDat();
        // 昵称
        loginDat.nickName = meetingNick;
        // 第三方账号
        loginDat.privAcnt = "demo@cloudroom.com";
        //云屋鉴权账号
        loginDat.authAcnt = "demo@cloudroom.com";
        // 登录密码必须做MD5处理
        loginDat.authPswd = MD5Util.MD5("123456");
        // 登录
        CloudroomVideoMgr.getInstance().login(loginDat);

        // 登录过程中登录按钮不可用
        btnMeetingLogin.setClickable(false);
        btnMeetingLogin.setEnabled(false);
    }

    @OnClick(R.id.btnAccount)
    public void onBtnAccountClick(View v) {
        startActivity(new Intent(MeetingLoginActivity.this, AccountLoginActivity.class));
    }

    @OnClick(R.id.tvNetworkSetting)
    public void onTvNetworkSettingClick(View v) {
        startActivity(new Intent(MeetingLoginActivity.this, NetSettingActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MgrCallback.getInstance().unregisterMgrCallback(mLoginCallback);
    }
}
