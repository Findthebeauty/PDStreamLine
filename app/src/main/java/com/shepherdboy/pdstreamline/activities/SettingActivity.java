package com.shepherdboy.pdstreamline.activities;

import static com.shepherdboy.pdstreamline.MyApplication.SETTING_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.activityIndex;
import static com.shepherdboy.pdstreamline.MyApplication.currentProduct;
import static com.shepherdboy.pdstreamline.MyApplication.draggableLinearLayout;
import static com.shepherdboy.pdstreamline.view.DraggableLinearLayout.DRAG_LEFT;
import static com.shepherdboy.pdstreamline.view.DraggableLinearLayout.DRAG_RIGHT;
import static com.shepherdboy.pdstreamline.view.DraggableLinearLayout.getViewState;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.beans.DateScope;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.beans.SingletonSettingBean;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.dao.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;
import com.shepherdboy.pdstreamline.utils.DateUtil;
import com.shepherdboy.pdstreamline.view.ActivityInfoChangeWatcher;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 *
 */
public class SettingActivity extends BaseActivity {

    public static final String AUTO_COMMIT_DELAY_TAG_NAME = "autoCommitDelayMs";

    public static final int DATE_SCOPE_RANGE_VALUE = 0;
    public static final int DATE_SCOPE_RANGE_UNIT = 1;
    public static final int DATE_SCOPE_PROMOTION_OFFSET_VALUE = 2;
    public static final int DATE_SCOPE_PROMOTION_OFFSET_UNIT = 3;
    public static final int DATE_SCOPE_EXPIRE_OFFSET_VALUE = 4;
    public static final int DATE_SCOPE_EXPIRE_OFFSET_UNIT = 5;
    public static final int SINGLETON_SETTING_AUTO_COMMIT_DELAY = 6;
    public static final int SINGLETON_SETTING_SALESMAN_LAST_CHECK_DATE = 7;
    public static final int SINGLETON_SETTING_DOUBLE_CLICK_DELAY = 8;
    public static final int SINGLETON_SETTING_LONG_CLICK_DELAY = 9;

    public static SingletonSettingBean settingInstance;

    private static ActivityInfoChangeWatcher watcher;
    //保存初始获取的所有scope用于后续的排序与加载
    public static List<DateScope> scopeList;
    //scopeId与scope映射
    public static final HashMap<String, DateScope> dateSettingMap = new HashMap<>();
    //scope下界毫秒值与scope映射
    public static final HashMap<Long, DateScope> mlScopeMap = new HashMap<>();
    //scopeViewId与scope映射
    public static final HashMap<Integer, DateScope> onShowScopeMap = new HashMap<>();
    //降序的scope下界毫秒值列表
    public static ArrayList<Long> dateSettingIndex;
    //相邻两个scope共享一个边界，上面的下界是下面的上界，上面的下界scopeId对应下界的上界TextView，用于共享边界的同步与插入scope
    private static final HashMap<String, TextView> upperBoundTextViewMap = new HashMap<>();

    @Override
    protected void onDestroy() {
        super.onDestroy();

        draglayout = null;
    }

    private static DraggableLinearLayout draglayout;

    public static final String DATE_OFFSET_INDEX = "dateOffset";
    public static final String SETTING_SINGLETON_INDEX_NAME = "settingSingleton";

    private final TextView[] textViews = new TextView[5];
    private final EditText[] editTexts = new EditText[3];
    private final LinearLayout[] box = new LinearLayout[3];

    public static final int TIMESTREAM_IN_PROMOTION = 3;
    public static final int TIMESTREAM_NOT_IN_PROMOTION = 4;

    private static boolean expSettingChanged = false;

    private static SettingActivity instance;

    public static long getUpperBoundMls(DateScope scope) {

        long mlsIndex = SettingActivity.stringToMillionSeconds(
                scope.getRangeValue(),scope.getRangeUnit()
        );

        long upperBoundMls = Long.MAX_VALUE;

        int i = dateSettingIndex.indexOf(mlsIndex);

        if ( i > 0 ) {
            upperBoundMls = dateSettingIndex.get(i - 1);
        }

        return upperBoundMls;
    }

