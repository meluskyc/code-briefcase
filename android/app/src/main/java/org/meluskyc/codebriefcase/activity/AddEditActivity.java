package org.meluskyc.codebriefcase.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.meluskyc.codebriefcase.R;
import org.meluskyc.codebriefcase.database.AppContentProvider;
import org.meluskyc.codebriefcase.database.AppDbHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AddEditActivity extends BaseActivity {

    private Long rowid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);
        Intent intent = getIntent();
        if (intent.hasExtra("rowid")) {
            rowid = getIntent().getLongExtra("rowid", 0);
            ContentResolver cr = getContentResolver();

            Cursor c = cr.query(AppContentProvider.ITEM_URI.buildUpon()
                            .appendPath(Long.toString(rowid)).build(),
                    new String[]{AppDbHelper.ITEM_DESCRIPTION, AppDbHelper.ITEM_TAG_PRIMARY,
                            AppDbHelper.ITEM_TAG_SECONDARY, AppDbHelper.ITEM_CONTENT}, null, null, null);

            if (!c.moveToFirst()) {
                this.setTitle(getString(R.string.add_a_snippet));
                Toast.makeText(this, getString(R.string.unable_to_retrieve), Toast.LENGTH_LONG).show();
                rowid = null;
            }
            else {
                this.setTitle(getString(R.string.edit_a_snippet));

                ((EditText) findViewById(R.id.addedit_edit_description)).setText(c.getString(0));

                // set the spinner's selection
                Spinner s = (Spinner) findViewById(R.id.addedit_edit_tag_primary);
                for (int i = 0; i < s.getCount(); i++) {
                    if (s.getItemAtPosition(i).toString().equals(c.getString(1))) {
                        s.setSelection(i);
                        break;
                    }
                }

                ((EditText) findViewById(R.id.addedit_edit_tag_secondary)).setText(c.getString(2));
                ((EditText) findViewById(R.id.addedit_edit_content)).setText(c.getString(3));
            }
            c.close();
        }
        else if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            this.setTitle(getString(R.string.add_a_snippet));
            ((EditText) findViewById(R.id.addedit_edit_content))
                    .setText(intent.getStringExtra(Intent.EXTRA_TEXT));
        }
        else {
            this.setTitle(getString(R.string.add_a_snippet));
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (rowid == null) {
            getMenuInflater().inflate(R.menu.menu_add, menu);
        }
        else {
            getMenuInflater().inflate(R.menu.menu_edit, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addedit_menu_done:
                addEditItem();
                return true;
            case R.id.edit_menu_delete:
                deleteItem();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteItem() {
        if (rowid != null) {
            ConfirmDeleteDialog confirmDialog = new ConfirmDeleteDialog();
            Bundle args = new Bundle();
            args.putLong("rowid", rowid);
            confirmDialog.setArguments(args);
            confirmDialog.show(getFragmentManager(), "deletionConfirmation");
        }
        else {
            finish();
        }
    }

    private void addEditItem() {
        ContentResolver cr = getContentResolver();

        ContentValues values = new ContentValues();

        // set a description and tag if none were entered
        String description = ((EditText) findViewById(R.id.addedit_edit_description)).getText().toString();
        String tag = ((Spinner) findViewById(R.id.addedit_edit_tag_primary)).getSelectedItem().toString();
        description = (description.equals("")) ?
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) : description;
        tag = (tag.equals("Tag")) ? "Text" : tag;

        values.put(AppDbHelper.ITEM_DESCRIPTION, description);
        values.put(AppDbHelper.ITEM_CONTENT, ((EditText)
                findViewById(R.id.addedit_edit_content)).getText().toString());
        values.put(AppDbHelper.ITEM_TAG_PRIMARY, tag);
        values.put(AppDbHelper.ITEM_TAG_SECONDARY, ((EditText)
                findViewById(R.id.addedit_edit_tag_secondary)).getText().toString());
        values.put(AppDbHelper.ITEM_DATE_UPDATED, System.currentTimeMillis());

        try {
            if (rowid == null) {
                values.put(AppDbHelper.ITEM_DATE_CREATED, System.currentTimeMillis());
                cr.insert(AppContentProvider.ITEM_URI, values);
                finish();
            } else {
                cr.update(AppContentProvider.ITEM_URI.buildUpon()
                        .appendPath(Long.toString(rowid)).build(), values, null, null);
                Toast.makeText(this, getString(R.string.updated), Toast.LENGTH_LONG).show();
            }

        } catch (SQLException e) {
            Toast.makeText(this, getString(R.string.unable_to_update), Toast.LENGTH_LONG).show();
        }
    }

    public static class ConfirmDeleteDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(getString(R.string.delete_the_item))
                    .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ContentResolver cr = getActivity().getContentResolver();

                            try {
                                long rowid = getArguments().getLong("rowid");
                                cr.delete(AppContentProvider.ITEM_URI, "_id = " + rowid, null);
                                getActivity().finish();
                            } catch (SQLException e) {
                                Toast.makeText(getActivity(), getString(R.string.unable_to_delete), Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new
                            DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {}
                            });
            return builder.create();
        }
    }

}