package com.shepherdboy.pdstreamline.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
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

public class DraggableLinearLayout extends LinearLayout {

    private LinearLayout currentTimestreamView;

    boolean horizontalDraggable;

    boolean verticalDraggable;

    float horizontalDistance = 0; // 控件的水平移动距离

    float verticalDistance = 0; // 控件的垂直移动距离

    public ViewDragHelper viewDragHelper;

    private static boolean layoutChanged = false;

    public DraggableLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);

        viewDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(@NonNull View child, int pointerId) {

                if (child instanceof LinearLayout) {

                    Set<Integer> timestreamIds = MyApplication.originalPositionHashMap.keySet();
                    return timestreamIds.contains(child.getId());
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

    public void putBack(View view){

        Point originalPoint = MyApplication.originalPositionHashMap.get(view.getId());

        viewDragHelper.settleCapturedViewAt(originalPoint.x, originalPoint.y);

        Timestream ts = MyApplication.onShowTimeStreamsHashMap.get(view.getId());
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

    }

    public LinearLayout getCurrentView(MotionEvent event) {

        View toCapture = viewDragHelper.findTopChildUnder((int)event.getX(), (int)event.getY());

        if (null != toCapture && toCapture instanceof LinearLayout) {

            LinearLayout currentTimeStreamView = (LinearLayout) toCapture;

            return currentTimeStreamView;

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

    private void init(Context context, AttributeSet attributeSet) {

        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.DraggableLinearLayout);
        horizontalDraggable = typedArray.getBoolean(R.styleable.DraggableLinearLayout_horizontalDraggable, true);
        verticalDraggable = typedArray.getBoolean(R.styleable.DraggableLinearLayout_verticalDraggable, true);
        MyApplication.activityIndex = typedArray.getInt(R.styleable.DraggableLinearLayout_activityIndex, 0);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {

            currentTimestreamView = getCurrentView(event);
            MyApplication.recordTimeStreamView();
        }


        if (currentTimestreamView != null ) {

            return viewDragHelper.shouldInterceptTouchEvent(event);
        }

        return super.onInterceptTouchEvent(event);
    }

    public static float dpToFloat(int dpInt, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dpInt,
                context.getResources().getDisplayMetrics());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {

            currentTimestreamView = getCurrentView(event);
        }

        if (currentTimestreamView != null ) {

            viewDragHelper.processTouchEvent(event);
            return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {

        if (viewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        MyApplication.recordTimeStreamView();

    }

    public static boolean isLayoutChanged() {
        return layoutChanged;
    }

    public static void setLayoutChanged(boolean layoutChanged) {
        DraggableLinearLayout.layoutChanged = layoutChanged;
    }
}
