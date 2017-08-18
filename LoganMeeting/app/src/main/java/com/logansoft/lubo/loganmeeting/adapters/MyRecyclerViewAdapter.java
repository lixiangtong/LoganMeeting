package com.logansoft.lubo.loganmeeting.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudroom.cloudroomvideosdk.model.MeetInfo;
import com.logansoft.lubo.loganmeeting.R;
import com.logansoft.lubo.loganmeeting.beans.RoomInfoBean;

import java.util.List;

/**
 * Created by logansoft on 2017/7/12.
 */

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {

    private static final String TAG = "MyRecyclerViewAdapter";
    private List<MeetInfo> mDatas;
    private Context mContext;
    private LayoutInflater inflater;
    private OnMyItemClickListener mOnItemClickListener;

    public MyRecyclerViewAdapter() {
    }

    public MyRecyclerViewAdapter(List<MeetInfo> mDatas, Context mContext) {
        this.mDatas = mDatas;
        this.mContext = mContext;
        this.inflater = LayoutInflater.from(mContext);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_room_info,parent, false);
        MyViewHolder holder= new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        MeetInfo meetInfo = mDatas.get(position);
        holder.tvRoomName.setText(meetInfo.subject);
        holder.tvRoomNumber.setText(meetInfo.ID+"");

        if (mOnItemClickListener!=null){
            holder.rvItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onClick(position);
                }
            });
            holder.rvItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onLongClick(position);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: mDatas.size()="+mDatas.size());
        if (mDatas!=null) {
            return mDatas.size();
        }
        return 0;
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvModerator;
        private final TextView tvRoomNumber;
        private final TextView tvOnlineCount;
        private final TextView tvRoomName;
        private final LinearLayout rvItem;

        public MyViewHolder(View view) {
            super(view);
            tvRoomName=(TextView) view.findViewById(R.id.tv_room_name);
            tvModerator = ((TextView) view.findViewById(R.id.tv_moderator));
            tvRoomNumber = ((TextView) view.findViewById(R.id.tv_room_number));
            tvOnlineCount = ((TextView) view.findViewById(R.id.tv_online_count));
            rvItem = ((LinearLayout) view.findViewById(R.id.llRvItem));
        }

    }

    public interface OnMyItemClickListener{
        void onClick(int position);
        void onLongClick(int position);
    }

    public void setMyOnItemClickListener(OnMyItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }
}
