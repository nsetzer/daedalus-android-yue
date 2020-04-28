package com.github.nicksetzer.daedalus;
/*
adb shell am broadcast -a android.intent.action.MEDIA_BUTTON
adb shell input keyevent 126
 */
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;

import android.graphics.Color;

import android.net.Uri;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;


import com.github.nicksetzer.daedalus.audio.AudioActions;
import com.github.nicksetzer.daedalus.audio.AudioManager;

import androidx.annotation.Nullable;

import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

public class AudioService extends Service {

    AudioManager m_manager;

    private IBinder m_binder = new AudioBinder();

    public AudioService() {

        super();

        m_manager = null;
    }

    @Override
    public void onCreate() {


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
        AudioService getService() {
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
}
