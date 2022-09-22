package com.shepherdboy.pdstreamline.beanview;

import static com.shepherdboy.pdstreamline.MyApplication.currentProduct;
import static com.shepherdboy.pdstreamline.MyApplication.sqLiteDatabase;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.dao.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;
import com.shepherdboy.pdstreamline.utils.AIInputter;
import com.shepherdboy.pdstreamline.view.ClosableScrollView;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;
import com.shepherdboy.pdstreamline.view.ActivityInfoChangeWatcher;

import java.util.LinkedHashMap;

/**
 * 统一的加载商品view的类
 */
public class ProductLoader {

    /**
     * 加载新的商品头部信息View
     * @param container 加载的父容器
     * @param index 添加的位置
     * @param product 加载的商品
     */
    public static void loadCellHead(int activityIndex, ViewGroup container, int index, Product product) {

        CellHeadView head = new CellHeadView(activityIndex, container.getContext(), product.getProductCode());
        container.addView(head, index);
    }

    /**
     * 加载商品所有timestream
     * @param activityIndex
     * @param container 父容器
     * @param index 添加的位置,必须是最开始的位置，因为要从此处开始往后计算combView数量
     * @param product 加载的商品
     */
    public static void loadCellBody(int activityIndex, ViewGroup container, int index, Product product) {

        LinkedHashMap<String, Timestream> timestreams = product.getTimeStreams();

        initCellBody(container, timestreams, index, product.getProductCode());

        loadTimestreams(activityIndex, container, timestreams, index);
    }

    /**
     * 从指定位置开始加载timestream
     * @param activityIndex
     * @param container 父容器
     * @param timestreams timestream列表
     * @param index 起始位置
     */
    public static void loadTimestreams(int activityIndex, ViewGroup container, LinkedHashMap<String, Timestream> timestreams, int index) {

        for (Timestream t : timestreams.values()) {

            TimestreamCombinationView combView =
                    (TimestreamCombinationView) container.getChildAt(index);

            combView.bindData(activityIndex, t);
            index++;
        }
    }

    /**
     * 匹配view数量
     * @param container
     * @param timestreams
     * @param index
     * @param productCode
     */
    public static void initCellBody(ViewGroup container, LinkedHashMap<String, Timestream> timestreams, int index, String productCode) {

        int startIndex = index;
        int combCount = 0;
        do {

            if (index > container.getChildCount()) break;
            View child = container.getChildAt(index);

            if (child instanceof TimestreamCombinationView) {

                combCount++;

            }else {

                break;
            }

            index++;
        } while (true);

        while (combCount - 1 > timestreams.size()) {

            container.removeViewAt(startIndex);
            combCount--;
        }

        while (combCount - 1 < timestreams.size()) {

            container.addView(new TimestreamCombinationView(container.getContext()), startIndex);
            combCount++;
        }
    }

    public static View prepareNext(int activityIndex, String productCode, DraggableLinearLayout view) {

        TimestreamCombinationView nextTrigger =
                new TimestreamCombinationView(view.getContext());

        nextTrigger.setProductCode(productCode);

        TextView nameTv = nextTrigger.getBuyProductNameTv();
        EditText inventoryEt = nextTrigger.getInventory();
        nameTv.setText(MyApplication.getAllProducts().get(productCode).getProductName());
        inventoryEt.setFocusable(false);
        inventoryEt.setVisibility(View.INVISIBLE);

        EditText e = nextTrigger.getBuyDOPEt();
        e.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                ActivityInfoChangeWatcher watcher = ActivityInfoChangeWatcher.getActivityWatcher(activityIndex);
                if (hasFocus && watcher.isShouldWatch()) {

                    //将edittext所在view滚动到上方
                    ClosableScrollView.postLocate(ClosableScrollView.SCROLL_FROM_TOUCH, nextTrigger);

                    Product p = MyApplication.getAllProducts().get(productCode);
                    Timestream t = new Timestream();
                    MyApplication.timeStreams.put(t.getId(), t);
                    AIInputter.fillTheBlanks(p, t);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            PDInfoWrapper.updateInfo(sqLiteDatabase, t, MyDatabaseHelper.NEW_TIMESTREAM);
                            currentProduct = PDInfoWrapper.getProduct(productCode, sqLiteDatabase, MyDatabaseHelper.ENTIRE_TIMESTREAM);
                        }
                    }).start();
                    TimestreamCombinationView next =
                            new TimestreamCombinationView(activityIndex, view.getContext(), t);
                    watcher.setShouldWatch(false);
                    view.addView(next,
                            view.indexOfChild(nextTrigger));
                    watcher.setShouldWatch(true);
                    MyApplication.clearOriginalInfo();
                    MyApplication.recordDraggableView();
                    DraggableLinearLayout.setFocus(next.getBuyDOPEt());
                    DraggableLinearLayout.setLayoutChanged(true);
                }
            }
        });

        return nextTrigger;
    }
}
