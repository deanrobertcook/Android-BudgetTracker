package org.theronin.expensetracker.data;

import org.theronin.expensetracker.data.source.DataSourceModule;

import dagger.Module;

@Module(includes = DataSourceModule.class,
        injects = {
            AbsDataSourceTest.class
        }
)
public class TestDataSourceModule {
}
