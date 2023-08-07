package com.github.nicksetzer.daedalus.audio.media2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import com.github.nicksetzer.daedalus.Log;
import com.github.nicksetzer.daedalus.audio.AudioService;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

public class NotificationListener implements PlayerNotificationManager.NotificationListener {
    AudioService m_service = null;
    NotificationManager m_platformMgr = null;

    NotificationListener(AudioService service, NotificationManager platformMgr) {
        m_service = service;
        m_platformMgr = platformMgr;
    }
    @Override
    public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {

        Context context = m_service.getApplicationContext();
        ContextCompat.startForegroundService(
                context,
                new Intent(context, m_service.getClass())
        );

        //new NotificationChannel("")
        //m_platformMgr.createNotificationChannel();
        Log.warn("start notification: " + notificationId + " " + (notification != null) + " " + notification.toString());


        m_service.startForeground(notificationId, notification);
    }

    @Override
    public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
        m_service.stopForeground(true);
        m_service.stopSelf();
    }


}
