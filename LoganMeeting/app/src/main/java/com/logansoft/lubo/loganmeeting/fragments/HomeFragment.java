package com.logansoft.lubo.loganmeeting.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudroom.cloudroomvideosdk.CloudroomQueue;
import com.cloudroom.cloudroomvideosdk.model.CRVIDEOSDK_ERR_DEF;
import com.cloudroom.cloudroomvideosdk.model.QueueStatus;
import com.cloudroom.cloudroomvideosdk.model.QueuingInfo;
import com.cloudroom.cloudroomvideosdk.model.UserInfo;
import com.logansoft.lubo.loganmeeting.MeetingActivity;
import com.logansoft.lubo.loganmeeting.MyApplication;
import com.logansoft.lubo.loganmeeting.R;
import com.logansoft.lubo.loganmeeting.adapters.MyRecyclerViewAdapter;
import com.logansoft.lubo.loganmeeting.beans.RoomInfoBean;
import com.logansoft.lubo.loganmeeting.utils.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by logansoft on 2017/7/6.
 */

public class HomeFragment extends Fragment{
    private static final String TAG = "HomeFragment";
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
    @BindView(R.id.rv)
    RecyclerView rv;
    Unbinder unbinder;
    private View view;
    private RoomInfoBean roomInfoBean1;
    private RoomInfoBean roomInfoBean2;
    private List<RoomInfoBean> data = new ArrayList<>();
    private MyRecyclerViewAdapter myRecyclerViewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.home_fragment, container, false);
            unbinder = ButterKnife.bind(this, view);
        }
        roomInfoBean1 = new RoomInfoBean();
        roomInfoBean2 = new RoomInfoBean();
        roomInfoBean1.setRoomName("语文教研室");
        roomInfoBean1.setModerator("张泽军");
        roomInfoBean1.setRoomNumber("55553544");
        roomInfoBean1.setWaitCount("2");

        roomInfoBean2.setRoomName("语文教研室");
        roomInfoBean2.setModerator("张泽军");
        roomInfoBean2.setRoomNumber("41782832");
        roomInfoBean2.setWaitCount("2");
        data.add(roomInfoBean1);
        data.add(roomInfoBean2);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //设置布局管理器
        rv.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        //设置分隔线
        rv.addItemDecoration(new DividerItemDecoration(getActivity(),LinearLayoutManager.VERTICAL));
        //设置增加或删除条目的动画
        rv.setItemAnimator(new DefaultItemAnimator());

        myRecyclerViewAdapter = new MyRecyclerViewAdapter(data,getActivity() );
        Log.d(TAG, "onCreateView: "+data.size());
        rv.setAdapter(myRecyclerViewAdapter);

        setAdapterListener();

        return view;
    }

    private void setAdapterListener() {
        myRecyclerViewAdapter.setMyOnItemClickListener(new MyRecyclerViewAdapter.OnMyItemClickListener() {
            @Override
            public void onClick(int position) {
                for (int i = 0; i < data.size(); i++) {
                    String roomNumber = data.get(i).getRoomNumber();
                    int meetID = Integer.parseInt(roomNumber);
                    Intent intent = new Intent(getActivity(), MeetingActivity.class);
                    intent.putExtra("meetID",meetID);
                    intent.putExtra("password","");
                    startActivity(intent);
                }
            }

            @Override
            public void onLongClick(int position) {
                MyApplication.getInstance().showToast("你长击了第"+position+"个Item");

            }
        });
    }

    @OnClick(R.id.rl)
    public void OnRlClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mLayout = inflater.inflate(R.layout.dialog_join, null);
        builder.setView(mLayout);
        Button btn_join_room = (Button) mLayout.findViewById(R.id.btn_join_room);
        final EditText et_room_number = (EditText) mLayout.findViewById(R.id.et_room_number);
        btn_join_room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String meetIDStr = et_room_number.getText().toString();
                int meetID = -1;
                try {
                    meetID = Integer.parseInt(meetIDStr);
                } catch (Exception e) {
                }
                if (meetID < 0) {
                    MyApplication.getInstance().showToast(R.string.err_meetid_prompt);
                    return;
                }
                Intent intent = new Intent();
                intent.setClass(getActivity(), MeetingActivity.class);
                intent.putExtra("meetID", meetID);
                intent.putExtra("password", "");
                startActivity(intent);
            }
        });
        builder.create()
                .show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
