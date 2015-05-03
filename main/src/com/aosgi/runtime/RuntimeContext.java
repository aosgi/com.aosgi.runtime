package com.aosgi.runtime;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

/**
 * The android OSGi runtime service which registered when the OSGi framework
 * startup.
 * 
 * @author johnson
 * 
 */
public class RuntimeContext extends ContextWrapper {

    RuntimeContext(Context base) {
        super(base);
    }

    @Override
    public ComponentName startService(Intent service) {
        // TODO
        return super.startService(service);
    }

    @Override
    public void startActivity(Intent intent) {
        // TODO
        super.startActivity(intent);
    }

    @Override
    public void sendBroadcast(Intent intent) {
        // TODO
        super.sendBroadcast(intent);
    }

}
