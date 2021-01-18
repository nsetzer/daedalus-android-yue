package com.github.nicksetzer.daedalus.audio;

import com.github.nicksetzer.metallurgy.orm.dsl.DslException;
import com.github.nicksetzer.metallurgy.orm.dsl.Token;
import com.github.nicksetzer.metallurgy.orm.dsl.Pair;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SongTableDslTest {

    @Test
    public void test_simple() throws DslException {

        Token token = SongTableDsl.parse("");

        Pair<String, List<String>> result = SongTableDsl.transform(token);

        System.out.println(result.first);
        System.out.println(result.second);

    }

}
