package com.shepherdboy.pdstreamline.utils;

import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.activities.SettingActivity;
import com.shepherdboy.pdstreamline.activities.TraversalTimestreamActivity;
import com.shepherdboy.pdstreamline.beans.DateScope;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.beans.Shelf;
import com.shepherdboy.pdstreamline.beans.Timestream;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 来自21世纪的AI输入器，自动填写缺少的信息
 */
public class AIInputter {

    public static ArrayList<String> essentialProductInfoList = new ArrayList<String>();
    public static ArrayList<String> essentialTimestreamInfoList = new ArrayList<String>();
    public static LinkedHashMap<String, EditText> blanksToFillQueue = new LinkedHashMap<>();
    public static LinkedList<Long> inputInterval = new LinkedList<>();

    static {

        //Todo 获取用户配置
        essentialProductInfoList.add("productName");
        essentialProductInfoList.add("productEXP");

        essentialTimestreamInfoList.add("productDOP");
    }

    public static void fillTheEssentialInfo(Product product) {

        Iterator<Map.Entry<String, EditText>> iterator;
        Map.Entry<String, EditText> entry;

        while(!blanksToFillQueue.isEmpty()) {

            iterator = blanksToFillQueue.entrySet().iterator();
            entry = iterator.next();
            iterator.remove();

            Map.Entry<String, EditText> finalEntry = entry;
            Thread  fillBlankThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    fillTheBlank(product, finalEntry.getKey(), finalEntry.getValue());

                }
            });

            fillBlankThread.start();

            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }






        }

    }

    public static Date getNextCheckDate(String productEXP, String productEXPTimeUnit) {

        Date DOP = getProductDOP(productEXP, productEXPTimeUnit);

        return DateUtil.calculatePromotionDate(DOP, Integer.parseInt(productEXP), productEXPTimeUnit);

    }

    public static Date getProductDOP(@Nullable String productEXP, String productEXPTimeUnit) {

        if (productEXP == null || "7" == productEXP) {


            return DateUtil.getStartPointToday();

        }

        // 7天以上保质期的按当前时间往前推33%保质期时间为预设生产日期
        else {

            return DateUtil.getMidnightDate(getPossibleDOP(productEXP, productEXPTimeUnit));

        }

    }

    // 根据当前时间和保质期推断生产日期
    public static Date getPossibleDOP(String productEXP, String productEXPTimeUnit) {

        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date());

        if ("年".equals(productEXPTimeUnit)) {
            calendar.add(calendar.DATE, -(int) ((Integer.parseInt(productEXP) * 366) / 3));
        } else if ("月".equals(productEXPTimeUnit)) {
            calendar.add(calendar.DATE, -(int) ((Integer.parseInt(productEXP) * 31) / 3));
        } else {
            calendar.add(calendar.DATE, -(int) (Integer.parseInt(productEXP) / 4));
        }

        Date result = DateUtil.getMidnightDate(calendar.getTime());

        return result;
    }


    // TODO 获取可能的库存
    public static int getInventory(String productCode) {

        return 0;

    }

    // TODO 按顺序生成坐标
    public static String getCoordinate(String productCode) {

        return "0000";

    }

    public static Date[] getPossibleTimeStream(String productEXP, String productEXPTimeUnit) {

        Date[] timeStream = new Date[3];

        timeStream[0] = getPossibleDOP(productEXP, productEXPTimeUnit);
        timeStream[1] = DateUtil.calculatePromotionDate(timeStream[0],
                Integer.parseInt(productEXP),
                productEXPTimeUnit);
        timeStream[2] = DateUtil.calculateProductExpireDate(timeStream[0],
                Integer.parseInt(productEXP),
                productEXPTimeUnit);

        return timeStream;

    }

    public static void fillTheBlanks(Product product, Timestream temp) {

        temp.setProductCode(product.getProductCode());
        temp.setProductName(product.getProductName());
        Date[] timestream = getPossibleTimeStream(product.getProductEXP(),
                product.getProductEXPTimeUnit());
        temp.setProductDOP(timestream[0]);
        temp.setProductPromotionDate(timestream[1]);
        temp.setProductExpireDate(timestream[2]);
        temp.setProductInventory(String.valueOf(getInventory(product.getProductCode())));
        temp.setProductCoordinate(getCoordinate(product.getProductCode()));
        temp.setUpdated(false);

    }

    public static void fillTheBlanks(Product product) {

        product.setProductName("正在从中心服务器查询。。。");

        DateScope scope = SettingActivity.mlScopeMap.get(SettingActivity.dateSettingIndex.get(SettingActivity.dateSettingIndex.size() - 1));
        product.setProductEXP(scope.getRangeValue());
        product.setProductSpec("spec");
        product.setProductEXPTimeUnit("天");

    }

    // 根据传入的字段名填入可能的信息
    public static void fillTheBlank(Product product, String filedName, EditText editText) {

//        MyDragLayout.setSynchronizeLock(false);

        switch (filedName) {

            case "productName":
            case "productEXP":
            case "productDOP":

//                MyDragLayout.setFocus(editText);

            default:
                break;

        }

    }



    public static boolean validate(String information, Timestream timestream, int fieldIndex) {

        Product product = MyApplication.getCurrentProduct(timestream);

        switch (fieldIndex) {

            case MyApplication.PRODUCT_NAME:

                return !information.equals(product.getProductName());

            case MyApplication.PRODUCT_EXP:

                try {

                    int EXP = Integer.parseInt(information);
                    int preEXP = Integer.parseInt(product.getProductEXP());

                    if (EXP <= 0) {

                        return false;
                    }

                    long mls = SettingActivity.stringToMillionSeconds(information,
                            product.getProductEXPTimeUnit());

                    long minMls = SettingActivity.dateSettingIndex.get(SettingActivity.dateSettingIndex.size() - 1);

                    if (mls < minMls) return false;

                    return EXP != preEXP;

                } catch (Exception e) {

                    return false;
                }

            case MyApplication.TIMESTREAM_DOP:

                switch (information.length()) {

                    case 1: return !information.equals("0");
                    case 3: return !information.startsWith("0");
                    case 4:
                            String reg = "((0[1-9])|(1[0-2]))(" +
                                    "(0[1-9])|" +
                                    "([1-2][0-9])|" +
                                    "(3[0-1]))";
                            return information.matches(reg);
                    default: return true;
                }
            case MyApplication.TIMESTREAM_COORDINATE:

                return !information.equals(timestream.getProductCoordinate());

            case MyApplication.TIMESTREAM_INVENTORY:

                String reg = "^[-\\+]?[\\d]*$";

                if (information.matches(reg)) {

                    int oldInventory = Integer.parseInt(timestream.getProductInventory());
                    int newInventory = oldInventory;

                    try {

                        newInventory = Integer.parseInt(information);

                    } catch (Exception e) {

                        e.printStackTrace();

                        if (information.equals("")) {
                            newInventory = 0;
                        }
                    }
                    return oldInventory != newInventory;

                } else {

                    return false;
                }

            default:
                return true;

        }

    }

    public static String translate(String after) {

        Date date = DateUtil.getStartPointToday();

        return translate(null, date, after);
    }

    public static String translate(Product product, Date productDOP, String after) {

        if (productDOP == null) {

            productDOP = getPossibleDOP(product.getProductEXP(), product.getProductEXPTimeUnit());
        }

        after = after.trim();
        String result = DateUtil.shortNormalFormat.format(productDOP);

        if (DateUtil.isDateFormat(after)) {

            if (after.length() == 3) {

                after = analysisAndDecorate(product, productDOP, after);

            }

            try {

                result = DateUtil.parseDate(productDOP, after);

                Date DOP = DateUtil.typeMach(result + " 00:00:00");

                if (DOP.after(DateUtil.getStartPointToday())) {

                    DOP = DateUtil.getLastYear(DOP);

                    result = DateUtil.typeMach(DOP).substring(0,10);

                }

            } catch (ParseException e) {

                e.printStackTrace();
                return after;
            }

//            ArrayList<Date> possibleDOPs = new ArrayList<>();
//
//            if (after.length() == 1) {
//
//                DateUtil.setCalender(productDOP, Calendar.DATE, after);
//
//                possibleDOPs.add(productDOP);
//                possibleDOPs.add(DateUtil.getLastMonth(productDOP));
//                possibleDOPs.add(DateUtil.getLastYear(productDOP));
//
//            }

        }

        return result;
    }

    // todo 3个数字的日期对应的具体日期推断
    private static String analysisAndDecorate(@Nullable Product product, Date productDOP, String after) {

        return "0" + after;

    }
    // todo 3个数字的日期对应的具体日期推断
    private static String analysisAndDecorate(Date productDOP, String after) {

        return "0" + after;

    }

    public static boolean validate(DateScope scope, int index, String after) {

        long upperBoundMls = 0;
        long lowerBoundMls = 0;
        long newBoundMls = 0;
        long promotionOffsetMls = 0;
        long expireOffsetMls = 0;

        DateScope newScope = null;

        if (scope != null) {

            upperBoundMls = SettingActivity.getUpperBoundMls(scope);
            lowerBoundMls = SettingActivity.getLowerBoundMls(scope);
            newScope = new DateScope(scope);
            newBoundMls = SettingActivity.stringToMillionSeconds(
                    newScope.getRangeValue(), newScope.getRangeUnit()
            );
            promotionOffsetMls = SettingActivity.stringToMillionSeconds(
                    newScope.getPromotionOffsetValue(),
                    newScope.getPromotionOffsetUnit()
            );
            expireOffsetMls = SettingActivity.stringToMillionSeconds(
                    newScope.getExpireOffsetValue(),
                    newScope.getExpireOffsetUnit()
            );

        }

        switch (index) {

            case SettingActivity.DATE_SCOPE_RANGE_VALUE:

                newScope.setRangeValue(after);
                newBoundMls = SettingActivity.stringToMillionSeconds(
                        newScope.getRangeValue(), newScope.getRangeUnit()
                );
                return newBoundMls > lowerBoundMls && newBoundMls < upperBoundMls;

            case SettingActivity.DATE_SCOPE_RANGE_UNIT:

                newScope.setRangeUnit(after);
                newBoundMls = SettingActivity.stringToMillionSeconds(
                        newScope.getRangeValue(), newScope.getRangeUnit()
                );
                return newBoundMls > lowerBoundMls && newBoundMls < upperBoundMls;

            case SettingActivity.DATE_SCOPE_PROMOTION_OFFSET_VALUE:

                newScope.setPromotionOffsetValue(after);
                promotionOffsetMls = SettingActivity.stringToMillionSeconds(
                        newScope.getPromotionOffsetValue(),
                        newScope.getPromotionOffsetUnit()
                );
               return promotionOffsetMls < newBoundMls && promotionOffsetMls > expireOffsetMls;

            case SettingActivity.DATE_SCOPE_EXPIRE_OFFSET_VALUE:

                newScope.setExpireOffsetValue(after);
                expireOffsetMls = SettingActivity.stringToMillionSeconds(
                        newScope.getExpireOffsetValue(),
                        newScope.getExpireOffsetUnit()
                );
                return expireOffsetMls > 0 && expireOffsetMls < promotionOffsetMls;

            default:
                return true;
        }
    }

    public static void recordInputTimeInterval(long interval) {

        Log.d("I'm in!", interval + "");

        if (inputInterval.size() == 10) inputInterval.removeFirst();

        inputInterval.add(interval);

    }

    public static long getAutoCommitInterval() {

        if ((!SettingActivity.settingInstance.isAutoCommitFlag()) || inputInterval.size() < 1) {

            inputInterval.add(1234L);
            return 1234;
        }

        Collections.sort(inputInterval);

        long sum = 0;

        for (Long l : inputInterval) {

            sum += l;
        }

        return sum / inputInterval.size();
    }

    public static boolean isNumeric(String str) {

        for (int i = 0; i < str.length(); i++) {

            if (!Character.isDigit(str.charAt(i)))
            return false;
        }

        return true;
    }

    public static boolean validate(String currentInf, Shelf shelf, int filedIndex) {

        switch (filedIndex) {

            case MyApplication.SHELF_NAME:

                for (Shelf temp : TraversalTimestreamActivity.getShelfList()) {

                    if (temp.getName().equals(currentInf)) {

                        Toast.makeText(MyApplication.getMyApplicationContext(), "货架名称已存在",Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                return true;

            case MyApplication.SHELF_CLASSIFY:

                if (!(currentInf.length() > 0 && currentInf.length() < 8)) {

                    Toast.makeText(MyApplication.getMyApplicationContext(), "类别不合法请输入1-7位的类别名称", Toast.LENGTH_SHORT).show();

                    return false;
                }

                if (TraversalTimestreamActivity.getClassifyList().contains(currentInf)) {

                    Toast.makeText(MyApplication.getMyApplicationContext(), "类别名称已存在", Toast.LENGTH_SHORT).show();
                    return false;
                }

                return true;

            case MyApplication.SHELF_MAX_ROW:

                return !currentInf.equals("");

            default:
                return true;

        }
    }


}
