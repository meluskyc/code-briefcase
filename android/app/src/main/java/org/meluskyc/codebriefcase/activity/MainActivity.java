package org.meluskyc.codebriefcase.activity;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import org.meluskyc.codebriefcase.R;
import org.meluskyc.codebriefcase.adapter.ItemsCursorAdapter;
import org.meluskyc.codebriefcase.database.CodeBriefcaseContract.Item;
import org.meluskyc.codebriefcase.database.CodeBriefcaseContract.ItemSearch;
import org.meluskyc.codebriefcase.database.CodeBriefcaseContract.Qualified;
import org.meluskyc.codebriefcase.database.CodeBriefcaseContract.Tag;
import org.meluskyc.codebriefcase.utils.AppUtils;

import java.util.HashMap;


public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SearchView.OnQueryTextListener {

    /**
     * Open {@link AddEditActivity} with an item's ID
     */
    public static final String EXTRA_ITEM_ID = "itemId";

    /**
     * {@code SearchView} search text
     */
    public static final String EXTRA_SEARCH_QUERY = "searchQuery";
    private String searchQuery;

    /**
     * Loader IDs
     */
    private final int ITEMS_LOADER = 1;
    private final int TAGS_LOADER = 2;

    /**
     * No filter on {@code ListView}
     */
    private final int FILTER_NONE = -1;

    /**
     * {@code RecyclerView} filter starred
     */
    private final int FILTER_STARRED = -2;

    /**
     * {@code RecyclerView} filter. Initialize to none.
     */
    private long filter = FILTER_NONE;

    /**
     * Adapter for the {@code RecyclerView}
     */
    private ItemsCursorAdapter itemsAdapter;

    /**
     * store the tag ID for each tag filter
     */
    private HashMap<String, Long> filterIdsMap;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        super.onCreate(savedInstanceState);
        filterIdsMap = new HashMap<>();
        searchQuery = "";
        setContentView(R.layout.activity_main);

        setupFab();
        setupToolbar();
        setupDrawer();
        setupListView();

        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(ITEMS_LOADER, null, this);
        loaderManager.initLoader(TAGS_LOADER, null, this);

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(EXTRA_SEARCH_QUERY);
        }
    }

    /**
     * Sets up the floating action button.
     */
    private void setupFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.main_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddEditActivity.class));
            }
        });
    }

    /**
     * Sets up the toolbar.
     */
    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Sets up the list view.
     */
    private void setupListView() {
        RecyclerView recyclerview = (RecyclerView) findViewById(R.id.main_list_items);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));

        itemsAdapter = new ItemsCursorAdapter(this);
        itemsAdapter.setHasStableIds(true);
        //recyclerview.setHasFixedSize(true);

        recyclerview.setAdapter(itemsAdapter);

    }


    /**
     * Sets up the navigation drawer. The navigation drawer contains static links
     * to activities and a dynamically generated list of tag filters.
     */
    private void setupDrawer() {
        toolbar.setNavigationIcon(R.drawable.ic_drawer);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        navigationView = (NavigationView) findViewById(R.id.main_nav);
        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        menuItem.setChecked(true);
                        switch (menuItem.getItemId()) {
                            case R.id.nav_web:
                                menuItem.setChecked(false);
                                startActivity(new Intent(MainActivity.this, WebActivity.class));
                                break;
                            case R.id.nav_settings:
                                menuItem.setChecked(false);
                                startActivity(new Intent(MainActivity.this,
                                        SettingsActivity.class));
                                break;
                            case R.id.nav_about:
                                menuItem.setChecked(false);
                                startActivity(new Intent(MainActivity.this,
                                        AboutActivity.class));
                                break;
                            default:
                                if (filter == filterIdsMap.get(menuItem.toString())) {
                                    filter = FILTER_NONE;
                                    menuItem.setChecked(false);
                                } else {
                                    filter = filterIdsMap.get(menuItem.toString());
                                }

                                getLoaderManager().restartLoader(ITEMS_LOADER, null,
                                        MainActivity.this);
                                break;
                        }

                        drawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ITEMS_LOADER:
                if (TextUtils.isEmpty(searchQuery)) {
                    final Uri uri;
                    if (filter == FILTER_STARRED) {
                        uri = Item.buildStarredUri();
                    } else {
                        uri = Item.buildTagDirUri(filter);
                    }
                    return new CursorLoader(this, uri, new String[]{Qualified.ITEM_ID,
                        Item.ITEM_TAG_PRIMARY, Item.ITEM_DESCRIPTION,
                        Item.ITEM_DATE_UPDATED, Item.ITEM_TAG_SECONDARY,
                        Tag.TAG_COLOR, Item.ITEM_STARRED}, null, null,
                            Item.ITEM_DATE_UPDATED + " DESC");
                } else {
                    return new CursorLoader(this, Item.buildSearchUri(searchQuery),
                            new String[]{ItemSearch.ITEM_SEARCH_DOCID + " AS _id",
                            Item.ITEM_TAG_PRIMARY, Item.ITEM_DESCRIPTION,
                            Item.ITEM_DATE_UPDATED, Item.ITEM_TAG_SECONDARY,
                            Tag.TAG_COLOR, Item.ITEM_STARRED}, null, null,
                            Item.ITEM_DATE_UPDATED + " DESC");
                }
            case TAGS_LOADER:
                return new CursorLoader(this, Item.buildTagDirUri(),
                        new String[]{AppUtils.formatQueryDistinctParameter(Qualified.TAG_ID),
                                Item.ITEM_TAG_PRIMARY}, null, null,
                        Item.ITEM_TAG_PRIMARY + " COLLATE NOCASE ASC");
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case ITEMS_LOADER:
                itemsAdapter.setCursor(cursor);
                break;
            case TAGS_LOADER:
                filterIdsMap.clear();
                Menu menu = navigationView.getMenu();
                menu.removeGroup(R.id.nav_filters);
                SubMenu submenu = menu.addSubMenu(R.id.nav_filters, Menu.NONE, Menu.NONE,
                        getString(R.string.Filter));
                submenu
                        .add(getString(R.string.starred))
                        .setIcon(R.drawable.ic_drawer_star)
                        .setCheckable(true);
                filterIdsMap.put(getString(R.string.starred), (long) FILTER_STARRED);
                while (cursor.moveToNext()) {
                    MenuItem newItem = submenu
                            .add(cursor.getString(cursor.getColumnIndex(Item.ITEM_TAG_PRIMARY)))
                            .setCheckable(true);

                    filterIdsMap.put(newItem.toString(),
                            cursor.getLong(cursor.getColumnIndex(Tag.TAG_ID)));
                }
                submenu.setGroupCheckable(R.id.nav_filters, true, true);

                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case ITEMS_LOADER:
                itemsAdapter.setCursor(null);
                break;
            default:
                break;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.restartLoader(ITEMS_LOADER, null, this);
        loaderManager.restartLoader(TAGS_LOADER, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(EXTRA_SEARCH_QUERY, searchQuery);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.main_menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        searchQuery = TextUtils.isEmpty(newText) ? "" : newText;
        getLoaderManager().restartLoader(ITEMS_LOADER, null, this);
        itemsAdapter.notifyDataSetChanged();
        return true;
    }

}
