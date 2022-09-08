package com.shepherdboy.pdstreamline.dao;

import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.view.MyInfoChangeWatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HttpDao {

    private static final String SERVER_ADDRESS =
            "https://www.shepherdboy.cool/webserver/product/";
    private static final String QUERY_PRODUCT_MAPPING_PREFIX = "queryProduct";
    private static final String SYNC_PRODUCT_MAPPING_PREFIX = "syncProductInfo";

    private static Thread queryThread;

    public static final ConcurrentLinkedQueue<Product> products = new ConcurrentLinkedQueue<>();
    static final Set<Integer> startIndexes = new HashSet<>();
    static final Set<Integer> indexesOnQuery = new HashSet<>();

    private static int totalCount = -1;
    private static boolean transmitEnd = false;
    private static long startTime;


    public static void queryFromServer(String productCode) {

        queryThread = new Thread(new Runnable() {
            @Override
            public void run() {

                Looper.prepare();
                String response = "null";

                String url = SERVER_ADDRESS + QUERY_PRODUCT_MAPPING_PREFIX + "?code=" + productCode;

                try {

                    response = getInfo(url);

                } catch (IOException e){

                    e.printStackTrace();
                }

                Message msg = Message.obtain();
                msg.what = MyInfoChangeWatcher.LAZY_LOAD;

                if (response.equals("null")) {

                    msg.obj = null;
                    MyInfoChangeWatcher.infoHandler.sendMessage(msg);
                    return;
                }

                JSONObject object = JSON.parseObject(response);

                msg.obj = parseProduct(object);
                MyInfoChangeWatcher.infoHandler.sendMessage(msg);

            }
        });

        queryThread.start();
    }

    /**
     * 将JSONObject解析为Product对象
     * @param object JSONObject
     * @return Product
     */
    private static Product parseProduct(JSONObject object) {

        Product p = new Product();
        p.setProductCode(object.getString("barcode"));
        p.setProductName(object.getString("name"));
        p.setProductEXP(object.getString("exp") == null ? "1" : object.getString("exp"));
        p.setProductEXPTimeUnit(object.getString("expTimeUnit") == null ? "天" : object.getString("expTimeUnit"));
        p.setProductSpec(object.getString("unit"));
        p.setUpdated(false);

        return p;
    }

    private static String getInfo(String url) throws IOException {

        HttpURLConnection connection;
        connection = (HttpURLConnection) new URL(url).openConnection();
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

        return response.toString();
    }

    public static void syncProductInfo(String lastSyncTime) {

        startTime = System.currentTimeMillis();

        // 避免数据量过大，分页查询
        final int count = 10000;
        totalCount = -1;
        transmitEnd = false;

        new Thread(new Runnable() {
            @Override
            public void run() {

                limitQuery(0, count, lastSyncTime);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (!(startIndexes.isEmpty() && indexesOnQuery.isEmpty())) {
                    for (int start : startIndexes) {

                        if (indexesOnQuery.contains(start)) continue;
                        limitQuery(start, count, lastSyncTime);
                    }
                }

                transmitEnd = true;
            }
        }).start();

    }

    private static void limitQuery(Integer start, int count, String lastSyncTime) {

        startIndexes.remove(start);
        indexesOnQuery.add(start);
        String url = SERVER_ADDRESS + SYNC_PRODUCT_MAPPING_PREFIX +
                "?start=" + start +
                "&count=" + count +
                "&timestamp=" + lastSyncTime.replace(" ", "%20");


        String response;

        try {

            response = getInfo(url);

            JSONObject object = JSON.parseObject(response);

            JSONObject head = object.getObject("head", JSONObject.class);
            if (head.getString("code").equals("0")) {

                startIndexes.remove(start);
                indexesOnQuery.remove(start);
                return;
            }

            if (totalCount == -1) totalCount = Integer.parseInt(head.getString("totalCount"));

            List<JSONObject> data = object.getObject("data", new TypeReference<List<JSONObject>>(){});

            for (JSONObject productObj : data) {

                Product p = parseProduct(productObj);
                products.add(p);
            }

            startIndexes.remove(start);
            indexesOnQuery.remove(start);
            Log.d("start", start + "");

            while (start + count < totalCount) {

                start += count;
                startIndexes.add(start);
            }

        } catch (IOException e) {

            indexesOnQuery.remove(start);
            e.printStackTrace();
        }
    }

    public static boolean isTransmitEnd() {
        return transmitEnd;
    }

    public static long getStartTime() {
        return startTime;
    }
}