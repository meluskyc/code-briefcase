package org.meluskyc.codebriefcase.adapter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.meluskyc.codebriefcase.R;
import org.meluskyc.codebriefcase.activity.AddEditActivity;
import org.meluskyc.codebriefcase.activity.MainActivity;
import org.meluskyc.codebriefcase.database.CodeBriefcaseContract.Item;
import org.meluskyc.codebriefcase.database.CodeBriefcaseContract.Tag;

public class ItemsCursorAdapter extends RecyclerView.Adapter<ItemsCursorAdapter.ViewHolder> {

    private Cursor cursor;
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tag_primary;
        public TextView date_updated;
        public TextView description;
        public TextView tag_secondary;
        public ImageView starred;


        public ViewHolder(View v) {
            super(v);
            tag_primary = (TextView) v.findViewById(R.id.main_text_tag_primary);
            date_updated = (TextView) v.findViewById(R.id.main_text_date_updated);
            description = (TextView) v.findViewById(R.id.main_text_description);
            tag_secondary = (TextView) v.findViewById(R.id.main_text_tag_secondary);
            starred = (ImageView) v.findViewById(R.id.main_image_starred);
        }
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_main, parent, false );

        final ViewHolder viewHolder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddEditActivity.class);
                int position = viewHolder.getAdapterPosition();
                cursor.moveToPosition(position);
                intent.putExtra(MainActivity.EXTRA_ITEM_ID,
                        cursor.getLong(cursor.getColumnIndex("_id")));
                context.startActivity(intent);
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int ix_id = cursor.getColumnIndex(Item.ITEM_ID);
        final int ix_tag_primary = cursor.getColumnIndex(Item.ITEM_TAG_PRIMARY);
        final int ix_date_updated = cursor.getColumnIndex(Item.ITEM_DATE_UPDATED);
        final int ix_description = cursor.getColumnIndex(Item.ITEM_DESCRIPTION);
        final int ix_tag_secondary = cursor.getColumnIndex(Item.ITEM_TAG_SECONDARY);
        final int ix_starred = cursor.getColumnIndex(Item.ITEM_STARRED);
        final int ix_tag_color = cursor.getColumnIndex(Tag.TAG_COLOR);

        cursor.moveToPosition(position);

        holder.tag_primary.setText(cursor.getString(ix_tag_primary));
        holder.description.setText(cursor.getString(ix_description));
        holder.tag_secondary.setText(cursor.getString(ix_tag_secondary));
        holder.date_updated.setText(DateUtils.getRelativeTimeSpanString(cursor
                        .getLong(ix_date_updated), System.currentTimeMillis(),
                DateUtils.FORMAT_ABBREV_RELATIVE));
        holder.tag_primary.setBackgroundColor(Color.parseColor(cursor.getString(ix_tag_color)));
        switch (cursor.getInt(ix_starred)) {
            case 0:
                holder.starred.setImageResource(R.drawable.ic_star_outline_24dp);
                holder.starred.setTag(0);
                break;
            case 1:
                holder.starred.setImageResource(R.drawable.ic_star_24dp);
                holder.starred.setTag(1);
                break;
            default:
                holder.starred.setImageResource(R.drawable.ic_star_outline_24dp);
                holder.starred.setTag(0);
                break;
        }

        final long id = cursor.getLong(ix_id);
        holder.starred.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleStarred(id, (ImageView) v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    @Override
    public long getItemId(int position) {
        if (cursor != null) {
            if (cursor.moveToPosition(position)) {
                return cursor.getLong(cursor.getColumnIndex(Item.ITEM_ID));
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    /**
     * Star or unstar an item
     * @param itemId the item's ID
     * @param imageView the star icon of the item clicked
     */
    private void toggleStarred(long itemId, ImageView imageView) {
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        int tagValue = (int) imageView.getTag();
        switch (tagValue) {
            case 0: {
                tagValue = 1;
                imageView.setImageResource(R.drawable.ic_star_24dp);
                break;
            } default: {
                tagValue = 0;
                imageView.setImageResource(R.drawable.ic_star_outline_24dp);
                break;
            }
        }
        imageView.setTag(tagValue);
        values.put(Item.ITEM_STARRED, tagValue);

        try {
            cr.update(Item.buildItemUri(itemId), values, null, null);
        } catch (SQLException e) {
            Toast.makeText(context, "Error updating record.", Toast.LENGTH_LONG).show();
        }
    }

}
