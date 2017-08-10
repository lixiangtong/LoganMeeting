package com.logansoft.lubo.loganmeeting.utils;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by logansoft on 2017/8/10.
 */

public class DragListener implements View.OnTouchListener {
    private int lastX;
    private int lastY;
    private int screenWidth;
    private int screenHeight;

    public DragListener(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                int dx = (int) event.getRawX() - lastX;
                int dy = (int) event.getRawY() - lastY;

                int left = v.getLeft() + dx;
                int top = v.getTop() + dy;
                int right = v.getRight() + dx;
                int bottom = v.getBottom() + dy;
                if (left < 0) {
                    left = 0;
                    right = left + v.getWidth();
                }
                if (right > screenWidth) {
                    right = screenWidth;
                    left = right - v.getWidth();
                }
                if (top < 0) {
                    top = 0;
                    bottom = top + v.getHeight();
                }
                if (bottom > screenHeight) {
                    bottom = screenHeight;
                    top = bottom - v.getHeight();
                }
                v.layout(left, top, right, bottom);
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }
}
