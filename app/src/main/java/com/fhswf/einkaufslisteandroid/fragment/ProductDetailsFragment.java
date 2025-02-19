package com.fhswf.einkaufslisteandroid.fragment;

import static androidx.core.util.TypedValueCompat.dpToPx;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import androidx.core.content.ContextCompat;



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
import java.util.Objects;

public class ProductDetailsFragment extends DialogFragment {

    private static final String ARG_NAME = "product_name";
    private static final String ARG_IMAGE_URL = "image_url";
    private static final String ARG_INGREDIENTS = "ingredients";
    private static final String ARG_NUTRIMENTS = "nutriments";
    private static final String ARG_ALLERGENS = "allergens_from_ingredients";
    private static final String ARG_STORE = "stores";


    private static final List<String> nutriListe = new ArrayList<>(Arrays.asList("calcium", "fat", "energy","energy-kcal",
            "energy-kj","proteins", "salt", "sugars", "sodium" ));


    /**
     * Zeigt das Product-Details-Fragment (Pop-Up-Fenster) an und lädt die Informationen des Produkts.
     *
     * @param inflater wandelt XML-Layout in UI-Element um, damit sie im Fragment angezeigt werden können.
     * @param container Ist das übergeordnete Layout, in das das Fragment eingefügt wird (z.B. einem Layout in einer Activity).
     * @param savedInstanceState Falls vorhanden, der zuvor gespeicherte Zustand des Fragments.
     *
     * @return Die erstellte View des Fragments.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_details, container, false);  //Um Layout zu laden und die entsprechenden Sachen anzuzeigen

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        ImageView produktBildDetails = view.findViewById(R.id.produktBildDetails);
        TextView produktNameText = view.findViewById(R.id.produktNameText);
        TextView produktZutatenText = view.findViewById(R.id.produktZutatenText);
        TableLayout nutriTabelle = view.findViewById(R.id.nutriTabelle);
        TextView produktAllergeneText = view.findViewById(R.id.produktAllergeneText);
        TextView produktStoreText = view.findViewById(R.id.produktHerkunftText);
        Button produktHinzufuegenButton = view.findViewById(R.id.produktHinzufuegen);
        EditText produktMenge = view.findViewById(R.id.productAmount);  //Hier initialisieren, sonst wird null beim Speichern in DB übergeben

        //mit getArguments() auf die Daten die im Bundle gespeichert sind zuzugreifen
        if (getArguments() != null) {
            produktNameText.setText(getArguments().getString(ARG_NAME, "Kein Name verfügbar"));  //holt sich den übergebenen Namen, der unter dem Key ARG_NAMe gespeichert ist
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

        Bundle args1 = getArguments();
        String productName = (args1 != null) ? args1.getString(ARG_NAME, "Kein Name") : null;
        String imageUrl = (args1 != null) ? args1.getString(ARG_IMAGE_URL, "") : null;



        //Button zum Hinzufügen des Produktes zu einer Liste
        Bundle args = getArguments();
        String listId = (args != null) ? args.getString("listId") : null;
        boolean fromUebersicht = (args != null) && args.getBoolean("fromUebersicht", false);
        Log.d("DEBUG", "Liste ID aus DetailsFragment: " + listId);
        Log.d("DEBUG", "ProductDetailsFragment - Name: " + productName + ", listId: " + listId + ", ImageURL: " + imageUrl);

        Button schliessenButton = new Button(requireContext());

        if (listId == null && !fromUebersicht) {
            produktHinzufuegenButton.setVisibility(View.GONE);
            produktMenge.setVisibility(View.GONE);
            schliessenButton.setText("Schließen");
            schliessenButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));
            schliessenButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.default_ThemeOverlay_AppCompat));
            schliessenButton.setOnClickListener(v -> dismiss());
            ((ViewGroup) view).addView(schliessenButton);
        }

        produktHinzufuegenButton.setOnClickListener(v -> {
            if (listId != null) {
                addProductToListById(listId, userId);
            } else {
                showSelectionDialog(userId);
            }
        });


        return view;
    }

    private int dpToPx(int dp) {
        return (int) (dp * requireContext().getResources().getDisplayMetrics().density);
    }


    /**
     * Bereinigt bei den Allergenen die Wörter mit "en:".
     * @param allergene Das zu übergebende Wort (allergen), welches bereinigt werden soll.
     * @return Gibt das bereinigte Wort zurück.
     */
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

