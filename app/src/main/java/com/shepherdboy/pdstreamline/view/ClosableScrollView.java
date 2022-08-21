package com.shepherdboy.pdstreamline.view;

import static com.shepherdboy.pdstreamline.MyApplication.draggableLinearLayout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

public class ClosableScrollView extends ScrollView {

    //记录触摸位置的变化，用于判断是滑动还是长按
    private static float newY;
    private static float oldY;
    private static float newX;
    private static float oldX;
    //滚动结束标志
    private boolean scrollOver;

    public ClosableScrollView(Context context) {
        super(context);
    }

    public ClosableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClosableScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ClosableScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public static float getDeltaX() {

        return Math.abs(newX - oldX);

    }

    public static float getDeltaY() {

        return Math.abs(newY - oldY);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {

            scrollOver = true;
            newY = oldY = ev.getY();
            newX = oldX = ev.getX();

        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {


        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {

            newY = oldY = ev.getY();
            newX = oldX = ev.getX();

        }

        if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {

            newY = ev.getY();
            newX = ev.getX();

        }

        if (ev.getActionMasked() == MotionEvent.ACTION_UP) performClick();

        return super.onTouchEvent(ev);
    }

    /**
     * 判断是水平滑动还是竖直滑动
     * @return
     */
    public boolean intentToScrollView() {

        float deltaY = getDeltaY();
        float deltaX = getDeltaX();

        Log.d("onTouch", "dx:" + deltaX + ",dy:" + deltaY);

        float k = deltaY / deltaX;
        double radios = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        Log.d("onTouch", "k: " + k + ",radios: " + radios);

        return (!Float.isNaN(k)) && (k < Math.tan(Math.toRadians(40)));
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public boolean isScrollOver() {
        return scrollOver;
    }

    @Override
    public void fling(int velocityY) {
        super.fling(velocityY);

        scrollOver = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                View view = ((ScrollView)(draggableLinearLayout.getParent()));
                long l = view.getScrollY();
                long s = System.currentTimeMillis();
                long delta = 0;

                try {

                    do {

                        Thread.sleep(100);

                        delta = view.getScrollY() - l;
                        l = view.getScrollY();

                    } while (delta > 0);

                    scrollOver = true;
                } catch (Exception e){

                    e.printStackTrace();
                }
            }
        }).start();
    }

}
