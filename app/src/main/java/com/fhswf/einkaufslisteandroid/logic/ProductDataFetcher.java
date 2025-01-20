package com.fhswf.einkaufslisteandroid.logic;

import android.content.Context;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fhswf.einkaufslisteandroid.models.Product;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ProductDataFetcher {

    private Context context;
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;

    public ProductDataFetcher(Context context, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.productList = new ArrayList<>();
        this.adapter = new ProductAdapter(context, productList);

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
    }


    public void fetchProductData(String searchTerm) {
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

            productList.clear(); // Liste zurücksetzen

            // Durch das Haupt-Array iterieren
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                // Überprüfen, ob der Schlüssel "products" existiert
                if (jsonObject.has("products")) {
                    JSONArray products = jsonObject.getJSONArray("products");

                    // Produkte durchsuchen
                    for (int j = 0; j < products.length(); j++) {
                        JSONObject product = products.getJSONObject(j);
                        String name = product.optString("product_name", "Unbekannt");

                        // Filter basierend auf dem Suchbegriff
                        if (name.toLowerCase().contains(searchTerm.toLowerCase())) {
                            String brand = product.optString("brands", "Unbekannt");
                            String imageUrl = product.optString("image_url", "");
                            String store = product.optString("stores", "Kein Laden verfügbar");
                            String nutrients = product.optString("nutriments", "Keine Nährwerte");
                            String zutaten = product.optString("ingredients_text", "Keine Zutaten");
                            String allergene = product.optString("allergens_from_ingredients", "Keine Allergene");

                            // Produkt zur Liste hinzufügen
                            productList.add(new Product(name, imageUrl, brand, store, nutrients, zutaten, allergene));
                        }
                    }
                }
            }

            // RecyclerView-Adapter aktualisieren
            adapter.notifyDataSetChanged();
            Log.d("RECYCLER_VIEW", "Adapter aktualisiert. Anzahl Produkte: " + productList.size());

        } catch (Exception e) {
            Log.e("JSON_FILE", "Fehler beim Verarbeiten der Datei: " + e.getMessage());
            e.printStackTrace();
        }
    }




}
