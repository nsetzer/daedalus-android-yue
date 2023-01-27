package com.github.nicksetzer.daedalus;

public class Log {

    private static final String TAG = "daedalus-js";
    private static int level = 4;

    public static int DISABLED=0;
    public static int ERROR=1;
    public static int WARNING=2;
    public static int INFO=3;
    public static int DEBUG=4;
    public static int TRACE=5;

    /**
     *
     * @return the stack frame of the calling method
     */
    private static StackTraceElement _getStackTraceElement() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        int index = 0;

        //for (StackTraceElement elem : elements) {
        //    android.util.Log.e(TAG, elem.getFileName() + " : " + elem.getClassName());
        //}

        while (index < elements.length - 2) {
            if (elements[index].getClassName().equals(Log.class.getName())) {
                index += 2;
                break;
            }
            index += 1;
        }
        return elements[index];
    }

    private static String _formatStackTraceElement(StackTraceElement element, Object[] messages) {

        StringBuilder sb = new StringBuilder();

        sb.append("[");
        sb.append(element.getClassName());
        sb.append("::");
        sb.append(element.getMethodName());
        sb.append("::");
        sb.append(element.getLineNumber());
        sb.append("] ");

        for (int i=0; i < messages.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            if (messages[i] != null) {
                sb.append(messages[i].toString());
            } else {
                sb.append("<NULL>");
            }
        }

        return sb.toString();
    }

    public void setLogLevel(int level) {
        Log.level = level;
    }

    public static void trace(Object... messages) {
        if (Log.level < Log.TRACE) {
            return;
        }
        StackTraceElement element = _getStackTraceElement();
        android.util.Log.d(TAG, _formatStackTraceElement(element, messages));
    }

    public static void debug(Object... messages) {
        if (Log.level < Log.DEBUG) {
            return;
        }
        StackTraceElement element = _getStackTraceElement();
        android.util.Log.d(TAG, _formatStackTraceElement(element, messages));
    }

    public static void info(Object... messages) {
        if (Log.level < Log.INFO) {
            return;
        }
        StackTraceElement element = _getStackTraceElement();
        android.util.Log.i(TAG, _formatStackTraceElement(element, messages));
    }

    public static void warn(Object... messages) {
        if (Log.level < Log.WARNING) {
            return;
        }
        StackTraceElement element = _getStackTraceElement();
        android.util.Log.w(TAG, _formatStackTraceElement(element, messages));
    }

    public static void error(Object... messages) {
        if (Log.level < Log.ERROR) {
            return;
        }
        StackTraceElement element = _getStackTraceElement();
        android.util.Log.e(TAG, _formatStackTraceElement(element, messages));
    }

    public static void error(Object message, Exception e) {
        if (Log.level < Log.ERROR) {
            return;
        }
        StackTraceElement element = _getStackTraceElement();

        android.util.Log.e(TAG, _formatStackTraceElement(element, new Object[]{message, e.toString(), e.getMessage()}));
    }
}
