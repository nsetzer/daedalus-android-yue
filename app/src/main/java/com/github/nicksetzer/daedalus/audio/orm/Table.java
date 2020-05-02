package com.github.nicksetzer.daedalus.audio.orm;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

public class Table {

    DatabaseConnection m_db;
    TableSchema m_schema;

    public Table(DatabaseConnection db, TableSchema schema) {
        m_db = db;
        this.m_schema = schema;
    }

    public Cursor insert(JSONObject item) {
        Statement statement = StatementBuilder.prepareInsert(m_schema, item);
        return m_db.query(statement);
    }

    public Cursor insertBulk(JSONArray items) {
        Statement statement = StatementBuilder.prepareInsertBulk(m_schema, items);
        return m_db.query(statement);
    }

    public Cursor update(long spk, JSONObject item) {
        Statement statement = StatementBuilder.prepareUpdate(m_schema, spk, item);
        return m_db.query(statement);
    }

    public Cursor upsert(INaturalPrimaryKey npk, JSONObject item) {
        Statement statement = StatementBuilder.prepareUpsertSelect(m_schema, npk);
        Cursor cursor = m_db.query(statement);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(StatementBuilder.SURROGATE_PRIMARY_KEY);
                long spk = cursor.getLong(index);
                cursor.close();
                statement = StatementBuilder.prepareUpdate(m_schema, spk, item);
                return m_db.query(statement);
            } else {
                // query returned an empty set
                cursor.close();
                statement = StatementBuilder.prepareInsert(m_schema, item);
                return m_db.query(statement);
            }
        }

        return null;
    }

    public Cursor delete(long spk) {
        Statement statement = StatementBuilder.prepareDelete(m_schema, spk);
        return m_db.query(statement);
    }

    public Cursor deleteBulk(long[] spks) {
        Statement statement = StatementBuilder.prepareDeleteBulk(m_schema, spks);
        return m_db.query(statement);
    }

    public boolean exists(INaturalPrimaryKey npk) {
        return false;
    }

    public boolean[] existsBatch(INaturalPrimaryKey[] npk) {
        return new boolean[]{};
    }

    public long count() {
        Cursor cursor = m_db.query("SELECT COUNT(*) FROM " + m_schema.name, null);
        long count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getLong(0);
            }
            cursor.close();
        }

        return count;
    }


}
