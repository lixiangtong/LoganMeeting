package com.logansoft.lubo.loganmeeting.adapters;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by logansoft on 2017/8/18.
 */

public class NavigationAdapter extends PagerAdapter {

    private ArrayList<View> mViewList;

    public NavigationAdapter(ArrayList<View> mViewList) {
        this.mViewList = mViewList;
    }

    @Override
    public int getCount() {
        return mViewList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        ((ViewPager) container).removeView(mViewList.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        ((ViewPager) container).addView(mViewList.get(position));
        return mViewList.get(position);
    }
}
