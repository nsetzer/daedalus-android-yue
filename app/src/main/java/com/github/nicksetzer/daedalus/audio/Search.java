package com.github.nicksetzer.daedalus.audio;

import android.app.PendingIntent;

import com.github.nicksetzer.daedalus.Log;

import java.util.ArrayList;
import java.util.List;

public class Search {

    /**
     * quick and dirty string tokenizer, separate on double quotes and
     * on operators, every token must be separated by white space
     *
     */
    private static String[] breakString(String query) {
        String[] qparts = query.split("\"");
        List<String> parts = new ArrayList<>();
        for (int i=0; i < qparts.length; i+=1) {
            if (i%2==0) {

                for (String s : qparts[i].split("\\s+")) {
                    if (s.length() > 0) {
                        parts.add(s);
                    }
                }

            } else if (qparts[i].length() > 0) {
                // when correctly quoted, every other section should be within quotes
                parts.add(qparts[i]);
            }
        }

        return parts.toArray(new String[]{});
    }

    /**
     * convert a query into SQL
     */
    public static String parse(String query, List<String> params) {

        Token tok = new Token(breakString(query));

        try {
            for (int i = 0; i < tok.children.size(); ) {

                if (tok.children.get(i).type == Token.T_OPERATOR) {
                    Token mid = tok.children.get(i);
                    Token rhs = tok.children.remove(i + 1);
                    Token lhs = tok.children.remove(i - 1);
                    mid.children.add(lhs);
                    mid.children.add(rhs);
                } else {
                    i += 1;
                }

            }

            for (int i = 0; i < tok.children.size(); i++) {

                if (tok.children.get(i).type == Token.T_VALUE) {
                    Token rhs = tok.children.get(i);
                    Token mid = new Token(Token.T_OPERATOR, "=");
                    Token lhs = new Token(Token.T_VALUE, "text");
                    mid.children.add(lhs);
                    mid.children.add(rhs);
                    tok.children.set(i, mid);
                }

            }

        } catch (Exception e) {
            Log.error("parse error", e);
            return "";
        }

        String s = tok.toString(params);
        return s;
    }

    private static class Token {

        public static final int T_COLLECTION = 0;
        public static final int T_AND = 1;
        public static final int T_OR = 2;
        public static final int T_VALUE = 3;
        public static final int T_OPERATOR = 4;

        public int type;
        public String value;
        public List<Token> children;

        public Token() {
            type = T_COLLECTION;
            value = "";
            children = new ArrayList<>();
        }

        public Token(int t, String v) {
            type = t;
            value = v;
            children = new ArrayList<>();
        }

        public Token(String[] parts) {
            type = T_COLLECTION;
            value = "";
            children = new ArrayList<>();

            for (String p : parts) {
                int t = 0;
                if (p.equals("<") || p.equals("<=") || p.equals("=") || p.equals("==") || p.equals(">=") || p.equals(">")) {
                    t = T_OPERATOR;
                } else {
                    t = T_VALUE;
                }
                children.add(new Token(t, p));
            }
        }

        public String toString(List<String> params) {

            switch (type) {
                case T_COLLECTION:
                    if (children.size()==0) {
                        return "";
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("(");
                    boolean first = true;
                    for (Token c : children) {
                        if (!first) {
                            sb.append (" AND ");
                        }
                        sb.append(c.toString(params));
                        first = false;
                    }
                    sb.append(")");
                    return sb.toString();
                case T_VALUE:
                    return "error";
                case T_OPERATOR:
                    String lhs = children.get(0).value;
                    String rhs = children.get(1).value;
                    if (lhs.equals("text")) {
                        rhs = "%" + rhs + "%";
                        params.add(rhs);
                        params.add(rhs);
                        params.add(rhs);
                        params.add(rhs);
                        params.add(rhs);

                        return "(artist LIKE ? OR album LIKE ? OR title LIKE ? OR genre LIKE ? OR comment LIKE ?)";
                    } else if (lhs.equals("artist") || lhs.equals("album") || lhs.equals("title") || lhs.equals("genre") || lhs.equals("comment") || lhs.equals("language")) {
                        params.add("%" + rhs + "%");
                        return  lhs + " LIKE ?";
                    } else {
                        return  lhs + " " + value + " " + rhs;
                    }

                default:
                    return "error";
            }
        }

    }
}