    public static long getLowerBoundMls(DateScope scope) {


        long mlsIndex = SettingActivity.stringToMillionSeconds(
                scope.getRangeValue(),scope.getRangeUnit()
        );

        long lowerBoundMls = 0;

        int i = dateSettingIndex.indexOf(mlsIndex);

        if ( i < dateSettingIndex.size() - 1 ) {
            lowerBoundMls = dateSettingIndex.get(i + 1);
        }

        return lowerBoundMls;
    }


    public static void changeViewStateByDistance(View changedView, float horizontalDistance) {

        int viewState = getViewState(changedView, horizontalDistance);

        switch (viewState) {

            case DRAG_RIGHT:

                changedView.setBackgroundColor(Color.parseColor("#8BC34A"));
                break;

            case DRAG_LEFT:

                changedView.setBackgroundColor(Color.parseColor("#FF0000"));
                break;

            default:

                changedView.setBackgroundColor(0);
        }
    }

    public static void onScopeViewReleased(View releasedChild, float horizontalDistance) {

        int stateCode = getViewState(releasedChild, horizontalDistance);

        switch (stateCode) {

            case DRAG_RIGHT:

                addScopeBeyond(releasedChild);
                releasedChild.setBackgroundColor(0);
                break;

            case DRAG_LEFT:

                deleteScope(releasedChild);
                break;

            default:

                draggableLinearLayout.putBack(releasedChild);
                break;
        }

    }

    private static void deleteScope(View releasedChild) {

        watcher.setShouldWatch(false);

        int id = releasedChild.getId();
        DateScope scope = onShowScopeMap.remove(id);

        long mls = stringToMillionSeconds(scope.getRangeValue(), scope.getRangeUnit());

        int removeIndex = dateSettingIndex.indexOf(mls) + 1;
        dateSettingIndex.remove(mls);
        scopeList.remove(scope);
        mlScopeMap.remove(mls);
        dateSettingMap.remove(scope.getScopeId());
        upperBoundTextViewMap.remove(scope.getScopeId());
        initScopeIndex();

        ViewGroup p = (ViewGroup) releasedChild.getParent();
        p.removeView(releasedChild);

        watcher.removeWatcher(releasedChild);

        LinearLayout nextScopeView = (LinearLayout) draggableLinearLayout.getChildAt(removeIndex);
        LinearLayout upperScopeView = null;

        if (removeIndex > 1) upperScopeView = (LinearLayout) draggableLinearLayout.getChildAt(removeIndex - 1);

        connectScopeView(nextScopeView, upperScopeView);
        watcher.setShouldWatch(true);
        setExpSettingChanged(true);
        draglayout.setLayoutChanged(true);
    }

    private static void addScopeBeyond(View releasedChild) {

        watcher.setShouldWatch(false);

        LinearLayout parent = (LinearLayout) releasedChild.getParent();
        int addIndex = 1;
        for (int i = 1; i < parent.getChildCount(); i++) {

            LinearLayout child = (LinearLayout) parent.getChildAt(i);
            if (child.getId() == releasedChild.getId()) {

                addIndex = i;
                break;
            }
        }

        LinearLayout newScopeView = getInstance().addScopeView(parent, addIndex);
        LinearLayout lowerScopeView = (LinearLayout) releasedChild;
        LinearLayout upperScopeView = null;

        if (addIndex > 1) {

            upperScopeView = (LinearLayout) draggableLinearLayout.getChildAt(addIndex - 1);
        }

        DateScope newScope = generateScope(onShowScopeMap.get(releasedChild.getId()), addIndex);

        long newBound = stringToMillionSeconds(newScope.getRangeValue(), newScope.getRangeUnit());
        dateSettingMap.put(newScope.getScopeId(),newScope);
        mlScopeMap.put(newBound, newScope);
        scopeList.add(newScope);
        initScopeIndex();

        long upperBound = getUpperBoundMls(newScope);

        getInstance().loadScope(newBound, upperBound, newScopeView);

        connectScopeView(lowerScopeView, newScopeView);
        connectScopeView(newScopeView, upperScopeView);

        EditText lEditText = ((EditText)((LinearLayout)(newScopeView.getChildAt(0))).getChildAt(0));

        watcher.setShouldWatch(true);
        setExpSettingChanged(true);
        DraggableLinearLayout.selectAll(lEditText);
        draglayout.setLayoutChanged(true);
    }

