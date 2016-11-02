package vn.bhxh.bhxhmail.mailstore.migrations;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import vn.bhxh.bhxhmail.K9;


class MigrationTo39 {
    public static void headersPruneOrphans(SQLiteDatabase db) {
        try {
            db.execSQL("DELETE FROM headers WHERE id in (SELECT headers.id FROM headers LEFT JOIN messages ON headers.message_id = messages.id WHERE messages.id IS NULL)");
        } catch (SQLiteException e) {
            Log.e(K9.LOG_TAG, "Unable to remove extra header data from the database");
        }
    }
}
