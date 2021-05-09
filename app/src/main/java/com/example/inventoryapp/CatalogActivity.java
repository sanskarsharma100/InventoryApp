package com.example.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.inventoryapp.Data.ProductContract.ProductEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Displays list of products that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    private static final int PRODUCT_LOADER = 0;
    ProductCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        //Floating button to open EditorActivity
        FloatingActionButton fb = (FloatingActionButton) findViewById(R.id.floating_button);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the product data
        ListView productListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of product data in the Cursor.
        // There is no product data yest (until the loader finishes) so pass in null for the Cursor
        mCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);

        // Setup item click listener
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Intent for going EditorActivity
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                // From the content URI that represents the specific product that was clicked on,
                // by appending "id"
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                // Set the URI on the data field of the intent
                intent.setData(currentProductUri);
                // Launch the EditorActivity to display the data for the current product
                startActivity(intent);
            }
        });
        // Kick off the loader
        getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    private void insertProduct() {

        Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.default_iphone)
                + '/' + getResources().getResourceTypeName(R.drawable.default_iphone) + '/' + getResources().getResourceEntryName(R.drawable.default_iphone) );

        // Create a ContentValues object where column names are the keys,
        // and example's product attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Apple iphone 11");
        values.put(ProductEntry.COLUMN_PRODUCT_CONDITION, ProductEntry.CONDITION_NEW);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, 5);
        values.put(ProductEntry.COLUMN_PRODUCT_PICTURE, String.valueOf(imageUri));
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, "Tanuj Mehta");
        values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL, "tanuj@apple.com");
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 85000);

        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.insert_dummy_node:
                insertProduct();
                ;
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.delete_all:
                showDeleteAllConfirmationBox();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        // Define a projection that specifies the columns from the table we care about
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_CONDITION,
                ProductEntry.COLUMN_PRODUCT_PICTURE
        };

        // This loader will execute the ContentProvider's query method ona background thread
        return new CursorLoader(this,
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        // Update {@link ProductCursorAdapter} with this new cursor containing updated product data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    /**
     * Method to delete all products in the database.
     */
    private void deleteAllProducts() {
        if (ProductEntry.CONTENT_URI != null) {
            int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);

            if (rowsDeleted == 0) {
                // Toast message if no entries found
                Toast.makeText(this, "No products to delete", Toast.LENGTH_SHORT).show();
            } else {
                // Toast message if all entries are deleted
                Toast.makeText(this, "All Products deleted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Confirmation box for confirming the deletion of all entries
     */
    private void showDeleteAllConfirmationBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete all items?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAllProducts();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}