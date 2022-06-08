package com.shepherdboy.pdstreamline.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.beans.TimestreamCombination;
import com.shepherdboy.pdstreamline.services.MidnightTimestreamManagerService;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;

import java.util.LinkedList;

public class PromotionActivity extends AppCompatActivity {

    private LinkedList<Timestream> oddments;
    private LinkedList<TimestreamCombination> combinations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion);
        MyApplication.initActionBar(getSupportActionBar());

        MyApplication.initDatabase(this);

        initActivity();

    }

    private void initActivity() {

        MyApplication.draggableLinearLayout = findViewById(R.id.parent);
        DraggableLinearLayout.setLayoutChanged(true);
        oddments = new LinkedList<>();
        combinations = new LinkedList<>();

        if (SettingActivity.settingInstance.isAutoCombine())
            autoCombine(MidnightTimestreamManagerService.basket);


    }

    public void autoCombine(LinkedList<Timestream> basket) {

        for (Timestream t : basket) {

            combinations.add(combine(t));
        }
    }

    //todo oddments的搭赠方法
    //todo 获取sibling列表

    /**
     * 将该批次商品自身进行买一赠一，如果有单数，将单出的一个放入oddments列表中由人工选择搭赠方式
     * @param t 需要做买一赠一的商品
     * @return TimestreamCombination捆绑好的商品
     */
    private TimestreamCombination combine(Timestream t) {

        int inventory = Integer.parseInt(t.getProductInventory());

        t.setSiblingPromotionId(t.getId());
        t.setBuySpecs("1");
        t.setGiveawaySpecs("1");
        t.setDiscountRate("0.5");
        t.setInBasket(false);

        if (inventory % 2 == 1) {

            inventory -= 1;
            t.setProductInventory(String.valueOf(inventory));
            oddments.add(new Timestream(t));
        }

        return new TimestreamCombination(t);
    }


    public static void actionStart() {

        MyApplication.getContext().startActivity(new Intent(MyApplication.getContext(), PromotionActivity.class));
    }
}