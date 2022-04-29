package com.shepherdboy.pdstreamline.utils;

import com.shepherdboy.pdstreamline.beans.Timestream;

import java.util.Comparator;

public class AscCoordinateComparator implements Comparator<Timestream> {

    private static final AscCoordinateComparator comparator = new AscCoordinateComparator();

    public static AscCoordinateComparator getInstance() {

        return  comparator;
    }

    @Override
    public int compare(Timestream t1, Timestream t2) {

        String[] coordinates1 = t1.getProductCoordinate().split(",");
        String[] coordinates2 = t2.getProductCoordinate().split(",");

        int coord1 = 0;
        int coord2 = 0;

        try {

            coord1 = Integer.parseInt(coordinates1[0].replaceAll(",", ""));
            coord2 = Integer.parseInt(coordinates2[0].replaceAll(",", ""));
        } catch (Exception e) {

            e.printStackTrace();
        }

        return coord1 - coord2;
    }
}


