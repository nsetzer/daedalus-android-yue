package com.github.nicksetzer.daedalus.audio.tasks;

import com.github.nicksetzer.daedalus.Log;
import com.github.nicksetzer.daedalus.api.YueApi;
import com.github.nicksetzer.daedalus.audio.AudioService;
import com.github.nicksetzer.metallurgy.orm.NaturalPrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.io.File;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

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

            ArrayList<JSONObject> filtered = new ArrayList<>();
            for (JSONObject obj : array) {
                try {
                    if (_filterOne(obj)) {
                        filtered.add(obj);
                    }
                } catch (JSONException e) {
                    Log.error(e.getMessage());
                }
            }

            Log.info("found " + filtered.size() + "/" + array.size() + " tracks to download");

            int index = 0;
            for (JSONObject obj : filtered) {

                if (m_service.taskIsKill()) {
                    break;
                }

                try {
                    // TODO: notify user number that failed to sync
                    _syncOne(index, filtered.size(), obj);

                } catch (JSONException e) {
                    Log.error("json", e);
                } catch (IOException e) {
                    Log.error("io", e);
                }
                index += 1;
            }



            array = m_service.m_database.m_songsTable.getSyncDeleteTracks();
            Log.info("found " + array.size() + " tracks to remove");
            index = 0;
            for (JSONObject obj : array) {
                if (m_service.taskIsKill()) {
                    break;
                }

                try {

                    _removeOne(index, filtered.size(), obj);

                } catch (JSONException e) {
                    Log.error("json", e);
                } catch (IOException e) {
                    Log.error("io", e);
                }
                index += 1;
            }


        } finally {
            m_service.syncComplete();
        }
    }

    String _getFilePath(JSONObject obj) throws JSONException {
        String uid = obj.getString("uid");
        String pattern = "['\"\\/\\^\\$\\|\\?\\*\\:\\<\\>\\[\\]]";
        String art = obj.getString("artist").replaceAll(pattern, "");
        String abm = obj.getString("album").replaceAll(pattern, "");
        String ttl = obj.getString("title").replaceAll(pattern, "");

        String file_path = "/music/" + art + "/" + abm + "/" + ttl + "_" + uid.substring(0, 6) + ".ogg";

        file_path =  m_service.getExternalFilesDir(null) + file_path.replaceAll("[\\s]", "_");
        return file_path;
    }

    boolean _filterOne(JSONObject obj) throws JSONException {
        // remove files that have already been synced
        // double check that the file size is correct
        boolean synced = obj.getInt("synced")!=0;

        if (synced) {
            long file_size = obj.getInt("file_size");

            String file_path =  _getFilePath(obj);

            File file = new File(file_path);

            if (!file.exists()) {
                synced = false;
            } else {
                long actual_size = file.length();
                if (actual_size != file_size) {
                    Log.warn(String.format("file_size = %d  actual_size = %d", file_size, actual_size));

                    synced = false;
                }
            }

        }

        return !synced;

    }
    boolean _syncOne(int index, int length, JSONObject obj) throws JSONException, IOException {

        long spk = obj.getLong("spk");
        //boolean sync = obj.getBoolean("sync");
        boolean synced = obj.getInt("synced")!=0;
        String uid = obj.getString("uid");

        String file_path =  _getFilePath(obj);

        Log.info("sync path=" + file_path + ", uid=" + uid);


        boolean result;

        try {
            result = YueApi.download(m_token, uid, file_path, (a, b) -> {
                String message = (a / 1024) + "/" + (b / 1024) + " kb";
                m_service.syncProgressUpdate(index + 1, length, message);
            });
        } catch (SocketTimeoutException e) {
            Log.info("failed to download song: " + uid);
            Log.info("download path: " + file_path);

            return false;
        }

        JSONObject newObject = new JSONObject();
        newObject.put("synced", result?1:0);
        newObject.put("file_path", result?file_path:"");
        if (!result) {
            Log.info("clearing entry for " + uid);
        }

        m_service.m_database.m_songsTable.update(spk, newObject);

        return result;
    }

    void _removeOne(int index, int length, JSONObject obj) throws JSONException, IOException {

        long spk = obj.getLong("spk");

        String file_path = obj.getString("file_path");

        Log.info("remove path=" + file_path);

        JSONObject newObject = new JSONObject();
        newObject.put("synced", 0);
        newObject.put("file_path", "");

        File file = new File(file_path);
        if (file.exists()) {
            file.delete();
        }

        m_service.m_database.m_songsTable.update(spk, newObject);

        String message = "removed " + (index+1) + "/" + (length);
        m_service.syncProgressUpdate(index + 1, length, message);

    }
}