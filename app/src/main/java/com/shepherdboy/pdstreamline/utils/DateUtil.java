package com.shepherdboy.pdstreamline.utils;

import androidx.annotation.Nullable;

import com.shepherdboy.pdstreamline.activities.SettingActivity;
import com.shepherdboy.pdstreamline.beans.DateScope;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static SimpleDateFormat formatNowToId = new SimpleDateFormat("MMddHHmmss");
    static SimpleDateFormat shortFormat = new SimpleDateFormat("yyyyMMdd");
    static SimpleDateFormat shortKeyFormat = new SimpleDateFormat("MMdd");
    static SimpleDateFormat shortNormalFormat = new SimpleDateFormat("yyyy-MM-dd");

    //todo 判断输入日期是否正确


    /**
     * 根据传入的日期长度解析成正确的日期，
     *
     * 4位双月双日，8位为4年双月双日
     * @param newDate
     * @return
     * @throws ParseException
     */
    public static String parseDate(@Nullable Date oldDate, String newDate) throws ParseException {

        newDate = newDate.trim();
        int length = newDate.length();

        Date tempDate;
        String tempDateString;
        StringBuilder stringBuilder = new StringBuilder();
        String result = shortNormalFormat.format(oldDate);

        switch (length) {

            case 8:

                tempDate = shortFormat.parse(newDate);

                result =  shortNormalFormat.format(tempDate);
                break;

            case 4:

                tempDate = getStartPointToday();
                tempDateString = shortFormat.format(tempDate);
                stringBuilder.append(tempDateString);
                stringBuilder.replace(4, 8, newDate);
                result =  shortNormalFormat.format(shortFormat.parse(stringBuilder.toString()));
                break;

            case 2:
            case 1:

                tempDateString = shortNormalFormat.format(oldDate);
                stringBuilder.append(tempDateString);
                stringBuilder.replace(8, 10, newDate);
                result = stringBuilder.toString();
                break;

            default:
                break;

        }
            return result;

    }

    public static String getShortKey(String dateString) {

        try {

            Date date = shortNormalFormat.parse(dateString);
            return shortKeyFormat.format(date);

        } catch (ParseException e) {

            e.printStackTrace();
            return "";
        }
    }

    public static int getIdByCurrentTime() {

        String time = String.valueOf(System.currentTimeMillis());

        time = time.substring(time.length() - 8);

        return Integer.parseInt(time);

    }

    /**
     * @param date 原始日期
     * @param index Calender.DATE(=Calender.DAY),Calender.MONTH,Calender.YEAR
     * @param amount 往回为负数，往后为正数在原始日期基础上切换日期
     * @return 修改后的日期Date
     */
    public static Date switchDate(Date date, int index, int amount) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.add(index, amount);

        return calendar.getTime();
    }

    public static Date getLastYear(Date date) {

        return switchDate(date, Calendar.YEAR, -1);
    }

    public static Date getLastMonth(Date date) {

        return switchDate(date, Calendar.MONTH, -1);
    }

    //获取当前日期时间
    public static Date getStartPointToday() {

        return getMidnightDate(new Date());

    }

    // 将时间调回当天0点
    public static Date getMidnightDate(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        // 晚上8点过后算第二天，便于夜班提前统计到期产品
        calendar.add(Calendar.HOUR, 4);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    // 将字符串转换为日期
    public static Date typeMach(String date) throws ParseException {

        Date tempDate;
        if (date != null && !date.equals("")) {

            try {

                tempDate = format.parse(date);

            } catch (ParseException e) {

                try {

                    tempDate = format.parse(date.trim() + " 00:00:00");

                } catch (ParseException exception) {

                    return null;

                }
            }

            return tempDate;

        } else {

            return null;

        }
    }

    // 将日期转换为字符串
    public static String typeMach(Date date) {

        if (date == null) {

            return "";

        }

        return format.format(date);
    }

    // 根据生产日期与保质期计算促销时间
    public static Date calculatePromotionDate(Date productDOP, int productEXP, String productEXPTimeUnit) {

        Calendar calendar = Calendar.getInstance();

        calendar.setTime(productDOP);
        calendar.add(Calendar.DATE, -1);

        long mlsEXP = SettingActivity.stringToMillionSeconds(String.valueOf(productEXP), productEXPTimeUnit);

        DateScope scope = getDateScope(mlsEXP);
        int promotionOffset = Integer.parseInt(scope.getPromotionOffsetValue());

        switch (productEXPTimeUnit) {

            case "年":

                calendar.add(Calendar.YEAR, productEXP);
//
//                if (productEXP >= 2) {
//
//                    calendar.add(Calendar.DATE, -90);
//
//                } else if (productEXP >= 1) {
//
//                    calendar.add(Calendar.DATE, -60);
//
//                }

            case "月":

                calendar.add(Calendar.MONTH, productEXP);
//
//                if (productEXP >= 24) {
//
//                    calendar.add(Calendar.DATE, -90);
//
//                } else if (productEXP >= 12) {
//
//                    calendar.add(Calendar.DATE, -60);
//
//                } else if (productEXP >= 6) {
//
//                    calendar.add(Calendar.DATE, -30);
//
//                } else {
//
//                    calendar.add(Calendar.DATE, -20);
//
//                }

            case "天":

                calendar.add(Calendar.DATE, productEXP);
//
//                if (productEXP >= 730) {
//
//                    calendar.add(Calendar.DATE, -90);
//
//                } else if (productEXP >= 365) {
//
//                    calendar.add(Calendar.DATE, -60);
//
//                } else if (productEXP >= 180) {
//
//                    calendar.add(Calendar.DATE, -30);
//
//                } else if (productEXP >= 90){
//
//                    calendar.add(Calendar.DATE, -20);
//
//                } else if (productEXP >= 30) {
//
//                    calendar.add(Calendar.DATE, -15);
//
//                } else if (productEXP >= 16) {
//
//                    calendar.add(Calendar.DATE, -5);
//
//                } else if (productEXP == 7) {
//
//                    calendar.add(Calendar.DATE, -2);
//
//                } else {
//
//                    calendar.add(Calendar.DATE, -4);
//
//                }

        }

        calendar.add(Calendar.DATE, -promotionOffset);

        return calendar.getTime();
    }

    private static DateScope getDateScope(long mlsEXP) {

        for (int i = 0; i < SettingActivity.dateSettingIndex.size(); i++){

            long lowerBoundIndex = SettingActivity.dateSettingIndex.get(i);

            if (mlsEXP >= lowerBoundIndex) {

                return SettingActivity.mlScopeMap.get(lowerBoundIndex);
            }
        }

        return null;
    }

    // 根据生产日期与保质期计算到期时间
    public static Date calculateProductExpireDate(Date productDOP, int productEXP,
                                                  String productEXPTimeUnit) {

        Calendar calendar = Calendar.getInstance();

        calendar.setTime(productDOP);

        calendar.add(Calendar.DATE, -1);

        long mlsIndex = SettingActivity.stringToMillionSeconds(String.valueOf(productEXP), productEXPTimeUnit);

        DateScope scope = getDateScope(mlsIndex);

        int expireOffset = Integer.parseInt(scope.getExpireOffsetValue());

        switch (productEXPTimeUnit) {

            case "年":

                calendar.add(Calendar.YEAR, productEXP);
                break;

            case "月":

                calendar.add(Calendar.MONTH, productEXP);
                break;

            case "天":

                calendar.add(Calendar.DATE, productEXP);
                break;

        }

        calendar.add(Calendar.DATE, -expireOffset);

        return calendar.getTime();
    }

    public static boolean isDateFormat(String after) {

        return true;

    }

    public static boolean compare(String newDate, Date oldDate) {

        Date nD;

        try {

            nD = typeMach(newDate);

        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }

        if (nD == null) {

            return oldDate != null;
        }

        return !nD.equals(oldDate);
    }

    public static void setCalender(Date productDOP, int index, String after) {

        Calendar calendar = Calendar.getInstance();

        calendar.setTime(productDOP);

        calendar.set(index, Integer.parseInt(after));

    }

}
