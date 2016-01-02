package org.theronin.expensetracker.data;

import org.theronin.expensetracker.AppModule;
import org.theronin.expensetracker.data.sync.EntityPushCoordinatorTest;
import org.theronin.expensetracker.data.sync.EntrySaverTest;

import dagger.Module;

@Module(includes = AppModule.class,
        injects = {
                AbsDataSourceTest.class,
                EntityPushCoordinatorTest.class,
                EntrySaverTest.class
        },
        overrides = true
)
public class TestDataSourceModule {
}
