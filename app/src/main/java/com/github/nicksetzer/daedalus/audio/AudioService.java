package com.github.nicksetzer.daedalus.audio;
/*
adb shell am broadcast -a android.intent.action.MEDIA_BUTTON
adb shell input keyevent 126


run a function on the main thread of a service
Handler mainHandler = new Handler(context.getMainLooper());

Runnable myRunnable = new Runnable() {
    @Override
    public void run() {....} // This is your code
};
mainHandler.post(myRunnable);


 */
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentProvider;
import android.content.Intent;
import android.content.Context;

import android.graphics.Color;

import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;

import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.view.KeyEvent;


import com.github.nicksetzer.daedalus.Log;
import com.github.nicksetzer.daedalus.R;
import com.github.nicksetzer.daedalus.audio.tasks.AudioFetchTask;
import com.github.nicksetzer.daedalus.audio.tasks.AudioSyncTask;
import com.github.nicksetzer.metallurgy.orm.dsl.DslException;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import androidx.annotation.Nullable;

import androidx.core.app.NotificationCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

/**
 * Android 11 turned this from a default `Service` into a MediaBrowserServiceCompat
 * this was to support bluetooth again, under a new uniformed media api
 */
public class AudioService extends MediaBrowserServiceCompat {

    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";


    public AudioManager m_manager;
    public Database m_database;
    Executor m_executor;

    private IBinder m_binder = new AudioBinder();

    private static class BackgroundThreadFactory implements ThreadFactory {
        private static int sTag = 1;

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("CustomThread" + sTag);
            thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);

