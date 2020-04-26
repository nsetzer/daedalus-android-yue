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
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.github.nicksetzer.daedalus.audio.AudioActions;
import com.github.nicksetzer.daedalus.audio.AudioWebView;
import com.github.nicksetzer.daedalus.javascript.AndroidClient;
import com.github.nicksetzer.daedalus.javascript.LocalStorage;
import com.github.nicksetzer.daedalus.javascript.NativeAudio;
import com.github.nicksetzer.daedalus.view.DaedalusWebChromeClient;
import com.github.nicksetzer.daedalus.view.DaedalusWebViewClient;

import androidx.core.content.ContextCompat;

public class WebActivity extends Activity {

    String profile = "prd";

    ServiceEventReceiver m_receiver;

    private Handler m_timeHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web);

        AudioWebView view = findViewById(R.id.DaedalusView);

        view.setWebContentsDebuggingEnabled(true);

        view.getSettings().setJavaScriptEnabled(true);

        m_receiver = new ServiceEventReceiver();
        registerReceiver(m_receiver, new IntentFilter(AudioActions.ACTION_EVENT));

        view.setWebChromeClient(new DaedalusWebChromeClient());
        view.setWebViewClient(new DaedalusWebViewClient(this, this.profile == "dev"));
        view.addJavascriptInterface(new LocalStorage(this), "LocalStorage");
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

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (m_serviceBound && m_audioService != null) {

                    if (m_audioService.mediaIsPlaying()) {
                        String update = m_audioService.getFormattedTimeUpdate();

                        WebActivity.this.invokeJavascriptCallback("ontimeupdate", update);
                    }
                }

                m_timeHandler.postDelayed(this, 1000);
            }
        });
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
        final String command = "true";
        final String script = "(function() {\n" +
            "const result = window.history.goBack();\n" + // non-standard function
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
        if (!result) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        android.util.Log.e("daedalus-js", "code: " + keyCode);
        return false;
    }

    public void launchAudioService() {

        Intent intent = new Intent(this, AudioService.class);
        startService(intent);
        android.util.Log.e("daedalus-js", "launching audio service");
    }

    private int STORAGE_PERMISSION_CODE = 23;

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

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, AudioService.class);
        bindService(intent, m_serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(m_receiver);
        if (m_serviceBound) {
            unbindService(m_serviceConnection);
        }
    }

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

}
