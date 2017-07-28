package com.logansoft.lubo.loganmeeting.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudroom.cloudroomvideosdk.model.MeetInfo;
import com.logansoft.lubo.loganmeeting.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by logansoft on 2017/7/28.
 */

public class MeetingInfoAdapter extends BaseAdapter {

    private List<MeetInfo> data;
    private Context context;
    private LayoutInflater inflater;
    private MyRecyclerViewAdapter.OnMyItemClickListener mOnItemClickListener;


    public MeetingInfoAdapter(List<MeetInfo> data, Context context) {
        this.data = data;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_room_info, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder =(ViewHolder) convertView.getTag();
        }

        MeetInfo meetInfo = data.get(position);
        holder.tvRoomName.setText(meetInfo.subject);
        holder.tvRoomNumber.setText(meetInfo.ID);

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
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.tv_room_name)
        TextView tvRoomName;
        @BindView(R.id.tv_moderator)
        TextView tvModerator;
        @BindView(R.id.tv_room_number)
        TextView tvRoomNumber;
        @BindView(R.id.tv_online_count)
        TextView tvOnlineCount;
        @BindView(R.id.llRvItem)
        LinearLayout rvItem;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public interface OnMyItemClickListener{
        void onClick(int position);
        void onLongClick(int position);
    }

    public void setMyOnItemClickListener(MyRecyclerViewAdapter.OnMyItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }
}
