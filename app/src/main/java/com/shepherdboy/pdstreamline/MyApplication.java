package com.shepherdboy.pdstreamline;

import static com.shepherdboy.pdstreamline.activities.SettingActivity.settingInstance;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.alibaba.fastjson.JSON;
import com.shepherdboy.pdstreamline.activities.MainActivity;
import com.shepherdboy.pdstreamline.activities.PDInfoActivity;
import com.shepherdboy.pdstreamline.activities.PossiblePromotionTimestreamActivity;
import com.shepherdboy.pdstreamline.activities.ScanActivity;
import com.shepherdboy.pdstreamline.activities.SettingActivity;
import com.shepherdboy.pdstreamline.activities.TraversalTimestreamActivity;
import com.shepherdboy.pdstreamline.beans.DateScope;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.beans.Shelf;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.beans.TimestreamCombination;
import com.shepherdboy.pdstreamline.beanview.BeanView;
import com.shepherdboy.pdstreamline.sql.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.sql.PDInfoWrapper;
import com.shepherdboy.pdstreamline.utils.AIInputter;
import com.shepherdboy.pdstreamline.utils.DateUtil;
import com.shepherdboy.pdstreamline.view.ClosableScrollView;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;
import com.shepherdboy.pdstreamline.view.MyInfoChangeWatcher;
import com.shepherdboy.pdstreamline.view.TouchEventDispatcher;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 */
public class MyApplication extends Application {

    //设置时区
//    public static final TimeZone timeZone = TimeZone.getTimeZone("Asia/Chongqing");

    public static final int PRODUCT_CODE = 11;
    public static final int PRODUCT_NAME = 12;
    public static final int PRODUCT_EXP = 13;
    public static final int PRODUCT_EXP_TIME_UNIT = 14;
    public static final int PRODUCT_SPEC = 15;
    public static final int TIMESTREAM_DOP = 16;
    public static final int TIMESTREAM_COORDINATE = 17;
    public static final int TIMESTREAM_INVENTORY = 18;
    public static final int TIMESTREAM_BUY_SPECS = 19;
    public static final int TIMESTREAM_PRESENT_SPECS = 20;

    public static final int SHELF_NAME = 21;
    public static final int SHELF_CLASSIFY = 22;
    public static final int SHELF_MAX_ROW = 23;

    public static final int MAIN_ACTIVITY = 0;
    public static final int PD_INFO_ACTIVITY = 1;
    public static final int POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY = 2;
    public static final int PROMOTION_TIMESTREAM_ACTIVITY = 3;
    public static final int POSSIBLE_EXPIRED_TIMESTREAM_ACTIVITY = 4;
    public static final int EXPIRED_TIMESTREAM_ACTIVITY = 5;
    public static final int SETTING_ACTIVITY = 6;
    public static final int TRAVERSAL_TIMESTREAM_ACTIVITY = 7;
    public static final int TRAVERSAL_TIMESTREAM_ACTIVITY_MODIFY_SHELF = 8;
    public static final int TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF = 9;

    public static final int ITEM_SELECTED = 100;

    public static Set<Handler> handlers = new HashSet<>();

    public static DraggableLinearLayout draggableLinearLayout;

    public static Product currentProduct;

    public static int activityIndex;

    public static int DRAG_RANGE_FIRST_LEVEL = Color.parseColor("#8BC34A");
    public static int DRAG_RANGE_SECOND_LEVEL = Color.parseColor("#FF0000");

    public static final ColorDrawable drawableSecondLevel = new ColorDrawable(MyApplication.DRAG_RANGE_SECOND_LEVEL);
    public static final ColorDrawable drawableFirstLevel = new ColorDrawable(MyApplication.DRAG_RANGE_FIRST_LEVEL);

//    public static ScrollView scrollView;

    private static long lastClickTime = 0L;
    private static int clickCount;
    private static long pressInterval;
    private static Handler handler;
    private static Runnable runnable;
    private static boolean scheduled;
    //数据库助手，全局
    public static MyDatabaseHelper databaseHelper;
    public static SQLiteDatabase sqLiteDatabase;
    //sqlite本地数据库地址，全局
    public static String databasePath;

