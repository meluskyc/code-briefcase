package org.meluskyc.codebriefcase.database;

/**
 * The list of {@code Uri}s recognised by the {@code ContentProvider} of the app.
 * <p />
 * It is important to order them in the order that follows {@link android.content.UriMatcher}
 * matching rules: wildcard {@code *} applies to one segment only and it processes matching per
 * segment in a tree manner over the list of {@code Uri} in the order they are added. The first
 * rule means that {@code sessions / *} would not match {@code sessions / id / room}. The second
 * rule is more subtle and means that if Uris are in the  order {@code * / room / counter} and
 * {@code sessions / room / time}, then {@code speaker / room / time} will not match anything,
 * because the {@code UriMatcher} will follow the path of the first  and will fail at the third
 * segment.
 *
 * based on Google I/O 2015 app at https://git.io/vKYuK
 */
public enum CodeBriefcaseUriEnum {
    ITEM(100, "item", CodeBriefcaseContract.Item.CONTENT_TYPE_ID, false,
            CodeBriefcaseDatabase.Tables.ITEM),
    ITEM_ID(101, "item/#", CodeBriefcaseContract.Item.CONTENT_TYPE_ID, true,
            CodeBriefcaseDatabase.Tables.ITEM),
    ITEM_ID_TAG(102, "item/#/tag", CodeBriefcaseContract.Item.CONTENT_TYPE_ID, true,
            CodeBriefcaseDatabase.Tables.ITEM),
    ITEM_TAG(103, "item/tag", CodeBriefcaseContract.Item.CONTENT_TYPE_ID, false,
            CodeBriefcaseDatabase.Tables.ITEM),
    ITEM_TAG_STARRED(104, "item/tag/starred", CodeBriefcaseContract.Item.CONTENT_TYPE_ID, false,
            CodeBriefcaseDatabase.Tables.ITEM),
    ITEM_TAG_SEARCH(105, "item/tag/search/*", CodeBriefcaseContract.Item.CONTENT_TYPE_ID, false,
            CodeBriefcaseDatabase.Tables.ITEM),
    ITEM_TAG_ID(106, "item/tag/*", CodeBriefcaseContract.Item.CONTENT_TYPE_ID, true,
            CodeBriefcaseDatabase.Tables.ITEM),
    TAG(200, "tag", CodeBriefcaseContract.Tag.CONTENT_TYPE_ID, false,
            CodeBriefcaseDatabase.Tables.TAG),
    TAG_ID(201, "tag/#", CodeBriefcaseContract.Tag.CONTENT_TYPE_ID, true,
            CodeBriefcaseDatabase.Tables.TAG);

    public int code;

    /**
     * The path to the {@link android.content.UriMatcher} will use to match. * may be used as a
     * wild card for any text, and # may be used as a wild card for numbers.
     */
    public String path;

    public String contentType;

    public String table;

    CodeBriefcaseUriEnum(int code, String path, String contentTypeId, boolean item, String table) {
        this.code = code;
        this.path = path;
        this.contentType = item ? CodeBriefcaseContract.makeContentItemType(contentTypeId)
                : CodeBriefcaseContract.makeContentType(contentTypeId);
        this.table = table;
    }
}
