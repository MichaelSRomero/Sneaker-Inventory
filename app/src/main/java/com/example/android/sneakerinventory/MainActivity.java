package com.example.android.sneakerinventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.sneakerinventory.data.SneakerContract.SneakerEntry;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private final int LOADER_ID = 0;

    private SneakerCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the sneaker data
        ListView sneakerListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        sneakerListView.setEmptyView(emptyView);

        mAdapter = new SneakerCursorAdapter(this, null);
        sneakerListView.setAdapter(mAdapter);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        sneakerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific sneaker that was clicked on,
                // by appeneding the "id" (passed as input to this method) onto the
                // {@link SneakerEntry#CONTENT_URI}
                // For example, the URI would be "content://com.example.android.sneakerinventory/sneakers/2"
                // if the pet with ID 2 was clicked on
                Uri currentUri = ContentUris.withAppendedId(SneakerEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentUri);

                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.dummy_data:
                insertDummyData();
                return true;
            case R.id.delete_all_entries:
                deleteAllEntries();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Helper method to insert a preset entry
     */
    public void insertDummyData() {

        ContentValues values = new ContentValues();
        values.put(SneakerEntry.COLUMN_SNEAKER_NAME, "Air Max 98 GMT");
        values.put(SneakerEntry.COLUMN_SNEAKER_PRICE, 160);
        values.put(SneakerEntry.COLUMN_SNEAKER_QUANTITY, 100);
        values.put(SneakerEntry.COLUMN_SUPPLIER_EMAIL, "media.relations@nike.com");

        values.put(SneakerEntry.COLUMN_IMAGE, convertToByte(R.drawable.airmax_98));

        Uri newUri = getContentResolver().insert(SneakerEntry.CONTENT_URI, values);
    }

    private byte[] convertToByte (int drawableResourceID) {
        //Convert to bitmap
        Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(),
                drawableResourceID);
        //Convert to byte to store
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] imageByte = bos.toByteArray();
        return imageByte;
    }

    /**
     * Helper method to delete all sneakers in the database
     */
    private void deleteAllEntries() {
        int rowsDeleted = getContentResolver().delete(SneakerEntry.CONTENT_URI, null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {SneakerEntry._ID,
                SneakerEntry.COLUMN_SNEAKER_NAME,
                SneakerEntry.COLUMN_SNEAKER_PRICE,
                SneakerEntry.COLUMN_SNEAKER_QUANTITY,
                SneakerEntry.COLUMN_IMAGE};
        return new CursorLoader(this,
                SneakerEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

}
