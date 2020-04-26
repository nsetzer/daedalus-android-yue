package com.github.nicksetzer.daedalus.audio;

import android.Manifest;
import android.app.Activity;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class AudioDownloadFile extends AsyncTask<String,Integer,Long> {
    String strFolderName;
    Activity m_activity;

    public AudioDownloadFile(Activity activity) {
        m_activity = activity;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Long doInBackground(String... opts) {
        int count;
        try {
            android.util.Log.e("daedalus-js", "download r: " + isReadStorageAllowed());
            android.util.Log.e("daedalus-js", "download w: " + isWriteStorageAllowed());
            android.util.Log.e("daedalus-js", "download start");
            URL url = new URL((String) opts[0]);
            String destinationDirectory = opts[1];
            String destinationName = opts[2];
            URLConnection connection = url.openConnection();
            connection.connect();

            int lenghtOfFile = connection.getContentLength();

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
                publishProgress ((int)(total*100/lenghtOfFile));
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

    protected void onProgressUpdate(Integer... progress) {
        android.util.Log.e("daedalus-js", "download progress " + progress);
    }

    protected void onPostExecute(String result) {
    }

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