    private static void connectScopeView(LinearLayout lowerScopeView, LinearLayout upperScopeView) {

        if (lowerScopeView == null) {

            int lastId = draggableLinearLayout.getChildAt(draggableLinearLayout.getChildCount() - 1).getId();
            DateScope lastScope = onShowScopeMap.get(lastId);
            upperBoundTextViewMap.remove(lastScope.getScopeId());
            return;
        }

        TextView t = (TextView) ((LinearLayout)(lowerScopeView.getChildAt(0))).getChildAt(3);
        TextView connector = (TextView) ((LinearLayout)(lowerScopeView.getChildAt(0))).getChildAt(2);

        if (upperScopeView == null) {

            connector.setText("及");
            t.setText("以上");
            return;
        }

        DateScope upperScope = onShowScopeMap.get(upperScopeView.getId());
        connector.setText("(含)~");
        t.setText(upperScope.getRange());

        upperBoundTextViewMap.put(upperScope.getScopeId(), t);

    }

    private static DateScope generateScope(DateScope template, int addIndex) {

        DateScope copy = new DateScope(template);

        if (addIndex > 1) {

            Long millionSeconds = ((long)dateSettingIndex.get(addIndex - 2) +
                    (long)dateSettingIndex.get(addIndex - 1)) / 2;

            copy.setRangeValue(millionSecondsToString(millionSeconds, copy.getRangeUnit()));

        } else {

            copy.setRangeValue(String.valueOf(Integer.parseInt(copy.getRangeValue()) + 1));
        }

        return copy;
    }


    @Override
    protected void onStop() {

        watcher.clearWatchers(SETTING_ACTIVITY);
        if (isExpSettingChanged()) {
            saveSetting();
        }
        super.onStop();
    }

