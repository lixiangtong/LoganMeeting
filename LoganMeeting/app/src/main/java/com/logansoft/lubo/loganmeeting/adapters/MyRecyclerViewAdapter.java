package com.logansoft.lubo.loganmeeting.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.logansoft.lubo.loganmeeting.R;

import java.util.List;

/**
 * Created by logansoft on 2017/7/12.
 */

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {

    private List<String> mDatas;
    private Context mContext;
    private LayoutInflater inflater;
    private OnItemClickListener mOnItemClickListener;

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public interface OnItemClickListener{
        void onClick(int position);
        void onLongClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener ){
        this.mOnItemClickListener=onItemClickListener;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvModerator;
        private final TextView tvRoomNumber;
        private final TextView tvOnlineCount;
        private final TextView tvRoomName;

        public MyViewHolder(View view) {
            super(view);
            tvRoomName=(TextView) view.findViewById(R.id.tv_room_name);
            tvModerator = ((TextView) view.findViewById(R.id.tv_moderator));
            tvRoomNumber = ((TextView) view.findViewById(R.id.tv_room_number));
            tvOnlineCount = ((TextView) view.findViewById(R.id.tv_online_count));
        }

    }
}
