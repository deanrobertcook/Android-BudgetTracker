package org.theronin.expensetracker.pages.launch;

public enum LaunchPage {
    WELCOME(new WelcomeFragment()),
    SKIP_ACCOUNT(new SkipAccountFragment()),
    CREATE_ACCOUNT(new CreateAccountFragment()),
    SIGN_IN(new SignInFragment()),
    ENTER_APP(null);

    public LaunchFragment fragment;

    LaunchPage(LaunchFragment fragment) {
        this.fragment = fragment;
    }
}