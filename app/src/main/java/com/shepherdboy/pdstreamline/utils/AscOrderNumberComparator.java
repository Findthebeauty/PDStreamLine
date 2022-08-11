package com.shepherdboy.pdstreamline.utils;

import com.shepherdboy.pdstreamline.beans.Cell;
import com.shepherdboy.pdstreamline.beans.Row;

import java.util.Comparator;

public class AscOrderNumberComparator implements Comparator<Object> {

    private AscOrderNumberComparator(){}

    private static final AscOrderNumberComparator instance = new AscOrderNumberComparator();

    public static AscOrderNumberComparator getInstance() {

        return instance;
    }

    @Override
    public int compare(Object o1, Object o2) {

        int order1 = 0;
        int order2 = 0;

        if (o1 instanceof Row && o2 instanceof Row) {

            order1 = ((Row)o1).getSortNumber();
            order2 = ((Row)o2).getSortNumber();
        }

        if (o1 instanceof Cell && o2 instanceof Cell) {

            order1 = ((Cell) o1).getColumnSortNumber();
            order2 = ((Cell) o2).getColumnSortNumber();
        }

        return order1 - order2;
    }
}
