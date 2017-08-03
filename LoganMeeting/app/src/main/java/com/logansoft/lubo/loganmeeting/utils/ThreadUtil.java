package com.logansoft.lubo.loganmeeting.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2016/10/8 0008.
 */
public class ThreadUtil {


    private static ExecutorService executorService;

    public static void execute(Runnable runnable){
        if(executorService==null){
            executorService = Executors.newFixedThreadPool(20);
        }
        executorService.submit(runnable);
    }

}
