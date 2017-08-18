package com.logansoft.lubo.loganmeeting;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudroom.cloudroomvideosdk.CloudroomVideoMgr;
import com.cloudroom.cloudroomvideosdk.CloudroomVideoSDK;
import com.cloudroom.cloudroomvideosdk.model.CRVIDEOSDK_ERR_DEF;
import com.cloudroom.cloudroomvideosdk.model.LoginDat;
import com.cloudroom.cloudroomvideosdk.model.UserInfo;
import com.logansoft.lubo.loganmeeting.utils.MD5Util;
import com.logansoft.lubo.loganmeeting.utils.UITool;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class AccountLoginActivity extends Activity implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = "AccountLoginActivity";
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
    @BindView(R.id.btnAccountLogin)
    Button btnAccountLogin;
    @BindView(R.id.btnMeeting)
    Button btnMeeting;
    @BindView(R.id.tvNetworkSetting)
    TextView tvNetworkSetting;
    @BindView(R.id.nsv)
    NestedScrollView nsv;
    @BindView(R.id.ct)
    CoordinatorLayout ct;


    private AlertDialog mAssignDailog = null;
    private UserInfo mAssignUserInfo = null;
    private boolean mAcceptAssignUser = false;
    private String[] per = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};


    public Callback mLoginCallback = new Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MgrCallback.MSG_LOGIN_SUCCESS:
                    Log.d(TAG, "MSG_LOGIN_SUCCESS");
                    btnAccountLogin.setClickable(true);
                    btnAccountLogin.setEnabled(true);

                    Intent intent = new Intent(AccountLoginActivity.this, MainActivity.class);
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
    private int meetID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_login);
        ButterKnife.bind(this);

        receivePermission();
//        initEvent();
        etAccount.setText("lixt@30168114");
        etAccountPassword.setText("123456");


        // 设置登录相关处理对象
        MgrCallback.getInstance().registerMgrCallback(mLoginCallback);

        //NestedScrollView禁止滑动事件
        nsv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        return true;
                    default:
                        break;
                }
                return true;
            }
        });
        //CoordinatorLayout禁止滑动事件
        ct.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        return true;
                    default:
                        break;
                }
                return true;
            }
        });
    }


    private void receivePermission() {
        AndPermission.with(this)
                .requestCode(300)
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
                .callback(this)
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, Rationale
                            rationale) {
                        AndPermission.rationaleDialog(AccountLoginActivity.this, rationale).show();
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
        startActivity(new Intent(AccountLoginActivity.this, NetSettingActivity.class));
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
//        loginDat.nickName = account;
        // 第三方账号
        loginDat.privAcnt = account;
        //云屋鉴权账号
        loginDat.authAcnt = account;
        // 登录密码必须做MD5处理
        loginDat.authPswd = MD5Util.MD5(accountPass);
        // 登录
        CloudroomVideoMgr.getInstance().login(loginDat);


//        String server = etAccount.getText().toString();
//        String nickName = etAccountPassword.getText().toString();
//			if (TextUtils.isEmpty(server)) {
//				MyApplication.getInstance().showToast(R.string.null_server);
//				return;
//			}
//			if (TextUtils.isEmpty(nickName)) {
//                MyApplication.getInstance().showToast(R.string.null_nickname);
//				return;
//			}
//			CloudroomVideoSDK.getInstance().setServerAddr(server);
//
//			LoginDat loginDat = new LoginDat();
//			// 昵称
//			loginDat.nickName = nickName;
//			// 第三方账号
//			loginDat.privAcnt = nickName;
//            //云屋鉴权账号
//			loginDat.authAcnt = "demo@cloudroom.com";
//			// 登录密码必须做MD5处理
//			loginDat.authPswd = MD5Util.MD5("123456");
//			// 登录
//			CloudroomVideoMgr.getInstance().login(loginDat);

        // 登录过程中登录按钮不可用
        btnAccountLogin.setClickable(false);
        btnAccountLogin.setEnabled(false);
    }

    @OnClick(R.id.btnMeeting)
    public void onBtnMeetingClick(View v) {
        Intent intent = new Intent(AccountLoginActivity.this, MeetingLoginActivity.class);
        startActivity(intent);
        finish();
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
        if (EasyPermissions.hasPermissions(this, per)) {
            Log.d("Debug", "有权限");
        } else {
            Log.d("Debug", "没权限");
            EasyPermissions.requestPermissions(this, "需要拍照录音和写内存", 1001, per);
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
        Log.d("Debug", "申请成功");
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d("Debug", "申请拒绝");
        if (EasyPermissions.somePermissionPermanentlyDenied(this, Arrays.asList(per))) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }
}
