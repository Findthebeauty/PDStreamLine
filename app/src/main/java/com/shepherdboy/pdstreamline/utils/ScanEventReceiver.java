package com.shepherdboy.pdstreamline.utils;

import static com.shepherdboy.pdstreamline.MyApplication.currentProduct;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.activities.PDInfoActivity;
import com.shepherdboy.pdstreamline.dao.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;

public class ScanEventReceiver extends BroadcastReceiver {

    public String productCode;
    public String newerProductCode;

    @Override
    public void onReceive(Context context, Intent intent) {

        newerProductCode = intent.getStringExtra("barcode_string");

        if (!AIInputter.isNumeric(newerProductCode)) return;

        MyApplication.saveChanges(currentProduct);

        if (newerProductCode != null && !newerProductCode.equals(productCode)) {

            show(newerProductCode);
            productCode = newerProductCode;
        }
    }

    public static void show(String productCode) {

        if (currentProduct != null)
        MyApplication.serialize(currentProduct);

        if (MyApplication.sqLiteDatabase == null) {

            MyApplication.sqLiteDatabase = MyApplication.databaseHelper.getWritableDatabase();
        }

        currentProduct = PDInfoWrapper.getProduct(productCode, MyApplication.sqLiteDatabase, MyDatabaseHelper.ENTIRE_TIMESTREAM);
        PDInfoActivity.postLoadProduct(currentProduct);

    }
}