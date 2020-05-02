package com.github.nicksetzer.daedalus.orm;

import java.util.HashMap;
import java.util.Set;

public class NaturalPrimaryKey extends HashMap<String,Object> implements INaturalPrimaryKey {

    public NaturalPrimaryKey() {
        super();
    }

    public boolean containsKey(String key) {
        return super.containsKey(key);
    }

    public Object get(String key) {
        return super.get(key);
    }

    public Set<String> keySet() {
        return super.keySet();
    }

    public void set(String key, Object value) {
        super.put(key, value);
    }
}
