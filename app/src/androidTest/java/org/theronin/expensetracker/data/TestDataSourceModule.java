package org.theronin.expensetracker.data;

import org.theronin.expensetracker.AppModule;
import org.theronin.expensetracker.data.sync.EntityPushCoordinatorTest;

import dagger.Module;

@Module(includes = AppModule.class,
        injects = {
                AbsDataSourceTest.class,
                EntityPushCoordinatorTest.class
        }
)
public class TestDataSourceModule {
}
