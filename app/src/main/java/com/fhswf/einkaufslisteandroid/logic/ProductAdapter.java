package com.fhswf.einkaufslisteandroid.logic;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.fhswf.einkaufslisteandroid.R;
import com.fhswf.einkaufslisteandroid.datenpersistierung.FirestoreManager;
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

import java.util.List;


/**
 * Der ProductApdater stellt die gefilterten Produktdaten in der RecyclerView dar.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private Context context;
    private List<Product> productList;

    private boolean checkBoxAnzeige;
    private String listName;


    /**
     * Konstruktor für den ProductAdapter.
     *
     * @param context     der Kontext, in dem der Adapter verwendet wird
     * @param productList die Liste der Produkte, die angezeigt werden sollen
     */
    public ProductAdapter(Context context, List<Product> productList, boolean checkBoxAnzeige) {
        this.context = context;
        this.productList = productList;
        this.checkBoxAnzeige = checkBoxAnzeige;
    }

    public ProductAdapter(Context context, List<Product> productList, boolean checkBoxAnzeige, String listName){
        this(context, productList, checkBoxAnzeige);
        this.listName = listName;
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


        // Prüfen, ob die CheckBox existiert (nur wenn checkBoxAnzeige = true)
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
                    );// HomeFragment neu laden, nachdem das Produkt als gekauft markiert wurde
                    if (context instanceof FragmentActivity) {
                        FragmentActivity activity = (FragmentActivity) context;
                        activity.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container_view_tag, new HomeFragment())  // Hier wird das HomeFragment neu geladen
                                .commit();
                    }
                }, e -> Log.e("ProductAdapter", "Fehler beim Abrufen der List-ID: " + e));
            });
        }


        //Wenn man auf ein Produkt klickt, wird das PopUp Fenster geöffnet
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ProductDetailsFragment pdf = ProductDetailsFragment.erstelleFragmentMitDaten(
                        product.getName(),
                        product.getImageURL(),
                        product.getZutaten(),
                        product.getNaehrwerte(),
                        product.getAllergene(),
                        product.getStore()
                );
                pdf.show(((FragmentActivity) context).getSupportFragmentManager(), "ProductDetailsFragment");  //Zeigt das Popup-Fenster an
            }
        });
    }

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

