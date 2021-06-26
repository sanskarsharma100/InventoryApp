package com.example.inventoryapp.Data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.inventoryapp.Constant;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = Constant.DATABASE_NAME)
public class ProductEntry implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "name")
    private String productName;

    @ColumnInfo(name = "condition")
    private int productCondition;

    @NonNull
    @ColumnInfo(name = "price")
    private String productPrice;

    @NonNull
    @ColumnInfo(name = "supplierName")
    private String supplierName;

    @NonNull
    @ColumnInfo(name = "supplierEmail")
    private String supplierEmail;

    @ColumnInfo(name = "image")
    private String productImageUri;

    @ColumnInfo(name = "quantity")
    private int productQuantity;

    @Ignore
    public ProductEntry(String productName, int productCondition,
                        String productPrice, String supplierName,
                        String supplierEmail, String productImageUri,
                        int productQuantity) {
        this.productName = productName;
        this.productCondition = productCondition;
        this.productPrice = productPrice;
        this.supplierName = supplierName;
        this.supplierEmail = supplierEmail;
        this.productImageUri = productImageUri;
        this.productQuantity = productQuantity;
    }

    public ProductEntry(int id, String productName, int productCondition,
                        String productPrice, String supplierName,
                        String supplierEmail, String productImageUri,
                        int productQuantity) {
        this.id = id;
        this.productName = productName;
        this.productCondition = productCondition;
        this.productPrice = productPrice;
        this.supplierName = supplierName;
        this.supplierEmail = supplierEmail;
        this.productImageUri = productImageUri;
        this.productQuantity = productQuantity;
    }

    protected ProductEntry(Parcel in) {
        id = in.readInt();
        productName = in.readString();
        productCondition = in.readInt();
        productPrice = in.readString();
        supplierName = in.readString();
        supplierEmail = in.readString();
        productImageUri = in.readString();
        productQuantity = in.readInt();
    }

    public static final Creator<ProductEntry> CREATOR = new Creator<ProductEntry>() {
        @Override
        public ProductEntry createFromParcel(Parcel in) {
            return new ProductEntry(in);
        }

        @Override
        public ProductEntry[] newArray(int size) {
            return new ProductEntry[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(@NotNull String productName) {
        this.productName = productName;
    }

    public int getProductCondition() {
        return productCondition;
    }

    public void setProductCondition(int productCondition) {
        this.productCondition = productCondition;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierEmail() {
        return supplierEmail;
    }

    public void setSupplierEmail(String supplierEmail) {
        this.supplierEmail = supplierEmail;
    }

    public String getProductImageUri() {
        return productImageUri;
    }

    public void setProductImage(String productImageUri) {
        this.productImageUri = productImageUri;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(productName);
        dest.writeInt(productCondition);
        dest.writeString(productPrice);
        dest.writeString(supplierName);
        dest.writeString(supplierEmail);
        dest.writeString(productImageUri);
        dest.writeInt(productQuantity);
    }
}
