package com.shepherdboy.pdstreamline.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.activities.PossiblePromotionTimestreamActivity;

import java.util.Timer;
import java.util.TimerTask;

public class MidnightTimestreamManagerService extends Service {

    public static Timer midnightTimer;

    private int inTime = 2 * 1000;
    private int periodTime = 2 * 1000;

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
            PossiblePromotionTimestreamActivity.pickOutPossiblePromotionTimestream();
        }
    }
}