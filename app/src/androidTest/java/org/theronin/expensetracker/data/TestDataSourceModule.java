package org.theronin.expensetracker.data;

import org.theronin.expensetracker.AppModule;
import org.theronin.expensetracker.data.backend.entry.EntrySyncCoordinatorTest;

import dagger.Module;

@Module(includes = AppModule.class,
        injects = {
                AbsDataSourceTest.class,
                EntrySyncCoordinatorTest.class
        },
        overrides = true
)
public class TestDataSourceModule {
}
