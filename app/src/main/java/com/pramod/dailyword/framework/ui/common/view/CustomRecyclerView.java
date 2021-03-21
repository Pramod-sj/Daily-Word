package com.pramod.dailyword.framework.ui.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class CustomRecyclerView extends RecyclerView {

    private boolean scrollEnabled = true;

    public void enableScroll(boolean enabled) {
        scrollEnabled = enabled;
    }

    public boolean isScrollEnabled() {
        return scrollEnabled;
    }

    @Override
    public int computeVerticalScrollRange() {

        if (isScrollEnabled())
            return super.computeVerticalScrollRange();
        return 0;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if(isScrollEnabled())
            return super.onInterceptTouchEvent(e);
        return false;
    }

    public CustomRecyclerView(Context context) {
        super(context);
    }

    public CustomRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}