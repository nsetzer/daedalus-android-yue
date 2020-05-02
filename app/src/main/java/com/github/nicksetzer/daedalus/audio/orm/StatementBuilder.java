package com.github.nicksetzer.daedalus.audio.orm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class StatementBuilder {

    public static final String SURROGATE_PRIMARY_KEY = "spk";

    public static Statement[] prepareTableSchema(TableSchema[] schema) {
        ArrayList<Statement> statements = new ArrayList<>();

        for (TableSchema table : schema) {

            // construct a statement for creating a table with the specified columns
            StringBuilder builder = new StringBuilder();

            builder.append("CREATE TABLE IF NOT EXISTS '");
            builder.append(table.name);
            builder.append("' ");

            builder.append("(");
            boolean first = true;
            for (ColumnSchema column : table.columns) {
                if (!first) {
                    builder.append(", ");
                }
                builder.append(column.name);
                builder.append(" ");
                builder.append(column.type);
                first = false;
            }
            builder.append(")");

            statements.add(new Statement(builder.toString()));

            statements.addAll(prepareColumnSchema(table));

        }

        return statements.toArray(new Statement[]{});
    }

    public static ArrayList<Statement> prepareColumnSchema(TableSchema table) {
        ArrayList<Statement> statements = new ArrayList<>();
        // build statements to create an index for specified columns
        for (ColumnSchema column : table.columns) {
            if (column.index) {
                StringBuilder builder = new StringBuilder();
                // the index name is "ix_${table}_${column}
                builder.append("CREATE INDEX IF NOT EXISTS 'ix_");
                builder.append(table.name);
                builder.append("_");
                builder.append(column.name);
                builder.append("' ON '");
                // index is created on ${table} ${column}
                builder.append(table.name);
                builder.append("' (");
                builder.append(column.name);
                builder.append(")");

                statements.add(new Statement(builder.toString()));
            }
        }

        return statements;
    }

    public static Statement prepareInsert(TableSchema table, JSONObject item) {
        ParamList params = new ParamList();
        StringBuilder columns = new StringBuilder();
        StringBuilder template = new StringBuilder();

        boolean first = true;
        for (ColumnSchema column : table.columns) {
            if (column.name.equals(SURROGATE_PRIMARY_KEY)) {
                continue;
            }

            if (!first) {
                columns.append(", ");
                template.append(", ?");
            } else {
                template.append("?");
            }

            columns.append(column.name);

            if (item.has(column.name)) {
                try {
                    params.add(item.get(column.name).toString());
                } catch (JSONException e) {
                    params.add(null);
                }
            } else {
                params.add(null);
            }

            first = false;
        }

        StringBuilder builder = new StringBuilder();

        builder.append("INSERT INTO ");
        builder.append(table.name);
        builder.append(" (");
        builder.append(columns.toString());
        builder.append(") VALUES (");
        builder.append(template.toString());
        builder.append(")");

        return new Statement(builder.toString(), params);
    }

    public static Statement prepareInsertBulk(TableSchema table, JSONArray items) {

        StringBuilder builder = new StringBuilder();
        ParamList params = new ParamList();

        StringBuilder columns = new StringBuilder();
        StringBuilder template = new StringBuilder();

        template.append("(");

        boolean first = true;

        for (ColumnSchema column : table.columns) {
            if (column.name.equals(SURROGATE_PRIMARY_KEY)) {
                continue;
            }

            if (!first) {
                columns.append(", ");
                template.append(", ?");
            } else {
                template.append("?");
            }

            columns.append(column.name);

            first = false;
        }
        template.append(")");

        builder.append("INSERT INTO ");
        builder.append(table.name);
        builder.append(" (");
        builder.append(columns.toString());
        builder.append(") VALUES ");

        first = true;

        for (int i=0; i < items.length(); i += 1) {

            JSONObject item;
            try {
                item = items.getJSONObject(i);
            } catch (JSONException e) {
                continue;
            }

            for (ColumnSchema column : table.columns) {
                if (column.name.equals(SURROGATE_PRIMARY_KEY)) {
                    continue;
                }

                if (item.has(column.name)) {
                    try {
                        params.add(item.get(column.name).toString());
                    } catch (JSONException e) {
                        params.add(null);
                    }
                } else {
                    params.add(null);
                }
            }

            if (!first) {
                builder.append(", ");
            }
            builder.append(template.toString());

            first = false;
        }

        return new Statement(builder.toString(), params);
    }

    /**
     *
     * @param table
     * @param spk
     * @param item
     *
     * TODO: support update by npk: {columnName1: value1, [columnNameN: valueN...]}
     *       such that the where clause is modified to be columnName1 == value1
     * @return
     */
    public static Statement prepareUpdate(TableSchema table, long spk, JSONObject item) {

        ParamList params = new ParamList();
        StringBuilder columns = new StringBuilder();
        StringBuilder template = new StringBuilder();
        StringBuilder clause = new StringBuilder();

        boolean first = true;
        for (ColumnSchema column : table.columns) {
            if (column.name.equals(SURROGATE_PRIMARY_KEY)) {
                continue;
            }

            if (item.has(column.name)) {

                if (!first) {
                    columns.append(", ");
                    template.append(", ?");
                } else {
                    template.append("?");
                }

                columns.append(column.name);

                try {
                    params.add(item.get(column.name).toString());
                } catch (JSONException e) {
                    params.add(null);
                }

                first = false;
            }
        }

        clause.append(table.name);
        clause.append(".spk == ?");
        params.add(Long.toString(spk));

        StringBuilder builder = new StringBuilder();

        builder.append("UPDATE ");
        builder.append(table.name);
        builder.append(" SET (");
        builder.append(columns.toString());
        builder.append(") = (");
        builder.append(template.toString());
        builder.append(") WHERE (");
        builder.append(clause.toString());
        builder.append(")");

        return new Statement(builder.toString(), params);
    }

    public static Statement prepareDelete(TableSchema table, long spk) {
        ParamList params = new ParamList();
        params.add(Long.toString(spk));
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM ");
        builder.append(table.name);
        builder.append(" WHERE (spk == ?)");
        return new Statement(builder.toString(), params);
    }

    public static Statement prepareDeleteBulk(TableSchema table, long[] spks) {
        StringBuilder template = new StringBuilder();
        for (int i=0; i < spks.length; i += 1) {
            if (i>0) {
                template.append(", ");
            }
            template.append("?");
        }

        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM ");
        builder.append(table.name);
        builder.append(" WHERE spk in (");
        builder.append(template.toString());
        builder.append(")");
        return new Statement(builder.toString());
    }

    public static Statement prepareExists(TableSchema table, String columnName, Object columnValue) {
        ParamList params = new ParamList();
        params.add(columnValue.toString());

        StringBuilder builder = new StringBuilder();
        builder.append("SELECT spk, ");
        builder.append(columnName);
        builder.append(" FROM ");
        builder.append(table.name);
        builder.append(" WHERE (");
        builder.append(columnName);
        builder.append(" == ?)");
        return new Statement(builder.toString(), params);
    }

    public static Statement prepareExistsBulk(TableSchema table, String columnName, Object[] columnValues) {
        ParamList params = new ParamList(columnValues);
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT spk, ");
        builder.append(columnName);
        builder.append(" FROM ");
        builder.append(table.name);
        builder.append(" WHERE ");
        builder.append(columnName);
        builder.append(" IN (");
        boolean first = true;
        for (Object value : columnValues) {
            params.add(value.toString());
            if (!first) {
                builder.append(", ");
            }
            builder.append("?");
            first = false;
        }
        builder.append(")");
        return new Statement(builder.toString(), params);
    }

    public static Statement prepareUpsertSelect(TableSchema table, INaturalPrimaryKey npk) {

        ParamList params = new ParamList();
        StringBuilder clause = new StringBuilder();
        boolean first = true;
        for (String key  : npk.keySet()) {
            params.add(npk.get(key).toString());

            if (!first) {
                clause.append(" && ");
            }
            clause.append(key);
            clause.append(" == ?");
            first = false;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * FROM ");
        builder.append(table.name);
        builder.append(" WHERE (");
        builder.append(clause.toString());
        builder.append(") LIMIT 1");

        return new Statement(builder.toString(), params);

    }
}
