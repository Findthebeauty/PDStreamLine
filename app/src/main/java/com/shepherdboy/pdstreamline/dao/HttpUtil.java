package com.shepherdboy.pdstreamline.dao;

import android.os.Looper;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.view.MyInfoChangeWatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {

    private static final String SERVER_ADDRESS =
            "https://www.shepherdboy.cool/webserver/product/queryProduct?code=";

    private static Thread queryThread;

    public static void queryFromServer(String productCode) {

        queryThread = new Thread(new Runnable() {
            @Override
            public void run() {

                Looper.prepare();
                URL url = null;
                HttpURLConnection connection = null;

                try {

                    url = new URL(SERVER_ADDRESS + productCode);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(3000);
                    connection.setReadTimeout(3000);
//                    connection.addRequestProperty("ACCEPT","application/json");

                    InputStream in = connection.getInputStream();
                    InputStreamReader reader = new InputStreamReader(in);
                    BufferedReader buffer = new BufferedReader(reader);
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = buffer.readLine()) != null) {
                        response.append(line);
                    }

                    Message msg = Message.obtain();
                    msg.what = MyInfoChangeWatcher.LAZY_LOAD;

                    if (response.toString().equals("null")) {

                        msg.obj = null;
                        MyInfoChangeWatcher.infoHandler.sendMessage(msg);
                        return;
                    }

                    JSONObject object = JSON.parseObject(response.toString());

                    Product p = new Product();
                    p.setProductCode(productCode);
                    p.setProductName(object.getString("name"));
                    p.setProductEXP(object.getString("exp") == null ? "1" : object.getString("exp"));
                    p.setProductEXPTimeUnit(object.getString("expTimeUnit") == null ? "天" : object.getString("expTimeUnit"));
                    p.setProductSpec(object.getString("unit"));
                    p.setUpdated(false);
                    msg.obj = p;
                    MyInfoChangeWatcher.infoHandler.sendMessage(msg);

                } catch (IOException e){

                    e.printStackTrace();
                }
            }
        });

        queryThread.start();
    }
}
