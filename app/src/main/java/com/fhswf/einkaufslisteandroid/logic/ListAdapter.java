// Quelle: https://developer.android.com/develop/ui/views/layout/recyclerview?hl=de

package com.fhswf.einkaufslisteandroid.logic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fhswf.einkaufslisteandroid.R;
import com.fhswf.einkaufslisteandroid.models.Product;
import com.fhswf.einkaufslisteandroid.models.ProductList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Klasse welche die Namen der Einkaufslisten anzeigt und sie durch ein Klick auf den Namen aufrufbar macht.
 * Die Einkaufslisten-Namen werden im HomeFragment angezeigt.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private final List<String> listNames;
    private final OnListClickListener listener;
    private Context context;

    /**
     * Interface, wenn auf ein Listenelement geklickt wird.
     */
    public interface OnListClickListener {
        void onListClick(String listName);
    }


    /**
     * Konstruktor für den ListAdapter.
     *
     * @param listNames Liste der Namen, die angezeigt werden sollen.
     * @param listener Listener für Klick-Ereignisse auf Listenelementen.
     */
    public ListAdapter(List<String> listNames, OnListClickListener listener, Context context) {
        this.listNames = listNames;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String listName = listNames.get(position);
        holder.listNameTextview.setText(listName);
        holder.itemView.setOnClickListener(v -> listener.onListClick(listName));

        // Hole den aktuellen Nutzer und initialisiere Firestore
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // vlt noch irgendwie mit getLists aus FirestoreManager, aber war bei mir fast genauso viel Code
        db.collection("users").document(userId)
                .collection("lists").document(listName)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    ProductList productList = documentSnapshot.toObject(ProductList.class);
                    if (productList != null) {
                        List<Product> products = productList.getProducts();
                        double progress = calculateCompletionOfList(products);
                        holder.progressBar.setProgress((int) progress);
                        holder.progressTextView.setText(String.format("%.0f%%", progress));
                    } else {
                        holder.progressBar.setProgress(0);
                        holder.progressTextView.setText("0%");
                    }
                })
                .addOnFailureListener(e -> {
                    holder.progressBar.setProgress(0);
                    holder.progressTextView.setText("0%");
                });

    }

    /**
     * Gibt die Anzahl der Elemente in der Liste zurück.
     *
     * @return Die Anzahl der Listenelemente.
     */
    @Override
    public int getItemCount() {
        return listNames.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView listNameTextview;
        ProgressBar progressBar;
        TextView progressTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.listNameTextview = itemView.findViewById(R.id.listNameTextView);
            this.progressBar = itemView.findViewById(R.id.progressBar);
            this.progressTextView = itemView.findViewById(R.id.progressTextView);
        }
    }

    /**
     * Berechnet den Prozentsatz der Produkte, die als "gekauft" markiert sind.
     * @param productList Liste der Produkte
     * @return Prozentsatz der gekauften/abgehakten Produkte
     */
    private double calculateCompletionOfList(List<Product> productList) {
        if (productList == null || productList.isEmpty()) {
            return 0.0;
        }
        int checkedCount = 0;
        for (Product product : productList) {
            if (product.getGekauft()) { // Hier wird das Feld "gekauft" ausgewertet
                checkedCount++;
            }
        }
        return (checkedCount * 100.0) / productList.size();
    }

}
