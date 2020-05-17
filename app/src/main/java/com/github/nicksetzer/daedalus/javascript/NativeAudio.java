package com.github.nicksetzer.daedalus.javascript;

import android.content.Intent;
import android.webkit.JavascriptInterface;

import com.github.nicksetzer.daedalus.audio.AudioService;
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
    public String getQueue() {
        AudioService service = m_activity.getBoundService();
        if (service != null) {
            return service.mediaQueueData();
        }
        return "";
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
        intent.putExtra("position", pos);
        m_activity.startForegroundService(intent);
    }

    @JavascriptInterface
    public boolean isPlaying() {

        AudioService service = m_activity.getBoundService();

        if (service != null) {
            return service.mediaIsPlaying();
        }

        return false;
    }

    @JavascriptInterface
    public void beginFetch(String token) {
        Intent intent = new Intent(m_activity, AudioService.class);
        intent.setAction(AudioActions.ACTION_FETCH);
        intent.putExtra("token", token);
        m_activity.startForegroundService(intent);
    }

    @JavascriptInterface
    public String buildForest(String query, int syncState) {
        String forest = m_activity.getBoundService().mediaBuildForest(query, syncState);
        return forest;
    }

    @JavascriptInterface
    public void updateSyncStatus(String payload) {
        Intent intent = new Intent(m_activity, AudioService.class);
        intent.setAction(AudioActions.ACTION_SYNC_UPDATE);
        intent.putExtra("payload", payload);
        m_activity.startForegroundService(intent);
    }

    @JavascriptInterface
    public void beginSync(String token) {
        Intent intent = new Intent(m_activity, AudioService.class);
        intent.setAction(AudioActions.ACTION_SYNC);
        intent.putExtra("token", token);
        m_activity.startForegroundService(intent);
    }

    @JavascriptInterface
    public void cancelTask() {
        Intent intent = new Intent(m_activity, AudioService.class);
        intent.setAction(AudioActions.ACTION_CANCEL_TASK);
        m_activity.startForegroundService(intent);
    }

}
