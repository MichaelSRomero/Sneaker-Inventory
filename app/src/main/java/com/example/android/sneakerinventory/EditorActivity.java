package com.example.android.sneakerinventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.sneakerinventory.utils.ImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.example.android.sneakerinventory.data.SneakerContract.SneakerEntry;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_LOADER = 0;

    /* Content URI for the existing sneaker (null if it's a new sneaker) */
    private Uri mCurrentSneakerUri;

    /* EditText field to enter the sneaker's name */
    private EditText mNameEditText;

    /* EditText field to enter the sneaker's price */
    private EditText mPriceEditText;

    /* EditText field to enter the sneaker's quantity */
    private EditText mQuantityEditText;

    /* EditText field to enter the Supplier */
    private EditText mSupplierEditText;

    private ImageView mProductImage;

    private Button mIncrementButton;

    private Button mDecrementButton;

    private Button mSendEmailButton;

    private ImageView mAddImageButton;

    /* Boolean flag that keeps track if an image has been uploaded from user's gallery (true) or not (false) */
    private Boolean mInsertedImage = false;

    /* Constant variable for the gallery intent */
    private int PICK_IMAGE_REQUEST = 1;

    /**
     * Boolean flag that keeps track to whether the sneaker has been edited (true) or not (false)
     */
    private boolean mEntryHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mEntryHasChanged boolean to true
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mEntryHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new entry or editing an existing one
        Intent intent = getIntent();
        mCurrentSneakerUri = intent.getData();

        // If the intent DOES NOT contain a sneaker content URI, then we know that we are
        // creating a new entry
        if (mCurrentSneakerUri == null) {
            // This is a new entry, so change the app bar to say "Add a Entry"
            setTitle(R.string.editor_activity_title_new_entry);

            // Invalidate the options menu, so the "Delete" menu option can be hidden
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing sneaker, so change app bar to say "Edit Entry"
            setTitle(getString(R.string.editor_activity_title_edit_entry));
        }

        mIncrementButton = findViewById(R.id.button_increment);
        mIncrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addOneToQuantity();
            }
        });

        mDecrementButton = findViewById(R.id.button_decrement);
        mDecrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subtractOneToQuantity();
            }
        });

        mSendEmailButton = findViewById(R.id.button_order);
        mSendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Send Email
                sendEmailIntent();
            }
        });

        mAddImageButton = findViewById(R.id.button_camera);
        mAddImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        mNameEditText = findViewById(R.id.edit_sneaker_name);
        mPriceEditText = findViewById(R.id.edit_sneaker_price);
        mQuantityEditText = findViewById(R.id.edit_sneaker_quantity);
        mSupplierEditText = findViewById(R.id.edit_sneaker_email);
        mProductImage = findViewById(R.id.editor_image);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);

        getLoaderManager().initLoader(EXISTING_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (mCurrentSneakerUri == null) {
            return null;
        }

        String[] projection = {
                SneakerEntry._ID,
                SneakerEntry.COLUMN_SNEAKER_NAME,
                SneakerEntry.COLUMN_SNEAKER_PRICE,
                SneakerEntry.COLUMN_SNEAKER_QUANTITY,
                SneakerEntry.COLUMN_SUPPLIER_EMAIL,
                SneakerEntry.COLUMN_IMAGE
        };

        return new CursorLoader(this,
                mCurrentSneakerUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of sneaker attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(SneakerEntry.COLUMN_SNEAKER_NAME);
            int priceColumnIndex = cursor.getColumnIndex(SneakerEntry.COLUMN_SNEAKER_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(SneakerEntry.COLUMN_SNEAKER_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(SneakerEntry.COLUMN_SUPPLIER_EMAIL);
            int imageColumnIndex = cursor.getColumnIndex(SneakerEntry.COLUMN_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            byte[] image = cursor.getBlob(imageColumnIndex);

            // Display views
            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mSupplierEditText.setText(supplier);

            // Convert "Blob" to Bitmap and display on view
            Bitmap bitmapImage = BitmapFactory.decodeByteArray(image, 0, image.length);
            mProductImage.setImageBitmap(bitmapImage);
            // Prevents the current Image stored in database from being set to the default image
            mInsertedImage = true;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierEditText.setText("");

    }

    private boolean saveEntry() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();

        // Check if this is supposed to be a new entry
        // and check if all the fields in the editor are blank
        if (mCurrentSneakerUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString)
                && TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(supplierString)) {
            // Since no fields were modified, we can return early without creating a new entry
            // No need to create ContentValues and no need to do any ContentProvider operations
            return true;
        }

        // If input fields "name" , "price" or "E-mail Address" are empty,
        // then we stay in the {@link EditorActivity} and display a Toast message
        if (nameString.equals("") || priceString.equals("") || supplierString.equals("")) {
            Toast.makeText(this, R.string.editor_empty_fields_error, Toast.LENGTH_SHORT).show();
            // return "false" to remain inside the {@link EditorActivity}
            return false;
        } else {
            // Otherwise, we continue with inserting or updating the URI
            ContentValues values = new ContentValues();
            values.put(SneakerEntry.COLUMN_SNEAKER_NAME, nameString);
            values.put(SneakerEntry.COLUMN_SNEAKER_PRICE, priceString);
            values.put(SneakerEntry.COLUMN_SUPPLIER_EMAIL, supplierString);

            // Store image pulled from gallery
            if (mInsertedImage) {
                //Read input fields
                Drawable getImage = mProductImage.getDrawable();
                //Convert to bitmap
                BitmapDrawable bitmapDrawable = ((BitmapDrawable) getImage);
                Bitmap bitmap = bitmapDrawable.getBitmap();
                //Convert to byte to store
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                byte[] imageByte = bos.toByteArray();
                values.put(SneakerEntry.COLUMN_IMAGE, imageByte);
            } else {
                // Otherwise, we set a default Image to be stored
                byte[] defaultImage = ImageUtils.convertToByte(R.drawable.default_sneaker_image, getBaseContext());
                values.put(SneakerEntry.COLUMN_IMAGE, defaultImage);
            }

            // If the quantity is not provided by the user, don't try to parse the string into an
            // integer value. Use 0 by default
            int quantity = 0;
            if (!TextUtils.isEmpty(quantityString)) {
                quantity = Integer.parseInt(quantityString);
            }

            values.put(SneakerEntry.COLUMN_SNEAKER_QUANTITY, quantity);

            // Determine if this is a new or existing entry by checking if mCurrentSneakerUri is null or not
            if (mCurrentSneakerUri == null) {
                // This is a NEW entry, so insert a new entry into the provider,
                // returning the content URI for the new entry
                Uri newUri = getContentResolver().insert(SneakerEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion
                    Toast.makeText(this, getString(R.string.editor_insert_entry_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast
                    Toast.makeText(this, getString(R.string.editor_insert_entry_successful),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                // Otherwise this is an EXISTING entry, so update the entry with content URI: mCurrentSneakerUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because mCurrentSneakerUri will already identify the correct row in the database that
                // we want to modify
                int rowsAffected = getContentResolver().update(mCurrentSneakerUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update
                    Toast.makeText(this, getString(R.string.editor_update_entry_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast
                    Toast.makeText(this, getString(R.string.editor_update_entry_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
            // return "true" to successfully exit from {@link EditorActivity} and display data
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible)
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new entry, hide the "Delete" menu item
        if (mCurrentSneakerUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                if (saveEntry()) {
                    // Exit activity
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the entry hasn't changed, continue with navigating up to parent activity
                // which is the (@link CatalogActivity}
                if (mEntryHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the entry.
                deleteEntry();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the entry.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteEntry() {
        // Only perform the delete if this is an existing entry
        if (mCurrentSneakerUri != null) {
            // Call the ContentResolver to delete the entry at the given content URI
            // Pass in null for the selection and selection args because the mCurrentSneakerUri
            // content URI already identifies the entry that we want
            int rowsDeleted = getContentResolver().delete(mCurrentSneakerUri, null, null);

            // Show a toast message depending on whether or not the delete was successful
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete
                Toast.makeText(this, getString(R.string.editor_delete_sneaker_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast
                Toast.makeText(this, getString(R.string.editor_delete_sneaker_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    @Override
    public void onBackPressed() {
        // If the entry hasn't changed, continue with handling back button press
        if (!mEntryHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user
        // Create a click listener to handle the user confirming that changes should be discarded
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard button, close the current activity
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the entry
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Helper method that subtracts one to the quantity
     */
    private void subtractOneToQuantity() {
        String previousValueString = mQuantityEditText.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty() || previousValueString.equals("0")) {
            return;
        } else {
            previousValue = Integer.parseInt(previousValueString);
            mQuantityEditText.setText(String.valueOf(previousValue - 1));
        }
    }

    /**
     * Helper method that adds one to the quantity
     */
    private void addOneToQuantity() {
        String previousValueString = mQuantityEditText.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty()) {
            previousValue = 0;
        } else {
            previousValue = Integer.parseInt(previousValueString);
        }
        mQuantityEditText.setText(String.valueOf(previousValue + 1));
    }

    //** Helper method to send an Email to the supplier for a restock */
    private void sendEmailIntent() {
        String[] emailString = new String[]{mSupplierEditText.getText().toString().trim()};
        String quantityString = mQuantityEditText.getText().toString().trim();
        String nameString = mNameEditText.getText().toString().trim();
        String orderShipment = "I would like to request a shipment of " + quantityString +
                " for the item:\n" + nameString;

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, emailString);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_order_subject));
        intent.putExtra(Intent.EXTRA_TEXT, orderShipment);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

                mProductImage = findViewById(R.id.editor_image);
                mProductImage.setImageBitmap(bitmap);
                mInsertedImage = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
