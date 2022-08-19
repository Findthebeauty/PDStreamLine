package com.shepherdboy.pdstreamline.view;

import static com.shepherdboy.pdstreamline.MyApplication.draggableLinearLayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class ClosableScrollView extends ScrollView {

    //记录触摸位置的变化，用于判断是滑动还是长按
    private static float newY;
    private static float oldY;

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

    public static float getOldY() {
        return oldY;
    }

    public static float getNewY() {
        return newY;
    }

    public static float getDeltaY() {

        return Math.abs(newY - oldY);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            oldY = ev.getY();
            newY = ev.getY();
        }

        if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {

            if (draggableLinearLayout != null && draggableLinearLayout.getHorizontalDistance() > getDeltaY()) {

                return false;
            }

        }


        if (draggableLinearLayout != null && draggableLinearLayout.isLongClicking()) {

            return false;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        boolean consumed = draggableLinearLayout.onTouchEvent(ev);

        if (draggableLinearLayout.viewDragHelper.continueSettling(true)) return false;

        if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {

            newY = ev.getY();

            if (draggableLinearLayout != null && draggableLinearLayout.isLongClicking()) {

                return consumed;
            }

        }

        if (ev.getActionMasked() == MotionEvent.ACTION_UP) performClick();
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public void fling(int velocityY) {
        super.fling(velocityY);
    }
}
