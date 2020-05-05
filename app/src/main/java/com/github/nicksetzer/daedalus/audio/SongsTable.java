package com.github.nicksetzer.daedalus.audio;

import android.database.Cursor;

import com.github.nicksetzer.daedalus.Log;
import com.github.nicksetzer.daedalus.orm.DatabaseConnection;
import com.github.nicksetzer.daedalus.orm.EntityTable;
import com.github.nicksetzer.daedalus.orm.TableSchema;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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

    public JSONArray queryForest(String query, boolean syncedOnly) {

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT spk, uid, artist, artist_key, album, title, length, sync, synced, file_path, rating FROM songs");

        ArrayList<String> params = new ArrayList<>();

        if (!query.isEmpty() || syncedOnly) {
            sb.append(" WHERE (");
            if (syncedOnly) {
                sb.append("(synced ==");
                sb.append(1);
                sb.append(") AND ");
            }
            String[] parts = query.split("\\s+");
            boolean first = true;
            for (String part : parts) {
                if (!first) {
                    sb.append(" AND ");
                }

                sb.append("(");
                sb.append("artist LIKE ? OR album LIKE ? OR title LIKE ?");
                params.add("%" + part + "%");
                params.add("%" + part + "%");
                params.add("%" + part + "%");
                sb.append(")");
            }

            sb.append(")");
        }

        sb.append(" ORDER BY artist_key COLLATE NOCASE, album COLLATE NOCASE, title COLLATE NOCASE");

        String sql = sb.toString();
        Cursor cursor = m_db.query(sql, params.toArray(new String[]{}));

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

                    //TODO: this is a bug, maybe in the new database
                    track.put("id", track.get("uid"));

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

    public ArrayList<JSONObject> getSyncDownloadTracks() {
        String query = "SELECT spk, uid, artist, album, title FROM songs WHERE (valid == 1 AND sync != synced AND sync == 1)";

        Cursor cursor = m_db.query(query, null);

        ArrayList<JSONObject> array = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                try {
                    JSONObject track = getObject(cursor);
                    array.add(track);
                } catch (JSONException e) {
                    Log.error("error parsing object", e);
                }
                cursor.moveToNext();
            }
        }

        return array;
    }

    public ArrayList<JSONObject> getSyncDeleteTracks() {
        String query = "SELECT spk, file_path, art_path FROM songs WHERE ((valid == 0 OR sync == 0) AND synced == 1)";

        Cursor cursor = m_db.query(query, null);

        ArrayList<JSONObject> array = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                try {
                    JSONObject track = getObject(cursor);
                    array.add(track);
                } catch (JSONException e) {
                    Log.error("error parsing object", e);
                }
                cursor.moveToNext();
            }
        }

        return array;
    }
}
