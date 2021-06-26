package com.example.inventoryapp.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.inventoryapp.AppExecutors;
import com.example.inventoryapp.Data.ProductDao;
import com.example.inventoryapp.Data.ProductDatabase;
import com.example.inventoryapp.Data.ProductEntry;

import java.util.List;

public class ProductRepository {
    private ProductDao productDao;
    private LiveData<List<ProductEntry>> productList;

    public ProductRepository(Application application) {
        ProductDatabase mDb = ProductDatabase.getsInstance(application);
        productDao = mDb.productDao();
        productList = productDao.getAllProducts();
    }

    public void insert(ProductEntry productEntry) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                productDao.insertProduct(productEntry);
            }
        });
    }

    public void update(ProductEntry productEntry) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                productDao.updateProduct(productEntry);
            }
        });
    }

    public void delete(ProductEntry productEntry) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                productDao.deleteProduct(productEntry);
            }
        });
    }

    public void deleteAll() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                productDao.deleteAll();
            }
        });
    }

    public void updateQuantity(int quantity,int productId) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                productDao.updateQuantity(quantity,productId);
            }
        });
    }

    public LiveData<List<ProductEntry>> getProductList() {
        return productList;
    }
}
