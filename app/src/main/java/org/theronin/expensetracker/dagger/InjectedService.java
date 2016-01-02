package org.theronin.expensetracker.dagger;

import android.app.IntentService;

public abstract class InjectedService extends IntentService implements InjectedComponent {

    public InjectedService(String name) {
        super(name);
        inject(this);
    }

    @Override
    public void inject(Object object) {
        ((InjectedComponent) getApplication()).inject(object);
    }
}