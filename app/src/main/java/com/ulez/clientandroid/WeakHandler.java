package com.ulez.clientandroid;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

public abstract class WeakHandler<T extends Context> extends Handler {
    private WeakReference<T> reference;

    public WeakHandler(T context) {
        reference = new WeakReference<>(context);
    }

    @Override
    public void handleMessage(Message msg) {
        T activity = reference.get();
        if (activity != null) {
            handle(activity, msg);
        }
    }

    public abstract void handle(T activity, Message msg);
}
