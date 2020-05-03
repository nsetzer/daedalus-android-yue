package com.github.nicksetzer.daedalus.orm;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;

//https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase
/*

        Cursor c = m_db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        if (c.moveToFirst()) {
            while ( !c.isAfterLast() ) {
                android.util.Log.e( "daedalus-js", "EntityTable Name=> "+c.getString(0));
                c.moveToNext();
            }
        }

if (c.moveToFirst()) {
  while(!c.isAfterLast()) {
    ...
    c.moveToNext();
  }
}

use beginTransactionNonExclusive() for read operations
 */
public class DatabaseConnection {

    SQLiteDatabase m_db;
    String m_path;
    TableSchema[] m_schema;

    public DatabaseConnection(String path, TableSchema[] schema) {
        m_db = null;
        m_path = path;
        m_schema = schema;
    }

    public void connect() {


/*
        File file = new File(m_path);
        if (file.exists()) {
            android.util.Log.e("daedalus-js", "removing database at: " + m_path);
            file.delete();
        }
*/

        android.util.Log.e("daedalus-js", "opening database at: " + m_path);
        m_db = SQLiteDatabase.openOrCreateDatabase(m_path, null);

        //m_db.enableWriteAheadLogging();

        Statement[] statements = StatementBuilder.prepareTableSchema(m_schema);

        beginTransaction(false);

        boolean success = false;
        try {
            executeBatch(statements);
            success = true;
        } catch (Exception e) {
            android.util.Log.e("daedalus-js", "error initializing database: " + e.getMessage());
        } finally{
            endTransaction(success);
        }

        listTables();

    }

    private void listTables() {
        Cursor c = m_db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        if (c.moveToFirst()) {
            while ( !c.isAfterLast() ) {
                android.util.Log.e( "daedalus-js", "EntityTable Name: "+c.getString(0));
                c.moveToNext();
            }
        }
    }

    public void close() {
        if (m_db != null && m_db.isOpen()) {
            m_db.close();
        }
    }

    /**
     * execute a single SQL statement that does not return data
     *
     * @param sql
     * @param args
     */
    public void execute(String sql, String[] args) {
        android.util.Log.d("daedalus-js", "execute: " + sql);
        m_db.execSQL(sql, args);
    }

    /**
     * execute a single SQL statement that does not return data
     *
     * @param statement a statement to execute
     */
    public void execute(Statement statement) {
        android.util.Log.d("daedalus-js", "execute: " + statement.text);
        m_db.execSQL(statement.text, statement.params.toArray(new String[]{}));
    }

    /**
     *
     * execute a batch of SQL statements that do not return data
     *
     * @param statements
     */
    public void executeBatch(Statement[] statements) {
        for (Statement statement : statements) {
            execute(statement);
        }
    }

    /**
     *
     * execute a single INSERT/UPDATE/SELECT/DELETE
     *
     * @param sql
     * @param args
     * @return
     */
    public Cursor query(String sql, String[] args) {
        android.util.Log.d("daedalus-js", "query: " + sql);
        return m_db.rawQuery(sql, args);
    }

    /**
     * execute a single INSERT/UPDATE/SELECT/DELETE
     *
     * @param statement
     * @return
     */
    public Cursor query(Statement statement) {
        android.util.Log.d("daedalus-js", "query: " + statement.text);
        ArrayList<String> params = new ArrayList<>();
        for (Object obj : statement.params) {
            params.add(obj.toString());
        }
        return m_db.rawQuery(statement.text, params.toArray(new String[]{}));
    }

    /**
     *
     * begin an exclusive transaction
     *
     */
    public void beginTransaction() {
        m_db.beginTransaction();
        android.util.Log.e("daedalus-js", "transaction opened. exclusive: true");
    }

    /**
     *
     * begin a transaction
     *
     * @param exclusive if true, begin an exclusive transaction otherwise a non-exclusive transaction
     */
    public void beginTransaction(boolean exclusive) {
        if (exclusive) {
            m_db.beginTransaction();
        } else {
            m_db.beginTransactionNonExclusive();
        }
        android.util.Log.d("daedalus-js", "transaction opened. exclusive: " + exclusive);
    }

    /**
     *
     * end a transaction
     *
     */
    public void endTransaction(boolean success) {
        if (success) {
            m_db.setTransactionSuccessful();
        }
        m_db.endTransaction();
        android.util.Log.d("daedalus-js", "transaction closed. success: " + success);
    }

}
