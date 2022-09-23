package com.shepherdboy.pdstreamline.view;

import static com.shepherdboy.pdstreamline.MyApplication.PD_INFO_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF;
import static com.shepherdboy.pdstreamline.MyApplication.currentProduct;
import static com.shepherdboy.pdstreamline.MyApplication.sqLiteDatabase;

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
import com.shepherdboy.pdstreamline.beanview.BeanView;
import com.shepherdboy.pdstreamline.dao.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;
import com.shepherdboy.pdstreamline.utils.AIInputter;
import com.shepherdboy.pdstreamline.utils.DateUtil;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ActivityInfoChangeWatcher {

    private static final HashMap<Integer, ActivityInfoChangeWatcher> activityInfoChangeWatchers = new HashMap<>();
    private final HashMap<EditText, Watcher> myWatchers = new HashMap<>();

    public static final int SELECT_ALL = 1;
    public static final int LAZY_LOAD = 2;

    private boolean scheduled = false;
    private Handler commitHandler;
    public Handler infoHandler;
    private Runnable commitRunnable;
    private long timeMillis = 0;
    private long lastInputTimeMillis = 0;
    private int activityIndex;

    private boolean shouldWatch = true;

    public static ActivityInfoChangeWatcher getActivityWatcher(int activityIndex) {

        return activityInfoChangeWatchers.get(activityIndex);
    }

    public HashMap<EditText, Watcher> getMyWatchers() {
        return myWatchers;
    }

    class Watcher implements TextWatcher, View.OnFocusChangeListener {

        private boolean autoCommitOnLostFocus = false;

        private EditText watchedEditText;
        private Timestream timestream;
        private DateScope scope;
        private int filedIndex;
        private String preInf = "";
        private String currentInf = "";
        private Shelf shelf;

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

                    timeMillis = System.currentTimeMillis();
                    startAutoCommit(this);
                }
            }

        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {

            if (v instanceof EditText && hasFocus) {

                DraggableLinearLayout.selectAllAfter(activityIndex, (EditText) v, 5);
                Watcher watcher = myWatchers.get(v);
                Timestream timestream;

                if (watcher != null && ((timestream = watcher.timestream) != null)) {

                    if (currentProduct != null &&
                            !currentProduct.getProductCode().equals(timestream.getProductCode())) {

                        currentProduct = PDInfoWrapper.getProduct(timestream.getProductCode(),
                                sqLiteDatabase, MyDatabaseHelper.ENTIRE_TIMESTREAM);
                    }
                }

                switch (activityIndex) {

                    case TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF:
                    case PD_INFO_ACTIVITY:
                        ClosableScrollView.postLocate(ClosableScrollView.SCROLL_FROM_TOUCH, v.getParent());
                        break;

                    default:
                        break;

                }

            }

            if (v instanceof EditText && (!hasFocus) && (!preInf.equals(currentInf)) && autoCommitOnLostFocus) {

                stopAutoCommit();
                commit(this);
            }

            if (activityIndex == MyApplication.TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF && hasFocus) {

                BeanView beanView = (BeanView) v.getParent().getParent();

                currentProduct = PDInfoWrapper.getProduct(beanView.getProductCode(), sqLiteDatabase,
                        MyDatabaseHelper.ENTIRE_TIMESTREAM);
            }

        }
    }

    public ActivityInfoChangeWatcher(Integer activityIndex) {
        initHandler();
        activityInfoChangeWatchers.put(activityIndex, this);
        this.activityIndex = activityIndex;
    }

    private void initHandler() {
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

    /**
     * 监听货架基本信息修改
     * @param editText
     * @param shelf
     * @param filedIndex
     */
    public void watch(EditText editText, Shelf shelf, int filedIndex) {

        editText.removeTextChangedListener(myWatchers.remove(editText));

        Watcher watcher = new Watcher();
        editText.addTextChangedListener(watcher);
        editText.setOnFocusChangeListener(watcher);

        watcher.autoCommitOnLostFocus = true;

        watcher.watchedEditText = editText;
        watcher.filedIndex = filedIndex;
        watcher.shelf = shelf;
        myWatchers.put(editText, watcher);

    }

    public void watch(EditText editText, @Nullable Timestream timestream, int filedIndex, boolean autoCommitOnLastFocus) {

        editText.removeTextChangedListener(myWatchers.remove(editText));

        Watcher watcher = new Watcher();
        editText.addTextChangedListener(watcher);
        watcher.autoCommitOnLostFocus = autoCommitOnLastFocus;

        if (autoCommitOnLastFocus) editText.setOnFocusChangeListener(watcher);

        watcher.timestream = timestream;
        watcher.watchedEditText = editText;
        watcher.filedIndex = filedIndex;

        myWatchers.put(editText, watcher);
    }

    public void watch(DateScope scope, EditText editText, int index) {

        editText.removeTextChangedListener(myWatchers.remove(editText));

        Watcher watcher = new Watcher();
        editText.addTextChangedListener(watcher);

        watcher.scope = scope;
        watcher.watchedEditText = editText;
        watcher.filedIndex = index;

        myWatchers.put(editText, watcher);
    }

    public void watch(DateScope scope, TextView t, int index) {

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

    public void watch(Button button) {

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

                        MyApplication.afterInfoChanged(newTimeUnit, null, null,
                                MyApplication.PRODUCT_EXP_TIME_UNIT,
                                ActivityInfoChangeWatcher.this.activityIndex);

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

    public void clearWatchers(int activityIndex) {

        stopAutoCommit();

        for (Map.Entry<EditText, Watcher> entry : myWatchers.entrySet()) {

            entry.getKey().removeTextChangedListener(entry.getValue());
            entry.getKey().setOnFocusChangeListener(null);
        }

        myWatchers.clear();

    }

    public static void destroyAll() {

        for(ActivityInfoChangeWatcher watcher : activityInfoChangeWatchers.values()) {
            watcher.destroy();
        }
        activityInfoChangeWatchers.clear();
    }

    public void destroy() {

        if (infoHandler != null) {

            infoHandler.removeCallbacksAndMessages(null);
            infoHandler = null;
        }

        clearWatchers(this.activityIndex);
    }

    public void removeWatcher(View view) {

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

    public void removeWatcher(EditText editText) {

        Watcher myInfoChangeWatcher;

        if (myWatchers.containsKey(editText)) {

            myInfoChangeWatcher = myWatchers.remove(editText);
            myInfoChangeWatcher.watchedEditText.removeTextChangedListener(myInfoChangeWatcher);
            myInfoChangeWatcher = null;
            editText.setOnFocusChangeListener(null);
        }
    }

    public boolean isShouldWatch() {
        return shouldWatch;
    }

    public void setShouldWatch(boolean shouldWatch) {
        if (shouldWatch) {

            for(Watcher w : myWatchers.values()) {

                w.preInf = w.watchedEditText.getText().toString().trim();

                if (!w.preInf.equals("") && w.filedIndex == MyApplication.TIMESTREAM_DOP) {

                    w.preInf = DateUtil.getShortKey(w.preInf);

                }
                w.currentInf = w.preInf;
            }
        }
        this.shouldWatch = shouldWatch;
    }

    public void startAutoCommit(Watcher watcher) {

        if (scheduled) return;

        commitHandler = new Handler();
        commitRunnable = new Runnable() {
            @Override
            public void run() {

                long interval = System.currentTimeMillis() - timeMillis;
                long averageInterval = AIInputter.getAutoCommitInterval();


                if (watcher.filedIndex == MyApplication.TIMESTREAM_DOP && watcher.currentInf.length() == 8) {

                    MyApplication.afterInfoChanged(watcher.currentInf, watcher.watchedEditText, watcher.timestream,
                            watcher.filedIndex, ActivityInfoChangeWatcher.this.activityIndex);

                    stopAutoCommit();
                    return;

                }

                if (interval >= averageInterval) {

                    commit(watcher);

//                    handler.postDelayed(this, 10000);
                    stopAutoCommit();
                    return;
                }

                commitHandler.postDelayed(this, 10);
            }
        };

        commitHandler.postDelayed(commitRunnable, 10);
        scheduled = true;
        MyApplication.handlers.add(commitHandler);
    }

    private void commit(Watcher watcher) {

        switch (MyApplication.activityIndex) {

            case MyApplication.SETTING_ACTIVITY:

                MyApplication.afterInfoChanged(watcher.watchedEditText,watcher.scope,watcher.filedIndex,watcher.currentInf);
                break;

            case MyApplication.TRAVERSAL_TIMESTREAM_ACTIVITY_MODIFY_SHELF:

                MyApplication.afterInfoChanged(watcher.shelf, watcher.currentInf, watcher.watchedEditText, watcher.filedIndex);
                watcher.preInf = watcher.currentInf;
                break;

            default:

                if (currentProduct == null && watcher.timestream != null)
                    currentProduct = MyApplication.getAllProducts().get(watcher.timestream.getProductCode());

                MyApplication.afterInfoChanged(watcher.currentInf, watcher.watchedEditText, watcher.timestream,
                        watcher.filedIndex, ActivityInfoChangeWatcher.this.activityIndex);
                if (watcher.watchedEditText != null && watcher.watchedEditText.hasFocus()) {

                    DraggableLinearLayout.selectAll(watcher.watchedEditText);
                }
                break;

        }
    }

    public void stopAutoCommit() {

        if (commitHandler != null) {

            commitHandler.removeCallbacks(commitRunnable);
            commitHandler = null;
            commitRunnable = null;
            scheduled = false;
        }
    }

    /**
     * 停止倒计时自动提交，将发生变更的信息全部提交
     */
    public void commitAll() {

        for (Watcher w : myWatchers.values()) {

            if (!w.currentInf.equals(w.preInf)) {

                stopAutoCommit();
                commit(w);
            }

        }
    }
}
