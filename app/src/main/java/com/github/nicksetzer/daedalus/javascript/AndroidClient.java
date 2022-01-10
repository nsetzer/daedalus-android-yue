package com.github.nicksetzer.daedalus.javascript;

import com.github.nicksetzer.daedalus.Log;
import com.github.nicksetzer.daedalus.audio.AudioDownloadFile;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.github.nicksetzer.daedalus.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

public class AndroidClient {
    private Activity m_activity;
    private AudioDownloadFile m_download;

    public static boolean ready = false;

    public AndroidClient(Activity activity) {
        this.m_activity = activity;
    }

    @JavascriptInterface
    public void documentLoaded() {
        Log.error("daedalus-js", "document ready");
        AndroidClient.ready = true;
    }

    @JavascriptInterface
    public void reloadPage() {
        // get the webview context and post a message to the UI thread
        try {
            final WebView wv = m_activity.findViewById(R.id.DaedalusView);
            wv.post(new Runnable() {
                @Override
                public void run() {
                    // TODO: reload original file
                    //wb.loadUrl("file:///android_asset/site/index.html");
                    wv.reload();
                }
            });
        } catch (Exception e) {
            android.util.Log.e("daedalus-js", e.toString());
        }
    }

    @JavascriptInterface
    public void loadUrl(final String url) {
        try {
            final WebView wv = m_activity.findViewById(R.id.DaedalusView);
            wv.post(new Runnable() {
                @Override
                public void run() {
                    wv.loadUrl(url);
                }
            });
        } catch (Exception e) {
            android.util.Log.e("daedalus-js", e.toString());
        }
    }

    @JavascriptInterface
    public void downloadUrl(final String url, final String destinationFolder, final String destinationName) {
        try {
            int code = m_activity.getPackageManager().checkPermission(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    m_activity.getPackageName());
            if (code == PackageManager.PERMISSION_GRANTED) {
                final WebView wv = m_activity.findViewById(R.id.DaedalusView);
                final String destinationPath = "/storage/emulated/0" + "/" + destinationFolder;

                wv.post(new Runnable() {
                    @Override
                    public void run() {
                        m_download = new AudioDownloadFile(m_activity);
                        android.util.Log.e("daedalus-js", "check: " + url);
                        android.util.Log.e("daedalus-js", "check: " + destinationPath);
                        android.util.Log.e("daedalus-js", "check: " + destinationName);

                        m_download.execute(url, destinationPath, destinationName);
                    }
                });
            } else {
                android.util.Log.e("daedalus-js", "file download permission denied");
            }


        } catch (Exception e) {
            android.util.Log.e("daedalus-js", e.toString());
        }
    }

    @JavascriptInterface
    public void browseUrl(final String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        m_activity.startActivity(browserIntent);
    }

    @JavascriptInterface
    public boolean fileExists(final String name) {
        try {
            final String path = m_activity.getExternalFilesDir(null)+ "/"+name;
            android.util.Log.e("daedalus-js", "check: " + name);
            File file = new File(path);
            if (file.exists()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            android.util.Log.e("daedalus-js", e.toString());
        }
        return false;
    }

    @JavascriptInterface
    public String listDirectory(final String path) {
        JSONObject obj = new JSONObject();
        JSONArray files = new JSONArray();
        JSONArray directories = new JSONArray();

        android.util.Log.e("daedalus-js", "reading directory " + path);

        /*
            DCIM
            Pictures
            Alarms
            Audiobooks
            Music
            Notifications
            Podcasts
            Ringtones
            Movies
            Download
         */
        if (path.equals("/")) {
            directories.put(m_activity.getExternalFilesDir(null));
            directories.put(m_activity.getExternalFilesDir(Environment.DIRECTORY_MUSIC));
            Environment.getExternalStorageDirectory();
            directories.put("storage");
            directories.put(Environment.getDataDirectory());
            directories.put(Environment.getExternalStorageDirectory().toString());
        }

        File[] contents;
        try {
            File directory = new File(path);
            android.util.Log.e("daedalus-js", "listing directory " + path);
            contents = directory.listFiles();
        } catch (Exception e) {
            android.util.Log.e("daedalus-js", e.toString());
            contents = new File[0];
        }

        try {

            for (int i = 0; contents != null && i < contents.length; i++)
            {
                JSONObject fo = new JSONObject();

                if (contents[i].isDirectory()) {
                    directories.put(contents[i].getName());
                } else if (contents[i].isFile()) {
                    fo.put("name", contents[i].getName());
                    files.put(fo);
                }
            }

            obj.put("path", path);
            obj.put("files", files);
            obj.put("directories", directories);
        } catch (JSONException e) {
            android.util.Log.e("daedalus-js", e.toString());
        }

        return obj.toString();
    }

    @JavascriptInterface
    public boolean isWifiConnected() {
        try {
            ConnectivityManager cm = (ConnectivityManager)m_activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean isMetered = cm.isActiveNetworkMetered();
            return !isMetered;

        } catch (Exception e) {
            android.util.Log.e("daedalus-js", e.toString());
        }
        return false;
    }

    @JavascriptInterface
    public void setClipboardUrl(final String title, final String url) {
        ClipboardManager clipboard = (ClipboardManager) m_activity.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(title, url));
    }

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

