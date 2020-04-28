package com.github.nicksetzer.daedalus.audio;

import android.content.Intent;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;

public class BTCallback extends MediaSessionCompat.Callback {

    AudioManager m_manager;
    public BTCallback(AudioManager manager) {
        m_manager = manager;
    }

    /*
    @Override
    public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
        android.util.Log.e("daedalus-js","received media button event");
        return false; // true when handled
    }
    */

    @Override
    public void onPlay() {

        android.util.Log.e("daedalus-js", "onplay");
        // some bluetooth devices only send play events
        if (m_manager.isPlaying()) {
            m_manager.pause();
        } else {
            m_manager.play();
        }

    }

    @Override
    public void onPause() {

        android.util.Log.e("daedalus-js","onpause");
        m_manager.pause();
    }

    @Override
    public void onStop() {

        android.util.Log.e("daedalus-js","onstop");
        m_manager.stop();
    }

    @Override
    public void onSkipToNext() {

        android.util.Log.e("daedalus-js","onskiptonext");
        m_manager.skipToNext();
    }

    @Override
    public void onSkipToPrevious() {

        android.util.Log.e("daedalus-js","onskiptoprev");
        m_manager.skipToPrev();
    }

    @Override
    public void onSeekTo(long pos) {

        android.util.Log.e("daedalus-js","onseek");
        m_manager.seek(pos);
    }

    @Override
    public void onFastForward() {

        android.util.Log.e("daedalus-js","onfastforward");
    }

    @Override
    public void onRewind() {

        android.util.Log.e("daedalus-js","onwind");
    }

    @Override
    public void onSetRating(RatingCompat rating) {

        android.util.Log.e("daedalus-js","onsetrating");
    }

}
