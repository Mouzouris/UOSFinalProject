package com.example.myapplication.Services;

import android.content.Context;
import android.util.AttributeSet;

import com.jjoe64.graphview.GraphView;

public class CustomGraphView extends GraphView {
    public CustomGraphView(Context context) {
        super(context);
    }
    public CustomGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public CustomGraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public void init() {
        super.init();
// set the custom style
    }
}
