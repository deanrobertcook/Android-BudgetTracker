package org.theronin.expensetracker.dagger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public abstract class InjectedActivity extends AppCompatActivity implements InjectedComponent {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inject(this);
    }

    @Override
    public void inject(Object object) {
        ((InjectedComponent) getApplication()).inject(object);
    }
}
