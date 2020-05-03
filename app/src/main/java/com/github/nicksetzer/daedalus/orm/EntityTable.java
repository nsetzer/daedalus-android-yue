package com.github.nicksetzer.daedalus.orm;

import android.database.Cursor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * An EntityTable is a SQL Table where each row represents a unique entity
 *
 * An Entity has an spk "Surrogate Primary Key" which uniquely identifies each row
 * and an npk "Natural Primary Key" which uniquely identifies the entity using data
 * which comes from the source system of record.
 *
 * Helper methods are provided for INSERT/UPDATE/UPSERT/DELETE to manage
 * the lifecycle of an entity correctly.
 */
public class EntityTable {

    protected DatabaseConnection m_db;
    protected TableSchema m_schema;

    public EntityTable(DatabaseConnection db, TableSchema schema) {
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


    public JSONObject getObject(Cursor cursor) throws JSONException {

        JSONObject obj = new JSONObject();
        for (int i=0; i < cursor.getColumnCount(); i++) {

            String key = cursor.getColumnName(i);
            int type = cursor.getType(i);
            switch (type) {
                case Cursor.FIELD_TYPE_BLOB:
                    throw new JSONException("unexpected blob");
                case Cursor.FIELD_TYPE_FLOAT:
                    obj.put(key, cursor.getDouble(i));
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    obj.put(key, cursor.getLong(i));
                    break;
                case Cursor.FIELD_TYPE_NULL:
                    obj.put(key, JSONObject.NULL);
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    obj.put(key, cursor.getString(i));
                    break;

            }
        }

        return obj;
    }

}
