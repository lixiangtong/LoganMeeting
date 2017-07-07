package com.logansoft.lubo.loganmeeting.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.logansoft.lubo.loganmeeting.R;

/**
 * Created by logansoft on 2017/7/6.
 */

public class SettingsFragment extends Fragment {
    private View view;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view==null){
            view = inflater.inflate(R.layout.settings_fragment,container,false);
        }
        return view;
    }
}
