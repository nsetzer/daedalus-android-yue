package com.github.nicksetzer.daedalus.audio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.KeyEvent;

import com.github.nicksetzer.daedalus.Log;

public class BTCallback extends MediaSessionCompat.Callback {

    AudioManager m_manager;
    public BTCallback(AudioManager manager) {
        m_manager = manager;
    }


    @Override
    public boolean onMediaButtonEvent(Intent mediaButtonIntent) {

        if (mediaButtonIntent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent event = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent.class);
            //event.getAction() == KeyEvent.ACTION_UP ||
            // somethings send down and up, others only down
            String button;
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                button = "down";
            } else {
                button = "up";
            }
            Log.info("onMediaButtonEvent ", button, event.getKeyCode());
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        m_manager.skipToPrev();
                        return true;
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        m_manager.skipToNext();
                        return true;
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        onPlay();
                        return true;
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        m_manager.pause();
                        return true;
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        if (m_manager.isPlaying()) {
                            m_manager.pause();
                        } else {
                            m_manager.play();
                        }
                        return true;

                    default:
                        break;
                }
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                Log.info("extra intent received", mediaButtonIntent.toString(), "action: up");

            } else {
                Log.info("extra intent received", mediaButtonIntent.toString(), "action:", event.getAction());
            }
            /*
            Log.info("received media button event: " + action);
            for (String key : mediaButtonIntent.getExtras().keySet()) {
                Log.info(key, mediaButtonIntent.getExtras().get(key).toString());
            }
            */
        } else {
            Log.warn("invalid onMediaButtonEvent", mediaButtonIntent.getAction());
        }

        return super.onMediaButtonEvent(mediaButtonIntent);
    }
    /*
    @Override
    protected boolean onKeyEvent(KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode())
            {
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    m_manager.play();
                    return true;
                break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    m_manager.pause();
                    return true;
                break;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    if (mediaIsPlaying()) {
                        m_manager.pause();
                    } else {
                        m_manager.play();
                    }
                    return true;
                break;
                default:
                    break;
            }
        }

        return false;
    }
    */


    @Override
    public void onPlay() {


        // some bluetooth devices only send play events
        Log.info( "onplay isplaying=", m_manager.isPlaying(), m_manager.isPlaying()?"-->pause":"-->play");
        if (m_manager.isPlaying()) {
            m_manager.pause();
        } else {
            m_manager.play();
        }

    }

    @Override
    public void onPause() {

        Log.info("onpause");
        m_manager.pause();
    }

    @Override
    public void onStop() {

        Log.info("onstop");
        m_manager.stop();
    }

    @Override
    public void onSkipToNext() {

        Log.info("onskiptonext");
        m_manager.skipToNext();
    }

    @Override
    public void onSkipToPrevious() {

        Log.info("onskiptoprev");
        m_manager.skipToPrev();
    }

    @Override
    public void onSeekTo(long pos) {

        Log.info("onseek", pos);
        m_manager.seek(pos);
    }

    @Override
    public void onFastForward() {

        Log.info("onfastforward");
    }

    @Override
    public void onRewind() {

        Log.info("onrewind");
    }

    @Override
    public void onSetRating(RatingCompat rating) {

        Log.info("onsetrating");
    }
    @Override
    public void onPlayFromMediaId(String mediaId, Bundle extras) {
        super.onPlayFromMediaId(mediaId, extras);

        Log.info( "lifecycle onPlayFromMediaId:" + mediaId + " : " +extras.toString());

        if (mediaId.startsWith("/nowplaying")) {
            int index = Integer.parseInt(mediaId.substring(mediaId.lastIndexOf('/') + 1));
            m_manager.loadIndex(index);
        }
        else if (mediaId.startsWith("/quicklist")) {
            int index = Integer.parseInt(mediaId.substring(mediaId.lastIndexOf('/') + 1));

            // tracks = m_service.m_songTable.query()
            // shuffle tracks
            // select top 200
            // set playlist
            m_manager.buildQuickList(index);
            Log.warn("load media id: " + index);
        }

    }

    @Override
    public void onCustomAction(String action, Bundle extras) {
        Log.warn("onCustomAction", action, extras);
        super.onCustomAction(action, extras);
    }
}
