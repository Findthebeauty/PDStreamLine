package com.shepherdboy.pdstreamline.view;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.activities.PDInfoActivity;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.utils.DateUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MyTextWatcher implements TextWatcher, View.OnFocusChangeListener {

    public static HashMap<EditText, MyTextWatcher> myTextWatchers = new HashMap<>();

    private int delayCount = 0;


    private boolean scheduled = false;
    private static Timer timer = new Timer();
    private TimerTask timerTask;

    private EditText watchedEditText;
    private Timestream timestream;
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

    public static void removeWatcher(EditText editText) {

        MyTextWatcher myTextWatcher;

        if (myTextWatchers.containsKey(editText)) {

            myTextWatcher = myTextWatchers.remove(editText);
            myTextWatcher.watchedEditText.removeTextChangedListener(myTextWatcher);
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

                MyApplication.afterInfoChanged(currentInf, watchedEditText, timestream, filedIndex);

            }
        }

    }

    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {


    }
}
