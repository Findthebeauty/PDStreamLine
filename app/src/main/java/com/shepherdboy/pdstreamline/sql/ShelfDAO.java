package com.shepherdboy.pdstreamline.sql;

import static com.shepherdboy.pdstreamline.MyApplication.sqLiteDatabase;

import android.database.Cursor;

import com.shepherdboy.pdstreamline.beans.Cell;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.beans.Row;
import com.shepherdboy.pdstreamline.beans.Shelf;
import com.shepherdboy.pdstreamline.utils.AscOrderNumberComparator;

import java.util.ArrayList;
import java.util.Collections;

/**
 *货架商品加载类，将货架对应的所有商品加载到货架对象中
 */
public class ShelfDAO {

    /**
     * 插入或更新货架
     * @param shelf 货架
     */
    public static void update(Shelf shelf) {

        String id = shelf.getId();
        String name = shelf.getName();
        String classify = shelf.getClassify();
        int maxRowCount = shelf.getMaxRowCount();

        String sql = "insert or replace into " + MyDatabaseHelper.SHELF_TABLE_NAME + "(" +
                MyDatabaseHelper.SHELVES_COLUMNS + ")" + "values('" + id +
                "','" + name + "','" + classify + "','" + maxRowCount + "')";
        sqLiteDatabase.execSQL(sql);
        shelf.setUpdated(false);
    }

    /**
     * 插入或更新行
     * @param row
     */
    public static void update(Row row) {

        String id = row.getId();
        int sortNumber = row.getSortNumber();
        String shelfId = row.getShelfId();
        String name = row.getName();

        String sql = "insert or replace into " + MyDatabaseHelper.ROW_TABLE_NAME + "(" +
                MyDatabaseHelper.ROWS_COLUMNS + ")" + "values('" + id + "','" +
                sortNumber + "','" + shelfId + "','" + name + "')";

        sqLiteDatabase.execSQL(sql);
        row.setUpdated(true);
    }

    /**
     * 插入或更新商品陈列面
     * @param cell
     */
    public static void update(Cell cell) {

        String id = cell.getId();
        String rowId = cell.getRowId();
        String shelfId = cell.getShelfId();
        String productCode = cell.getProductCode();
        int columnSortNumber = cell.getColumnSortNumber();

        String sql = "insert or replace into " + MyDatabaseHelper.CELL_TABLE_NAME + "(" +
                MyDatabaseHelper.CELLS_COLUMNS + ")" + "values('" + id + "','" +
                rowId + "','" + shelfId + "','" + productCode + "','" +
                columnSortNumber + "')";

        sqLiteDatabase.execSQL(sql);
        cell.setUpdated(true);
    }

    /**
     * 获取所有的货架
     * @return
     */
    public static ArrayList<Shelf> getShelves() {

        ArrayList<Shelf> shelves = new ArrayList<>();
        Shelf shelf;

        Cursor cursor = MyDatabaseHelper.query(sqLiteDatabase, MyDatabaseHelper.SHELF_TABLE_NAME,
                new String[]{"*"}, null, null);

        if (cursor.moveToFirst()) {

            do {

                shelf = new Shelf();
                shelf.setId(cursor.getString(0));
                shelf.setName(cursor.getString(1));
                shelf.setClassify(cursor.getString(2));
                shelf.setMaxRowCount(cursor.getInt(3));

                shelves.add(shelf);

            } while (cursor.moveToNext());
        }

        return shelves;
    }


    /**
     * 获取shelf以及其陈列的的商品
     * @param id
     * @return
     */
    public static Shelf getShelf(String id) {

        Shelf shelf = new Shelf(id);

        Cursor cursor = MyDatabaseHelper.query(sqLiteDatabase, MyDatabaseHelper.SHELF_TABLE_NAME,
                new String[]{"*"}, "id=?", new String[]{id});

        if (cursor.getCount() > 0) {

            cursor.moveToNext();

            shelf.setName(cursor.getString(1));
            shelf.setClassify(cursor.getString(2));
            shelf.setMaxRowCount(cursor.getInt(3));
        }

        shelf.setRows(getRows(id));

        return shelf;
    }

    public static ArrayList<Row> getRows(String shelfId) {

        ArrayList<Row> arrayList = new ArrayList<>();

        Cursor cursor = MyDatabaseHelper.query(sqLiteDatabase, MyDatabaseHelper.ROW_TABLE_NAME,
                new String[]{"*"}, "shelf_id=?", new String[]{shelfId});

        if (cursor.moveToNext()) {

            do {

                Row row = new Row(shelfId);
                row.setId(cursor.getString(0));
                row.setSortNumber(cursor.getInt(1));
                row.setName(cursor.getString(3));
                row.setCells(getCells(row.getId()));
                arrayList.add(row);
            } while (cursor.moveToNext());
        }

        Collections.sort(arrayList, AscOrderNumberComparator.getInstance());
        return arrayList;
    }

    public static ArrayList<Cell> getCells(String rowId) {

        ArrayList<Cell> arrayList = new ArrayList<>();

        Cursor cursor = MyDatabaseHelper.query(sqLiteDatabase, MyDatabaseHelper.CELL_TABLE_NAME,
                new String[]{"*"}, "row_id=?", new String[]{rowId});

        if (cursor.moveToNext()) {

            do {

                Cell cell = new Cell(rowId);
                cell.setId(cursor.getString(0));
                cell.setShelfId(cursor.getString(2));
                cell.setProductCode(cursor.getString(3));
                cell.setColumnSortNumber(cursor.getInt(4));

                arrayList.add(cell);
            } while (cursor.moveToNext());
        }

        for (Cell cell : arrayList) {


            Product product = PDInfoWrapper.getProduct(cell.getProductCode(),
                    sqLiteDatabase, MyDatabaseHelper.ENTIRE_TIMESTREAM);

            cell.setTimestreams(product.getTimeStreams());

        }

        Collections.sort(arrayList, AscOrderNumberComparator.getInstance());
        return arrayList;
    }

    public static int selectCount(String id) {

        try (Cursor cursor = sqLiteDatabase.rawQuery("select count(id) from " +
                MyDatabaseHelper.CELL_TABLE_NAME + " where shelf_id=?", new String[]{id})) {

            cursor.moveToFirst();

            return cursor.getInt(0);
        }
    }


    public static void deleteShelf(String id) {

        delete(MyDatabaseHelper.CELL_TABLE_NAME ,"shelf_id", id);
        delete(MyDatabaseHelper.ROW_TABLE_NAME, "shelf_id", id);
        delete(MyDatabaseHelper.SHELF_TABLE_NAME, "id", id);
    }

    public static void delete(String tableName,String selection, String selectionArg) {

        String sql = "delete from " + tableName + " where " + selection +"='" + selectionArg + "'";

        sqLiteDatabase.execSQL(sql);
    }

    public static ArrayList<String> getClassify() {


        ArrayList<String> arrayList = new ArrayList<>();

        Cursor cursor = MyDatabaseHelper.query(sqLiteDatabase, MyDatabaseHelper.SHELF_TABLE_NAME,
                new String[]{"distinct classify"}, null, null);

        if (cursor.moveToNext()) {

            do {

                String classify = cursor.getString(0);

                arrayList.add(classify);
            } while (cursor.moveToNext());
        }

        return arrayList;
    }
}
