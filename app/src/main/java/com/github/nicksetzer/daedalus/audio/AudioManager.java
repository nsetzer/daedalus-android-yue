package com.github.nicksetzer.daedalus.audio;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.github.nicksetzer.daedalus.Log;
import com.github.nicksetzer.daedalus.audio.tasks.RadioNextTrackTask;
import com.github.nicksetzer.metallurgy.orm.EntityTable;
import com.github.nicksetzer.metallurgy.orm.NaturalPrimaryKey;
import com.github.nicksetzer.metallurgy.orm.Statement;
import com.github.nicksetzer.metallurgy.orm.StatementBuilder;

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

    private AudioService m_service;

    final String MP3URL = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";
    private BTReceiver m_receiver;

    private MediaSessionCompat m_session;

    private MediaPlayer m_mediaPlayer;

    private android.media.AudioManager m_manager;

    private boolean m_autoPlay = true;
    private int playback_mode = 0;

    private String m_token = null;
    private String m_station = null;

    private String m_currentUrl = null;
    private int m_pausedTimeMs = -1;

    AudioQueue m_queue;

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
        m_session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        PlaybackStateCompat state = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE).build();

        m_session.setPlaybackState(state);
        m_session.setCallback(new BTCallback(this));
        m_session.setActive(true);

        m_mediaPlayer = new MediaPlayer();

        //m_mediaPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
        m_mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());

        m_mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                // what:
                //MediaPlayer.MEDIA_ERROR_UNKNOWN
                //MediaPlayer.MEDIA_ERROR_SERVER_DIED
                String s_what = "";
                switch (what) {
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                        s_what = "unknown";
                        break;
                    case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                        s_what = "unsupported";
                        break;
                    case -38:
                        loadResume();
                        s_what = "state error";
                        break;
                    default:
                        s_what = "other";
                        break;
                }
                s_what += " (" + what + ")";


                // extra:
                //MediaPlayer.MEDIA_ERROR_IO
                //MediaPlayer.MEDIA_ERROR_MALFORMED
                //MediaPlayer.MEDIA_ERROR_UNSUPPORTED
                //MediaPlayer.MEDIA_ERROR_TIMED_OUT
                //MEDIA_ERROR_SYSTEM
                String payload = "{\"what\": " + what + ", \"extra\": " + extra + "}";
                Log.error("sending error to javascript: " + s_what + " extra=" + extra);
                m_service.sendEvent(AudioEvents.ONERROR, payload);

                return true; // true when error is handled
            }
        });

        m_mediaPlayer.setOnPreparedListener (new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // do stuff here
                if (m_autoPlay) {
                    m_mediaPlayer.start();
                    if (m_pausedTimeMs >= 0) {
                        m_mediaPlayer.seekTo(m_pausedTimeMs);
                    }
                    m_service.updateNotification();
                    m_service.sendEvent(AudioEvents.ONPLAY, "{}");
                }

                m_service.sendEvent("onprepared", "{}");

                m_service.sendEvent("ontimeupdate", AudioManager.this.formatTimeUpdate());

            }
        });

        m_mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                onSongEnd();
            }
        });

        m_manager = (android.media.AudioManager) context.getSystemService(context.AUDIO_SERVICE);
        m_manager.setMode(android.media.AudioManager.MODE_NORMAL);


        loadQueueData();

        loadMediaPlayerState();

        android.util.Log.e("daedalus-js", "media session created");
    }

    public String formatTimeUpdate() {
        JSONObject update = new JSONObject();
        // position is the time in milliseconds
        int duration = m_mediaPlayer.getDuration();
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

    public MediaPlayer getPlayer() { return m_mediaPlayer; }

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

    public void loadUrl(final String url) {
        m_currentUrl = null;
        m_pausedTimeMs = -1;

        if (url == null) {
            android.util.Log.e("daedalus-js", "null url given");
            return;
        }
        try {
            if (m_mediaPlayer.isPlaying()) {
                m_mediaPlayer.stop();
            }
            m_mediaPlayer.reset();

            // note: logging the url may Log the user token
            android.util.Log.i("daedalus-js", "url: " + url);
            m_mediaPlayer.setDataSource(url);
            m_mediaPlayer.prepareAsync();

            m_currentUrl = url;

        } catch(IOException e) {
            android.util.Log.e("daedalus-js", e.toString());
        }
    }

    public void loadResume() {
        if (playback_mode != 0) {
            Log.warn("unable to resume in current playback_mode=" + playback_mode);
            return;
        }


        m_autoPlay = true;
        int index = m_queue.getCurrentIndex();
        Log.info("resume playback for index=" + index);
        loadUrl( m_queue.getUrl(index));

        m_service.updateNotification();
    }

    public void loadRadioUrl(final String url) {
        playback_mode = 1;
        m_autoPlay = false;
        loadUrl(url);

        // replace the existing meta data with dummy data if there was an error
        MediaMetadataCompat data = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Unknown Artist")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Unknown Album")
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Unknown Title")
                .build();

        m_session.setMetadata(data);
    }

    public void playRadioUrl(final String url) {
        playback_mode = 1;
        m_autoPlay = true;
        loadUrl(url);

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
        playback_mode = 0;
        m_autoPlay = true;
        loadUrl( m_queue.getUrl(index));

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

        m_mediaPlayer.start();
        m_service.updateNotification();

        sendStatus();
    }

    public void sendStatus() {
        String event = (m_mediaPlayer.isPlaying()?AudioEvents.ONPLAY:AudioEvents.ONERROR);
        m_service.sendEvent(event, "{}");
    }

    public void pause() {
        Log.info("mediaplayer pause");
        m_mediaPlayer.pause();
        m_service.updateNotification();

        String event = (m_mediaPlayer.isPlaying()?AudioEvents.ONERROR:AudioEvents.ONPAUSE);
        m_service.sendEvent(event, "{}");

        // todo: implement resume by calling start() followed by seekto(saved_position)
        m_pausedTimeMs = m_mediaPlayer.getCurrentPosition();

        saveMediaPlayerState();
    }

    public void stop() {
        m_mediaPlayer.stop();
    }

    public void nextRadioTrack() {

        m_service.m_executor.execute(new RadioNextTrackTask(m_service, m_token, m_station));

    }

    public void onSongEnd() {

        if (playback_mode == 0) {
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
        android.util.Log.e("daedalus-js", "seek to " + pos);
        m_mediaPlayer.seekTo(pos, MediaPlayer.SEEK_PREVIOUS_SYNC);
    }

    public boolean isPlaying() {
        return m_mediaPlayer.isPlaying();
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
        catch (IOException e) {
            android.util.Log.e("Exception", "File write failed: " + e.toString());
        }
        catch (RuntimeException e) {
            android.util.Log.e("Exception", "File write failed: " + e.toString());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    android.util.Log.e("daedalus-js", "File close failed: " + e.toString());
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
        } catch (IOException e) {
            android.util.Log.e("daedalus-js", "File read failed: " + e.toString());
        } catch (RuntimeException e) {
            android.util.Log.e("daedalus-js", "File read failed: " + e.toString());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    android.util.Log.e("daedalus-js", "File close failed: " + e.toString());
                }
            }
        }
    }

    private void loadMediaPlayerState() {
        EntityTable tab = m_service.m_database.m_settingsTable;
        Cursor cursor = null;
        JSONObject obj = null;
        int current_index = -1;
        int current_time = -1;

        long count = tab.count();

        try {
            current_index = ((SettingsTable) tab).getInt("current_index");
        } catch (SettingsTable.MissingValue e) {
            current_index = -1;
        }

        try {
            current_time = ((SettingsTable) tab).getInt("current_time");
        } catch (SettingsTable.MissingValue e) {
            current_time = -1;
        }

        Log.info("load state: count=" + count + " index=" + current_index + " time_ms=" + current_time);

        if (current_index >= 0 && current_index < m_queue.length()) {
            m_queue.setCurrentIndex(current_index);
            m_autoPlay = false;
            loadUrl( m_queue.getUrl(current_index));
        }
    }


    private void saveMediaPlayerState() {

        int index = m_queue.getCurrentIndex();
        Log.info("save state: index=" + index + " time_ms=" + m_pausedTimeMs);
        SettingsTable tab = m_service.m_database.m_settingsTable;
        //tab.delete_all_rows();
        tab.setInt("current_index", index);
        tab.setInt("current_time", m_pausedTimeMs);


    }
}