            // A exception handler is created to Log the exception from threads
            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    android.util.Log.e("daedalus-js", thread.getName() + " encountered an error: " + ex.getMessage());
                }
            });
            return thread;
        }
    }

    private boolean m_fetchRunning = false;
    private boolean m_fetchAlive = false;
    private Lock m_fetchLock;

    static final String NOTIFICATION_CHANNEL_ID = "com.github.nicksetzer.daedalus";
    public AudioService() {

        super();

        m_manager = null;

        Log.info("service construct");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.info("lifecycle service create");

        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        m_executor = new ThreadPoolExecutor(
                0,
                2,
                30,
                TimeUnit.SECONDS,
                queue,
                new BackgroundThreadFactory());

        m_database = new Database(this);

        m_database.connect();
        Log.error("service create" + m_database.m_songsTable.count());

        m_fetchRunning = false;
        m_fetchLock = new ReentrantLock();
        //super.onCreate();

        Log.info("lifecycle manager = " + (m_manager != null));
        if (m_manager == null) {
            Log.error("lifecycle new manager");
            m_manager = new AudioManager(this);

            setSessionToken(m_manager.getSession().getSessionToken());



        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public BrowserRoot onGetRoot(
            String clientPackageName,
            int clientUid,
            Bundle rootHints) {

        // (Optional) Control the level of access for the specified package name.
        // You'll need to write your own logic to do this.
        /*
        if (allowBrowsing(clientPackageName, clientUid)) {
            // Returns a root ID that clients can use with onLoadChildren() to retrieve
            // the content hierarchy.
            return new BrowserRoot(MY_MEDIA_ROOT_ID, null);
        } else {
            // Clients can connect, but this BrowserRoot is an empty hierachy
            // so onLoadChildren returns nothing. This disables the ability to browse for content.
            return new BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null);
        }
        */
        Log.info("lifecycle onGetRoot");

        Bundle extras = new Bundle();
        String CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT";
        int CONTENT_STYLE_LIST = 1;
        extras.putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_LIST);

        return new BrowserRoot(MY_MEDIA_ROOT_ID, extras);
    }

    @Override
    public void onLoadChildren(final String parentMediaId,
                               final Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.info("lifecycle service onLoadChildren:" + parentMediaId);
        //  Browsing not allowed
        if (MY_EMPTY_MEDIA_ROOT_ID.equals(parentMediaId)) {
            Log.info("lifecycle service onLoadChildren: send result null");
            result.sendResult(null);
            return;
        }

        // Check if this is the root menu:
        if (MY_MEDIA_ROOT_ID.equals(parentMediaId)) {
            Log.info("lifecycle service onLoadChildren: send result media items");
            result.sendResult(m_manager.m_queue.getMediaItems());
        } else {
            Log.info("lifecycle service onLoadChildren: send result null");
            result.sendResult(null);
        }

    }

    public void updateNotification() {

        //Log.warn("lifecycle notification : not this way");
        return;
    }

    public void disableNotification() {
        return;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String s_action = intent.getAction();
            Bundle extras = intent.getExtras();
            if (extras != null) {
                s_action += " : " + extras;
            }
            Log.info( "receiver onStartCommand intent=" + s_action);
        } else {
            Log.error( "receiver onStartCommand null intent");
        }




        if (m_manager != null) {
            MediaButtonReceiver.handleIntent(m_manager.getSession(), intent);
        }
        //startForeground();

        if (m_manager == null) {
            // TODO: is it now an error to hit this
            Log.error("receiver not sure if this is correct anymore");
            m_manager = new AudioManager(this);

        }

        if (intent != null) {
            String action = intent.getAction();

            Log.info( "receiver action intent: " + action);

            String token;
            String data;
            String url;
            int index;
            if (action != null) {
                switch (action) {
                    case AudioActions.ACTION_SET_QUEUE:
                        data = intent.getExtras().getString("data");
                        m_manager.setQueueData(data);
                        break;
                    case AudioActions.ACTION_UPDATE_QUEUE:
                        data = intent.getExtras().getString("data");
                        index = intent.getExtras().getInt("index");
                        m_manager.updateQueueData(index, data);
                        break;
                    case AudioActions.ACTION_LOAD_INDEX:
                        index = intent.getExtras().getInt("index");
                        m_manager.loadIndex(index);
                        break;
                    case AudioActions.ACTION_LOAD_URL:
                        url = intent.getExtras().getString("url");
                        if (url != null) {
                            m_manager.loadUrl(url);
                        } else {
                            android.util.Log.e("daedalus-js", "received null url");
                        }
                        break;
                    case AudioActions.ACTION_LOAD_RADIO_URL:
                        url = intent.getExtras().getString("url");
                        if (url != null) {
                            m_manager.loadRadioUrl(url);
                        } else {
                            android.util.Log.e("daedalus-js", "received null url");
                        }
                        break;
                    case AudioActions.ACTION_PLAY_RADIO_URL:
                        url = intent.getExtras().getString("url");
                        if (url != null) {
                            m_manager.playRadioUrl(url);
                        } else {
                            android.util.Log.e("daedalus-js", "received null url");
                        }
                        break;
                    case AudioActions.ACTION_PLAY_NEXT_RADIO_URL:
                        m_manager.nextRadioTrack();
                        break;
                    case AudioActions.ACTION_PLAY:
                        Log.info("play");
                        m_manager.play();
                        break;
                    case AudioActions.ACTION_PAUSE:
                        Log.info("pause");
                        m_manager.pause();
                        break;
                    case AudioActions.ACTION_STOP:
                        Log.info("stop");
                        m_manager.stop();
                        break;
                    case AudioActions.ACTION_SKIPTONEXT:
                        Log.info("next");
                        m_manager.skipToNext();
                        break;
                    case AudioActions.ACTION_SKIPTOPREV:
                        Log.info( "prev");
                        m_manager.skipToPrev();
                        break;
                    case AudioActions.ACTION_SEEK:
                        Log.info( "prev");
                        long position = intent.getExtras().getLong("position");
                        m_manager.seek(position);
                        break;
                    case AudioActions.ACTION_FETCH:
                        Log.info( "fetch");
                        token = intent.getExtras().getString("token");
                        launchFetchTask(token);
                        break;
                    case AudioActions.ACTION_SYNC_UPDATE:
                        Log.info("sync_update");
                        String payload = intent.getExtras().getString("payload");
                        m_database.m_songsTable.updateSyncStatus(payload);
                        sendEvent(AudioEvents.ONSYNCSTATUSUPDATED, "{}");
                        break;

                    case AudioActions.ACTION_SYNC_QUERY:
                        Log.info("received sync query");
                        this.syncQuery();
                        break;
                    case AudioActions.ACTION_SYNC:
                        Log.info( "sync");
                        token = intent.getExtras().getString("token");
                        launchSyncTask(token);
                        break;
                    case AudioActions.ACTION_CANCEL_TASK:
                        Log.info("cancel task");
                        taskKill();
                    case AudioActions.ACTION_INIT_RADIO:
                        Log.info( "init radio");
                        token = intent.getExtras().getString("token");
                        m_manager.setToken(token);
                        token = intent.getExtras().getString("station");
                        m_manager.setStation(token);
                        break;
                    default:
                        if (action.equals("android.intent.action.MEDIA_BUTTON")) {
                            Log.info("receiver weird action", action);
                            if (mediaIsPlaying()) {
                                m_manager.pause();
                            } else {
                                m_manager.play();
                            }
                        } else {
                            Log.error("receiver unknown action", action);
                        }


                        break;

                }
            }
        }
        return START_NOT_STICKY;
    }



    @Override
    public void onDestroy() {

        if (m_database != null) {
            m_database.close();
        }
        Log.error("destroy service");
        if (m_manager != null && m_manager.isPlaying()) {
            m_manager.stop();
        }

        m_manager.release();

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.info("lifecycle service onbind", m_binder!=null);
        if (SERVICE_INTERFACE.equals(intent.getAction())) {
            Log.info("lifecycle super.bind");
            return super.onBind(intent);
        } else {
            Log.info("lifecycle default.bind");
        }
        return m_binder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.info("lifecycle service rebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.info("service unbind");
        return true;
    }

    public class AudioBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }

    public boolean mediaIsPlaying() {
        return m_manager.isPlaying();
    }

    public String mediaQueueData() {
        return m_manager.getQueueData();
    }

    public String getFormattedTimeUpdate() {
        return m_manager.formatTimeUpdate();
    }

    public int getCurrentIndex() {
        return m_manager.m_queue.getCurrentIndex();
    }

    public void sendEvent(String name, String payload) {

        Intent intent = new Intent();
        intent.setAction(AudioActions.ACTION_EVENT);
        intent.putExtra( "name",name);
        intent.putExtra( "payload",payload);
        sendBroadcast(intent);
    }

    public void launchFetchTask(String token) {

        m_fetchLock.lock();
        try {
            if (!m_fetchRunning) {
                m_fetchRunning = true;
                m_fetchAlive = true;
                m_executor.execute(new AudioFetchTask(this, token));

            }
        } finally {
            m_fetchLock.unlock();
        }
    }

    public void syncQuery() {
        m_fetchLock.lock();
        try {
            if (!m_fetchRunning) {
                sendEvent(AudioEvents.ONSYNCCOMPLETE, "{}");
            }
        } finally {
            m_fetchLock.unlock();
        }
    }

    public void taskKill() {
        m_fetchLock.lock();
        try {
            if (m_fetchRunning) {
                m_fetchAlive = false;
            }
        } finally {
            m_fetchLock.unlock();
        }
    }

    public boolean taskIsKill() {
        boolean kill;
        m_fetchLock.lock();
        try {
            kill = !m_fetchAlive;
        } finally {
            m_fetchLock.unlock();
        }
        return kill;
    }

    public void fetchProgressUpdate(int count, int total) {
        sendEvent(AudioEvents.ONFETCHPROGRESS, "{\"count\": " + count + ", \"total\": " + total + "}");
    }
    
    public void fetchComplete() {
        m_fetchLock.lock();
        try {
            m_fetchRunning = false;
            sendEvent(AudioEvents.ONFETCHCOMPLETE, "{}");
        } finally {
            m_fetchLock.unlock();
        }
    }

    public void launchSyncTask(String token) {

        m_fetchLock.lock();
        try {
            if (!m_fetchRunning) {
                m_fetchRunning = true;
                m_fetchAlive = true;
                m_executor.execute(new AudioSyncTask(this, token));
            }
        } finally {
            m_fetchLock.unlock();
        }
    }

    public void syncProgressUpdate(int index, int total, String message) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("index", index);
            obj.put("total", total);
            obj.put("message", message);

            sendEvent(AudioEvents.ONSYNCPROGRESS, obj.toString());
        } catch (JSONException e) {
            Log.error("failed to update sync progress", e);
        }

    }

    public void syncComplete() {
        m_fetchLock.lock();
        try {
            m_fetchRunning = false;
            sendEvent(AudioEvents.ONSYNCCOMPLETE, "{}");
        } finally {
            m_fetchLock.unlock();
        }
    }

    public String mediaBuildForest(String query, int syncState, int showBannished) {

        try {
            return m_database.m_songsTable.queryForest(query, syncState, showBannished).toString();
        } catch (DslException e) {
            Log.error("dsl error", e);
            JSONObject obj = new JSONObject();
            try {
                obj.put("title", "Query Error");
                String msg = e.getMessage();
                msg = msg.replace('\"', '\'');
                obj.put("message", msg);


                Log.info(obj.toString());
                sendEvent(AudioEvents.ONEXCEPT, obj.toString());
            } catch (JSONException ex2) {
                Log.error("error formatting exception", ex2);
            }
        }
        return "";

    }

    public String getSyncInfo() {

        JSONObject obj = new JSONObject();

        long synced = m_database.m_songsTable.getSyncedCount();
        long count = m_database.m_songsTable.count();

        try {
            obj.put("record_count", count);
            obj.put("synced_tracks", synced);
        } catch (JSONException e) {
            Log.error("format error", e);
        }

        return obj.toString();
    }


}
