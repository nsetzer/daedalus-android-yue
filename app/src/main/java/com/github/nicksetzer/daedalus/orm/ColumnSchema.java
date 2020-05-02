package com.github.nicksetzer.daedalus.orm;

public class ColumnSchema {

    public String name;
    public String type;
    public boolean index;

    public ColumnSchema(String name, String type) {
        this.name = name;
        this.type = type;
        this.index = false;
    }

    public ColumnSchema(String name, String type, boolean index) {
        this.name = name;
        this.type = type;
        this.index = index;
    }
}
