package com.shepherdboy.pdstreamline.activities.transaction;

import static com.shepherdboy.pdstreamline.MyApplication.sqLiteDatabase;

import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.dao.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;

import java.util.List;

/**
 * 用于管理Timestream生命周期
 */
public class Streamline {

    /**
     * 将解绑的商品根据商品所处的生命周期{fresh,close to expire(promotion),expired}
     * 存入对应的数据库
     * @param unpackedTimestreams
     */
    public static void reposition(List<Timestream> unpackedTimestreams) {

        for(Timestream t : unpackedTimestreams) {

            position(t);
        }
    }

    /**
     * 根据Timestream所处的生命周期，放入对应的数据库
     * @param t
     */
    public static void position(Timestream t) {
        int stateCode = t.getTimeStreamStateCode();

        switch (stateCode) {

            case Timestream.FRESH:

                PDInfoWrapper.updateInfo(sqLiteDatabase,
                        t, MyDatabaseHelper.FRESH_TIMESTREAM_TABLE_NAME);
                break;

            case Timestream.CLOSE_TO_EXPIRE:

                PDInfoWrapper.updateInfo(sqLiteDatabase,t,
                        MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME);
                break;

            case Timestream.EXPIRED:

                PDInfoWrapper.updateInfo(sqLiteDatabase, t,
                        MyDatabaseHelper.POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME);
                break;

            default:
                break;
        }
    }
}
