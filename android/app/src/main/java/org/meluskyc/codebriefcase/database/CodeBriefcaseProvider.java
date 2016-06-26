package org.meluskyc.codebriefcase.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.meluskyc.codebriefcase.database.CodeBriefcaseContract.Item;
import org.meluskyc.codebriefcase.database.CodeBriefcaseContract.Qualified;
import org.meluskyc.codebriefcase.database.CodeBriefcaseContract.Tag;
import org.meluskyc.codebriefcase.database.CodeBriefcaseDatabase.Tables;
import org.meluskyc.codebriefcase.utils.AppUtils;
import org.meluskyc.codebriefcase.utils.SelectionBuilder;


@SuppressWarnings("ConstantConditions")
public class CodeBriefcaseProvider extends ContentProvider {

    private CodeBriefcaseDatabase dbHelper;
    private CodeBriefcaseProviderUriMatcher uriMatcher;

    @Override
    public boolean onCreate() {
        dbHelper = CodeBriefcaseDatabase.getInstance(getContext());
        uriMatcher = new CodeBriefcaseProviderUriMatcher();
        return true;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        long id = -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        CodeBriefcaseUriEnum uriEnum = uriMatcher.matchUri(uri);
        if (uriEnum.table != null) {
            id = db.insertOrThrow(uriEnum.table, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
        }

        switch (uriEnum) {
            case ITEM:
            case ITEM_TAG_ID:
            case ITEM_TAG_STARRED:
            case ITEM_TAG_SEARCH:
            case ITEM_TAG:
            case ITEM_ID: {
                return CodeBriefcaseContract.Item.buildItemUri(id);
            }
            case TAG:
            case TAG_ID: {
                return CodeBriefcaseContract.Tag.buildTagUri(id);
            }
            default: {
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        SelectionBuilder builder = buildSelection(uri);

        boolean distinct = AppUtils.isQueryDistinct(uri);

        Cursor cursor = builder
                .where(selection, selectionArgs)
                .query(db, distinct, projection, sortOrder, null);

        Context context = getContext();
        if (null != context) {
            cursor.setNotificationUri(context.getContentResolver(), uri);
        }

        return cursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        final SelectionBuilder builder = buildSelection(uri);
        int count = builder.where(selection, selectionArgs).update(db, values);

        CodeBriefcaseUriEnum uriEnum = uriMatcher.matchUri(uri);

        switch (uriEnum) {
            case ITEM_ID: {
                getContext().getContentResolver().notifyChange(Item.CONTENT_URI, null);
            }
            case TAG_ID: {
                getContext().getContentResolver().notifyChange(Tag.CONTENT_URI, null);
            }
            default: {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }

        return count;
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        final SelectionBuilder builder = buildSelection(uri);

        int count = builder.where(selection, selectionArgs).delete(db);

         CodeBriefcaseUriEnum uriEnum = uriMatcher.matchUri(uri);

        switch (uriEnum) {
            case ITEM_ID: {
                getContext().getContentResolver().notifyChange(Item.CONTENT_URI, null);
            }
            case TAG_ID: {
                getContext().getContentResolver().notifyChange(Tag.CONTENT_URI, null);
            }
            default: {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }
        return count;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        CodeBriefcaseUriEnum uriEnum = uriMatcher.matchUri(uri);
        return uriEnum.contentType;
    }

    private SelectionBuilder buildSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        CodeBriefcaseUriEnum uriEnum = uriMatcher.matchUri(uri);
        switch (uriEnum) {
            case ITEM:
            case TAG:
                return builder.table(uriEnum.table);
            case ITEM_TAG: {
                return builder.table(Tables.ITEM_JOIN_TAG);
            }
            case ITEM_TAG_ID: {
                return builder.table(Tables.ITEM_JOIN_TAG)
                        .where(Qualified.TAG_ID + "=?", uri.getLastPathSegment());
            }
            case ITEM_TAG_SEARCH: {
                return builder.table(Tables.ITEM_SEARCH_JOIN_TAG)
                        .where(Tables.ITEM_SEARCH + " MATCH ?", uri.getLastPathSegment());
            }
            case ITEM_TAG_STARRED: {
                return builder.table(Tables.ITEM_JOIN_TAG)
                        .where(Item.ITEM_STARRED + "=1");
            }
            case ITEM_ID: {
                return builder.table(Tables.ITEM)
                        .where(Item.ITEM_ID + "=?", uri.getLastPathSegment());
            }
            case ITEM_ID_TAG: {
                return builder.table(Tables.ITEM_JOIN_TAG)
                        .where(Qualified.ITEM_ID + "=?", uri.getPathSegments().get(1));
            }
            case TAG_ID: {
                return builder.table(Tables.TAG)
                        .where(Tag.TAG_ID + "=?", uri.getLastPathSegment());
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri for " + uri);
            }
        }
    }
}
