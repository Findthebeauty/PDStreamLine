package com.shepherdboy.pdstreamline.view;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.activities.PDInfoActivity;
import com.shepherdboy.pdstreamline.activities.SettingActivity;
import com.shepherdboy.pdstreamline.beans.DateScope;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.utils.AIInputter;
import com.shepherdboy.pdstreamline.utils.DateUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MyInfoChangeWatcher implements TextWatcher, View.OnFocusChangeListener {

    public static HashMap<EditText, MyInfoChangeWatcher> myTextWatchers = new HashMap<>();

    private static boolean scheduled = false;
    private static Handler handler;
    private static Runnable runnable;
    private static long timeMillis = 0;
    private static long lastInputTimeMillis = 0;
    private static MyInfoChangeWatcher currentWatcher;
    private static String info;

    private EditText watchedEditText;
    private Timestream timestream;
    private DateScope scope;
    private int filedIndex;
    private static boolean shouldWatch = true;
    private String preInf = "";
    private String currentInf = "";

    public static void watch(EditText editText, @Nullable Timestream timestream, int filedIndex) {

        MyInfoChangeWatcher myInfoChangeWatcher = new MyInfoChangeWatcher();
        editText.addTextChangedListener(myInfoChangeWatcher);

        myInfoChangeWatcher.timestream = timestream;
        myInfoChangeWatcher.watchedEditText = editText;
        myInfoChangeWatcher.filedIndex = filedIndex;

        myTextWatchers.put(editText, myInfoChangeWatcher);
    }

    public static void watch(DateScope scope, EditText editText, int index) {

        if (myTextWatchers.containsKey(editText)) return;
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

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Date date = SettingActivity.settingInstance.getNextSalesmanCheckDay();

                        if (date.after(DateUtil.getStartPointToday())) {

                            date = DateUtil.switchDate(date, Calendar.DATE, -1);
                            button.setText("今天");

                        } else {

                            date = DateUtil.switchDate(date, Calendar.DATE, 1);
                            button.setText("明天");
                        }

                        SettingActivity.settingInstance.setNextSalesmanCheckDay(date);
                        SettingActivity.settingInstance.setUpdated(false);
                        SettingActivity.getInstance().loadLastSalesmanCheckDate();
                    }
                });
                break;

        }


    }

    public static void clearWatchers() {

        for (Map.Entry<EditText, MyInfoChangeWatcher> entry : myTextWatchers.entrySet()) {

            entry.getKey().removeTextChangedListener(entry.getValue());

        }

        myTextWatchers.clear();
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

            currentInf = s.toString().trim();

            if (!preInf.equals(currentInf)) {

                currentWatcher = this;
                info = currentInf;
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


    }

    public void startAutoCommit() {

        if (scheduled) return;

        handler = new Handler();
        runnable = new Runnable() {
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

                    switch (MyApplication.activityIndex) {

                        case MyApplication.SETTING_ACTIVITY:

                            MyApplication.afterInfoChanged(watchedEditText,scope,filedIndex,currentInf);
                            break;

                        default:

                            MyApplication.afterInfoChanged(info, currentWatcher.watchedEditText, currentWatcher.timestream,
                                    currentWatcher.filedIndex);

                    }

                    handler.postDelayed(this, 10000);
                    stopAutoCommit();
                    return;
                }

                handler.postDelayed(this, 10);
            }
        };

        handler.postDelayed(runnable, 10);
        setScheduled(true);
    }

    public void stopAutoCommit() {

        if (handler != null) {

            handler.removeCallbacks(runnable);
            handler = null;
            runnable = null;
            setScheduled(false);
        }
    }
}
