package org.theronin.expensetracker.data;

import org.theronin.expensetracker.AppModule;
import org.theronin.expensetracker.data.sync.PushCoordinatorTest;

import dagger.Module;

@Module(includes = AppModule.class,
        injects = {
                AbsDataSourceTest.class,
                PushCoordinatorTest.class
        },
        overrides = true
)
public class TestDataSourceModule {
}
