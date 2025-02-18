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

    // Name der SharedPreferences-Datei.
    private static final String SHARED_PREFS_NAME = "sharedPrefs";
    // Schlüssel für den letzten Suchbegriff in den SharedPreferences.
    private static final String LAST_SEARCH_TERM_KEY = "lastSearchTerm";

    // Objekt zur Datenabfrage für Produkte.
    private ProductDataFetcher productDataFetcher;
    // SharedPreferences zum Speichern und Laden des letzten Suchbegriffs.
    private SharedPreferences sharedPreferences;  //Um letzte Suche zu speichern
    // Eingabefeld, in das der Suchbegriff eingegeben wird.
    private EditText eingabeProdukt;

    /**
     * Wird bevor das Fragment-Layout erstellt wird aufgerufen. Hier werden die SharedPreferences
     * initialisiert, um den letzten Suchbegriff zu speichern.
     * @param savedInstanceState Falls vorhanden, der zuvor gespeicherte Zustand des Fragments.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * erstellt und gibt die View für dieses Fragment zurück.
     * Hier wird das Layout inflatiert, die UI-Elemente (wie EditText und RecyclerView) initialisiert
     * und der ProductDataFetcher eingerichtet.
     * @param inflater LayoutInflater zum Erzeugen der View aus XML.
     * @param container Container, in dem das Fragment angezeigt wird.
     * @param savedInstanceState Falls vorhanden, der zuvor gespeicherte Zustand des Fragments.
     *
     * @return Die erstellte View des Fragments.
     */
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

    /**
     * Speichert den aktuellen Suchbegriff in den SharedPreferences.
     *
     * @param searchTerm Der Suchbegriff, der gespeichert werden soll.
     */
    private void saveLastSearch(String searchTerm) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LAST_SEARCH_TERM_KEY, searchTerm);
        editor.apply();
    }

    /**
     * Lädt den zuletzt gespeicherten Suchbegriff und führt eine Produktsuche aus, um die
     * Ergebnisse direkt beim Öffnen des Fragments anzuzeigen.
     */
    private void loadLastSearch() {
        String lastSearchTerm = sharedPreferences.getString(LAST_SEARCH_TERM_KEY, "");
        if (!lastSearchTerm.isEmpty()) {
            eingabeProdukt.setText(lastSearchTerm);
            productDataFetcher.fetchProductData(lastSearchTerm);  //letzte Suchergebnisse laden
        }
    }
}