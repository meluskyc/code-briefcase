package org.meluskyc.codebriefcase.database;

import android.net.Uri;

import org.meluskyc.codebriefcase.database.CodeBriefcaseDatabase.Tables;

/**
 * Contract class for interacting with {@link CodeBriefcaseProvider}.
 *
 * based on Google I/O 2015 app at https://git.io/vKYuK
 */
public final class CodeBriefcaseContract {

    private CodeBriefcaseContract() {}

    public static final String CONTENT_AUTHORITY = "org.meluskyc.codebriefcase";
    public static final String CONTENT_TYPE_APP_BASE = "codebriefcase.";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String CONTENT_TYPE_BASE =
            "vnd.android.cursor.dir/vnd" + CONTENT_TYPE_APP_BASE;
    public static final String CONTENT_ITEM_TYPE_BASE =
            "vnd.android.cursor.item/vnd" + CONTENT_TYPE_APP_BASE;

    public static final String PATH_ITEM = "item";

    public static final String PATH_TAG = "tag";

    public static final String PATH_SEARCH = "search";

    public static final String PATH_STARRED = "starred";

    interface ItemColumns {
        String ITEM_ID = "_id";
        String ITEM_DESCRIPTION = "description";
        String ITEM_CONTENT = "content";
        String ITEM_DATE_CREATED = "date_created";
        String ITEM_DATE_UPDATED = "date_updated";
        String ITEM_TAG_PRIMARY = "tag_primary";
        String ITEM_TAG_SECONDARY = "tag_secondary";
        String ITEM_STARRED = "starred";
    }

    interface TagColumns {
        String TAG_ID = "_id";
        String TAG_NAME = "name";
        String TAG_ACE_MODE = "ace_mode";
        String TAG_COLOR = "color";
    }

    interface ItemSearchColumns {
        String ITEM_SEARCH_DOCID = "docid";
        String ITEM_SEARCH_DESCRIPTION = "description";
        String ITEM_SEARCH_DATE_UPDATED = "date_updated";
        String ITEM_SEARCH_TAG_PRIMARY = "tag_primary";
        String ITEM_SEARCH_TAG_SECONDARY = "tag_secondary";
        String ITEM_SEARCH_STARRED = "starred";
    }

    public interface Qualified {
        String ITEM_ID = Tables.ITEM + "." + ItemColumns.ITEM_ID;
        String TAG_ID = Tables.TAG + "." + TagColumns.TAG_ID;
    }

    public static class Item implements ItemColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ITEM).build();

        public static final String CONTENT_TYPE_ID = "item";

        public static final String ITEM_ORDER_BY_DATE_UPDATED = Item.ITEM_DATE_UPDATED + " DESC";

        public static Uri buildItemUri(long itemId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(itemId)).build();
        }

        public static long getItemId(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }

        public static Uri buildTagItemUri(long itemId) {
            return CONTENT_URI.buildUpon().appendPath("" + itemId).appendPath(PATH_TAG).build();
        }

        public static Uri buildTagDirUri() {
            return CONTENT_URI.buildUpon().appendPath(PATH_TAG).build();
        }

        public static Uri buildTagDirUri(long tagId) {
            if (tagId < 0) {
                return buildTagDirUri();
            }
            return CONTENT_URI.buildUpon().appendPath(PATH_TAG).appendPath("" + tagId).build();
        }

        public static Uri buildStarredUri() {
            return CONTENT_URI.buildUpon()
                    .appendPath(PATH_TAG).appendPath(PATH_STARRED).build();
        }

        public static Uri buildSearchUri(String query) {
            if (query == null) {
                query = "";
            }
            // convert "lorem ipsum dolor sit" to "lorem* ipsum* dolor* sit*"
            query = query.replaceAll(" +", " *") + "*";
            return CONTENT_URI.buildUpon()
                    .appendPath(PATH_TAG).appendPath(PATH_SEARCH).appendPath(query).build();
        }

        public static final String[] PROJECTION_ALL =
                {ITEM_ID, ITEM_DESCRIPTION, ITEM_CONTENT, ITEM_DATE_CREATED,
                        ITEM_DATE_UPDATED, ITEM_TAG_PRIMARY, ITEM_TAG_SECONDARY, ITEM_STARRED};
    }

    public static class Tag implements TagColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TAG).build();

        public static final String CONTENT_TYPE_ID = "tag";

        public static Uri buildTagUri(long tagId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(tagId)).build();
        }

        public static long getTagId(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }

        public static final String[] PROJECTION_ALL =
                {TAG_ID, TAG_NAME, TAG_ACE_MODE, TAG_COLOR};
    }

    public static class ItemSearch implements ItemSearchColumns {
        public static final String CONTENT_TYPE_ID = "item_search";

        public static final String[] PROJECTION_ALL =
                {ITEM_SEARCH_DOCID, ITEM_SEARCH_DESCRIPTION, ITEM_SEARCH_DATE_UPDATED,
                ITEM_SEARCH_TAG_PRIMARY, ITEM_SEARCH_TAG_SECONDARY, ITEM_SEARCH_STARRED};
    }

    public static String makeContentType(String id) {
        if (id != null) {
            return CONTENT_TYPE_BASE + id;
        } else {
            return null;
        }
    }

    public static String makeContentItemType(String id) {
        if (id != null) {
            return CONTENT_ITEM_TYPE_BASE + id;
        } else {
            return null;
        }
    }


}
