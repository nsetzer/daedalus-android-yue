package com.github.nicksetzer.daedalus.audio;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SearchTest {


    @Test
    public void test_singleWord() {

        List<String> params = new ArrayList<>();
        String sql = Search.parse("foo", params);
        assertEquals("((artist LIKE ? || album LIKE ? || title LIKE ? || genre LIKE ? || comment LIKE ?))", sql);
        assertEquals(5, params.size());
    }

    @Test
    public void test_numeric() {

        List<String> params = new ArrayList<>();
        String sql = Search.parse("rating > 5", params);
        assertEquals("(rating > 5)", sql);
        assertEquals(0, params.size());
    }

    @Test
    public void test_string() {

        List<String> params = new ArrayList<>();
        String sql = Search.parse("artist = stone", params);
        assertEquals("(artist LIKE ?)", sql);
        assertEquals(1, params.size());
    }


    @Test
    public void test_combined1() {

        List<String> params = new ArrayList<>();
        String sql = Search.parse("abc rating >= 5", params);
        assertEquals("((artist LIKE ? OR album LIKE ? OR title LIKE ? OR genre LIKE ? OR comment LIKE ?) AND rating >= 5)", sql);
        assertEquals(1, params.size());
    }

}
