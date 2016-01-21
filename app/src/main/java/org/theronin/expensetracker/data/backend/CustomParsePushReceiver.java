package org.theronin.expensetracker.data.backend;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.parse.ParseInstallation;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

import org.theronin.expensetracker.data.backend.entry.ParseEntryRemoteSync;
import org.theronin.expensetracker.utils.SyncUtils;

import timber.log.Timber;

public class CustomParsePushReceiver extends ParsePushBroadcastReceiver {

    public static final String UPDATE_NOTIFICATION_FLAG = "deviceShouldUpdate";
    public static final String PARSE_DATA_INTENT_KEY = "com.parse.Data";
    public static final String PARSE_CHANNEL_INTENT_KEY = "com.parse.Channel";
    public static final String PARSE_DATA_INSTALLATION_ID_KEY = ParseEntryRemoteSync.PARSE_DATA_INSTALLATION_ID_KEY;

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        Timber.v("onPushReceive");
        String pushData = intent.getStringExtra(PARSE_DATA_INTENT_KEY);
        if (pushData.contains(UPDATE_NOTIFICATION_FLAG)) {
            String channel = intent.getStringExtra(PARSE_CHANNEL_INTENT_KEY);
            ParseUser user = ParseUser.getCurrentUser();
            if (user != null && user.getObjectId().equals(channel)) {
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(pushData, JsonObject.class);
                if (jsonObject.has(PARSE_DATA_INSTALLATION_ID_KEY)) {
                    String installationId = jsonObject.get(PARSE_DATA_INSTALLATION_ID_KEY).getAsString();
                    if (!installationId.equals(ParseInstallation.getCurrentInstallation().getInstallationId())) {
                        SyncUtils.requestSync(context);
                    }
                }
            }
        } else {
            super.onPushReceive(context, intent);
        }
    }
}
