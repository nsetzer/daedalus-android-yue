

### WebView Debugger

In the Activity, enable debugging on the `WebView`.

```java
WebView view = findViewById(R.id.webView1);
view.setWebContentsDebuggingEnabled(true);
```

In chrome browse the the following URL:

```
chrome://inspect/#devices
```

Then click `inspect` on the Remote Target.

### Branches
````
  master
    using mediaplayer not exoplayer
    not working with android auto

  exoplayer
    working with android auto
    old style notifications (from master, manual update notification)
    android auto buttons not working
    notification buttons not working

  exoplayer-notification-manager
    working with android auto
    new style notifications
    android auto buttons not working
    notification buttons not working

  media3
    using media3 exoplayer, instead of media2 simpleexoplayer
    notifications not workig
    android auto not working
    delete and try again when exoplayer-notification-manager is ready
```