    static Point originalPosition;

    private static Context context;

    static LinearLayout temp;

    public static LinkedHashMap<Integer, Timestream> onShowTimeStreamsHashMap = new LinkedHashMap<>(); // hashMap存放当前展示的时光流，key为viewId
    public static LinkedHashMap<Integer, TimestreamCombination> onShowCombsHashMap = new LinkedHashMap<>(); // hashMap存放当前展示的捆绑商品，key为viewId

    public static  HashMap<Integer, Point> originalPositionHashMap = new HashMap<>(); // hashMap存放每个时光流的初始坐标，key为viewId
    public static  HashMap<Integer, Drawable> originalBackgroundHashMap = new HashMap<>(); // hashMap存放每个view的初始背景，key为viewId

    public static LinkedList thingsToSaveList = new LinkedList();

    public static HashMap<String, Product> allProducts; //无Timestream的Product

    public static HashMap<String, TimestreamCombination> combinationHashMap; //已经捆绑的所有商品

    public static Date today = DateUtil.getStartPointToday();

    public static void setContext(Context context) {
        MyApplication.context = context;
    }

    public static void initActionBar(ActionBar actionBar) {

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.actionbar_layout);

        TextView mainPage = actionBar.getCustomView().findViewById(R.id.main_page);
        TextView setting = actionBar.getCustomView().findViewById(R.id.setting);

        mainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.actionStart();
            }
        });

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SettingActivity.actionStart();
            }
        });
    }

    public static void setScheduled(boolean scheduled) {
        MyApplication.scheduled = scheduled;
    }



    public static boolean tryCaptureClickEvent(MotionEvent event) {

        long clickInterval = 0L;

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {

            clickCount++;

            if (lastClickTime != 0L) clickInterval = System.currentTimeMillis() - lastClickTime;

            lastClickTime = System.currentTimeMillis();

            startCountPressTime();
        }

        if (event.getActionMasked() ==  MotionEvent.ACTION_UP) {

            draggableLinearLayout.setLongClicking(false);
            stopCountPressTime();

            switch (activityIndex) {

                case TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF:

                    draggableLinearLayout.getCapturedView().setBackground(
                            originalBackgroundHashMap.get(draggableLinearLayout.getCapturedView().getId()));
                    break;
                default:
                    break;
            }
        }

        if (clickInterval > Long.parseLong(settingInstance.getDoubleClickDelay())) {

            clickCount = 1;
            return false;
        }

        if (clickCount > 1) {

            clickCount = 0;
            lastClickTime = 0L;
            return onDBClick();
        }

        return false;
    }

    private static boolean onDBClick() {

        stopCountPressTime();
        switch (activityIndex) {

            case TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF:

                BeanView beanView = (BeanView) draggableLinearLayout.getCapturedView();

                String code = beanView.getProductCode();
                PDInfoActivity.actionStart(code);

                break;

            case PD_INFO_ACTIVITY:
                serialize();
                TraversalTimestreamActivity.actionStart();
                break;

            default:
                break;

        }

        return true;
    }

    public static void stopCountPressTime() {

        if (handler != null) {

            handler.removeCallbacks(runnable);
            handler = null;
            runnable = null;
            setScheduled(false);
        }
    }

    private static void startCountPressTime() {

        if (scheduled) return;

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {

                pressInterval = System.currentTimeMillis() - lastClickTime;

                if (pressInterval >= Long.parseLong(settingInstance.getLongClickDelay())) {

                    clickCount = 0;

                    if((!ClosableScrollView.isFlingFinished()) || !TouchEventDispatcher.validateDragRange(
                            ClosableScrollView.getDeltaX(), ClosableScrollView.getDeltaY(),90d)) return;

                    draggableLinearLayout.setLongClicking(true);
                    onLongClick();
                }

            }
        };

        handler.postDelayed(runnable, Long.parseLong(settingInstance.getLongClickDelay()));
        setScheduled(true);

        handlers.add(handler);
    }

    private static boolean onLongClick() {

        switch (activityIndex) {

            case TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF:

                draggableLinearLayout.setVerticalDraggable(true);
                draggableLinearLayout.setBackgroundToPointedView(ITEM_SELECTED);

        }

        return false;
    }

    public static void restoreTimestreams(LinkedList linkedList) {

        while (!linkedList.isEmpty()) {

            Timestream t = (Timestream) linkedList.remove();
            t.setInBasket(false);
            PDInfoWrapper.updateInfo(sqLiteDatabase, t, MyDatabaseHelper.UPDATE_BASKET);
        }
    }


    public static void saveChanges() {

        pickupChanges();

        while (!thingsToSaveList.isEmpty()) {

            Object bean = thingsToSaveList.remove();

            if (bean instanceof Product) {

                PDInfoWrapper.updateInfo(sqLiteDatabase, (Product) bean);

            }

            if (bean instanceof Timestream) {

                int state = ((Timestream) bean).getTimeStreamStateCode();

                switch (state) {

                    case Timestream.FRESH:

                        PDInfoWrapper.updateInfo(sqLiteDatabase, (Timestream) bean, MyDatabaseHelper.FRESH_TIMESTREAM_TABLE_NAME);
                        break;

                    case Timestream.CLOSE_TO_EXPIRE:

                        PDInfoWrapper.updateInfo(sqLiteDatabase, (Timestream) bean, MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME);
                        break;

                    case Timestream.EXPIRED:

                        PDInfoWrapper.updateInfo(sqLiteDatabase, (Timestream) bean, MyDatabaseHelper.POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME);
                        break;
                }


            }

        }
    }

    public static void serialize() {

        saveChanges();
    }

    public static boolean tryCatchVolumeDown(Activity activity, int keyCode) {

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {

            ScanActivity.actionStart(activity);

            return true;
        }


        return false;
    }

    /**
     * @param activity 注册扫描功能的activity
     */
    public static void registerCameraScanner(Activity activity, View view) {

//        view.setOnClickListener(new View.OnClickListener() { todo 活的按钮
//            @Override
//            public void onClick(View v) {
//                Log.d("registerCameraScanner", "I'm in!");
//            }
//        });
    }

    /**
     * 将日期记录上传到服务器
     */
    public static void uploadData() {

        List<Timestream> notPromotionTimestreams = PDInfoWrapper.getAllTimestreams(SettingActivity.TIMESTREAM_NOT_IN_PROMOTION);
        List<Timestream> promotionTimestreams = PDInfoWrapper.getAllTimestreams(SettingActivity.TIMESTREAM_IN_PROMOTION);

        String scopeSetting = MyDatabaseHelper.getSetting(SettingActivity.DATE_OFFSET_INDEX, MyApplication.sqLiteDatabase);
        String singletonSetting = MyDatabaseHelper.getSetting(SettingActivity.SETTING_SINGLETON_INDEX_NAME, MyApplication.sqLiteDatabase);

        String notPromotionTS = JSON.toJSONString(notPromotionTimestreams);
        String promotionTS = JSON.toJSONString(promotionTimestreams);

        String url = "https://h41548z146.goho.co";

        OkHttpClient okHttpClient = new OkHttpClient();

        FormBody formBody = new FormBody.Builder()
                .add("data", notPromotionTS).build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("uploadData", "失败:" + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                Log.d("uploadData", "成功:" + response.body().toString());
                Log.d("uploadData", "成功:" + response.message());
            }
        });


    }

    public static void downloadData() {

        String url = "https://h41548z146.goho.co";
        OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("downloadData", "失败:" + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("downloadData", "成功：" + response.message());
            }
        });
    }

    public static void afterInfoChanged(Shelf shelf,String currentInf, EditText watchedEditText, int filedIndex) {

        boolean validated = AIInputter.validate(currentInf, shelf, filedIndex);

        shelf.setInfoChanged(true);

        switch (filedIndex) {

            case SHELF_NAME:

                if (validated) {

                    shelf.setName(currentInf);

                } else {

                    MyInfoChangeWatcher.setShouldWatch(false);
                    watchedEditText.setText(shelf.getName());
                    MyInfoChangeWatcher.setShouldWatch(true);

                }
                break;

            case SHELF_MAX_ROW:

                if (validated) {

                    shelf.setMaxRowCount(Integer.parseInt(currentInf));

                } else {

                    Toast.makeText(getContext(), "货架行数不合法", Toast.LENGTH_SHORT).show();
                    MyInfoChangeWatcher.setShouldWatch(false);
                    watchedEditText.setText(String.valueOf(shelf.getMaxRowCount()));
                    MyInfoChangeWatcher.setShouldWatch(true);
                }

            default:
                break;
        }
    }

    public static int getColorByTimestreamStateCode(int timeStreamStateCode) {


        switch (timeStreamStateCode) {

            case Timestream.FRESH:

                return Color.GREEN;

            case Timestream.CLOSE_TO_EXPIRE:

                return Color.YELLOW;

            case Timestream.EXPIRED:

                return Color.GRAY;

            default:
                break;
        }

        return 0;
    }
