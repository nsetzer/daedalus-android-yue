package com.github.nicksetzer.daedalus.api;

public class URLParamEncoder {

    public static String encode(String input) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (isUnsafe(ch)) {
                resultStr.append('%');
                resultStr.append(toHex(ch / 16));
                resultStr.append(toHex(ch % 16));
            } else {
                resultStr.append(ch);
            }
        }
        return resultStr.toString();
    }

    private static char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    private static boolean isUnsafe(char ch) {
        if (ch > 128 || ch < 0)
            return true;
        return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
    }

    public static class Builder {

        StringBuilder m_sb;
        boolean m_first;

        public Builder() {
            m_sb = new StringBuilder();
            m_first = true;
        }

        public void add(String key, String val) {

            if (m_first) {
                m_sb.append("?");
            } else {
                m_sb.append("&");
            }

            m_first = false;

            m_sb.append(encode(key));
            m_sb.append("=%22");
            m_sb.append(encode(val));
            m_sb.append("%22");
        }

        public void add(String key, int val) {

            if (m_first) {
                m_sb.append("?");
            } else {
                m_sb.append("&");
            }

            m_first = false;

            m_sb.append(encode(key));
            m_sb.append("=");
            m_sb.append(val);
        }

        public void add(String key, long val) {

            if (m_first) {
                m_sb.append("?");
            } else {
                m_sb.append("&");
            }

            m_first = false;

            m_sb.append(encode(key));
            m_sb.append("=");
            m_sb.append(val);
        }

        public void add(String key, boolean val) {

            if (m_first) {
                m_sb.append("?");
            } else {
                m_sb.append("&");
            }

            m_first = false;

            m_sb.append(encode(key));
            m_sb.append("=");
            m_sb.append((val)?"true":"false");
        }

        public String build() {
            return m_sb.toString();
        }
    }
}
