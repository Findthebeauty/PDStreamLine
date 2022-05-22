package com.shepherdboy.pdstreamline.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
    }

    public static void actionStart() {

        MyApplication.getContext().startActivity(new Intent(MyApplication.getContext(), SettingActivity.class));
    }
}