package com.goofood.gofood.com.goofood.gofood.geolocation;

import android.content.Context;
import android.location.Location;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by Thanuj on 5/15/2016.
 */

public class MapWrapperLayout extends FrameLayout {

    public interface OnDragListener {
        public void onDrag(MotionEvent motionEvent);
    }

    private OnDragListener mOnDragListener;
    private Location location;

    public MapWrapperLayout(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mOnDragListener != null) {
            mOnDragListener.onDrag(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setOnDragListener(OnDragListener mOnDragListener) {
        this.mOnDragListener = mOnDragListener;
    }



}
