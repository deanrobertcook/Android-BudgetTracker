package org.theronin.expensetracker.data;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;

import org.theronin.expensetracker.dagger.InjectedComponent;
import org.theronin.expensetracker.data.source.DataSourceModule;
import org.theronin.expensetracker.data.source.DbHelper;

import dagger.ObjectGraph;

public class TestApplication implements InjectedComponent {

    private ObjectGraph graph;
    private Instrumentation instrumentation;
    private DbHelper testDbHelper;

    public TestApplication() {
        instrumentation = InstrumentationRegistry.getInstrumentation();
        testDbHelper = DbHelper.getInstance(instrumentation.getTargetContext(), null);

        graph = ObjectGraph.create(
                new TestDataSourceModule(),
                new DataSourceModule(instrumentation.getTargetContext(), this, testDbHelper));
    }

    @Override
    public void inject(Object object) {
        graph.inject(object);
    }
}
