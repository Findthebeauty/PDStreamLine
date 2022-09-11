package com.shepherdboy.pdstreamline.view;

import static com.shepherdboy.pdstreamline.MyApplication.draggableLinearLayout;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import androidx.annotation.NonNull;

import com.shepherdboy.pdstreamline.MyApplication;

public class ClosableScrollView extends ScrollView {

    //记录触摸位置的变化，用于判断是滑动还是长按
    private static float newY;
    private static float oldY;
    private static float newX;
    private static float oldX;

    private static int originalY = 0;
    //滚动结束标志
    private static boolean flingFinished = true;
    int[] location = new int[2];

    private static Handler scrollHandler;

    public ClosableScrollView(Context context) {
        super(context);
        init();
    }

    public ClosableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        this.setScrollbarFadingEnabled(false);

        if (scrollHandler != null) {

            MyApplication.handlers.remove(scrollHandler);
            scrollHandler.removeCallbacksAndMessages(null);

        }

        scrollHandler = new Handler() {

            @Override
            public void handleMessage(@NonNull Message msg) {

                int dy = msg.arg1;

                ScrollView view = ClosableScrollView.this;

                view.smoothScrollBy(0, dy - originalY);
            }
        };

        MyApplication.handlers.add(scrollHandler);

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

            newY = oldY = ev.getRawY();
            newX = oldX = ev.getRawX();
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);


        if (originalY != 0) return;

        getLocationOnScreen(location);
        originalY = location[1];
        location = null;

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {

            newY = oldY = ev.getRawY();
            newX = oldX = ev.getRawX();


        }

        if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {

            newY = ev.getRawY();
            newX = ev.getRawX();

        }

        if (ev.getActionMasked() == MotionEvent.ACTION_UP) {

            MyApplication.stopCountPressTime();
            performClick();
        }


        return super.onTouchEvent(ev);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public static boolean isFlingFinished() {
        return flingFinished;
    }

    public static int getOriginalY() {
        return originalY;
    }

    @Override
    public void fling(int velocityY) {
        super.fling(velocityY);

        flingFinished = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                View view = ((ScrollView)(draggableLinearLayout.getParent()));
                long l = view.getScrollY();
                long delta;
                try {

                    do {

                        Thread.sleep(100);

                        delta = view.getScrollY() - l;
                        l = view.getScrollY();

                    } while (delta > 0);

                    flingFinished = true;
                } catch (Exception e){

                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void setNewY(float newY) {
        ClosableScrollView.newY = newY;
    }

    public static void setOldY(float oldY) {
        ClosableScrollView.oldY = oldY;
    }

    public static void setNewX(float newX) {
        ClosableScrollView.newX = newX;
    }

    public static void setOldX(float oldX) {
        ClosableScrollView.oldX = oldX;
    }

    public static Handler getScrollHandler() {
        return scrollHandler;
    }
}
