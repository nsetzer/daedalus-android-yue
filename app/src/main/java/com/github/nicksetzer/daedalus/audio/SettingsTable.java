package com.github.nicksetzer.daedalus.audio;

import android.database.Cursor;

import com.github.nicksetzer.daedalus.Log;
import com.github.nicksetzer.metallurgy.orm.DatabaseConnection;
import com.github.nicksetzer.metallurgy.orm.EntityTable;
import com.github.nicksetzer.metallurgy.orm.NaturalPrimaryKey;
import com.github.nicksetzer.metallurgy.orm.TableSchema;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsTable extends EntityTable {

    public SettingsTable(DatabaseConnection db, TableSchema schema) {
        super(db, schema);
    }

    public void setInt(String key, int value) {
        NaturalPrimaryKey npk = new NaturalPrimaryKey();
        npk.set("key", key);
        JSONObject obj = new JSONObject();
        try {
            obj.put("key", key);
            obj.put("value", "" + value);

            upsert(npk, obj);
        } catch (JSONException e) {
            Log.error("failed to insert");
        }

    }

    public int getInt(String key) throws MissingValue {
        try {
            NaturalPrimaryKey npk1 = new NaturalPrimaryKey();
            npk1.set("key", key);
            Cursor cursor = this.select(npk1, 1, 0);
            JSONObject obj = this.getFirstObject(cursor);
            if (obj != null) {
                return Integer.parseInt(obj.getString("value"));
            } else {
                throw new MissingValue();
            }
        } catch (JSONException e) {
            throw new MissingValue();
        }
    }

    public void setLong(String key, long value) {
        NaturalPrimaryKey npk = new NaturalPrimaryKey();
        npk.set("key", key);
        JSONObject obj = new JSONObject();
        try {
            obj.put("key", key);
            obj.put("value", "" + value);

            upsert(npk, obj);
        } catch (JSONException e) {
            Log.error("failed to insert");
        }

    }

    public long getLong(String key) throws MissingValue {
        try {
            NaturalPrimaryKey npk1 = new NaturalPrimaryKey();
            npk1.set("key", key);
            Cursor cursor = this.select(npk1, 1, 0);
            JSONObject obj = this.getFirstObject(cursor);
            if (obj != null) {
                return Long.parseLong(obj.getString("value"));
            } else {
                throw new MissingValue();
            }
        } catch (JSONException e) {
            throw new MissingValue();
        }
    }
    public class MissingValue extends Exception {
    }
}
