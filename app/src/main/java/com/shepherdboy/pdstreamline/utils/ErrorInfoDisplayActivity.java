package com.shepherdboy.pdstreamline.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;

public class ErrorInfoDisplayActivity extends AppCompatActivity {

    public static void actionStart(Context context, String errorLog) {

        Intent intent = new Intent(context, ErrorInfoDisplayActivity.class);
        intent.putExtra("error_log", errorLog);
        context.startActivity(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_info_display);
        MyApplication.initActionBar(getSupportActionBar());

        Intent intent = getIntent();
        String errorLog = intent.getStringExtra("error_log");

        TextView textView = findViewById(R.id.error_info);
        textView.setText(errorLog);
    }
}