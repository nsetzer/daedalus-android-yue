package com.github.nicksetzer.daedalus.audio.orm;
/**
 * run-as com.github.nicksetzer.daedalus
 * cd /data/data/com.github.nicksetzer.daedalus
 * cd /storage/emulated/0/Android/data/com.github.nicksetzer.daedalus/files/app-v1.sqlite
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;

public class Database {

    TableSchema[] buildTestSchema() {
        TableSchema table = new TableSchema("songs");

        table.addColumn("spk", "INTEGER PRIMARY KEY AUTOINCREMENT");
        table.addColumn("artist", "VARCHAR", true);
        table.addColumn("album", "VARCHAR");
        table.addColumn("title", "VARCHAR");

        TableSchema[] schema = new TableSchema[]{table};

        return schema;
    }

    @Test
    public void test_prepareTableSchema() {

        TableSchema[] schema = buildTestSchema();

        Statement[] statements = StatementBuilder.prepareTableSchema(schema);

        for (Statement statement : statements) {
            System.out.print(statement.text);
        }

        assertEquals(4, 4);

    }

    @Test
    public void test_prepareInsert() {

        TableSchema[] schema = buildTestSchema();

        JSONObject values = new JSONObject();
        try {
            values.put("artist", "test");
            values.put("album", "test");
        } catch (JSONException e) {
            fail(e.getMessage());
        }

        Statement statement = StatementBuilder.prepareInsert(schema[0], values);

        System.out.print(statement.text);

        assertEquals(statement.text, "INSERT INTO songs (artist, album, title) VALUES (?, ?, ?)");
        assertEquals(3, statement.params.size());


    }

    @Test
    public void test_prepareInsertBulk() {

        TableSchema[] schema = buildTestSchema();

        JSONObject obj = new JSONObject();
        try {
            obj.put("test", "Test");
        } catch (JSONException e) {

        }

        JSONObject values = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            values.put("artist", "test");
            values.put("album", "test");

            array.put(values);
            array.put(values);
        } catch (JSONException e) {
            fail(e.getMessage());
        }

        Statement statement = StatementBuilder.prepareInsertBulk(schema[0], array);

        System.out.print(statement.text);

        assertEquals(statement.text, "INSERT INTO songs (artist, album, title) VALUES (?, ?, ?), (?, ?, ?)");


    }

    @Test
    public void test_prepareUpdate() {

        TableSchema[] schema = buildTestSchema();

        JSONObject obj = new JSONObject();
        try {
            obj.put("test", "Test");
        } catch (JSONException e) {

        }

        JSONObject item = new JSONObject();
        try {
            item.put("artist", "test");
            item.put("album", "test");
        } catch (JSONException e) {
            fail(e.getMessage());
        }

        Statement statement = StatementBuilder.prepareUpdate(schema[0], 0, item);

        System.out.print(statement.text);

        assertEquals(statement.text, "UPDATE songs SET (artist, album) = (?, ?) WHERE (songs.spk == ?)");


    }

    @Test
    public void test_prepareDelete() {

        TableSchema[] schema = buildTestSchema();

        Statement statement = StatementBuilder.prepareDelete(schema[0], 1);

        System.out.print(statement.text);

        assertEquals(statement.text, "DELETE FROM songs WHERE (spk == ?)");
    }

    @Test
    public void test_prepareDeleteBulk() {

        TableSchema[] schema = buildTestSchema();

        Statement statement = StatementBuilder.prepareDeleteBulk(schema[0], new long[]{1,2,3});

        System.out.print(statement.text);

        assertEquals(statement.text, "DELETE FROM songs WHERE spk in (?, ?, ?)");
    }

    @Test
    public void test_prepareExists() {

        TableSchema[] schema = buildTestSchema();

        Statement statement = StatementBuilder.prepareExists(schema[0], "artist", "test");

        System.out.print(statement.text);

        assertEquals(statement.text, "SELECT spk, artist FROM songs WHERE (artist == ?)");
    }

    @Test
    public void test_prepareExistsBulk() {

        TableSchema[] schema = buildTestSchema();

        Statement statement = StatementBuilder.prepareExistsBulk(schema[0], "artist", new Object[]{"test1", "test2"});

        System.out.print(statement.text);

        assertEquals(statement.text, "SELECT spk, artist FROM songs WHERE artist IN (?, ?)");
    }

    @Test
    public void test_prepareUpsertSelect() {

        TableSchema[] schema = buildTestSchema();

        NaturalPrimaryKey npk = new NaturalPrimaryKey();
        npk.set("artist", "test");

        Statement statement = StatementBuilder.prepareUpsertSelect(schema[0], npk);

        System.out.print(statement.text);

        assertEquals(statement.text, "SELECT * FROM songs WHERE (artist == ?) LIMIT 1");
    }

    @Test
    public void test_prepareUpsertSelect2() {

        TableSchema[] schema = buildTestSchema();

        NaturalPrimaryKey npk = new NaturalPrimaryKey();
        npk.set("artist", "test");
        npk.set("album", "test");

        Statement statement = StatementBuilder.prepareUpsertSelect(schema[0], npk);

        System.out.print(statement.text);

        assertEquals(statement.text, "SELECT * FROM songs WHERE (artist == ? && album == ?) LIMIT 1");
    }
}
