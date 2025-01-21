package com.fhswf.einkaufslisteandroid.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.fhswf.einkaufslisteandroid.R;
import com.fhswf.einkaufslisteandroid.datenpersistierung.JsonListManager;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ProductDetailsFragment extends DialogFragment {

    private static final String ARG_NAME = "product_name";
    private static final String ARG_IMAGE_URL = "image_url";
    private static final String ARG_INGREDIENTS = "ingredients";
    private static final String ARG_NUTRIMENTS = "nutriments";
    private static final String ARG_ALLERGENS = "allergens_from_ingredients";
    private static final String ARG_STORE = "stores";
    private static final List<String> nutriListe = new ArrayList<>(Arrays.asList("calcium", "fat", "energy","energy-kcal",
            "energy-kj","proteins", "salt", "sugars", "sodium" ));

    // Factory-Methode zum Erstellen einer neuen Fragment-Instanz mit Daten
    public static ProductDetailsFragment newInstance(String name, String imageUrl, String ingredients, String nutriments, String allergens, String store) {
        ProductDetailsFragment fragment = new ProductDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_IMAGE_URL, imageUrl);
        args.putString(ARG_INGREDIENTS, ingredients);
        args.putString(ARG_NUTRIMENTS, nutriments);
        args.putString(ARG_ALLERGENS, allergens);
        args.putString(ARG_STORE, store);
        fragment.setArguments(args);
        return fragment;
    }

    //TODO: getArguments() anschauen und verstehen, wie greift man überhaupt auf die Daten eines Produktes zu
    //Zeigt die gefetchten Daten an
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_details, container, false);

        // Views aus dem Layout
        ImageView produktBildDetails = view.findViewById(R.id.produktBildDetails);
        TextView produktNameText = view.findViewById(R.id.produktNameText);
        TextView produktZutatenText = view.findViewById(R.id.produktZutatenText);
        TableLayout nutriTabelle = view.findViewById(R.id.nutriTabelle);
        TextView produktAllergeneText = view.findViewById(R.id.produktAllergeneText);
        TextView produktStoreText = view.findViewById(R.id.produktHerkunftText);
        Button produktHinzufuegenButton = view.findViewById(R.id.produktHinzufuegen);

        // Daten aus dem Bundle holen
        if (getArguments() != null) {
            produktNameText.setText(getArguments().getString(ARG_NAME, "Kein Name verfügbar"));
            String ingredientsJson = getArguments().getString(ARG_INGREDIENTS, "Keine Zutaten verfügbar");
            produktZutatenText.setText("Zutaten: " + jsonZutaten(ingredientsJson));
            produktAllergeneText.setText("Allergene: " + cleanAllergene(getArguments().getString(ARG_ALLERGENS, "Keine Angaben")));
            produktStoreText.setText("Verfügbar bei: " + getArguments().getString(ARG_STORE, "Kein Laden verfügbar"));

            //Nährwerte auslesen
            String nutrimentsJson = getArguments().getString(ARG_NUTRIMENTS, "Keine Nährwerte gefunden");
            try {
                JSONObject nutri = new JSONObject(nutrimentsJson);
                nutriWerte(nutriTabelle, nutri);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            // Bild laden mit Glide
            String imageUrl = getArguments().getString(ARG_IMAGE_URL);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(requireContext()).load(imageUrl).into(produktBildDetails);
            }
        }

        produktHinzufuegenButton.setOnClickListener(v -> {

            showSelectionDialog();
        });

        return view;
    }

    //Eine Funktion, um bei den Allergenen alle Wörter mit "en:" zu entfernen
    private String cleanAllergene(String allergene){
        String cleanA = allergene.replaceAll(",?\\s*en:[^,]*", "").trim();

        // Wenn am Anfang des Satzes ein Wort mit en: entfernt wird, dann würde der Satz mit einem Komma anfangen
        // Um das zu verhindern wird erste Komma entfernt
        return cleanA.replaceFirst("^,\\s*", "");
    }

    private void nutriWerte(TableLayout table, JSONObject nutri){
        Iterator<String> keys = nutri.keys();

        while (keys.hasNext()){
            String key = keys.next();

            //Nur Nährwerte für 100g
            if(key.endsWith("_100g")){
                String nutrientKey = key.replace("_100g", "");  // Z.B. "calcium"
                String value = nutri.optString(key, "-");
                String unit = nutri.optString(nutrientKey + "_unit", ""); //gibt Einheit

                TableRow row = new TableRow(getContext());

                //Damit nur die wichtigsten Nährwerte angezeigt werden
                if (!filterNutriWerte(nutrientKey)) {
                    continue;
                }

                TextView nameTextView = new TextView(getContext());
                nameTextView.setText(nutrientKey);
                nameTextView.setPadding(8, 8, 8, 8);

                TextView valueTextView = new TextView(getContext());
                valueTextView.setText(value);
                valueTextView.setPadding(8, 8, 8, 8);

                TextView unitTextView = new TextView(getContext());
                unitTextView.setText(unit);
                unitTextView.setPadding(8, 8, 8, 8);

                // Zeile zur Tabelle hinzufügen
                row.addView(nameTextView);
                row.addView(valueTextView);
                row.addView(unitTextView);

                table.addView(row);
            }
        }
    }
    //Nur die wichtigsten Nährwerte anzeigen
    //TODO: Vielleicht eine Map zurückgeben mit einem String und bool als Paar, sodass die Nähwerte in deutsch übersetzt werden
    //Tipp: vllt ein switch case für das übesetzen?
    private boolean filterNutriWerte(String nutri){
        if(nutriListe.contains(nutri)){
            return true;
        }
        return false;
    }


    //Die ganzen Zutaten, Nährwerte rausholen
    private String jsonZutaten(String ingredientsJson) {
        StringBuilder builder = new StringBuilder();
        try {
            JSONArray ingredientsArray = new JSONArray(ingredientsJson);
            for (int i = 0; i < ingredientsArray.length(); i++) {
                JSONObject ingredient = ingredientsArray.getJSONObject(i);
                builder.append(ingredient.optString("text", "Unbekannt").replace("_", ""));  //Holt String aus JsonObject und entfernt das _
                if (i < ingredientsArray.length() - 1) {
                    builder.append(", ");
                }
            }
        } catch (JSONException e) {
            builder.append("Fehler beim Laden der Zutaten.");
        }
        return builder.toString();
    }

    private void showSelectionDialog() {
        List<String> listNames = JsonListManager.loadListsFromJSON(getContext()); // Deine Listen aus JSON laden

        // Dialog mit einer Liste von Listen anzeigen
        new AlertDialog.Builder(requireContext())
                .setTitle("Liste auswählen")
                .setItems(listNames.toArray(new String[0]), (dialog, which) -> {
                    String selectedList = listNames.get(which);
                    Toast.makeText(getContext(), "Produkt wird in " + selectedList + " gespeichert!", Toast.LENGTH_SHORT).show();
                    addProductToList(selectedList);
                })
                .show();
    }

    //TODO: Funktioniert noch nicht wirklich, wegen deN Eigenschaften des JSON Objekts
    private void addProductToList(String selectedList) {
        File file = new File(requireContext().getFilesDir(), "listen.json");

        if (file.exists()) {
            try {
                // Lese den Inhalt der Datei als String
                String content = new String(Files.readAllBytes(file.toPath()));
                JSONArray listsArray = new JSONArray(content);

                // Durchsuche alle Listen, um die richtige zu finden
                for (int i = 0; i < listsArray.length(); i++) {
                    JSONObject listObject = listsArray.getJSONObject(i);
                    if (listObject.getString("listName").equals(selectedList)) {
                        // Produktdetails holen
                        String produktName = getArguments().getString(ARG_NAME);
                        String produktZutaten = getArguments().getString(ARG_INGREDIENTS);
                        String produktNutri = getArguments().getString(ARG_NUTRIMENTS);
                        String produktAllergene = getArguments().getString(ARG_ALLERGENS);
                        String produktStore = getArguments().getString(ARG_STORE);

                        // Erstelle ein neues Produkt-JSON-Objekt
                        JSONObject productObject = new JSONObject();
                        productObject.put("name", produktName);
                        productObject.put("ingredients", produktZutaten);
                        productObject.put("nutriments", produktNutri);
                        productObject.put("allergens", produktAllergene);
                        productObject.put("stores", produktStore);

                        // Füge das Produkt zur Liste hinzu
                        JSONArray productsArray = listObject.getJSONArray("products");
                        productsArray.put(productObject);

                        // Liste wieder in die JSON-Datei schreiben
                        FileWriter writer = new FileWriter(file);
                        writer.write(listsArray.toString());
                        writer.close();

                        Toast.makeText(getContext(), "Produkt wird zu " + selectedList + " hinzugefügt!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
