package com.logansoft.lubo.loganmeeting.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.logansoft.lubo.loganmeeting.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by logansoft on 2017/7/6.
 */

public class SettingsFragment extends Fragment {
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
    Unbinder unbinder;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
