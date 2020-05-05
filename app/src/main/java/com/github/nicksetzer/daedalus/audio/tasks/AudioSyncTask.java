package com.github.nicksetzer.daedalus.audio.tasks;

import com.github.nicksetzer.daedalus.Log;
import com.github.nicksetzer.daedalus.api.YueApi;
import com.github.nicksetzer.daedalus.audio.AudioService;
import com.github.nicksetzer.daedalus.orm.NaturalPrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class AudioSyncTask implements Runnable {

    AudioService m_service;
    String m_token;

    public AudioSyncTask(AudioService service, String token) {
        m_service = service;
        m_token = token;

    }

    public void run() {

        try {
            ArrayList<JSONObject> array = m_service.m_database.m_songsTable.getSyncDownloadTracks();
            Log.info("found " + array.size() + " tracks to download");

            int index = 0;
            for (JSONObject obj : array) {

                if (m_service.taskIsKill()) {
                    break;
                }

                try {
                    _syncOne(index, array.size(), obj);
                } catch (JSONException e) {
                    Log.error("json", e);
                } catch (IOException e) {
                    Log.error("io", e);
                }
                index += 1;
            }

            array = m_service.m_database.m_songsTable.getSyncDeleteTracks();
            Log.info("found " + array.size() + " tracks to remove");

        } finally {
            m_service.syncComplete();
        }
    }

    void _syncOne(int index, int length, JSONObject obj) throws JSONException, IOException {
        String uid = obj.getString("uid");
        String pattern = "['\"\\/\\^\\$\\|\\?\\*\\:\\<\\>\\[\\]]";
        String art = obj.getString("artist").replaceAll(pattern, "");
        String abm = obj.getString("album").replaceAll(pattern, "");
        String ttl = obj.getString("title").replaceAll(pattern, "");
        long spk = obj.getLong("spk");

        Log.info("obj", uid);

        String file_path = "/music/" + art + "/" + abm + "/" + ttl + "_" + uid.substring(0, 6) + ".ogg";


        file_path =  m_service.getExternalFilesDir(null) + file_path.replaceAll("[\\s]", "_");

        Log.info(file_path);
        YueApi.download(m_token, uid, file_path, (a, b) -> {
            String message = (a/1024) + "/" + (b/1024) + " kb";
            m_service.syncProgressUpdate(index+1, length, message);
        });

        JSONObject newObject = new JSONObject();
        newObject.put("synced", 1);
        newObject.put("file_path", file_path);

        m_service.m_database.m_songsTable.update(spk, newObject);
    }

}