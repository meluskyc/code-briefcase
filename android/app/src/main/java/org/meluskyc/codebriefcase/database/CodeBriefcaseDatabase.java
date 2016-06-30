package org.meluskyc.codebriefcase.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.meluskyc.codebriefcase.R;
import org.meluskyc.codebriefcase.database.CodeBriefcaseContract.ItemColumns;
import org.meluskyc.codebriefcase.database.CodeBriefcaseContract.TagColumns;

/**
 * {@link SQLiteOpenHelper} class for {@link CodeBriefcaseProvider}.
 *
 * based on Google I/O 2015 app at https://git.io/vKYuK
 */
public class CodeBriefcaseDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "codebriefcase.db";
    private static final int DB_VERSION = 8;

    interface Tables {
        String ITEM = "item";
        String TAG = "tag";
        String ITEM_SEARCH = "item_search";
        String ITEM_JOIN_TAG = "item INNER JOIN tag " +
                "ON item.tag_primary = tag.name";
        String ITEM_SEARCH_JOIN_TAG = "item_search INNER JOIN tag " +
                "ON item_search.tag_primary = tag.name";
    }

    private interface Triggers {
        // Deletes from dependent tables when corresponding sessions are deleted.
        String ITEM_SEARCH_BU = "item_search_bu";
        String ITEM_SEARCH_BD = "item_search_bd";
        String ITEM_SEARCH_AU = "item_search_au";
        String ITEM_SEARCH_AI = "item_search_ai";
    }

    interface ItemSearchColumns {
        String ITEM_SEARCH_DOCID = "docid";
        String ITEM_SEARCH_DESCRIPTION = "description";
        String ITEM_SEARCH_DATE_UPDATED = "date_updated";
        String ITEM_SEARCH_TAG_PRIMARY = "tag_primary";
        String ITEM_SEARCH_TAG_SECONDARY = "tag_secondary";
        String ITEM_SEARCH_STARRED = "starred";
    }

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

        db.execSQL("CREATE TABLE " + Tables.ITEM + " ("
                + ItemColumns.ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ItemColumns.ITEM_DESCRIPTION + " TEXT,"
                + ItemColumns.ITEM_CONTENT + " TEXT,"
                + ItemColumns.ITEM_DATE_CREATED + " INTEGER,"
                + ItemColumns.ITEM_DATE_UPDATED + " INTEGER,"
                + ItemColumns.ITEM_TAG_PRIMARY + " TEXT,"
                + ItemColumns.ITEM_TAG_SECONDARY + " TEXT,"
                + ItemColumns.ITEM_STARRED + " INTEGER)");

        db.execSQL("CREATE TABLE " + Tables.TAG + " ("
                + TagColumns.TAG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TagColumns.TAG_NAME + " TEXT,"
                + TagColumns.TAG_ACE_MODE + " TEXT,"
                + TagColumns.TAG_COLOR + " TEXT)");

        db.execSQL("CREATE VIRTUAL TABLE " + Tables.ITEM_SEARCH + " USING fts4("
                + "content='" + Tables.ITEM + "', "
                + ItemSearchColumns.ITEM_SEARCH_DESCRIPTION + ", "
                + ItemSearchColumns.ITEM_SEARCH_DATE_UPDATED + ", "
                + ItemSearchColumns.ITEM_SEARCH_TAG_PRIMARY + ", "
                + ItemSearchColumns.ITEM_SEARCH_TAG_SECONDARY + ", "
                + ItemSearchColumns.ITEM_SEARCH_STARRED + ")");

        db.execSQL("CREATE TRIGGER " + Triggers.ITEM_SEARCH_BU + " BEFORE UPDATE ON "
                + Tables.ITEM + " BEGIN DELETE FROM " + Tables.ITEM_SEARCH
                + " WHERE " + ItemSearchColumns.ITEM_SEARCH_DOCID + "=old.rowid;END;");

        db.execSQL("CREATE TRIGGER " + Triggers.ITEM_SEARCH_BD + " BEFORE DELETE ON "
                + Tables.ITEM + " BEGIN DELETE FROM " + Tables.ITEM_SEARCH
                + " WHERE " + ItemSearchColumns.ITEM_SEARCH_DOCID + "=old.rowid;END;");

        db.execSQL("CREATE TRIGGER " + Triggers.ITEM_SEARCH_AU + " AFTER UPDATE ON "
                + Tables.ITEM + " BEGIN INSERT INTO " + Tables.ITEM_SEARCH + "("
                + ItemSearchColumns.ITEM_SEARCH_DOCID + ", "
                + ItemSearchColumns.ITEM_SEARCH_DESCRIPTION + ", "
                + ItemSearchColumns.ITEM_SEARCH_DATE_UPDATED + ", "
                + ItemSearchColumns.ITEM_SEARCH_TAG_PRIMARY + ", "
                + ItemSearchColumns.ITEM_SEARCH_TAG_SECONDARY + ", "
                + ItemSearchColumns.ITEM_SEARCH_STARRED + ") VALUES (new.rowid, "
                + "new." + ItemSearchColumns.ITEM_SEARCH_DESCRIPTION + ", "
                + "new." + ItemSearchColumns.ITEM_SEARCH_DATE_UPDATED + ", "
                + "new." + ItemSearchColumns.ITEM_SEARCH_TAG_PRIMARY + ", "
                + "new." + ItemSearchColumns.ITEM_SEARCH_TAG_SECONDARY + ", "
                + "new." + ItemSearchColumns.ITEM_SEARCH_STARRED + ");END;");

        db.execSQL("CREATE TRIGGER " + Triggers.ITEM_SEARCH_AI + " AFTER INSERT ON "
                + Tables.ITEM + " BEGIN INSERT INTO " + Tables.ITEM_SEARCH + "("
                + ItemSearchColumns.ITEM_SEARCH_DOCID + ", "
                + ItemSearchColumns.ITEM_SEARCH_DESCRIPTION + ", "
                + ItemSearchColumns.ITEM_SEARCH_DATE_UPDATED + ", "
                + ItemSearchColumns.ITEM_SEARCH_TAG_PRIMARY + ", "
                + ItemSearchColumns.ITEM_SEARCH_TAG_SECONDARY + ", "
                + ItemSearchColumns.ITEM_SEARCH_STARRED + ") VALUES (new.rowid, "
                + "new." + ItemSearchColumns.ITEM_SEARCH_DESCRIPTION + ", "
                + "new." + ItemSearchColumns.ITEM_SEARCH_DATE_UPDATED + ", "
                + "new." + ItemSearchColumns.ITEM_SEARCH_TAG_PRIMARY + ", "
                + "new." + ItemSearchColumns.ITEM_SEARCH_TAG_SECONDARY + ", "
                + "new." + ItemSearchColumns.ITEM_SEARCH_STARRED + ");END;");

        // add the tags
        db.beginTransaction();
        ContentValues values = new ContentValues();
        for (int i = 0; i < tags.length; i++) {
            values.put(TagColumns.TAG_NAME, tags[i]);
            values.put(TagColumns.TAG_ACE_MODE, tagAceModes[i]);
            values.put(TagColumns.TAG_COLOR, tagColors[i]);
            db.insert(Tables.TAG, null, values);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Tables.ITEM);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.TAG);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.ITEM_SEARCH);
        db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.ITEM_SEARCH_BU);
        db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.ITEM_SEARCH_BD);
        db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.ITEM_SEARCH_AU);
        db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.ITEM_SEARCH_AI);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}