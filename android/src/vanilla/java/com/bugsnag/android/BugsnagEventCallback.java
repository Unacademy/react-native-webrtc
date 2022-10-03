package com.bugsnag.android;

public interface BugsnagEventCallback {
    boolean onEvent(BugsnagEvent event);
}
