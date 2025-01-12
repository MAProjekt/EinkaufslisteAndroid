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
        String url = "https://de.openfoodfacts.org/cgi/search.pl?search_terms=" + searchTerm + "&json=1";

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray products = response.getJSONArray("products");
                            for (int i = 0; i < products.length(); i++) {
                                JSONObject product = products.getJSONObject(i);
                                String name = product.optString("product_name", "Unbekannt");
                                String brand = product.optString("brands", "Unbekannt");
                                String imageUrl = product.optString("image_url", "");

                                productList.add(new Product(name, imageUrl, brand));
                            }
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Log.e("API_TEST", "Fehler beim Verarbeiten der API-Antwort: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API_TEST", "Fehler bei der API-Anfrage: " + error.getMessage());
                    }
                }
        );

        queue.add(jsonObjectRequest);
    }

    public void seeProductDetailsAndAddToList(Product product) {

    }
}
