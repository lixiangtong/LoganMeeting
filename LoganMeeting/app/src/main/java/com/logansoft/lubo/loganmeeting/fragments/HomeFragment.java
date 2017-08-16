package com.logansoft.lubo.loganmeeting.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cloudroom.cloudroomvideosdk.CloudroomVideoMgr;
import com.cloudroom.cloudroomvideosdk.model.CRVIDEOSDK_ERR_DEF;
import com.cloudroom.cloudroomvideosdk.model.MeetInfo;
import com.logansoft.lubo.loganmeeting.MeetingActivity;
import com.logansoft.lubo.loganmeeting.MgrCallback;
import com.logansoft.lubo.loganmeeting.MyApplication;
import com.logansoft.lubo.loganmeeting.R;
import com.logansoft.lubo.loganmeeting.VideoCallback;
import com.logansoft.lubo.loganmeeting.adapters.MyRecyclerViewAdapter;
import com.logansoft.lubo.loganmeeting.adapters.MyRecyclerViewAdapterI;
import com.logansoft.lubo.loganmeeting.beans.RoomInfoBean;
import com.logansoft.lubo.loganmeeting.utils.DividerItemDecoration;
import com.logansoft.lubo.loganmeeting.utils.VideoSDKHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by logansoft on 2017/7/6.
 */

public class HomeFragment extends Fragment {
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
    @BindView(R.id.cl)
    CoordinatorLayout cl;
    @BindView(R.id.nsv)
    NestedScrollView nsv;
    @BindView(R.id.lv)
    ListView lv;
//    @BindView(R.id.srl_home_root)
//    SwipeRefreshLayout srlHomeRoot;
    private View view;
    private RoomInfoBean roomInfoBean1;
    private RoomInfoBean roomInfoBean2;
    private List<RoomInfoBean> data = new ArrayList<>();
    private MyRecyclerViewAdapterI myRecyclerViewAdapterI;

    private AlertDialog alertDialog;
    private MyRecyclerViewAdapter myRecyclerViewAdapter;
    private ArrayList<MeetInfo> mData = new ArrayList<>();

    private Callback mMgrCallback = new Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case MgrCallback.MSG_LINEOFF:
                    getActivity().finish();
                    break;
                case VideoCallback.MSG_ENTERMEETING_RSLT:
//                    enableOption(true);
                    break;
                case MgrCallback.MSG_GETMEETING_SUCCESS:
                    ArrayList<MeetInfo> meetInfos = (ArrayList<MeetInfo>) msg.obj;
                    for (MeetInfo meetInfo : meetInfos) {
                        Log.d(TAG, "handleMessage: meetInfo=" + meetInfo.ID);
                    }
                    if (mData.size()!=0){
                        mData.clear();
                    }
                    mData.addAll(meetInfos);
                    myRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case MgrCallback.MSG_GETMEETING_FAILED:
                    CRVIDEOSDK_ERR_DEF sdkError = (CRVIDEOSDK_ERR_DEF) msg.obj;
                    MyApplication.getInstance().showToast("获取会议列表失败", sdkError);
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    public Handler mMainHandler = new Handler(mMgrCallback);
    private RoomInfoBean roomInfoBean3;
    private RoomInfoBean roomInfoBean4;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置呼叫处理对象
        MgrCallback.getInstance().registerMgrCallback(mMgrCallback);

        String userID = VideoSDKHelper.getInstance().getLoginUserID();
        Log.d(TAG, "onCreate: userID" + userID);
        if (TextUtils.isEmpty(userID)) {
            getActivity().finish();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.home_fragment, container, false);
            unbinder = ButterKnife.bind(this, view);

            CloudroomVideoMgr.getInstance().getMeetings();
            nsv.smoothScrollTo(0, 0);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            //设置布局管理器
            rv.setLayoutManager(layoutManager);
            //设置为垂直布局，这也是默认的
            layoutManager.setOrientation(OrientationHelper.VERTICAL);
            //设置分隔线
            rv.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
            //设置增加或删除条目的动画
            rv.setItemAnimator(new DefaultItemAnimator());
            //解决RecyclerView和NestedScrollViewd的滑动冲突
            rv.setNestedScrollingEnabled(false);

