package com.github.nicksetzer.daedalus.audio;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;

/**
 * This class prevents audio from being stalled when the app is in the background
 */
public class AudioWebView extends WebView {

    public AudioWebView(Context context) {
        super(context);
    }

    public AudioWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AudioWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility != View.GONE) {
            super.onWindowVisibilityChanged(View.VISIBLE);
        }
    }

}
