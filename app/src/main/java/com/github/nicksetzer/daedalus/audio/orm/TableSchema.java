package com.github.nicksetzer.daedalus.audio.orm;

import java.util.ArrayList;

public class TableSchema {
    public String name;
    public ArrayList<ColumnSchema> columns;

    public TableSchema(String name) {
        this.name = name;
        this.columns = new ArrayList<>();
    }

    public void addColumn(String name, String type) {
        this.columns.add(new ColumnSchema(name, type));
    }

    public void addColumn(String name, String type, boolean index) {
        this.columns.add(new ColumnSchema(name, type, index));
    }

}
