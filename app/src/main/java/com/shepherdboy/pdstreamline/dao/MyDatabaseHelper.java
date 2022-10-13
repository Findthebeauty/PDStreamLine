package com.shepherdboy.pdstreamline.dao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    public static final int PLAIN_PRODUCT_WITH_NO_TIMESTREAM = -1;

    public static final int ENTIRE_TIMESTREAM = 0;

    public static final int POSSIBLE_PROMOTION_TIMESTREAM = 1;

    public static final int POSSIBLE_EXPIRED_TIMESTREAM = 2;

    public static final int TIMESTREAM_TO_CHECK = 3;

    public static final int PROMOTION_TIMESTREAM = 4;

    public static final int OFF_SHELVES_HISTORY= 5;

    public static final int NEW_TIMESTREAM = 6;

    public static final int UPDATE_BASKET = 7;

    public static final int LOSS_LOG = 8;

    public static final int UNPACK_COMBINATION = 9;

    public static final String PROMOTION_DATE_SELECTION = "product_promotion_date";

    public static final String EXPIRE_DATE_SELECTION = "product_expire_date";

    public static final String IN_BASKET_SELECTION = "in_basket";

    public static final String PRODUCT_INVENTORY_SELECTION = "product_inventory";

    public static final String PROCESS_DATE_SELECTION = "process_date";

    public static final String PRODUCT_INFO_TABLE_NAME = "product_inf";

    public static final String PRODUCT_TO_CHECK_TABLE_NAME = "product_to_check";

    public static final String FRESH_TIMESTREAM_TABLE_NAME = "fresh_timestream";

    public static final String PROMOTION_TIMESTREAM_TABLE_NAME = "promotion_timestream";

    public static final String POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME = "possible_promotion_timestream";

    public static final String POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME = "possible_expired_timestream";

    public static final String PROMOTION_HISTORY_TABLE_NAME = "promotion_history";

    public static final String PRODUCT_LOSS_LOG_TABLE_NAME = "product_loss_log";

    public static final String OFF_SHELVES_HISTORY_TABLE_NAME = "off_shelves_history";

    public static final String OBSERVER_TABLE_NAME = "observer";

    public static final String DOP_INTERVALS_TABLE_NAME = "dop_intervals";

    public static final String SETTING_TABLE_NAME = "manage_setting";

    public static final String SHELF_TABLE_NAME = "shelves";

    public static final String ROW_TABLE_NAME = "rows";

    public static final String CELL_TABLE_NAME = "cells";

    public static final String PRODUCT_INFO_COLUMNS = "product_code,product_name," +
            "product_exp,product_exp_time_unit,product_group_number,product_spec," +
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

    public static final String PRODUCT_LOSS_LOG_COLUMNS = "sibling_product_code,sibling_product_dop," +
            "loss_product_code,loss_product_dop,loss_inventory,loss_type,process_date,process_account," +
            "process_photo_id";

    public static final String OBSERVER_COLUMNS = "id,product_code,product_has_fresh_timestream," +
            "product_last_check_date,product_next_check_date,product_dop_interval";

    public static final String DOP_INTERVALS_COLUMNS = "id,product_code,product_dop," +
            "dop_interval,dop_interval_count";

    public static final String SETTING_COLUMNS = "id,setting_index,setting_value";

    public static final String SHELVES_COLUMNS = "id,name,classify,max_row_count";

    public static final String ROWS_COLUMNS = "id,sort_number,shelf_id,name";

    public static final String CELLS_COLUMNS = "id,row_id,shelf_id,product_code,column_sort_number";

    public static final String CREATE_TABLE_PRODUCT_INF = "create table product_inf(" +
            "product_code text primary key," +
            "product_name text," +
            "product_exp text,product_exp_time_unit text," +
            "product_group_number text," +
            "product_spec text," +
            "product_shelves_indexes text," +
            "product_last_check_date," +
            "product_next_check_date," +
            "product_default_coordinate text)";

    public static final String CREATE_TABLE_PRODUCT_TO_CHECK = "create table " +
            PRODUCT_TO_CHECK_TABLE_NAME
            +"(product_code text primary key)";

    public static final String CREATE_TABLE_FRESH_TIMESTREAM = "create table " +
            FRESH_TIMESTREAM_TABLE_NAME +
            "(id text primary key," +
            "product_code text," +
            "product_dop text," +
            "product_promotion_date text," +
            "product_expire_date text," +
            "product_coordinate text," +
            "product_inventory text)";

    public static final String CREATE_TABLE_POSSIBLE_PROMOTION_TIMESTREAM = "create table " +
            POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME +
            "(id text primary key," +
            "product_code text not null," +
            "product_dop text," +
            "product_promotion_date text," +
            "product_expire_date text," +
            "product_coordinate text," +
            "product_inventory text," +
            "in_basket text default 'false')";

    public static final String CREATE_TABLE_PROMOTION_TIMESTREAM = "create table "+
            PROMOTION_TIMESTREAM_TABLE_NAME +
            "(id text primary key," +
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

    public static final String CREATE_TABLE_POSSIBLE_EXPIRED_TIMESTREAM = "create table "
        + POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME +
            "(id text primary key," +
            "product_code text," +
            "product_dop text," +
            "product_promotion_date text," +
            "product_expire_date text," +
            "product_coordinate text," +
            "product_inventory text," +
            "product_discount_rate text," +
            "product_buy_specs text," +
            "product_giveaway_specs text," +
            "sibling_promotion_id text," +
            "in_basket text default 'false')";

    public static final String CREATE_TABLE_PROMOTION_HISTORY = "create table " +
            PROMOTION_HISTORY_TABLE_NAME +
            "(promotion_id text primary key," +
            "product_code text," +
            "product_dop text," +
            "product_promotion_date text," +
            "product_expire_date text," +
            "product_coordinate text," +
            "product_inventory text," +
            "product_discount_rate text," +
            "sibling_promotion_id text)";

    public static final String CREATE_TABLE_PRODUCT_LOSS_LOG = "create table " +
            PRODUCT_LOSS_LOG_TABLE_NAME +
            "(id integer primary key autoincrement," +
            "sibling_product_code text," +
            "sibling_product_dop text," +
            "loss_product_code text," +
            "loss_product_dop text," +
            "loss_inventory text," +
            "loss_type text," +
            "process_date text," +
            "process_account text," +
            "process_photo_id text)";

    public static final String CREATE_TABLE_OFF_SHELVES_HISTORY = "create table " +
            OFF_SHELVES_HISTORY_TABLE_NAME +
            "(id text primary key," +
            "product_code text," +
            "product_dop text," +
            "product_promotion_date text," +
            "product_expire_date text," +
            "product_coordinate text," +
            "product_off_shelves_inventory text)";

    public static final String CREATE_TABLE_OBSERVER = "create table " +
            OBSERVER_TABLE_NAME +
            "(id text primary key," +
            "product_code text," +
            "product_has_fresh_timestream boolean," +
            "product_last_check_date text," +
            "product_next_check_date text," +
            "product_dop_interval text)";

    public static final String CREATE_TABLE_DOP_INTERVALS = "create table " +
            DOP_INTERVALS_TABLE_NAME +
            "(id text primary key," +
            "product_code text," +
            "dop_interval text," +
            "dop_interval_count text," +
            "unique(product_code,dop_interval))";

    public static final String CREATE_TABLE_SETTING = "create table " +
            SETTING_TABLE_NAME +
            "(id integer primary key autoincrement," +
            "setting_index text unique," +
            "setting_value text)";

    public static final String CREATE_TABLE_SHELVES = "create table " +
            SHELF_TABLE_NAME +
            "(id text primary key," +
            "name text unique," +
            "classify text," +
            "max_row_count integer)";

    public static final String CREATE_TABLE_ROWS = "create table " +
            ROW_TABLE_NAME +
            "(id text primary key," +
            "sort_number integer," +
            "shelf_id text," +
            "name text," +
            "unique(sort_number,shelf_id))";

    public static final String CREATE_TABLE_CELLS = "create table " +
            CELL_TABLE_NAME +
            "(id text primary key," +
            "row_id text," +
            "shelf_id text," +
            "product_code text," +
            "column_sort_number integer," +
            "unique(row_id,column_sort_number))";

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
        sqLiteDatabase.execSQL(CREATE_TABLE_PRODUCT_LOSS_LOG);
        sqLiteDatabase.execSQL(CREATE_TABLE_OFF_SHELVES_HISTORY);
        sqLiteDatabase.execSQL(CREATE_TABLE_OBSERVER);
        sqLiteDatabase.execSQL(CREATE_TABLE_DOP_INTERVALS);
        sqLiteDatabase.execSQL(CREATE_TABLE_SETTING);
        sqLiteDatabase.execSQL(CREATE_TABLE_SHELVES);
        sqLiteDatabase.execSQL(CREATE_TABLE_ROWS);
        sqLiteDatabase.execSQL(CREATE_TABLE_CELLS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        

    }

    public static void saveSetting(String index, String value, SQLiteDatabase sqLiteDatabase) {

        String sql = "insert or replace into " + SETTING_TABLE_NAME + "(" +
                SETTING_COLUMNS + ")" + "values (" + null + ",'" + index + "','" + value
                +"')";

        sqLiteDatabase.execSQL(sql);
    }

    @SuppressLint("Range")
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

}
