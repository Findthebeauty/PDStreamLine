package com.shepherdboy.pdstreamline.activities;

import android.app.Activity;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.services.MidnightTimestreamManagerService;
import com.shepherdboy.pdstreamline.sql.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.utils.ErrorInfoDisplayActivity;
import com.shepherdboy.pdstreamline.utils.ScanEventReceiver;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    public static Handler mainHandler;

    //将未处理一场打印到界面上，全局
    final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

    //广播接收过滤器，全局
    static IntentFilter intentFilter;

    //扫描广播接收器，全局
    static ScanEventReceiver scanEventReceiver;

    //sqlite本地数据库地址，全局
    static String databasePath;

    //临时数据库缓存地址，用于本地数据库与服务器数据库的替换，全局
    static String tempDataBasePath;

    //数据库名，全局
    private static String dataBaseName = "ProductDateStreamline.db";

    //请求本地文件读写权限参数,退出时
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    //请求本地文件读写权限参数,退出时
    private static String[] PERMISSION_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainHandler = new Handler(Looper.getMainLooper());

        databasePath = this.getFilesDir().getPath().replaceAll("files", "databases/ProductDateStreamline.db");

        //未处异常，全局
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(@NonNull Thread thread, @NonNull Throwable exception) {

                long timeMillis = System.currentTimeMillis();

                StringBuilder stringBuilder = new StringBuilder(new SimpleDateFormat(
                   "yy/MM/dd HH:mm:ss").format(timeMillis));
                stringBuilder.append(":\n");

                stringBuilder.append(exception.getMessage());
                stringBuilder.append(":\n");

                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                exception.printStackTrace(printWriter);
                stringBuilder.append(stringWriter.toString());

                String errorLog = stringBuilder.toString();



                ErrorInfoDisplayActivity.actionStart(MainActivity.this, errorLog);

                defaultHandler.uncaughtException(thread, exception);

            }
        });


        //开始信息录入模式
        Button startPDInfoActivityButton = findViewById(R.id.pd_info_activity);
        Button startPossiblePromotionActivityButton = findViewById(R.id.possible_promotion_activity);
        Button stopMNSBT = findViewById(R.id.stop_midnight_manager_service);

        startPDInfoActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PDInfoActivity.actionStart(MainActivity.this);

            }
        });

        startPossiblePromotionActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PossiblePromotionTimestreamActivity.actionStart(MainActivity.this);
            }
        });

        stopMNSBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MidnightTimestreamManagerService.actionStop(MainActivity.this);
            }
        });

        //本地数据库应用外部保存位置，退出时
        tempDataBasePath = Environment.getExternalStorageDirectory() + "/Android/v2";

        //获取本地文件读写权限并获取应用外部数据库
        verifyStoragePermissions(this);
        MyDatabaseHelper.copyDataBase(tempDataBasePath, databasePath);

        //获取数据库连接助手
        MyApplication.databaseHelper = new MyDatabaseHelper(this, dataBaseName,
                null, 1);

        // 接收扫码枪扫描动作广播的条码号,全局
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.ACTION_DECODE_DATA");
        scanEventReceiver = new ScanEventReceiver();
        registerReceiver(scanEventReceiver, intentFilter);
    }

    /**
     *   获取运行时本地文件读写权限
     */
    public static void verifyStoragePermissions(Activity activity) {

        try {

            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity, "android.permission." +
                    "WRITE_EXTERNAL_STORAGE");

            if (permission != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(activity, PERMISSION_STORAGE,
                        REQUEST_EXTERNAL_STORAGE);

            }
        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    @Override
    protected void onStart() {

        MidnightTimestreamManagerService.actionStart(MainActivity.this);
        super.onStart();
    }

    @Override
    protected void onDestroy() {

        //注销广播接收器，退出时
        unregisterReceiver(scanEventReceiver);

        //将应用内部数据库拷贝到应用外部文件夹
        verifyStoragePermissions(this);
        MyDatabaseHelper.copyDataBase(databasePath,
                tempDataBasePath);
        super.onDestroy();
    }
}