            myRecyclerViewAdapter = new MyRecyclerViewAdapter(mData, getActivity());
            rv.setAdapter(myRecyclerViewAdapter);
        }
//        roomInfoBean1 = new RoomInfoBean();
//        roomInfoBean2 = new RoomInfoBean();
//        roomInfoBean3 = new RoomInfoBean();
//        roomInfoBean4 = new RoomInfoBean();
//
//
//        roomInfoBean1.setRoomName("语文教研室");
//        roomInfoBean1.setModerator("张泽军");
//        roomInfoBean1.setRoomNumber("74040371");
//        roomInfoBean1.setWaitCount("2");
//
//        roomInfoBean2.setRoomName("人文教研室");
//        roomInfoBean2.setModerator("张泽军");
//        roomInfoBean2.setRoomNumber("36826479");
//        roomInfoBean2.setWaitCount("2");
//
//        roomInfoBean3.setRoomName("生物教研室");
//        roomInfoBean3.setModerator("李利群");
//        roomInfoBean3.setRoomNumber("28166679");
//        roomInfoBean3.setWaitCount("5");
//
//        roomInfoBean4.setRoomName("数学教研室");
//        roomInfoBean4.setModerator("李林丽");
//        roomInfoBean4.setRoomNumber("36826479");
//        roomInfoBean4.setWaitCount("1");
//
//        data.add(roomInfoBean1);
//        data.add(roomInfoBean2);
//        data.add(roomInfoBean3);
//        data.add(roomInfoBean4);
//
//
//
//        myRecyclerViewAdapterI = new MyRecyclerViewAdapterI(data, getActivity());
//        Log.d(TAG, "onCreateView: " + data.size());
//        rv.setAdapter(myRecyclerViewAdapterI);

        setViewListener(myRecyclerViewAdapter,mData);

        return view;
    }

    private void setViewListener(MyRecyclerViewAdapter myRecyclerViewAdapter, final ArrayList<MeetInfo> mData) {
        //MyRecyclerViewAdapter的item点击事件
        myRecyclerViewAdapter.setMyOnItemClickListener(new MyRecyclerViewAdapter.OnMyItemClickListener() {
            @Override
            public void onClick(int position) {
                int meetID = mData.get(position).ID;
//                String roomNumber = data.get(position).getRoomNumber();
//                int meetID = Integer.parseInt(roomNumber);
                Intent intent = new Intent();
                intent.setClass(getActivity(), MeetingActivity.class);
//                intent.setClass(getActivity(), MeetingPerfoActivity.class);
                intent.putExtra("meetID", meetID);
                intent.putExtra("password", "");
                intent.putExtra("isLogout",false);
                startActivity(intent);
            }

            @Override
            public void onLongClick(int position) {
//                MyApplication.getInstance().showToast("你长击了第" + position + "个Item");

            }
        });

//        nsv.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
//            @Override
//            public void onScrollChanged() {
//                srlHomeRoot.setEnabled(nsv.getScrollY()==0);
//            }
//        });
//
//        srlHomeRoot.setColorSchemeResources(R.color.colorAccent,R.color.colorPrimaryDark);
//        srlHomeRoot.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                srlHomeRoot.setRefreshing(true);
//                MyApplication.getInstance().showToast("正在刷新");
//                CloudroomVideoMgr.getInstance().getMeetings();
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        srlHomeRoot.setRefreshing(false);
//                    }
//                },3000);
//            }
//        });

    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次进入界面刷新队列状态
//        CloudroomQueue.getInstance().refreshAllQueueStatus();
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
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MgrCallback.getInstance().unregisterMgrCallback(mMgrCallback);
    }
}