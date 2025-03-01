package com.github.nicksetzer.daedalus.audio;

import android.database.Cursor;

import com.github.nicksetzer.daedalus.Log;
import com.github.nicksetzer.metallurgy.orm.DatabaseConnection;
import com.github.nicksetzer.metallurgy.orm.EntityTable;
import com.github.nicksetzer.metallurgy.orm.TableSchema;
import com.github.nicksetzer.metallurgy.orm.dsl.DateUtil;
import com.github.nicksetzer.metallurgy.orm.dsl.DslException;
import com.github.nicksetzer.metallurgy.orm.dsl.Pair;
import com.github.nicksetzer.metallurgy.orm.dsl.QDateTime;
import com.github.nicksetzer.metallurgy.orm.dsl.Token;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SongsTable extends EntityTable {

    public SongsTable(DatabaseConnection db, TableSchema schema) {
        super(db, schema);
    }

    public void updatePlayTime(long spk) {

        long epoch_time = System.currentTimeMillis()/1000;

        JSONObject obj;
        try {
            obj = new JSONObject();
            obj.put("last_played", epoch_time);
        } catch (JSONException e) {
            return;
        }

        Log.info("updating playtime for " + spk + " to " + epoch_time);

        update(spk, obj);
    }
    /**
     * prior to running the fetch process, invalidate all records
     * after fetch is run, delete files which are still invalid
     */
    public void invalidate() {

        String query = "UPDATE songs SET valid = 0";
        m_db.execute(query, new String[]{});
    }

    public ArrayList<Pair<Long, String>> getInvalid() {
        String query = "SELECT spk, file_path FROM songs WHERE valid == 0 AND synced == 1";
        Cursor cursor = m_db.query(query, null);

        ArrayList<Pair<Long, String>> array = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Long spk = cursor.getLong(0);
                String path = cursor.getString(1);
                array.add(new Pair<>(spk, path));
                cursor.moveToNext();
            }
        }

        cursor.close();

        return array;
    }

    public long getSyncedCount() {
        String query = "SELECT COUNT(*) FROM songs where synced == 1";
        Cursor cursor  = m_db.query(query, new String[]{});
        long count = 0;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    count = cursor.getLong(0);
                }
            } finally {
                cursor.close();
            }
        }

        return count;
    }

    public String getFilePath(long spk) {
        String query = "SELECT file_path from songs where spk == ?";
        List<String> params = new ArrayList<>();
        params.add(Long.toString(spk));
        Cursor cursor = m_db.query(query,  params.toArray(new String[]{}));
        cursor.moveToFirst();
        String path = cursor.getString(0);
        cursor.close();
        return path;
    }

    public int removeInvalid() {

        String query = "DELETE FROM songs WHERE valid == 0";
        Cursor cursor  = m_db.query(query, new String[]{});
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    private String _buildQuery(String query, int syncState, int showBannished, List<String> params) throws DslException {

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT spk, uid, artist, artist_key, album, album_index, year, title, length, sync, synced, file_path, play_count, rating FROM songs");

        Token token = null;
        Pair<String, List<String>> result;

        if (!query.isEmpty() || syncState > 0) {

            sb.append(" WHERE (");

            QDateTime now = DateUtil.now();
            if (!query.isEmpty()) {
                token = SongTableDsl.parse(query, now);
            }

            if (syncState == 1) {
                Token tok = SongTableDsl.compare("synced", "==", SongTableDsl.number(1));
                token = SongTableDsl.and_(tok, token);
            } else if (syncState == 2) {
                Token tok = SongTableDsl.compare("synced", "!=", SongTableDsl.number(1));
                token = SongTableDsl.and_(tok, token);
            }

            result = SongTableDsl.transform(token, now);

            sb.append(result.first);
            sb.append(")");

            params.addAll(result.second);
        }


        /*
        List<String> params = new ArrayList<>();
        List<String> qparams = new ArrayList<>();

        String where = Search.parse(query, qparams);

        // || showBannished == 0
        if (!query.isEmpty() || syncState > 0) {
            sb.append(" WHERE (");
            if (syncState == 1) {
                sb.append("(synced == ");
                sb.append(1);
                sb.append(")");
                if (!where.isEmpty()) {
                    sb.append(" AND ");
                }
            } else if (syncState == 2) {
                sb.append("(synced != ");
                sb.append(1);
                sb.append(")");
                if (!where.isEmpty()) {
                    sb.append(" AND ");
                }
            }

            //if (showBannished == 0) {
            //    sb.append("(ban == 0");
            //}

            Log.error(query);

            params.addAll(qparams);
            sb.append(where);
            sb.append(")");
            Log.error(String.join(", ", params));

            //String[] parts = query.split("\\s+");
            //boolean first = true;
            //for (String part : parts) {
            //    if (!first) {
            //        sb.append(" AND ");
            //    }
            //    sb.append("(");
            //    sb.append("artist LIKE ? OR album LIKE ? OR title LIKE ?");
            //    params.add("%" + part + "%");
            //    params.add("%" + part + "%");
            //    params.add("%" + part + "%");
            //    sb.append(")");
            //}
            //sb.append(")");
            //
        }

        */

        sb.append(" ORDER BY artist_key COLLATE NOCASE, album COLLATE NOCASE, title COLLATE NOCASE");

        String sql = sb.toString();
        return sql;
    }

    public JSONArray query(String query, int syncState, int showBannished) throws DslException {
        List<String> params = new ArrayList<>();
        String sql = _buildQuery(query, syncState, showBannished, params);

        Log.error(sql);
        Log.error(String.join(", ", params));

        Cursor cursor = m_db.query(sql, params.toArray(new String[]{}));

        Log.info("query returned " + cursor.getCount() + " rows");

        JSONArray tracks = new JSONArray();

        if (cursor != null && cursor.moveToFirst()) {
            while(!cursor.isAfterLast()) {
                try {
                    JSONObject track = getObject(cursor);
                    tracks.put(track);
                } catch (JSONException e) {
                    android.util.Log.e("daeadalus-js","failed to build forest node: " + e.getMessage());
                }
                cursor.moveToNext();
            }
        } else {
            Log.error("query failed to return result");
        }

        cursor.close();

        return tracks;

    }
    public JSONArray queryForest(String query, int syncState, int showBannished) throws DslException {

        List<String> params = new ArrayList<>();
        String sql = _buildQuery(query, syncState, showBannished, params);

        Log.error(sql);
        Log.error(String.join(", ", params));

        Cursor cursor = m_db.query(sql, params.toArray(new String[]{}));

        Log.info("query returned " + cursor.getCount() + " rows");

        JSONArray forest = new JSONArray();
        JSONObject artist = null;
        String current_artist = null;
        JSONObject album = null;
        String current_album = null;

        boolean selected_all_art = false; // initial state of sync checkbox in tree view
        boolean selected_any_art = false;
        boolean selected_all_abm = false;
        boolean selected_any_abm = false;

        if (cursor != null && cursor.moveToFirst()) {
            while(!cursor.isAfterLast()) {

                try {

                    JSONObject track = getObject(cursor);

                    String track_artist = track.getString("artist").toLowerCase(Locale.ROOT);
                    String track_album = track.getString("album").toLowerCase(Locale.ROOT);

                    if (current_artist == null || !track_artist.equals(current_artist)) {
                        _setObjectSelected(artist, selected_all_art, selected_any_art);
                        _setObjectSelected(album, selected_all_abm, selected_any_abm);
                        artist = new JSONObject();
                        artist.put("name", track.getString("artist"));
                        artist.put("albums", new JSONArray());
                        selected_all_art = true;
                        selected_any_art = false;
                        selected_all_abm = true;
                        selected_any_abm = false;
                        current_artist = track_artist;
                        current_album = null;

                        album = null;

                        forest.put(artist);
                    }

                    if (current_album == null || !track_album.equals(current_album)) {
                        _setObjectSelected(album, selected_all_abm, selected_any_abm);
                        album = new JSONObject();
                        album.put("name", track.getString("album"));
                        album.put("tracks", new JSONArray());
                        selected_all_abm = true;
                        selected_any_abm = false;

                        current_album = track_album;

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

        cursor.close();

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
        String query = "SELECT spk, uid, artist, album, title, file_size, valid, sync, synced FROM songs WHERE (valid == 1 AND sync == 1)";

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

        cursor.close();

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

        cursor.close();

        return array;
    }
}
