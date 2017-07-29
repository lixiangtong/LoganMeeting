package com.logansoft.lubo.loganmeeting.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.cloudroom.cloudroomvideosdk.CloudroomVideoMgr;
import com.cloudroom.cloudroomvideosdk.CloudroomVideoSDK;
import com.logansoft.lubo.loganmeeting.AboutActivity;
import com.logansoft.lubo.loganmeeting.ChangePasswordActivity;
import com.logansoft.lubo.loganmeeting.MgrCallback;
import com.logansoft.lubo.loganmeeting.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by logansoft on 2017/7/6.
 */

public class SettingsFragment extends Fragment {
    @BindView(R.id.llChangeMeetingPassword)
    LinearLayout llChangeMeetingPassword;
    @BindView(R.id.llChangePassword)
    LinearLayout llChangePassword;
    @BindView(R.id.llAbout)
    LinearLayout llAbout;
    @BindView(R.id.llMeetting)
    LinearLayout llMeetting;
    Unbinder unbinder;
    @BindView(R.id.vChangeMeetingPass)
    View vChangeMeetingPass;
    @BindView(R.id.btnLogout)
    Button btnLogout;
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.settings_fragment, container, false);
            unbinder = ButterKnife.bind(this, view);
        }
        return view;
    }


    @OnClick(R.id.llAbout)
    public void onLlAboutClick(View v) {
        startActivity(new Intent(getActivity(), AboutActivity.class));
    }

    @OnClick(R.id.llChangeMeetingPassword)
    public void onLlChangeMeetingPasswordClick(View v) {
        startActivity(new Intent(getActivity(), ChangePasswordActivity.class));
    }

    @OnClick(R.id.llChangePassword)
    public void onLlChangePasswordClick(View v) {
        startActivity(new Intent(getActivity(), ChangePasswordActivity.class));
    }

    @OnClick(R.id.btnLogout)
    public void onBtnLogoutClick(View v) {
        CloudroomVideoMgr.getInstance().logout();
        getActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
