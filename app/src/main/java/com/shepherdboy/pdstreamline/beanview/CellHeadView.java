package com.shepherdboy.pdstreamline.beanview;

import static com.shepherdboy.pdstreamline.MyApplication.sqLiteDatabase;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.beans.Cell;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;

import java.util.List;

public class CellHeadView extends LinearLayout implements BeanView{

    private String productCode;
    private final TextView headNameTv;
    private final TextView headCodeTv;
    private final TextView expTv;

    public CellHeadView(Context context, Cell cell) {
        super(context);

        this.setBackground(new ColorDrawable());

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

        bindData(cell.getProductCode());
    }

    /**
     *
     * @param o productCode
     */
    public void bindData(Object o) {

        String productCode = (String) o;

        Product p = MyApplication.getAllProducts().get(productCode);

        headNameTv.setText(PDInfoWrapper.getProductName(productCode,sqLiteDatabase));

        String productEXP = p.getProductEXP() + p.getProductEXPTimeUnit();

        if (productCode.length() > 6)
            productCode = productCode.substring(productCode.length() - 6);

        headCodeTv.setText(productCode);

        expTv.setText(productEXP);

        this.productCode = productCode;

        List<BeanView> beanViews = MyApplication.productBeanViewsMap.get(productCode);
        if (beanViews != null && !beanViews.contains(this))
            beanViews.add(this);
    }

    public String getProductCode() {
        return productCode;
    }
}
