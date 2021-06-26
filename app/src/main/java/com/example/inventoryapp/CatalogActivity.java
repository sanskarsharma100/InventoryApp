package com.example.inventoryapp;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventoryapp.Data.ProductEntry;
import com.example.inventoryapp.databinding.ActivityCatalogBinding;
import com.example.inventoryapp.viewModel.ProductViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Displays list of products that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements RecyclerViewAdapter.ItemListClickListener {

    public static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    private RecyclerViewAdapter adapter;
    private List<ProductEntry> productList;
    private ProductViewModel productViewModel;
    private ActivityCatalogBinding catalogBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        catalogBinding = DataBindingUtil.setContentView(this,R.layout.activity_catalog);
        catalogBinding.floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivityForResult(intent,Constant.ADD_REQUEST_CODE);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        catalogBinding.recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerViewAdapter(this);
        catalogBinding.recyclerView.setAdapter(adapter);
        LoadProductList();
    }

    private void LoadProductList() {
        productViewModel =
                new ViewModelProvider(this,
                        ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication()))
                        .get(ProductViewModel.class);
        productViewModel.getProductList().observe(this, new Observer<List<ProductEntry>>() {
            @Override
            public void onChanged(List<ProductEntry> productEntries) {
                if(productEntries!=null) {
                    productList = productEntries;
                    adapter.setData(productEntries);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    private void insertProduct() {
        Uri imageUri = getUriToDrawable(this,R.drawable.default_iphone);
        String productName = "Apple iphone 11";
        int productCondition = 1;
        int productQuantity = 4;
        String supplierName = "Tanuj Mehta";
        String supplierEmail= "tanuj@gmail.com";
        String productPrice = "85000";
        ProductEntry productEntry = new ProductEntry(productName,productCondition,productPrice,
                supplierName,supplierEmail,imageUri.toString(),productQuantity);
        productViewModel.insert(productEntry);
    }

    public static Uri getUriToDrawable(@NonNull Context context,
                                       @AnyRes int drawableId) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + context.getResources().getResourcePackageName(drawableId)
                + '/' + context.getResources().getResourceTypeName(drawableId)
                + '/' + context.getResources().getResourceEntryName(drawableId));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Constant.ADD_REQUEST_CODE && resultCode==RESULT_OK) {
            productViewModel.insert(data.getParcelableExtra(Constant.INTENT_EXTRA));
            Toast.makeText(CatalogActivity.this,"Product Added Successfully",Toast.LENGTH_SHORT).show();
        } else if(requestCode==Constant.EDIT_REQUEST_CODE && resultCode==RESULT_OK) {
            if(data.hasExtra(Constant.DELETE_INTENT_EXTRA)) {
                ProductEntry entry = data.getParcelableExtra(Constant.DELETE_INTENT_EXTRA);
                productViewModel.delete(entry);
                Toast.makeText(CatalogActivity.this,"Product Deleted Successfully",Toast.LENGTH_SHORT).show();
            } else {
                productViewModel.update(data.getParcelableExtra(Constant.INTENT_EXTRA));
                Toast.makeText(CatalogActivity.this, "Product Saved Successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.insert_dummy_node:
                insertProduct();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.delete_all:
                showDeleteAllConfirmationBox();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
                if(productList.isEmpty()) {
                    Toast.makeText(CatalogActivity.this, "No products to delete", Toast.LENGTH_SHORT).show();
                } else {
                    productViewModel.deleteAll();
                    Toast.makeText(CatalogActivity.this, "All Products deleted", Toast.LENGTH_SHORT).show();
                }
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

    @Override
    public void onItemClick(View v, com.example.inventoryapp.Data.ProductEntry productEntry) {
        Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
        intent.putExtra(Constant.INTENT_EXTRA,productEntry);
        startActivityForResult(intent,Constant.EDIT_REQUEST_CODE);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void onSaleClick(View v, ProductEntry productEntry) {
        int quantity = productEntry.getProductQuantity();
        int productId = productEntry.getId();
        adjustProductQuantity(quantity,productId);
    }

    private void adjustProductQuantity(int currentQuantityInStock,int productId) {
        if (currentQuantityInStock == 0) {
            Toast.makeText(CatalogActivity.this, "Product is out of stock!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Subtract 1 if product quantity is greater than 0 otherwise assign 0
        int newQuantityValue = (currentQuantityInStock >= 1) ? currentQuantityInStock - 1 : 0;
        productViewModel.updateQuantity(newQuantityValue,productId);
    }

}