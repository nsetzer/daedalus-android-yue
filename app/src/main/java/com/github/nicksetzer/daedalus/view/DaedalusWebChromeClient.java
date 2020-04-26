package com.github.nicksetzer.daedalus.view;

import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;

public class DaedalusWebChromeClient extends WebChromeClient {

    @Override
    public boolean onConsoleMessage(ConsoleMessage cm) {

        String tag = "daedalus-js";
        String msg = cm.message() + " -- From line "
                + cm.lineNumber() + " of "
                + cm.sourceId();

        switch (cm.messageLevel()) {

            case ERROR:
                android.util.Log.e(tag, msg);
                break;
            case WARNING:
                android.util.Log.w(tag, msg);
                break;
            case LOG:
                android.util.Log.i(tag, msg);
                break;
            case DEBUG:
                android.util.Log.d(tag, msg);
                break;

            case TIP:
            default:
                android.util.Log.i(tag, msg);
                break;
        }

        return true;
    }
}
