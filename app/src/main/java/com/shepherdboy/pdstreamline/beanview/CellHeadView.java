package com.shepherdboy.pdstreamline.beanview;

import static com.shepherdboy.pdstreamline.MyApplication.sqLiteDatabase;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.dao.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;

public class CellHeadView extends LinearLayout implements BeanView{

    private String productCode;
    private TextView headNameTv;
    private TextView headCodeTv;
    private TextView expTv;

    public CellHeadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CellHeadView(int activityIndex, Context context, String productCode) {
        super(context);

        this.setBackground(new ColorDrawable());
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);

        this.setId(View.generateViewId());
        this.setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.cell_head_layout, this,true);
        headNameTv = findViewById(R.id.name);
        headCodeTv = findViewById(R.id.code);
        expTv = findViewById(R.id.exp);

        headNameTv.setId(View.generateViewId());
        headCodeTv.setId(View.generateViewId());
        expTv.setId(View.generateViewId());

        bindData(activityIndex, productCode);
    }

    /**
     *
     * @param o productCode
     */
    public void bindData(int activityIndex, Object o) {

        String productCode = (String) o;

        Product p = PDInfoWrapper.getProduct(productCode,
                sqLiteDatabase, MyDatabaseHelper.ENTIRE_TIMESTREAM);

        headNameTv.setText(PDInfoWrapper.getProductName(productCode,sqLiteDatabase));

        String productEXP = p.getProductEXP() + p.getProductEXPTimeUnit();

        this.productCode = productCode;

        if (productCode.length() > 6)
            productCode = productCode.substring(productCode.length() - 6);

        headCodeTv.setText(productCode);

        expTv.setText(productEXP);
    }

    public String getProductCode() {
        return productCode;
    }
}
