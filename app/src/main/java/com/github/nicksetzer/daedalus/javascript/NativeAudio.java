package com.github.nicksetzer.daedalus.javascript;

import android.app.Service;
import android.content.Intent;
import android.webkit.JavascriptInterface;

import com.github.nicksetzer.daedalus.Log;
import com.github.nicksetzer.daedalus.audio.AudioService;
import com.github.nicksetzer.daedalus.WebActivity;
import com.github.nicksetzer.daedalus.audio.AudioActions;

import org.json.JSONArray;
import org.json.JSONObject;


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
    public void loadRadioUrl(String url) {
        Intent intent = new Intent(m_activity, AudioService.class);
        intent.setAction(AudioActions.ACTION_LOAD_RADIO_URL);
        intent.putExtra("url", url);
        m_activity.startForegroundService(intent);
    }

    @JavascriptInterface
    public void playRadioUrl(String url) {
        Intent intent = new Intent(m_activity, AudioService.class);
        intent.setAction(AudioActions.ACTION_PLAY_RADIO_URL);
        intent.putExtra("url", url);
        m_activity.startForegroundService(intent);
    }

    @JavascriptInterface
    public void playNextRadioUrl() {
        Intent intent = new Intent(m_activity, AudioService.class);
        intent.setAction(AudioActions.ACTION_PLAY_NEXT_RADIO_URL);
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
    public String buildForest(String query, int syncState, int showBannished) {

        AudioService service = m_activity.getBoundService();
        if (service != null) {
            Log.error(query);
            String forest = service.mediaBuildForest(query, syncState, showBannished);
            return forest;
        } else {
            Log.error("failed to build forest -- no bound service");
            return "{}";
        }


    }

    @JavascriptInterface
    public void updateSyncStatus(String payload) {
        Intent intent = new Intent(m_activity, AudioService.class);
        intent.setAction(AudioActions.ACTION_SYNC_UPDATE);
        intent.putExtra("payload", payload);
        m_activity.startForegroundService(intent);
    }

    @JavascriptInterface
    public void syncQueryStatus() {
        Intent intent = new Intent(m_activity, AudioService.class);
        intent.setAction(AudioActions.ACTION_SYNC_QUERY);
        m_activity.startForegroundService(intent);
    }

    @JavascriptInterface
    public void beginSync(String token) {
        Log.info("begin sync: " + token);
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

    @JavascriptInterface
    public String getSyncInfo() {
        // # fetched track records
        // # number synced songs

        String info = m_activity.getBoundService().getSyncInfo();
        return info;

    }

    @JavascriptInterface
    public void initRadio(String token, String station) {
        Log.info("init radio token: " + token);
        Log.info("init radio station: " + station);
        Intent intent = new Intent(m_activity, AudioService.class);
        intent.setAction(AudioActions.ACTION_INIT_RADIO);
        intent.putExtra("token", token);
        intent.putExtra("station", station);
        m_activity.startForegroundService(intent);
    }

    @JavascriptInterface
    public int getCurrentIndex() {
        AudioService service = m_activity.getBoundService();
        if (service != null) {
            return service.getCurrentIndex();
        }
        return 0;
    }
}
