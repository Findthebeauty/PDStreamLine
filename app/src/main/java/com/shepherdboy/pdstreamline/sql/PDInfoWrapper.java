package com.shepherdboy.pdstreamline.sql;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.activities.SettingActivity;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.beans.TimestreamCombination;
import com.shepherdboy.pdstreamline.utils.AIInputter;
import com.shepherdboy.pdstreamline.utils.AscCoordinateComparator;
import com.shepherdboy.pdstreamline.utils.DateUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 商品包装类，从数据库查询信息并包装成Product和Timestream对象
 */
public class PDInfoWrapper {


    @SuppressLint("Range")
    public static String getProductName(String productCode, SQLiteDatabase sqLiteDatabase) {

        String name;

        Cursor cursor = MyDatabaseHelper.query(sqLiteDatabase, MyDatabaseHelper.PRODUCT_INFO_TABLE_NAME,
                new String[]{"*"},"product_code=?", new String[]{productCode});
        try {

            cursor.moveToFirst();
            name = cursor.getString(cursor.getColumnIndex("product_name"));
        } catch (Exception e) {

            name = "null";
        }

        return name;

    }


    /**
     * 获取Product的完整信息，包含所有的Timestream
     * @param productCode
     * @param sqLiteDatabase
     * @param typeCode
     * @return
     */
    public static Product getProduct(String productCode, SQLiteDatabase sqLiteDatabase, int typeCode) {

        Product product = new Product();

        LinkedHashMap<String, Timestream> timeStreamHashMap = new LinkedHashMap<>();

        sqLiteDatabase.beginTransaction();

        Cursor cursor = MyDatabaseHelper.query(sqLiteDatabase, MyDatabaseHelper.PRODUCT_INFO_TABLE_NAME,
                new String[]{"*"}, "product_code=?", new String[]{productCode});

        product.setProductCode(productCode);
        product.setLastCheckDate(DateUtil.typeMach(MyApplication.today));

        if (cursor.getCount() > 0) {

            cursor.moveToNext();

            product.setProductName(cursor.getString(1));
            product.setProductEXP(cursor.getString(2));
            product.setProductEXPTimeUnit(cursor.getString(3));
            product.setProductGroupNumber(cursor.getString(4));
            product.setProductSpec(cursor.getString(5));
            product.setShelvesIndexes(cursor.getString(6));
            product.setUpdated(true);
            product.setNextCheckDate(DateUtil.typeMach(AIInputter.getNextCheckDate(product.getProductEXP(),
                    product.getProductEXPTimeUnit())));

            product.setDefaultCoordinate(cursor.getString(9));
        } else {

            AIInputter.fillTheBlanks(product);

        }

        switch (typeCode) {

            case MyDatabaseHelper.ENTIRE_TIMESTREAM:

                getEntireTimestream(product, sqLiteDatabase, timeStreamHashMap);
                break;

            case MyDatabaseHelper.PLAIN_PRODUCT_WITH_NO_TIMESTREAM:
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
     * @param sqLiteDatabase sqliteDatabase
     * @param intentCode     POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME，POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME
     * @return timestreams
     */
    public static LinkedList<Timestream> getStaleTimestreams(SQLiteDatabase sqLiteDatabase,
                                                             int intentCode) {

        LinkedList<Timestream> tsLList = new LinkedList<>();

        Cursor cursor;
        Timestream tempTs;
        String tableName = null;

        switch (intentCode) {

            case MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM:

                tableName = MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME;
                break;


            case MyDatabaseHelper.POSSIBLE_EXPIRED_TIMESTREAM:

                tableName = MyDatabaseHelper.POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME;
                break;


        }

        cursor = MyDatabaseHelper.query(sqLiteDatabase, tableName,
                new String[]{"*"}, null, null);

        if (cursor.moveToFirst()) {

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

                    case MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM:

                        tempTs.setInBasket(Boolean.parseBoolean(cursor.getString(7)));
                        break;

                    case MyDatabaseHelper.POSSIBLE_EXPIRED_TIMESTREAM:

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

    public static HashMap<String, TimestreamCombination> getTimestreamCombinations(SQLiteDatabase sqLiteDatabase) {

        HashMap<String, Timestream> promotingTimestreams = new HashMap<>();
        HashMap<String, TimestreamCombination> combs = new HashMap<>();

        queryAndGeneratePromotingTimestream(sqLiteDatabase,
                MyDatabaseHelper.PROMOTION_TIMESTREAM_TABLE_NAME,
                promotingTimestreams);
        queryAndGeneratePromotingTimestream(sqLiteDatabase,
                MyDatabaseHelper.POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME,
                promotingTimestreams);

        assemblingTimestreamCombinations(combs, promotingTimestreams);

        return combs;
    }

    private static void assemblingTimestreamCombinations(HashMap<String, TimestreamCombination> combs, HashMap<String, Timestream> promotingTimestreams) {

        for (Timestream timestream : promotingTimestreams.values()) {

            String discountRate = timestream.getDiscountRate();

            discountRate = discountRate == null ? "" : discountRate;

            switch (discountRate) {

                case "0.5":

                    combs.put(timestream.getId(), new TimestreamCombination(timestream));
                    break;

                case "1":

                    Timestream giveawayTimestream = promotingTimestreams.get(timestream.getSiblingPromotionId());
                    combs.put(timestream.getId(), new TimestreamCombination(timestream, giveawayTimestream));
                    break;

                default:
                    break;

            }

        }

    }

    /**
     * 从指定表{MyDatabaseHelper.PROMOTION_TIMESTREAM_TABLE_NAME,
     * MyDatabaseHelper.POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME}中查询所有timestream
     * @param sqLiteDatabase
     * @param tableName
     * @param timestreams
     */
    private static void queryAndGeneratePromotingTimestream(SQLiteDatabase sqLiteDatabase, String tableName, HashMap<String, Timestream> timestreams) {


        Cursor cursor = MyDatabaseHelper.query(sqLiteDatabase, tableName,
                new String[]{"*"}, null, null);

        if (cursor.moveToFirst()) {

            do {

                Timestream timestream = new Timestream();

                timestream.setId(cursor.getString(0));
                timestream.setProductCode(cursor.getString(1));
                try {
                    timestream.setProductDOP(DateUtil.typeMach(cursor.getString(2)));
                    timestream.setProductPromotionDate(DateUtil.typeMach(cursor.getString(3)));
                    timestream.setProductExpireDate(DateUtil.typeMach(cursor.getString(4)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                timestream.setProductCoordinate(cursor.getString(5));
                timestream.setProductInventory(cursor.getString(6));
                timestream.setDiscountRate(cursor.getString(7));
                timestream.setBuySpecs(cursor.getString(8));
                timestream.setGiveawaySpecs(cursor.getString(9));
                timestream.setSiblingPromotionId(cursor.getString(10));

                timestreams.put(timestream.getId(), timestream);
            } while (cursor.moveToNext());

        }
    }

    /**
     * 构建已经捆绑的timestream
     * @param cursor
     * @param timestream
     */
    private static void inflatePromotionTimestream(Cursor cursor, Timestream timestream) {

        timestream.setId(cursor.getString(0));
        timestream.setProductCode(cursor.getString(1));
        try {
            timestream.setProductDOP(DateUtil.typeMach(cursor.getString(2)));
            timestream.setProductPromotionDate(DateUtil.typeMach(cursor.getString(3)));
            timestream.setProductExpireDate(DateUtil.typeMach(cursor.getString(4)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        timestream.setProductCoordinate(cursor.getString(5));
        timestream.setProductInventory(cursor.getString(6));
        timestream.setDiscountRate(cursor.getString(7));
        timestream.setBuySpecs(cursor.getString(8));
        timestream.setGiveawaySpecs(cursor.getString(9));
        timestream.setSiblingPromotionId(cursor.getString(10));
    }

    /**
     * @param sqLiteDatabase
     * @param date           当前日期
     */
    public static void getAndMoveTimestreamByDate(SQLiteDatabase sqLiteDatabase, String date) {

        String sql;
        String deleteSql;

        sql = "replace into " + MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME + "(" +
                MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM_COLUMNS + ") select " +
                MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM_COLUMNS + " from " +
                MyDatabaseHelper.FRESH_TIMESTREAM_TABLE_NAME + " where " + MyDatabaseHelper.PROMOTION_DATE_SELECTION + "<= '" + date + "'";
        deleteSql = "delete from " + MyDatabaseHelper.FRESH_TIMESTREAM_TABLE_NAME + " where " + MyDatabaseHelper.PROMOTION_DATE_SELECTION + "<='" + date + "'";
        sqLiteDatabase.execSQL(sql);
        sqLiteDatabase.execSQL(deleteSql);

        sql = "replace into " + MyDatabaseHelper.POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME + "(" +
                MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM_COLUMNS + ") select " +
                MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM_COLUMNS + " from " +
                MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME + " where " + MyDatabaseHelper.EXPIRE_DATE_SELECTION + "<'" + date + "'";
        deleteSql = "delete from " + MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME + " where " + MyDatabaseHelper.EXPIRE_DATE_SELECTION + "<'" + date + "'";
        sqLiteDatabase.execSQL(sql);
        sqLiteDatabase.execSQL(deleteSql);

        sql = "replace into " + MyDatabaseHelper.POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME + " select " +
                MyDatabaseHelper.POSSIBLE_EXPIRED_TIMESTREAM_COLUMNS + " from " +
                MyDatabaseHelper.PROMOTION_TIMESTREAM_TABLE_NAME + " where " + MyDatabaseHelper.EXPIRE_DATE_SELECTION + "<'" + date + "'";
        deleteSql = "delete from " + MyDatabaseHelper.PROMOTION_TIMESTREAM_TABLE_NAME + " where " + MyDatabaseHelper.EXPIRE_DATE_SELECTION + "<'" + date + "'";
        sqLiteDatabase.execSQL(sql);
        sqLiteDatabase.execSQL(deleteSql);

    }

    private static void getEntireTimestream(Product product, SQLiteDatabase sqLiteDatabase,
                                            LinkedHashMap<String, Timestream> timestreamHashMap) {

        queryAndGenerateTimestream(product, sqLiteDatabase, timestreamHashMap, MyDatabaseHelper.FRESH_TIMESTREAM_TABLE_NAME);
        queryAndGenerateTimestream(product, sqLiteDatabase, timestreamHashMap, MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME);
        queryAndGenerateTimestream(product, sqLiteDatabase, timestreamHashMap, MyDatabaseHelper.PROMOTION_TIMESTREAM_TABLE_NAME);
        queryAndGenerateTimestream(product, sqLiteDatabase, timestreamHashMap, MyDatabaseHelper.POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME);

        switch (MyApplication.activityIndex) {

            case MyApplication.PD_INFO_ACTIVITY:

                if (timestreamHashMap.size() == 0) {

                    Timestream temp = new Timestream();
                    AIInputter.fillTheBlanks(product, temp);
                    timestreamHashMap.put(temp.getId(), temp);

                }
                break;

            default:
                break;
        }

    }

    private static void queryAndGenerateTimestream(Product product, SQLiteDatabase sqLiteDatabase,
                                                   LinkedHashMap<String, Timestream> timestreamHashMap, String tableName) {

        Cursor cursor = MyDatabaseHelper.query(sqLiteDatabase, tableName,
                new String[]{"*"}, "product_code=?", new String[]{product.getProductCode()});

        boolean isPromoting = Objects.equals(tableName, MyDatabaseHelper.PROMOTION_TIMESTREAM_TABLE_NAME) ||
                Objects.equals(tableName, MyDatabaseHelper.POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME);

        Timestream temp;

        if (cursor.moveToFirst()) {

            do {

                temp = new Timestream();
                temp.setProductName(product.getProductName());
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

                if (tableName.equals(MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME)) {

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
        String productSpec = product.getProductSpec();
        String productShelvesIndexes = product.getShelvesIndexes();
        String productLastCheckDate = product.getLastCheckDate();
        String productNextCheckDate = product.getNextCheckDate();
        String productDefaultCoordinate = product.getDefaultCoordinate();

        String sql = "insert or replace into " + MyDatabaseHelper.PRODUCT_INFO_TABLE_NAME + "(" +
                MyDatabaseHelper.PRODUCT_INFO_COLUMNS + ") " + "values ('" + productCode +
                "','" + productName + "','" + productEXP + "','" + productEXPTimeUnit +
                "','" + productGroupNumber + "','" + productSpec + "','" + productShelvesIndexes +
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
        String buySpecs = timestream.getBuySpecs();
        String giveawaySpecs = timestream.getGiveawaySpecs();
        String siblingPromotionId = timestream.getSiblingPromotionId();
        String inBasket = String.valueOf(timestream.isInBasket());

        String sql;

        switch (transactionCode) {

            case MyDatabaseHelper.NEW_TIMESTREAM:

                sql = "insert or replace into " + MyDatabaseHelper.FRESH_TIMESTREAM_TABLE_NAME + "(" +
                        MyDatabaseHelper.FRESH_TIMESTREAM_COLUMNS + ") " + "values " + "('" + id +
                        "','" + productCode +
                        "','" + productDOP +
                        "','" + productPromotionDate +
                        "','" + productExpireDate +
                        "','" + productCoordinate +
                        "','" + productInventory + "')";

                sqLiteDatabase.execSQL(sql);
                timestream.setUpdated(true);
                break;

            case MyDatabaseHelper.PROMOTION_TIMESTREAM:

                sql = "insert or replace into " + MyDatabaseHelper.PROMOTION_TIMESTREAM_TABLE_NAME + "(" +
                        MyDatabaseHelper.PROMOTION_TIMESTREAM_COLUMNS + ") " + "values " + "('" + id +
                        "','" + productCode +
                        "','" + productDOP +
                        "','" + productPromotionDate +
                        "','" + productExpireDate +
                        "','" + productCoordinate +
                        "','" + productInventory +
                        "','" + discountRate +
                        "','" + buySpecs +
                        "','" + giveawaySpecs +
                        "','" + siblingPromotionId + "')";

                sqLiteDatabase.execSQL(sql);
                timestream.setUpdated(true);
                break;

            case MyDatabaseHelper.OFF_SHELVES_HISTORY:

                sql = "insert into " + MyDatabaseHelper.OFF_SHELVES_HISTORY_TABLE_NAME + "(" +
                        MyDatabaseHelper.OFF_SHELVES_HISTORY_COLUMNS + ") " + "values " + "('" + id +
                        "','" + productCode +
                        "','" + productDOP +
                        "','" + productPromotionDate +
                        "','" + productExpireDate +
                        "','" + productCoordinate +
                        "','" + productInventory + "')";

                sqLiteDatabase.execSQL(sql);
                timestream.setUpdated(true);
                break;

            case MyDatabaseHelper.UPDATE_BASKET:

                sql = "update " + MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME + " set " + MyDatabaseHelper.IN_BASKET_SELECTION +
                        "='" + inBasket + "' where id='" + id + "'";
                sqLiteDatabase.execSQL(sql);

                sql = "update " + MyDatabaseHelper.POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME + " set " + MyDatabaseHelper.IN_BASKET_SELECTION +
                        "='" + inBasket + "' where id='" + id + "'";

                sqLiteDatabase.execSQL(sql);
                timestream.setUpdated(true);
                break;

            default:
                Log.e("updateInfo", "更新失败，传入的transactionCode未分配更新方案");
                break;

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

            case MyDatabaseHelper.FRESH_TIMESTREAM_TABLE_NAME:
            case MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME:

                sql = "insert into " + tableName + "(" +
                        MyDatabaseHelper.FRESH_TIMESTREAM_COLUMNS + ") " + "values " +
                        "('" + id +
                        "','" + productCode +
                        "','" + productDOP +
                        "','" + productPromotionDate +
                        "','" + productExpireDate +
                        "','" + productCoordinate +
                        "','" + productInventory + "')";

                sqLiteDatabase.execSQL(sql);
                timestream.setUpdated(true);
                break;

            case MyDatabaseHelper.PROMOTION_TIMESTREAM_TABLE_NAME:
            case MyDatabaseHelper.POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME:

                sql = "insert into " + tableName + "(" +
                        MyDatabaseHelper.PROMOTION_TIMESTREAM_COLUMNS + ") " + "values " +
                        "('" + id +
                        "','" + productCode +
                        "','" + productDOP +
                        "','" + productPromotionDate +
                        "','" + productExpireDate +
                        "','" + productCoordinate +
                        "','" + productInventory +
                        "','" + productDiscountRate +
                        "','" + productBuySpecs +
                        "','" + productGiveawaySpecs +
                        "','" + siblingPromotionId + "')";

                sqLiteDatabase.execSQL(sql);
                timestream.setUpdated(true);
                break;

        }


    }

    public static void deleteTimestream(SQLiteDatabase sqLiteDatabase, String timeStreamId) {

        String sql1 = "delete from " + MyDatabaseHelper.FRESH_TIMESTREAM_TABLE_NAME + " where " +
                "id='" + timeStreamId + "'";
        String sql2 = "delete from " + MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME + " where " +
                "id='" + timeStreamId + "'";
        String sql3 = "delete from " + MyDatabaseHelper.PROMOTION_TIMESTREAM_TABLE_NAME + " where " +
                "id='" + timeStreamId + "'";
        String sql4 = "delete from " + MyDatabaseHelper.POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME + " where " +
                "id='" + timeStreamId + "'";
        String sql5 = "delete from " + MyDatabaseHelper.PROMOTION_TIMESTREAM_TABLE_NAME + " where " +
                "sibling_promotion_id='" + timeStreamId + "'";
        String sql6 = "delete from " + MyDatabaseHelper.POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME + " where " +
                "sibling_promotion_id='" + timeStreamId + "'";

        sqLiteDatabase.execSQL(sql1);
        sqLiteDatabase.execSQL(sql2);
        sqLiteDatabase.execSQL(sql3);
        sqLiteDatabase.execSQL(sql4);
        sqLiteDatabase.execSQL(sql5);
        sqLiteDatabase.execSQL(sql6);

    }

    /**
     * 不包含Timestream列表
     * @return
     */
    public static HashMap<String, Product> getAllProduct() {

        HashMap<String, Product> r = new HashMap<>();

        Cursor cursor = MyDatabaseHelper.query(MyApplication.sqLiteDatabase, MyDatabaseHelper.PRODUCT_INFO_TABLE_NAME,
                new String[]{MyDatabaseHelper.PRODUCT_INFO_COLUMNS},
                null, null);

        if (!cursor.moveToFirst()) return r;

        do {

            Product p = new Product();
            p.setProductCode(cursor.getString(0));
            p.setProductName(cursor.getString(1));
            p.setProductEXP(cursor.getString(2));
            p.setProductEXPTimeUnit(cursor.getString(3));
            p.setProductGroupNumber(cursor.getString(4));
            p.setProductSpec(cursor.getString(5));
            p.setShelvesIndexes(cursor.getString(6));
            p.setLastCheckDate(cursor.getString(7));
            p.setNextCheckDate(cursor.getString(8));
            p.setDefaultCoordinate(cursor.getString(9));
            r.put(p.getProductCode(), p);

        } while (cursor.moveToNext());

        return r;
    }

    public static void truncate(String tableName) {

        MyApplication.sqLiteDatabase.execSQL("delete from " + tableName);
    }


    /**
     * @param timestreamState SettingActivity.TIMESTREAM_IN_PROMOTING,
     *                        SettingActivity.TIMESTREAM_NOT_IN_PROMOTING
     * @return ArrayList促销中的所有timestream，或者未促销的所有timestream
     */
    public static List<Timestream> getAllTimestreams(int timestreamState) {

        ArrayList<Timestream> list = new ArrayList<>();
        Cursor cursor;

        switch (timestreamState) {

            case SettingActivity.TIMESTREAM_IN_PROMOTION:

                cursor = MyDatabaseHelper.query(MyApplication.sqLiteDatabase,
                        MyDatabaseHelper.POSSIBLE_EXPIRED_TIMESTREAM_TABLE_NAME, new String[]{"*"}, null, null);
                generateAndAppendTimestream(list, cursor, timestreamState);

                cursor = MyDatabaseHelper.query(MyApplication.sqLiteDatabase,
                        MyDatabaseHelper.PROMOTION_TIMESTREAM_TABLE_NAME, new String[]{"*"}, null, null);
                generateAndAppendTimestream(list, cursor, timestreamState);
                return list;

            case SettingActivity.TIMESTREAM_NOT_IN_PROMOTION:

                cursor = MyDatabaseHelper.query(MyApplication.sqLiteDatabase,
                        MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM_TABLE_NAME, new String[]{"*"}, null, null);
                generateAndAppendTimestream(list, cursor, timestreamState);

                cursor = MyDatabaseHelper.query(MyApplication.sqLiteDatabase,
                        MyDatabaseHelper.FRESH_TIMESTREAM_TABLE_NAME, new String[]{"*"}, null, null);
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
