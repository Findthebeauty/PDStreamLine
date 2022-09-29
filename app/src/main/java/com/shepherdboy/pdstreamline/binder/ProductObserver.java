package com.shepherdboy.pdstreamline.binder;

import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.dao.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;

import java.util.HashMap;

/**
 * 接收商品信息更新message，在对应布局中重新加载对应的商品
 */
public class ProductObserver implements Observer{

    private final int activityIndex;

    private final Handler handler;

    private final HashMap<String, LinearLayout> timestreamViewMap;

    public ProductObserver(int activityIndex, Handler handler) {
        this.activityIndex = activityIndex;
        this.handler = handler;
        this.timestreamViewMap = new HashMap<>();
    }

    public void bind(String timestreamId, LinearLayout view) {
        timestreamViewMap.put(timestreamId, view);
    }

    public HashMap<String, LinearLayout> getTimestreamViewMap() {
        return timestreamViewMap;
    }

    public int getActivityIndex() {
        return activityIndex;
    }

    @Override
    public void update(String message) {

        Product product = PDInfoWrapper.getProduct(message, MyApplication.sqLiteDatabase,
                MyDatabaseHelper.ENTIRE_TIMESTREAM);

        Message msg = handler.obtainMessage();
        msg.what = ProductSubject.SYNC_PRODUCT;
        msg.obj = product;
        handler.sendMessage(msg);
    }
}
