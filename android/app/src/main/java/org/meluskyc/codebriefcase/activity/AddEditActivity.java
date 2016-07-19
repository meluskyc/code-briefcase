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
import org.meluskyc.codebriefcase.database.CodeBriefcaseContract.Item;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Activity to add a new item or display an existing item for edit.
 * Launched via an {@link Intent} {@link AddEditActivity#INTENT_ITEMID}
 * with the ID of the item to edit.
 */
public class AddEditActivity extends BaseActivity {

    public static final String INTENT_ITEMID = "itemId";

    // the item's ID
    private Long itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);
        Intent intent = getIntent();
        if (intent.hasExtra(INTENT_ITEMID)) {
            itemId = getIntent().getLongExtra(INTENT_ITEMID, 0);
            ContentResolver cr = getContentResolver();

            Cursor c = cr.query(Item.buildItemUri(itemId),
                    new String[]{Item.ITEM_DESCRIPTION, Item.ITEM_TAG_PRIMARY,
                            Item.ITEM_TAG_SECONDARY, Item.ITEM_CONTENT}, null, null, null);

            if (!c.moveToFirst()) {
                this.setTitle(getString(R.string.add_a_snippet));
                Toast.makeText(this, getString(R.string.unable_to_retrieve), Toast.LENGTH_LONG).show();
                itemId = null;
            } else {
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
        } else if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            this.setTitle(getString(R.string.add_a_snippet));
            ((EditText) findViewById(R.id.addedit_edit_content))
                    .setText(intent.getStringExtra(Intent.EXTRA_TEXT));
        } else {
            this.setTitle(getString(R.string.add_a_snippet));
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (itemId == null) {
            getMenuInflater().inflate(R.menu.menu_add, menu);
        } else {
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

    /**
     * Delete the item.
     */
    private void deleteItem() {
        if (itemId != null) {
            ConfirmDeleteDialog confirmDialog = new ConfirmDeleteDialog();
            Bundle args = new Bundle();
            args.putLong(INTENT_ITEMID, itemId);
            confirmDialog.setArguments(args);
            confirmDialog.show(getFragmentManager(), "deletionConfirmation");
        } else {
            finish();
        }
    }

    /**
     * Edit the item if it exists. Otherwise add a new item.
     */
    private void addEditItem() {
        ContentResolver cr = getContentResolver();

        ContentValues values = new ContentValues();

        // set a description and tag if none were entered
        String description = ((EditText) findViewById(R.id.addedit_edit_description)).getText().toString();
        String tag = ((Spinner) findViewById(R.id.addedit_edit_tag_primary)).getSelectedItem().toString();
        description = (description.equals("")) ?
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) : description;
        tag = (tag.equals("Tag")) ? "Text" : tag;

        values.put(Item.ITEM_DESCRIPTION, description);
        values.put(Item.ITEM_CONTENT, ((EditText)
                findViewById(R.id.addedit_edit_content)).getText().toString());
        values.put(Item.ITEM_TAG_PRIMARY, tag);
        values.put(Item.ITEM_TAG_SECONDARY, ((EditText)
                findViewById(R.id.addedit_edit_tag_secondary)).getText().toString());
        values.put(Item.ITEM_DATE_UPDATED, System.currentTimeMillis());

        try {
            if (itemId == null) {
                values.put(Item.ITEM_DATE_CREATED, System.currentTimeMillis());
                cr.insert(Item.CONTENT_URI, values);
                finish();
            } else {
                cr.update(Item.buildItemUri(itemId), values, null, null);
                Toast.makeText(this, getString(R.string.updated), Toast.LENGTH_LONG).show();
            }

        } catch (SQLException e) {
            Toast.makeText(this, getString(R.string.unable_to_update), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Simple dialog to confirm a delete
     */
    public static class ConfirmDeleteDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(getString(R.string.delete_the_item))
                    .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ContentResolver cr = getActivity().getContentResolver();

                            try {
                                long itemId = getArguments().getLong(INTENT_ITEMID);
                                cr.delete(Item.buildItemUri(itemId), null, null);
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
