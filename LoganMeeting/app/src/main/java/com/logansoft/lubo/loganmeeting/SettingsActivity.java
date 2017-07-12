package com.logansoft.lubo.loganmeeting;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cloudroom.cloudroomvideosdk.CloudroomVideoMgr;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.left_button)
    TextView leftButton;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.right_button)
    TextView rightButton;
    @BindView(R.id.rl)
    RelativeLayout rl;
    @BindView(R.id.llChangePassword)
    LinearLayout llChangePassword;
    @BindView(R.id.llAbout)
    LinearLayout llAbout;
    @BindView(R.id.llAccount)
    LinearLayout llAccount;
    @BindView(R.id.llChangeMeetingPassword)
    LinearLayout llChangeMeetingPassword;
    @BindView(R.id.llChangePassword2)
    LinearLayout llChangePassword2;
    @BindView(R.id.llAbout2)
    LinearLayout llAbout2;
    @BindView(R.id.llMeetting)
    LinearLayout llMeetting;
    @BindView(R.id.btn_logout)
    Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        title.setText("设置");

    }

    @OnClick(R.id.btn_logout)
    public void onBtnLogoutClick(View v){
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.mgrInstance.logout();
        MyApplication.sdkInstance.uninit();
    }
}
