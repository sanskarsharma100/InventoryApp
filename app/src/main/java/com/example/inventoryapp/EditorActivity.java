package com.example.inventoryapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.example.inventoryapp.Data.ProductEntry;
import com.example.inventoryapp.databinding.ActivityEditorBinding;

/**
 * Allows user to create a new product or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity {

    /**
     * URI of product image
     */
    private Uri mImageUri;

    /**
     * Current quantity of product
     */
    private int mQuantity;

    private int mCondition = Constant.CONDITION_UNKNOWN;

    /**
     * Boolean value that keeps track of whether the product has been edited (true) or not (false)
     */
    private boolean mProductHasChanged = false;
    private boolean isEditProduct = false;
    private ActivityEditorBinding editorBinding;
    private ProductEntry productEntry;

    /**
     * OnTouchListener which listens if user has touched a view so mProductHasChanged can be changes to true
     */
    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    // boolean value for required fields,TRUE if these fields have been populated
    boolean allRequiredValuesFilled = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        editorBinding = DataBindingUtil.setContentView(this,R.layout.activity_editor);
        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one
        if(getIntent().hasExtra(Constant.INTENT_EXTRA)) {
            productEntry = getIntent().getParcelableExtra(Constant.INTENT_EXTRA);
            isEditProduct = true;
        }
        setupSpinner();
        // if product uri is null then we will be creating new product
        if (!isEditProduct) {
            //When a new product is added it change the app bar to "Add a Product"
            setTitle("Add an item");
            editorBinding.addPhotoHint.setText(getString(R.string.add_new_photo_text));
            editorBinding.editSupplierName.setEnabled(true);
            editorBinding.editSupplierEmail.setEnabled(true);
            editorBinding.editProductQuantity.setEnabled(true);
            editorBinding.addProductPhoto.setImageResource(R.drawable.add_product_image);
            editorBinding.addProductButton.setVisibility(View.GONE);
            editorBinding.removeProductButton.setVisibility(View.GONE);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            //if user is editing existing product,app bar will show "Edit Product"
            setTitle("Edit Product");
            editorBinding.addPhotoHint.setText(getString(R.string.change_photo_text));
            editorBinding.editSupplierName.setEnabled(false);
            editorBinding.editSupplierEmail.setEnabled(false);
            editorBinding.editProductQuantity.setEnabled(false);
            editorBinding.addProductButton.setVisibility(View.VISIBLE);
            editorBinding.removeProductButton.setVisibility(View.VISIBLE);
            setProductInfo();
        }

        //Use setOnTouchListener on all input fields so we can determine if user
        //has ever touched or modified them.
        editorBinding.editProductName.setOnTouchListener(mTouchListener);
        editorBinding.editProductQuantity.setOnTouchListener(mTouchListener);
        editorBinding.spinnerCondition.setOnTouchListener(mTouchListener);
        editorBinding.addProductPhoto.setOnTouchListener(mTouchListener);
        editorBinding.editSupplierName.setOnTouchListener(mTouchListener);
        editorBinding.editSupplierEmail.setOnTouchListener(mTouchListener);
        editorBinding.addProductButton.setOnTouchListener(mTouchListener);
        editorBinding.removeProductButton.setOnTouchListener(mTouchListener);

        editorBinding.addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItemButton();
            }
        });

        editorBinding.removeProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeItemButton();
            }
        });

        editorBinding.addProductPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trySelector();
                mProductHasChanged = true;
            }
        });
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
        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select image"),0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
                editorBinding.addProductPhoto.setImageURI(mImageUri);
                editorBinding.addProductPhoto.invalidate();
            }
        }
    }

    private void setProductInfo() {
        // Extract out the value from the Cursor for the given column index
        String name = productEntry.getProductName();
        String price = productEntry.getProductPrice();
        String imageUriString = productEntry.getProductImageUri();;
        int quantity = productEntry.getProductQuantity();
        int condition = productEntry.getProductCondition();
        String supplierName = productEntry.getSupplierName();
        String supplierEmail = productEntry.getSupplierEmail();
        mQuantity = quantity;
        mImageUri = Uri.parse(imageUriString);

        editorBinding.editProductName.setText(name);
        editorBinding.editProductPrice.setText(price);
        editorBinding.addProductPhoto.setImageURI(mImageUri);
        editorBinding.editSupplierName.setText(supplierName);
        editorBinding.editSupplierEmail.setText(supplierEmail);
        editorBinding.editProductQuantity.setText(String.valueOf(quantity));
        // Condition is a dropdown spinner, so map the constant value from the database
        // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
        // Then call setSelection() so that option is displayed on screen as the current selection.
        switch (condition) {
            case Constant.CONDITION_NEW:
                editorBinding.spinnerCondition.setSelection(1);
                break;

            case Constant.CONDITION_USED:
                editorBinding.spinnerCondition.setSelection(2);
                break;

            case Constant.CONDITION_UNKNOWN:
                editorBinding.spinnerCondition.setSelection(0);
                break;
        }
    }

    public void orderMore() {
        Intent intent = new Intent(android.content.Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.setData(Uri.parse("mailto:" + editorBinding.editSupplierEmail.getText().toString().trim()));
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "New order: " +
                editorBinding.addPhotoHint.getText().toString().trim());
        String message = "We need more stocks of: " +
                editorBinding.addPhotoHint.getText().toString().trim() +
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
     * Setup the dropdown spinner that allows the user to select the condition of the product.
     */
    public void setupSpinner() {

        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter condSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_condition_option, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        condSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        editorBinding.spinnerCondition.setAdapter(condSpinnerAdapter);

        // Set the integer mSelected to the constant values
        editorBinding.spinnerCondition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals("New")) {
                        mCondition = Constant.CONDITION_NEW;
                    } else if (selection.equals("Used")) {
                        mCondition = Constant.CONDITION_USED;
                    } else {
                        mCondition = Constant.CONDITION_UNKNOWN;
                    }
                }
            }
            //As AdapterView is abstract class, so onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCondition = Constant.CONDITION_UNKNOWN;
            }
        });
    }

    private void saveProduct() {
        // Read from input fields
        // Use trim to eliminate extra leading or trailing spaces
        String nameString = editorBinding.editProductName.getText().toString().trim();
        String quantityString = editorBinding.editProductQuantity.getText().toString().trim();
        String priceString = editorBinding.editProductPrice.getText().toString().trim();
        String supplierNameString = editorBinding.editSupplierName.getText().toString().trim();
        String supplierEmailString = editorBinding.editSupplierEmail.getText().toString().trim();

        // Check whether all fields are blank to check if the product is new
        if (TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierEmailString) &&
                mImageUri == null &&
                mCondition == Constant.CONDITION_UNKNOWN) {
            // Since no fields were modified, we can return early without creating a new product.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            allRequiredValuesFilled = true;
            return;
        }

        if(TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, "Product name is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if(mImageUri==null) {
            Toast.makeText(this, "Product photo is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(supplierNameString)) {
            Toast.makeText(this, "Supplier Name is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(supplierEmailString)) {
            Toast.makeText(this, "Supplier Email is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, "Price is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, "Quantity is required", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent data = new Intent();
        if(isEditProduct) {
            productEntry.setProductName(nameString);
            productEntry.setProductImage(mImageUri.toString());
            productEntry.setProductPrice(priceString);
            productEntry.setProductQuantity(Integer.parseInt(quantityString));
            productEntry.setSupplierName(supplierNameString);
            productEntry.setSupplierEmail(supplierEmailString);
            productEntry.setProductCondition(mCondition);
            data.putExtra(Constant.INTENT_EXTRA,productEntry);
        } else {
            ProductEntry newProductEntry = new ProductEntry(nameString,mCondition,priceString,supplierNameString,
                supplierEmailString,mImageUri.toString(),Integer.parseInt(quantityString));
            data.putExtra(Constant.INTENT_EXTRA,newProductEntry);
        }
        setResult(RESULT_OK,data);
        allRequiredValuesFilled = true;
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
        if (!isEditProduct) {
            MenuItem menuItem = menu.findItem(R.id.delete_one_product);
            menuItem.setVisible(false);
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
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
        Log.d("BACKPRESS",String.valueOf(mProductHasChanged));
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
        Intent data = new Intent();
        data.putExtra(Constant.DELETE_INTENT_EXTRA,productEntry);
        setResult(RESULT_OK,data);
        finish();
    }

    private void addItemButton() {
        mQuantity++;
        displayQuantity();
    }

    private void removeItemButton() {
        if (mQuantity == 0) {
            Toast.makeText(this, "Quantity can't be less than zero", Toast.LENGTH_SHORT).show();
        } else {
            mQuantity--;
            displayQuantity();
        }
    }

    public void displayQuantity() {
        editorBinding.editProductQuantity.setText(String.valueOf(mQuantity));
    }

}
