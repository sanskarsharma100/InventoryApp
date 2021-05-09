package com.example.inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
//import androidx.loader.app.LoaderManager;
import android.app.LoaderManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventoryapp.Data.ProductContract;

/**
 * Allows user to create a new product or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the product data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /**
     * URI of product image
     */
    private Uri mImageUri;

    /**
     * Current quantity of product
     */
    private int mQuantity;

    /**
     * Content URI for the existing product (null if it's a new product)
     */
    private Uri mCurrentProductUri;

    /**
     * EditText field to enter the product's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the product's condition
     */
    private Spinner mConditionSpinner;

    /**
     * ImageView field to enter the product's photo
     */
    private ImageView mPhoto;

    /**
     * EditText field to enter the product's price
     */
    private EditText mPrice;

    /**
     * EditText field to enter the supplier's namee
     */
    private EditText mSupplierName;

    /**
     * EditText field to enter the supplier's e-mail
     */
    private EditText mSupplierEmail;

    /**
     * EditText field to enter the product's quantity
     */
    private EditText mQuantityEditText;

    /**
     * EditText field to displaying hint on EditorActivity at the bottom of photo
     */
    private TextView mPhotoHintText;

    private int mCondition = ProductContract.ProductEntry.CONDITION_UNKNOWN;

    /**
     * Boolean value that keeps track of whether the product has been edited (true) or not (false)
     */
    private boolean mProductHasChanged = false;

    /**
     * EditText field to enter the product's quantity
     */
    private final int mCurrentQuantity = 0;

    /**
     * OnTouchListener which listens if user has touched a view so mProductHasChanged can be changes to true
     */
    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    // boolean value for required fields,TRUE if these fields have been populated
    boolean allRequiredValuesFilled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mConditionSpinner = (Spinner) findViewById(R.id.spinner_condition);
        mPhoto = (ImageView) findViewById(R.id.add_product_photo);
        mPrice = (EditText) findViewById(R.id.edit_product_price);
        mSupplierName = (EditText) findViewById(R.id.edit_supplier_name);
        mSupplierEmail = (EditText) findViewById(R.id.edit_supplier_email);
        mPhotoHintText = (TextView) findViewById(R.id.add_photo_hint);

        //Button ADD PRODUCT to increase product items
        Button mAddProductButton = (Button) findViewById(R.id.addProductButton);

        //Button REJECT PRODUCT to decrease product items
        Button mRemoveProductButton = (Button) findViewById(R.id.removeProductButton);

        // if product uri is null then we will be creating new product
        if (mCurrentProductUri == null) {
            //When a new product is added it change the app bar to "Add a Product"
            setTitle("Add an item");
            mPhotoHintText.setText("Tap to add photo");
            mSupplierName.setEnabled(true);
            mSupplierEmail.setEnabled(true);
            mQuantityEditText.setEnabled(true);
            mPhoto.setImageResource(R.drawable.add_product_image);
            mAddProductButton.setVisibility(View.GONE);
            mRemoveProductButton.setVisibility(View.GONE);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            //if user is editing existing product,app bar will show "Edit Product"
            setTitle("Edit Product");
            mPhotoHintText.setText("Tap to change photo");
            mSupplierName.setEnabled(false);
            mSupplierEmail.setEnabled(false);
            mQuantityEditText.setEnabled(false);
            mAddProductButton.setVisibility(View.VISIBLE);
            mRemoveProductButton.setVisibility(View.VISIBLE);

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        //Use setOnTouchListener on all input fields so we can determine if user
        //has ever touched or modified them.
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mConditionSpinner.setOnTouchListener(mTouchListener);
        mPrice.setOnTouchListener(mTouchListener);
        mSupplierName.setOnTouchListener(mTouchListener);
        mSupplierEmail.setOnTouchListener(mTouchListener);
        mAddProductButton.setOnTouchListener(mTouchListener);
        mRemoveProductButton.setOnTouchListener(mTouchListener);

        mAddProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItemButton(view);
            }
        });

        mRemoveProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeItemButton(view);
            }
        });

        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trySelector();
                mProductHasChanged = true;
            }
        });
        setupSpinner();
    }

    public void trySelector() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            return;
        }
        openSelector();
    }

    private void openSelector() {
        Intent intent;
        if(Build.VERSION.SDK_INT<19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select image"),0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSelector();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mImageUri = data.getData();
                mPhoto.setImageURI(mImageUri);
                mPhoto.invalidate();
            }
        }
    }

    public void orderMore() {
        Intent intent = new Intent(android.content.Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.setData(Uri.parse("mailto:" + mSupplierEmail.getText().toString().trim()));
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "New order: " +
                mNameEditText.getText().toString().trim());
        String message = "We need more stocks of: " +
                mNameEditText.getText().toString().trim() +
                "\n" +
                "We require total ____ pcs of the product." +
                "\n" +
                "\n" +
                "Regards," + "\n" +
                "__________";
        intent.putExtra(android.content.Intent.EXTRA_TEXT,message);
        startActivity(intent);
    }

    /**
     * Setup the dropdown spinner that allows the user to select the grade of the product.
     */
    public void setupSpinner() {

        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter condSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_condition_option, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        condSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mConditionSpinner.setAdapter(condSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mConditionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals("New")) {
                        mCondition = ProductContract.ProductEntry.CONDITION_NEW;
                    } else if (selection.equals("Used")) {
                        mCondition = ProductContract.ProductEntry.CONDITION_USED;
                    } else {
                        mCondition = ProductContract.ProductEntry.CONDITION_UNKNOWN;
                    }
                }
            }

            //As AdaperView is abstract class, so onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCondition = ProductContract.ProductEntry.CONDITION_UNKNOWN;
            }
        });
    }

    private boolean saveProduct() {
        // Quantity of products
        int quantity;

        // Read from input fields
        // Use trim to eliminate extra leading or trailing spaces
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPrice.getText().toString().trim();
        String supplierNameString = mSupplierName.getText().toString().trim();
        String supplierEmailString = mSupplierEmail.getText().toString().trim();

        // Check whether all fields are blank to check if the product is new
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierEmailString) &&
                mImageUri == null &&
                mCondition == ProductContract.ProductEntry.CONDITION_UNKNOWN) {
            // Since no fields were modified, we can return early without creating a new product.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            allRequiredValuesFilled = true;
            return allRequiredValuesFilled;
        }

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();

        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, "Product name is required", Toast.LENGTH_SHORT).show();
            return allRequiredValuesFilled;
        } else {
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        }

        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, "Quantity is required", Toast.LENGTH_SHORT).show();
            return allRequiredValuesFilled;
        } else {
            quantity = Integer.parseInt(quantityString);
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        }
        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, "Price is required", Toast.LENGTH_SHORT).show();
            return allRequiredValuesFilled;
        } else {
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
        }

        if (mImageUri == null) {
            Toast.makeText(this, "Product photo is required", Toast.LENGTH_SHORT).show();
            return allRequiredValuesFilled;
        } else {
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PICTURE, mImageUri.toString());
        }

        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_CONDITION, mCondition);
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL, supplierEmailString);

        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, "Error with saving item", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Product is saved succesfully", Toast.LENGTH_SHORT).show();
            }
        } else {
            int itemAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            if (itemAffected == 0) {
                Toast.makeText(this, "Error in updating product", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show();
            }
        }

        allRequiredValuesFilled = true;
        return allRequiredValuesFilled;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete_one_product);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.save_item:
                // Save the product
                saveProduct();
                if (allRequiredValuesFilled) {
                    finish();
                }
                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.delete_one_product:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;

            // Respond to a click on the "Order more" menu option
            case R.id.order_more_product:
                orderMore();
                return true;

            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtomClickListner = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtomClickListner);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_CONDITION,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PICTURE,
                ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
        };
        return new CursorLoader(this, mCurrentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {
            int nameColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int pictureColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PICTURE);
            int quantityColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierNameColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierEmailColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL);
            int conditionColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_CONDITION);

            // Extract out the value from the Cursor for the given column index
            String name = data.getString(nameColumnIndex);
            String price = data.getString(priceColumnIndex);
            String imageUriString = data.getString(pictureColumnIndex);
            int quantity = data.getInt(quantityColumnIndex);
            int condition = data.getInt(conditionColumnIndex);
            String supplierName = data.getString(supplierNameColumnIndex);
            String supplierEmail = data.getString(supplierEmailColumnIndex);
            mQuantity = quantity;
            mImageUri = Uri.parse(imageUriString);

            mNameEditText.setText(name);
            mPrice.setText(price);
            mPhoto.setImageURI(mImageUri);
            mSupplierName.setText(supplierName);
            mSupplierEmail.setText(supplierEmail);
            mQuantityEditText.setText(Integer.toString(quantity));

            // Condition is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (condition) {
                case ProductContract.ProductEntry.CONDITION_NEW:
                    mConditionSpinner.setSelection(1);
                    break;

                case ProductContract.ProductEntry.CONDITION_USED:
                    mConditionSpinner.setSelection(2);
                    break;

                case ProductContract.ProductEntry.CONDITION_UNKNOWN:
                    mConditionSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPrice.setText("");
        mPhoto.setImageResource(R.drawable.add_product_image);
        mQuantityEditText.setText("");
        mSupplierName.setText("");
        mSupplierEmail.setText("");
        mConditionSpinner.setSelection(0);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard changes and quit editing?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
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

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this Product?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteProduct();
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

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, "Error in deleting product", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Product deleted succesfully", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void addItemButton(View view) {
        mQuantity++;
        displayQuantity();
    }

    private void removeItemButton(View view) {
        if (mQuantity == 0) {
            Toast.makeText(this, "Quantity can't be less than zero", Toast.LENGTH_SHORT).show();
        } else {
            mQuantity--;
            displayQuantity();
        }
    }

    public void displayQuantity() {
        mQuantityEditText.setText(String.valueOf(mQuantity));
    }

}
