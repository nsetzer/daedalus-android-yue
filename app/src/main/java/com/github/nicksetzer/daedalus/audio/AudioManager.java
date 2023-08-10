package com.github.nicksetzer.daedalus.audio;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.github.nicksetzer.daedalus.Log;
import com.github.nicksetzer.daedalus.audio.tasks.RadioNextTrackTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

/*
TODO: update the media session queue.
    every time a new song is loaded update the queue with the next few tracks.

        MediaDescription desc = new MediaDescription.Builder().setTitle()
        MediaSession.QueueItem item = new MediaSession.QueueItem(desc));

 */
public class AudioManager {

    private final AudioService m_service;

    //final String MP3URL = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";

    private final BTReceiver m_receiver;

    private final MediaSessionCompat m_session;

    private ExoPlayer m_mediaPlayer;

    private final android.media.AudioManager m_manager;

    private boolean m_autoPlay = true;
    private int m_playback_mode = 0;

    private String m_token = null;
    private String m_station = null;

    private String m_currentUrl = null;
    private long m_pausedTimeMs = -1;

    AudioQueue m_queue;

    private boolean m_isPlaying = false;
    PlayerEventListener m_mediaListener;

    public AudioManager(AudioService service) {

        m_service = service;

        m_queue = new AudioQueue();

        Context context = service.getApplicationContext();

        // adb shell input keyevent <keycode>
        // keycode: 126: play, 85: pause
        // https://developer.android.com/reference/android/view/KeyEvent.html
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_BUTTON);

        m_receiver = new BTReceiver();
        context.registerReceiver(m_receiver, filter);

        m_session = new MediaSessionCompat(context, "AudioService");

