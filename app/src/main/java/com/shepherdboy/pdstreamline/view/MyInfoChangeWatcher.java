package com.shepherdboy.pdstreamline.view;

import static com.shepherdboy.pdstreamline.MyApplication.currentProduct;

import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.activities.PDInfoActivity;
import com.shepherdboy.pdstreamline.activities.SettingActivity;
import com.shepherdboy.pdstreamline.beans.DateScope;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.beans.Shelf;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.utils.AIInputter;
import com.shepherdboy.pdstreamline.utils.DateUtil;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MyInfoChangeWatcher implements TextWatcher, View.OnFocusChangeListener {

    public static HashMap<EditText, MyInfoChangeWatcher> myTextWatchers;
    public static HashMap<Integer, HashMap<EditText, MyInfoChangeWatcher>> activityWatchers = new HashMap<>();

    private boolean autoCommitOnLostFocus = false;

    public static final int SELECT_ALL = 1;
    public static final int LAZY_LOAD = 2;

    private static boolean scheduled = false;
    private static Handler commitHandler;
    public static Handler infoHandler;
    private static Runnable commitRunnable;
    private static long timeMillis = 0;
    private static long lastInputTimeMillis = 0;
    private static MyInfoChangeWatcher currentWatcher;

    private EditText watchedEditText;
    private Timestream timestream;
    private DateScope scope;
    private int filedIndex;
    private static boolean shouldWatch = true;
    private String preInf = "";
    private String currentInf = "";
    private Shelf shelf;

    static {

        initHandler();
    }

    private static void initHandler() {
        infoHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {

                switch (msg.what) {

                    case SELECT_ALL:

                        EditText editText = (EditText) msg.obj;
                        Editable text = editText.getText();
                        if (text.length() > 0) {
                            setShouldWatch(false);
                            text.replace(0, 1, text.subSequence(0, 1), 0, 1);
                            editText.selectAll();
                            setShouldWatch(true);
                        }
                        break;

                    case LAZY_LOAD:

                        if (msg.obj == null) {
                            MyApplication.lazyLoad(null);
                        }

                        Product p = (Product) msg.obj;
                        MyApplication.lazyLoad(p);
                        break;

                    default:
                        break;

                }

            }
        };

        MyApplication.handlers.add(infoHandler);
    }

    public static void init(int activityIndex) {

        if (!activityWatchers.containsKey(activityIndex))
            activityWatchers.put(activityIndex, new HashMap<>());
        myTextWatchers = activityWatchers.get(activityIndex);

        initHandler();
    }

    /**
     * 监听货架基本信息修改
     * @param editText
     * @param shelf
     * @param filedIndex
     */
    public static void watch(EditText editText, Shelf shelf, int filedIndex) {

        editText.removeTextChangedListener(myTextWatchers.remove(editText));

        MyInfoChangeWatcher myInfoChangeWatcher = new MyInfoChangeWatcher();
        editText.addTextChangedListener(myInfoChangeWatcher);
        editText.setOnFocusChangeListener(myInfoChangeWatcher);

        myInfoChangeWatcher.autoCommitOnLostFocus = true;

        myInfoChangeWatcher.watchedEditText = editText;
        myInfoChangeWatcher.filedIndex = filedIndex;
        myInfoChangeWatcher.shelf = shelf;
        myTextWatchers.put(editText, myInfoChangeWatcher);

    }

    public static void watch(EditText editText, @Nullable Timestream timestream, int filedIndex, boolean autoCommitOnLastFocus) {

        editText.removeTextChangedListener(myTextWatchers.remove(editText));

        MyInfoChangeWatcher myInfoChangeWatcher = new MyInfoChangeWatcher();
        editText.addTextChangedListener(myInfoChangeWatcher);
        myInfoChangeWatcher.autoCommitOnLostFocus = autoCommitOnLastFocus;

        if (autoCommitOnLastFocus) editText.setOnFocusChangeListener(myInfoChangeWatcher);

        myInfoChangeWatcher.timestream = timestream;
        myInfoChangeWatcher.watchedEditText = editText;
        myInfoChangeWatcher.filedIndex = filedIndex;

        myTextWatchers.put(editText, myInfoChangeWatcher);
    }

    public static void watch(DateScope scope, EditText editText, int index) {

        editText.removeTextChangedListener(myTextWatchers.remove(editText));

        MyInfoChangeWatcher myInfoChangeWatcher = new MyInfoChangeWatcher();
        editText.addTextChangedListener(myInfoChangeWatcher);

        myInfoChangeWatcher.scope = scope;
        myInfoChangeWatcher.watchedEditText = editText;
        myInfoChangeWatcher.filedIndex = index;

        myTextWatchers.put(editText, myInfoChangeWatcher);
    }

    public static void watch(DateScope scope, TextView t, int index) {

        t.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (!shouldWatch) {
                    return true;
                }

                String oldTimeUnit;
                String newTimeUnit = null;

                oldTimeUnit = (String) t.getText();

                switch (oldTimeUnit) {

                    case "天":

                        newTimeUnit = "年";
                        break;

                    case "年":

                        newTimeUnit = "月";
                        break;

                    case "月":

                        newTimeUnit = "天";
                        break;

                }

                t.setText(newTimeUnit);

                MyApplication.afterInfoChanged( t, scope, index, newTimeUnit);

                return true;
            }
        });
    }

    public static void watch(Button button) {

        switch (MyApplication.activityIndex) {

            case MyApplication.PD_INFO_ACTIVITY:

                button.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        if (!shouldWatch) {
                            return true;
                        }

                        String oldTimeUnit;
                        String newTimeUnit = null;

                        oldTimeUnit = PDInfoActivity.productEXPTimeUnitButton.getText().toString();

                        switch (oldTimeUnit) {

                            case "天":

                                newTimeUnit = "年";
                                break;

                            case "年":

                                newTimeUnit = "月";
                                break;

                            case "月":

                                newTimeUnit = "天";
                                break;

                        }

                        PDInfoActivity.productEXPTimeUnitButton.setText(newTimeUnit);

                        MyApplication.afterInfoChanged(newTimeUnit, null, null, MyApplication.PRODUCT_EXP_TIME_UNIT);

                        return true;
                    }
                });

                break;

            case MyApplication.SETTING_ACTIVITY:

                if (button.hasOnClickListeners()) return;

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Date date = null;
                        try {
                            date = DateUtil.typeMach(SettingActivity.settingInstance.getNextSalesmanCheckDay());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        assert date != null;
                        if (date.after(DateUtil.getStartPointToday())) {

                            date = DateUtil.switchDate(date, Calendar.DATE, -1);
                            button.setText("今天");

                        } else {

                            date = DateUtil.switchDate(date, Calendar.DATE, 1);
                            button.setText("明天");
                        }

                        SettingActivity.settingInstance.setNextSalesmanCheckDay(DateUtil.typeMach(date));
                        SettingActivity.settingInstance.setUpdated(false);
                        SettingActivity.setExpSettingChanged(true);
                        SettingActivity.getInstance().loadLastSalesmanCheckDate();
                    }
                });
                break;
        }
    }

    private static void clearAllWatchers() {
        for (HashMap<EditText, MyInfoChangeWatcher> watcherHashMap : activityWatchers.values()) {
            for (Map.Entry<EditText, MyInfoChangeWatcher> entry : watcherHashMap.entrySet()) {

                entry.getKey().removeTextChangedListener(entry.getValue());
                entry.getKey().setOnFocusChangeListener(null);
                entry.getValue().stopAutoCommit();
            }

            watcherHashMap.clear();
        }
        currentWatcher = null;
    }

    public static void clearWatchers(int activityIndex) {

        HashMap<EditText, MyInfoChangeWatcher> watcherHashMap = activityWatchers.get(activityIndex);

        if (watcherHashMap == null) return;
        for (Map.Entry<EditText, MyInfoChangeWatcher> entry : watcherHashMap.entrySet()) {

            entry.getKey().removeTextChangedListener(entry.getValue());
            entry.getKey().setOnFocusChangeListener(null);
            entry.getValue().stopAutoCommit();
        }

        watcherHashMap.clear();

        currentWatcher = null;
    }

    public static void destroy() {

        clearAllWatchers();

        if (infoHandler != null) {

            infoHandler.removeCallbacksAndMessages(null);
            infoHandler = null;
        }

    }

    public static void removeWatcher(View view) {

        if (view instanceof EditText) {

            removeWatcher((EditText) view);
            return;
        }

        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View v = ((ViewGroup) view).getChildAt(i);
                removeWatcher(v);
            }
        }
    }

    public static void removeWatcher(EditText editText) {

        MyInfoChangeWatcher myInfoChangeWatcher;

        if (myTextWatchers.containsKey(editText)) {

            myInfoChangeWatcher = myTextWatchers.remove(editText);
            myInfoChangeWatcher.watchedEditText.removeTextChangedListener(myInfoChangeWatcher);
            myInfoChangeWatcher = null;
            editText.setOnFocusChangeListener(null);
        }
    }

    public static boolean isShouldWatch() {
        return shouldWatch;
    }

    public static void setShouldWatch(boolean shouldWatch) {
        if (shouldWatch) {

            for(MyInfoChangeWatcher w : myTextWatchers.values()) {

                w.preInf = w.watchedEditText.getText().toString().trim();

                if (!w.preInf.equals("") && w.filedIndex == MyApplication.TIMESTREAM_DOP) {

                    w.preInf = DateUtil.getShortKey(w.preInf);

                }
                w.currentInf = w.preInf;
            }
        }
        MyInfoChangeWatcher.shouldWatch = shouldWatch;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {

        long currentTimeMillis = System.currentTimeMillis();

        long inputInterval = currentTimeMillis - lastInputTimeMillis;

        lastInputTimeMillis = currentTimeMillis;

        if (inputInterval > 500 && inputInterval < 2000) {

            AIInputter.recordInputTimeInterval(inputInterval);
        }

        if (shouldWatch) {

            currentInf = DateUtil.getShortKey(s.toString().trim());

            if (!preInf.equals(currentInf)) {

                currentWatcher = this;
                timeMillis = System.currentTimeMillis();
                startAutoCommit();
            }
        }

    }

    public static void setScheduled(boolean scheduled) {
        MyInfoChangeWatcher.scheduled = scheduled;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

        if (v instanceof EditText && hasFocus) {

            DraggableLinearLayout.selectAllAfter((EditText) v, 5);
        }

        if (v instanceof EditText && (!hasFocus) && (!preInf.equals(currentInf)) && this.autoCommitOnLostFocus) {

            stopAutoCommit();
            commit();
        }

    }

    public void startAutoCommit() {

        if (scheduled) return;

        commitHandler = new Handler();
        commitRunnable = new Runnable() {
            @Override
            public void run() {

                long interval = System.currentTimeMillis() - timeMillis;
                long averageInterval = AIInputter.getAutoCommitInterval();


                if (filedIndex == MyApplication.TIMESTREAM_DOP && currentInf.length() == 8) {

                    MyApplication.afterInfoChanged(currentInf, watchedEditText, timestream,
                            filedIndex);

                    stopAutoCommit();
                    return;

                }

                if (interval >= averageInterval) {

                    commit();

//                    handler.postDelayed(this, 10000);
                    stopAutoCommit();
                    return;
                }

                commitHandler.postDelayed(this, 10);
            }
        };

        commitHandler.postDelayed(commitRunnable, 10);
        setScheduled(true);
        MyApplication.handlers.add(commitHandler);
    }

    private void commit() {

        switch (MyApplication.activityIndex) {

            case MyApplication.SETTING_ACTIVITY:

                MyApplication.afterInfoChanged(watchedEditText,scope,filedIndex,currentInf);
                break;

            case MyApplication.TRAVERSAL_TIMESTREAM_ACTIVITY_MODIFY_SHELF:

                MyApplication.afterInfoChanged(shelf, currentInf, watchedEditText, filedIndex);
                preInf = currentInf;
                break;

            default:

                if (currentProduct == null && timestream != null)
                    currentProduct = MyApplication.getAllProducts().get(timestream.getProductCode());

                MyApplication.afterInfoChanged(currentInf, currentWatcher.watchedEditText, currentWatcher.timestream,
                        currentWatcher.filedIndex);

        }
    }

    public void stopAutoCommit() {

        if (commitHandler != null) {

            commitHandler.removeCallbacks(commitRunnable);
            commitHandler = null;
            commitRunnable = null;
            setScheduled(false);
        }
    }

    /**
     * 停止倒计时自动提交，将发生变更的信息全部提交
     */
    public static void commitAll() {

        for (MyInfoChangeWatcher w : myTextWatchers.values()) {

            if (!w.currentInf.equals(w.preInf)) {

                w.stopAutoCommit();
                w.commit();
            }

        }
    }
}
