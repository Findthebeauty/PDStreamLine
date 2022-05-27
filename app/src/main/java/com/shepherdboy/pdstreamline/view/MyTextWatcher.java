package com.shepherdboy.pdstreamline.view;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.activities.PDInfoActivity;
import com.shepherdboy.pdstreamline.beans.DateScope;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.utils.DateUtil;

import java.util.HashMap;
import java.util.Map;

public class MyTextWatcher implements TextWatcher, View.OnFocusChangeListener {

    public static HashMap<EditText, MyTextWatcher> myTextWatchers = new HashMap<>();

    private static boolean scheduled = false;
    private static Handler handler;
    private static Runnable runnable;
    private static long timeMillis = 0;
    private static MyTextWatcher currentWatcher;
    private static String info;


    private EditText watchedEditText;
    private Timestream timestream;
    private DateScope scope;
    private int filedIndex;
    private static boolean shouldWatch = true;
    private String preInf = "";
    private String currentInf = "";

    public static void watch(EditText editText, @Nullable Timestream timestream, int filedIndex) {

        MyTextWatcher myTextWatcher = new MyTextWatcher();
        editText.addTextChangedListener(myTextWatcher);

        myTextWatcher.timestream = timestream;
        myTextWatcher.watchedEditText = editText;
        myTextWatcher.filedIndex = filedIndex;

        myTextWatchers.put(editText, myTextWatcher);
    }

    public static void watch(DateScope scope, EditText editText, int index) {

        MyTextWatcher myTextWatcher = new MyTextWatcher();
        editText.addTextChangedListener(myTextWatcher);

        myTextWatcher.scope = scope;
        myTextWatcher.watchedEditText = editText;
        myTextWatcher.filedIndex = index;

        myTextWatchers.put(editText, myTextWatcher);
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


    }

    public static void clearWatchers() {

        for (Map.Entry<EditText, MyTextWatcher> entry : myTextWatchers.entrySet()) {

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

        MyTextWatcher myTextWatcher;

        if (myTextWatchers.containsKey(editText)) {

            myTextWatcher = myTextWatchers.remove(editText);
            myTextWatcher.watchedEditText.removeTextChangedListener(myTextWatcher);
            myTextWatcher = null;
        }
    }

    public static boolean isShouldWatch() {
        return shouldWatch;
    }

    public static void setShouldWatch(boolean shouldWatch) {
        if (shouldWatch) {

            for(MyTextWatcher w : myTextWatchers.values()) {

                w.preInf = w.watchedEditText.getText().toString().trim();

                if (w.preInf != "") {

                    w.preInf = DateUtil.getShortKey(w.preInf);

                }
            }
        }
        MyTextWatcher.shouldWatch = shouldWatch;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {


        if (shouldWatch) {

            currentInf = s.toString().trim();

            if (!preInf.equals(currentInf)) {

                if (MyApplication.activityIndex == MyApplication.SETTING_ACTIVITY) {

                    MyApplication.afterInfoChanged(watchedEditText, scope, filedIndex, currentInf);

                } else {

                    currentWatcher = this;
                    info = currentInf;
                    timeMillis = System.currentTimeMillis();
                    startAutoCommit();
                }

            }
        }

    }

    public static void setScheduled(boolean scheduled) {
        MyTextWatcher.scheduled = scheduled;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {


    }

    public static void startAutoCommit() {

        if (scheduled) return;

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {

                long interval = System.currentTimeMillis() - timeMillis;

                if (interval >= 1234) {

                    Log.d("autoCommit", "I'm in!" + interval);
                    MyApplication.afterInfoChanged(info, currentWatcher.watchedEditText, currentWatcher.timestream,
                            currentWatcher.filedIndex);

                    handler.postDelayed(this, 1000000);
                    stopAutoCommit();
                    return;
                }

                Log.d("autoCommit", "NormalTick" + interval);
                handler.postDelayed(this, 10);
            }
        };

        handler.postDelayed(runnable, 10);
        setScheduled(true);
    }

    public static void stopAutoCommit() {

        if (handler != null) {

            handler.removeCallbacks(runnable);
            handler = null;
            runnable = null;
            setScheduled(false);
        }
    }
}
