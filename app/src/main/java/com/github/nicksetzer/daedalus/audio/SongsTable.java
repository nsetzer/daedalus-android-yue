package com.github.nicksetzer.daedalus.audio;

import android.database.Cursor;

import com.github.nicksetzer.daedalus.orm.DatabaseConnection;
import com.github.nicksetzer.daedalus.orm.EntityTable;
import com.github.nicksetzer.daedalus.orm.TableSchema;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SongsTable extends EntityTable {

    public SongsTable(DatabaseConnection db, TableSchema schema) {
        super(db, schema);
    }

    /**
     * prior to running the fetch process, invalidate all records
     * after fetch is run, delete files which are still invalid
     */
    public void invalidate() {

        String query = "UPDATE songs SET valid = 0";
        m_db.execute(query, new String[]{});
    }

    public JSONArray queryForest() {

        String query = "SELECT spk, uid, artist, album, title, length, sync, synced, file_path, rating FROM songs ORDER BY artist, album, title";
        Cursor cursor = m_db.query(query, null);

        JSONArray forest = new JSONArray();
        JSONObject artist = null;
        JSONObject album = null;
        boolean selected_all_art = false;
        boolean selected_any_art = false;
        boolean selected_all_abm = false;
        boolean selected_any_abm = false;

        if (cursor != null && cursor.moveToFirst()) {
            while(!cursor.isAfterLast()) {

                try {

                    JSONObject track = getObject(cursor);

                    if (artist == null || !track.getString("artist").equals(artist.getString("name"))) {
                        _setObjectSelected(artist, selected_all_art, selected_any_art);
                        _setObjectSelected(album, selected_all_abm, selected_any_abm);
                        artist = new JSONObject();
                        artist.put("name", track.getString("artist"));
                        artist.put("albums", new JSONArray());
                        selected_all_art = true;
                        selected_any_art = false;
                        selected_all_abm = true;
                        selected_any_abm = false;

                        album = null;

                        forest.put(artist);
                    }

                    if (album == null || !track.getString("album").equals(album.getString("name"))) {
                        _setObjectSelected(album, selected_all_abm, selected_any_abm);
                        album = new JSONObject();
                        album.put("name", track.getString("album"));
                        album.put("tracks", new JSONArray());
                        selected_all_abm = true;
                        selected_any_abm = false;

                        artist.getJSONArray("albums").put(album);
                    }

                    album.getJSONArray("tracks").put(track);

                    if (track.getInt("sync") > 0) {
                        selected_any_art = true;
                        selected_any_abm = true;
                    } else {
                        selected_all_art = false;
                        selected_all_abm = false;
                    }

                } catch (JSONException e) {
                    android.util.Log.e("daeadalus-js","failed to build forest node: " + e.getMessage());
                }

                cursor.moveToNext();
            } // end while

            try {
                _setObjectSelected(artist, selected_all_art, selected_any_art);
                _setObjectSelected(album, selected_all_abm, selected_any_abm);
            } catch (JSONException e) {
                android.util.Log.e("daeadalus-js","failed to finalize forest node: " + e.getMessage());
            }
        }

        return forest;
    }

    private void _setObjectSelected(JSONObject obj, boolean all, boolean any) throws JSONException{
        if (obj == null) {
            return;
        }

        if (all) {
            obj.put("selected", 1);
        } else if (any) {
            obj.put("selected", 2);
        } else {
            obj.put("selected", 0);
        }
    }

    public void updateSyncStatus(long spk, boolean sync) {

        JSONObject obj;
        try {
            obj = new JSONObject();
            obj.put("sync", (sync)?1:0);
        } catch (JSONException e) {
            return;
        }

        update(spk, obj);
    }

    public void updateSyncStatus(HashMap<Long, Boolean> map) {

        m_db.beginTransaction();
        boolean success = false;
        try {
            for (Map.Entry<Long, Boolean> entry : map.entrySet()) {
                updateSyncStatus(entry.getKey(), entry.getValue());
            }
            success = true;
        } finally {
            m_db.endTransaction(success);
        }
    }

    public void updateSyncStatus(String payload) {

        m_db.beginTransaction();
        boolean success = false;
        try {
            JSONObject data = new JSONObject(payload);

            Iterator<String> it = data.keys();
            while (it.hasNext()) {
                String strkey = it.next();
                boolean sync = data.getInt(strkey) > 0;
                long spk = Long.parseLong(strkey);
                updateSyncStatus(spk, sync);
            }
            success = true;
        } catch (JSONException e) {
            android.util.Log.e("daedalus-js", "error updating status: " + e.getMessage());
        } finally {
            m_db.endTransaction(success);
        }
    }

}
