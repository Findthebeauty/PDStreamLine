package com.shepherdboy.pdstreamline.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.customview.widget.ViewDragHelper;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.beans.Timestream;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class DraggableLinearLayout extends LinearLayout {

    private LinearLayout currentTimestreamView;

    final boolean initHorizontalDraggable;

    final boolean initVerticalDraggable;

    boolean horizontalDraggable;

    boolean verticalDraggable;

    private boolean longClicking;

    float horizontalDistance = 0; // 控件的水平移动距离

    float verticalDistance = 0; // 控件的垂直移动距离

    public ViewDragHelper viewDragHelper;

    private static boolean layoutChanged = false;

    public DraggableLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DraggableLinearLayout);
        initHorizontalDraggable = typedArray.getBoolean(R.styleable.DraggableLinearLayout_horizontalDraggable, true);
        initVerticalDraggable = typedArray.getBoolean(R.styleable.DraggableLinearLayout_verticalDraggable, true);

        init();

        viewDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(@NonNull View child, int pointerId) {

                if (child instanceof LinearLayout) {

                    Set<Integer> timestreamIds = MyApplication.originalPositionHashMap.keySet();
                    return timestreamIds.contains(child.getId()) || MyApplication.activityIndex == MyApplication.PROMOTION_TIMESTREAM_ACTIVITY;
                }

                return false;
            }

            @Override
            public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {

                if (horizontalDraggable) {

                    return left;
                }
                return child.getLeft();
            }

            @Override
            public int clampViewPositionVertical(@NonNull View child, int top, int dy) {

                if (verticalDraggable) {

                    return top;
                }
                return child.getTop();
            }

            @Override
            public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {


                releasedChild = currentTimestreamView;

                MyApplication.onTimestreamViewReleased(releasedChild, horizontalDistance, verticalDistance, xvel, yvel);

                invalidate();

                horizontalDistance = 0;
                verticalDistance = 0;

            }

            @Override
            public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);

                changedView = currentTimestreamView;

                if (changedView == null) {
                    return;
                }

                Point originalPoint = MyApplication.originalPositionHashMap.get(currentTimestreamView.getId());

                if (null != originalPoint) {

                    horizontalDistance = changedView.getX() - originalPoint.x;
                    verticalDistance = changedView.getY() - originalPoint.y;

                }

                MyApplication.onViewPositionChanged(changedView, horizontalDistance, verticalDistance);

            }

            @Override
            public int getViewHorizontalDragRange(@NonNull View child) {

                return getMeasuredWidth() - child.getMeasuredWidth();

            }

            @Override
            public int getViewVerticalDragRange(@NonNull View child) {

                return getMeasuredHeight() - child.getMeasuredHeight();

            }



        });


    }

    public float getHorizontalDistance() {
        return Math.abs(horizontalDistance);
    }

    public void setVerticalDraggable(boolean verticalDraggable) {
        this.verticalDraggable = verticalDraggable;
    }

    public void setHorizontalDraggable(boolean horizontalDraggable) {
        this.horizontalDraggable = horizontalDraggable;
    }

    public void putBack(View view){

        Point originalPoint = MyApplication.originalPositionHashMap.get(view.getId());

        viewDragHelper.settleCapturedViewAt(originalPoint.x, originalPoint.y);

        Timestream ts = (Timestream) MyApplication.onShowTimeStreamsHashMap.get(view.getId());
        MyApplication.setTimeStreamViewOriginalBackgroundColor(ts);

        currentTimestreamView.invalidate();
    }

    // 设置view的字体
    public void setTextSiz(View view, int dpSze) {


        if (view instanceof EditText) {

            ((EditText) view).setTextSize(TypedValue.COMPLEX_UNIT_DIP, dpSze);

        }

        if (view instanceof TextView) {

            ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_DIP, dpSze);

        }

        if (view instanceof Button) {

            ((Button) view).setTextSize(TypedValue.COMPLEX_UNIT_DIP, dpSze);

        }


    }

    public static void setFocus(EditText editText) {

        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        editText.requestFocusFromTouch();
        editText.selectAll();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

            InputMethodManager m = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            m.showSoftInput(editText, 0);
            }
        },200); //todo 关于软键盘弹出edittext内容消失的问题

    }

    public LinearLayout getCurrentView(MotionEvent event) {

        View toCapture = viewDragHelper.findTopChildUnder((int)event.getX(), (int)event.getY());

        if (toCapture instanceof LinearLayout &&
                MyApplication.originalPositionHashMap.containsKey(toCapture.getId())) {

            currentTimestreamView = (LinearLayout) toCapture;

            return (LinearLayout) toCapture;

        }

        return null;
    }

    private int getOriginalBackgroundColor(LinearLayout currentTimeStreamView) {

        try {

            return ((ColorDrawable) currentTimeStreamView.getBackground()).getColor();

        } catch (Exception e) {

        }

        return 0;
    }

    private void init() {

        this.horizontalDraggable = initHorizontalDraggable;
        this.verticalDraggable = initVerticalDraggable;

    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        initTouch(event);

        if (currentTimestreamView != null) {

            return viewDragHelper.shouldInterceptTouchEvent(event);
        }

        return super.onInterceptTouchEvent(event);
    }

    public void initTouch(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP) init();

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {

            getCurrentView(event);
            MyApplication.recordDraggableView();
        }
    }

    public static float dpToFloat(int dpInt, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dpInt,
                context.getResources().getDisplayMetrics());
    }

    public static int dpToPx(int dpInt, WindowManager windowManager) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        return (dpInt * displayMetrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;
    }

    public static DraggableLinearLayout getContainer(View child) {

        if (child instanceof DraggableLinearLayout) return (DraggableLinearLayout) child;
        return getContainer((View) child.getParent());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getActionMasked() == MotionEvent.ACTION_UP) init();

        if (viewDragHelper.continueSettling(true)) return false;

        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            init();
            performClick();

        }

        if (MyApplication.tryCaptureClickEvent(event)) return true;

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {

            getCurrentView(event);
        }

        if (currentTimestreamView != null) {

            viewDragHelper.processTouchEvent(event);
            return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {

        super.computeScroll();

        if (viewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        MyApplication.recordDraggableView();

    }

    public boolean isLongClicking() {
        return longClicking;
    }

    public void setLongClicking(boolean longClicking) {
        this.longClicking = longClicking;
    }

    public static boolean isLayoutChanged() {
        return layoutChanged;
    }

    public static void setLayoutChanged(boolean layoutChanged) {
        DraggableLinearLayout.layoutChanged = layoutChanged;
    }
}
