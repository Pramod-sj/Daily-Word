package com.pramod.dailyword.framework.util;

import android.content.Context;

import java.io.PrintWriter;
import java.io.StringWriter;


public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {
    Context context;
    Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    public CustomExceptionHandler(Context context) {
        this.context = context;
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        String stackTrace = stringWriter.toString();
        printWriter.close();
        killCurrentProcess();
    }

    public static void killCurrentProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

}
