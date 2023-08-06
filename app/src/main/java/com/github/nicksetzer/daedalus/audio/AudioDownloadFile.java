package com.github.nicksetzer.daedalus.audio;

import android.Manifest;
import android.app.Activity;

import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class AudioDownloadFile implements Runnable {
    String strFolderName;
    Activity m_activity;

    String m_url;
    String m_destinationPath;
    String m_destinationName;

    Handler m_handler;

    public AudioDownloadFile(Activity activity, final String url, final String destinationPath, final String destinationName) {
        m_activity = activity;

        m_url = url;
        m_destinationPath = destinationPath;
        m_destinationName = destinationName;

        m_handler = new Handler(Looper.getMainLooper());

    }
    @Override
    public void run() {

        doInBackground();

    }

    protected Long doInBackground() {
        int count;
        try {
            android.util.Log.e("daedalus-js", "download r: " + isReadStorageAllowed());
            android.util.Log.e("daedalus-js", "download w: " + isWriteStorageAllowed());
            android.util.Log.e("daedalus-js", "download start");
            URL url = new URL((String) m_url);
            String destinationDirectory = m_destinationPath;
            String destinationName = m_destinationName;
            URLConnection connection = url.openConnection();
            connection.connect();

            int lengthOfFile = connection.getContentLength();

            File folder = new File(destinationDirectory);
            if(!folder.exists()){
                android.util.Log.e("daedalus-js", "download creating:" + destinationDirectory + ":" + folder.mkdir());
                //If there is no folder it will be created.
            } else {
                android.util.Log.e("daedalus-js", "download exists:" + destinationDirectory);
            }
            InputStream input = new BufferedInputStream(url.openStream());
            String name = destinationDirectory+"/"+destinationName;
            android.util.Log.e("daedalus-js", "download name:" + name);
            OutputStream output = new FileOutputStream(name);
            byte data[] = new byte[1024];
            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                // TODO: handler post
                // https://stackoverflow.com/questions/58767733/the-asynctask-api-is-deprecated-in-android-11-what-are-the-alternatives
                //onProgressUpdate ((int)(total*100/lengthOfFile));
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            android.util.Log.e("daedalus-js", "download fail:" + e.toString());
        }
        android.util.Log.e("daedalus-js", "download end");
        return null;
    }

    //protected void onProgressUpdate(Integer... progress) {
    //    android.util.Log.e("daedalus-js", "download progress " + progress);
    //}

    private boolean isReadStorageAllowed() {
        //Getting the permission status
        int result = m_activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    private boolean isWriteStorageAllowed() {
        //Getting the permission status
        int result = m_activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }





}