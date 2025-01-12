package com.fhswf.einkaufslisteandroid.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.fhswf.einkaufslisteandroid.R;

public class ProductDetailsFragment extends DialogFragment {

    private static final String ARG_NAME = "product_name";
    private static final String ARG_IMAGE_URL = "image_url";
    private static final String ARG_INGREDIENTS = "ingredients";
    private static final String ARG_NUTRIMENTS = "nutriments";
    private static final String ARG_ALLERGENS = "allergens";
    private static final String ARG_COUNTRY = "country";

    // Factory-Methode zum Erstellen einer neuen Fragment-Instanz mit Daten
    public static ProductDetailsFragment newInstance(String name, String imageUrl, String ingredients, String nutriments, String allergens, String country) {
        ProductDetailsFragment fragment = new ProductDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_IMAGE_URL, imageUrl);
        args.putString(ARG_INGREDIENTS, ingredients);
        args.putString(ARG_NUTRIMENTS, nutriments);
        args.putString(ARG_ALLERGENS, allergens);
        args.putString(ARG_COUNTRY, country);
        fragment.setArguments(args);
        return fragment;
    }

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
        TextView produktHerkunftText = view.findViewById(R.id.produktHerkunftText);

        // Daten aus dem Bundle holen
        if (getArguments() != null) {
            produktNameText.setText(getArguments().getString(ARG_NAME, "Kein Name verfügbar"));
            produktZutatenText.setText("Zutaten: " + getArguments().getString(ARG_INGREDIENTS, "Keine Angaben"));
            produktNutriText.setText("Nährwerte: " + getArguments().getString(ARG_NUTRIMENTS, "Keine Angaben"));
            produktAllergeneText.setText("Allergene: " + getArguments().getString(ARG_ALLERGENS, "Keine Angaben"));
            produktHerkunftText.setText("Herkunft: " + getArguments().getString(ARG_COUNTRY, "Unbekannt"));

            // Bild laden mit Glide
            String imageUrl = getArguments().getString(ARG_IMAGE_URL);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(requireContext()).load(imageUrl).into(produktBildDetails);
            }
        }

        return view;
    }
}
