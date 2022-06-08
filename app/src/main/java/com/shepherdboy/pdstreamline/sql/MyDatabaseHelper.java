package com.shepherdboy.pdstreamline.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.activities.SettingActivity;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    public static final int PLAIN_PRODUCT_WITH_NO_TIMESTREAM = -1;

    public static final int ENTIRE_TIMESTREAM = 0;

    public static final int POSSIBLE_PROMOTION_TIMESTREAM = 1;

    public static final int POSSIBLE_EXPIRED_TIMESTREAM = 2;

    public static final int PROMOTION_TIMESTREAM = 3;

    public static final int OFF_SHELVES_HISTORY= 4;

    public static final int NEW_TIMESTREAM = 5;

    public static final int UPDATE_BASKET = 6;

    public static final String PROMOTION_DATE_SELECTION = "product_promotion_date";

    public static final String EXPIRE_DATE_SELECTION = "product_expire_date";

    public static final String IN_BASKET_SELECTION = "in_basket";

    public static final String PRODUCT_INFO_TABLE_NAME = "product_inf";

    public static final String FRESH_TIMESTREAM_TABLE_NAME = "fresh_timestream";

    public static final String PROMOTION_TIMESTREAM_TABLE_NAME = "promotion_timestream";

    public static final String POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME = "possible_promotion_timestream";

    public static final String OFF_SHELVES_HISTORY_TABLE_NAME = "off_shelves_history";

    public static final String POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME = "possible_expired_timestream";

    public static final String SETTING_TABLE_NAME = "manage_setting";

    public static final String PRODUCT_INFO_COLUMNS = "product_code,product_name," +
            "product_exp,product_exp_time_unit,product_group_number," +
            "product_shelves_indexes,product_last_check_date,product_next_check_date," +
            "product_default_coordinate";

    public static final String FRESH_TIMESTREAM_COLUMNS = "id,product_code,product_dop," +
            "product_promotion_date,product_expire_date,product_coordinate,product_inventory";

    public static final String POSSIBLE_PROMOTION_TIMESTREAM_COLUMNS = "id,product_code,product_dop," +
            "product_promotion_date,product_expire_date,product_coordinate,product_inventory";

    public static final String POSSIBLE_EXPIRED_TIMESTREAM_COLUMNS = "id,product_code,product_dop," +
            "product_promotion_date,product_expire_date,product_coordinate,product_inventory," +
            "product_discount_rate,product_buy_specs,product_giveaway_specs,sibling_promotion_id";

    public static final String PROMOTION_TIMESTREAM_COLUMNS = "id,product_code,product_dop," +
            "product_promotion_date,product_expire_date,product_coordinate,product_inventory," +
            "product_discount_rate,product_buy_specs,product_giveaway_specs,sibling_promotion_id";

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

    public static final String SETTING_COLUMNS = "id,setting_index,setting_value";

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
            "product_inventory text)";

    public static final String CREATE_TABLE_POSSIBLE_PROMOTION_TIMESTREAM = "create table possible_promotion_timestream(" +
            "id text primary key," +
            "product_code text not null," +
            "product_dop text," +
            "product_promotion_date text," +
            "product_expire_date text," +
            "product_coordinate text," +
            "product_inventory text," +
            "in_basket text default 'false')";

    public static final String CREATE_TABLE_PROMOTION_TIMESTREAM = "create table promotion_timestream(" +
            "id text primary key," +
            "product_code text," +
            "product_dop text," +
            "product_promotion_date text," +
            "product_expire_date text," +
            "product_coordinate text," +
            "product_inventory text," +
            "product_discount_rate text," +
            "product_buy_specs text," +
            "product_giveaway_specs text," +
            "sibling_promotion_id text)";

    public static final String CREATE_TABLE_POSSIBLE_EXPIRED_TIMESTREAM = "create table possible_expired_timestream(" +
            "id text primary key," +
            "product_code text," +
            "product_dop text," +
            "product_promotion_date text," +
            "product_expire_date text," +
            "product_coordinate text," +
            "product_inventory text," +
            "product_discount_rate text," +
            "product_buy_specs text," +
            "product_giveaway_specs text," +
            "sibling_promotion_id text)";

    public static final String CREATE_TABLE_PROMOTION_HISTORY = "create table promotion_history(" +
            "promotion_id text primary key," +
            "product_code text," +
            "product_dop text," +
            "product_promotion_date text," +
            "product_expire_date text," +
            "product_coordinate text," +
            "product_inventory text," +
            "product_discount_rate text," +
            "sibling_promotion_id text)";

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

    public static final String CREATE_TABLE_SETTING = "create table " + SETTING_TABLE_NAME + "(" +
            "id integer primary key autoincrement," +
            "setting_index text unique," +
            "setting_value text)";

    public MyDatabaseHelper(Context context, String name,
                            SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static Cursor query(SQLiteDatabase sqLiteDatabase, String tableName, String[] columns, String selection,
                           String[] selectionArgs) {

        return sqLiteDatabase.query(tableName, columns,
                selection, selectionArgs, null,
                null, null);

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
        sqLiteDatabase.execSQL(CREATE_TABLE_SETTING);
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
    public static void saveSetting(String index, String value, SQLiteDatabase sqLiteDatabase) {

        String sql = "insert or replace into " + SETTING_TABLE_NAME + "(" +
                SETTING_COLUMNS + ")" + "values (" + null + ",'" + index + "','" + value
                +"')";

        sqLiteDatabase.execSQL(sql);
    }

    public static String getSetting(String index, SQLiteDatabase sqLiteDatabase) {

        String value = null;

        Cursor cursor = query(sqLiteDatabase, SETTING_TABLE_NAME, new String[]{"*"},
                "setting_index=?", new String[]{index});

        try {

            if (!cursor.moveToFirst()) return null;

            value = cursor.getString(cursor.getColumnIndex("setting_value"));

        } catch (Exception e) {

            e.printStackTrace();
        }

        return value;
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

                case PLAIN_PRODUCT_WITH_NO_TIMESTREAM:
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

                    switch (intentCode) {

                        case POSSIBLE_PROMOTION_TIMESTREAM:

                            tempTs.setInBasket(Boolean.parseBoolean(cursor.getString(7)));
                            break;

                        case POSSIBLE_EXPIRED_TIMESTREAM:

                            tempTs.setDiscountRate(cursor.getString(7));
                            tempTs.setBuySpecs(cursor.getString(8));
                            tempTs.setGiveawaySpecs(cursor.getString(9));
                            tempTs.setSiblingPromotionId(cursor.getString(10));
                            break;
                    }

                    tsLList.add(tempTs);
                } while (cursor.moveToNext());
            }

            if (tsLList.size() > 1) {

                Collections.sort(tsLList, AscCoordinateComparator.getInstance());
            }

            return tsLList;
        }

        /**
         * @param sqLiteDatabase
         * @param date 当前日期
         */
        public static void getAndMoveTimestreamByDate(SQLiteDatabase sqLiteDatabase, String date) {

            String sql;
            String deleteSql;

            sql = "replace into " + POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME + "(" +
                    POSSIBLE_PROMOTION_TIMESTREAM_COLUMNS + ") select " +
                    POSSIBLE_PROMOTION_TIMESTREAM_COLUMNS +" from " +
                    FRESH_TIMESTREAM_TABLE_NAME + " where " + PROMOTION_DATE_SELECTION + "<= '" + date + "'";
            deleteSql = "delete from " + FRESH_TIMESTREAM_TABLE_NAME + " where " + PROMOTION_DATE_SELECTION + "<='" + date + "'";
            sqLiteDatabase.execSQL(sql);
            sqLiteDatabase.execSQL(deleteSql);

            sql = "replace into " + POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME + "(" +
                    POSSIBLE_PROMOTION_TIMESTREAM_COLUMNS + ") select " +
                    POSSIBLE_PROMOTION_TIMESTREAM_COLUMNS +" from " +
                    POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME + " where " + EXPIRE_DATE_SELECTION + "<'" + date + "'";
            deleteSql = "delete from " + POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME + " where " + EXPIRE_DATE_SELECTION + "<'" + date + "'";
            sqLiteDatabase.execSQL(sql);
            sqLiteDatabase.execSQL(deleteSql);

            sql = "replace into " + POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME + " select " +
                    POSSIBLE_EXPIRED_TIMESTREAM_COLUMNS +" from " +
                    PROMOTION_TIMESTREAM_TABLE_NAME + " where " + EXPIRE_DATE_SELECTION + "<'" + date + "'";
            deleteSql = "delete from " + PROMOTION_TIMESTREAM_TABLE_NAME + " where " + EXPIRE_DATE_SELECTION + "<'" + date + "'";
            sqLiteDatabase.execSQL(sql);
            sqLiteDatabase.execSQL(deleteSql);

        }

        private static void getEntireTimestream(Product product, SQLiteDatabase sqLiteDatabase,
            LinkedHashMap<String, Timestream> timestreamHashMap) {

            queryAndGenerateTimestream(product, sqLiteDatabase, timestreamHashMap, FRESH_TIMESTREAM_TABLE_NAME);
            queryAndGenerateTimestream(product, sqLiteDatabase, timestreamHashMap, POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME);
            queryAndGenerateTimestream(product, sqLiteDatabase, timestreamHashMap, PROMOTION_TIMESTREAM_TABLE_NAME);
            queryAndGenerateTimestream(product, sqLiteDatabase, timestreamHashMap, POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME);

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

            boolean isPromoting = Objects.equals(tableName, PROMOTION_TIMESTREAM_TABLE_NAME) ||
                    Objects.equals(tableName, POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME);

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

                    if (tableName.equals(POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME)) {

                        temp.setInBasket(Boolean.parseBoolean(cursor.getString(7)));
                    }

                    temp.setUpdated(true);
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
                    PRODUCT_INFO_COLUMNS + ") " + "values ('" + productCode +
                    "','" + productName + "','" + productEXP + "','" + productEXPTimeUnit +
                    "','" + productGroupNumber + "','" + productShelvesIndexes +
                    "','" + productLastCheckDate + "','" + productNextCheckDate +
                    "','" + productDefaultCoordinate + "')";

            sqLiteDatabase.execSQL(sql);
            product.setUpdated(true);

        }

        public static void updateInfo(SQLiteDatabase sqLiteDatabase, Timestream timestream, int transactionCode) {

            String id = timestream.getId();
            String productCode = timestream.getProductCode();
            String productDOP = DateUtil.typeMach(timestream.getProductDOP());
            String productPromotionDate = DateUtil.typeMach(timestream.getProductPromotionDate());
            String productExpireDate = DateUtil.typeMach(timestream.getProductExpireDate());
            String productCoordinate = timestream.getProductCoordinate();
            String productInventory = timestream.getProductInventory();
            String discountRate = timestream.getDiscountRate();
            String siblingPromotionId = timestream.getSiblingPromotionId();
            String inBasket = String.valueOf(timestream.isInBasket());

            String sql = null;

            switch (transactionCode) {

                case NEW_TIMESTREAM:

                    sql = "insert or replace into " + FRESH_TIMESTREAM_TABLE_NAME + "(" +
                            FRESH_TIMESTREAM_COLUMNS + ") " + "values " + "('" + id + "','" + productCode +
                            "','" + productDOP + "','" + productPromotionDate + "','" + productExpireDate +
                            "','" + productCoordinate + "','" + productInventory + "')";
                    break;

                case PROMOTION_TIMESTREAM:

                    sql = "insert or replace into " + PROMOTION_TIMESTREAM_TABLE_NAME + "(" +
                            PROMOTION_TIMESTREAM_COLUMNS + ") " + "values " + "('" + id + "','" + productCode +
                            "','" + productDOP + "','" + productPromotionDate + "','" + productExpireDate +
                            "','" + productCoordinate + "','" + productInventory + "','" + discountRate +
                            "','" + siblingPromotionId + "')";
                    break;

                case OFF_SHELVES_HISTORY:

                    sql = "insert into " + OFF_SHELVES_HISTORY_TABLE_NAME + "(" +
                            OFF_SHELVES_HISTORY_COLUMNS + ") " + "values " + "('" + id + "','" + productCode +
                            "','" + productDOP + "','" + productPromotionDate + "','" + productExpireDate +
                            "','" + productCoordinate + "','" + productInventory + "')";
                    break;

                case UPDATE_BASKET:

                    sql = "update " + POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME + " set " + IN_BASKET_SELECTION +
                            "='" + inBasket + "' where id='" + id +"'";
                    break;

                default:
                    break;

            }


            if (sql != null) {

                sqLiteDatabase.execSQL(sql);
                timestream.setUpdated(true);
            }
        }

        public static void updateInfo(SQLiteDatabase sqLiteDatabase, Timestream timestream, String tableName) {

            String id = timestream.getId();
            String productCode = timestream.getProductCode();
            String productDOP = DateUtil.typeMach(timestream.getProductDOP());
            String productPromotionDate = DateUtil.typeMach(timestream.getProductPromotionDate());
            String productExpireDate = DateUtil.typeMach(timestream.getProductExpireDate());
            String productCoordinate = timestream.getProductCoordinate();
            String productInventory = timestream.getProductInventory();
            String productDiscountRate = timestream.getDiscountRate();
            String productBuySpecs = timestream.getBuySpecs();
            String productGiveawaySpecs = timestream.getGiveawaySpecs();
            String siblingPromotionId = timestream.getSiblingPromotionId();

            deleteTimestream(sqLiteDatabase, id);

            String sql;

            switch (tableName) {

                case FRESH_TIMESTREAM_TABLE_NAME:
                case POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME:

                    sql = "insert into " + tableName + "(" +
                            FRESH_TIMESTREAM_COLUMNS + ") " + "values " + "('" + id + "','" + productCode +
                            "','" + productDOP + "','" + productPromotionDate + "','" + productExpireDate +
                            "','" + productCoordinate + "','" + productInventory + "')";

                    sqLiteDatabase.execSQL(sql);
                    timestream.setUpdated(true);
                    break;

                case PROMOTION_TIMESTREAM_TABLE_NAME:
                case POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME:

                    sql = "insert into " + tableName + "(" +
                            PROMOTION_TIMESTREAM_COLUMNS + ") " + "values " + "('" + id + "','" + productCode +
                            "','" + productDOP + "','" + productPromotionDate + "','" + productExpireDate +
                            "','" + productCoordinate + "','" + productInventory + "','" + productDiscountRate +
                            "','" + productBuySpecs +"','" + productGiveawaySpecs + "','" + siblingPromotionId + "')";

                    sqLiteDatabase.execSQL(sql);
                    timestream.setUpdated(true);
                    break;

            }


        }

        public static void deleteTimestream(SQLiteDatabase sqLiteDatabase, String timeStreamId) {

            String sql1 = "delete from " + FRESH_TIMESTREAM_TABLE_NAME + " where " +
                    "id='" + timeStreamId + "'";
            String sql2 = "delete from " + POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME + " where " +
                    "id='" + timeStreamId + "'";
            String sql3 = "delete from " + PROMOTION_TIMESTREAM_TABLE_NAME + " where " +
                    "id='" + timeStreamId + "'";
            String sql4 = "delete from " + POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME + " where " +
                    "id='" + timeStreamId + "'";

            sqLiteDatabase.execSQL(sql1);
            sqLiteDatabase.execSQL(sql2);
            sqLiteDatabase.execSQL(sql3);
            sqLiteDatabase.execSQL(sql4);

        }

        public static HashMap<String, Product> getAllProduct() {

            HashMap<String, Product> r = new HashMap<>();

            Cursor cursor = query(MyApplication.sqLiteDatabase, PRODUCT_INFO_TABLE_NAME,
                    new String[]{"product_code", "product_exp", "product_exp_time_unit"},
                    null, null);

            if (!cursor.moveToFirst()) return r;

            do {

                Product p = new Product();
                p.setProductCode(cursor.getString(0));
                p.setProductEXP(cursor.getString(1));
                p.setProductEXPTimeUnit(cursor.getString(2));
                r.put(p.getProductCode(),p);

            } while ( cursor.moveToNext());

            return r;
        }

        public static void truncate(String tableName) {

            MyApplication.sqLiteDatabase.execSQL("delete from " + tableName);
        }

        /**
         * @param timestreamState
         * SettingActivity.TIMESTREAM_IN_PROMOTING,
         * SettingActivity.TIMESTREAM_NOT_IN_PROMOTING
         * @return ArrayList促销中的所有timestream，或者未促销的所有timestream
         */
        public static List<Timestream> getAllTimestreams(int timestreamState) {

            ArrayList<Timestream> list = new ArrayList<>();
            Cursor cursor;

            switch (timestreamState) {

                case SettingActivity.TIMESTREAM_IN_PROMOTION:

                    cursor = query(MyApplication.sqLiteDatabase,
                            POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME,new String[]{"*"}, null, null);
                    generateAndAppendTimestream(list, cursor, timestreamState);

                    cursor = query(MyApplication.sqLiteDatabase,
                            PROMOTION_TIMESTREAM_TABLE_NAME,new String[]{"*"}, null, null);
                    generateAndAppendTimestream(list, cursor, timestreamState);
                    return list;

                case SettingActivity.TIMESTREAM_NOT_IN_PROMOTION:

                    cursor = query(MyApplication.sqLiteDatabase,
                            POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME,new String[]{"*"}, null, null);
                    generateAndAppendTimestream(list, cursor, timestreamState);

                    cursor = query(MyApplication.sqLiteDatabase,
                            FRESH_TIMESTREAM_TABLE_NAME,new String[]{"*"}, null, null);
                    generateAndAppendTimestream(list, cursor, timestreamState);

                    return list;
            }
            return list;
        }

        private static void generateAndAppendTimestream(ArrayList<Timestream> list, Cursor cursor, int timestreamState) {

            if (!cursor.moveToFirst()) return;

            do {

                Timestream t = new Timestream();

                t.setId(cursor.getString(0));
                t.setProductCode(cursor.getString(1));

                try {
                    t.setProductDOP(DateUtil.typeMach(cursor.getString(2)));
                    t.setProductPromotionDate(DateUtil.typeMach(cursor.getString(3)));
                    t.setProductExpireDate(DateUtil.typeMach(cursor.getString(4)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                t.setProductCoordinate(cursor.getString(5));
                t.setProductInventory(cursor.getString(6));

                if (timestreamState == SettingActivity.TIMESTREAM_IN_PROMOTION) {

                    t.setDiscountRate(cursor.getString(7));
                    t.setBuySpecs(cursor.getString(8));
                    t.setGiveawaySpecs(cursor.getString(9));
                    t.setSiblingPromotionId(cursor.getString(10));
                }

                list.add(t);

            } while (cursor.moveToNext());

        }
    }
}
