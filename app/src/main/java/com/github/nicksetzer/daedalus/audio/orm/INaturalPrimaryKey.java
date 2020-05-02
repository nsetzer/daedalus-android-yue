package com.github.nicksetzer.daedalus.audio.orm;

import java.util.Set;

public interface INaturalPrimaryKey {

    boolean containsKey(String key);

    Object get(String key);

    Set<String> keySet();
}
