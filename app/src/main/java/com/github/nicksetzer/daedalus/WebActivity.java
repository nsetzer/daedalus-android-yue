package com.github.nicksetzer.daedalus;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.view.KeyEvent;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Toast;

import com.github.nicksetzer.daedalus.audio.AudioActions;
import com.github.nicksetzer.daedalus.audio.AudioEvents;
import com.github.nicksetzer.daedalus.audio.AudioService;
import com.github.nicksetzer.daedalus.audio.AudioWebView;
import com.github.nicksetzer.daedalus.javascript.AndroidClient;
import com.github.nicksetzer.daedalus.javascript.LocalStorage;
import com.github.nicksetzer.daedalus.javascript.NativeAudio;
import com.github.nicksetzer.daedalus.view.DaedalusWebChromeClient;
import com.github.nicksetzer.daedalus.view.DaedalusWebViewClient;

public class WebActivity extends Activity {

    String profile = "prd";

    ServiceEventReceiver m_receiver;
    ScreenEventReceiver m_screen_receiver = null;
    public LocalStorage m_storage;

    private Handler m_timeHandler = new Handler();

    Runnable m_updateTimeout;

    boolean m_isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.info("lifecycle onCreate");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web);

        m_receiver = new ServiceEventReceiver();

        AudioWebView view = findViewById(R.id.DaedalusView);

        view.setWebContentsDebuggingEnabled(true);

        view.getSettings().setJavaScriptEnabled(true);

        view.setWebChromeClient(new DaedalusWebChromeClient());
        view.setWebViewClient(new DaedalusWebViewClient(this, this.profile == "dev"));
        m_storage = new LocalStorage(this);
        view.addJavascriptInterface(m_storage, "LocalStorage");
        view.addJavascriptInterface(new AndroidClient(this), "Client");
        view.addJavascriptInterface(new NativeAudio(this), "AndroidNativeAudio");

        view.getSettings().setAllowUniversalAccessFromFileURLs(true);

        if (this.profile == "dev") {
            // 192.168.1.149
            view.loadUrl("http://10.0.2.2:4100");
        } else {
            view.loadUrl("file:///android_asset/site/index.html");
        }

        launchAudioService();

        m_updateTimeout = new Runnable() {

            @Override
            public void run() {

                if (m_serviceBound && m_audioService != null) {

                    if (m_audioService.mediaIsPlaying()) {
                        String update = m_audioService.getFormattedTimeUpdate();

                        WebActivity.this.invokeJavascriptCallback("ontimeupdate", update);
                    }
                }

                // cancel the timer when the app has been destroyed or paused
                // resuming the app will restart the timer
                if (!m_isPaused && m_timeHandler != null) {
                    m_timeHandler.postDelayed(this, 1000);
                }

            }
        };

        runOnUiThread(m_updateTimeout);
        //m_timeHandler.postDelayed(m_updateTimeout, 1000);

        Log.info("successfully launch on create");
        Log.warn("successfully launch on create");
        Log.error("successfully launch on create");

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);

        m_screen_receiver = new ScreenEventReceiver();
        registerReceiver(m_screen_receiver, filter);


        /*
        // use reflection to get a stack trace whenever a resource is not closed.

        try {
            Class.forName("dalvik.system.CloseGuard")
                    .getMethod("setEnabled", boolean.class)
                    .invoke(null, true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        */
    }

    @Override
    protected void onStart() {
        Log.info("lifecycle onStart");
        super.onStart();

        android.util.Log.e("daedalus-js", "register receiver: ");

        registerReceiver(m_receiver, new IntentFilter(AudioActions.ACTION_EVENT));


        Intent intent = new Intent(this, AudioService.class);
        bindService(intent, m_serviceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onPause() {
        Log.info("lifecycle onPause");
        super.onPause();

        m_isPaused = true;
    }

    @Override
    protected void onResume() {
        Log.info("lifecycle onResume");
        super.onResume();

        m_isPaused = false;
        m_timeHandler.postDelayed(m_updateTimeout, 1000);
    }

    @Override
    public void onPostResume() {
        Log.info("lifecycle onPostResume");
        super.onPostResume();
        // called when the app comes into focus, the user is present

        invokeJavascriptCallback(AudioEvents.ONRESUME, "{}");

        if (m_serviceBound && m_audioService != null) {

            // always even if not playing
            String update = m_audioService.getFormattedTimeUpdate();
            WebActivity.this.invokeJavascriptCallback("ontimeupdate", update);

            m_audioService.m_manager.sendStatus();
        }
    }

    @Override
    protected void onStop() {
        Log.info("lifecycle onStop");
        super.onStop();
        try {
            unregisterReceiver(m_receiver);
        } catch(IllegalArgumentException e) {
            // api issue on android
            android.util.Log.e("daedalus-js", "Receiver not registered: " + e.toString());
        }
        if (m_serviceBound) {
            unbindService(m_serviceConnection);
        }
    }

    @Override
    protected void onDestroy() {
        Log.info("lifecycle onDestroy");
        super.onDestroy();
        if (m_screen_receiver != null) {
            unregisterReceiver(m_screen_receiver);
            m_screen_receiver = null;
        }

        m_timeHandler = null;
    }

    @Override
    public void onBackPressed() {
        /**
         * When the back button is pressed return to the previous page in the browser.
         *
         * Warning: This assumes a Daedalus Application is running in the webview
         * if using a different javascript function the command below may need to be changed
         */

        WebView view = findViewById(R.id.DaedalusView);
        final String script = "(function() {\n" +
            "const result = window.history.back();\n" + // non-standard function
            "return result;\n" +
            "})();";

        view.evaluateJavascript(script, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String str) {
                onBackPressedResult(str.toLowerCase().equals("true"));
            }
        });

    }

    public void onBackPressedResult(boolean result) {
        //if (!result) {
        //    super.onBackPressed();
        //}
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {



        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                android.util.Log.i("daedalus-js", "KeyEvent.KEYCODE_BACK");
                onBackPressed();
                break;
            default:
                android.util.Log.e("daedalus-js", "android key code: " + keyCode);
        }
        return false;
    }

    public void launchAudioService() {

        Intent intent = new Intent(this, AudioService.class);
        // originally was startService
        //startService(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        android.util.Log.e("daedalus-js", "launching audio service");
    }

    private int STORAGE_PERMISSION_CODE = 1000; // application specific request code

    private void requestStoragePermission(){


        if (this.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }

        //And finally ask for the permission
        this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        //Checking the request code of our request
        if(requestCode == STORAGE_PERMISSION_CODE){

            //If permission is granted
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                //Displaying a toast
                Toast.makeText(this,"Permission granted now you can read the storage",Toast.LENGTH_LONG).show();
            }else{
                //Displaying another toast if permission is not granted
                Toast.makeText(this,"Oops you just denied the permission",Toast.LENGTH_LONG).show();
            }
        }


    }

    private AudioService m_audioService;
    private boolean m_serviceBound = false;

    private ServiceConnection m_serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            m_audioService = null;
            m_serviceBound = false;
            android.util.Log.e("daedalus-js", "unbound service");
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.AudioBinder myBinder = (AudioService.AudioBinder) service;
            m_audioService = myBinder.getService();
            m_serviceBound = true;
            android.util.Log.e("daedalus-js", "binding to service");

        }
    };

    public AudioService getBoundService() {
        return m_audioService;
    }

    public void invokeJavascriptCallback(String name, String payload) {

        AudioWebView view = findViewById(R.id.DaedalusView);
        if (!AndroidClient.ready) {
            Log.error("daedalus-js", "attempt to invoke js before document is ready. signal: " + name + ":" + payload + " (AndroidClient.documentLoaded not called)");
            return;
        }

        view.loadUrl("javascript:invokeAndroidEvent('" + name + "', '" + payload.replace("\'", "\\\'") + "')");
    }

    class ServiceEventReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(AudioActions.ACTION_EVENT))
            {
                final String name = intent.getExtras().getString("name");
                final String payload = intent.getExtras().getString("payload");
                invokeJavascriptCallback(name, payload);

            }
        }

    }

    class ScreenEventReceiver extends BroadcastReceiver {

        public boolean wasScreenOn = true;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                wasScreenOn = false;
                Log.info("screen is off");
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                wasScreenOn = true;
                Log.info("screen is on");
            } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                Log.info("user now present");
            }
        }
    }





}
