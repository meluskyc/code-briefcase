package org.meluskyc.codebriefcase.database;

import android.content.UriMatcher;
import android.net.Uri;
import android.util.SparseArray;

/**
 * Provides methods to match a {@link android.net.Uri} to a {@link CodeBriefcaseUriEnum}.
 * <p />
 * All methods are thread safe, except {@link #buildUriMatcher()} and {@link #buildEnumsMap()},
 * which is why they are called only from the constructor.
 *
 * based on Google I/O 2015 app at https://git.io/vKYuK
 */
public class CodeBriefcaseProviderUriMatcher {
    /**
     * All methods on a {@link UriMatcher} are thread safe, except {@code addURI}.
     */
    private UriMatcher mUriMatcher;

    private SparseArray<CodeBriefcaseUriEnum> mEnumsMap = new SparseArray<>();

    /**
     * This constructor needs to be called from a thread-safe method as it isn't thread-safe itself.
     */
    public CodeBriefcaseProviderUriMatcher(){
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        buildUriMatcher();
    }

    private void buildUriMatcher() {
        final String authority = CodeBriefcaseContract.CONTENT_AUTHORITY;

        CodeBriefcaseUriEnum[] uris = CodeBriefcaseUriEnum.values();
        for (int i = 0; i < uris.length; i++) {
            mUriMatcher.addURI(authority, uris[i].path, uris[i].code);
        }

        buildEnumsMap();
    }

    private void buildEnumsMap() {
        CodeBriefcaseUriEnum[] uris = CodeBriefcaseUriEnum.values();
        for (int i = 0; i < uris.length; i++) {
            mEnumsMap.put(uris[i].code, uris[i]);
        }
    }

    /**
     * Matches a {@code uri} to a {@link CodeBriefcaseUriEnum}.
     *
     * @return the {@link CodeBriefcaseUriEnum}, or throws new UnsupportedOperationException if no match.
     */
    public CodeBriefcaseUriEnum matchUri(Uri uri){
        final int code = mUriMatcher.match(uri);
        try {
            return matchCode(code);
        } catch (UnsupportedOperationException e){
            throw new UnsupportedOperationException("Unknown uri " + uri);
        }
    }

    /**
     * Matches a {@code code} to a {@link CodeBriefcaseUriEnum}.
     *
     * @return the {@link CodeBriefcaseUriEnum}, or throws new UnsupportedOperationException if no match.
     */
    public CodeBriefcaseUriEnum matchCode(int code){
        CodeBriefcaseUriEnum uriEnum = mEnumsMap.get(code);
        if (uriEnum != null){
            return uriEnum;
        } else {
            throw new UnsupportedOperationException("Unknown uri with code " + code);
        }
    }
}
