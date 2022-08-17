package com.shepherdboy.pdstreamline.beans;

import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * 商品位，保存一个{@link Product}的productCode，包括对应{@link Timestream}
 */
public class Cell {

    private String id; //UUID

    private String rowId;

    private int columnSortNumber; //Cell在一列中横向的排序号，列与列之间不对齐

    private String shelfId; //货架号

    private String productCode; //商品条码号，占位

    private LinkedHashMap<String, Timestream> timestreams; //当前位置摆放的最旧的timestream，最多3种，新鲜，临期，到期

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

    public Cell() {
        this.id = UUID.randomUUID().toString();
    }
    public Cell(String rowId) {
        setRowId(rowId);
    }

    public Cell(Product product) {

        this();
        this.productCode = product.getProductCode();
        this.timestreams = product.getTimeStreams();

    }

    public String getId() {
        return id;
    }

    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public int getColumnSortNumber() {
        return columnSortNumber;
    }

    public void setColumnSortNumber(int columnSortNumber) {
        this.columnSortNumber = columnSortNumber;
    }

    public String getShelfId() {
        return shelfId;
    }

    public void setShelfId(String shelfId) {
        this.shelfId = shelfId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public LinkedHashMap<String, Timestream> getTimestreams() {
        return timestreams;
    }

    public void setTimestreams(LinkedHashMap<String, Timestream> timestreams) {
        this.timestreams = timestreams;
    }

    public boolean isEmpty() {

        return timestreams == null || timestreams.isEmpty();
    }
}
