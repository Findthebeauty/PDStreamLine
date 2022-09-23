package com.shepherdboy.pdstreamline.view;

import static com.shepherdboy.pdstreamline.MyApplication.PD_INFO_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF;
import static com.shepherdboy.pdstreamline.MyApplication.activityIndex;
import static com.shepherdboy.pdstreamline.MyApplication.closableScrollView;
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
import com.shepherdboy.pdstreamline.activities.TraversalTimestreamActivity;

public class ClosableScrollView extends ScrollView {

    public static final int SCROLL_FROM_TOUCH = 100;
    public static final int SCROLL_FROM_RELOCATE = 101;
    //记录触摸位置的变化，用于判断是滑动还是长按
    private float newY;
    private float oldY;
    private float newX;
    private float oldX;

    private int originalY = 0;
    //滚动结束标志
    private boolean flingFinished = true;
    int[] location = new int[2];

    private Handler scrollHandler;

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

                View view = (View) msg.obj;
                switch (msg.what) {

                    case SCROLL_FROM_TOUCH:
                        view.getLocationOnScreen(location);
                        int dy = location[1];
                        ClosableScrollView.this.smoothScrollBy(0,
                                dy - view.getHeight() * 4 - originalY);
                        break;

                    case SCROLL_FROM_RELOCATE:
                        ScrollView scrollView = ClosableScrollView.this;
                        view.getLocationOnScreen(location);
                        scrollView.smoothScrollBy(0,
                                view.getTop() - scrollView.getScrollY());
                        break;

                    default:
                        break;

                }

            }
        };

        MyApplication.handlers.add(scrollHandler);
    }

    public static void postLocate(int what, Object obj) {

        switch (activityIndex) {

            case PD_INFO_ACTIVITY:
            case TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF:
                if (closableScrollView == null) return;
                Message msg = MyApplication.closableScrollView.scrollHandler.obtainMessage();
                msg.what = what;
                msg.obj = obj;
                MyApplication.closableScrollView.scrollHandler.sendMessage(msg);
                break;

            default:
                break;

        }
    }

    public float getDeltaX() {

        return Math.abs(newX - oldX);

    }

    public float getDeltaY() {

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
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {

        if (MyApplication.activityIndex == MyApplication.TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF) {

            TraversalTimestreamActivity.recordTopProduct(null);
        }
        super.onScrollChanged(l, t, oldl, oldt);
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

    public boolean isFlingFinished() {
        return flingFinished;
    }

    public int getOriginalY() {
        return originalY;
    }

    @Override
    public void fling(int velocityY) {
        super.fling(velocityY);

        if (draggableLinearLayout == null) return;
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

    public void setNewY(float newY) {
        this.newY = newY;
    }

    public void setOldY(float oldY) {
        this.oldY = oldY;
    }

    public void setNewX(float newX) {
        this.newX = newX;
    }

    public void setOldX(float oldX) {
        this.oldX = oldX;
    }

    public Handler getScrollHandler() {
        return scrollHandler;
    }
}
