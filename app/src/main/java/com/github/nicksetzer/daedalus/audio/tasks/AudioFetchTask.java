package com.github.nicksetzer.daedalus.audio.tasks;

import android.os.AsyncTask;

import com.github.nicksetzer.daedalus.api.YueApi;
import com.github.nicksetzer.daedalus.audio.AudioService;
import com.github.nicksetzer.daedalus.orm.NaturalPrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class AudioFetchTask implements Runnable {

    AudioService m_service;
    String m_token;

    public AudioFetchTask(AudioService service, String token) {
        m_service = service;
        m_token = token;

    }

    public void run() {

        JSONArray array;
        try {
            array = YueApi.librarySearch(m_token, "", 500, 0, "", false);
        } catch (IOException e) {
            android.util.Log.e("daedalus-js-api", "io error: " + e.toString());
            return;
        } catch (Exception e) {
            android.util.Log.e("daedalus-js-api", "fail: " + e.toString());
            return;
        }
        android.util.Log.e("daedalus-js-api", "task finished");

        try {
            _insertSlice(array, 0, 10);
        } catch (Exception e) {
            android.util.Log.e("daedalus-js-api", "fail: " + e.toString());
            return;
        }

        return;
    }

    void _insertSlice(JSONArray array, int start, int length) {

        boolean success = false;
        m_service.m_database.beginTransaction();
        try {
            for (int i=start; i < start+length; i++) {
                NaturalPrimaryKey npk = new NaturalPrimaryKey();
                JSONObject remoteTrack = null;
                try {
                    remoteTrack = array.getJSONObject(i);
                    android.util.Log.d("daedalus-js-api", remoteTrack.toString());
                    remoteTrack.put("uid", remoteTrack.get("id"));
                    npk.put("uid", remoteTrack.get("id"));


                } catch (JSONException e) {
                    android.util.Log.d("daedalus-js-api", e.getMessage());
                    return;
                }


                m_service.m_database.m_songsTable.upsert(npk, remoteTrack);

                success = true;
            }
        } finally {
            m_service.m_database.endTransaction(success);
            android.util.Log.d("daedalus-js-api", "count: " + m_service.m_database.m_songsTable.count());
        }

    }

}
