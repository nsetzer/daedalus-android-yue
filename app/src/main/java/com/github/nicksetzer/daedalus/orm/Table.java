package com.github.nicksetzer.daedalus.orm;

import android.database.Cursor;

import org.json.JSONArray;
import org.json.JSONObject;

public class Table {

    DatabaseConnection m_db;
    TableSchema m_schema;

    public Table(DatabaseConnection db, TableSchema schema) {
        m_db = db;
        this.m_schema = schema;
    }

    public void insert(JSONObject item) {
        Statement statement = StatementBuilder.prepareInsert(m_schema, item);
        m_db.execute(statement);
    }

    public void insertBulk(JSONArray items) {
        Statement statement = StatementBuilder.prepareInsertBulk(m_schema, items);
        m_db.execute(statement);
    }

    public void update(long spk, JSONObject item) {
        Statement statement = StatementBuilder.prepareUpdate(m_schema, spk, item);
        m_db.execute(statement);
    }

    public void upsert(INaturalPrimaryKey npk, JSONObject item) {
        Statement statement = StatementBuilder.prepareUpsertSelect(m_schema, npk);
        Cursor cursor = m_db.query(statement);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(StatementBuilder.SURROGATE_PRIMARY_KEY);
                long spk = cursor.getLong(index);
                cursor.close();
                statement = StatementBuilder.prepareUpdate(m_schema, spk, item);
                m_db.execute(statement);
            } else {
                // query returned an empty set
                cursor.close();
                statement = StatementBuilder.prepareInsert(m_schema, item);
                m_db.execute(statement);
            }
        }

        return;
    }

    public void updateSet(Long[] spks, String columnName, Object columnValue) {
        Statement statement = StatementBuilder.prepareUpdateSet1(m_schema, spks, columnName, columnValue);
        m_db.execute(statement);
    }

    public void delete(long spk) {
        Statement statement = StatementBuilder.prepareDelete(m_schema, spk);
        m_db.execute(statement);
    }

    public void deleteBulk(long[] spks) {
        Statement statement = StatementBuilder.prepareDeleteBulk(m_schema, spks);
        m_db.execute(statement);
    }

    public Cursor select(INaturalPrimaryKey npk, long limit, long offset) {
        Statement statement = StatementBuilder.prepareSelect(m_schema, npk, limit, offset);
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
