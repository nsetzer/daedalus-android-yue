package com.github.nicksetzer.daedalus.audio;

import android.media.MediaMetadata;
import android.support.v4.media.MediaMetadataCompat;

import com.github.nicksetzer.daedalus.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.OutputStreamWriter;

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
        return m_queue.toString();
    }

    public int length() {
        if (m_queue == null) {
            return 0;
        }
        return m_queue.length();
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
            Log.info(obj.toString());
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

        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, obj.optString("artist", "Unknown Artist"))
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, obj.optString("album", "Unknown Album"))
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, obj.optString("title", "Unknown Title"))
                .build();

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
}
