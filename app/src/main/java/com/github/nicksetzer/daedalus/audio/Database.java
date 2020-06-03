package com.github.nicksetzer.daedalus.audio;

import android.content.Context;
import android.database.Cursor;

import com.github.nicksetzer.metallurgy.orm.DatabaseConnection;
import com.github.nicksetzer.metallurgy.orm.EntityTable;
import com.github.nicksetzer.metallurgy.orm.NaturalPrimaryKey;
import com.github.nicksetzer.metallurgy.orm.TableSchema;

import org.json.JSONException;
import org.json.JSONObject;

public class Database {

    public final int m_schema_version = 1;

    private Context m_context;
    private String m_path;
    private DatabaseConnection m_db;
    private TableSchema[] m_schema;

    public EntityTable m_testsTable;
    public SongsTable m_songsTable;
    public EntityTable m_usersTable;
    public EntityTable m_filesTable;
    public EntityTable m_recordsTable;

    public Database(Context context) {
        m_context = context;

        TableSchema tests = _initTestSchema();
        TableSchema users = _initUserSchema();
        TableSchema songs = _initSongsSchema();
        TableSchema files = _initFilesSchema();
        TableSchema history_records = _initHistoryRecordsSchema();

        m_path = m_context.getExternalFilesDir(null)+ "/app-v" + m_schema_version + ".sqlite";
        m_schema = new TableSchema[]{tests, users, songs, files, history_records};
        m_db = new DatabaseConnection(m_path, m_schema);

        m_testsTable = new EntityTable(m_db, tests);
        m_usersTable = new EntityTable(m_db, users);
        m_songsTable = new SongsTable(m_db, songs);
        m_filesTable = new EntityTable(m_db, files);
        m_recordsTable = new EntityTable(m_db, history_records);
    }

    /**
     * @return the schema for the Test table
     */
    private TableSchema _initTestSchema() {

        TableSchema tests = new TableSchema("tests");
        tests.addColumn("spk", "INTEGER PRIMARY KEY AUTOINCREMENT");
        tests.addColumn("vstr", "VARCHAR");
        tests.addColumn("vint", "INTEGER");
        tests.addColumn("vdbl", "DOUBLE");
        return tests;
    }

    private TableSchema _initUserSchema() {
        TableSchema users = new TableSchema("users");
        users.addColumn("spk", "INTEGER PRIMARY KEY AUTOINCREMENT");
        users.addColumn("uid", "VARCHAR");
        users.addColumn("username", "VARCHAR");
        users.addColumn("apikey", "VARCHAR");
        return users;
    }

    /**
     *
     * @return The Schema for the Songs table
     *
     * the natural primary key is the uid received from the remote database
     * remote tracks will have a "song_id" which is the uid
     */
    private TableSchema _initSongsSchema() {
        TableSchema songs = new TableSchema("songs");
        songs.addColumn("spk", "INTEGER PRIMARY KEY AUTOINCREMENT");
        songs.addColumn("uid", "VARCHAR");
        songs.addColumn("user_id", "VARCHAR NOT NULL"); // users.uid

        songs.addColumn("valid", "INTEGER DEFAULT 0");  // if 0, resource marked for deletion
                                                                    // during fetch set 0 and mark as 1 when the record is updated
        songs.addColumn("sync", "INTEGER DEFAULT 0");   // download this resource
        songs.addColumn("synced", "INTEGER DEFAULT 0"); // resource has been downloaded

        songs.addColumn("artist", "VARCHAR NOT NULL", true);
        songs.addColumn("artist_key", "VARCHAR");
        songs.addColumn("album", "VARCHAR", true);
        songs.addColumn("title", "VARCHAR", true);
        songs.addColumn("composer", "VARCHAR");
        songs.addColumn("comment", "VARCHAR");
        songs.addColumn("country", "VARCHAR");
        songs.addColumn("language", "VARCHAR");
        songs.addColumn("genre", "VARCHAR");

        songs.addColumn("file_path", "VARCHAR");
        songs.addColumn("file_size", "INTEGER");
        songs.addColumn("art_path", "VARCHAR");
        songs.addColumn("art_size", "INTEGER");

        songs.addColumn("play_count", "INTEGER DEFAULT 0");
        songs.addColumn("skip_count", "INTEGER DEFAULT 0");
        songs.addColumn("album_index", "INTEGER DEFAULT 0");
        songs.addColumn("year", "INTEGER DEFAULT 0");
        songs.addColumn("length", "INTEGER DEFAULT 0");
        songs.addColumn("rating", "INTEGER DEFAULT 0");

        songs.addColumn("date_added", "INTEGER DEFAULT 0");
        songs.addColumn("last_played", "INTEGER DEFAULT 0");
        return songs;
    }

