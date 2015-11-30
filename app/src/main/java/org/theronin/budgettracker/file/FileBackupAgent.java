package org.theronin.budgettracker.file;

import android.os.AsyncTask;
import android.os.Environment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.theronin.budgettracker.model.Entry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FileBackupAgent  {
    private final String BACKUP_DIRECTORY = "BudgetTracker";
    private final String BACKUP_FILE = "backup.json";
    private Listener listener;

    public void backupEntries(List<Entry> entries) {
        new FileWriterTask().execute(entries);
    }

    public void restoreEntriesFromBackup(Listener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener cannot be null");
        }
        this.listener = listener;
        new FileReaderTask().execute();
    }

    private class FileWriterTask extends AsyncTask<List<Entry>, Void, Void> {
        @SafeVarargs
        @Override
        protected final Void doInBackground(List<Entry>... params) {
            List<Entry> entries = params[0];
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String entriesJson = gson.toJson(entries);

            if (isExternalStorageWriteable()) {
                File backupsDirectory = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS) + "/" + BACKUP_DIRECTORY);

                if (!backupsDirectory.mkdirs() && !backupsDirectory.isDirectory()) {
                    throw new RuntimeException("Could not find or create backup directory");
                }

                File backupFile = new File(backupsDirectory, BACKUP_FILE);

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
            return Environment.MEDIA_MOUNTED.equals(state);
        }
    }

    private class FileReaderTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            if (isExternalStorageReadable()) {
                File backupFile = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS) + "/" + BACKUP_DIRECTORY, BACKUP_FILE);

                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(backupFile));
                    StringBuilder sb = new StringBuilder();
                    String line = br.readLine();

                    while (line != null) {
                        sb.append(line);
                        sb.append(System.lineSeparator());
                        line = br.readLine();
                    }
                    return sb.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null;
        }

        private boolean isExternalStorageReadable() {
            String state = Environment.getExternalStorageState();
            return Environment.MEDIA_MOUNTED.equals(state) ||
                    Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
        }

        @Override
        protected void onPostExecute(String entriesJson) {
            Type entryListType = new TypeToken<ArrayList<Entry>>(){}.getType();
            Gson gson = new Gson();
            List<Entry> entries = gson.fromJson(entriesJson, entryListType);
            listener.onEntriesRestored(entries);
        }
    }

    public interface Listener {
        void onEntriesRestored(List<Entry> entries);
    }
}
