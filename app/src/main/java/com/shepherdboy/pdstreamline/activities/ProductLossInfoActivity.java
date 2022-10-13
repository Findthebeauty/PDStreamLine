package com.shepherdboy.pdstreamline.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.beans.ProductLoss;
import com.shepherdboy.pdstreamline.dao.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;

public class ProductLossInfoActivity extends BaseActivity {

    private static String lossId;

    TextView lossProductCodeTV, lossProductNameTV, lossProductDOPTV, lossInventoryTV, lossTypeTV,
        buyProductCodeTV, buyProductNameTV, buyProductDOPTV, processDateTV, accountTV;

    ImageView picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_loss_info);

        lossProductCodeTV = findViewById(R.id.loss_product_code);
        lossProductNameTV = findViewById(R.id.loss_product_name);
        lossProductDOPTV = findViewById(R.id.loss_product_dop);
        lossInventoryTV = findViewById(R.id.loss_product_inventory);
        lossTypeTV = findViewById(R.id.loss_type);
        buyProductCodeTV = findViewById(R.id.buy_product_code);
        buyProductNameTV = findViewById(R.id.buy_product_name);
        buyProductDOPTV = findViewById(R.id.buy_product_dop);
        processDateTV = findViewById(R.id.process_date);
        accountTV = findViewById(R.id.account);
        picture = findViewById(R.id.picture);

        findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ProductLossInfoActivity.this.finish();
            }
        });

    }

    public static void actionStart(Context context, String lossId) {

        ProductLossInfoActivity.lossId = lossId;
        context.startActivity(new Intent(context, ProductLossInfoActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadProductLoss(ProductLossLogActivity.getLossMap().get(lossId));
    }

    private void loadProductLoss(ProductLoss productLoss) {

        Product lossProduct = PDInfoWrapper.getProduct(productLoss.getLossProductCode(),
                MyApplication.sqLiteDatabase, MyDatabaseHelper.PLAIN_PRODUCT_WITH_NO_TIMESTREAM);
        lossProductCodeTV.setText(productLoss.getLossProductCode());
        lossProductNameTV.setText(lossProduct.getProductName());
        lossProductDOPTV.setText(productLoss.getLossProductDOP());
        lossInventoryTV.setText(productLoss.getLossInventory());
        lossTypeTV.setText(productLoss.getLossType());

        if(productLoss.getSiblingProductDOP() != null) {

            Product buyProduct = PDInfoWrapper.getProduct(productLoss.getSiblingProductCode(),
                    MyApplication.sqLiteDatabase, MyDatabaseHelper.PLAIN_PRODUCT_WITH_NO_TIMESTREAM);
            buyProductCodeTV.setText(productLoss.getSiblingProductDOP());
            buyProductNameTV.setText(buyProduct.getProductName());
            buyProductDOPTV.setText(productLoss.getSiblingProductDOP());
        }

        processDateTV.setText(productLoss.getProcessDate());
        accountTV.setText(productLoss.getProcessAccount());

        picture.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher_background));

    }
}