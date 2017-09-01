package com.logansoft.lubo.loganmeeting.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by jun on 2016/10/28.
 */
public class HttpUtil {
    static Context mContext;

    private static final String TAG = "test";
    private static int pege;
    private static int page;


    public static Cache provideCache() {
        return new Cache(mContext.getCacheDir(), 10240 * 1024);
    }

    public static class CacheInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);
            Response response1 = response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    //cache for 30 days
                    .header("Cache-Control", "max-age=" + 3600 * 24 * 30)
                    .build();
            return response1;
        }
    }


    /**
     * 使用OkHttp获得String类型的网络数据--异步
     *
     * @param url     网址
     * @param handler 发送消息所使用的Handler
     */
    public static void getStringByOkHttp(Context context, String url, final Handler handler, final int what) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        Response response = chain.proceed(request).newBuilder()
                                .removeHeader("Pragma")
                                .removeHeader("Cache-Control")
                                .header("Cache-Control", "max-age=" + 3600 * 24 * 7)//缓存保留七天
                                .build();
                        return response;
                    }
                })
                .cache(new Cache(context.getExternalCacheDir(), 10240 * 1024))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(Constant.MSG_WHAT_NO_RESPONSE);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Message msg = handler.obtainMessage();
                    msg.what = what;
                    msg.obj = response.body().string();
                    msg.sendToTarget();
                } else {
                    handler.sendEmptyMessage(Constant.MSG_WHAT_NO_RESPONSE);
                }
            }
        });
    }

    /**
     * 使用OkHttp获得字符流--同步
     */
    public static String getStrByOkhttp(String json) {
        OkHttpClient okHttpClient = new OkHttpClient();

        OkHttpClient newClient = okHttpClient.newBuilder()
                .addNetworkInterceptor(new CacheInterceptor())
                .cache(provideCache())
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(json)
                .tag("tag")
                .build();

        try {
            Response response = newClient.newCall(request).execute();
            if (response.isSuccessful()) {
                json = response.body().string();
            } else {
                json = "response is fail";
            }

        } catch (IOException e) {
            e.printStackTrace();

        }

        return json;
    }


    /**
     * 使用OkHttp获得字节流--异步
     *
     * @param url     网址
     * @param handler 发送消息所使用的Handler
     */
    public static void getByteStreamByOkHttp(String url, final Handler handler, final int what) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(Constant.MSG_WHAT_NO_RESPONSE);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Message msg = handler.obtainMessage();
                    msg.what = what;
                    msg.obj = response.body().byteStream();
                    msg.sendToTarget();
                } else {
                    handler.sendEmptyMessage(Constant.MSG_WHAT_NO_RESPONSE);
                }
            }
        });
    }

    /**
     * 使用OkHttp获得字符流--异步
     *
     * @param url     网址
     * @param handler 处理消息所使用的Handler
     */
    public static void getCharStreamByOkHttp(String url, final Handler handler, final int what) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        final Request request = new Request.Builder()
                .url(url)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(Constant.MSG_WHAT_NO_RESPONSE);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Message msg = handler.obtainMessage();
                    msg.what = what;
                    msg.obj = response.body().charStream();
                    msg.sendToTarget();
                } else {
                    handler.sendEmptyMessage(Constant.MSG_WHAT_NO_RESPONSE);
                }
            }
        });
    }

    /**
     * 使用OkHttp获字节数组--异步
     *
     * @param url     网址
     * @param handler 处理消息所使用的Handler
     * @param what    msg.what
     */
    public static void getBytesByOkHttp(String url, final Handler handler, final int what) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        final Request request = new Request.Builder()
                .url(url)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(Constant.MSG_WHAT_NO_RESPONSE);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Message msg = handler.obtainMessage();
                    msg.what = what;
                    msg.obj = response.body().charStream();
                    msg.sendToTarget();
                } else {
                    handler.sendEmptyMessage(Constant.MSG_WHAT_NO_RESPONSE);
                }
            }
        });
    }

