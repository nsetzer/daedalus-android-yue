package com.github.nicksetzer.daedalus.audio;

import android.content.Intent;
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
            KeyEvent event = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event.getAction() == KeyEvent.ACTION_UP) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        m_manager.skipToNext();
                        return true;
                    default:
                        break;
                }
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


    @Override
    public void onPlay() {

        Log.info( "onplay");
        // some bluetooth devices only send play events
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

        Log.info("onseek");
        m_manager.seek(pos);
    }

    @Override
    public void onFastForward() {

        Log.info("onfastforward");
    }

    @Override
    public void onRewind() {

        Log.info("onwind");
    }

    @Override
    public void onSetRating(RatingCompat rating) {

        Log.info("onsetrating");
    }

}
