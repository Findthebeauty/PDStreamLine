package com.shepherdboy.pdstreamline.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.shepherdboy.pdstreamline.MyApplication;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityManager.getInstance().addActivity(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        MyApplication.setCurrentActivityContext(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ActivityManager.getInstance().remove(this);
    }
}