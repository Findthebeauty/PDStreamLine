package com.shepherdboy.pdstreamline.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;

public class TraversalTimestreamActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traversal_timestream);
    }

    public static void actionStart() {

        Context c = MyApplication.getContext();

        Intent i = new Intent(c, TraversalTimestreamActivity.class);

        c.startActivity(i);
    }

}