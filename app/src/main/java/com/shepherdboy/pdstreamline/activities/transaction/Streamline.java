package com.shepherdboy.pdstreamline.activities.transaction;

import static com.shepherdboy.pdstreamline.MyApplication.POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.activityIndex;
import static com.shepherdboy.pdstreamline.MyApplication.onShowTimeStreamsHashMap;
import static com.shepherdboy.pdstreamline.MyApplication.sqLiteDatabase;
import static com.shepherdboy.pdstreamline.services.MidnightTimestreamManagerService.basket;

import android.view.View;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.activities.PromotionActivity;
import com.shepherdboy.pdstreamline.beans.ProductLoss;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.beans.TimestreamCombination;
import com.shepherdboy.pdstreamline.beanview.TimestreamCombinationView;
import com.shepherdboy.pdstreamline.dao.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用于管理Timestream生命周期
 */
public class Streamline {

    public static final Set<Timestream> giveawayTimestreams = new HashSet<>();

    public static final Set<Timestream> offShelvesTimestreams = new HashSet<>();

    public static void pickOut(View releasedChild, DraggableLinearLayout draggableLinearLayout) {

        if(releasedChild instanceof TimestreamCombinationView) {
            TimestreamCombination comb = ((TimestreamCombinationView) releasedChild)
                    .getTimestreamCombination();
            if(comb == null) {

                Timestream ts = onShowTimeStreamsHashMap.get(releasedChild.getId());

                if(ts == null) {

                    draggableLinearLayout.putBack(releasedChild);
                    return;
                }

                if(ts.getTimeStreamStateCode() == Timestream.EXPIRED
                        || activityIndex == POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY)
                    releasedChild.setVisibility(View.GONE);

                Streamline.pickOutByStateCode(ts);

                MyApplication.productSubject.notify(ts.getProductCode());

            } else {

                Timestream[] timestreams = PromotionActivity.unCombine(comb);

                if((timestreams[0].getTimeStreamStateCode() == Timestream.EXPIRED
                        && timestreams[1].getTimeStreamStateCode() == Timestream.EXPIRED)
                        || activityIndex == POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY) {
                    releasedChild.setVisibility(View.GONE);
                } else {
                    MyApplication.draggableLinearLayout.putBack(releasedChild);
                }

                for (Timestream t : timestreams) Streamline.pickOutByStateCode(t);

                MyApplication.productSubject.notify(timestreams[0].getProductCode());
                MyApplication.productSubject.notify(timestreams[1].getProductCode());
            }
        } else {

            draggableLinearLayout.putBack(releasedChild);
        }

    }

    public static void pickOutByStateCode(Timestream ts) {

        switch (ts.getTimeStreamStateCode()) {

            case Timestream.EXPIRED:

                ProductLoss productLoss = new ProductLoss(ts);
                MyApplication.deleteTimestream(ts);
                PDInfoWrapper.updateInfo(sqLiteDatabase, productLoss);

                break;

            case Timestream.CLOSE_TO_EXPIRE:

                basket.put(ts.getId(), ts);
                ts.setInBasket(true);
                update(ts);
                break;

            default:
                update(ts);
                break;
        }
    }

    /**
     * 将解绑的商品根据商品所处的生命周期{fresh,close to expire(promotion),expired}
     * 存入对应的数据库
     * @param unpackedTimestreams
     */
    public static void reposition(List<Timestream> unpackedTimestreams) {

        for(Timestream t : unpackedTimestreams) {


            update(t);
        }
    }

    /**
     * 根据Timestream所处的生命周期，放入对应的数据库
     * @param t
     */
    public static void update(Timestream t) {

        MyApplication.getAllProducts().get(t.getProductCode()).getTimeStreams().put(t.getId(), t); // product缓存同步

        if(t.getSiblingPromotionId() == null) {

            int state = t.getTimeStreamStateCode();

            switch (state) {

                case Timestream.FRESH:

                    PDInfoWrapper.updateInfo(sqLiteDatabase, t, MyDatabaseHelper.FRESH_TIMESTREAM_TABLE_NAME);
                    break;

                case Timestream.CLOSE_TO_EXPIRE:

                    PDInfoWrapper.updateInfo(sqLiteDatabase, t, MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME);
                    PDInfoWrapper.updateInfo(sqLiteDatabase, t, MyDatabaseHelper.UPDATE_BASKET);
                    break;

                case Timestream.EXPIRED:

                    PDInfoWrapper.updateInfo(sqLiteDatabase, t, MyDatabaseHelper.POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME);
                    PDInfoWrapper.updateInfo(sqLiteDatabase, t, MyDatabaseHelper.UPDATE_BASKET);

                    break;
            }

        } else {

            PDInfoWrapper.updateInfo(sqLiteDatabase, t, MyDatabaseHelper.PROMOTION_TIMESTREAM_TABLE_NAME);
        }
    }

}
