package com.example.inventoryapp.Data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id DESC")
    LiveData<List<ProductEntry>> getAllProducts();

    @Insert
    void insertProduct(ProductEntry productEntry);

    @Delete
    void deleteProduct(ProductEntry productEntry);

    @Query("DELETE FROM products")
    void deleteAll();

    @Update
    void updateProduct(ProductEntry productEntry);

    @Query("UPDATE products SET quantity=:updatedQuantity WHERE id=:productId")
    void updateQuantity(int updatedQuantity, int productId);
}
