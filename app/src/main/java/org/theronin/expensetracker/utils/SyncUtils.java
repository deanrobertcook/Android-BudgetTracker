package org.theronin.expensetracker.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import org.theronin.expensetracker.R;

public class SyncUtils {

    private static final String ACCOUNT = "dummyaccount";

    public static void requestSync(Context context) {
        Bundle extras = new Bundle();
        ContentResolver.requestSync(createSyncAccount(context),
                context.getString(R.string.content_authority),
                extras);
    }

    private static Account createSyncAccount(Context context) {
        Account account = new Account(ACCOUNT, context.getString(R.string.sync_account_type));
        AccountManager accountManager = AccountManager.get(context);

        ContentResolver.setSyncAutomatically(account, context.getString(R.string.content_authority), true);
        accountManager.addAccountExplicitly(account, null, null);

        return account;
    }
}