    /**
     * Die Methode dient dazu, um nur die in der nutriListe definiereten Werte anzuzeigen.
     * @param nutri Der übergebene Nährwert.
     * @return true, wenn der übergebene Nährwert in der Liste vorhanden ist, false, wenn er nicht in der Liste vorhanden ist.
     */
    private boolean filterNutriWerte(String nutri){
        if(nutriListe.contains(nutri)){
            return true;
        }
        return false;
    }


    /**
     * Holt und formatiert die Zutatenliste aus einer JSON String.
     * @param ingredientsJson Json String der Zutaten.
     * @return Gibt einen formatierten und mit Komma getrennten String zurück, welcher die Zutaten enthält.
     */
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
     * Öffnet ein Dialog-Fenster, um die Liste auswählen, in der das Produkt hinzugefügt werden soll.
     *
     * @param userId User id, damit die von ihm zuvor erstellten Einkaufslisten angezeigt werden
     */
    private void showSelectionDialog(String userId) {
        FirestoreManager firestoreManager = new FirestoreManager();

        firestoreManager.getLists(userId,
                documents -> {  //verkürzte Schreibweise des onSuccessListeners
                    // Namen der Einkaufslisten extrahieren
                    List<String> listNamesSingle = new ArrayList<>();
                    List<String> listNamesGruppe = new ArrayList<>();
                    for (DocumentSnapshot document : documents) {
                        String listName = document.getString("name");
                        List<String> members = (List<String>) document.get("members");
                        if(members.size() > 1){
                            listNamesGruppe.add(listName + " (Gruppe)");
                        } else {
                            listNamesSingle.add(listName);
                        }
                    }

                    List<String> gemeinsameListe = new ArrayList<>();
                    gemeinsameListe.addAll(listNamesSingle);
                    gemeinsameListe.addAll(listNamesGruppe);

                    // Alert / PopUp Fenster erstellen
                    if (!gemeinsameListe.isEmpty()) {

                        new AlertDialog.Builder(requireContext())
                                .setTitle("Liste auswählen")
                                .setItems(gemeinsameListe.toArray(new String[0]), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String selectedList = gemeinsameListe.get(which); // gewählte Liste
                                        selectedList = selectedList.replace(" (Gruppe)",""); //Um Liste finden zu können
                                        addProductToListByName(selectedList, userId);
                                    }
                                })
                                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    } else {
                        Toast.makeText(getContext(), "Keine Listen gefunden", Toast.LENGTH_SHORT).show();
                    }
                },
                //e-> kürzere Schreibweise des onFailureListeners
                e -> Toast.makeText(getContext(), "Fehler beim Laden der Listen: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }


    /**
     * Fügt ein Produkt anhand der List-ID der entsprechenden Einkaufsliste hinzu.
     * @param listId ID der Einkaufsliste.
     * @param userId
     */
    private void addProductToListById(String listId, String userId) {
        FirestoreManager firestoreManager = new FirestoreManager();
        String productName = getArguments().getString(ARG_NAME, "Unbekannt");
        String imageUrl = getArguments().getString(ARG_IMAGE_URL, "");
        String store = getArguments().getString(ARG_STORE, "Kein Laden verfügbar");
        String zutaten = getArguments().getString(ARG_INGREDIENTS, "Keine Zutaten verfügbar");
        String nutriments = getArguments().getString(ARG_NUTRIMENTS, "Keine Nährwerte verfügbar");
        String allergene = getArguments().getString(ARG_ALLERGENS, "Keine Allergene gefunden!");

        EditText editText = requireView().findViewById(R.id.productAmount);
        String productMenge = editText.getText().toString();

        Log.e("Menge", productMenge);
        Product neuesProduct = new Product(productName, imageUrl, zutaten, nutriments, store, allergene, false, productMenge);

        firestoreManager.addProductToList(listId, getContext(), neuesProduct,
                aVoid -> {Toast.makeText(getContext(), "Produkt hinzugefügt", Toast.LENGTH_SHORT).show();
                          dismiss();
                },
                e -> Toast.makeText(getContext(), "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Fügt ein Produkt anhand des Namens der entsprechenden Einkaufsliste hinzu.
     * @param selectedList Name der gewählten Liste.
     * @param userId Die ID des aktuellen Benutzers.
     */
    private void addProductToListByName(String selectedList, String userId) {
        FirestoreManager firestoreManager = new FirestoreManager();

        firestoreManager.getListIdByName(selectedList, listId -> {
            addProductToListById(listId, userId);
        }, e -> Toast.makeText(getContext(), "Fehler: Liste nicht gefunden", Toast.LENGTH_SHORT).show());
    }



}
