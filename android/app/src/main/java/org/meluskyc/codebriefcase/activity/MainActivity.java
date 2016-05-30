package org.meluskyc.codebriefcase.activity;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.meluskyc.codebriefcase.R;
import org.meluskyc.codebriefcase.database.AppContentProvider;
import org.meluskyc.codebriefcase.database.AppDbHelper;


public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SearchView.OnQueryTextListener {

    private SimpleCursorAdapter itemsAdapter;
    private ListView itemsList;
    private final int ITEMS_LOADER = 1;
    private String searchQuery = "";
    private String filter = "";
    private SearchView searchView;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private final int TAGS_LOADER = 2;
    private final int FILTER_GROUP = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupFab();
        setupToolbar();
        setupDrawer();
        setupItemsView();

        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(ITEMS_LOADER, null, this);
        loaderManager.initLoader(TAGS_LOADER, null, this);

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString("searchQuery");
        }
    }

    private void setupFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.main_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddEditActivity.class));
            }
        });
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupItemsView() {
        itemsAdapter = new SimpleCursorAdapter(this, R.layout.item_main, null,
                new String[]{AppDbHelper.ITEM_TAG_PRIMARY, AppDbHelper.ITEM_DESCRIPTION,
                        AppDbHelper.ITEM_DATE_UPDATED, AppDbHelper.ITEM_TAG_SECONDARY, AppDbHelper.TAG_COLOR,
                        AppDbHelper.ITEM_STARRED},
                new int[] {R.id.main_text_tag_primary, R.id.main_text_description, R.id.main_text_date_updated,
                        R.id.main_text_tag_secondary, SimpleCursorAdapter.NO_SELECTION, R.id.main_image_starred}, 0);

        itemsAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                switch (columnIndex) {
                    case 1:
                        view.setBackgroundColor(Color.parseColor(cursor.getString(cursor.getColumnIndex(AppDbHelper.TAG_COLOR))));
                        ((TextView) view).setText(cursor.getString(cursor.getColumnIndex(AppDbHelper.ITEM_TAG_PRIMARY)));
                        return true;
                    case 3:
                        long dateLong = cursor.getLong(cursor.getColumnIndex(AppDbHelper.ITEM_DATE_UPDATED));
                        ((TextView) view).setText(DateUtils.getRelativeTimeSpanString(dateLong,
                                System.currentTimeMillis(), DateUtils.FORMAT_ABBREV_RELATIVE));
                        return true;
                    case 6:
                        switch (cursor.getInt(cursor.getColumnIndex(AppDbHelper.ITEM_STARRED))) {
                            case 0:
                                ((ImageView) view).setImageResource(R.drawable.ic_star_outline_24dp);
                                view.setTag(0);
                                break;
                            case 1:
                                ((ImageView) view).setImageResource(R.drawable.ic_star_24dp);
                                view.setTag(1);
                                break;
                            default:
                                ((ImageView) view).setImageResource(R.drawable.ic_star_outline_24dp);
                                view.setTag(0);
                        }
                        final long rowid = cursor.getLong(cursor.getColumnIndex("_id"));

                        view.setOnClickListener(new View.OnClickListener() {
                            long _rowid = rowid;

                            public void onClick(View v) {
                                toggleImage(_rowid, (ImageView) v);
                            }
                        });
                        return true;
                }
                return false;
            }
        });

        itemsList = (ListView) findViewById(R.id.main_list_items);
        itemsList.setAdapter(itemsAdapter);
        itemsList.setTextFilterEnabled(true);

        itemsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(MainActivity.this,
                        AddEditActivity.class).putExtra("rowid", id));
            }
        });
    }



    private void setupDrawer() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                            case R.id.nav_add:
                                menuItem.setChecked(false);
                                startActivity(new Intent(MainActivity.this, AddEditActivity.class));
                                break;
                            case R.id.nav_settings:
                                menuItem.setChecked(false);
                                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                                break;
                            default:
                                if (filter.equals(menuItem.toString())) {
                                    filter = "";
                                    menuItem.setChecked(false);
                                } else {
                                    filter = menuItem.toString();
                                }

                                getLoaderManager().restartLoader(ITEMS_LOADER, null, MainActivity.this);
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
                    String where = "";
                    if (!TextUtils.isEmpty(filter)) {
                        where = filter.equals("Starred")
                                ? AppDbHelper.ITEM_STARRED + " = 1"
                                : AppDbHelper.ITEM_TAG_PRIMARY + " = '" + filter + "'";
                    }
                    return new CursorLoader(this, AppContentProvider.ITEM_JOIN_TAG_URI,
                            new String[]{AppDbHelper.ITEM_TABLE + "." + AppDbHelper.ITEM_ID,
                                    AppDbHelper.ITEM_TAG_PRIMARY, AppDbHelper.ITEM_DESCRIPTION,
                                    AppDbHelper.ITEM_DATE_UPDATED, AppDbHelper.ITEM_TAG_SECONDARY,
                                    AppDbHelper.TAG_COLOR, AppDbHelper.ITEM_STARRED}, where, null,
                            AppDbHelper.ITEM_DATE_UPDATED + " DESC");
                }
                else {
                    return new CursorLoader(this, AppContentProvider.ITEM_SEARCH_JOIN_TAG_URI,
                            new String[]{"docid as _id", AppDbHelper.ITEM_TAG_PRIMARY, AppDbHelper.ITEM_DESCRIPTION,
                                    AppDbHelper.ITEM_DATE_UPDATED, AppDbHelper.ITEM_TAG_SECONDARY, AppDbHelper.TAG_COLOR,
                                    AppDbHelper.ITEM_STARRED}, "item_search MATCH ?",
                            new String[]{searchQuery}, AppDbHelper.ITEM_DATE_UPDATED + " DESC");
                }
            case TAGS_LOADER:
                return new CursorLoader(this, AppContentProvider.ITEM_JOIN_TAG_URI,
                        new String[]{"DISTINCT " + AppDbHelper.TAG_TABLE + "." + AppDbHelper.TAG_ID,
                                AppDbHelper.ITEM_TAG_PRIMARY}, null, null,
                        AppDbHelper.ITEM_TAG_PRIMARY + " COLLATE NOCASE ASC");
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case ITEMS_LOADER:
                itemsAdapter.swapCursor(cursor);
                break;
            case TAGS_LOADER:
                Menu menu = navigationView.getMenu();
                menu.removeGroup(R.id.nav_filters);
                SubMenu submenu = menu.addSubMenu(R.id.nav_filters, Menu.NONE, Menu.NONE, "Filter");
                submenu.add("Starred").setIcon(R.drawable.ic_drawer_star).setCheckable(true);
                while (cursor.moveToNext()) {
                    submenu
                            .add(cursor.getString(cursor.getColumnIndex(AppDbHelper.ITEM_TAG_PRIMARY)))
                            .setCheckable(true);
                }
                submenu.setGroupCheckable(R.id.nav_filters, true, true);

                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case ITEMS_LOADER:
                itemsAdapter.swapCursor(null);
                break;
            case TAGS_LOADER:
                navigationView.getMenu().removeGroup(FILTER_GROUP);
                break;
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        getLoaderManager().restartLoader(ITEMS_LOADER, null, this);
        getLoaderManager().restartLoader(TAGS_LOADER, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString("searchQuery", searchQuery);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView =
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

    private void toggleImage(long rowid, ImageView imageView) {
        int newVal;
        if (imageView.getTag().equals(0)) {
            newVal = 1;
            imageView.setImageResource(R.drawable.ic_star_24dp);
        }
        else if (imageView.getTag().equals(1)) {
            newVal = 0;
            imageView.setImageResource(R.drawable.ic_star_outline_24dp);
        }
        else {
            newVal = 1;
            imageView.setImageResource(R.drawable.ic_star_24dp);
        }
        imageView.setTag(newVal);

        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(AppDbHelper.ITEM_STARRED, newVal);

        try {
            cr.update(AppContentProvider.ITEM_URI.buildUpon().appendPath(Long.toString(rowid)).build(), values, null, null);
        }
        catch (SQLException e) {
            Toast.makeText(this, "Error updating record.", Toast.LENGTH_LONG).show();
        }
    }

}
