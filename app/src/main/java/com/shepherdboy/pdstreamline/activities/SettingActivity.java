package com.shepherdboy.pdstreamline.activities;

import static com.shepherdboy.pdstreamline.MyApplication.settingsMap;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.sql.MyDatabaseHelper;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SettingActivity extends AppCompatActivity {

    private static final String DATE_OFFSET_INDEX = "date_offset";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        MyApplication.initActionBar(getSupportActionBar());

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

    private static void saveDateSetting(List<String[]> dateSetting) {

        String setting = JSON.toJSONString(dateSetting);
        MyDatabaseHelper.saveSetting(DATE_OFFSET_INDEX, setting, MyApplication.sqLiteDatabase);
    }

    public static void initSetting() {

        List<String[]> dateSetting = getDateSetting();

        for (String[] s : dateSetting) {

            settingsMap.put(stringToMillionSeconds(s[0]),s);
        }
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