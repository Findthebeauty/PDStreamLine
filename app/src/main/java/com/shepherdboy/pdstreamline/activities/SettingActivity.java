package com.shepherdboy.pdstreamline.activities;

import static com.shepherdboy.pdstreamline.MyApplication.dateSettingIndex;
import static com.shepherdboy.pdstreamline.MyApplication.dateSettingMap;
import static com.shepherdboy.pdstreamline.MyApplication.draggableLinearLayout;
import static com.shepherdboy.pdstreamline.MyApplication.mlScopeMap;
import static com.shepherdboy.pdstreamline.MyApplication.onShowScopeMap;

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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.beans.DateScope;
import com.shepherdboy.pdstreamline.sql.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.utils.DateUtil;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;
import com.shepherdboy.pdstreamline.view.MyTextWatcher;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *
 */
public class SettingActivity extends AppCompatActivity {

    private static final int ADD_SCOPE = 1;
    private static final int DELETE_SCOPE = 2;

    private static final String DATE_OFFSET_INDEX = "date_offset";

    private final TextView[] textViews = new TextView[5];
    private final EditText[] editTexts = new EditText[3];
    private final LinearLayout[] box = new LinearLayout[3];

    private static final HashMap<String, TextView> upperBoundMap = new HashMap<>();

    private static boolean dateSettingChanged = false;

    private static SettingActivity instance;

    public static void onScopeViewPositionChanged(View changedView, float horizontalDistance) {

        int viewState = getViewState(changedView, horizontalDistance);

        switch (viewState) {

            case ADD_SCOPE:

                changedView.setBackgroundColor(Color.parseColor("#8BC34A"));
                break;

            case DELETE_SCOPE:

                changedView.setBackgroundColor(Color.parseColor("#FF0000"));
                break;

            default:

                changedView.setBackgroundColor(0);
        }
    }


    /** 根据左右拖动的距离返回int值*/
    public static int getViewState(View draggedView, double horizontalDraggedDistance) {

        boolean isDragToADD = horizontalDraggedDistance > 0 && horizontalDraggedDistance >= draggedView.getHeight() * 1.6;
        boolean isDragToDelete = horizontalDraggedDistance < 0 && -horizontalDraggedDistance >= draggedView.getHeight() * 1.6;

        if (isDragToADD) {

            return ADD_SCOPE;
        } else if (isDragToDelete) {

            return DELETE_SCOPE;
        } else {
            return 0;
        }
    }

    public static void onScopeViewReleased(View releasedChild, float horizontalDistance) {

        int stateCode = getViewState(releasedChild, horizontalDistance);

        switch (stateCode) {

            case ADD_SCOPE:

                addScopeBeyond(releasedChild);
                break;

        }

    }

    private static void addScopeBeyond(View releasedChild) {

        LinearLayout parent = (LinearLayout) releasedChild.getParent();
        int addIndex = 1;
        for (int i = 1; i < parent.getChildCount(); i++) {

            LinearLayout child = (LinearLayout) parent.getChildAt(i);
            if (child.getId() == releasedChild.getId()) {

                addIndex = i;
                break;
            }
        }

        MyTextWatcher.setShouldWatch(false);

        LinearLayout newScopeView = getInstance().addScopeView(parent, addIndex);
        DateScope scope = generateScope(onShowScopeMap.get(releasedChild.getId()), addIndex);

        if (addIndex == 1) {

            TextView preTopUpperBoundTextView = (TextView) ((LinearLayout)((LinearLayout)(parent.getChildAt(2))).getChildAt(0)).getChildAt(3);
            TextView preTopConnectorTextView = (TextView) ((LinearLayout)((LinearLayout)(parent.getChildAt(2))).getChildAt(0)).getChildAt(2);
            preTopConnectorTextView.setText("(含)~");
            String range = scope.getRange();
            preTopUpperBoundTextView.setText(range);
            upperBoundMap.put(range, preTopUpperBoundTextView);
        }

        long upperBound = getUpperBound(addIndex);
        long lowerBound = stringToMillionSeconds(scope.getRangeValue(), scope.getRangeUnit());

        dateSettingMap.put(scope.getScopeId(),scope);

        initScopeIndex();
        getInstance().loadScope(lowerBound, upperBound, newScopeView);

        EditText lEditText = ((EditText)((LinearLayout)(newScopeView.getChildAt(0))).getChildAt(0));

        MyTextWatcher.setShouldWatch(true);
        DraggableLinearLayout.setFocus(lEditText);
    }