    public static SettingActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        watcher = new ActivityInfoChangeWatcher(SETTING_ACTIVITY);
    }

    @Override
    protected void onStart() {

        super.onStart();
        initActivity();
    }

    private void initActivity() {

        setContentView(R.layout.activity_setting);

        instance = SettingActivity.this;

        MyApplication.initActionBar(getSupportActionBar());
        MyApplication.init();
        MyApplication.initDatabase(this);
        draglayout = findViewById(R.id.date_offset_setting_table);
        draggableLinearLayout = draglayout;
        activityIndex = MyApplication.SETTING_ACTIVITY;
        initDateSettingView();

        if (dateSettingMap.isEmpty() || dateSettingIndex == null || settingInstance == null)
            initSetting();

        loadSetting();

        TextView textView = getSupportActionBar().getCustomView().findViewById(R.id.setting);

        textView.setText("应用");
        textView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                saveSetting();
                SettingActivity.this.finish();
                watcher.commitAll();
                watcher.clearWatchers(SETTING_ACTIVITY);
            }
        });

        Button setDefaultBt = findViewById(R.id.default_setting);
        Button saveSettingBt = findViewById(R.id.save_setting);

        setDefaultBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setAllToDefaultDateSetting();
            }
        });

        saveSettingBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveSetting();
            }
        });
    }

    private void loadSetting() {

        watcher.clearWatchers(SETTING_ACTIVITY);
        loadEXPSetting();
        loadSingletonSetting();
    }

    private void loadSingletonSetting() {

        loadTimeIntervalSetting();
        loadLastSalesmanCheckDate();
    }

    public void loadLastSalesmanCheckDate() {

        TextView t = findViewById(R.id.next_salesman_check_date_tv);
        Button b = findViewById(R.id.next_salesman_check_date_bt);

        t.setText(settingInstance.getNextSalesmanCheckDay().substring(0,10));

        Date date = null;

        try {
            date = DateUtil.typeMach(settingInstance.getNextSalesmanCheckDay());

        } catch (ParseException e) {
            e.printStackTrace();
        }


        assert date != null;
        if (date.after(DateUtil.getStartPointToday())) {

            b.setText("明天");

        } else {

            b.setText("今天");
        }

        watcher.watch(b);
//        saveSingletonSetting();
    }

    private void  loadTimeIntervalSetting() {

        CheckBox autoCommitCKBox = findViewById(R.id.auto_commit_checkbox);
        TextView autoCommitTV = findViewById(R.id.auto_commit_textview);
        EditText autoCommitEText = findViewById(R.id.auto_commit_delay_edittext);
        CheckBox doubleClickCKBox = findViewById(R.id.double_click_checkbox);
        TextView doubleClickTV = findViewById(R.id.double_click_textview);
        EditText doubleClickEText = findViewById(R.id.double_click_delay_edittext);
        CheckBox longClickCKBox = findViewById(R.id.long_click_checkbox);
        TextView longClickTV = findViewById(R.id.long_click_textview);
        EditText longClickEText = findViewById(R.id.long_click_delay_edittext);
        CheckBox autoCombine = findViewById(R.id.auto_combine_checkbox);
        TextView autoCombineTV = findViewById(R.id.auto_combine_textview);

        autoCommitCKBox.setChecked(settingInstance.isAutoCommitFlag());
        autoCommitEText.setText(settingInstance.getAutoCommitDelay());
        doubleClickCKBox.setChecked(settingInstance.isDoubleClickFlag());
        doubleClickEText.setText(settingInstance.getDoubleClickDelay());
        longClickCKBox.setChecked(settingInstance.isLongClickFlag());
        longClickEText.setText(settingInstance.getLongClickDelay());
        autoCombine.setChecked(settingInstance.isAutoCombine());

        if (!watcher.getMyWatchers().containsKey(autoCommitEText)) {

            watcher.watch(null, autoCommitEText, SINGLETON_SETTING_AUTO_COMMIT_DELAY);

            autoCommitCKBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    settingInstance.setAutoCommitFlag(isChecked);
                    settingInstance.setUpdated(false);

                    if (isChecked) {

                        autoCommitEText.setFocusable(false);
                        autoCommitEText.setFocusableInTouchMode(false);
                        autoCommitEText.setTextColor(Color.parseColor("gray"));
                        return;
                    }
                    autoCommitEText.setFocusable(true);
                    autoCommitEText.setFocusableInTouchMode(true);
                    autoCommitEText.setTextColor(Color.BLACK);

                }
            });

            if (autoCommitCKBox.isChecked()) {

                autoCommitEText.setFocusable(false);
                autoCommitEText.setFocusableInTouchMode(false);
                autoCommitEText.setTextColor(Color.parseColor("gray"));
            }

        }

        autoCommitTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoCommitCKBox.setChecked(!autoCommitCKBox.isChecked());
            }
        });

        if (!watcher.getMyWatchers().containsKey(doubleClickEText)) {

            watcher.watch(null, doubleClickEText, SINGLETON_SETTING_DOUBLE_CLICK_DELAY);

            doubleClickCKBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    settingInstance.setDoubleClickFlag(isChecked);
                    settingInstance.setUpdated(false);

                    if (isChecked) {

                        doubleClickEText.setFocusable(false);
                        doubleClickEText.setFocusableInTouchMode(false);
                        doubleClickEText.setTextColor(Color.parseColor("gray"));
                        return;
                    }
                    doubleClickEText.setFocusable(true);
                    doubleClickEText.setFocusableInTouchMode(true);
                    doubleClickEText.setTextColor(Color.BLACK);

                }
            });

            if (doubleClickCKBox.isChecked()) {

                doubleClickEText.setFocusable(false);
                doubleClickEText.setFocusableInTouchMode(false);
                doubleClickEText.setTextColor(Color.parseColor("gray"));
            }

        }

        doubleClickTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doubleClickCKBox.setChecked(!doubleClickCKBox.isChecked());
            }
        });

        if (!watcher.getMyWatchers().containsKey(longClickEText)) {

            watcher.watch(null, longClickEText, SINGLETON_SETTING_LONG_CLICK_DELAY);

            longClickCKBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    settingInstance.setLongClickFlag(isChecked);
                    settingInstance.setUpdated(false);

                    if (isChecked) {

                        longClickEText.setFocusable(false);
                        longClickEText.setFocusableInTouchMode(false);
                        longClickEText.setTextColor(Color.parseColor("gray"));
                        return;
                    }
                    longClickEText.setFocusable(true);
                    longClickEText.setFocusableInTouchMode(true);
                    longClickEText.setTextColor(Color.BLACK);

                }
            });

            if (longClickCKBox.isChecked()) {

                longClickEText.setFocusable(false);
                longClickEText.setFocusableInTouchMode(false);
                longClickEText.setTextColor(Color.parseColor("gray"));
            }

        }

        longClickTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                longClickCKBox.setChecked(!longClickCKBox.isChecked());
            }
        });

        autoCombine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingInstance.setAutoCombine(isChecked);
                settingInstance.setUpdated(false);
            }
        });

        autoCombineTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoCombine.setChecked(!autoCombine.isChecked());
            }
        });

    }

    private void setAllToDefaultDateSetting() {

        setDefaultEXPSetting();
        getDefaultSingletonSetting();

    }

    private void setDefaultEXPSetting() {

        getDefaultEXPSetting();
        initDateSettingView();

        if (mlScopeMap != null) mlScopeMap.clear();
        if (dateSettingMap != null) dateSettingMap.clear();
        if (dateSettingIndex != null) dateSettingIndex.clear();

        for (DateScope d : scopeList) {

            dateSettingMap.put(d.getScopeId(), d);
        }

        initScopeIndex();

        loadEXPSetting();
        setExpSettingChanged(true);
    }

    private void initDateSettingView() {

        int scopesCount = scopeList.size();

//        MyApplication.init();
        watcher.clearWatchers(SETTING_ACTIVITY);

        int scopesViewCount = draggableLinearLayout.getChildCount() - 1;

        while (scopesViewCount < scopesCount) {

            addScopeView(draggableLinearLayout, null);
            scopesViewCount++;
        }

        while (scopesViewCount > scopesCount) {

            deleteScope(draggableLinearLayout.getChildAt(1));
            scopesViewCount--;
        }

        draglayout.setLayoutChanged(true);

        if (upperBoundTextViewMap != null) upperBoundTextViewMap.clear();
        if (onShowScopeMap != null) onShowScopeMap.clear();
    }

    private LinearLayout addScopeView(LinearLayout rootView, Integer index) {

        if (index == null) index = 1 + MyApplication.originalPositionHashMap.size();
        Context context = rootView.getContext();
        LinearLayout childView = new LinearLayout(context);
        rootView.addView(childView, index);

        for (int i = 0; i < 5; i++) {

            textViews[i] = new TextView(context);
        }

        for (int i = 0; i < 3; i++) {

            editTexts[i] = new EditText(context);
            editTexts[i].setSelectAllOnFocus(true);
            editTexts[i].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            editTexts[i].setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        for (int i = 0; i < 3; i++) {

            box[i] = new LinearLayout(context);
            box[i].setFocusable(true);
            box[i].setFocusableInTouchMode(true);
        }

        box[0].addView(editTexts[0]);
        box[0].addView(textViews[0]);
        box[0].addView(textViews[1]);
        box[0].addView(textViews[2]);
        childView.addView(box[0]);

        box[1].addView(editTexts[1]);
        box[1].addView(textViews[3]);
        childView.addView(box[1]);

        box[2].addView(editTexts[2]);
        box[2].addView(textViews[4]);
        childView.addView(box[2]);

        decorate(childView);

        return childView;
    }

    private void decorate(LinearLayout childView) {

        childView.setId(View.generateViewId());

        WindowManager windowManager = getWindowManager();
        LinearLayout.LayoutParams lParams;

        lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        childView.setLayoutParams(lParams);


        for (int i = 0; i < 3; i++) {

            int width = (i == 0) ? 120 : 95;
            lParams = new LinearLayout.LayoutParams(DraggableLinearLayout.dpToPx(width, windowManager),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            box[i].setLayoutParams(lParams);
            box[i].setGravity(Gravity.CENTER);
            box[i].setBackgroundResource(R.drawable.table_frame_gray);
            box[i].setAlpha(0.6f);
        }

    }


    public static void synchronizeUpperBound(DateScope scope) {

        long mls = stringToMillionSeconds(scope.getRangeValue(), scope.getRangeUnit());

        if (dateSettingIndex.indexOf(mls) == dateSettingIndex.size() - 1) return;

        String range = scope.getRange();
        TextView t = upperBoundTextViewMap.get(scope.getScopeId());
        t.setText(range);
    }

    private void loadEXPSetting() {

        watcher.setShouldWatch(false);

        long upperBound = 0;
        long lowerBound = 0;
        for (int i = 0; i < dateSettingIndex.size(); i++) {

            upperBound = lowerBound;
            lowerBound = (long) dateSettingIndex.get(i);
            loadScope(lowerBound, upperBound, (LinearLayout) draggableLinearLayout.getChildAt(i + 1));
        }

        draglayout.setLayoutChanged(true);
        watcher.setShouldWatch(true);
    }

    private void loadScope(long lowerBound, long upperBound, LinearLayout t) {

        DateScope scope = mlScopeMap.get(lowerBound);
        DateScope scope1 = mlScopeMap.get(upperBound);
        onShowScopeMap.put(t.getId(), scope);
        scope.setScopeViewId(t.getId());

        box[0] = (LinearLayout)(t.getChildAt(0));
        box[1] = (LinearLayout)(t.getChildAt(1));
        box[2] = (LinearLayout)(t.getChildAt(2));
        editTexts[0] = (EditText) box[0].getChildAt(0);
        editTexts[1] = (EditText) box[1].getChildAt(0);
        editTexts[2] = (EditText) box[2].getChildAt(0);
        textViews[0] = (TextView) box[0].getChildAt(1);
        textViews[1] = (TextView) box[0].getChildAt(2);
        textViews[2] = (TextView) box[0].getChildAt(3);
        textViews[3] = (TextView) box[1].getChildAt(1);
        textViews[4] = (TextView) box[2].getChildAt(1);

        String rangeValue = scope.getRangeValue();
        String rangeUnit = scope.getRangeUnit();
        String pOffsetValue = scope.getPromotionOffsetValue();
        String eOffsetValue = scope.getExpireOffsetValue();
        editTexts[0].setText(rangeValue);
        watcher.watch(scope, editTexts[0], DATE_SCOPE_RANGE_VALUE);

        textViews[0].setText(rangeUnit);
        watcher.watch(scope, textViews[0], DATE_SCOPE_RANGE_UNIT);
        if (scope1 != null) {

            textViews[1].setText("(含)~");
            String range1 = scope1.getRange();
            textViews[2].setText(range1);
            upperBoundTextViewMap.put(scope1.getScopeId(), textViews[2]);

        } else {

            textViews[1].setText("及");
            textViews[2].setText("以上");
        }

        editTexts[1].setText(pOffsetValue);
        watcher.watch(scope, editTexts[1], DATE_SCOPE_PROMOTION_OFFSET_VALUE);
        textViews[3].setText("天");

        editTexts[2].setText(eOffsetValue);
        watcher.watch(scope, editTexts[2], DATE_SCOPE_EXPIRE_OFFSET_VALUE);
        textViews[4].setText("天");
    }

    public static boolean isExpSettingChanged() {
        return expSettingChanged;
    }

    public static void setExpSettingChanged(boolean expSettingChanged) {
        SettingActivity.expSettingChanged = expSettingChanged;
    }

    @Override
    protected void onPause() {
        draggableLinearLayout = null;

        if (isExpSettingChanged()) {
            saveSetting();
        }
        super.onPause();
    }


    private static void saveSetting() {

        saveSingletonSetting();
        saveEXPSetting();

    }

    public static void saveSingletonSetting() {


        SettingActivity.applyEXPSetting();
        PossiblePromotionTimestreamActivity.pickOutPossibleStaleTimestream();
        Gson gson = new Gson();
        String setting = gson.toJson(settingInstance);
        MyDatabaseHelper.saveSetting(SETTING_SINGLETON_INDEX_NAME, setting, MyApplication.sqLiteDatabase);
        settingInstance.setUpdated(true);
    }

    private static void saveEXPSetting() {

        if (!expSettingChanged) return;

        applyEXPSetting();
        Gson gson = new Gson();

        String setting = gson.toJson(new ArrayList(dateSettingMap.values()));
        MyDatabaseHelper.saveSetting(DATE_OFFSET_INDEX, setting, MyApplication.sqLiteDatabase);
        setExpSettingChanged(false);
    }

    /**
     * 根据新日期设置更新所有商品临期以及下架日期
     */
    public synchronized static void applyEXPSetting() {

//        MyApplication.saveChanges();

        if(!isExpSettingChanged()) return;

        HashMap<String, Product> pdMap = PDInfoWrapper.getAllProduct();

        List<Timestream> ts = PDInfoWrapper.getAllTimestreams(TIMESTREAM_IN_PROMOTION);
        applySetting(pdMap, ts, TIMESTREAM_IN_PROMOTION);
        PDInfoWrapper.truncate(MyDatabaseHelper.POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME);

        List<Timestream> ts1 = PDInfoWrapper.getAllTimestreams(TIMESTREAM_NOT_IN_PROMOTION);
        applySetting(pdMap, ts1, TIMESTREAM_NOT_IN_PROMOTION);
        PDInfoWrapper.truncate(MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME);

        if (currentProduct != null) {

            currentProduct = PDInfoWrapper.getProduct(currentProduct.getProductCode(),
                    MyApplication.sqLiteDatabase,MyDatabaseHelper.ENTIRE_TIMESTREAM);
        }
    }

    private static void applySetting(HashMap<String, Product> pdMap, List<Timestream> ts, int timestreamState) {

        for (Timestream t : ts) {

            Product p = pdMap.get(t.getProductCode());
            t.setProductPromotionDate(DateUtil.calculatePromotionDate(
                    t.getProductDOP(), Integer.parseInt(p.getProductEXP()),p.getProductEXPTimeUnit()
            ));
            t.setProductExpireDate(DateUtil.calculateProductExpireDate(
                    t.getProductDOP(), Integer.parseInt(p.getProductEXP()),p.getProductEXPTimeUnit()
            ));

            switch (timestreamState) {

                case TIMESTREAM_IN_PROMOTION:

                    PDInfoWrapper.updateInfo(MyApplication.sqLiteDatabase,
                            t, MyDatabaseHelper.PROMOTION_TIMESTREAM_TABLE_NAME);
                    break;

                case TIMESTREAM_NOT_IN_PROMOTION:

                    PDInfoWrapper.updateInfo(MyApplication.sqLiteDatabase,
                            t, MyDatabaseHelper.FRESH_TIMESTREAM_TABLE_NAME);
                    break;

            }

        }

    }

    public static void initSetting() {

        initEXPSetting();
        initSingletonSetting();
    }

    private static void initEXPSetting() {

        getEXPSetting();
        initScopeIndex();
        saveEXPSetting();
    }

    private static void initSingletonSetting() {

        String settingSingleton = MyDatabaseHelper.getSetting(SETTING_SINGLETON_INDEX_NAME, MyApplication.sqLiteDatabase);

        if (settingSingleton == null) {

            getDefaultSingletonSetting();

            return;
        }
        Gson gson = new Gson();

        settingInstance = gson.fromJson(settingSingleton, SingletonSettingBean.class);
    }

    private static void getDefaultSingletonSetting() {

        settingInstance = SingletonSettingBean.getSingletonSettingBean();
        settingInstance.setAutoCommitFlag(true);
        settingInstance.setAutoCommitDelay(readSingleDefaultSetting(AUTO_COMMIT_DELAY_TAG_NAME));
        settingInstance.setDoubleClickFlag(false);
        settingInstance.setDoubleClickDelay("300");
        settingInstance.setLongClickFlag(false);
        settingInstance.setLongClickDelay("600");
        settingInstance.setNextSalesmanCheckDay(DateUtil.typeMach(DateUtil.getStartPointToday()));
        settingInstance.setUpdated(false);

        if (instance != null) instance.loadSingletonSetting();
    }

    public static void initScopeIndex() {

        if (dateSettingIndex != null) {
            dateSettingIndex.clear();
            dateSettingIndex = null;
        }

        dateSettingIndex = new ArrayList<>();

        for (String s : dateSettingMap.keySet()) {

            DateScope d = dateSettingMap.get(s);
            assert d != null;
            long mls = stringToMillionSeconds(d.getRangeValue(), d.getRangeUnit());
            dateSettingIndex.add(mls);
            mlScopeMap.put(mls, d);
        }
        Collections.sort(dateSettingIndex, Collections.reverseOrder());
    }

    public static void getEXPSetting() {

        if (scopeList != null) scopeList.clear();

        String setting = MyDatabaseHelper.getSetting(DATE_OFFSET_INDEX, MyApplication.sqLiteDatabase);

        if (setting != null) {

            Gson gson = new Gson();

            scopeList = gson.fromJson(setting, new TypeToken<List<DateScope>>(){}.getType());

        } else {

            getDefaultEXPSetting();
        }

        for (DateScope s : scopeList) {

            dateSettingMap.put(s.getScopeId(),s);
        }
    }

    public static String readSingleDefaultSetting(String tagName) {

        String packageName = MyApplication.getMyApplicationContext().getPackageName();
        Resources resources = null;

        try {

            resources = MyApplication.getMyApplicationContext().getPackageManager().getResourcesForApplication(packageName);

        } catch (PackageManager.NameNotFoundException e) {

            e.printStackTrace();
        }

        int resId = resources.getIdentifier("default_setting", "xml", packageName);

        try (XmlResourceParser xmlResourceParser = resources.getXml(resId)){

            int eventType = xmlResourceParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if ((eventType == XmlPullParser.START_TAG) && (xmlResourceParser.getName().equals(tagName))) {

                    return xmlResourceParser.nextText();
                }

                eventType = xmlResourceParser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void getDefaultEXPSetting() {

        List<DateScope> setting = new ArrayList<>();
        String packageName = MyApplication.getMyApplicationContext().getPackageName();
        Resources resources = null;

        try {
            resources = MyApplication.getMyApplicationContext().getPackageManager().getResourcesForApplication(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        int resId = resources.getIdentifier("default_date_management_setting", "xml", packageName);

        try (XmlResourceParser xmlResourceParser = resources.getXml(resId)) {

            int eventType = xmlResourceParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if ((eventType == XmlPullParser.START_TAG) && (xmlResourceParser.getName().equals("scope"))) {

                    DateScope scope = (DateScope) parseTag(xmlResourceParser, "item");
                    setting.add(scope);
                }

                eventType = xmlResourceParser.next();
            }

        } catch (XmlPullParserException | IOException e) {

            e.printStackTrace();

        }

        scopeList = setting;
        setExpSettingChanged(true);
    }

    private static Object parseTag(XmlResourceParser xmlParser, String tagName) throws XmlPullParserException, IOException {

        int eventType = xmlParser.getEventType();

        switch (tagName) {

            case "item":

                String[] info = new String[6];

                int count = 0;

                while ((eventType != XmlPullParser.END_TAG) || (xmlParser.getName().equals("item"))) {


                    if ((eventType == XmlPullParser.START_TAG) && (xmlParser.getName().equals("item"))){

                        xmlParser.next();
                        info[count]  = xmlParser.getText();
                        count++;
                    }
                    eventType = xmlParser.next();

                }

                return new DateScope(info);
        }

        return null;
    }

    public static long stringToMillionSeconds(String valueStr, String unit) {

        long value = Long.parseLong(valueStr);
        switch (unit) {

            case "年":
                return value * 365 * 24 * 60 * 60 * 1000;

            case "月":
                return value * 30 * 24 * 60 * 60 * 1000;

            case "天":
                return value * 24 * 60 * 60 * 1000;

            default:
                return -1;
        }
    }
    private static String millionSecondsToString(long millionSeconds, String unit) {

        String value = "";
        switch (unit) {

            case "年":
                value = String.valueOf(millionSeconds / (365L * 24 * 60 * 60 * 1000));
                break;

            case "月":
                value = String.valueOf(millionSeconds /  (30L * 24 * 60 * 60 * 1000));
                break;

            case "天":
                value = String.valueOf(millionSeconds / ( 24 * 60 * 60 * 1000));
                break;

        }

        return value;
    }

    public static void actionStart() {
        MyApplication.getCurrentActivityContext().startActivity(new Intent(MyApplication.getCurrentActivityContext(), SettingActivity.class));
    }
}