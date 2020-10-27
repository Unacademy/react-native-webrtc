package com.oney.WebRTCModule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.bugsnag.android.Bugsnag;

final class ThreadUtils {
    /**
     * Thread which will be used to call all WebRTC PeerConnection APIs. They
     * they don't run on the calling thread anyway, we are deferring the calls
     * to this thread to avoid (potentially) blocking the calling thread.
     */
    private static final ExecutorService executor
        = Executors.newSingleThreadExecutor();

    /**
     * Runs the given {@link Runnable} on the executor.
     * @param runnable
     */
    public static void runOnExecutor(Runnable runnable) {
        executor.execute(runnable);
    }

    public static void addExceptionHandlerForThread(Thread.UncaughtExceptionHandler h, String threadName) {
        Thread t = getThreadByName(threadName);
        if (t != null) {
            t.setUncaughtExceptionHandler(h);
        } else {
            Bugsnag.notify(new IllegalAccessError("Thread not found in webrtc: " + threadName));
        }
    }

    public static Thread getThreadByName(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName) && t.isAlive()) return t;
        }
        return null;
    }
}
