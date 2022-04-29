package com.shepherdboy.pdstreamline.view;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

public class MultipleClickListener implements View.OnTouchListener {


    private static int clickTimeInterval = 400;

    private int clickCount = 0;

    private Handler handler;

    private MyClickCallBack myClickCallBack;



    public interface MyClickCallBack {

        void oneClick();

        void doubleClick();

        void trebleClick();

    }

    public MultipleClickListener(MyClickCallBack myClickCallBack) {

        this.myClickCallBack = myClickCallBack;

        handler = new Handler();

    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            clickCount++;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (clickCount == 1) {

                        myClickCallBack.oneClick();

                    }
                    if (clickCount == 2) {

                        myClickCallBack.doubleClick();

                    }
                    if (clickCount >= 3) {

                        myClickCallBack.trebleClick();

                    }

                    handler.removeCallbacksAndMessages(null);

                    clickCount = 0;
                }
            }, clickTimeInterval);

        }

        return false;

    }
}
