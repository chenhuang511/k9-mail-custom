package vn.bhxh.bhxhmail.mailstore.migrations;


import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import vn.bhxh.bhxhmail.Account;
import vn.bhxh.bhxhmail.mail.Folder;


class MigrationTo50 {
    public static void foldersAddNotifyClassColumn(SQLiteDatabase db, MigrationsHelper migrationsHelper) {
        try {
            db.execSQL("ALTER TABLE folders ADD notify_class TEXT default '" +
                    Folder.FolderClass.INHERITED.name() + "'");
        } catch (SQLiteException e) {
            if (!e.getMessage().startsWith("duplicate column name:")) {
                throw e;
            }
        }

        ContentValues cv = new ContentValues();
        cv.put("notify_class", Folder.FolderClass.FIRST_CLASS.name());

        Account account = migrationsHelper.getAccount();
        db.update("folders", cv, "name = ?", new String[] { account.getInboxFolderName() });
    }
}
