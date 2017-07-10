package com.logansoft.lubo.loganmeeting.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.logansoft.lubo.loganmeeting.MainActivity;
import com.logansoft.lubo.loganmeeting.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.R.attr.onClick;

/**
 * Created by logansoft on 2017/7/6.
 */

public class HomeFragment extends Fragment {
    @BindView(R.id.ctl)
    CollapsingToolbarLayout ctl;
    @BindView(R.id.abl)
    AppBarLayout abl;
    @BindView(R.id.left_button)
    TextView leftButton;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.right_button)
    TextView rightButton;
    @BindView(R.id.rl)
    RelativeLayout rl;
    @BindView(R.id.rlv)
    RecyclerView rlv;
    Unbinder unbinder;
    private View view;
    private Context context = getContext();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.home_fragment, container, false);
            unbinder = ButterKnife.bind(this, view);
        }
        return view;
    }

    @OnClick(R.id.rl)
    public void OnRlClick(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mLayout = inflater.inflate(R.layout.item_room_info, null);
        builder.setView(mLayout);
        Button btn_join_room = (Button) mLayout.findViewById(R.id.btn_join_room);
        btn_join_room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "未实现", Toast.LENGTH_SHORT).show();
            }
        });
        builder.create();
        builder.show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
