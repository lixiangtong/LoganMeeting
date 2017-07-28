package com.logansoft.lubo.loganmeeting;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.os.Handler.Callback;


import com.cloudroom.cloudroomvideosdk.CloudroomVideoMgr;
import com.cloudroom.cloudroomvideosdk.CloudroomVideoSDK;
import com.logansoft.lubo.loganmeeting.adapters.BarFragmentAdapter;
import com.logansoft.lubo.loganmeeting.fragments.HomeFragment;
import com.logansoft.lubo.loganmeeting.fragments.SettingsFragment;
import com.logansoft.lubo.loganmeeting.utils.VideoSDKHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;




public class MainActivity extends FragmentActivity {

    private static final String TAG = "MainActivity";

    List<Fragment> fragments = new ArrayList<>();
    @BindView(R.id.top_item)
    TextView topItem;
    @BindView(R.id.vp)
    ViewPager vp;
    @BindView(R.id.rb_home)
    RadioButton rbHome;
    @BindView(R.id.rb_settings)
    RadioButton rbSettings;
    @BindView(R.id.rg)
    RadioGroup rg;
    private FragmentTransaction ft;
    private HomeFragment homeFragment;
    private SettingsFragment settingsFragment;
    private BarFragmentAdapter fragmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        Log.d(TAG, "onCreate: "+1);

        homeFragment = new HomeFragment();
        settingsFragment = new SettingsFragment();
        fragments.add(homeFragment);
        fragments.add(settingsFragment);
        fragmentAdapter = new BarFragmentAdapter(getSupportFragmentManager(),fragments);
        vp.setAdapter(fragmentAdapter);
        vp.setCurrentItem(0);

        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position==0){
                    ((RadioButton) rg.getChildAt(0)).setChecked(true);
                    topItem.setText("首页");
                }else {
                    ((RadioButton) rg.getChildAt(2)).setChecked(true);
                    topItem.setText("设置");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId){
                    case R.id.rb_home:
                        vp.setCurrentItem(0);
                        topItem.setText("首页");
                        break;
                    case R.id.rb_settings:
                        vp.setCurrentItem(1);
                        topItem.setText("设置");
                        break;
                }
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: "+1);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: "+1);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: "+1);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: "+1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: "+1);
        CloudroomVideoMgr.getInstance().logout();
//        CloudroomVideoSDK.getInstance().uninit();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: "+1);

    }

    //    public void showFragment(Fragment fragment) {
//        ft = getSupportFragmentManager().beginTransaction();
//        if (!fragment.isAdded()) {
//            ft.add(R.id.fl_content, fragment);
//        }
//
//        for (Fragment fragment1 : fragments) {
//            if (fragment1 == fragment) {
//                ft.show(fragment1);
//            } else {
//                ft.hide(fragment1);
//            }
//        }
//        ft.commit();
//    }
}
