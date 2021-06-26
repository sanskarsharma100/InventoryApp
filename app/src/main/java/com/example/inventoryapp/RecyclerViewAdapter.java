package com.example.inventoryapp;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventoryapp.Data.ProductEntry;
import com.example.inventoryapp.databinding.ItemListBinding;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ProductViewHolder> {

    public ItemListClickListener listener;
    public List<ProductEntry> productEntries;

    public RecyclerViewAdapter(ItemListClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @org.jetbrains.annotations.NotNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemListBinding listBinding = DataBindingUtil.inflate(inflater,R.layout.item_list,parent,false);
        return new ProductViewHolder(listBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull @org.jetbrains.annotations.NotNull RecyclerViewAdapter.ProductViewHolder holder, int position) {
        ProductEntry productEntry = productEntries.get(position);
        holder.listBinding.productName.setText(productEntry.getProductName());
        String quantity = String.valueOf(productEntry.getProductQuantity());
        holder.listBinding.productQuantityNumber.setText(quantity);
        holder.listBinding.productPrice.setText(productEntry.getProductPrice());
        Uri productImageUri = Uri.parse(productEntry.getProductImageUri());
        holder.listBinding.productImage.setImageURI(productImageUri);
        int productCondition = productEntry.getProductCondition();
        if (productCondition == 1) {
            holder.listBinding.productCondition.setText(R.string.new_condition);
        } else if (productCondition == 2) {
            holder.listBinding.productCondition.setText(R.string.used_condition);
        } else {
            holder.listBinding.productCondition.setText(R.string.unknown_condition);
        }
        holder.listBinding.productBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSaleClick(v,productEntry);
            }
        });
    }

    public void setData(List<ProductEntry> entries) {
        productEntries=entries;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if(productEntries==null) return 0;
        return productEntries.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ItemListBinding listBinding;
        public ProductViewHolder(ItemListBinding listBinding) {
            super(listBinding.getRoot());
            this.listBinding = listBinding;
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            ProductEntry productEntry = productEntries.get(getAbsoluteAdapterPosition());
            listener.onItemClick(v,productEntry);
        }
    }

    public interface ItemListClickListener{
        void onItemClick(View v, ProductEntry productEntry);
        void onSaleClick(View v,ProductEntry productEntry);
    }
}
