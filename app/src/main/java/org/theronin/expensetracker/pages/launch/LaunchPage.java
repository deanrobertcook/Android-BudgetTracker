package org.theronin.expensetracker.pages.launch;

public enum LaunchPage {
    WELCOME(new WelcomeFragment()),
    SIGN_IN(new SignInFragment()),
    CREATE_ACCOUNT(new CreateAccountFragment());

    public LaunchFragment fragment;

    LaunchPage(LaunchFragment fragment) {
        this.fragment = fragment;
    }
}