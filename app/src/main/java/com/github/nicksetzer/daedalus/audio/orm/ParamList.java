package com.github.nicksetzer.daedalus.audio.orm;

import java.util.ArrayList;

public class ParamList extends ArrayList<String> {

    public ParamList() {
        super();
    }

    public ParamList(String[] seq) {
        super();
        for (String obj : seq) {
            this.add(obj);
        }
    }

    public ParamList(Object[] seq) {
        super();
        for (Object obj : seq) {
            this.add(obj.toString());
        }
    }
}
