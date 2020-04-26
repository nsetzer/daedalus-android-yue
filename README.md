

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