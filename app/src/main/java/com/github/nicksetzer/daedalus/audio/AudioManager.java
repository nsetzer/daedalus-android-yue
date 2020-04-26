package com.github.nicksetzer.daedalus.audio;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.github.nicksetzer.daedalus.AudioService;

import java.io.IOException;

public class AudioManager {

    private AudioService m_service;

    final String MP3URL = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";
    private BTReceiver m_receiver;

    private MediaSessionCompat m_session;

    private MediaPlayer m_mediaPlayer;

    private android.media.AudioManager m_manager;

    private boolean m_autoPlay = true;

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

                // extra:
                //MediaPlayer.MEDIA_ERROR_IO
                //MediaPlayer.MEDIA_ERROR_MALFORMED
                //MediaPlayer.MEDIA_ERROR_UNSUPPORTED
                //MediaPlayer.MEDIA_ERROR_TIMED_OUT
                //MEDIA_ERROR_SYSTEM

                return true; // true when error is handled
            }
        });

        m_mediaPlayer.setOnPreparedListener (new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // do stuff here
                if (m_autoPlay) {
                    m_mediaPlayer.start();
                }
            }
        });

        m_mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // do stuff here
                skipToNext();
            }
        });

        m_manager = (android.media.AudioManager) context.getSystemService(context.AUDIO_SERVICE);
        m_manager.setMode(android.media.AudioManager.MODE_NORMAL);

        android.util.Log.e("daedalus-js", "media session created");
    }

    public MediaSessionCompat getSession() {
        return m_session;
    }

    public void setQueueData(final String data) {
        m_queue.setData(data);
        if (m_queue.length() > 0) {
            m_queue.setCurrentIndex(0);
        } else {
            m_queue.setCurrentIndex(-1);
        }
    }

    public void updateQueueData(int index, final String data) {
        m_queue.setData(data);
        if (m_queue.length() > 0) {
            m_queue.setCurrentIndex(index);
        } else {
            m_queue.setCurrentIndex(-1);
        }
    }

    public void loadUrl(final String path) {

        if (path == null) {
            android.util.Log.e("daedalus-js", "null url given");
            return;
        }
        try {
            if (m_mediaPlayer.isPlaying()) {
                m_mediaPlayer.stop();
            }
            m_mediaPlayer.reset();

            m_mediaPlayer.setDataSource(path);
            m_mediaPlayer.prepareAsync();
        } catch(IOException e) {
            android.util.Log.e("daedalus-js", e.toString());
        }
    }

    public void loadIndex(int index) {
        loadUrl( m_queue.getUrl(index));
    }


    public void play() {
        m_mediaPlayer.start();
    }

    public void pause() {
        m_mediaPlayer.pause();
    }

    public void stop() {
        m_mediaPlayer.stop();
    }

    public void skipToNext() {
        if (m_queue.next()) {
            loadIndex(m_queue.getCurrentIndex());
        }

    }

    public void skipToPrev() {
        if (m_queue.prev()) {
            loadIndex(m_queue.getCurrentIndex());
        }
    }

    public void seek(long pos) {
        // seek to a position, units: ms
        m_mediaPlayer.seekTo((int) pos);
    }

}
