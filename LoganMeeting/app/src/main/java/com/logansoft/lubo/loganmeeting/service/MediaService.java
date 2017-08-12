package com.logansoft.lubo.loganmeeting.service;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by logansoft on 2017/8/12.
 */

public class MediaService extends Service{

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder{

        public MediaService getService() {
            return MediaService.this;
        }
    }
}
