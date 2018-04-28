package com.example.android.sneakerinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.android.sneakerinventory.data.SneakerContract.SneakerEntry;

/**
 * Created by Mrome on 2/23/2018.
 */

public class SneakerCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link SneakerCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public SneakerCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the sneaker data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current sneaker can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // The current ID of where the cursor is clicked on
        final long id = cursor.getLong(cursor.getColumnIndexOrThrow(SneakerEntry._ID));
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = view.findViewById(R.id.tv_product_name);
        TextView priceTextView = view.findViewById(R.id.tv_product_price);
        final TextView quantityTextView = view.findViewById(R.id.tv_product_stock);
        ImageView productImageView = view.findViewById(R.id.iv_product_image);
        ImageView sellButton = view.findViewById(R.id.button_sell);

        // Retrieve the data from each individual column
        String name = cursor.getString(cursor.getColumnIndexOrThrow(SneakerEntry.COLUMN_SNEAKER_NAME));
        String price = cursor.getString(cursor.getColumnIndexOrThrow(SneakerEntry.COLUMN_SNEAKER_PRICE));
        final String quantity = cursor.getString(cursor.getColumnIndexOrThrow(SneakerEntry.COLUMN_SNEAKER_QUANTITY));
        byte[] image = cursor.getBlob(cursor.getColumnIndexOrThrow(SneakerEntry.COLUMN_IMAGE));

        // Display the data onto the views
        nameTextView.setText(name);
        priceTextView.setText("$" + price);
        quantityTextView.setText(quantity);

        //Update ImageView by converting to bitmap
        Bitmap sneakerBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
        productImageView.setImageBitmap(sneakerBitmap);

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantityNumber = Integer.parseInt(quantity);
                Uri currentUri = ContentUris.withAppendedId(SneakerEntry.CONTENT_URI, id);
                // Decrease Quantity by 1
                makeSell(context, quantityNumber, currentUri);
            }
        });
    }

    /**
     * Helper method to subtract one from the quantity inside the database
     *
     * @param context               of the view
     * @param quantity              of the current URI as an int
     * @param uriSneaker            the current URI
     */
    private void makeSell(Context context, int quantity, Uri uriSneaker) {
        // Display a toast message when attempting to update quantity below 0
        if (quantity == 0) {
            Toast.makeText(context, R.string.sale_error, Toast.LENGTH_SHORT).show();
        } else {
            int newQuantity = quantity - 1;

            //Create content value
            ContentValues values = new ContentValues();
            values.put(SneakerEntry.COLUMN_SNEAKER_QUANTITY, newQuantity);
            int rowsAffected = context.getContentResolver().update(uriSneaker, values, null, null);
        }
    }
}
