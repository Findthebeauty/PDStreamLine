package com.shepherdboy.pdstreamline.view;

import android.view.MotionEvent;

/**
 * 对touchEvent坐标计算，判断手势的意图然
 */
public class TouchEventDispatcher {

    private static boolean intentToScroll; //竖直滑动flag
    private static boolean directionConfirmed; //确认手势意图flag

    static float startX;
    static float startY;

    private TouchEventDispatcher() {
    }

    /**
     * 判断是否是指定角度范围内滑动
     * @return true表示滑动范围小于给的角度，false大于
     * @param deltaX
     * @param deltaY
     * @param degree 指定角度
     */
    public static boolean validateDragRange(float deltaX, float deltaY, double degree) {

        float k = deltaY / deltaX;
        double radios = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        return radios == 0 || (k < Math.tan(Math.toRadians(degree)) && radios < 50);
    }

    public static void onTouchEvent(ClosableScrollView scrollView, MotionEvent ev) {

        if (ev.getActionMasked() == MotionEvent.ACTION_UP || ev.getActionMasked() == MotionEvent.ACTION_DOWN )
            directionConfirmed = false;

        float dx = scrollView.getDeltaX();
        float dy = scrollView.getDeltaY();

        if (dx + dy > 0) {

            directionConfirmed = true;
            intentToScroll = !validateDragRange(dx, dy, 40d);
        }
    }

    public static boolean isIntentToScroll() {

        return directionConfirmed && intentToScroll;
    }

}