    /**
     * Implements an API similar to js 'fetch' for sending and receiving JSON
     *
     * @param url_string
     * @param body
     * @param parameters
     * @return
     */
    @JavascriptInterface
    public String fetch_json(final String url_string, String body, final String parameters) {

        JSONObject params;
        JSONObject headers;
        try {
            params = new JSONObject(parameters);
            headers = params.getJSONObject("headers");
            if (headers == null) {
                headers = new JSONObject();
            }
        } catch (JSONException e) {
            android.util.Log.e("daedalus-js","unable to parse parameters");
            return "error1";
        }

        URL url;
        try {
            url = new URL(url_string);
        } catch (java.net.MalformedURLException e) {
            android.util.Log.e("daedalus-js","unable to parse url");
            return "error2";
        }

        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (java.io.IOException e) {
            android.util.Log.e("daedalus-js","unable to open connection");
            return "error3";
        }

        try {
            conn.setRequestMethod("GET");
        } catch (java.net.ProtocolException e) {
            android.util.Log.e("daedalus-js","unable to set method");
            return "error4";
        }

        Iterator<String> keys = headers.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                String value = headers.getString(key);
                conn.setRequestProperty(key, value);
            } catch (org.json.JSONException e) {
                android.util.Log.e("daedalus-js","unable to set request property " + key);
            }
        }

        conn.setReadTimeout(10000);
        conn.setConnectTimeout(10000);

        android.util.Log.e("daedalus-js-api", "protocol: " + url.getProtocol());
        android.util.Log.e("daedalus-js-api", "method: " + conn.getRequestMethod());
        android.util.Log.e("daedalus-js-api", "url: " + url.toString());

        if (!body.isEmpty()) {
            conn.setDoOutput(true);
            try {
                OutputStream os = conn.getOutputStream();
                byte[] input = body.getBytes("utf-8");
                os.write(input, 0, input.length);
            } catch (java.io.IOException e) {
                android.util.Log.e("daedalus-js","error writing output stream");
                return "error5:" + e.getMessage();
            }
        }

        try {
            conn.connect();
        } catch (Exception e) {
            android.util.Log.e("daedalus-js-api", "failed to connect: " + e.toString());
            return "error6";
        }

        int status;
        try {
            status = conn.getResponseCode();
        } catch (java.io.IOException e) {
            android.util.Log.e("daedalus-js-api", "unable to get response status code");
            return "error7";
        }
        android.util.Log.e("daedalus-js-api", "status: " + status);

        if (status != HttpURLConnection.HTTP_OK) {
            InputStream stream = conn.getErrorStream();
            String text;
            try {
                text = readResponseText(stream);
            } catch (java.io.IOException e) {
                android.util.Log.e("daedalus-js-api", "unable to get response error text");
                return "error8";
            }
            android.util.Log.e("daedalus-js-api", "response error:" + text);
            return "error9";
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        String contentLength = conn.getHeaderField("Content-Length");
        int total_length = 0;
        if (!contentLength.isEmpty()) {
            total_length = Integer.parseInt(contentLength);
        }

        String text;
        try {
            text = readResponseText(conn.getInputStream());
        } catch (java.io.IOException e) {
            android.util.Log.e("daedalus-js-api", "unable to get response text");
            return "error10";
        }

        return text;
    }
}
