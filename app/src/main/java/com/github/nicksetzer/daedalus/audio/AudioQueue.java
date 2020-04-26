package com.github.nicksetzer.daedalus.audio;

import org.json.JSONArray;
import org.json.JSONException;

public class AudioQueue {

    JSONArray m_queue;

    int m_currentIndex = -1;

    public void setData(String data) {
        android.util.Log.i("daedalus-js",data);
        try {
            m_queue = new JSONArray(data);
        } catch (JSONException e) {
            android.util.Log.e("daedalus-js","unable to parse json");
        }
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
            return m_queue.getJSONObject(index).getString("url");
        } catch (JSONException e) {
            android.util.Log.e("daedalus-js", "unable to get index " + index);
        }
        return null;
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
