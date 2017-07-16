package com.logansoft.lubo.loganmeeting;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler.Callback;

import com.cloudroom.cloudroomvideosdk.CloudroomQueue;
import com.cloudroom.cloudroomvideosdk.CloudroomVideoMgr;
import com.cloudroom.cloudroomvideosdk.CloudroomVideoSDK;
import com.cloudroom.cloudroomvideosdk.model.CRVIDEOSDK_ERR_DEF;
import com.cloudroom.cloudroomvideosdk.model.LoginDat;
import com.cloudroom.cloudroomvideosdk.model.MeetInfo;
import com.cloudroom.cloudroomvideosdk.model.QueueInfo;
import com.cloudroom.cloudroomvideosdk.model.QueueStatus;
import com.cloudroom.cloudroomvideosdk.model.QueuingInfo;
import com.cloudroom.cloudroomvideosdk.model.UserInfo;
import com.logansoft.lubo.loganmeeting.utils.MD5Util;
import com.logansoft.lubo.loganmeeting.utils.UITool;
import com.logansoft.lubo.loganmeeting.utils.VideoSDKHelper;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class LoginActivity extends Activity implements EasyPermissions.PermissionCallbacks{

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


    private AlertDialog mAssignDailog = null;
    private UserInfo mAssignUserInfo = null;
    private boolean mAcceptAssignUser = false;
    private  String [] per = {Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE};


    public Callback mLoginCallback = new Callback() {
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
    public  Handler mMainHandler = new Handler(mLoginCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        receivePermission();
//        initEvent();
        etAccount.setText("lixt@30168114");
        etAccountPassword.setText("123456");
        etMeeting.setText("55553544");
        etMeetingName.setText("哈哈");

        // 设置登录相关处理对象
        MgrCallback.getInstance().registerMgrCallback(mLoginCallback);
    }


    private void receivePermission() {
        AndPermission.with(this)
                .requestCode(300)
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA)
                .callback(this)
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, Rationale
                            rationale) {
                        AndPermission.rationaleDialog(LoginActivity.this, rationale).show();
                    }
                })
                .start();
    }

    @PermissionYes(300)
    private void getSTORAGEYes(@NonNull List<String> grantedPermissions) {
//        Toast.makeText(this, "成功", Toast.LENGTH_SHORT).show();
    }

    @PermissionNo(300)
    private void getSTORAGENo(@NonNull List<String> deniedPermissions) {
        Toast.makeText(this, "拒绝授权", Toast.LENGTH_SHORT).show();
        // 用户否勾选了不再提示并且拒绝了权限，那么提示用户到设置中授权
        if (AndPermission.hasAlwaysDeniedPermission(this, deniedPermissions)) {
            AndPermission.defaultSettingDialog(this, 300).show();
        }
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
        try {
            meetID = Integer.parseInt(meetingNumber);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
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

    private void initEvent() {
        if (EasyPermissions.hasPermissions(this,per)){
            Log.d("Debug","有权限");
        }else{
            Log.d("Debug","没权限");
            EasyPermissions.requestPermissions(this,"需要拍照录音和写内存",1001,per);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d("Debug","申请成功");
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d("Debug","申请拒绝");
        if (EasyPermissions.somePermissionPermanentlyDenied(this, Arrays.asList(per))) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }
}
