package com.logansoft.lubo.loganmeeting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends Activity {

    @BindView(R.id.ivLogo)
    ImageView ivLogo;
    @BindView(R.id.ctl)
    CollapsingToolbarLayout ctl;
    @BindView(R.id.abl)
    AppBarLayout abl;
    @BindView(R.id.etAccount)
    EditText etAccount;
    @BindView(R.id.etAccountPassword)
    EditText etAccountPassword;
    @BindView(R.id.etMeeting)
    EditText etMeeting;
    @BindView(R.id.etMeetingPassword)
    EditText etMeetingPassword;
    @BindView(R.id.accountLogin)
    Button accountLogin;
    @BindView(R.id.btnMeeting)
    Button btnMeeting;
    @BindView(R.id.btnAccount)
    Button btnAccount;
    @BindView(R.id.tvNetworkSetting)
    TextView tvNetworkSetting;
    @BindView(R.id.tvAccount)
    TextView tvAccount;
    @BindView(R.id.tvAccountPass)
    TextView tvAccountPass;
    @BindView(R.id.tvMeeting)
    TextView tvMeeting;
    @BindView(R.id.tvMeetingPass)
    TextView tvMeetingPass;
    @BindView(R.id.llAccount)
    LinearLayout llAccount;
    @BindView(R.id.llMeetting)
    LinearLayout llMeetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

    }

    @OnClick(R.id.tvNetworkSetting)
    public void onTvNetworkSettingClick(View v) {
        startActivity(new Intent(LoginActivity.this, NetSettingActivity.class));
    }

    @OnClick(R.id.accountLogin)
    public void onAccountLoginClick(View v) {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
    }

    @OnClick(R.id.btnMeeting)
    public void onBtnMeetingClick(View v) {
        llMeetting.setVisibility(View.VISIBLE);
        llAccount.setVisibility(View.GONE);
        btnMeeting.setVisibility(View.GONE);
        btnAccount.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.btnAccount)
    public void onBtnAccountgClick(View v) {
        llAccount.setVisibility(View.VISIBLE);
        llMeetting.setVisibility(View.GONE);
        btnAccount.setVisibility(View.GONE);
        btnMeeting.setVisibility(View.VISIBLE);
    }

}
