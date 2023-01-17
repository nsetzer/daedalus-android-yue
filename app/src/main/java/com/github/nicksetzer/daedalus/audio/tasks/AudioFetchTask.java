package com.github.nicksetzer.daedalus.audio.tasks;

import android.util.Log;

import com.github.nicksetzer.daedalus.api.YueApi;
import com.github.nicksetzer.daedalus.audio.AudioService;
import com.github.nicksetzer.metallurgy.orm.NaturalPrimaryKey;
import com.github.nicksetzer.metallurgy.orm.dsl.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AudioFetchTask implements Runnable {

    AudioService m_service;
    String m_token;

    int upserted;

    Set<String> m_upserted;

    public AudioFetchTask(AudioService service, String token) {
        m_service = service;
        m_token = token;



    }

    public void run() {

        this.upserted = 0;
        this.m_upserted = new HashSet<>();

        try {

            m_service.m_database.m_songsTable.invalidate();

            int page_index = 0;
            int page_step = 100;
            int page_size = 1500; // must be a multiple of page_step
            int count = 0;
            int total = 0;

            m_service.fetchProgressUpdate(0, 0);

            while (true) {

                if (m_service.taskIsKill()) {
                    break;
                }

                JSONArray array = _fetch(page_index, page_size);

                if (array == null) {
                    Log.e("daedalus-js-api","fetch error");
                    return;
                }
                if (array.length() == 0) {
                    break;
                }

                if (m_service.taskIsKill()) {
                    break;
                }

                page_index += 1;
                total += array.length();

                for (int start=0; start < array.length(); start += page_step) {
                    m_service.fetchProgressUpdate(count + start, total);

                    _insertSlice(array, start, page_step);
                }

                count = total;

            }

            Log.i("daedalus-js-api", " received: " + total + " pages: " + page_index + " indexed: " +  m_service.m_database.m_songsTable.count());

            m_service.fetchProgressUpdate(total, total);

            if (m_service.taskIsKill()) {
                return;
            }

            ArrayList<Pair<Long, String>> tracks = m_service.m_database.m_songsTable.getInvalid();

            Log.i("daedalus-js-api", "removing " + tracks.size() + " invalid synced files");

            for (Pair<Long, String> track : tracks) {

                Log.i("daedalus-js-api", "removing file: " + track.first + " " + track.second);
                if (m_service.taskIsKill()) {
                    break;
                }

                _removeFile(track.second);

                m_service.m_database.beginTransaction();
                try {
                    m_service.m_database.m_songsTable.delete(track.first);
                } finally {
                    m_service.m_database.endTransaction(true);
                }

            }

            Log.i("daedalus-js-api", "removed " + tracks.size() + " invalid synced files");

            if (m_service.taskIsKill()) {
                return;
            }

            Log.i("daedalus-js-api", "removing remaining invalid synced files");

            int removed = m_service.m_database.m_songsTable.removeInvalid();

            Log.i("daedalus-js-api", "removed " + removed + " invalid synced files");

            // TODO: the number of tracks received, and the number of unique tracks received is not equal
            // ensure upserted == set.size; if correct then the indexed count should also match
            Log.i("daedalus-js-api",
                    "updated: " + this.upserted +
                    " set_size: " + this.m_upserted.size() +
                    " indexed: " +  m_service.m_database.m_songsTable.count());

            this.m_upserted.clear();


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
            array = YueApi.librarySearch(m_token, "", page_size, page_index, "id", false);
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

                    m_upserted.add(remoteTrack.getString("id"));

                } catch (JSONException e) {
                    android.util.Log.d("daedalus-js-api", e.getMessage());
                    return;
                }

                m_service.m_database.m_songsTable.upsert(npk, remoteTrack);
                this.upserted += 1;
                //Log.e("daedalus-js-api", "failed to upsert " + npk.toString() + " - " + remoteTrack.toString());


            }
            success = true;
        } finally {
            m_service.m_database.endTransaction(success);
        }

    }

    private void _removeFile(String path) {
        File file = new File(path);

        if (file.exists()) {
            file.delete();
            Log.w("daedalus-js-api", "removed:" + path);
        } else {
            Log.w("daedalus-js-api", "file not found:" + path);
        }

    }
}
