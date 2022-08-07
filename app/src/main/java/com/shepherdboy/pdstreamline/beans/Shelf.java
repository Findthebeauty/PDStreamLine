package com.shepherdboy.pdstreamline.beans;

import java.util.ArrayList;
import java.util.UUID;

/**
 * 商品货架，一个货架可以包含多个{@link Row}
 */
public class Shelf {

    private final String id; //货架UUID

    private String name; //货架名

    private ArrayList<Row> rows; //货架可放商品行数

    private int maxRowCount; //最大货架行数，跟真实货架对应

    public Shelf() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Row> getRows() {
        return rows;
    }

    public void setRows(ArrayList<Row> rows) {
        this.rows = rows;
    }

    public int getMaxRowCount() {
        return maxRowCount;
    }

    public void setMaxRowCount(int maxRowCount) {
        this.maxRowCount = maxRowCount;
    }
}
