package com.shepherdboy.pdstreamline;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.shepherdboy.pdstreamline.activities.PDInfoActivity;
import com.shepherdboy.pdstreamline.activities.PossiblePromotionTimestreamActivity;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.sql.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.utils.AIInputter;
import com.shepherdboy.pdstreamline.utils.DateUtil;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;
import com.shepherdboy.pdstreamline.view.MyTextWatcher;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class MyApplication extends Application {

    //设置时区
//    public static final TimeZone timeZone = TimeZone.getTimeZone("Asia/Chongqing");

    public static int timeStreamIndex = 0;

    public static final int PRODUCT_CODE = 11;
    public static final int PRODUCT_NAME = 12;
    public static final int PRODUCT_EXP = 13;
    public static final int PRODUCT_EXP_TIME_UNIT = 14;
    public static final int TIMESTREAM_DOP = 15;
    public static final int TIMESTREAM_COORDINATE = 16;
    public static final int TIMESTREAM_INVENTORY = 17;

    public static final int MAIN_ACTIVITY = 0;
    public static final int PD_INFO_ACTIVITY = 1;
    public static final int POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY = 2;
    public static final int PROMOTION_TIMESTREAM_ACTIVITY = 3;
    public static final int POSSIBLE_EXPIRED_TIMESTREAM_ACTIVITY = 4;
    public static final int EXPIRED_TIMESTREAM_ACTIVITY = 5;

    public static DraggableLinearLayout draggableLinearLayout;

    public static Product currentProduct;

    public static int activityIndex;
    //数据库助手，全局
    public static MyDatabaseHelper databaseHelper;
    public static SQLiteDatabase sqLiteDatabase;

    static Point originalPosition;

    static LinearLayout temp;

    public static LinkedHashMap<Integer, Timestream> onShowTimeStreamsHashMap = new LinkedHashMap<>(); // hashMap存放当前展示的时光流

    public static  HashMap<Integer, Point> originalPositionHashMap = new HashMap<>(); // hashMap存放每个时光流的初始坐标

    public static LinkedList thingsToSaveList = new LinkedList();

    public static Date today = DateUtil.getNow();
//
//    static {
//
//        TimeZone.setDefault(MyApplication.timeZone);
//
//    }

    // 将改变的信息保存到thingsToSaveList里面
    public static void pickupChanges() {

        if (MyApplication.currentProduct != null) {

            if (!MyApplication.currentProduct.isUpdated()) {

                thingsToSaveList.add(MyApplication.currentProduct);

            }

            if (!MyApplication.currentProduct.getTimeStreams().isEmpty()) {

                for (Timestream timeStream : currentProduct.getTimeStreams().values()) {

                    if (!timeStream.isUpdated()) {

                        thingsToSaveList.add(timeStream);

                    }

                }

            }

        }
    }

    public static void recordTimeStreamView() {

        if (DraggableLinearLayout.isLayoutChanged()) {

            switch (activityIndex) {

                case PD_INFO_ACTIVITY:

                    for (int i = 0; i < draggableLinearLayout.getChildCount() - 4; i++) {

                        recordViewStateByChildIndex(i + 3);
                    }
                    break;

                case POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY:

                    for (int i = 0; i < draggableLinearLayout.getChildCount() - 1; i++) {

                        recordViewStateByChildIndex(i + 1);

                    }
                    break;
            }


        }
        DraggableLinearLayout.setLayoutChanged(false);
    }

    private static void recordViewStateByChildIndex(int childIndex) {

        temp = (LinearLayout) draggableLinearLayout.getChildAt(childIndex);

        originalPosition = new Point(temp.getLeft(), temp.getTop());
        MyApplication.originalPositionHashMap.put(temp.getId(), originalPosition);

    }

    public static void init() {

        onShowTimeStreamsHashMap.clear();
        originalPositionHashMap.clear();
    }

    public static void initDatabase(Context context) {

        if (MyApplication.databaseHelper == null) {

            MyApplication.databaseHelper = new MyDatabaseHelper(context,"ProductDateStreamline.db",
                    null, 1);
        }

        if (MyApplication.sqLiteDatabase == null) {

            MyApplication.sqLiteDatabase = MyApplication.databaseHelper.getWritableDatabase();
        }


    }

    public static void onTimestreamViewReleased(View releasedChild, float horizontalDistance, float verticalDistance, float xvel, float yvel) {

        switch (MyApplication.activityIndex) {

            case PD_INFO_ACTIVITY:

                PDInfoActivity.onTimestreamViewReleased(releasedChild, horizontalDistance, verticalDistance);

                break;

            case POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY:
                PossiblePromotionTimestreamActivity.onTimestreamViewReleased(releasedChild, horizontalDistance);
                break;

        }
    }

    public static void afterInfoChanged(String after, EditText watchedEditText, Timestream timestream, int filedIndex) {

        boolean infoValidated = AIInputter.validate(after, timestream, filedIndex);

        if (infoValidated) {

            switch (filedIndex) {

                case PRODUCT_CODE:

                    break;

                case PRODUCT_NAME:

                    currentProduct.setProductName(after);
                    currentProduct.setUpdated(false);

                    break;

                case PRODUCT_EXP:

                    currentProduct.setProductEXP(after);
                    synchronize(null, filedIndex);

                    break;

                case PRODUCT_EXP_TIME_UNIT:

                    currentProduct.setProductEXPTimeUnit(after);
                    synchronize(null, filedIndex);


                case TIMESTREAM_DOP:

                    if (after.length() <= 8) {

                        try {

                            after = AIInputter.translate(currentProduct, timestream.getProductDOP(), after);

                            MyTextWatcher.setShouldWatch(false);
                            watchedEditText.setText(after);
                            MyTextWatcher.setShouldWatch(true);

                            DraggableLinearLayout.setFocus(watchedEditText);

                        } catch (Exception e) {

                            e.printStackTrace();
                        }

                        try {

                            timestream.setProductDOP(DateUtil.typeMach(after));

                        } catch (Exception e) {

                            e.printStackTrace();

                        }

                    }
                    synchronize(timestream, filedIndex);
                    break;

                case TIMESTREAM_COORDINATE:

                    timestream.setProductCoordinate(after);
                    timestream.setUpdated(false);
                    break;

                case TIMESTREAM_INVENTORY:

                    timestream.setProductInventory(after);
                    timestream.setUpdated(false);
                    break;
                }


        }

    }

    /**
     * 根据timeStream日期状态设置linearLayout颜色，针对单个timestream
     */

    public static void setTimeStreamViewOriginalBackgroundColor(Timestream ts) {

        int timeStreamStateCode = ts.getTimeStreamStateCode();
        LinearLayout timeStreamLinearLayout =
                draggableLinearLayout.findViewById(Integer.parseInt(ts.getBoundLayoutId()
        ));

        switch (timeStreamStateCode) {

            case 1:

                timeStreamLinearLayout.setBackgroundColor(Color.YELLOW);
                break;

            case -1:

                timeStreamLinearLayout.setBackgroundColor(Color.GRAY);
                break;

            default:
                timeStreamLinearLayout.setBackgroundColor(0);
                break;

        }

    }

    /**
     * 根据timeStream日期状态设置linearLayout颜色，针对单个timestream
     */

    public static void setTimeStreamViewOriginalBackgroundColor(LinearLayout timestreamLinearLayout) {

        Timestream ts = onShowTimeStreamsHashMap.get(timestreamLinearLayout.getId());

        int timeStreamStateCode = ts.getTimeStreamStateCode();

        switch (timeStreamStateCode) {

            case 1:

                timestreamLinearLayout.setBackgroundColor(Color.YELLOW);
                break;

            case -1:

                timestreamLinearLayout.setBackgroundColor(Color.GRAY);
                break;

            default:
                timestreamLinearLayout.setBackgroundColor(0);
                break;

        }

    }

    private static void synchronize(@Nullable Timestream timestream, int filedIndex) {

        switch (filedIndex) {

            case PRODUCT_EXP:
            case PRODUCT_EXP_TIME_UNIT:

                currentProduct.setUpdated(false);

                for (Timestream ts : currentProduct.getTimeStreams().values()) {

                    synchronizeSingleTimestream(ts);
                }
                break;

            case TIMESTREAM_DOP:

                synchronizeSingleTimestream(timestream);
                break;

        }
    }

    private static void synchronizeSingleTimestream(Timestream timestream) {

        timestream.setProductPromotionDate(DateUtil.calculatePromotionDate(
                timestream.getProductDOP(),
                Integer.parseInt(currentProduct.getProductEXP()),
                currentProduct.getProductEXPTimeUnit()
        ));

        timestream.setProductExpireDate(DateUtil.calculateProductExpireDate(
                timestream.getProductDOP(),
                Integer.parseInt(currentProduct.getProductEXP()),
                currentProduct.getProductEXPTimeUnit()

        ));

        setTimeStreamViewOriginalBackgroundColor(timestream);

        timestream.setUpdated(false);

    }

    public static void onViewPositionChanged(View changedView, float horizontalDistance, float verticalDistance) {

        switch (activityIndex) {

            case PD_INFO_ACTIVITY:

                PDInfoActivity.onTimestreamViewPositionChanged(changedView, horizontalDistance, verticalDistance);
                break;

            case POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY:

                PossiblePromotionTimestreamActivity.onTimestreamViewPositionChanged(changedView, horizontalDistance);
                break;
        }
    }

    public static Timestream removeTimestream(LinearLayout releasedChild) {


        Timestream mT = onShowTimeStreamsHashMap.remove(releasedChild.getId());

        MyDatabaseHelper.PDInfoWrapper.deleteProductDOP(MyApplication.sqLiteDatabase, mT.getId());

        originalPositionHashMap.remove(releasedChild.getId());

        DraggableLinearLayout.setLayoutChanged(true);

        for (int i = 0; i < releasedChild.getChildCount(); i++) {

            Object o = releasedChild.getChildAt(i);

            if (o instanceof EditText) MyTextWatcher.removeWatcher((EditText) o);
        }

        draggableLinearLayout.removeView(releasedChild);

        return mT;
    }

}
