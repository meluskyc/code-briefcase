package org.meluskyc.codebriefcase.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.meluskyc.codebriefcase.R;

public class CodeBriefcaseDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "codebriefcase.db";
    private static final int DB_VERSION = 8;

    // tables and fields
    public static final String ITEM_TABLE = "item";
    public static final String TAG_TABLE = "tag";
    public static final String ITEM_SEARCH_TABLE = "item_search";
    public static final String ITEM_ID = "_id";
    public static final String ITEM_DESCRIPTION = "description";
    public static final String ITEM_CONTENT = "content";
    public static final String ITEM_DATE_CREATED = "date_created";
    public static final String ITEM_DATE_UPDATED = "date_updated";
    public static final String ITEM_TAG_PRIMARY = "tag_primary";
    public static final String ITEM_TAG_SECONDARY = "tag_secondary";
    public static final String ITEM_STARRED = "starred";
    public static final String TAG_ID = "_id";
    public static final String TAG_NAME = "name";
    public static final String TAG_ACE_MODE = "ace_mode";
    public static final String TAG_COLOR = "color";

    // SQL
    private static final String SQL_CREATE_ITEM =
            "CREATE TABLE item " +
                    "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "description TEXT, " +
                    "content TEXT, " +
                    "date_created INTEGER, " +
                    "date_updated INTEGER, " +
                    "tag_primary TEXT, " +
                    "tag_secondary TEXT, " +
                    "starred INTEGER);";

    private static final String SQL_CREATE_TAG =
            "CREATE TABLE tag " +
                    "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "ace_mode TEXT, " +
                    "color TEXT);";

    private static final String SQL_CREATE_ITEM_SEARCH =
            "CREATE VIRTUAL TABLE item_search USING fts4 " +
                    "(content='item', " +
                    "description, " +
                    "date_updated, " +
                    "tag_primary, " +
                    "tag_secondary, " +
                    "starred);";

    private static final String SQL_CREATE_TRIGGER_SEARCH_BU =
            "CREATE TRIGGER search_bu BEFORE UPDATE ON item BEGIN " +
                "DELETE FROM item_search WHERE docid=old.rowid;" +
            "END;";

    private static final String SQL_CREATE_TRIGGER_SEARCH_BD =
            "CREATE TRIGGER search_bd BEFORE DELETE ON item BEGIN " +
                    "DELETE FROM item_search WHERE docid=old.rowid;" +
            "END;";

    private static final String SQL_CREATE_TRIGGER_SEARCH_AU =
            "CREATE TRIGGER search_au AFTER UPDATE ON item BEGIN " +
                    "INSERT INTO item_search(docid, description, date_updated, " +
                    "tag_primary, tag_secondary, starred) VALUES (new.rowid, new.description, " +
                    "new.date_updated, new.tag_primary, new.tag_secondary, new.starred);" +
            "END;";

    private static final String SQL_CREATE_TRIGGER_SEARCH_AI =
            "CREATE TRIGGER search_ai AFTER INSERT ON item BEGIN " +
                    "INSERT INTO item_search(docid, description, date_updated, tag_primary, " +
                    "tag_secondary, starred) VALUES (new.rowid, new.description, new.date_updated, " +
                    "new.tag_primary, new.tag_secondary, new.starred);" +
            "END;";

    private static final String SQL_CREATE_TAG_INDEX = "CREATE INDEX tag_index ON tag (name);";

    private static final String SQL_DROP_ITEM = "DROP TABLE IF EXISTS item;";
    private static final String SQL_DROP_TAG = "DROP TABLE IF EXISTS tag;";
    private static final String SQL_DROP_TAG_INDEX = "DROP INDEX IF EXISTS tag_index;";
    private static final String SQL_DROP_ITEM_SEARCH = "DROP TABLE IF EXISTS item_search;";
    private static final String SQL_DROP_TRIGGER_SEARCH_BU = "DROP TRIGGER IF EXISTS search_bu;";
    private static final String SQL_DROP_TRIGGER_SEARCH_BD = "DROP TRIGGER IF EXISTS search_bd;";
    private static final String SQL_DROP_TRIGGER_SEARCH_AU = "DROP TRIGGER IF EXISTS search_au;";
    private static final String SQL_DROP_TRIGGER_SEARCH_AI = "DROP TRIGGER IF EXISTS search_ai;";

    private static CodeBriefcaseDatabase dbHelper;
    private Context context;

    private CodeBriefcaseDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context.getApplicationContext();
    }

    public static synchronized CodeBriefcaseDatabase getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new CodeBriefcaseDatabase(context.getApplicationContext());
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String[] tags = context.getResources().getStringArray(R.array.Tag);
        String[] tagAceModes = context.getResources().getStringArray(R.array.TagAceMode);
        String[] tagColors = context.getResources().getStringArray(R.array.TagColors);

        db.execSQL(SQL_CREATE_ITEM);
        db.execSQL(SQL_CREATE_TAG);
        db.execSQL(SQL_CREATE_TAG_INDEX);
        db.execSQL(SQL_CREATE_ITEM_SEARCH);
        db.execSQL(SQL_CREATE_TRIGGER_SEARCH_BU);
        db.execSQL(SQL_CREATE_TRIGGER_SEARCH_BD);
        db.execSQL(SQL_CREATE_TRIGGER_SEARCH_AU);
        db.execSQL(SQL_CREATE_TRIGGER_SEARCH_AI);

        // add the tags
        db.beginTransaction();
        ContentValues values = new ContentValues();
        for (int i = 0; i < tags.length; i++) {
            values.put(TAG_NAME, tags[i]);
            values.put(TAG_ACE_MODE, tagAceModes[i]);
            values.put(TAG_COLOR, tagColors[i]);
            db.insert(TAG_TABLE, null, values);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_ITEM);
        db.execSQL(SQL_DROP_TAG);
        db.execSQL(SQL_DROP_ITEM_SEARCH);
        db.execSQL(SQL_DROP_TAG_INDEX);
        db.execSQL(SQL_DROP_TRIGGER_SEARCH_BU);
        db.execSQL(SQL_DROP_TRIGGER_SEARCH_BD);
        db.execSQL(SQL_DROP_TRIGGER_SEARCH_AU);
        db.execSQL(SQL_DROP_TRIGGER_SEARCH_AI);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
