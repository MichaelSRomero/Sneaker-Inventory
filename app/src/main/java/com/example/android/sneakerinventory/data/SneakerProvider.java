package com.example.android.sneakerinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import static com.example.android.sneakerinventory.data.SneakerContract.CONTENT_AUTHORITY;
import static com.example.android.sneakerinventory.data.SneakerContract.PATH_SNEAKERS;
import static com.example.android.sneakerinventory.data.SneakerContract.SneakerEntry;

/**
 * Created by Mrome on 2/20/2018.
 */

public class SneakerProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = SneakerProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the sneakers table */
    private static final int SNEAKERS = 100;

    /** URI matcher code for the content URI for a single sneaker in the sneaker table */
    private static final int SNEAKER_ID = 101;

    /* Database helper object */
    public SneakerDBHelper mDbHelper;

    public static void decrementQuantity() {

    }

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // The content URI of the form "content://com.example.android.sneakerinventory/sneakers"
        // will map to the integer code {@link #SNEAKERS}. This URI is use to provide access to
        // MULTIPLE rows of the sneakers table
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_SNEAKERS, SNEAKERS);

        // The content URI of the form "content://com.example.android.sneakerinventory/sneakers/#"
        // will map to the integer code {@link #SNEAKER_ID}. This URI is use to provide access to
        // ONE single rows of the sneakers table
        sUriMatcher.addURI(CONTENT_AUTHORITY,
                PATH_SNEAKERS + "/#", SNEAKER_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new SneakerDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // Holds the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case SNEAKERS:
                cursor = database.query(SneakerEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case SNEAKER_ID:
                // For the SNEAKER_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.sneakerinventory/sneakers/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = SneakerEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(SneakerEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // If the data at this URI changes, then we update the Cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SNEAKERS:
                return SneakerEntry.CONTENT_LIST_TYPE;
            case SNEAKER_ID:
                return SneakerEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SNEAKERS:
                return insertSneaker(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Helper method for "insert()"
     *
     * @param uri               the Content URI that is being inserted
     * @param values            of the Content URI (name, price, quantity, supplier_email)
     * @return                  URI with appended ID for a new row
     */
    private Uri insertSneaker(Uri uri, ContentValues values) {
        // Sanity Check for "name"
        String name = values.getAsString(SneakerEntry.COLUMN_SNEAKER_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Entry requires a name");
        }

        // Sanity Check for "price" (Must be equal to or greater than $0)
        Integer price = values.getAsInteger(SneakerEntry.COLUMN_SNEAKER_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Entry requires a valid price");
        }

        // Sanity Check for "quantity" (Must be equal to or greater than 0)
        Integer quantity = values.getAsInteger(SneakerEntry.COLUMN_SNEAKER_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Entry requires a valid amount");
        }

        // Sanity Check for "supplier_email"
        String supplierEmail = values.getAsString(SneakerEntry.COLUMN_SUPPLIER_EMAIL);
        if (supplierEmail == null) {
            throw new IllegalArgumentException("Entry requires an email");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(SneakerEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the sneaker content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SNEAKERS:
                rowsDeleted = database.delete(SneakerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SNEAKER_ID:
                selection = SneakerEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(SneakerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SNEAKERS:
                return updateSneaker(uri, values, selection, selectionArgs);
            case SNEAKER_ID:
                selection = SneakerEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateSneaker(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Helper method for "update()"
     *
     * @param uri                   the Content URI that is being updated
     * @param values                of the Content URI (name, price, quantity, supplier_email)
     * @param selection             Columns to update
     * @param selectionArgs         "where" clause to use on Columns
     * @return                      number of rows updated as an int
     */
    private int updateSneaker(Uri uri, ContentValues values, String selection,
                              String[] selectionArgs) {
        // Check if the passed in "values" contains a "name"
        // Then do a Sanity Check
        if (values.containsKey(SneakerEntry.COLUMN_SNEAKER_NAME)) {
            String name = values.getAsString(SneakerEntry.COLUMN_SNEAKER_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Entry requires a name");
            }
        }

        // Check if the passed in "values" contains a "price"
        // Then do a Sanity Check where "price" cannot be less than 0
        if (values.containsKey(SneakerEntry.COLUMN_SNEAKER_PRICE)) {
            Integer price = values.getAsInteger(SneakerEntry.COLUMN_SNEAKER_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Entry requires a valid price");
            }
        }

        // Check if the passed in "values" contains a "quantity"
        // Then do a Sanity Check where "quantity" cannot be less than 0
        if (values.containsKey(SneakerEntry.COLUMN_SNEAKER_QUANTITY)) {
            Integer quantity = values.getAsInteger(SneakerEntry.COLUMN_SNEAKER_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Entry requires a valid amount");
            }
        }

        // Check if the passed in "values" contains a "supplier_email"
        // Then do a Sanity Check
        if (values.containsKey(SneakerEntry.COLUMN_SUPPLIER_EMAIL)) {
            String supplierEmail = values.getAsString(SneakerEntry.COLUMN_SUPPLIER_EMAIL);
            if (supplierEmail == null) {
                throw new IllegalArgumentException("Entry requires an Email");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(SneakerEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }
}
