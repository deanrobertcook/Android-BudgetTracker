package org.theronin.expensetracker.data.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import timber.log.Timber;

public class SyncService extends Service {

    private static SyncAdapter syncAdapter = null;

    private static final Object syncAdapterLock = new Object();

    @Override
    public void onCreate() {
        Timber.d("onCreate()");
        synchronized (syncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = new SyncAdapter(getApplication(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