//    http://120.55.198.252/home/m?target=android&v=310&act=home%2Fdata_v2_6&json=%7B%22session%22%3A%7B%22uid%22%3A3979552%2C%22sid%22%3A%22mfbwww%22%7D%2C%22pagination%22%3A%7B%22page%22%3A1%7D%7D

    public static void getHomePageJson(Context context, int page, Handler handler, int what) {
        String url = "http://120.55.198.252/home/m?target=android&v=310&act=home%2Fdata_v2_6&json=%7B%22session%22%3A%7B%22uid%22%3A3979552%2C%22sid%22%3A%22mfbwww%22%7D%2C%22pagination%22%3A%7B%22page%22%3A" + page + "%7D%7D";
        Log.e(TAG, url);
        getStringByOkHttp(context, url, handler, what);
    }

    public static String getAnnounced(int page, Context context) {
        mContext = context;
        String json = getStrByOkhttp("http://120.55.198.252/home/m?target=android&v=310&act=yungou%2Flist&json=%7B%22pagination%22%3A%7B%22page%22%3A" + page + "%7D%2C%22status%22%3A%22END%22%2C%22update_time%22%3A1477905768%7D");
        return json;
    }

    public static String getIndiana(int page, Context context) {
        mContext = context;
        String json = getStrByOkhttp("http://120.55.198.252/home/m?target=android&v=310&act=yungou%2Flist&json=%7B%22pagination%22%3A%7B%22page%22%3A" + page + "%7D%2C%22status%22%3A%22ING%22%2C%22update_time%22%3A1477986778%7D");
        return json;
    }


    public static String getIndianaDetail(int id, Context context) {
        mContext = context;
        String json = getStrByOkhttp("http://120.55.198.252/home/m?target=android&v=310&act=yungou%2Fdetail&json=%7B%22pagination%22%3A%7B%22page%22%3A1%7D%2C%22session%22%3A%7B%22uid%22%3A3979552%2C%22sid%22%3A%22mfbwww%22%7D%2C%22yungou_id%22%3A"+id+"%7D");
        return json;
    }


    public static String getFind(Context context) {
        mContext = context;
        String json = getStrByOkhttp("http://120.55.198.252/home/m?target=android&v=310&act=discovery_menu&json=%7B%22session%22%3A%7B%22uid%22%3A3979552%2C%22sid%22%3A%22mfbwww%22%7D%7D");
        return json;
    }

    public static void getHomeVpJson(Context context, String url, Handler handler, int what){
        getStringByOkHttp(context, url, handler, what);
    }

//    http://120.55.198.252/home/m?target=android&v=310&act=home%2Fdata_v2_6&json=%7B%22session%22%3A%7B%22uid%22%3A3983342%2C%22sid%22%3A%22oogavw%22%7D%2C%22search%22%3A%7B%22cid%22%3A%221%22%7D%2C%22pagination%22%3A%7B%22page%22%3A1%7D%7D
//    http://120.55.198.252/home/m?target=android&v=310&act=home%2Fdata_v2_6&json=%7B%22session%22%3A%7B%22uid%22%3A3983342%2C%22sid%22%3A%22oogavw%22%7D%2C%22search%22%3A%7B%22cid%22%3A%226%22%7D%2C%22pagination%22%3A%7B%22page%22%3A1%7D%7D
//    http://120.55.198.252/home/m?target=android&v=310&act=home%2Fdata_v2_6&json=%7B%22session%22%3A%7B%22uid%22%3A3983342%2C%22sid%22%3A%22oogavw%22%7D%2C%22search%22%3A%7B%22keyword%22%3A%22%E7%94%B7%E8%A3%85%22%7D%2C%22pagination%22%3A%7B%22page%22%3A1%7D%7D


//    http://www.wowozhe.com/Home/m/app_yungou/id/3045429

    public static String getContentDetail(String id) {
        String json = getStrByOkhttp("http://www.wowozhe.com/Home/m/app_yungou/id/"+id);
        return json;
    }

}
