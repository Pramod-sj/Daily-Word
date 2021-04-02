package com.pramod.dailyword.framework.ui.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

public class CustomNestedScrollView extends NestedScrollView {

    private boolean scrollEnabled = true;

    public void enableScroll(boolean enabled) {
        scrollEnabled = enabled;
    }

    public boolean isScrollEnabled() {
        return scrollEnabled;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (isScrollEnabled())
            return super.onInterceptTouchEvent(e);
        return false;
    }

    public CustomNestedScrollView(Context context) {
        super(context);
    }

    public CustomNestedScrollView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomNestedScrollView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}