//
//    static {
//
//        TimeZone.setDefault(MyApplication.timeZone);
//
//    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
        initDatabase(context);
        SettingActivity.initSetting();
    }

    public static Context getContext() {
        return context;
    }

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

    public static void recordDraggableView() {

        if (DraggableLinearLayout.isLayoutChanged()) {

            switch (activityIndex) {

                case PD_INFO_ACTIVITY:

                case TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF:

                    for (int i = 0; i < draggableLinearLayout.getChildCount() - 1; i++) {

                        recordViewStateByChildIndex(draggableLinearLayout, i);
                    }
                    break;

                case POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY:

                case SETTING_ACTIVITY:

                    for (int i = 0; i < draggableLinearLayout.getChildCount() - 1; i++) {

                        recordViewStateByChildIndex(draggableLinearLayout, i + 1);

                    }
                    break;

                case PROMOTION_TIMESTREAM_ACTIVITY:

                    for (int i = 0; i < draggableLinearLayout.getChildCount(); i++) {

                        recordViewStateByChildIndex(draggableLinearLayout, i);

                        temp = (LinearLayout) draggableLinearLayout.getChildAt(i);

                        if (temp instanceof DraggableLinearLayout) {

                            for (int j = 0; j < temp.getChildCount(); j++) {

                                recordViewStateByChildIndex(temp, j);
                            }
                        }
                    }
                    break;

            }


        }
        DraggableLinearLayout.setLayoutChanged(false);
    }

    private static void recordViewStateByChildIndex(ViewGroup parent, int childIndex) {

        temp = (LinearLayout) parent.getChildAt(childIndex);

        originalPosition = new Point(temp.getLeft(), temp.getTop());
        MyApplication.originalPositionHashMap.put(temp.getId(), originalPosition);
        MyApplication.originalBackgroundHashMap.put(temp.getId(),temp.getBackground());
    }

    public static void init() {

        onShowTimeStreamsHashMap.clear();

        clearOriginalInfo();
    }

    public static void clearOriginalInfo() {
        originalPositionHashMap.clear();
        originalBackgroundHashMap.clear();

    }

    public static void initDatabase(Context context) {

        databasePath = context.getFilesDir().getPath().replaceAll("files", "databases/streamline.db");
//        MyDatabaseHelper.copyDataBase(databasePath);

        if (MyApplication.databaseHelper == null) {

            MyApplication.databaseHelper = new MyDatabaseHelper(context, "streamline.db",
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

            case SETTING_ACTIVITY:
                SettingActivity.onScopeViewReleased(releasedChild, horizontalDistance);
                break;

            case TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF:
                TraversalTimestreamActivity.onViewReleased(releasedChild, horizontalDistance, verticalDistance);
                break;

            default:
                DraggableLinearLayout container = DraggableLinearLayout.getContainer(releasedChild);
                container.putBack(releasedChild);
                break;

        }
    }

    public static void makeToast(Context context, String info, int duration) {

        Toast.makeText(context,info, duration).show();

    }

    public static void afterInfoChanged(TextView v, DateScope scope, int index, String after) {

        boolean validated = AIInputter.validate(scope, index, after);

        switch (index) {

            case SettingActivity.DATE_SCOPE_RANGE_VALUE:

                if (!validated) {

                    makeToast(SettingActivity.getInstance(), "下限值超界", Toast.LENGTH_SHORT);
                    return;
                }
                scope.setRangeValue(after);
                break;

            case SettingActivity.DATE_SCOPE_RANGE_UNIT:

                if (!validated) {

                    makeToast(SettingActivity.getInstance(), "下限单位超界", Toast.LENGTH_SHORT);
                    scope.setRangeUnit(after);
                    synchronizeSetting(scope, index);
                    return;
                }
                break;

            case SettingActivity.DATE_SCOPE_PROMOTION_OFFSET_VALUE:

                if (!validated) {

                    makeToast(SettingActivity.getInstance(), "临期偏移量值超界", Toast.LENGTH_SHORT);
                    return;
                }
                scope.setPromotionOffsetValue(after);
                break;

            case SettingActivity.DATE_SCOPE_EXPIRE_OFFSET_VALUE:

                if (!validated) {

                    makeToast(SettingActivity.getInstance(), "下架偏移量值超界", Toast.LENGTH_SHORT);
                    return;
                }
                scope.setExpireOffsetValue(after);
                break;

            case SettingActivity.SINGLETON_SETTING_AUTO_COMMIT_DELAY:

                if (validated) {

                    settingInstance.setUpdated(false);
                    settingInstance.setAutoCommitDelay(after);

                } else {

                    makeToast(SettingActivity.getInstance(), "延时值不合法", Toast.LENGTH_SHORT);
                    v.setText(settingInstance.getAutoCommitDelay());
                    return;

                }
                break;

            case SettingActivity.SINGLETON_SETTING_DOUBLE_CLICK_DELAY:

                if (validated) {

                    settingInstance.setDoubleClickDelay(after);

                } else {

                    makeToast(SettingActivity.getInstance(), "双击间隔时间值不合法", Toast.LENGTH_SHORT);
                    v.setText(settingInstance.getDoubleClickDelay());
                    return;

                }

                break;

            case SettingActivity.SINGLETON_SETTING_LONG_CLICK_DELAY:

                if (validated) {

                    settingInstance.setLongClickDelay(after);

                } else {

                    makeToast(SettingActivity.getInstance(), "长按触发延时值不合法", Toast.LENGTH_SHORT);
                    v.setText(settingInstance.getLongClickDelay());
                    return;

                }

                break;
            default:
                break;
        }
        synchronizeSetting(scope, index);
        SettingActivity.setExpSettingChanged(true);
    }

    public static void afterInfoChanged(String after, EditText watchedEditText, Timestream timestream, int filedIndex) {

        boolean infoValidated = AIInputter.validate(after, timestream, filedIndex);

        if (infoValidated) {

            switch (filedIndex) {

                case PRODUCT_CODE:

                    if (allProducts == null) allProducts = PDInfoWrapper.getAllProduct();
                    if (allProducts.containsKey(after)) {

                        Message msg = Message.obtain();
                        msg.obj = after;
                        PDInfoActivity.getShowHandler().sendMessage(msg);
                    }
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

                case PRODUCT_SPEC:

                    currentProduct.setProductSpec(after);
                    currentProduct.setUpdated(false);


                case TIMESTREAM_DOP:

                    if (after.length() <= 8) {

                        try {

                            after = AIInputter.translate(currentProduct, timestream.getProductDOP(), after);

                            MyInfoChangeWatcher.setShouldWatch(false);

                            watchedEditText.setText(after);

                            if (watchedEditText.hasFocus()) {

                                DraggableLinearLayout.selectAll(watchedEditText);
                            }

                            MyInfoChangeWatcher.setShouldWatch(true);

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

                case TIMESTREAM_BUY_SPECS:
                    timestream.setBuySpecs(after);
                    timestream.setUpdated(false);
                    break;

                case TIMESTREAM_PRESENT_SPECS:
                    timestream.setGiveawaySpecs(after);
                    timestream.setUpdated(false);
                    break;
                }

        }

    }

    /**
     * 根据timeStream日期状态设置linearLayout颜色，针对单个timestream
     */

    public static void setTimeStreamViewOriginalBackgroundColor(Timestream ts) {

        if (ts == null) return;

        LinearLayout timeStreamLinearLayout =
                draggableLinearLayout.findViewById(Integer.parseInt(ts.getBoundLayoutId()
        ));

        setTimeStreamViewOriginalBackgroundColor(timeStreamLinearLayout);

    }

    /**
     * 根据timeStream日期状态设置linearLayout颜色，针对单个timestream
     */

    public static void setTimeStreamViewOriginalBackgroundColor(LinearLayout timestreamLinearLayout) {

        Timestream ts = onShowTimeStreamsHashMap.get(timestreamLinearLayout.getId());


        switch (activityIndex) {

            case TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF:
                Drawable drawable = originalBackgroundHashMap.get(draggableLinearLayout.getCapturedView().getId());
                draggableLinearLayout.getCapturedView().setBackground(drawable);
                break;

            default:

                int timeStreamStateCode = 0;

                if (ts == null) return;

                timeStreamStateCode = ts.getTimeStreamStateCode();

                switch (timeStreamStateCode) {

                        case 1:

                            timestreamLinearLayout.setBackgroundColor(Color.YELLOW);
                            break;

                        case -1:

                            timestreamLinearLayout.setBackgroundColor(Color.GRAY);
                            break;

                        default:
                            timestreamLinearLayout.setBackgroundColor(Color.GREEN);
                            break;

                    }
                break;
        }

    }


    private static void synchronizeSetting(DateScope scope, int index) {

        switch (index) {

            case SettingActivity.DATE_SCOPE_RANGE_VALUE:
            case SettingActivity.DATE_SCOPE_RANGE_UNIT:

                SettingActivity.initScopeIndex();
                SettingActivity.synchronizeUpperBound(scope);
                break;

            case SettingActivity.DATE_SCOPE_PROMOTION_OFFSET_VALUE:
            case SettingActivity.DATE_SCOPE_EXPIRE_OFFSET_VALUE:

                //todo 临期和下架偏移量改变后所有timestream的同步

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

        switch (activityIndex) {

            case TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF:

                timestream.setBuySpecs(null);
                timestream.setGiveawaySpecs(null);
                timestream.setSiblingPromotionId(null);
                timestream.setDiscountRate("");
                timestream.setPromoting(false);
                PDInfoWrapper.updateInfo(sqLiteDatabase, timestream, MyDatabaseHelper.FRESH_TIMESTREAM_TABLE_NAME);
                break;

            default:
                break;
        }
    }

    public static void onViewPositionChanged(View changedView, float horizontalDistance, float verticalDistance) {

        switch (activityIndex) {

            case PD_INFO_ACTIVITY:

                PDInfoActivity.onTimestreamViewPositionChanged(changedView, horizontalDistance, verticalDistance);
                break;

            case POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY:

                PossiblePromotionTimestreamActivity.onTimestreamViewPositionChanged(changedView, horizontalDistance);
                break;

            case SETTING_ACTIVITY:
                SettingActivity.onScopeViewPositionChanged(changedView, horizontalDistance);
                break;

            case TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF:
                TraversalTimestreamActivity.onViewPositionChanged(changedView, horizontalDistance, verticalDistance);
                break;

            default:
                break;
        }
    }

    public static Timestream unloadTimestream(LinearLayout releasedChild) {

        Timestream mT = onShowTimeStreamsHashMap.remove(releasedChild.getId());

        if (mT == null) return null;

        onShowCombsHashMap.remove(mT.getId());

        originalPositionHashMap.remove(releasedChild.getId());
        originalBackgroundHashMap.remove(releasedChild.getId());

        DraggableLinearLayout.setLayoutChanged(true);

        for (int i = 0; i < releasedChild.getChildCount(); i++) {

            Object o = releasedChild.getChildAt(i);

            if (o instanceof EditText) MyInfoChangeWatcher.removeWatcher((EditText) o);
        }

        draggableLinearLayout.removeView(releasedChild);

        return mT;
    }


}
