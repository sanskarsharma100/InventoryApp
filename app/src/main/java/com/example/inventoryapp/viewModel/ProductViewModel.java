package com.example.inventoryapp.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.inventoryapp.Data.ProductEntry;
import com.example.inventoryapp.repository.ProductRepository;

import java.util.List;

public class ProductViewModel extends AndroidViewModel {
    private final ProductRepository productRepository;
    LiveData<List<ProductEntry>> productList;

    public ProductViewModel(@NonNull Application application) {
        super(application);
        productRepository = new ProductRepository(application);
        productList = productRepository.getProductList();
    }

    public void insert(ProductEntry productEntry) {
        productRepository.insert(productEntry);
    }

    public void update(ProductEntry productEntry) {
        productRepository.update(productEntry);
    }

    public void updateQuantity(int quantity,int productId) {
        productRepository.updateQuantity(quantity,productId);
    }

    public void delete(ProductEntry productEntry) {
        productRepository.delete(productEntry);
    }

    public void deleteAll() {
        productRepository.deleteAll();
    }

    public LiveData<List<ProductEntry>> getProductList() {
        return productList;
    }
}
