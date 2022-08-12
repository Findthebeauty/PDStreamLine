package com.shepherdboy.pdstreamline.activities;

import static com.shepherdboy.pdstreamline.MyApplication.TRAVERSAL_TIMESTREAM_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.draggableLinearLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.sql.ShelfDAO;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;

import java.util.ArrayList;

public class TraversalTimestreamActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traversal_timestream);

        MyApplication.scrollView = findViewById(R.id.scroll_root);

        initActivity(this);
        
    }



    private void initActivity(Context context) {

        MyApplication.activityIndex = TRAVERSAL_TIMESTREAM_ACTIVITY;
        draggableLinearLayout = findViewById(R.id.parent);
        DraggableLinearLayout.setLayoutChanged(true);

        Button addShelfBt = findViewById(R.id.add_shelf);

        addShelfBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setContentView(R.layout.shelf_info);

                setClassifyList(context);

                Button cancelBt = findViewById(R.id.cancel_bt);
                Button saveBt = findViewById(R.id.save_bt);
                Button addClassify = findViewById(R.id.add_classify_bt);

                cancelBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setContentView(R.layout.activity_traversal_timestream);
                        initActivity(context);
                    }
                });

                saveBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        setContentView(R.layout.activity_traversal_timestream);
                        initActivity(context);
                    }
                });

                addClassify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final EditText editText = new EditText(context);
                        AlertDialog.Builder inputClassify = new AlertDialog.Builder(context);
                        inputClassify.setTitle("请输入类别名").setView(editText);
                        inputClassify.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Toast.makeText(context, editText.getText().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }).show();
                    }
                });
            }
        });


        loadShelf(TraversalTimestreamActivity.this, null);

    }

    private void setClassifyList(Context context) {
        Spinner spinner = findViewById(R.id.classify_sp);
        ArrayList<String> dataList = ShelfDAO.getClassify();
        dataList.add("低温");
        dataList.add("调料");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.item_selected,
                dataList);
        adapter.setDropDownViewResource(R.layout.item_dropdown);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(context, dataList.get(position), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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