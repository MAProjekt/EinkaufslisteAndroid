package com.fhswf.einkaufslisteandroid.logic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fhswf.einkaufslisteandroid.R;
import com.fhswf.einkaufslisteandroid.fragment.ProductDetailsFragment;
import com.fhswf.einkaufslisteandroid.models.Product;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(view);
    }


    //Anzeigen des Produkts, Listener für PopupFenster
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.nameTextView.setText(product.getName());
        holder.brandTextView.setText(product.getMarke());
        Glide.with(context).load(product.getImageURL()).into(holder.productImageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProductDetailsFragment pdf = ProductDetailsFragment.newInstance(
                        product.getName(),
                        product.getImageURL(),
                        product.getZutaten(),
                        product.getNaehrwerte(),
                        product.getAllergene(),
                        product.getHerkunft()
                );
                pdf.show(((FragmentActivity) context).getSupportFragmentManager(), "ProductDetailsFragment");
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, brandTextView;
        ImageView productImageView;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            brandTextView = itemView.findViewById(R.id.brandTextView);
            productImageView = itemView.findViewById(R.id.productImageView);
        }
    }
}

