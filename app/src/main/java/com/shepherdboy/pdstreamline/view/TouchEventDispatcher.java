package com.shepherdboy.pdstreamline.view;

import android.util.Log;
import android.view.MotionEvent;

/**
 * 对touchEvent进行缓存，判断手势的意图然后将事件分发到指定的view进行处理
 */
public class TouchEventDispatcher {

    private static boolean intentToScroll;
    private static boolean directionConfirmed;


    private TouchEventDispatcher() {
    }

    /**
     * 判断是水平滑动还是竖直滑动
     * @return true表示竖直滑动，false表示水平滑动
     * @param deltaY
     * @param deltaX
     */
    public static boolean intentToScrollView(float deltaX, float deltaY) {

        float k = deltaY / deltaX;
        double radios = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        Log.d("intentToScrollView", "k: " + k + ",radios: " + radios);

        return k < Math.tan(Math.toRadians(40)) && radios > 0 && radios < 30;
    }

    public static void onTouchEvent(MotionEvent ev) {

        if (!directionConfirmed) {

            float startX = 0;
            float startY = 0;

            if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {

                startX = ev.getX();
                startY = ev.getY();

            } else {

                float dx = Math.abs(ev.getX() - startX);
                float dy = Math.abs(ev.getY() - startY);

                if (startX + startY > 0 && dx + dy > 0) {

                    directionConfirmed = true;
                    intentToScroll = intentToScrollView(dx, dy);
                    Log.d("TouchEventDispatcher", ev.toString());
                }
            }
        }
    }

    public static boolean directionConfirmed() {

        return directionConfirmed;
    }

    public static boolean isIntentToScroll() {

        return intentToScroll;
    }

}
