package com.github.nicksetzer.daedalus.audio.media2;

import android.app.PendingIntent;
import android.content.Context;

import com.github.nicksetzer.daedalus.R;
import com.github.nicksetzer.daedalus.audio.AudioService;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExoNotificationManager {

    AudioService m_service;
    //Player m_player;

    NotificationManager m_platformMgr;
    PlayerNotificationManager m_mgr;

    public ExoNotificationManager(AudioService service, MediaSessionCompat.Token sessionToken) {
        m_service = service;
        //m_player = player;

        Context context = service.getApplicationContext();

        MediaControllerCompat mediaController = new MediaControllerCompat(context, sessionToken);

        m_platformMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // notifiation id is an arbitrary number
        m_mgr = new CustomPlayerNotificationManager.Builder(context, 123, "com.github/nicksetzer.daedalus.channel")
                .setMediaDescriptionAdapter(new DescriptionAdapter(mediaController))
                .setNotificationListener(new NotificationListener(m_service, m_platformMgr))
                .setChannelNameResourceId(R.string.notification_channel)
                .setChannelDescriptionResourceId(R.string.notification_channel_description)
                .build();

        m_mgr.setUsePreviousAction(true);
        m_mgr.setUseNextAction(true);
        m_mgr.setUseRewindAction(false);
        m_mgr.setUseFastForwardAction(false);

        m_mgr.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        m_mgr.setPriority(NotificationCompat.PRIORITY_HIGH);
        m_mgr.setMediaSessionToken(sessionToken);
        //m_mgr.setUseNavigationActionsInCompactView(true);

    }

    public void hideNotification() {
        m_mgr.setPlayer(null);
    }

    public void showNotificationForPlayer(Player player) {
        m_mgr.setPlayer(player);
    }

    private class CustomPlayerNotificationManager extends PlayerNotificationManager {
        private Context context;
        private Map<String, NotificationCompat.Action> actionMap2 = new HashMap<>();

        public CustomPlayerNotificationManager(Context context, String channelId, int notificationId, MediaDescriptionAdapter mediaDescriptionAdapter) {

            super(context, channelId, notificationId, mediaDescriptionAdapter, null, null,
                    0, 0, 0, 0, 0, 0, 0, 0, "grpkey");
            this.context = context;
        }

        public CustomPlayerNotificationManager(Context context, String channelId, int notificationId, MediaDescriptionAdapter mediaDescriptionAdapter, @Nullable NotificationListener notificationListener) {
            super(context, channelId, notificationId, mediaDescriptionAdapter, notificationListener, null,
                    0, 0, 0, 0, 0, 0, 0, 0, "grpkey");
            this.context = context;
        }

        public CustomPlayerNotificationManager(Context context, String channelId, int notificationId, MediaDescriptionAdapter mediaDescriptionAdapter, @Nullable CustomActionReceiver customActionReceiver) {
            super(context, channelId, notificationId, mediaDescriptionAdapter, null, customActionReceiver,
                    0, 0, 0, 0, 0, 0, 0, 0, "grpkey");
            this.context = context;
        }

        public CustomPlayerNotificationManager(Context context, String channelId, int notificationId, MediaDescriptionAdapter mediaDescriptionAdapter, @Nullable NotificationListener notificationListener, @Nullable CustomActionReceiver customActionReceiver) {
            super(context, channelId, notificationId, mediaDescriptionAdapter, notificationListener, customActionReceiver,
                    0, 0, 0, 0, 0, 0, 0, 0, "grpkey");
            this.context = context;
        }

        private boolean isPlaying(Player player) {
            return player.getPlaybackState() != Player.STATE_ENDED
                    && player.getPlaybackState() != Player.STATE_IDLE
                    && player.getPlayWhenReady();
        }

        @Override
        protected List<String> getActions(Player player) {

            List<String> stringActions = new ArrayList<>();

            stringActions.add(ACTION_PREVIOUS);

            if (player.isPlaying()) {
                stringActions.add(ACTION_PAUSE);
            } else {
                stringActions.add(ACTION_PLAY);
            }
            stringActions.add(ACTION_NEXT);

            //    stringActions.add(ACTION_REWIND);
            //    stringActions.add(ACTION_FAST_FORWARD);
            //    stringActions.add(ACTION_STOP);

            return stringActions;
        }
    }
    private class DescriptionAdapter implements PlayerNotificationManager.MediaDescriptionAdapter {

        MediaControllerCompat m_controller;

        DescriptionAdapter(MediaControllerCompat controller) {
            m_controller = controller;
        }

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(Player player) {
            return m_controller.getSessionActivity();
        }

        @Nullable
        @Override
        public CharSequence getCurrentContentText(Player player) {
            return "!c:"; //+ m_controller.getMetadata().getDescription().getSubtitle().toString();
        }

        @Override
        public CharSequence getCurrentContentTitle(Player player) {
            return "!t:"; //+ m_controller.getMetadata().getDescription().getTitle().toString();
        }

        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
            return null;
        }


    }


    }
