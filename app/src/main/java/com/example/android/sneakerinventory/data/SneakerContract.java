package com.example.android.sneakerinventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Mrome on 2/17/2018.
 */

public class SneakerContract {

    /** Constructor set to private to avoid calling it */
    private SneakerContract() {}

    public static final String CONTENT_AUTHORITY = "com.example.android.sneakerinventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_SNEAKERS = "sneakers";

    public static class SneakerEntry implements BaseColumns {
        /** The content URI to access the pet data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SNEAKERS);

        /** The MIME type of the {@link #CONTENT_URI} for a list of sneakers. */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SNEAKERS;

        /** The MIME type of the {@link #CONTENT_URI} for a single sneaker. */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SNEAKERS;

        public static final String TABLE_NAME = "sneakers";

        public final static String _ID = BaseColumns._ID;
        public static final String COLUMN_SNEAKER_NAME = "name";
        public static final String COLUMN_SNEAKER_QUANTITY = "quantity";
        public static final String COLUMN_SNEAKER_PRICE = "price";
        public static final String COLUMN_SUPPLIER_EMAIL = "supplier_email";
        public static final String COLUMN_IMAGE = "image";
    }
}
