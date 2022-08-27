package com.shepherdboy.pdstreamline.beanview;

import static com.shepherdboy.pdstreamline.MyApplication.allProducts;
import static com.shepherdboy.pdstreamline.MyApplication.sqLiteDatabase;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.beans.Cell;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.sql.PDInfoWrapper;

public class CellHeadView extends LinearLayout implements BeanView{

    private String productCode;

    public CellHeadView(Context context, Cell cell) {
        super(context);

        this.setBackground(new ColorDrawable());

        this.setId(View.generateViewId());
        this.setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.cell_head_layout, this,true);
        TextView headNameTv = findViewById(R.id.name);
        TextView headCodeTv = findViewById(R.id.code);
        TextView expTv = findViewById(R.id.exp);

        headNameTv.setId(View.generateViewId());
        headCodeTv.setId(View.generateViewId());
        expTv.setId(View.generateViewId());

        if (allProducts == null) {
            allProducts = PDInfoWrapper.getAllProduct();
        }

        Product p = allProducts.get(cell.getProductCode());

        headNameTv.setText(PDInfoWrapper.getProductName(cell.getProductCode(),
                sqLiteDatabase));

        String productCode = cell.getProductCode();
        String productEXP = p.getProductEXP() + p.getProductEXPTimeUnit();

        if (productCode.length() > 6)
            productCode = productCode.substring(cell.getProductCode().length() - 6);

        headCodeTv.setText(productCode);

        expTv.setText(productEXP);

        this.productCode = cell.getProductCode();
    }

    public String getProductCode() {
        return productCode;
    }
}
