package com.theronin.budgettracker.file;

import android.os.AsyncTask;
import android.os.Environment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.theronin.budgettracker.model.Entry;
import com.theronin.budgettracker.utils.DateUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

public class FileWriterTask extends AsyncTask<List<Entry>, Void, Void> {
    private final String BACKUP_DIRECTORY = "BudgetTracker";

    @Override
    protected Void doInBackground(List<Entry>... params) {
        List<Entry> entries = params[0];
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String entriesJson = gson.toJson(entries);

        if (isExternalStorageWriteable()) {
            File backupsDirectory = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS) + "/" + BACKUP_DIRECTORY);

            if (!backupsDirectory.mkdirs() && !backupsDirectory.isDirectory()) {
                throw new RuntimeException("Could not find or create backup directory");
            }

            File backupFile = new File(backupsDirectory, DateUtils.getStorageFormattedCurrentDate() + ".json");

            PrintWriter writer = null;
            try {
                writer = new PrintWriter(backupFile);
                writer.println(entriesJson);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
            return null;
        }
        throw new RuntimeException("External storage currently not available");
    }

    private boolean isExternalStorageWriteable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
