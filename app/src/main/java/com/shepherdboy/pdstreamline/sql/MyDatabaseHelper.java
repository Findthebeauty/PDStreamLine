package com.shepherdboy.pdstreamline.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.utils.AIInputter;
import com.shepherdboy.pdstreamline.utils.AscCoordinateComparator;
import com.shepherdboy.pdstreamline.utils.DateUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Objects;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    public static final int ENTIRE_TIMESTREAM = 0;

    public static final int POSSIBLE_PROMOTION_TIMESTREAM = 1;

    public static final int POSSIBLE_EXPIRED_TIMESTREAM = 2;


    public static final String PROMOTION_DATE_SELECTION = "product_promotion_date";

    public static final String EXPIRE_DATE_SELECTION = "product_expired_date";


    public static final String PRODUCT_INFO_TABLE_NAME = "product_inf";

    public static final String FRESH_TIMESTREAM_TABLE_NAME = "fresh_timestream";

    public static final String PROMOTION_TIMESTREAM_TABLE_NAME = "promotion_timestream";

    public static final String POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME = "possible_promotion_timestream";

    public static final String POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME = "possible_expired_timestream";

    public static final String PRODUCT_INFO_COLUMNS = "product_code,product_name," +
            "product_exp,product_exp_time_unit,product_group_number," +
            "product_shelves_indexes,product_last_check_date,product_next_check_date," +
            "product_default_coordinate";

    public static final String FRESH_TIMESTREAM_COLUMNS = "id,product_code,product_dop," +
            "product_promotion_date,product_expire_date,product_coordinate,product_inventory";

    public static final String POSSIBLE_PROMOTION_TIMESTREAM_COLUMNS = "id,product_code,product_dop," +
            "product_promotion_date,product_expire_date,product_coordinate,product_inventory";

    public static final String POSSIBLE_EXPIRED_TIMESTREAM_COLUMNS = "id,product_code,product_dop," +
            "product_promotion_date,product_expire_date,product_coordinate,product_inventory";

    public static final String PROMOTION_TIMESTREAM_COLUMNS = "id,product_code,product_dop," +
            "product_promotion_date,product_expire_date,product_coordinate,product_inventory," +
            "product_discount_rate,sibling_promotion_id";

    public static final String OFF_SHELVES_HISTORY_COLUMNS = "id,product_code,product_dop," +
            "product_promotion_date,product_expire_date,product_coordinate,product_off_shelves_inventory";

    public static final String PROMOTION_HISTORY_COLUMNS = "id,product_code,product_dop," +
            "product_promotion_date,product_expire_date,product_coordinate," +
            "product_exact_promotion_inventory,product_discount_rate," +
            "sibling_promotion_id,product_exact_promotion_date";

    public static final String OBSERVER_COLUMNS = "id,product_code,product_has_fresh_timestream," +
            "product_last_check_date,product_next_check_date,product_dop_interval";

    public static final String DOP_INTERVALS_COLUMNS = "id,product_code,product_dop," +
            "dop_interval,dop_interval_count";

    public static final String CREATE_TABLE_PRODUCT_INF = "create table product_inf(" +
            "product_code text primary key," +
            "product_name text," +
            "product_exp text,product_exp_time_unit text," +
            "product_group_number text," +
            "product_shelves_indexes text," +
            "product_last_check_date," +
            "product_next_check_date," +
            "product_default_coordinate text)";

    public static final String CREATE_TABLE_PRODUCT_TO_CHECK = "create table product_to_check(" +
            "product_code text primary key)";

    public static final String CREATE_TABLE_FRESH_TIMESTREAM = "create table fresh_timestream(" +
            "id text primary key," +
            "product_code text," +
            "product_dop text," +
            "product_promotion_date text," +
            "product_expire_date text," +
            "product_coordinate text," +
            "product_inventory text," +
            "unique(product_code,product_dop))";

    public static final String CREATE_TABLE_POSSIBLE_PROMOTION_TIMESTREAM = "create table possible_promotion_timestream(" +
            "id text primary key," +
            "product_code text not null," +
            "product_dop text," +
            "product_promotion_date text," +
            "product_expire_date text," +
            "product_coordinate text," +
            "product_inventory text," +
            "unique(product_code,product_dop))";

    public static final String CREATE_TABLE_PROMOTION_TIMESTREAM = "create table promotion_timestream(" +
            "id text primary key," +
            "product_code text," +
            "product_dop text," +
            "product_promotion_date text," +
            "product_expire_date text," +
            "product_coordinate text," +
            "product_inventory text," +
            "product_discount_rate text," +
            "sibling_promotion_id text)";

    public static final String CREATE_TABLE_POSSIBLE_EXPIRED_TIMESTREAM = "create table possible_expired_timestream(" +
            "promotion_id text primary key," +
            "product_code text," +
            "product_dop text," +
            "product_promotion_date text," +
            "product_expire_date text," +
            "product_coordinate text," +
            "product_inventory text)";

    public static final String CREATE_TABLE_PROMOTION_HISTORY = "create table promotion_history(" +
            "promotion_id text primary key," +
            "product_code text," +
            "product_dop text," +
            "product_promotion_date text," +
            "product_expire_date text," +
            "product_coordinate text," +
            "product_exact_promotion_inventory text," +
            "product_discount_rate text," +
            "sibling_promotion_id text," +
            "product_exact_promotion_date text)";

    public static final String CREATE_TABLE_OFF_SHELVES_HISTORY = "create table off_shevles_history(" +
            "id text primary key," +
            "product_code text," +
            "product_dop text," +
            "product_promotion_date text," +
            "product_expire_date text," +
            "product_coordinate text," +
            "product_off_shelves_inventory text)";

    public static final String CREATE_TABLE_OBSERVER = "create table observer(" +
            "id text primary key," +
            "product_code text," +
            "product_has_fresh_timestream boolean," +
            "product_last_check_date text," +
            "product_next_check_date text," +
            "product_dop_interval text)";

    public static final String CREATE_TABLE_DOP_INTERVALS = "create table dop_intervals(" +
            "id text primary key," +
            "product_code text," +
            "dop_interval text," +
            "dop_interval_count text," +
            "unique(product_code,dop_interval))";

    public MyDatabaseHelper(Context context, String name,
                            SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_PRODUCT_INF);
        sqLiteDatabase.execSQL(CREATE_TABLE_PRODUCT_TO_CHECK);
        sqLiteDatabase.execSQL(CREATE_TABLE_FRESH_TIMESTREAM);
        sqLiteDatabase.execSQL(CREATE_TABLE_POSSIBLE_PROMOTION_TIMESTREAM);
        sqLiteDatabase.execSQL(CREATE_TABLE_POSSIBLE_EXPIRED_TIMESTREAM);
        sqLiteDatabase.execSQL(CREATE_TABLE_PROMOTION_TIMESTREAM);
        sqLiteDatabase.execSQL(CREATE_TABLE_PROMOTION_HISTORY);
        sqLiteDatabase.execSQL(CREATE_TABLE_OFF_SHELVES_HISTORY);
        sqLiteDatabase.execSQL(CREATE_TABLE_OBSERVER);
        sqLiteDatabase.execSQL(CREATE_TABLE_DOP_INTERVALS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        

    }

    /**
     * 拷贝数据库到sd卡
     */
    public static void copyDataBase(String copyFrom, String copyTo) {

        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return;
        }

        File dataBaseFile = new File(copyFrom);
        File tempDataBaseFile = new File(copyTo);

        FileChannel inChannel = null;
        FileChannel outChannel = null;



        try {
            tempDataBaseFile.createNewFile();
            FileInputStream fileInputStream = new FileInputStream(dataBaseFile);
            FileOutputStream fileOutputStream = new FileOutputStream(tempDataBaseFile);
            inChannel = fileInputStream.getChannel();
            outChannel = fileOutputStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            try {

                if (inChannel != null) {

                    inChannel.close();
                    inChannel = null;

                }

                if (outChannel != null) {

                    outChannel.close();
                    outChannel = null;

                }

            } catch (Exception e) {

                e.printStackTrace();

            }

        }
    }

    /**
     * ???
     */
    public static class PDInfoWrapper {


        public static String getProductName(String productCode, SQLiteDatabase sqLiteDatabase) {

            String name;


            Cursor cursor = query(sqLiteDatabase, PRODUCT_INFO_TABLE_NAME, new String[]{ "*"},
                    "product_code=?", new String[]{ productCode });
            try {

                cursor.moveToFirst();
                name = cursor.getString(cursor.getColumnIndex("product_name"));
            } catch (Exception e) {

                name = "null";
            }

            return name;

        }



        public static Product getProduct(String productCode, SQLiteDatabase sqLiteDatabase, int typeCode) {

            Product product = new Product();

            LinkedHashMap<String, Timestream> timeStreamHashMap = new LinkedHashMap<>();

            sqLiteDatabase.beginTransaction();

            Cursor cursor = query(sqLiteDatabase, PRODUCT_INFO_TABLE_NAME,
                    new String[]{ "*" }, "product_code=?", new String[]{ productCode });

            product.setProductCode(productCode);
            product.setLastCheckDate(DateUtil.typeMach(MyApplication.today));

            if (cursor.getCount() > 0) {

                cursor.moveToNext();

                product.setProductName(cursor.getString(1));
                product.setProductEXP(cursor.getString(2));
                product.setProductEXPTimeUnit(cursor.getString(3));
                product.setUpdated(true);
                product.setNextCheckDate(DateUtil.typeMach(AIInputter.getNextCheckDate(product.getProductEXP(),
                        product.getProductEXPTimeUnit())));

            } else {

                AIInputter.fillTheBlanks(product);

            }

            switch (typeCode) {

                case ENTIRE_TIMESTREAM:

                    getEntireTimestream(product, sqLiteDatabase, timeStreamHashMap);
                    break;
                    
//                case POSSIBLE_EXPIRED_TIMESTREAM:
//
//                    getAndMoveTimestreamBySelection(product, sqLiteDatabase, timeStreamHashMap);
//                    break;
//
//                default:
//                    break;

            }


            cursor.close();
            sqLiteDatabase.setTransactionSuccessful();
            sqLiteDatabase.endTransaction();

            product.setTimeStreams(timeStreamHashMap);

            return product;
        }


        /**
         * @param sqLiteDatabase    sqliteDatabase
         * @param intentCode    POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME，POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME
         * @return timestreams
         */
        public static LinkedList<Timestream> getStaleTimestreams(SQLiteDatabase sqLiteDatabase,
                                                      int intentCode) {

            LinkedList<Timestream> tsLList = new LinkedList<>();

            Cursor cursor;
            Timestream tempTs;
            String tableName = null;

            switch (intentCode) {

                case POSSIBLE_PROMOTION_TIMESTREAM:

                    tableName = POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME;
                    break;


                case POSSIBLE_EXPIRED_TIMESTREAM:

                    tableName = POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME;
                    break;


            }

            cursor = query(sqLiteDatabase, tableName,
                    new String[]{"*"}, null, null);

            if(cursor.moveToFirst()) {

                do {

                    tempTs = new Timestream();
                    tempTs.setId(cursor.getString(0));
                    tempTs.setProductCode(cursor.getString(1));

                    try {

                        tempTs.setProductDOP(DateUtil.typeMach(cursor.getString(2)));
                        tempTs.setProductPromotionDate(DateUtil.typeMach(cursor.getString(3)));
                        tempTs.setProductExpireDate(DateUtil.typeMach(cursor.getString(4)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    tempTs.setProductCoordinate(cursor.getString(5));
                    tempTs.setProductInventory(cursor.getString(6));

                    tempTs.setProductName(getProductName(tempTs.getProductCode(), sqLiteDatabase));

                    tsLList.add(tempTs);
                } while (cursor.moveToNext());
            }

            if (tsLList.size() > 1) {

                Collections.sort(tsLList, AscCoordinateComparator.getInstance());
            }

            return tsLList;
        }

        public static void getAndMoveTimestreamBySelection(SQLiteDatabase sqLiteDatabase,
            String selection, String selectionArg) {

            String sql;
            String deleteSql;

            switch (selection) {

                case PROMOTION_DATE_SELECTION:

                    sql = "replace into " + POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME + " select * from " +
                            FRESH_TIMESTREAM_TABLE_NAME + " where " + selection + "<= '" + selectionArg + "'";
                    deleteSql = "delete from " + FRESH_TIMESTREAM_TABLE_NAME + " where " + selection + "<='" + selectionArg + "'";
                    sqLiteDatabase.execSQL(sql);
                    sqLiteDatabase.execSQL(deleteSql);
//                    sqLiteDatabase.delete(FRESH_TIMESTREAM_TABLE_NAME, selection, new String[]{selectionArg});

                    break;

                case EXPIRE_DATE_SELECTION:

                    sql = "replace into " + POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME + " select * from " +
                            PROMOTION_TIMESTREAM_TABLE_NAME + " where " + selection + "<'" + selectionArg + "'";
                    deleteSql = "delete from " + PROMOTION_TIMESTREAM_TABLE_NAME + " where " + selection + "<'" + selectionArg + "'";
                    sqLiteDatabase.execSQL(sql);
                    sqLiteDatabase.execSQL(deleteSql);
//                    sqLiteDatabase.delete(PROMOTION_TIMESTREAM_TABLE_NAME, selection, new String[]{selectionArg});
                    break;

            }


        }

        private static void getEntireTimestream(Product product, SQLiteDatabase sqLiteDatabase,
            LinkedHashMap<String, Timestream> timestreamHashMap) {

            queryAndGenerateTimestream(product, sqLiteDatabase, timestreamHashMap, FRESH_TIMESTREAM_TABLE_NAME);
            queryAndGenerateTimestream(product, sqLiteDatabase, timestreamHashMap, POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME);
            queryAndGenerateTimestream(product, sqLiteDatabase, timestreamHashMap, PROMOTION_TIMESTREAM_TABLE_NAME);

            if (timestreamHashMap.size() == 0) {

                Timestream temp = new Timestream();
                AIInputter.fillTheBlanks(product, temp);
                timestreamHashMap.put(temp.getId(), temp);

            }

        }

        private static void queryAndGenerateTimestream(Product product, SQLiteDatabase sqLiteDatabase,
                                                       LinkedHashMap<String, Timestream> timestreamHashMap, String tableName) {

            Cursor cursor;
            cursor = query(sqLiteDatabase, tableName,
                    new String[]{ "*" }, "product_code=?", new String[]{ product.getProductCode() });

            boolean isPromoting = Objects.equals(tableName, PROMOTION_TIMESTREAM_TABLE_NAME);

            Timestream temp;

            if (cursor.moveToFirst()) {

                do {

                    temp = new Timestream();
                    temp.setProductCode(product.getProductCode());
                    temp.setId(cursor.getString(0));

                    String productDOP = cursor.getString(2);
                    String productPromotionDate = cursor.getString(3);
                    String productExpireDate = cursor.getString(4);

                    try {
                        temp.setProductDOP(DateUtil.typeMach(productDOP));
                        temp.setProductPromotionDate(DateUtil.typeMach(productPromotionDate));
                        temp.setProductExpireDate(DateUtil.typeMach(productExpireDate));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    temp.setProductCoordinate(cursor.getString(5));
                    temp.setProductInventory(cursor.getString(6));
                    temp.setPromoting(isPromoting);

                    timestreamHashMap.put(temp.getId(), temp);

                } while (cursor.moveToNext());

            }

            cursor.close();
        }

        public static void updateInfo(SQLiteDatabase sqLiteDatabase, Product product) {

            String productCode = product.getProductCode();
            String productName = product.getProductName();
            String productEXP = product.getProductEXP();
            String productEXPTimeUnit = product.getProductEXPTimeUnit();
            String productGroupNumber = product.getProductGroupNumber();
            String productShelvesIndexes = product.getShelvesIndexes();
            String productLastCheckDate = product.getLastCheckDate();
            String productNextCheckDate = product.getNextCheckDate();
            String productDefaultCoordinate = product.getDefaultCoordinate();

            String sql = "insert or replace into " + PRODUCT_INFO_TABLE_NAME + "(" +
                    PRODUCT_INFO_COLUMNS + ") " + "values " + "('" + productCode +
                    "','" + productName + "','" + productEXP + "','" + productEXPTimeUnit +
                    "','" + productGroupNumber + "','" + productShelvesIndexes +
                    "','" + productLastCheckDate + "','" + productNextCheckDate +
                    "','" + productDefaultCoordinate + "')";

            sqLiteDatabase.execSQL(sql);
            product.setUpdated(true);

        }

        public static void updateInfo(SQLiteDatabase sqLiteDatabase, Timestream timeStream) {

            String id = timeStream.getId();
            String productCode = timeStream.getProductCode();
            String productDOP = DateUtil.typeMach(timeStream.getProductDOP());
            String productPromotionDate = DateUtil.typeMach(timeStream.getProductPromotionDate());
            String productExpireDate = DateUtil.typeMach(timeStream.getProductExpireDate());
            String productCoordinate = timeStream.getProductCoordinate();
            String productInventory = timeStream.getProductInventory();

            if ("".equals(productDOP)) {

                deleteProductDOP(sqLiteDatabase, id);
            }


            String sql = "insert or replace into " + FRESH_TIMESTREAM_TABLE_NAME + "(" +
                    FRESH_TIMESTREAM_COLUMNS + ") " + "values " + "('" + id + "','" + productCode +
                    "','" + productDOP + "','" + productPromotionDate + "','" + productExpireDate +
                    "','" + productCoordinate + "','" + productInventory + "')";

            sqLiteDatabase.execSQL(sql);
            timeStream.setUpdated(true);

        }

        public static void deleteProductDOP(SQLiteDatabase sqLiteDatabase, String timeStreamId) {

            String sql1 = "delete from " + FRESH_TIMESTREAM_TABLE_NAME + " where " +
                    "id='" + timeStreamId + "'";
            String sql2 = "delete from " + POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME + " where " +
                    "id='" + timeStreamId + "'";
            String sql3 = "delete from " + PROMOTION_TIMESTREAM_TABLE_NAME + " where " +
                    "id='" + timeStreamId + "'";

            sqLiteDatabase.execSQL(sql1);
            sqLiteDatabase.execSQL(sql2);
            sqLiteDatabase.execSQL(sql3);

        }

        public static Cursor query(SQLiteDatabase sqLiteDatabase, String tableName, String[] columns, String selection,
                             String[] selectionArgs) {

            return sqLiteDatabase.query(tableName, columns,
                    selection, selectionArgs, null,
                    null, null);

        }

    }
}
