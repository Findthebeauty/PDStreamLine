package com.shepherdboy.pdstreamline.view;

import static com.shepherdboy.pdstreamline.MyApplication.draggableLinearLayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import com.shepherdboy.pdstreamline.MyApplication;

public class ClosableScrollView extends ScrollView {

    //记录触摸位置的变化，用于判断是滑动还是长按 todo 重写一个Draggable scrollView
    private static float newY;
    private static float oldY;
    private static float newX;
    private static float oldX;
    //滚动结束标志
    private static boolean flingFinished;

    private GestureDetector mGestureDetector;
    View.OnTouchListener mGestureListener;

    public ClosableScrollView(Context context) {
        super(context);
    }

    public ClosableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

//        mGestureDetector = new GestureDetector(new YScrollDetector());
//        setFadingEdgeLength(0);
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
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

//        if (draggableLinearLayout.isDragging() || draggableLinearLayout.isLongClicking()) return false;
//
//        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
//
//            newY = oldY = ev.getY();
//            newX = oldX = ev.getX();
//        }
//
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

    @Override
    public void fling(int velocityY) {
        super.fling(velocityY);

        flingFinished = false;

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

                    flingFinished = true;
                } catch (Exception e){

                    e.printStackTrace();
                }
            }
        }).start();
    }

    class YScrollDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            return TouchEventDispatcher.intentToScrollView(distanceX, distanceY);

        }
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

}
