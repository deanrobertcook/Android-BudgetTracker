package org.theronin.expensetracker.dagger;

/**
 * An InjectedComponent, after implementing inject(), should automatically inject themselves at an
 * appropriate point in the their lifecycle so that all classes using the component have access
 * to the injected dependencies. See the abstract implementations InjectedActivity, InjectedFragment
 * or InjectedService.
 */
public interface InjectedComponent {

    /**
     * InjectedComponents should implement the inject method so that objects that use them can inject
     * themselves with the modules from the Application's ObjectGraph.
     * @param object
     */
    void inject(Object object);
}
