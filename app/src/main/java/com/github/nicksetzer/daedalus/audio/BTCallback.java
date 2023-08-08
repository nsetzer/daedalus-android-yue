package com.github.nicksetzer.daedalus.audio;

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
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
        Log.error("lifecycle onMediaButtonEvent");
        if (mediaButtonIntent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent event = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            //event.getAction() == KeyEvent.ACTION_UP ||
            // somethings send down and up, others only down
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        m_manager.skipToPrev();
                        return true;
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        m_manager.skipToNext();
                        return true;
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        m_manager.play();
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
                        Log.warn("lifecycle unhandled keycode:" + event.getKeyCode());
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
        //super.onPlay();

        Log.info( "lifecycle onPlay");
        // some bluetooth devices only send play events
        if (m_manager.isPlaying()) {
            m_manager.pause();
        } else {
            m_manager.play();
        }

    }

    @Override
    public void onPlayFromMediaId(String mediaId, Bundle extras) {
        super.onPlayFromMediaId(mediaId, extras);
        Log.info( "lifecycle onPlayFromMediaId:" + mediaId + " : " +extras.toString());
        int index = Integer.parseInt(mediaId.substring(mediaId.lastIndexOf('-') + 1));
        m_manager.loadIndex(index);

    }

    @Override
    public void onPause() {
        //super.onPause();

        Log.info("lifecycle onpause");
        m_manager.pause();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.info("lifecycle onstop");
        m_manager.stop();
    }

    @Override
    public void onSkipToNext() {

        Log.info("lifecycle onskiptonext");
        m_manager.skipToNext();
    }

    @Override
    public void onSkipToPrevious() {

        Log.info("lifecycle onskiptoprev");
        m_manager.skipToPrev();
    }

    @Override
    public void onSeekTo(long pos) {

        Log.info("lifecycle onseek");
        m_manager.seek(pos);
    }

    @Override
    public void onFastForward() {

        Log.info("lifecycle onfastforward");
    }

    @Override
    public void onRewind() {

        Log.info("lifecycle onRewind");
    }

    @Override
    public void onSetRating(RatingCompat rating) {

        Log.info("lifecycle onSetRating");
    }

    @Override
    public void onCommand(String command, Bundle extras, ResultReceiver cb) {
        Log.info("lifecycle onCommand");

        super.onCommand(command, extras, cb);
    }

    @Override
    public void onCustomAction(String action, Bundle extras) {
        Log.info("lifecycle onCustomAction");
        super.onCustomAction(action, extras);
    }


}
