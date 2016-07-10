package org.meluskyc.codebriefcase.server;

/**
 * URIs recognized by REST API
 */
public enum AppApiUriEnum {
    // all items
    ITEMS("/api/items"),

    // distinct tags on items
    ITEM_DISTINCT_TAGS("/api/items/tags"),

    // item by ID
    ITEM_BY_ID("/api/items/:_id"),

    // all tags
    TAGS("/api/tags");

    public String path;

    AppApiUriEnum(String path) {
        this.path = path;
    }
}