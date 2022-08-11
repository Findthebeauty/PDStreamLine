package com.shepherdboy.pdstreamline.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.shepherdboy.pdstreamline.MyApplication;

public class ClosableScrollView extends ScrollView {

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

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return !(MyApplication.draggableLinearLayout.isLongClicking()) && super.onInterceptTouchEvent(ev);
    }

}
