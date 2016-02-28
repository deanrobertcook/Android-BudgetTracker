package org.theronin.expensetracker.dagger;

import android.app.Fragment;

public abstract class InjectedFragment extends Fragment implements InjectedComponent {

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        inject(this);
//    }

    @Override
    public void onStart() {
        super.onStart();
        inject(this);
    }

    @Override
    public void inject(Object object) {
        ((InjectedComponent) getActivity()).inject(object);
    }
}
