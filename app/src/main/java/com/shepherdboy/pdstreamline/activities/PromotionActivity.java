package com.shepherdboy.pdstreamline.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;

public class PromotionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion);
        MyApplication.initActionBar(getSupportActionBar());

        MyApplication.draggableLinearLayout = findViewById(R.id.parent);
        MyApplication.initDatabase(this);
        DraggableLinearLayout.setLayoutChanged(true);

    }

    public static void actionStart() {

        MyApplication.getContext().startActivity(new Intent(MyApplication.getContext(), PromotionActivity.class));
    }
}