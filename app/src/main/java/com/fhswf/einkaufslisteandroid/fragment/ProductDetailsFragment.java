package com.fhswf.einkaufslisteandroid.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.fhswf.einkaufslisteandroid.R;
import com.fhswf.einkaufslisteandroid.datenpersistierung.JsonListManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ProductDetailsFragment extends DialogFragment {

    private static final String ARG_NAME = "product_name";
    private static final String ARG_IMAGE_URL = "image_url";
    private static final String ARG_INGREDIENTS = "ingredients";
    private static final String ARG_NUTRIMENTS = "nutriments";
    private static final String ARG_ALLERGENS = "allergens";
    private static final String ARG_STORE = "stores";

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

        // Views aus dem Layout referenzieren
        ImageView produktBildDetails = view.findViewById(R.id.produktBildDetails);
        TextView produktNameText = view.findViewById(R.id.produktNameText);
        TextView produktZutatenText = view.findViewById(R.id.produktZutatenText);
        TextView produktNutriText = view.findViewById(R.id.produktNutriText);
        TextView produktAllergeneText = view.findViewById(R.id.produktAllergeneText);
        TextView produktStoreText = view.findViewById(R.id.produktHerkunftText);
        Button produktHinzufuegenButton = view.findViewById(R.id.produktHinzufuegen);

        // Daten aus dem Bundle holen
        if (getArguments() != null) {
            produktNameText.setText(getArguments().getString(ARG_NAME, "Kein Name verfügbar"));
            String ingredientsJson = getArguments().getString(ARG_INGREDIENTS, "[]");
            produktZutatenText.setText("Zutaten: " + parseIngredients(ingredientsJson));
            produktNutriText.setText(getArguments().getString(ARG_NUTRIMENTS, "Keine Angaben"));
            produktAllergeneText.setText(getArguments().getString(ARG_ALLERGENS, "Keine Angaben"));
            produktStoreText.setText("Verfügbar bei: " + getArguments().getString(ARG_STORE, "Kein Laden verfügbar"));


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


    //Die ganzen Zutaten rausholen
    private String parseIngredients(String ingredientsJson) {
        StringBuilder builder = new StringBuilder();
        try {
            JSONArray ingredientsArray = new JSONArray(ingredientsJson);
            for (int i = 0; i < ingredientsArray.length(); i++) {
                JSONObject ingredient = ingredientsArray.getJSONObject(i);
                builder.append(ingredient.optString("text", "Unbekannt"));
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