        PlaybackStateCompat state = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_SEEK_TO |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_PLAY_PAUSE).build();

        m_session.setPlaybackState(state);
        m_session.setCallback(new BTCallback(this));
        m_session.setActive(true);
        m_service.setSessionToken(m_session.getSessionToken());

        m_mediaPlayer = new ExoPlayer.Builder(m_service).build();
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build();
        m_mediaPlayer.setAudioAttributes(attrs, true);
        m_mediaPlayer.setHandleAudioBecomingNoisy(true);

        m_mediaListener = new PlayerEventListener(this);
        m_mediaPlayer.addListener(m_mediaListener);

        m_manager = (android.media.AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        m_manager.setMode(android.media.AudioManager.MODE_NORMAL);

        loadQueueData();

        loadMediaPlayerState();

        Log.info("media session created");
    }

    public String formatTimeUpdate() {
        JSONObject update = new JSONObject();
        // position is the time in milliseconds
        long duration = m_mediaPlayer.getDuration();
        // duration is -1 during streaming
        if (duration < 0) {
            duration = 0;
        }
        try {
            update.put("duration", duration);
            update.put("position", m_mediaPlayer.getCurrentPosition());
            update.put("currentIndex", m_queue.getCurrentIndex());
        } catch (JSONException e) {
            android.util.Log.e("daedalus-js", "failed to format json");
        }
        return update.toString();
    }

    public MediaSessionCompat getSession() {
        return m_session;
    }

    //public MediaPlayer getPlayer() { return m_mediaPlayer; }

    public long getCurrentPosition() {
        return m_mediaPlayer.getCurrentPosition();
    }

    public void setQueueData(final String data) {

        m_queue.setData(data);

        if (m_queue.length() > 0) {
            m_queue.setCurrentIndex(0);
        } else {
            m_queue.setCurrentIndex(-1);
        }

        saveQueueData(data, m_queue.getCurrentIndex());


    }

    public String getQueueData() {
        return m_queue.getData();
    }

    public void updateQueueData(int index, final String data) {
        // update the queue to contain the new sequence of tracks
        // if index >=0 then update the current index to index
        //
        m_queue.setData(data);

        if (m_queue.length() > 0) {
            if (index >= 0 && m_queue.getCurrentIndex() != index) {
                m_queue.setCurrentIndex(index);
            }
        } else {
            m_queue.setCurrentIndex(-1);
        }

        saveQueueData(data, m_queue.getCurrentIndex());
    }

    /**
     *
     * @param url path to resource local file or url
     * @param autoPlay begin playback when finished loading
     * @param initialTimeMs -1: play from beginning, >=0 begin playback from time T in MS
     */
    public void loadUrl(final String url, boolean autoPlay, long initialTimeMs) {
        m_currentUrl = null;
        m_autoPlay = autoPlay;
        m_pausedTimeMs = initialTimeMs;

        if (url == null) {
            android.util.Log.e("daedalus-js", "null url given");
            return;
        }

        if (m_mediaPlayer.isPlaying()) {
            m_mediaPlayer.stop();
        }

        MediaItem item = new MediaItem.Builder()
                .setMediaId("0")
                .setUri(url).build();
        m_mediaPlayer.setMediaItem(item);
        m_mediaPlayer.setPlayWhenReady(m_autoPlay);
        m_mediaPlayer.prepare();

        m_currentUrl = url;
    }

    @Deprecated
    public void loadUrl(final String url) {
        m_currentUrl = null;
        m_pausedTimeMs = -1;

        if (url == null) {
            android.util.Log.e("daedalus-js", "null url given");
            return;
        }

        if (m_mediaPlayer.isPlaying()) {
            m_mediaPlayer.stop();
        }

        MediaItem item = new MediaItem.Builder()
                .setMediaId(url)
                .setUri(url).build();
        m_mediaPlayer.setMediaItem(item);
        m_mediaPlayer.prepare();
        m_currentUrl = url;
    }

    public void loadRadioUrl(final String url) {
        m_playback_mode = 1;
        loadUrl(url, false, -1);

        // replace the existing meta data with dummy data if there was an error
        MediaMetadataCompat data = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Unknown Artist")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Unknown Album")
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Unknown Title")
                .build();

        m_session.setMetadata(data);
    }

    public void playRadioUrl(final String url) {
        m_playback_mode = 1;
        loadUrl(url, true, -1);

        // replace the existing meta data with dummy data if there was an error
        MediaMetadataCompat data = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Unknown Artist")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Unknown Album")
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Unknown Title")
                .build();

        m_session.setMetadata(data);
    }

    public void loadIndex(int index) {
        m_queue.setCurrentIndex(index);
        SettingsTable tab = m_service.m_database.m_settingsTable;
        tab.setInt("current_index", index);
        m_playback_mode = 0;
        loadUrl( m_queue.getUrl(index), true, -1);

        // get the current meta data
        MediaMetadataCompat data = m_queue.getMetadata(index);

        if (data == null) {
            // replace the existing meta data with dummy data if there was an error
            data = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Unknown Artist")
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Unknown Album")
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Unknown Title")
                    .build();
        }

        m_session.setMetadata(data);

        m_service.updateNotification();

        m_service.sendEvent("onindexchanged", "{\"index\": " + m_queue.getCurrentIndex() + "}");
    }

    public void play() {

        /*
            playback can fail with an error (-32, 0) if the media has been paused
            for a long time. there is no way to query the current state,
            but m_currentUrl will be set in this case. Since the MediaPlayer state cannot
            be queried, catch the error and call loadResume instead of preemptivley
            trying to reload the current track.

            seems like simulating paused for a long time can be done by closing
            the app then swiping the notification while paused. using
            bluetooth to resume playback will then continue to play from the same spot

         */
        if (m_currentUrl == null) {
            Log.warn("mediaplayer play: no current url");
        } else {
            Log.info("mediaplayer play");
        }
        android.util.Log.d("myapp", android.util.Log.getStackTraceString(new Exception()));

        if (m_pausedTimeMs >= 0) {
            m_mediaPlayer.seekTo((int)m_pausedTimeMs);
        }

        m_mediaPlayer.play();

        long ct = getCurrentPosition();
        Log.info("on play current_time=" + ct + " paused_time=" + m_pausedTimeMs);

        m_service.updateNotification();

        sendStatus();
    }

    public void sendStatus() {
        String event = (m_mediaPlayer.isPlaying()?AudioEvents.ONPLAY:AudioEvents.ONERROR);
        m_service.sendEvent(event, "{}");
    }

    public void pause() {
        Log.info("mediaplayer pause");
        android.util.Log.d("myapp", android.util.Log.getStackTraceString(new Exception()));
        m_mediaPlayer.pause();
        m_service.updateNotification();

        String event = (m_mediaPlayer.isPlaying()?AudioEvents.ONERROR:AudioEvents.ONPAUSE);
        m_service.sendEvent(event, "{}");

        // todo: implement resume by calling start() followed by seekto(saved_position)
        m_pausedTimeMs = getCurrentPosition();

        saveMediaPlayerState();
    }

    public void stop() {
        m_mediaPlayer.stop();
    }

    public void release() {

        try {
            Log.info("service release");
            if (m_mediaPlayer.isPlaying()) {
                m_mediaPlayer.stop();
            }

            m_mediaPlayer.removeListener(m_mediaListener);
            m_mediaPlayer.release();

            m_exoHandler.removeCallbacks(updateProgressAction);

            m_mediaPlayer = null;
        }catch (RuntimeException e) {
            Log.error("failed to release", e.getMessage());
        }
    }

    public void nextRadioTrack() {

        m_service.m_executor.execute(new RadioNextTrackTask(m_service, m_token, m_station));

    }

    public void onSongEnd() {

        if (m_playback_mode == 0) {
            // normal playback
            try {
                long spk = m_queue.getSpk(m_queue.getCurrentIndex());
                m_service.m_database.m_songsTable.updatePlayTime(spk);
            } catch (JSONException e) {
                android.util.Log.e("daedalus-js", "unable update song playtime");
            }
            skipToNext();
        } else {
            // radio playback
            nextRadioTrack();
        }
    }

    public void skipToNext() {
        if (m_queue.next()) {
            loadIndex(m_queue.getCurrentIndex());
            m_service.sendEvent("onindexchanged", "{\"index\": " + m_queue.getCurrentIndex() + "}");
        } else {
            m_service.disableNotification();
        }

    }

    public void skipToPrev() {
        if (m_queue.prev()) {
            loadIndex(m_queue.getCurrentIndex());
            m_service.sendEvent("onindexchanged", "{\"index\": " + m_queue.getCurrentIndex() + "}");
        }
    }

    public void seek(long pos) {
        // seek to a position, units: ms
        Log.info("seek to " + pos);
        android.util.Log.d("myapp", android.util.Log.getStackTraceString(new Exception()));
        if (!m_isPlaying) {
            m_pausedTimeMs = pos;
        }
        m_mediaPlayer.seekTo(pos);
        m_service.updateNotification();
    }

    public boolean isPlaying() {
        return m_isPlaying;
    }

    public void setToken(final String token) {
        m_token = token;
    }

    public void setStation(final String station) {
        m_station = station;
    }

    public void saveQueueData(String data, int index) {
        // save the queue to a text file using json format
        String path = m_service.getExternalFilesDir(null)+ "/" + "queue.json";
        FileOutputStream stream = null;
        try {

            android.util.Log.e("daedalus-js", "write queue path is: '" + path + "'");
            android.util.Log.e("daedalus-js", "writing " + data.length() + " bytes.");

            File file = new File(path);
            stream = new FileOutputStream(file, false);

            stream.write(data.getBytes());
            stream.flush();

            android.util.Log.e("daedalus-js", "saved queue data: " +  m_queue.length());
        }
        catch (IOException|RuntimeException e) {
            android.util.Log.e("Exception", "File write failed: " + e);
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    android.util.Log.e("daedalus-js", "File close failed: " + e);
                }
            }
        }
    }

    public void loadQueueData() {
        String path = m_service.getExternalFilesDir(null)+ "/" + "queue.json";
        FileInputStream stream = null;
        try {

            File f = new File(path);
            if (!f.exists()) {
                android.util.Log.e("daedalus-js", "queue data does not exist");
                return;
            }

            android.util.Log.e("daedalus-js", "load queue path is: '" + path + "'");
            stream = new FileInputStream(path);

            InputStreamReader streamReader = new InputStreamReader(stream);
            BufferedReader reader = new BufferedReader(streamReader);

            StringWriter writer = new StringWriter();
            String line = reader.readLine();

            while (line != null && !line.equals("")) {
                writer.write(line);
                line = reader.readLine();
            }

            String data = writer.toString();
            android.util.Log.e("daedalus-js", "read " + data.length() + " bytes.");
            m_queue.setData(data);

            android.util.Log.e("daedalus-js", "loaded queue data: " + m_queue.length());
        } catch (IOException|RuntimeException e) {
            android.util.Log.e("daedalus-js", "File read failed: " + e);
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    android.util.Log.e("daedalus-js", "File close failed: " + e);
                }
            }
        }
    }

    private void loadMediaPlayerState() {
        SettingsTable tab = m_service.m_database.m_settingsTable;
        int current_index;
        long current_time;

        long count = tab.count();

        try {
            current_index = tab.getInt("current_index");
        } catch (SettingsTable.MissingValue e) {
            current_index = -1;
        }

        try {
            current_time = tab.getLong("current_time");
        } catch (SettingsTable.MissingValue e) {
            current_time = -1;
        }

        Log.info("load state: count=" + count + " index=" + current_index + " time_ms=" + current_time);

        if (current_index >= 0 && current_index < m_queue.length()) {
            m_queue.setCurrentIndex(current_index);
            loadUrl(m_queue.getUrl(current_index), false, current_time);
        }
    }


    private void saveMediaPlayerState() {

        if (m_service.m_database.isClosed()) {
            Log.warn("unable to save media player state because DB is closed");
            return;
        }
        int index = m_queue.getCurrentIndex();
        Log.info("save state: index=" + index + " time_ms=" + m_pausedTimeMs);
        SettingsTable tab = m_service.m_database.m_settingsTable;
        //tab.delete_all_rows();
        tab.setInt("current_index", index);
        tab.setLong("current_time", m_pausedTimeMs);


    }

    static class PlayerEventListener implements Player.Listener {

        private final AudioManager m_manager;
        PlayerEventListener(AudioManager manager) {
            m_manager = manager;
        }

        private String stateName(int state) {
            switch (state){
                case Player.STATE_IDLE:
                    return "IDLE";
                case Player.STATE_BUFFERING:
                    return "BUFFERING";
                case Player.STATE_READY:
                    return "READY";
                case Player.STATE_ENDED:
                    return "ENDED";
                default:
                    return "UNKNOWN";
            }
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            m_manager.m_isPlaying = isPlaying;
        }

        @Override
        public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason) {

            Log.warn("player discontinuity " + reason + " : " + oldPosition.positionMs + " => " + newPosition.positionMs);
        }

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            Log.warn("player state changed. state: " + playbackState + " " + stateName(playbackState));

            switch (playbackState) {

                case Player.STATE_ENDED:
                    m_manager.onSongEnd();
                    break;

                case Player.STATE_IDLE:
                case Player.STATE_BUFFERING:
                case Player.STATE_READY:
                default:
                    m_manager.updateProgressBar();
                    break;
            }
        }

        @Override
        public void onEvents(@NonNull Player player, Player.Events events) {
            //Player.Listener.super.onEvents(player, events);
            Log.error("lifecycle playback event: " + events.toString());
        }

        @Override
        public void onPlayerError(PlaybackException error) {
            Log.error("lifecycle playback error: " + error.getMessage());
            //Player.Listener.super.onPlayerError(error);
            //onSongEnd();
        }



    }

    private final Handler m_exoHandler = new Handler(Looper.getMainLooper());
    private final Runnable updateProgressAction = this::updateProgressBar;
    private void updateProgressBar() {


        // updating the notification here breaks seeing in android auto
        // only the session state needs to be updated
        // the notification and android auto progress will be updated.

        if (m_mediaPlayer == null) {
            return;
        }

        m_session.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(
                        m_isPlaying?PlaybackStateCompat.STATE_PLAYING:PlaybackStateCompat.STATE_PAUSED,
                        m_mediaPlayer.getCurrentPosition(),
                        1.0F)
                .setActions(PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_SEEK_TO |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .build());

        m_exoHandler.removeCallbacks(updateProgressAction);

        int playbackState = m_mediaPlayer.getPlaybackState();

        long delayMS=1000;
        if (m_mediaPlayer.getPlayWhenReady() && playbackState == Player.STATE_READY) {
            long delta = 1000 - m_mediaPlayer.getCurrentPosition() % 1000;
            if (delta < 200) {
                delta = 200;
            }
            delayMS = delta;
        }

        if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
            m_exoHandler.postDelayed(updateProgressAction, delayMS);
        }

    }
}
