package org.theronin.budgettracker.task;

import android.os.AsyncTask;
import android.os.Environment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.theronin.budgettracker.model.Category;
import org.theronin.budgettracker.model.Currency;
import org.theronin.budgettracker.model.Entry;
import org.theronin.budgettracker.utils.DateUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class FileBackupAgent  {
    private static final String TAG = FileBackupAgent.class.getName();
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
            entriesJson = simplifyJson(gson, entriesJson);

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

        /**
         * Removes any redundant fields from the json array
         */
        private String simplifyJson(Gson gson, String entriesJson) {
            JsonArray jsonArray = gson.fromJson(entriesJson, JsonElement.class).getAsJsonArray();
            for (JsonElement element : jsonArray) {
                JsonObject object = (JsonObject) element;
                changeDateToUtc(object);
                replaceCategoryObjectWithName(object);
                replaceCurrencyObjectWithCode(object);
            }
            return gson.toJson(jsonArray);
        }

        private void changeDateToUtc(JsonObject object) {
            if (object.has("utcDateEntered")) {
                long utcTime = object.get("utcDateEntered").getAsLong();
                String formattedDate = DateUtils.getStorageFormattedDate(utcTime);
                object.remove("utcDateEntered");
                object.addProperty("dateEntered", formattedDate);
            }
        }

        private void replaceCategoryObjectWithName(JsonObject object) {
            if (object.has("category")) {
                JsonObject category = (JsonObject) object.get("category");
                String categoryName = category.get("name").getAsString();
                object.addProperty("category", categoryName);
            }
        }

        private void replaceCurrencyObjectWithCode(JsonObject object) {
            if (object.has("currency")) {
                JsonObject category = (JsonObject) object.get("currency");
                String categoryName = category.get("code").getAsString();
                object.addProperty("currency", categoryName);
            }
        }

        private boolean isExternalStorageWriteable() {
            String state = Environment.getExternalStorageState();
            return Environment.MEDIA_MOUNTED.equals(state);
        }
    }

    private class FileReaderTask extends AsyncTask<Void, Void, List<Entry>> {

        @Override
        protected List<Entry> doInBackground(Void... params) {
            Timber.d("FileReaderTask: doInBackground");
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
                    return buildEntries(buildJsonArray(sb.toString()));
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

        private JsonArray buildJsonArray(String jsonString) {
            Gson gson = new Gson();
            return gson.fromJson(jsonString, JsonArray.class);
        }

        private List<Entry> buildEntries(JsonArray jsonArray) {
            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject object = (JsonObject) jsonArray.get(i);
                Entry entry = new Entry(
                        findId(object),
                        findDate(object),
                        findAmount(object),
                        findCategory(object),
                        findCurrency(object)
                );
                entries.add(entry);
            }
            return entries;
        }

        private long findId(JsonObject object) {
            return object.get("id").getAsLong();
        }

        private long findDate(JsonObject object) {
            String formattedDate = object.get("dateEntered").getAsString();
            return DateUtils.getUtcTimeFromStorageFormattedDate(formattedDate);
        }

        private long findAmount(JsonObject object) {
            return object.get("amount").getAsLong();
        }

        private Category findCategory(JsonObject object) {
            String categoryName = object.get("category").getAsString();
            return new Category(categoryName);
        }

        private Currency findCurrency(JsonObject object) {
            String currencyCode = object.get("currency").getAsString();
            return new Currency(currencyCode);
        }

        @Override
        protected void onPostExecute(List<Entry> entries) {
            listener.onEntriesRestored(entries);
        }
    }

    public interface Listener {
        void onEntriesRestored(List<Entry> entries);
    }
}