    private TableSchema _initFilesSchema() {
        TableSchema files = new TableSchema("files");
        files.addColumn("spk", "INTEGER PRIMARY KEY AUTOINCREMENT");
        files.addColumn("uid", "VARCHAR", true);
        files.addColumn("sync", "INTEGER DEFAULT 0");
        files.addColumn("synced", "INTEGER DEFAULT 0");
        files.addColumn("rel_path", "VARCHAR", true);

        files.addColumn("local_version", "INTEGER DEFAULT 0");
        files.addColumn("remote_version", "INTEGER DEFAULT 0");

        files.addColumn("local_size", "INTEGER DEFAULT 0");
        files.addColumn("remote_size", "INTEGER DEFAULT 0");

        files.addColumn("local_permission", "INTEGER DEFAULT 0");
        files.addColumn("remote_permission", "INTEGER DEFAULT 0");

        files.addColumn("local_mtime", "INTEGER DEFAULT 0");
        files.addColumn("remote_mtime", "INTEGER DEFAULT 0");

        files.addColumn("remote_public", "VARCHAR");
        files.addColumn("remote_encryption", "VARCHAR");
        return files;
    }

    private TableSchema _initHistoryRecordsSchema() {
        TableSchema history_records = new TableSchema("history_records");
        history_records.addColumn("spk", "INTEGER PRIMARY KEY AUTOINCREMENT");
        history_records.addColumn("song_id", "VARCHAR");
        history_records.addColumn("user_id", "VARCHAR");
        history_records.addColumn("timestamp", "INTEGER DEFAULT 0", true);
        return history_records;
    }

    public void connect() {
        m_db.connect();


        //test();
    }

    private boolean test() {
        JSONObject test = new JSONObject();
        try {
            test.put("vstr", "abc");
            test.put("vint", 123);
            test.put("vdbl", 3.14);
        } catch (JSONException e) {
            android.util.Log.e("daedalus-js", "test failed");
            return false;
        }

        // INSERT
        {
            m_db.beginTransaction();
            try {
                m_testsTable.insert(test);
            } finally {
                m_db.endTransaction(true);

            }

            long count = m_testsTable.count();
            android.util.Log.d("daedalus-js", "count: " + count);

            if (count != 1) {
                android.util.Log.e("daedalus-js", "test failed");
                return false;
            }
        }
        // SELECT
        long spk = -1;
        {
            NaturalPrimaryKey npk = new NaturalPrimaryKey();
            npk.put("vstr", "abc");
            Cursor cursor = m_testsTable.select(npk, 1, 0);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        int index = cursor.getColumnIndex("spk");
                        spk = cursor.getLong(index);
                        android.util.Log.d("daedalus-js", "value=> " + spk);
                        cursor.moveToNext();
                    }
                }
            }
        }

        if (spk < 0) {
            return false;
        }

        // UPDATE
        try {
            test.put("vstr", "def");
        } catch (JSONException e) {
            android.util.Log.e("daedalus-js", "test failed");
            return false;
        }

        {
            m_testsTable.update(spk, test);
            NaturalPrimaryKey npk = new NaturalPrimaryKey();
            npk.put("vstr", "def");
            Cursor cursor = m_testsTable.select(npk, 1, 0);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        int index = cursor.getColumnIndex("spk");
                        spk = cursor.getLong(index);
                        android.util.Log.d("daedalus-js", "value=> " + spk);
                        cursor.moveToNext();
                    }
                }
            }
        }

        {
            m_testsTable.delete(spk);
            long count = m_testsTable.count();

            android.util.Log.d("daedalus-js", "count: " + count);
            if (count > 0) {
                android.util.Log.e("daedalus-js", "test failed");
                return false;
            }
        }

        android.util.Log.i("daedalus-js", "test passed");
        return true;
    }

    public void close() {
        m_db.close();
    }

    public void beginTransaction() {
        m_db.beginTransaction();
    }

    public void beginTransaction(boolean exclusive) {
        m_db.beginTransaction(exclusive);
    }

    public void endTransaction(boolean success) {
        m_db.endTransaction(success);
    }
}
