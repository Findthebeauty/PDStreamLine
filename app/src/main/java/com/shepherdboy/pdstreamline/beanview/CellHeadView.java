package com.shepherdboy.pdstreamline.beanview;

import static com.shepherdboy.pdstreamline.MyApplication.allProducts;
import static com.shepherdboy.pdstreamline.MyApplication.sqLiteDatabase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.beans.Cell;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.sql.PDInfoWrapper;

public class CellHeadView extends LinearLayout {
    public CellHeadView(Context context, Cell cell) {
        super(context);

        this.setId(View.generateViewId());
        LinearLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout cellHead = inflater.inflate(R.layout.cell_head_layout, null).findViewById(R.id.cell_head);
        TextView headNameTv = cellHead.findViewById(R.id.name);
        TextView headCodeTv = cellHead.findViewById(R.id.code);
        TextView expTv = cellHead.findViewById(R.id.exp);

        cellHead.setId(View.generateViewId());
        headNameTv.setId(View.generateViewId());
        headCodeTv.setId(View.generateViewId());
        expTv.setId(View.generateViewId());

        if (MyApplication.allProducts == null) {
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

        this.addView(cellHead, lp);
    }
}
