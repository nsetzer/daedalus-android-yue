package com.github.nicksetzer.daedalus.javascript;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.JavascriptInterface;

public class LocalStorage {
    private Activity m_activity;

    public LocalStorage(Activity activity) {
        this.m_activity = activity;
    }

    @JavascriptInterface
    public String getItem(String key) {
        SharedPreferences prefs = this.m_activity.getPreferences(Context.MODE_PRIVATE);
        return prefs.getString(key, "");
    }

    @JavascriptInterface
    public void setItem(String key, String value) {
        SharedPreferences prefs = this.m_activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    @JavascriptInterface
    public void removeItem(String key) {
        SharedPreferences prefs = this.m_activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        editor.commit();
    }
}
