package org.meluskyc.codebriefcase.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;


@SuppressWarnings("ConstantConditions")
public class CodeBriefcaseProvider extends ContentProvider {

    private CodeBriefcaseDatabase dbHelper;

    private static final String AUTHORITY = "org.meluskyc.codebriefcase";
    public static final String ITEM_PATH = "item";
    public static final String TAG_PATH = "tag";

    public static final Uri ITEM_URI = Uri.parse("content://" + AUTHORITY + "/" +
            ITEM_PATH);
    public static final Uri TAG_URI = Uri.parse("content://" + AUTHORITY + "/" +
            TAG_PATH);
    public static final Uri ITEM_JOIN_TAG_URI = Uri.parse("content://" + AUTHORITY + "/" +
            ITEM_PATH + "/" + TAG_PATH);
    public static final Uri ITEM_SEARCH_JOIN_TAG_URI = Uri.parse("content://" + AUTHORITY + "/" +
            ITEM_PATH + "/search/" + TAG_PATH);

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int ITEM = 1;
    private static final int ITEM_ID = 2;
    private static final int TAG = 3;
    private static final int TAG_ID = 4;
    private static final int ITEM_JOIN_TAG = 5;
    private static final int ITEM_ID_JOIN_TAG = 6;
    private static final int ITEM_SEARCH_JOIN_TAG = 7;
    static {
        uriMatcher.addURI(AUTHORITY, ITEM_PATH, ITEM);
        uriMatcher.addURI(AUTHORITY, ITEM_PATH + "/#", ITEM_ID);
        uriMatcher.addURI(AUTHORITY, TAG_PATH, TAG);
        uriMatcher.addURI(AUTHORITY, TAG_PATH + "/#", TAG_ID);
        uriMatcher.addURI(AUTHORITY, ITEM_PATH + "/" + TAG_PATH, ITEM_JOIN_TAG);
        uriMatcher.addURI(AUTHORITY, ITEM_PATH + "/search/" + TAG_PATH, ITEM_SEARCH_JOIN_TAG);
        uriMatcher.addURI(AUTHORITY, ITEM_PATH + "/#/" + TAG_PATH, ITEM_ID_JOIN_TAG);
    }

    @Override
    public boolean onCreate() {
        dbHelper = CodeBriefcaseDatabase.getInstance(getContext());
        return true;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        long id;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri newRow;

        switch (uriMatcher.match(uri)) {
            case ITEM:
                id = db.insert(CodeBriefcaseDatabase.ITEM_TABLE, null, values);
                newRow = Uri.parse(ITEM_PATH + "/" + id);
                break;
            case TAG:
                id = db.insert(CodeBriefcaseDatabase.TAG_TABLE, null, values);
                newRow = Uri.parse(TAG_PATH + "/" + id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return newRow;
    }


    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        Cursor cursor;

        switch (uriMatcher.match(uri)) {
            case ITEM:
                qb.setTables(CodeBriefcaseDatabase.ITEM_TABLE);
                break;
            case ITEM_ID:
                qb.setTables(CodeBriefcaseDatabase.ITEM_TABLE);
                qb.appendWhere(CodeBriefcaseDatabase.ITEM_ID + " = " + uri.getLastPathSegment());
                break;
            case ITEM_JOIN_TAG:
                qb.setTables(CodeBriefcaseDatabase.ITEM_TABLE + " INNER JOIN " + CodeBriefcaseDatabase.TAG_TABLE
                        + " ON " + CodeBriefcaseDatabase.ITEM_TABLE + "." + CodeBriefcaseDatabase.ITEM_TAG_PRIMARY + " = "
                        + CodeBriefcaseDatabase.TAG_TABLE + "." + CodeBriefcaseDatabase.TAG_NAME);
                break;
            case ITEM_SEARCH_JOIN_TAG:
                qb.setTables(CodeBriefcaseDatabase.ITEM_SEARCH_TABLE + " INNER JOIN " + CodeBriefcaseDatabase.TAG_TABLE
                        + " ON " + CodeBriefcaseDatabase.ITEM_SEARCH_TABLE + "." + CodeBriefcaseDatabase.ITEM_TAG_PRIMARY + " = "
                        + CodeBriefcaseDatabase.TAG_TABLE + "." + CodeBriefcaseDatabase.TAG_NAME);
                break;
            case ITEM_ID_JOIN_TAG:
                qb.setTables(CodeBriefcaseDatabase.ITEM_TABLE + " INNER JOIN " + CodeBriefcaseDatabase.TAG_TABLE
                        + " ON " + CodeBriefcaseDatabase.ITEM_TABLE + "." + CodeBriefcaseDatabase.ITEM_TAG_PRIMARY + " = "
                        + CodeBriefcaseDatabase.TAG_TABLE + "." + CodeBriefcaseDatabase.TAG_NAME);
                qb.appendWhere(CodeBriefcaseDatabase.ITEM_TABLE + "." + CodeBriefcaseDatabase.ITEM_ID + " = " + uri.getPathSegments().get(1));
                break;
            case TAG:
                qb.setTables(CodeBriefcaseDatabase.TAG_TABLE);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        cursor = qb.query(dbHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count;
        Uri notifyUri = uri;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)){
            case ITEM:
                count = db.update(CodeBriefcaseDatabase.ITEM_TABLE, values, selection, selectionArgs);
                break;
            case ITEM_ID:
                notifyUri = ITEM_URI;
                count = db.update(CodeBriefcaseDatabase.ITEM_TABLE, values,
                        appendIdToSelection(selection, uri.getLastPathSegment()),
                        selectionArgs);
                break;
            case TAG:
                count = db.update(CodeBriefcaseDatabase.TAG_TABLE, values, selection, selectionArgs);
                break;
            case TAG_ID:
                notifyUri = TAG_URI;
                count = db.update(CodeBriefcaseDatabase.TAG_TABLE, values,
                        appendIdToSelection(selection, uri.getLastPathSegment()),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(notifyUri, null);
        return count;
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int count;
        Uri notifyUri = uri;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)){
            case ITEM:
                count = db.delete(CodeBriefcaseDatabase.ITEM_TABLE, selection, selectionArgs);
                break;
            case ITEM_ID:
                notifyUri = ITEM_URI;
                count = db.delete(CodeBriefcaseDatabase.ITEM_TABLE,
                        appendIdToSelection(selection, uri.getLastPathSegment()),
                        selectionArgs);
            case TAG:
                count = db.delete(CodeBriefcaseDatabase.TAG_TABLE, selection, selectionArgs);
                break;
            case TAG_ID:
                notifyUri = TAG_URI;
                count = db.delete(CodeBriefcaseDatabase.TAG_TABLE,
                        appendIdToSelection(selection, uri.getLastPathSegment()),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(notifyUri, null);
        return count;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    private String appendIdToSelection(String selection, String sId) {
        int id = Integer.valueOf(sId);

        if (selection == null || selection.trim().equals(""))
            return CodeBriefcaseDatabase.ITEM_ID + " = " + id;
        else
            return selection + " AND " + CodeBriefcaseDatabase.ITEM_ID + " = " + id;
    }
}
