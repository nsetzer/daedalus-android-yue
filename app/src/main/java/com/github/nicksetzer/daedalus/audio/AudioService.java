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
import android.content.Intent;
import android.content.Context;

import android.graphics.Color;

import android.net.Uri;

import android.os.Binder;
import android.os.IBinder;
import android.os.Process;


import com.github.nicksetzer.daedalus.R;
import com.github.nicksetzer.daedalus.audio.tasks.AudioFetchTask;

import java.util.ArrayList;
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
import androidx.media.session.MediaButtonReceiver;

public class AudioService extends Service {

    AudioManager m_manager;
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

            // A exception handler is created to log the exception from threads
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

    public AudioService() {

        super();

        m_manager = null;
    }

    @Override
    public void onCreate() {

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
        android.util.Log.e("daedalus-js", "" + m_database.m_songsTable.count());

        m_fetchRunning = false;
        m_fetchLock = new ReentrantLock();
        super.onCreate();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    private void startForeground() {
        Context context = getApplicationContext();
        String packageName = context.getPackageName();
        Intent openApp = context.getPackageManager().getLaunchIntentForPackage(packageName);

        openApp.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Add the Uri data so apps can identify that it was a notification click
        openApp.setAction(Intent.ACTION_VIEW);
        openApp.setData(Uri.parse("daedalus://notification.click"));

        String NOTIFICATION_CHANNEL_ID = "com.github.nicksetzer.daedalus";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.play)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(PendingIntent.getActivity(context, 0, openApp, PendingIntent.FLAG_CANCEL_CURRENT))
                .build();

        startForeground(1, notification);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        android.util.Log.e("daedalus-js", "onStartCommand");
        if (m_manager != null) {
            MediaButtonReceiver.handleIntent(m_manager.getSession(), intent);
        }

        startForeground();

        if (m_manager == null) {
            m_manager = new AudioManager(this);
        }

        if (intent != null) {
            String action = intent.getAction();
            android.util.Log.e("daedalus-js", "action intent: " + action);

            String data;
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
                        String url = intent.getExtras().getString("url");
                        if (url != null) {
                            m_manager.loadUrl(url);
                        } else {
                            android.util.Log.e("daedalus-js", "received null url");
                        }
                        break;
                    case AudioActions.ACTION_PLAY:
                        android.util.Log.e("daedalus", "play");
                        m_manager.play();
                        break;
                    case AudioActions.ACTION_PAUSE:
                        android.util.Log.e("daedalus", "pause");
                        m_manager.pause();
                        break;
                    case AudioActions.ACTION_STOP:
                        android.util.Log.e("daedalus", "stop");
                        m_manager.stop();
                        break;
                    case AudioActions.ACTION_SKIPTONEXT:
                        android.util.Log.e("daedalus", "next");
                        m_manager.skipToNext();
                        break;
                    case AudioActions.ACTION_SKIPTOPREV:
                        android.util.Log.e("daedalus", "prev");
                        m_manager.skipToPrev();
                        break;
                    case AudioActions.ACTION_SEEK:
                        android.util.Log.e("daedalus", "prev");
                        long position = intent.getExtras().getLong("position");
                        m_manager.seek(position);
                        break;
                    case AudioActions.ACTION_FETCH:
                        android.util.Log.e("daedalus", "fetch");
                        String token = intent.getExtras().getString("token");
                        launchFetchTask(token);
                        break;
                    case AudioActions.ACTION_SYNC_UPDATE:
                        android.util.Log.e("daedalus", "sync_update");
                        String payload = intent.getExtras().getString("payload");
                        m_database.m_songsTable.updateSyncStatus(payload);
                        sendEvent(AudioEvents.ONSYNCSTATUSUPDATED, "{}");
                        break;
                    default:
                        android.util.Log.e("daedalus", "unknown action");
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
        android.util.Log.e("daedalus", "destroy service");
        if (m_manager.isPlaying()) {
            m_manager.stop();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return m_binder;
    }

    @Override
    public void onRebind(Intent intent) {

        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
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

    public String mediaBuildForest() {
        return m_database.m_songsTable.queryForest().toString();
    }

}
