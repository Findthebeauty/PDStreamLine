package com.shepherdboy.pdstreamline.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.NonNull;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.activities.PossiblePromotionTimestreamActivity;
import com.shepherdboy.pdstreamline.beans.Timestream;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class MidnightTimestreamManagerService extends Service {

    public static Timer midnightTimer;
    public static LinkedList<Timestream> basket = new LinkedList<>();

    public static Timer timestreamRestoreTimer;
    public static TimerTask timestreamRestoreTask;
    public static Handler timestreamRestoreHandler;

    private int inTime = 60 * 60 * 1000;
    private int periodTime = 60 * 60 * 1000;

    public MidnightTimestreamManagerService() {

    }

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, MidnightTimestreamManagerService.class);
        context.startService(intent);
    }

    public static void actionStop(Context context) {
        Intent intent = new Intent(context, MidnightTimestreamManagerService.class);
        context.stopService(intent);
    }

    @Override
    public void onCreate() {

        midnightTimer = new Timer();
        MidNightTask midNightTask = new MidNightTask();
        midnightTimer.schedule(midNightTask, inTime, periodTime);

        timestreamRestoreHandler = new Handler() {

            @Override
            public void handleMessage(@NonNull Message msg) {

                MyApplication.restoreTimestreams(basket);
                timestreamRestoreTimer.cancel();
                super.handleMessage(msg);
            }
        };
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        midnightTimer.cancel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class MidNightTask extends TimerTask {

        @Override
        public void run() {

            MyApplication.initDatabase(getApplicationContext());
            PossiblePromotionTimestreamActivity.pickOutPossibleStaleTimestream();
        }
    }
}