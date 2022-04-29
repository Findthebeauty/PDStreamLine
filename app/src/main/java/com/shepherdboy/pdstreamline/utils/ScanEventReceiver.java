package com.shepherdboy.pdstreamline.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.sql.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.activities.PDInfoActivity;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.beans.Timestream;

import java.util.LinkedList;

public class ScanEventReceiver extends BroadcastReceiver {

    public String productCode;
    public String newerProductCode;

    @Override
    public void onReceive(Context context, Intent intent) {

        //todo 判断pdInfoactivity的状态决定是resume还是start
        PDInfoActivity.actionStart(context);
        MyApplication.pickupChanges();
        executeChanges(MyApplication.thingsToSaveList);

        newerProductCode = intent.getStringExtra("barcode_string");

        if (newerProductCode != null && !newerProductCode.equals(productCode)) {


            show(newerProductCode);
            productCode = newerProductCode;

        }

    }

    public static void executeChanges(LinkedList linkedList) {

        Object view;
        while (!linkedList.isEmpty()) {

            view = linkedList.remove();

            if (view instanceof Product) {

                MyDatabaseHelper.PDInfoWrapper.updateInfo(MyApplication.sqLiteDatabase, (Product) view);

            }

            if (view instanceof Timestream) {

                MyDatabaseHelper.PDInfoWrapper.updateInfo(MyApplication.sqLiteDatabase, (Timestream) view);

            }

        }

    }

    public static void show(String productCode) {

        MyApplication.pickupChanges();

        executeChanges(MyApplication.thingsToSaveList);

        if (MyApplication.sqLiteDatabase == null) {

            MyApplication.sqLiteDatabase = MyApplication.databaseHelper.getWritableDatabase();
        }

        // 查询信息
        MyApplication.currentProduct = MyDatabaseHelper.PDInfoWrapper.getProduct(productCode, MyApplication.sqLiteDatabase, MyDatabaseHelper.ENTIRE_TIMESTREAM);
        PDInfoActivity.loadProduct(MyApplication.currentProduct);

    }
}