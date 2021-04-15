package com.github.nicksetzer.daedalus.javascript;

import com.github.nicksetzer.daedalus.audio.AudioDownloadFile;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.github.nicksetzer.daedalus.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class AndroidClient {
    private Activity m_activity;
    private AudioDownloadFile m_download;

    public AndroidClient(Activity activity) {
        this.m_activity = activity;
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
}
