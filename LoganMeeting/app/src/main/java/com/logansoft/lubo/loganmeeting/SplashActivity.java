package com.logansoft.lubo.loganmeeting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends Activity {

    private static final int MSG_CRRENTTIME = 500;
    private static final String TAG = "SplashActivity";
    @BindView(R.id.tvLogo)
    TextView tvLogo;
    @BindView(R.id.tv)
    TextView tv;
    @BindView(R.id.pbLoadingSplash)
    ProgressBar pbLoadingSplash;
    private long startTime;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CRRENTTIME:
                    long obj = (long) msg.obj;
                    pbLoadingSplash.setProgress(((int) (obj - startTime) / 40));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        final Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, AccountLoginActivity.class));
                SplashActivity.this.finish();
            }
        };
        timer.schedule(timerTask, 2000);
        final Date date = new Date();
        startTime = date.getTime();
        new Thread(new Runnable() {
            private long time;

            @Override
            public void run() {
                for (int i = 0; i < 40; i++) {
                    time = date.getTime();
                }
                Message message = handler.obtainMessage();
                message.what = MSG_CRRENTTIME;
                message.obj = time;
                Log.d(TAG, "run: time=" + time);
                handler.sendMessage(message);
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
