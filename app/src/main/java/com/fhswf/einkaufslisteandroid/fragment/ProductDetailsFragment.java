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
import com.fhswf.einkaufslisteandroid.datenpersistierung.FirestoreManager;
import com.fhswf.einkaufslisteandroid.models.Product;
import com.fhswf.einkaufslisteandroid.models.ProductList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    //Zeigt im PopUp-Fnester die gefetchten Daten an
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_details, container, false);  //Um Layout zu laden und die entsprechenden Sachen anzuzeigen

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

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


        //Button zum Hinzufügen des Produktes zu einer Liste
        produktHinzufuegenButton.setOnClickListener(v -> {

            showSelectionDialog(userId);
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

    /**
     * Diese Funktion erstellt eine Tabelle für die Nährwerte und bearbeitet die
     * vorhandenen Nährwerte des Produkts.
     *
     * @param table Die Tabelle in der die Nährwerte angezeigt werden sollen
     * @param nutri Der Nährwert welcher bearbeitet und angezeigt wird
     */
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

    /**
     *
     *
     * @param userId User id, damit die von ihm zuvor erstellten Einkaufslisten angezeigt werden
     */
    private void showSelectionDialog(String userId) {
        FirestoreManager firestoreManager = new FirestoreManager();

        // Einkaufslisten mit FirestoreManager laden
        firestoreManager.getLists(userId, new FirestoreManager.FirestoreCallbackList() {
            @Override
            public void onSuccess(List<DocumentSnapshot> documents) { //Hier die Listen (Einkaufslisten)
                // Namen der Einkaufslisten extrahieren
                List<String> listNames = new ArrayList<>();
                for (DocumentSnapshot document : documents) {
                    String listName = document.getString("name");
                    listNames.add(listName);
                }

                // AlertDialog erstellen und anzeigen
                if (!listNames.isEmpty()) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Liste auswählen")
                            .setItems(listNames.toArray(new String[0]), (dialog, which) -> {
                                String selectedList = listNames.get(which); // Gewählte Liste
                                addProductToList(selectedList, userId); // Produkt zur Liste hinzufügen
                            })
                            .setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss())
                            .show();
                } else {
                    Toast.makeText(getContext(), "Keine Listen gefunden", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(getContext(), "Fehler beim Laden der Listen: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addProductToList(String selectedList, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String productName = getArguments().getString(ARG_NAME, "Unbekannt");
        String imageUrl = getArguments().getString(ARG_IMAGE_URL, "");
        String store = getArguments().getString(ARG_STORE, "Kein Laden verfügbar");
        String zutaten = getArguments().getString(ARG_INGREDIENTS, "Keine Zutaten verfügbar");
        String nutriments = getArguments().getString(ARG_NUTRIMENTS, "Keine Nährwerte verfügbar");
        String allergene = getArguments().getString(ARG_ALLERGENS, "Keine Allergene gefunden!");

        Product neuesProduct = new Product(productName, imageUrl, zutaten, nutriments, store, allergene);  //anderen Konstruktor

        // Liste aus Firestore holen
        db.collection("users").document(userId).collection("lists").document(selectedList)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    ProductList productList = documentSnapshot.toObject(ProductList.class);//Inhalt der Einkaufsliste bzw. Einkaufslisten initialisierne
                    if (productList == null) {
                        Toast.makeText(getContext(), "Liste existiert nicht!" + selectedList, Toast.LENGTH_SHORT).show();
                    }
                    List<Product> products = productList.getProducts();
                    products.add(neuesProduct);

                    System.out.println(neuesProduct.getImageURL());
                    // Liste speichern
                    db.collection("users").document(userId).collection("lists").document(selectedList)
                            .update("products", products)
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Produkt hinzugefügt", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Fehler beim Hinzufügen: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Fehler beim Laden der Liste: " + e.getMessage(), Toast.LENGTH_SHORT).show());

    }


}
