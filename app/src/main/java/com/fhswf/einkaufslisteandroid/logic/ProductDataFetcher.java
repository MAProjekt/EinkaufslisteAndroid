package com.fhswf.einkaufslisteandroid.logic;

import android.content.Context;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fhswf.einkaufslisteandroid.models.Product;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Diese Klasse lädt und filtert Produktdaten aus einer JSON-File.
 * Aktualisiert auch die RecyclerView mit dem ProduktAdapter.
 */
public class ProductDataFetcher {

    private Context context;
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private ExecutorService executor;

    public ProductDataFetcher(Context context, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.productList = new ArrayList<>();
        this.adapter = new ProductAdapter(context, productList);
        this.executor = Executors.newSingleThreadExecutor(); // Für Hintergrundaufgaben

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
    }


    public void fetchProductData(String searchTerm) {
        executor.execute(() -> {
            try {
                // Datei aus dem assets-Ordner laden
                InputStream inputStream = context.getAssets().open("daten.json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();
                inputStream.close();

                // JSON-Daten als Array verarbeiten
                JSONArray jsonArray = new JSONArray(stringBuilder.toString());

                List<Product> filteredList = new ArrayList<>(); // Temporäre Liste für gefilterte Produkte

                // Durch das Haupt-Array iterieren
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    // Products, JSONArrays extgrahieren
                    if (jsonObject.has("products")) {
                        JSONArray products = jsonObject.getJSONArray("products");

                        // Produkte durchsuchen
                        for (int j = 0; j < products.length(); j++) {
                            JSONObject product = products.getJSONObject(j);
                            String name = product.optString("product_name", "Unbekannt");

                            // Nach Suchbegriff filtern
                            if (name.toLowerCase().contains(searchTerm.toLowerCase())) {
                                String marke = product.optString("brands", "Unbekannt");
                                String imageUrl = product.optString("image_url", "");
                                String laden = product.optString("stores", "Kein Laden verfügbar");
                                String nutrients = product.optString("nutriments", "Keine Nährwerte");
                                String zutaten = product.optString("ingredients", "Keine Zutaten");
                                String allergene = product.optString("allergens_from_ingredients", "Keine Allergene");

                                filteredList.add(new Product(name, imageUrl, marke, laden, nutrients, zutaten, allergene));
                            }
                        }
                    }
                }

                // RecyclerView-Adapter aktualisieren
                updateRecyclerView(filteredList);

            } catch (Exception e) {
                Log.e("JSON_FILE", "Fehler beim Verarbeiten der Datei: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Aktualisiert die RecyclerView mit den gefilterten Produkten.
     * @param filteredList Die gefilterte Liste von Produkten.
     *
     */
    private void updateRecyclerView(List<Product> filteredList) {
        // UI-Update muss auf dem Haupt-Thread erfolgen
        ((RecyclerView) recyclerView).post(() -> {
            productList.clear();
            productList.addAll(filteredList);
            adapter.notifyDataSetChanged();
        });
    }


}