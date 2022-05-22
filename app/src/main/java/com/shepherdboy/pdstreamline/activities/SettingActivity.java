package com.shepherdboy.pdstreamline.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;

public class SettingActivity extends AppCompatActivity {

    private static Intent preIntent;
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
                SettingActivity.this.finish();
            }
        });
    }

    public static void actionStart() {
        MyApplication.getContext().startActivity(new Intent(MyApplication.getContext(), SettingActivity.class));
    }
}