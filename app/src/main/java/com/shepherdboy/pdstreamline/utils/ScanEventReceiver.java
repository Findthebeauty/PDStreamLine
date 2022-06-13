package com.shepherdboy.pdstreamline.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.sql.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.activities.PDInfoActivity;

public class ScanEventReceiver extends BroadcastReceiver {

    public String productCode;
    public String newerProductCode;

    @Override
    public void onReceive(Context context, Intent intent) {

        newerProductCode = intent.getStringExtra("barcode_string");

        if (!AIInputter.isNumeric(newerProductCode)) return;

        //todo 判断pdInfoactivity的状态决定是resume还是start
        PDInfoActivity.actionStart();
        MyApplication.pickupChanges();
        MyApplication.saveChanges(MyApplication.thingsToSaveList);



        if (newerProductCode != null && !newerProductCode.equals(productCode)) {

            show(newerProductCode);
            productCode = newerProductCode;
        }
    }

    public static void show(String productCode) {

        MyApplication.pickupChanges();

        MyApplication.saveChanges(MyApplication.thingsToSaveList);

        if (MyApplication.sqLiteDatabase == null) {

            MyApplication.sqLiteDatabase = MyApplication.databaseHelper.getWritableDatabase();
        }

        // 查询信息
        MyApplication.currentProduct = MyDatabaseHelper.PDInfoWrapper.getProduct(productCode, MyApplication.sqLiteDatabase, MyDatabaseHelper.ENTIRE_TIMESTREAM);
        PDInfoActivity.loadProduct(MyApplication.currentProduct);

    }
}