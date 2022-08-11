package com.shepherdboy.pdstreamline.beans;

import java.util.ArrayList;
import java.util.UUID;

/**
 * 货架行，一个行有很多个{@link Cell}
 */
public class Row {

    private String id; //UUID

    private int sortNumber; //行号

    private String name; //行名，第一行，第二行·······

    private String shelfId; //货架号

    private ArrayList<Cell> cells; //该行所有的商品位，一般只加载有货的商品位

    private boolean updated;

    public void setId(String id) {
        this.id = id;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public Row() {
        this.id = UUID.randomUUID().toString();
    }
    public Row(String shelfId) {
        setShelfId(id);
    }

    public String getId() {
        return id;
    }

    public int getSortNumber() {
        return sortNumber;
    }

    public void setSortNumber(int sortNumber) {
        this.sortNumber = sortNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShelfId() {
        return shelfId;
    }

    public void setShelfId(String shelfId) {
        this.shelfId = shelfId;
    }

    public ArrayList<Cell> getCells() {
        return cells;
    }

    public void setCells(ArrayList<Cell> cells) {
        this.cells = cells;
    }
}
