package com.shepherdboy.pdstreamline.utils;

import android.util.Log;

import com.shepherdboy.pdstreamline.beans.Timestream;

import java.util.Comparator;
import java.util.Date;
import java.util.Map;

// 比较timestream的生产日期先后
public class AscDateComparator implements Comparator<Map.Entry<String, Timestream>> {


    private static final AscDateComparator ascDateComparator = new AscDateComparator();

    public static AscDateComparator getInstance() {

        return ascDateComparator;

    }


    @Override
    public int compare(Map.Entry<String, Timestream> o1, Map.Entry<String, Timestream> o2) {

        Date date1 = o1.getValue().getProductDOP();
        Date date2 = o2.getValue().getProductDOP();

        if (date1 == null) {

            date1 = new Date();
        }

        if (date2 == null) {

            date2 = new Date();
        }

        return (int) ((date1.getTime()  - date2.getTime()) / (1000 * 60 * 60 * 24));
    }
}
