package com.github.nicksetzer.daedalus.audio;

import android.content.Intent;
import android.media.MediaMetadata;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.github.nicksetzer.daedalus.Log;
import com.github.nicksetzer.daedalus.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import androidx.core.app.NotificationCompat;

public class AudioQueue {

    JSONArray m_queue;

    int m_currentIndex = -1;

    public void setData(String data) {

        try {
            m_queue = new JSONArray(data);
            android.util.Log.i("daedalus-js","queue data set");
        } catch (JSONException e) {
            android.util.Log.e("daedalus-js","unable to parse json");
        }
    }

    public String getData() {
        if (m_queue == null) {
            Log.error("queue contains no data");
        }
        return m_queue.toString();
    }

    public int length() {
        if (m_queue == null) {
            return 0;
        }
        return m_queue.length();
    }

    public long getSpk(int index) throws JSONException {
        JSONObject obj = m_queue.getJSONObject(index);
        return obj.getLong("spk");

    }

    public String getUrl(int index) {
        if (m_queue == null) {
            return null;
        }
        if (index < 0 || index >= m_queue.length()) {
            return null;
        }
        try {
            JSONObject obj = m_queue.getJSONObject(index);
            //Log.info(obj.toString());
            if (obj.has("file_path")) {
                String file_path = obj.getString("file_path");
                File file = new File(file_path);
                if (file.exists()) {
                    return file_path;
                }
            }
            return obj.getString("url");
        } catch (JSONException e) {
            android.util.Log.e("daedalus-js", "unable to get index " + index);
        }
        return null;
    }

    public MediaMetadataCompat getMetadata(int index) {

        if (m_queue == null) {
            return null;
        }

        if (index < 0 || index >= m_queue.length()) {
            return null;
        }

        JSONObject obj = null;
        try {
            obj =  m_queue.getJSONObject(index);
        } catch (JSONException e) {
            android.util.Log.e("daedalus-js", "unable to get index " + index);
        }

        if (obj == null) {
            return null;
        }

        //Log.info("duration: " + obj.optLong("length", 0));
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, obj.optString("artist", "Unknown Artist"))
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, obj.optString("album", "Unknown Album"))
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, obj.optString("title", "Unknown Title"))
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, obj.optLong("length", 0) * 1000)
                .build();

    }

    /**
     * this is a half baked implementation
     * @param index
     * @return
     */
    public MediaBrowserCompat.MediaItem getMediaItem(int index) {

        if (m_queue == null) {
            return null;
        }

        if (index < 0 || index >= m_queue.length()) {
            return null;
        }

        JSONObject obj = null;
        try {
            obj =  m_queue.getJSONObject(index);
        } catch (JSONException e) {
            android.util.Log.e("daedalus-js", "unable to get index " + index);
        }

        if (obj == null) {
            return null;
        }

        Bundle duration = new Bundle();
        duration.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, obj.optLong("length", 0));

        MediaDescriptionCompat desc = new MediaDescriptionCompat.Builder()
                .setMediaId("queue-" + obj.optString("uid", "null") + "-" + index)
                .setTitle(obj.optString("title", "Unknown Album"))
                .setSubtitle(obj.optString("artist", "Unknown Album"))
                .setExtras(duration)
                .build();

        MediaBrowserCompat.MediaItem item = new MediaBrowserCompat.MediaItem(desc,
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);

        return item;
    }

    public List<MediaBrowserCompat.MediaItem> getMediaItems() {

        if (m_queue == null) {
            return null;
        }

        if (m_queue.length()==0) {
            return null;
        }

        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        for (int i=0; i < m_queue.length(); i++) {
            mediaItems.add(this.getMediaItem(i));
        }

        return mediaItems;
    }

    public void setCurrentIndex(int index) {
        m_currentIndex = index;
    }

    public int getCurrentIndex() {
        return m_currentIndex;
    }

    public String getCurrentUrl() {
        return getUrl(m_currentIndex);
    }

    public boolean next() {
        if (m_currentIndex < m_queue.length() - 1) {
            m_currentIndex += 1;
            return true;
        }
        return false;
    }

    public boolean prev() {
        if (m_currentIndex > 0) {
            m_currentIndex -= 1;
            return true;
        }
        return false;
    }

    public void updateNotification(NotificationCompat.Builder builder) {

        if (m_queue == null || m_currentIndex < 0 || m_currentIndex >= m_queue.length()) {

            builder.setContentTitle("App is running in background");
            return;
        }
        try {
            JSONObject obj = m_queue.getJSONObject(m_currentIndex);

            if (obj.has("artist")) {
                String artist = obj.getString("artist");
                builder.setContentText(artist);
            }
            if (obj.has("album")) {
                String album = obj.getString("album");
                builder.setSubText(album);
            }
            if (obj.has("title")) {
                String title = obj.getString("title");
                builder.setContentTitle(title);
            }


        } catch (JSONException e) {
            Log.error("unable to format notification", e);
        }
        return;

    }
}
