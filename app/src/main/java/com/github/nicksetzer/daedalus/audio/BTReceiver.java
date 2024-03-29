package com.github.nicksetzer.daedalus.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.github.nicksetzer.daedalus.Log;

public class BTReceiver extends BroadcastReceiver {

    private final String TAG = "BTReceiver";

    public BTReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        android.util.Log.e("daedalus-js", "received action: " + action);
        final KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent.class);

        if (event != null) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    // stop music
                    Log.info("bt stop");
                    break;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    // pause music
                    Log.info("bt play_pause");
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    // next track
                    Log.info("bt next");
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    // previous track
                    Log.info("bt previous");
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    // play/pause
                    Log.info("bt play");
                    break;
            }
        }
    }


}
