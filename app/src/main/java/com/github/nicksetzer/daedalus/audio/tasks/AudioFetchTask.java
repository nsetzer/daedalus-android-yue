package com.github.nicksetzer.daedalus.audio.tasks;

import com.github.nicksetzer.daedalus.api.YueApi;
import com.github.nicksetzer.daedalus.audio.AudioService;
import com.github.nicksetzer.metallurgy.orm.NaturalPrimaryKey;

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


        try {

            m_service.m_database.m_songsTable.invalidate();

            int page_index = 0;
            int page_step = 100;
            int page_size = 800; // must be a multiple of page_step
            int count = 0;
            int total = 0;

            m_service.fetchProgressUpdate(0, 0);

            while (true) {

                if (m_service.taskIsKill()) {
                    break;
                }

                JSONArray array = _fetch(page_index, page_size);

                if (array == null || array.length() == 0) {
                    break;
                }

                if (m_service.taskIsKill()) {
                    break;
                }

                page_index += 1;
                total += array.length();

                android.util.Log.e("daedalus-js-api", "task finished");

                for (int start=0; start < page_size; start += page_step) {
                    m_service.fetchProgressUpdate(count + start, total);

                    _insertSlice(array, start, page_step);
                }

                count = total;

            }

            m_service.fetchProgressUpdate(total, total);
        } catch (Exception e) {
            android.util.Log.e("daedalus-js-api", "fail: " + e.toString());
            return;
        } finally {
            m_service.fetchComplete();
        }

        return;
    }

    private JSONArray _fetch(int page_index, int page_size) {
        JSONArray array = null;
        try {
            array = YueApi.librarySearch(m_token, "", page_size, page_index, "", false);
        } catch (IOException e) {
            android.util.Log.e("daedalus-js-api", "io error: " + e.toString());
            return null;
        } catch (Exception e) {
            android.util.Log.e("daedalus-js-api", "fail: " + e.toString());
            return null;
        }

        return array;
    }

    private void _insertSlice(JSONArray array, int start, int length) {

        boolean success = false;
        m_service.m_database.beginTransaction();
        try {
            for (int i=start; i < start+length && i < array.length(); i++) {

                NaturalPrimaryKey npk = new NaturalPrimaryKey();
                JSONObject remoteTrack;
                try {
                    // convert the remote track fields into the format
                    // for the local database
                    remoteTrack = array.getJSONObject(i);
                    remoteTrack.put("uid", remoteTrack.get("id"));
                    if (remoteTrack.has("file_path")) {
                        remoteTrack.remove("file_path");
                    }
                    if (remoteTrack.has("file_size")) {
                        remoteTrack.remove("file_size");
                    }
                    if (remoteTrack.has("art_path")) {
                        remoteTrack.remove("art_path");
                    }
                    if (remoteTrack.has("art_size")) {
                        remoteTrack.remove("art_size");
                    }
                    remoteTrack.put("valid", 1);

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
