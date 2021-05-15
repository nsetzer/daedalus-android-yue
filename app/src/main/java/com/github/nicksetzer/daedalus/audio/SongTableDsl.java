package com.github.nicksetzer.daedalus.audio;

import com.github.nicksetzer.metallurgy.orm.dsl.DslException;
import com.github.nicksetzer.metallurgy.orm.dsl.Pair;
import com.github.nicksetzer.metallurgy.orm.dsl.Position;
import com.github.nicksetzer.metallurgy.orm.dsl.QDateTime;
import com.github.nicksetzer.metallurgy.orm.dsl.QueryParser;
import com.github.nicksetzer.metallurgy.orm.dsl.QueryTransform;
import com.github.nicksetzer.metallurgy.orm.dsl.StringUtil;
import com.github.nicksetzer.metallurgy.orm.dsl.Token;
import com.github.nicksetzer.metallurgy.orm.dsl.TokenKind;

import java.util.ArrayList;
import java.util.List;

public class SongTableDsl {

    private static class SongTableQueryTransform extends QueryTransform {

        SongTableQueryTransform() {

            // boolean
            addColumnDef("valid", new String[]{"valid"}, SqlType.INTEGER);
            addColumnDef("sync", new String[]{"sync"}, SqlType.INTEGER);
            addColumnDef("synced", new String[]{"synced"}, SqlType.INTEGER);

            // number
            addColumnDef("play_count", new String[]{"pcnt", "play_count"}, SqlType.INTEGER);
            addColumnDef("skip_count", new String[]{"scnt", "skip_count"}, SqlType.INTEGER);
            addColumnDef("album_index", new String[]{"index", "album_index"}, SqlType.INTEGER);
            addColumnDef("rating", new String[]{"rating", "rte"}, SqlType.INTEGER);

            // year
            addColumnDef("year", new String[]{"year"}, SqlType.INTEGER);

            // duration
            addColumnDef("length", new String[]{"length", "duration"}, SqlType.INTEGER);

            // date
            addColumnDef("date_added", new String[]{"added"}, SqlType.EPOCHTIME_SECONDS);
            addColumnDef("last_played", new String[]{"played", "p"}, SqlType.EPOCHTIME_SECONDS);

            // text
            addColumnDef("artist", new String[]{"art", "artist"}, SqlType.STRING);
            addColumnDef("album", new String[]{"alb", "album"}, SqlType.STRING);
            addColumnDef("title", new String[]{"ttl", "tit", "title"}, SqlType.STRING);
            addColumnDef("composer", new String[]{"composer"}, SqlType.STRING);
            addColumnDef("country", new String[]{"country"}, SqlType.STRING);
            addColumnDef("comment", new String[]{"comment"}, SqlType.STRING);
            addColumnDef("language", new String[]{"language", "lang"}, SqlType.STRING);
            addColumnDef("genre", new String[]{"genre"}, SqlType.STRING);

            // not used
            /*
            songs.addColumn("file_size", "INTEGER");
            songs.addColumn("art_size", "INTEGER");
            songs.addColumn("file_path", "VARCHAR");
            songs.addColumn("art_path", "VARCHAR");
            */

            enableAllText(new String[]{
                    "artist", "album", "title", "composer",
                    "country", "comment", "language", "genre"
            });
        }

    }

    static Token parse(String text, QDateTime now) throws DslException {
        QueryParser parser = new QueryParser();
        parser.setCurrentDateTime(now);
        return parser.parse(text);
    }

    static Pair<String, List<String>> transform(Token token, QDateTime now) throws DslException {
        SongTableQueryTransform xform = new SongTableQueryTransform();
        xform.setCurrentDateTime(now);
        return xform.transform(token);
    }

    static Token and_(Token lhs, Token rhs) {
        if (lhs == null) {
            return rhs;
        }
        if (rhs == null) {
            return lhs;
        }

        if (lhs.kind() != TokenKind.P_GROUPING || !lhs.value().equals("()")) {
            List<Token> children = new ArrayList<>();
            children.add(lhs);
            lhs = new Token(TokenKind.P_GROUPING, "()", new Position(0,0), children);
        }

        if (rhs.kind() != TokenKind.P_GROUPING || !rhs.value().equals("()")) {
            List<Token> children = new ArrayList<>();
            children.add(rhs);
            rhs = new Token(TokenKind.P_GROUPING, "()", new Position(0,0), children);
        }

        List<Token> children = new ArrayList<>();
        children.add(lhs);
        children.add(rhs);
        return new Token(TokenKind.P_LOGICAL_AND, "&&", new Position(0,0), children);
    }

    static Token or_(Token lhs, Token rhs) {
        if (lhs == null) {
            return rhs;
        }
        if (rhs == null) {
            return lhs;
        }

        if (lhs.kind() != TokenKind.P_GROUPING || !lhs.value().equals("()")) {
            List<Token> children = new ArrayList<>();
            children.add(lhs);
            lhs = new Token(TokenKind.P_GROUPING, "()", new Position(0,0), children);
        }

        if (rhs.kind() != TokenKind.P_GROUPING || !rhs.value().equals("()")) {
            List<Token> children = new ArrayList<>();
            children.add(rhs);
            rhs = new Token(TokenKind.P_GROUPING, "()", new Position(0,0), children);
        }

        List<Token> children = new ArrayList<>();
        children.add(lhs);
        children.add(rhs);
        return new Token(TokenKind.P_LOGICAL_OR, "||", new Position(0,0), children);
    }

    static Token not_(Token token) {
        List<Token> children = new ArrayList<>();
        children.add(token);
        return new Token(TokenKind.P_LOGICAL_NOT, "!", new Position(0,0), children);
    }

    static Token compare(String column_alias, String operator, Token rhs) {
        List<Token> children = new ArrayList<>();
        children.add(new Token(TokenKind.L_IDENTIFIER, column_alias, new Position(0,0)));
        children.add(rhs);
        return new Token(TokenKind.P_COMPARE, operator, new Position(0,0), children);
    }

    static Token string_(String value) {
        String str_value = StringUtil.escape(value);
        return new Token(TokenKind.L_NUMBER, str_value, new Position(0,0));
    }

    static Token number(int value) {
        if (value < 0) {
            String str_value = Integer.toString(-value);
            Token token = new Token(TokenKind.L_NUMBER, str_value, new Position(0,0));

            List<Token> children = new ArrayList<>();
            children.add(token);
            return new Token(TokenKind.P_UNARY, "-", new Position(0,0), children);
        } else {
            String str_value = Integer.toString(value);
            return new Token(TokenKind.L_NUMBER, str_value, new Position(0,0));
        }
    }
}
