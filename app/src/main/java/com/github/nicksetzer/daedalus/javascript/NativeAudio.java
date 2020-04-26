package com.github.nicksetzer.daedalus.javascript;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.JavascriptInterface;

import com.github.nicksetzer.daedalus.AudioService;
import com.github.nicksetzer.daedalus.WebActivity;
import com.github.nicksetzer.daedalus.audio.AudioActions;


public class NativeAudio {
    WebActivity m_activity;

    public NativeAudio(WebActivity activity) {
        this.m_activity = activity;
    }

    @JavascriptInterface
    public void startService() {
        //Intent intent = new Intent(m_activity, AudioService.class);

        //m_activity.startForegroundService(intent);
    }

    @JavascriptInterface
    public void stopService() {
        Intent intent = new Intent(m_activity, AudioService.class);
        m_activity.stopService(intent);
        return;
    }

    @JavascriptInterface
    public void setQueue(String jsonQueue) {
        Intent intent = new Intent(m_activity, AudioService.class);
        intent.setAction(AudioActions.ACTION_SET_QUEUE);
        intent.putExtra("data", jsonQueue);
        m_activity.startForegroundService(intent);
    }

    @JavascriptInterface
    public void updateQueue(int currentIndex, String jsonQueue) {
        Intent intent = new Intent(m_activity, AudioService.class);
        intent.setAction(AudioActions.ACTION_UPDATE_QUEUE);
        intent.putExtra("data", jsonQueue);
        intent.putExtra("index", currentIndex);
        m_activity.startForegroundService(intent);
    }

    @JavascriptInterface
    public void loadUrl(String url) {
        Intent intent = new Intent(m_activity, AudioService.class);
        intent.setAction(AudioActions.ACTION_LOAD_URL);
        intent.putExtra("url", url);
        m_activity.startForegroundService(intent);
    }

    @JavascriptInterface
    public void loadIndex(int index) {
        Intent intent = new Intent(m_activity, AudioService.class);
        intent.setAction(AudioActions.ACTION_LOAD_INDEX);
        intent.putExtra("index", index);
        m_activity.startForegroundService(intent);
    }

    @JavascriptInterface
    public void play() {
        Intent intent = new Intent(m_activity, AudioService.class);
        intent.setAction(AudioActions.ACTION_PLAY);
        m_activity.startForegroundService(intent);
    }

    @JavascriptInterface
    public void pause() {
        Intent intent = new Intent(m_activity, AudioService.class);
        intent.setAction(AudioActions.ACTION_PAUSE);
        m_activity.startForegroundService(intent);
    }

    @JavascriptInterface
    public void stop() {
        Intent intent = new Intent(m_activity, AudioService.class);
        intent.setAction(AudioActions.ACTION_STOP);
        m_activity.startForegroundService(intent);
    }

    @JavascriptInterface
    public void skipToNext() {
        Intent intent = new Intent(m_activity, AudioService.class);
        intent.setAction(AudioActions.ACTION_SKIPTONEXT);
        m_activity.startForegroundService(intent);
    }

    @JavascriptInterface
    public void skipToPrev() {
        Intent intent = new Intent(m_activity, AudioService.class);
        intent.setAction(AudioActions.ACTION_SKIPTOPREV);
        m_activity.startForegroundService(intent);
    }

    @JavascriptInterface
    public void seekms(long pos) {
        Intent intent = new Intent(m_activity, AudioService.class);
        intent.setAction(AudioActions.ACTION_SEEK);
        m_activity.startForegroundService(intent);
    }
}
