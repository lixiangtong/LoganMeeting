package com.logansoft.lubo.loganmeeting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
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
import com.logansoft.lubo.loganmeeting.utils.UITool;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends Activity {

    private static final String TAG = "LoginActivity";

    @BindView(R.id.ivLogo)
    ImageView ivLogo;
    @BindView(R.id.ctl)
    CollapsingToolbarLayout ctl;
    @BindView(R.id.abl)
    AppBarLayout abl;
    @BindView(R.id.tvAccount)
    TextView tvAccount;
    @BindView(R.id.etAccount)
    EditText etAccount;
    @BindView(R.id.tvAccountPass)
    TextView tvAccountPass;
    @BindView(R.id.etAccountPassword)
    EditText etAccountPassword;
    @BindView(R.id.llAccount)
    LinearLayout llAccount;
    @BindView(R.id.tvMeeting)
    TextView tvMeeting;
    @BindView(R.id.etMeeting)
    EditText etMeeting;
    @BindView(R.id.tvMeetingName)
    TextView tvMeetingName;
    @BindView(R.id.etMeetingName)
    EditText etMeetingName;
    @BindView(R.id.llMeetting)
    LinearLayout llMeetting;
    @BindView(R.id.btnAccountLogin)
    Button btnAccountLogin;
    @BindView(R.id.btnMeetingLogin)
    Button btnMeetingLogin;
    @BindView(R.id.btnMeeting)
    Button btnMeeting;
    @BindView(R.id.btnAccount)
    Button btnAccount;
    @BindView(R.id.tvNetworkSetting)
    TextView tvNetworkSetting;

    public Handler.Callback mLoginCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case VideoCallback.MSG_ENTERMEETING_RSLT:
                    Log.d(TAG, "MSG_LOGIN_SUCCESS");
                    btnMeetingLogin.setClickable(true);
                    btnMeetingLogin.setEnabled(true);
                    break;
                case MgrCallback.MSG_LOGIN_SUCCESS:
                    Log.d(TAG, "MSG_LOGIN_SUCCESS");
                    btnAccountLogin.setClickable(true);
                    btnAccountLogin.setEnabled(true);

                    Intent intent = new Intent(LoginActivity.this,
                            MainActivity.class);
                    startActivity(intent);

                    MyApplication.getInstance().showToast(R.string.login_success);
                    break;
                case MgrCallback.MSG_LOGIN_FAIL:
                    btnAccountLogin.setClickable(true);
                    btnAccountLogin.setEnabled(true);

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
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);


        // 设置登录相关处理对象
        MgrCallback.getInstance().registerMgrCallback(mLoginCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清空登录相关处理对象
        MgrCallback.getInstance().unregisterMgrCallback(mLoginCallback);
    }

    @OnClick(R.id.tvNetworkSetting)
    public void onTvNetworkSettingClick(View v) {
        startActivity(new Intent(LoginActivity.this, NetSettingActivity.class));
    }

    //账号登录
    @OnClick(R.id.btnAccountLogin)
    public void onBtnLoginClick(View v) {

        String account = etAccount.getText().toString();
        String accountPass = etAccountPassword.getText().toString();
        if (TextUtils.isEmpty(account)) {
            MyApplication.getInstance().showToast(R.string.null_account);
            return;
        }
        if (TextUtils.isEmpty(accountPass)) {
            MyApplication.getInstance().showToast(R.string.null_account_password);
            return;
        }
        CloudroomVideoSDK.getInstance().setServerAddr("www.cloudroom.com");

        LoginDat loginDat = new LoginDat();
        // 昵称
        loginDat.nickName = account;
        // 第三方账号
        loginDat.privAcnt = account;
        //云屋鉴权账号
        loginDat.authAcnt = account;
        // 登录密码必须做MD5处理
        loginDat.authPswd = MD5Util.MD5(accountPass);
        // 登录
        CloudroomVideoMgr.getInstance().login(loginDat);

        // 登录过程中登录按钮不可用
        btnAccountLogin.setClickable(false);
        btnAccountLogin.setEnabled(false);
    }

    //会议号登录
    @OnClick(R.id.btnMeetingLogin)
    public void onBtnMeetingLoginClick(View v) {
        int meetID = -1;
        String meetingNumber = etMeeting.getText().toString();
        String nickName = etMeetingName.getText().toString();
        if (TextUtils.isEmpty(meetingNumber)) {
            MyApplication.getInstance().showToast(R.string.null_meeting_number);
            return;
        }
        meetID = Integer.parseInt(meetingNumber);
        if (TextUtils.isEmpty(nickName)) {
            MyApplication.getInstance().showToast(R.string.null_nickname);
            return;
        }

        if (meetID < 0) {
            MyApplication.getInstance().showToast(R.string.err_meetid_prompt);
            return;
        }
        LoginDat loginDat = new LoginDat();

        //设置服务器地址
        MyApplication.sdkInstance.setServerAddr("www.cloudroom.com");
        // 昵称
        loginDat.nickName = nickName;
        // 第三方账号
        loginDat.privAcnt = nickName;
        //云屋鉴权账号
        loginDat.authAcnt = meetingNumber;
        // 登录密码必须做MD5处理
        loginDat.authPswd = MD5Util.MD5("123456");
        // 登录
        CloudroomVideoMgr.getInstance().login(loginDat);

        Intent intent = new Intent(LoginActivity.this, MeetingActivity.class);
        intent.putExtra("meetID",meetID);
        intent.putExtra("password","");
        startActivity(intent);

        // 登录过程中登录按钮不可用
        btnMeetingLogin.setClickable(false);
        btnMeetingLogin.setEnabled(false);
    }

    @OnClick(R.id.btnMeeting)
    public void onBtnMeetingClick(View v) {
        llMeetting.setVisibility(View.VISIBLE);
        llAccount.setVisibility(View.GONE);
        btnMeeting.setVisibility(View.GONE);
        btnAccount.setVisibility(View.VISIBLE);
        btnMeetingLogin.setVisibility(View.VISIBLE);
        btnAccountLogin.setVisibility(View.GONE);
    }

    @OnClick(R.id.btnAccount)
    public void onBtnAccountgClick(View v) {
        llAccount.setVisibility(View.VISIBLE);
        llMeetting.setVisibility(View.GONE);
        btnAccount.setVisibility(View.GONE);
        btnMeeting.setVisibility(View.VISIBLE);
        btnMeetingLogin.setVisibility(View.GONE);
        btnAccountLogin.setVisibility(View.VISIBLE);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onKeyDown " + keyCode);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onKeyUp " + keyCode);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            UITool.showConfirmDialog(this, getString(R.string.quit)
                            + getString(R.string.app_name),
                    new UITool.ConfirmDialogCallback() {

                        @Override
                        public void onOk() {
                            // TODO Auto-generated method stub
                            Process.killProcess(Process
                                    .myPid());
                        }

                        @Override
                        public void onCancel() {
                            // TODO Auto-generated method stub

                        }
                    });
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

}
