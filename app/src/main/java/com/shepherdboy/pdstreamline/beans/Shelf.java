package com.shepherdboy.pdstreamline.beans;

import java.util.ArrayList;
import java.util.UUID;

/**
 * 商品货架，一个货架可以包含多个{@link Row}
 */
public class Shelf {

    private String id; //货架UUID

    private String name; //货架名

    private String classify; //货架类别

    private ArrayList<Row> rows; //货架可放商品行数

    private int shelfViewId;

    private int maxRowCount; //最大货架行数，跟真实货架对应

    private boolean updated; //false表示信息未变更且已同步到数据库，true表示信息变更未同步到数据库

    public Shelf(String id) {
        setId(id);
    }
    public Shelf(Shelf shelf) {

        this.id = shelf.getId();
        this.name = shelf.getName();
        this.classify = shelf.getClassify();
        this.maxRowCount = shelf.getMaxRowCount();
    }

    public int getShelfViewId() {
        return shelfViewId;
    }

    public void setShelfViewId(int shelfViewId) {
        this.shelfViewId = shelfViewId;
    }

    public String getClassify() {
        return classify;
    }

    public void setClassify(String classify) {
        this.classify = classify;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public Shelf() {
        this.id = UUID.randomUUID().toString();
        this.name = "新货架" + id.hashCode();
        this.classify = "通用";
        this.maxRowCount = Integer.MAX_VALUE;
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
