package com.shepherdboy.pdstreamline.activities;

import static com.shepherdboy.pdstreamline.MyApplication.dateSettingIndex;
import static com.shepherdboy.pdstreamline.MyApplication.draggableLinearLayout;
import static com.shepherdboy.pdstreamline.MyApplication.dateSettingMap;
import static com.shepherdboy.pdstreamline.MyApplication.onShowScopeMap;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
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

    private static final String DATE_OFFSET_INDEX = "date_offset";

    private final TextView[] textViews = new TextView[7];
    private final EditText[] editTexts = new EditText[4];
    private final LinearLayout[] box = new LinearLayout[3];
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


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
                getDateSetting();
                SettingActivity.this.finish();
            }
        });
    }

    private void initDateSettingView(HashMap<Long, String[]> settingsMap) {

        MyApplication.init();
        MyTextWatcher.clearWatchers();
        DraggableLinearLayout.setLayoutChanged(true);

        int scopesCount = draggableLinearLayout.getChildCount() - 1;

        while (scopesCount < settingsMap.size()) {

            addScopeView(draggableLinearLayout);
            scopesCount++;
        }

    }

    private void addScopeView(DraggableLinearLayout rootView) {

        Context context = rootView.getContext();
        LinearLayout childView = new LinearLayout(context);
        rootView.addView(childView, 1 + MyApplication.originalPositionHashMap.size());

        for (int i = 0; i < 7; i++) {

            textViews[i] = new TextView(context);
        }

        for (int i = 0; i < 4; i++) {

            editTexts[i] = new EditText(context);
            editTexts[i].setSelectAllOnFocus(true);
            editTexts[i].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        }

        for (int i = 0; i < 3; i++) {

            box[i] = new LinearLayout(context);
            box[i].setFocusable(true);
            box[i].setFocusableInTouchMode(true);
        }

        box[0].addView(editTexts[0]);
        box[0].addView(textViews[0]);
        box[0].addView(textViews[1]);
        box[0].addView(editTexts[1]);
        box[0].addView(textViews[2]);
        childView.addView(box[0]);

        box[1].addView(textViews[3]);
        box[1].addView(editTexts[2]);
        box[1].addView(textViews[4]);
        childView.addView(box[1]);

        box[2].addView(textViews[5]);
        box[2].addView(editTexts[3]);
        box[2].addView(textViews[6]);
        childView.addView(box[2]);

        decorate(childView);
    }

    private void decorate(LinearLayout childView) {

        childView.setId(DateUtil.getIdByCurrentTime() + MyApplication.timeStreamIndex++);

        WindowManager windowManager = getWindowManager();
        LinearLayout.LayoutParams lParams;
        TextView tv;
        EditText et;

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
        }

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

        String[] scope = dateSettingMap.get(lowerBound);
        String[] scope1 = dateSettingMap.get(upperBound);
        onShowScopeMap.put(t.getId(), scope);

        box[0] = (LinearLayout)(t.getChildAt(0));
        box[1] = (LinearLayout)(t.getChildAt(1));
        box[2] = (LinearLayout)(t.getChildAt(2));
        editTexts[0] = (EditText) box[0].getChildAt(0);
        editTexts[1] = (EditText) box[0].getChildAt(3);
        editTexts[2] = (EditText) box[1].getChildAt(1);
        editTexts[3] = (EditText) box[2].getChildAt(1);
        textViews[0] = (TextView) box[0].getChildAt(1);
        textViews[1] = (TextView) box[0].getChildAt(2);
        textViews[2] = (TextView) box[0].getChildAt(4);
        textViews[3] = (TextView) box[1].getChildAt(0);
        textViews[4] = (TextView) box[1].getChildAt(2);
        textViews[5] = (TextView) box[2].getChildAt(0);
        textViews[6] = (TextView) box[2].getChildAt(2);

        assert scope != null;
        String range = scope[0];
        String pOffset = scope[1];
        String eOffset = scope[2];
        editTexts[0].setText(range.substring(0, range.length() - 1));
        MyTextWatcher.watch(scope, editTexts[0], 0);

        String lUnit = range.substring(range.length() - 1);
        textViews[0].setText(lUnit);
        MyTextWatcher.watch(scope, textViews[0], 0);
        if (scope1 != null) {

            textViews[1].setText("(含)~");
            String range1 = scope1[0];
            editTexts[1].setText(range1.substring(0, range1.length() - 1));
            textViews[2].setText(range1.substring(range1.length() - 1));

        } else {

            textViews[1].setText("(含)以上");
            editTexts[1].setLayoutParams(new LinearLayout.LayoutParams(0,0));
            textViews[2].setLayoutParams(new LinearLayout.LayoutParams(0,0));
        }

        textViews[3].setText(pOffset.substring(0, 1));
        editTexts[2].setText(pOffset.substring(1, pOffset.length() - 1));
        textViews[4].setText(pOffset.substring(pOffset.length() - 1));

        textViews[5].setText(eOffset.substring(0, 1));
        editTexts[3].setText(eOffset.substring(1, eOffset.length() - 1));
        textViews[6].setText(eOffset.substring(eOffset.length() - 1));
    }

    private static void saveDateSetting(List<String[]> dateSetting) {

        String setting = JSON.toJSONString(dateSetting);
        MyDatabaseHelper.saveSetting(DATE_OFFSET_INDEX, setting, MyApplication.sqLiteDatabase);
    }

    public static void initSetting() {

        List<String[]> dateSetting = getDateSetting();

        for (String[] s : dateSetting) {

            dateSettingMap.put(stringToMillionSeconds(s[0]),s);
        }

        dateSettingIndex = new ArrayList(dateSettingMap.keySet());
        Collections.sort(dateSettingIndex,Collections.reverseOrder());
    }

    private static List<String[]> getDateSetting() {

        String setting = MyDatabaseHelper.getSetting(DATE_OFFSET_INDEX, MyApplication.sqLiteDatabase);
        List<String[]> dateSetting;

        if (setting != null) {

            dateSetting = JSON.parseObject(setting, new TypeReference<List<String[]>>(){});

        } else {

            dateSetting = getDefaultDateSetting();
            saveDateSetting(dateSetting);
        }

        return dateSetting;
    }

    private static List<String[]> getDefaultDateSetting() {

        List<String[]> setting = new ArrayList<>();
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

                    String[] scope = parseTag(xmlResourceParser);
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

    private static String[] parseTag(XmlResourceParser xmlParser) throws XmlPullParserException, IOException {

        int eventType = xmlParser.getEventType();
        String[] scope = new String[3];

        int count = 0;

        while ((eventType != XmlPullParser.END_TAG) || (xmlParser.getName().equals("item"))) {


            if ((eventType == XmlPullParser.START_TAG) && (xmlParser.getName().equals("item"))){

                xmlParser.next();
                scope[count]  = xmlParser.getText();
                count++;
            }
            eventType = xmlParser.next();

        }

        return scope;
    }

    private static long stringToMillionSeconds(String text) {

        long value = Long.parseLong(text.substring(0, text.length() - 1));
        String unit = text.substring(text.length() - 1);

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

    public static void actionStart() {
        MyApplication.getContext().startActivity(new Intent(MyApplication.getContext(), SettingActivity.class));
    }
}