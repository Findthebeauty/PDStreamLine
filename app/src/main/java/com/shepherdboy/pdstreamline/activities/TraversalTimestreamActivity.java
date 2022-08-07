package com.shepherdboy.pdstreamline.activities;

import static com.shepherdboy.pdstreamline.MyApplication.TRAVERSAL_TIMESTREAM_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.draggableLinearLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;

public class TraversalTimestreamActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traversal_timestream);
        
        initActivity();
        
    }
    private void initActivity() {

        MyApplication.activityIndex = TRAVERSAL_TIMESTREAM_ACTIVITY;
        draggableLinearLayout = findViewById(R.id.parent);
        DraggableLinearLayout.setLayoutChanged(true);

        loadShelf(TraversalTimestreamActivity.this, null);

    }


    public static void loadShelf(Context context, String shelfId) {

        LayoutInflater inflater = LayoutInflater.from(context);

        LinearLayout parent = ((AppCompatActivity)context).findViewById(R.id.parent);

        LinearLayout cellHead = inflater.inflate(R.layout.cell_head_layout, null).findViewById(R.id.cell_head);
        LinearLayout combination = inflater.inflate(R.layout.comb_layout, null).findViewById(R.id.combination);

        parent.addView(cellHead);
        parent.addView(combination);
    }


    public static void actionStart() {

        Context c = MyApplication.getContext();

        Intent i = new Intent(c, TraversalTimestreamActivity.class);

        c.startActivity(i);
    }

}