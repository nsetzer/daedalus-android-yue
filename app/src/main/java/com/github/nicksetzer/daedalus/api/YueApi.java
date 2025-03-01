package com.github.nicksetzer.daedalus.api;

import android.net.Uri;

import com.github.nicksetzer.daedalus.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class YueApi {

    public static final String PROTOCOL = "https";
    public static final String DOMAIN = "yueapp.duckdns.org";
    //public static final String DOMAIN = "104.248.122.206";
    public static final int PORT = 443;

    public static class ApiException extends IOException {
        public ApiException(String message) {
            super(message);
        }
    }

    public static interface Callback {
        void callback(int count, int total);
    }

    //public static final String PROTOCOL = "http";
    //public static final String DOMAIN = "192.168.1.149";
    //public static final int PORT = 4200;
    //myURLConnection.setDoInput(true);

    static String readResponseText(InputStream stream) throws IOException {
        return readResponseText(stream, null);
    }

    /**
     *
     * @param stream
     * @param encoding one of "gzip", "utf-8", an empty string, or null
     * @return the entire text document from stream
     * @throws IOException
     */
    static String readResponseText(InputStream stream, String encoding) throws IOException {
        if (encoding != null && encoding.equals("gzip")) {
            stream = new GZIPInputStream(stream);
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();
        return sb.toString();
    }

    static void dumpResponseHeaders(HttpURLConnection conn) {
        for (Map.Entry<String, List<String>> entry :  conn.getHeaderFields().entrySet()) {
            String text = entry.getKey() + ": " + String.join(",", entry.getValue());
            Log.error( "content header: " + text);
        }
    }

    public static JSONArray librarySearch(String token, String query, long limit, long page, String order, boolean showBanished) throws IOException {


        URLParamEncoder.Builder builder = new URLParamEncoder.Builder();

        builder.add("query", query);

        if (limit >= 0) {
            builder.add("limit", limit);
        }

        if (page >= 0) {
            builder.add("page", page);
        }

        if (!order.isEmpty()) {
            builder.add("orderby", order);
        }

        builder.add("showBanished", showBanished);

        String query_string = builder.build();

        URL url = new URL(PROTOCOL, DOMAIN,PORT, "/api/library" + query_string);
        Log.info(url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        http://localhost:4200/api/user/login
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", token);
        conn.setRequestProperty("Accept-Encoding", "gzip");
        //conn.setRequestProperty("Content-Type", "application/json");
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(10000);
        //conn.setDoOutput(true);

        try {
            conn.connect();
        } catch (Exception e) {
            Log.error( "failed to connect: " + e.toString());
            return null;
        }

        int status = conn.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            String text = readResponseText(conn.getErrorStream());
            Log.error("body:" + text);

            return null;
        }

        dumpResponseHeaders(conn);

        String contentEncoding = conn.getHeaderField("Content-Encoding");  // may return null if not found
        String contentType = conn.getHeaderField("Content-Type");

        if (!contentType.equals("application/json")) {
            android.util.Log.w("daedalus-js-api", "unexpected content type" + contentType);
        }

        String text = readResponseText(conn.getInputStream(), contentEncoding);

        JSONObject obj = null;
        String err = null;

        try {
            obj = new JSONObject(text);
        } catch (JSONException e) {
            err = e.toString() + ": " + e.getMessage();
        }

        if (err != null) {
            throw new ApiException(err);
        }

        if (obj == null || !obj.has("result")) {
            throw new ApiException("result not found");
        }

        JSONArray arr = null;
        err = null;

        try {
            arr = obj.getJSONArray("result");
        } catch (JSONException e) {
            err = e.toString() + ": " + e.getMessage();
        }

        if (err != null) {
            throw new ApiException(err);
        }

        return arr;
    }

    public static boolean download(String token, String uid, String filepath, Callback callback) throws IOException {

        URL url = new URL(PROTOCOL, DOMAIN,PORT, "/api/library/" + uid + "/audio");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", token);
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(10000);

        try {
            conn.connect();
        } catch (Exception e) {
            Log.error("failed to connect: " + e.toString());
            return false;
        }

        int status = conn.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            String text = readResponseText(conn.getErrorStream());
            Log.error("error body:" + text);
            return false;
        }

        String contentLength = conn.getHeaderField("Content-Length");
        int total_length = 0;
        if (!contentLength.isEmpty()) {
            total_length = Integer.parseInt(contentLength);
        }

        File file = new File(filepath);

        file.getParentFile().mkdirs();

        InputStream netStream = conn.getInputStream();
        OutputStream fileStream = new FileOutputStream(file, false);

        try {
            byte[] bytes = new byte[2048];
            int received = 0;
            int length;
            int notify = 0;

            while ((length = netStream.read(bytes))!=-1) {
                received += length;
                // notify roughly every 50kb :: about 100 updates for a 5mb file
                if (callback != null && (notify++)%26 == 0) {
                    callback.callback(received, total_length);
                }
                fileStream.write(bytes, 0, length);
            }

            if (callback != null) {
                callback.callback(received, total_length);
            }

        } finally {
            fileStream.close();
        }

        return true;
    }

    public static String librarySongAudioUrl(final String token, final String uid) throws IOException{
        URL url = new URL(PROTOCOL, DOMAIN,PORT, "/api/library/" + uid + "/audio?token=" + token);
        return url.toString();

    }

    public static JSONObject radioStationNextTrack(final String token, final String station) throws IOException {
        URL url = new URL(PROTOCOL, DOMAIN,PORT, "/api/radio/station/" + station + "/next_track");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", token);
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(10000);

        try {
            conn.connect();
        } catch (Exception e) {
            Log.error("failed to connect: " + e.toString());
            return null;
        }

        int status = conn.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            String text = readResponseText(conn.getErrorStream());
            Log.error( "body:" + text);
            return null;
        }

        String contentLength = conn.getHeaderField("Content-Length");
        int total_length = 0;
        if (!contentLength.isEmpty()) {
            total_length = Integer.parseInt(contentLength);
        }

        InputStream netStream = conn.getInputStream();

        StringBuilder sb = new StringBuilder();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] bytes = new byte[2048];
        int length;
        int received = 0;
        while ((length = netStream.read(bytes))!=-1) {
            received += length;
            out.write(bytes, 0, length);
        }

        if (received != total_length) {
            Log.warn("received: " + received + ", expected: " + total_length);
        }

        JSONObject track = null;
        try {
            JSONObject obj = new JSONObject(out.toString("UTF-8"));

            track = obj.getJSONObject("result");
        } catch (JSONException e) {
            return null;
        }

        return track;
    }
}