    private static long getUpperBound(int addIndex) {

        long upperBound = 0;
        if (addIndex > 1) upperBound = (long) dateSettingIndex.get(addIndex - 1);

        return upperBound;
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

        if (isDateSettingChanged()) {
            saveDateSetting(new ArrayList(dateSettingMap.values()));
        }
        super.onStop();
    }

    public static SettingActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        instance = SettingActivity.this;

        MyApplication.initActionBar(getSupportActionBar());
        MyApplication.init();
        MyApplication.initDatabase(this);
        draggableLinearLayout = findViewById(R.id.date_offset_setting_table);

        initDateSettingView(dateSettingMap);

        loadDateSetting();

        TextView textView = getSupportActionBar().getCustomView().findViewById(R.id.setting);

        textView.setText("应用");
        textView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isDateSettingChanged()) {
                    saveDateSetting(new ArrayList(dateSettingMap.values()));
                }
                SettingActivity.this.finish();
            }
        });
    }

    private void initDateSettingView(HashMap<String, DateScope> settingsMap) {

        MyApplication.init();
        MyTextWatcher.clearWatchers();
        DraggableLinearLayout.setLayoutChanged(true);

        int scopesCount = draggableLinearLayout.getChildCount() - 1;

        while (scopesCount < settingsMap.size()) {

            addScopeView(draggableLinearLayout, null);
            scopesCount++;
        }

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

        childView.setId(DateUtil.getIdByCurrentTime() + MyApplication.timeStreamIndex++);

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


    public static void synchronizeUpperBound(String preRange, DateScope scope) {

        String range = scope.getRange();
        TextView t = upperBoundMap.get(preRange);
        t.setText(range);
        upperBoundMap.remove(preRange);
        upperBoundMap.put(range, t);
    }

    private void loadDateSetting() {

        MyTextWatcher.setShouldWatch(false);

        long upperBound = 0;
        long lowerBound = 0;
        for (int i = 0; i < dateSettingIndex.size(); i++) {

            upperBound = lowerBound;
            lowerBound = (long) dateSettingIndex.get(i);
            loadScope(lowerBound, upperBound, (LinearLayout) draggableLinearLayout.getChildAt(i + 1));
        }

        DraggableLinearLayout.setLayoutChanged(true);
        MyTextWatcher.setShouldWatch(true);

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

        assert scope != null;
        String rangeValue = scope.getRangeValue();
        String rangeUnit = scope.getRangeUnit();
        String pOffsetValue = scope.getPromotionOffsetValue();
        String pOffsetUnit = scope.getPromotionOffsetUnit();
        String eOffsetValue = scope.getExpireOffsetValue();
        String eOffsetUnit = scope.getExpireOffsetUnit();
        editTexts[0].setText(rangeValue);
        MyTextWatcher.watch(scope, editTexts[0], DateScope.RANGE_VALUE);

        textViews[0].setText(rangeUnit);
        MyTextWatcher.watch(scope, textViews[0], DateScope.RANGE_UNIT);
        if (scope1 != null) {

            textViews[1].setText("(含)~");
            String range1 = scope1.getRange();
            textViews[2].setText(range1);
            upperBoundMap.put(range1, textViews[2]);

        } else {

            textViews[1].setText("及");
            textViews[2].setText("以上");
        }

        editTexts[1].setText(pOffsetValue);
        MyTextWatcher.watch(scope, editTexts[1], DateScope.PROMOTION_OFFSET_VALUE);
        textViews[3].setText(pOffsetUnit);
        MyTextWatcher.watch(scope, textViews[3], DateScope.PROMOTION_OFFSET_UNIT);

        editTexts[2].setText(eOffsetValue);
        MyTextWatcher.watch(scope, editTexts[2], DateScope.EXPIRE_OFFSET_VALUE);
        textViews[4].setText(eOffsetUnit);
        MyTextWatcher.watch(scope, textViews[4], DateScope.EXPIRE_OFFSET_UNIT);
    }

    public static boolean isDateSettingChanged() {
        return dateSettingChanged;
    }

    public static void setDateSettingChanged(boolean dateSettingChanged) {
        SettingActivity.dateSettingChanged = dateSettingChanged;
    }

    @Override
    protected void onPause() {

        if (isDateSettingChanged()) {
            saveDateSetting(new ArrayList(dateSettingMap.values()));
        }
        super.onPause();
    }

    private static void saveDateSetting(List<DateScope> dateSetting) {

        String setting = JSON.toJSONString(dateSetting);
        MyDatabaseHelper.saveSetting(DATE_OFFSET_INDEX, setting, MyApplication.sqLiteDatabase);
        setDateSettingChanged(true);
    }

    public static void initSetting() {

        List<DateScope> dateSetting = getDateSetting();

        for (DateScope s : dateSetting) {

            dateSettingMap.put(s.getScopeId(),s);
        }

        initScopeIndex();
    }

    private static void initScopeIndex() {

        if (dateSettingIndex != null) {
            dateSettingIndex.clear();
            dateSettingIndex = null;
        }

        dateSettingIndex = new ArrayList<Long>();
        for (String s : dateSettingMap.keySet()) {

            DateScope d = dateSettingMap.get(s);
            long mls = stringToMillionSeconds(d.getRangeValue(), d.getRangeUnit());
            dateSettingIndex.add(mls);
            mlScopeMap.put(mls, d);
        }
        Collections.sort(dateSettingIndex,Collections.reverseOrder());
    }

    private static List<DateScope> getDateSetting() {

        String setting = MyDatabaseHelper.getSetting(DATE_OFFSET_INDEX, MyApplication.sqLiteDatabase);
        List<DateScope> dateSetting;

        if (setting != null) {

            dateSetting = JSON.parseObject(setting, new TypeReference<List<DateScope>>(){});

        } else {

            dateSetting = getDefaultDateSetting();
            saveDateSetting(dateSetting);
        }

        return dateSetting;
    }

    private static List<DateScope> getDefaultDateSetting() {

        List<DateScope> setting = new ArrayList<>();
        String packageName = MyApplication.getContext().getPackageName();
        Resources resources = null;

        try {
            resources = MyApplication.getContext().getPackageManager().getResourcesForApplication(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        int resId = resources.getIdentifier("default_date_management_setting", "xml", packageName);
        XmlResourceParser xmlResourceParser = resources.getXml(resId);

        try {

            int eventType = xmlResourceParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if ((eventType == XmlPullParser.START_TAG) && (xmlResourceParser.getName().equals("scope"))){

                    DateScope scope = parseTag(xmlResourceParser);
                    setting.add(scope);
                }

                eventType = xmlResourceParser.next();
            }

        } catch (XmlPullParserException | IOException e) {

            e.printStackTrace();

        } finally {
            xmlResourceParser.close();
        }

        return setting;
    }

    private static DateScope parseTag(XmlResourceParser xmlParser) throws XmlPullParserException, IOException {

        int eventType = xmlParser.getEventType();
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

        DateScope scope = new DateScope(info);

        return scope;
    }

    private static long stringToMillionSeconds(String valueStr, String unit) {

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
        MyApplication.getContext().startActivity(new Intent(MyApplication.getContext(), SettingActivity.class));
    }
}