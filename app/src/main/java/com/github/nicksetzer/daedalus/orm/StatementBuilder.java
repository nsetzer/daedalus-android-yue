package com.github.nicksetzer.daedalus.orm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

public class StatementBuilder {

    public static final String SURROGATE_PRIMARY_KEY = "spk";

    public static String marshall(ParamList params, Object value) {

        if (value == null) {
            return "NULL";
        } else if (value instanceof Integer) {
            return value.toString();
        } else if (value instanceof Long) {
            return value.toString();
        } else if (value instanceof Float) {
            return value.toString();
        }else if (value instanceof Double) {
            return value.toString();
        } else if (value instanceof String) {
            params.add(value.toString());
            return "?";
        }

        throw new RuntimeException("Unexpected Type: " + value.getClass().getSimpleName());
    }

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

            if (!item.has(column.name)) {
                continue;
            }

            if (!first) {
                columns.append(", ");
                template.append(", ");
            }

            columns.append(column.name);

            Object value;
            try{
                value = item.get(column.name);
            } catch (JSONException e) {
                value = null;
            }

            template.append(marshall(params, value));

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
                    template.append(", ");
                }

                columns.append(column.name);

                Object value;
                try {
                    value = item.get(column.name);
                } catch (JSONException e) {
                    value = null;
                }
                template.append(marshall(params, value));

                first = false;
            }
        }

        clause.append(table.name);
        clause.append(".spk == ");
        clause.append(marshall(params, spk));

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

    public static Statement prepareUpdateSet1(TableSchema table, Long[] spks, String columnName, Object columnValue) {
        ParamList params = new ParamList();
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE ");
        builder.append(table.name);
        builder.append(" SET ");
        builder.append(columnName);
        builder.append(" = ");
        builder.append(marshall(params, columnValue));
        builder.append(" WHERE (");
        builder.append(SURROGATE_PRIMARY_KEY);
        builder.append(" in (");
        for (int i=0; i < spks.length; i++) {
            if (i>0) {
                builder.append(", ");
            }
            builder.append(marshall(params, spks[i]));
        }
        builder.append("))");

        return new Statement(builder.toString(), params);
    }

    public static Statement prepareDelete(TableSchema table, long spk) {
        ParamList params = new ParamList();
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM ");
        builder.append(table.name);
        builder.append(" WHERE (spk == ");
        builder.append(marshall(params, spk));
        builder.append(")");
        return new Statement(builder.toString(), params);
    }

    public static Statement prepareDeleteBulk(TableSchema table, long[] spks) {
        // no params required since all long values can be embedded
        StringBuilder template = new StringBuilder();
        for (int i=0; i < spks.length; i += 1) {
            if (i>0) {
                template.append(", ");
            }
            template.append(spks[i]);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM ");
        builder.append(table.name);
        builder.append(" WHERE spk in (");
        builder.append(template.toString());
        builder.append(")");
        return new Statement(builder.toString());
    }

    public static Statement prepareSelect(TableSchema table, INaturalPrimaryKey npk, long limit, long offset) {

        ParamList params = new ParamList();
        StringBuilder clause = new StringBuilder();
        boolean first = true;
        for (String key  : npk.keySet()) {
            if (!first) {
                clause.append(" && ");
            }
            clause.append(key);
            clause.append(" == ");
            clause.append(marshall(params, npk.get(key)));
            first = false;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * FROM ");
        builder.append(table.name);
        builder.append(" WHERE (");
        builder.append(clause.toString());
        builder.append(")");
        if (limit >= 0) {
            builder.append(" LIMIT ");
            builder.append(limit);
        }
        if (offset >= 0) {
            builder.append(" OFFSET ");
            builder.append(offset);
        }

        return new Statement(builder.toString(), params);
    }

    public static Statement prepareSelect(TableSchema table, String[] columns, INaturalPrimaryKey npk, long limit, long offset) {

        ParamList params = new ParamList();
        StringBuilder clause = new StringBuilder();
        boolean first = true;
        for (String key  : npk.keySet()) {
            if (!first) {
                clause.append(" && ");
            }
            clause.append(key);
            clause.append(" == ");
            clause.append(marshall(params, npk.get(key)));
            first = false;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("SELECT ");
        for (int i=0; i < columns.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(columns[i]);
        }
        builder.append(" FROM ");
        builder.append(table.name);
        builder.append(" WHERE (");
        builder.append(clause.toString());
        builder.append(")");
        if (limit >= 0) {
            builder.append(" LIMIT ");
            builder.append(limit);
        }
        if (offset >= 0) {
            builder.append(" OFFSET ");
            builder.append(offset);
        }

        return new Statement(builder.toString(), params);
    }
    public static Statement prepareExists(TableSchema table, String columnName, Object columnValue) {
        ParamList params = new ParamList();
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT spk, ");
        builder.append(columnName);
        builder.append(" FROM ");
        builder.append(table.name);
        builder.append(" WHERE (");
        builder.append(columnName);
        builder.append(" == ");
        builder.append(marshall(params, columnValue));
        builder.append(")");
        return new Statement(builder.toString(), params);
    }

    public static Statement prepareExistsBulk(TableSchema table, String columnName, Object[] columnValues) {
        //ParamList params = new ParamList(columnValues); FIXME
        ParamList params = new ParamList();
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
            if (!first) {
                builder.append(", ");
            }
            builder.append(marshall(params, value));
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
            if (!first) {
                clause.append(" && ");
            }
            clause.append(key);
            clause.append(" == ");
            clause.append(marshall(params, npk.get(key)));
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

    /**
     * Wrap an instance of an object in an interface at run time.
     * @param obj      the object instance which impliments traits of an interface
     * @param intface  the interface
     * @param <T>      the class type of interface
     *
     * usage:
     *
     *    getWrapper(new MyObject(), MyInterface.class);
     *
     * @return an object implementing the given interface
     */
    public static <T> T getWrapper(final Object obj, final Class<T> intface) {
        InvocationHandler invocationHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return obj.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes()).invoke(obj, args);
            }
        };
        return (T) Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class[]{intface}, invocationHandler);
    }
}
