package com.fhswf.einkaufslisteandroid.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.fhswf.einkaufslisteandroid.R;
import com.fhswf.einkaufslisteandroid.logic.ProductDataFetcher;

public class UebersichtFragment extends Fragment {

    private static final String SHARED_PREFS_NAME = "sharedPrefs";
    private static final String LAST_SEARCH_TERM_KEY = "lastSearchTerm";

    private ProductDataFetcher productDataFetcher;
    private SharedPreferences sharedPreferences;  //Um letzte Suche zu speichern
    private EditText eingabeProdukt;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_uebersicht, container, false);

        Bundle args = getArguments();
        String listId = (args != null) ? args.getString("listId") : null;

        Log.d("DEBUG", "Liste ID aus ÜbersichtFragment: " + listId);
        eingabeProdukt = view.findViewById(R.id.eingabeProduktEditText);
        RecyclerView recyclerView = view.findViewById(R.id.productRecyclerView);
        productDataFetcher = new ProductDataFetcher(getContext(), recyclerView, listId);


        loadLastSearch(); //letzte Suche laden

        eingabeProdukt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String productQuery = eingabeProdukt.getText().toString().trim();
                    if (!productQuery.isEmpty()) {
                        // Suchergebnisse laden
                        productDataFetcher.fetchProductData(productQuery);
                        // Suchbegriff speichern
                        saveLastSearch(productQuery);
                    } else {
                        Toast.makeText(getContext(), "Bitte einen Suchbegriff eingeben", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    private void saveLastSearch(String searchTerm) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LAST_SEARCH_TERM_KEY, searchTerm);
        editor.apply();
    }

    private void loadLastSearch() {
        String lastSearchTerm = sharedPreferences.getString(LAST_SEARCH_TERM_KEY, "");
        if (!lastSearchTerm.isEmpty()) {
            eingabeProdukt.setText(lastSearchTerm);
            productDataFetcher.fetchProductData(lastSearchTerm);  //letzte Suchergebnisse laden
        }
    }
}