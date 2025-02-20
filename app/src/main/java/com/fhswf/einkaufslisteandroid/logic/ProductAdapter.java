package com.fhswf.einkaufslisteandroid.logic;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.fhswf.einkaufslisteandroid.R;
import com.fhswf.einkaufslisteandroid.datenpersistierung.FirestoreManager;
import com.fhswf.einkaufslisteandroid.fragment.GroupsFragment;
import com.fhswf.einkaufslisteandroid.fragment.HomeFragment;
import com.fhswf.einkaufslisteandroid.fragment.ProductDetailsFragment;
import com.fhswf.einkaufslisteandroid.fragment.UebersichtFragment;
import com.fhswf.einkaufslisteandroid.models.Product;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Der ProductApdater stellt die gefilterten Produktdaten in der RecyclerView dar.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private Context context;
    private List<Product> productList;

    private boolean checkBoxAnzeige;
    private String listName;
    private String listId;


    /**
     * Konstruktor für den ProductAdapter.
     *
     * @param context der Kontext, in dem der Adapter verwendet wird.
     * @param productList die Liste der Produkte, die angezeigt werden sollen.
     * @param checkBoxAnzeige Prüfwert der angibt, ob ein Produkt abgehakt ist.
     */
    public ProductAdapter(Context context, List<Product> productList, boolean checkBoxAnzeige) {
        this.context = context;
        this.productList = productList;
        this.checkBoxAnzeige = checkBoxAnzeige;
    }

    /**
     * Konstruktor mit dem dazugehörigen Listennamen, verwendet den ersten Konstruktor.
     * @param context der Kontext, in dem der Adapter verwendet wird.
     * @param productList die Liste der Produkte, die angezeigt werden sollen.
     * @param checkBoxAnzeige Prüfwert der angibt, ob ein Produkt abgehakt ist.
     * @param listName Name der Liste.
     */
    public ProductAdapter(Context context, List<Product> productList, boolean checkBoxAnzeige, String listName){
        this(context, productList, checkBoxAnzeige);
        this.listName = listName;
        sortProducts();
    }
    public ProductAdapter(Context context, List<Product> productList, String listId, boolean checkBoxAnzeige) {
        this.listId = listId; // Speichere die listId
        this.context = context;
        this.productList = productList;
        this.checkBoxAnzeige = checkBoxAnzeige;
        sortProducts();
    }

    /**
     * Sortiert die Produkte nach dem Status "gekauft".
     * Die als gekauft markierten Produkte stehen am Ende der Liste.
     */
    private void sortProducts(){
        Collections.sort(productList, new Comparator<Product>() {
            @Override
            public int compare(Product o1, Product o2) {
                return Boolean.compare(o1.getGekauft(), o2.getGekauft());
            }
        });
    }

    /**
     * Erstellt eine neue View für ein Produktelement.
     *
     * @param parent  die übergeordnete ViewGroup, in der die neue View hinzugefügt wird
     * @param viewType wird nicht verwendet
     * @return Ein neuer ProductViewHolder mit der erstellten View
     */
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (checkBoxAnzeige) {
            view = LayoutInflater.from(context).inflate(R.layout.product_item_checkbox, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.product_item, parent, false);
        }
        return new ProductViewHolder(view);
    }


    /**
     * füllt die View eines Produktelements mit den entsprechenden Daten aus der Produktliste.
     *
     * @param holder   ViewHolder, der die Daten enthält.
     * @param position  Position des aktuellen Produkts in der Liste.
     */
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.nameText.setText(product.getName());
        holder.brandText.setText(product.getMarke());
        Glide.with(context).load(product.getImageURL()).into(holder.productImage);

        if(holder.produktItemMenge != null){
            holder.produktItemMenge.setText("Menge: " + product.getMenge());
        }

        holder.itemView.post(() -> {
            int itemWidth = holder.itemView.getWidth();
            Log.d("ItemSize", "Breite des Items an Position " + position + ": " + itemWidth + "px");
        });


        if (holder.gekauftCheckBox != null) {
            holder.gekauftCheckBox.setChecked(product.getGekauft());
            holder.itemView.setAlpha(product.getGekauft() ? 0.5f : 1.0f);

            holder.gekauftCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                product.setGekauft(isChecked);
                holder.itemView.setAlpha(isChecked ? 0.5f : 1.0f);

                FirestoreManager firestoreManager = new FirestoreManager();

                firestoreManager.getListIdByName(listName, listId -> {
                    firestoreManager.updateProductStatus(
                            listId,
                            product,
                            aVoid -> Log.d("ProductAdapter", "Produktstatus aktualisiert"),
                            e -> Log.e("ProductAdapter", "Fehler beim Aktualisieren: " + e)
                    );

                    if (context instanceof FragmentActivity) {
                        FragmentActivity activity = (FragmentActivity) context;

                        // Prüfe, ob das aktuelle Fragment HomeFragment oder GroupsFragment ist
                        Fragment currentFragment = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container_view_tag);

                        if (currentFragment instanceof HomeFragment) {
                            ((HomeFragment) currentFragment).updateProgressBar();
                        } else if (currentFragment instanceof GroupsFragment) {
                            ((GroupsFragment) currentFragment).updateProgressBar();
                        }
                    }

                }, e -> Log.e("ProductAdapter", "Fehler beim Abrufen der List-ID: " + e));
            });
        }


        //Wenn man auf ein Produkt klickt, wird das PopUp Fenster geöffnet
        holder.itemView.setOnClickListener(v -> {
            Log.d("DEBUG", "Produkt wird geöffnet: " + product.getName() + " | listId: " + listId);

            Bundle args = new Bundle();
            args.putString("listId", listId);
            args.putString("product_name", product.getName());
            args.putString("image_url", product.getImageURL());
            args.putString("ingredients", product.getZutaten());
            args.putString("nutriments", product.getNaehrwerte());
            args.putString("allergens_from_ingredients", product.getAllergene());
            args.putString("stores", product.getStore());

            boolean fromUebersicht = false;

            if (context instanceof FragmentActivity) {
                FragmentActivity activity = (FragmentActivity) context;
                Fragment currentFragment = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container_view_tag);

                if (currentFragment instanceof UebersichtFragment) {
                    fromUebersicht = true;
                }
            }

            args.putBoolean("fromUebersicht", fromUebersicht);

            ProductDetailsFragment pdf = new ProductDetailsFragment();
            pdf.setArguments(args);

            pdf.show(((FragmentActivity) context).getSupportFragmentManager(), "ProductDetailsFragment");
        });




    }

    /**
     * Methode die die Anzahl der Produkte der Liste zurückgibt.
     * @return Größe bzw. Anzahl der Produkte.
     */
    @Override
    public int getItemCount() {
        return productList.size();
    }

    /**
     * ViewHolder-Klasse für ein einzelnes Produktelement in der RecyclerView.
     */
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, brandText, produktItemMenge;
        ImageView productImage;
        CheckBox gekauftCheckBox;

        /**
         * Konstruktor für das ViewHolder.
         *
         * @param itemView Die View des Elements.
         */
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameTextView);
            brandText = itemView.findViewById(R.id.brandTextView);
            productImage = itemView.findViewById(R.id.productImageView);
            gekauftCheckBox = itemView.findViewById(R.id.checkBox); // Kann null sein
            produktItemMenge = itemView.findViewById(R.id.produktItemMenge);
        }
    }
}

