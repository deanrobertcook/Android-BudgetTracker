package org.theronin.expensetracker.data;

import org.theronin.expensetracker.data.source.DataSourceModule;
import org.theronin.expensetracker.data.sync.EntityPushCoordinatorTest;

import dagger.Module;

@Module(includes = DataSourceModule.class,
        injects = {
                AbsDataSourceTest.class,
                EntityPushCoordinatorTest.class
        }
)
public class TestDataSourceModule {
}
