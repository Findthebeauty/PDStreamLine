package com.shepherdboy.pdstreamline.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.shepherdboy.pdstreamline.MyApplication;

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


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            oldY = ev.getY();
            newY = ev.getY();
        }
        if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) newY = ev.getY();

        return !(MyApplication.draggableLinearLayout.isLongClicking()) && super.onInterceptTouchEvent(ev);
    }



}
