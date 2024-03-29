package vn.bhxh.bhxhmail.preferences;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import vn.bhxh.bhxhmail.K9;


public class StorageEditor {
    private Storage storage;
    private Map<String, String> changes = new HashMap<String, String>();
    private List<String> removals = new ArrayList<String>();

    Map<String, String> snapshot = new HashMap<String, String>();


    StorageEditor(Storage storage) {
        this.storage = storage;
        snapshot.putAll(storage.getAll());
    }

    public void copy(android.content.SharedPreferences input) {
        Map < String, ? > oldVals = input.getAll();
        for (Entry < String, ? > entry : oldVals.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key != null && value != null) {
                if (K9.DEBUG) {
                    Log.d(K9.LOG_TAG, "Copying key '" + key + "', value '" + value + "'");
                }
                changes.put(key, "" + value);
            } else {
                if (K9.DEBUG) {
                    Log.d(K9.LOG_TAG, "Skipping copying key '" + key + "', value '" + value + "'");
                }
            }
        }
    }

    public boolean commit() {
        try {
            commitChanges();
            return true;
        } catch (Exception e) {
            Log.e(K9.LOG_TAG, "Failed to save preferences", e);
            return false;
        }
    }

    private void commitChanges() {
        long startTime = System.currentTimeMillis();
        Log.i(K9.LOG_TAG, "Committing preference changes");
        Runnable committer = new Runnable() {
            public void run() {
                for (String removeKey : removals) {
                    storage.remove(removeKey);
                }
                Map<String, String> insertables = new HashMap<String, String>();
                for (Entry<String, String> entry : changes.entrySet()) {
                    String key = entry.getKey();
                    String newValue = entry.getValue();
                    String oldValue = snapshot.get(key);
                    if (removals.contains(key) || !newValue.equals(oldValue)) {
                        insertables.put(key, newValue);
                    }
                }
                storage.put(insertables);
            }
        };
        storage.doInTransaction(committer);
        long endTime = System.currentTimeMillis();
        Log.i(K9.LOG_TAG, "Preferences commit took " + (endTime - startTime) + "ms");

    }

    public StorageEditor putBoolean(String key,
            boolean value) {
        changes.put(key, "" + value);
        return this;
    }

    public StorageEditor putInt(String key, int value) {
        changes.put(key, "" + value);
        return this;
    }

    public StorageEditor putLong(String key, long value) {
        changes.put(key, "" + value);
        return this;
    }

    public StorageEditor putString(String key, String value) {
        if (value == null) {
            remove(key);
        } else {
            changes.put(key, value);
        }
        return this;
    }

    public StorageEditor remove(String key) {
        removals.add(key);
        return this;
    }
}
