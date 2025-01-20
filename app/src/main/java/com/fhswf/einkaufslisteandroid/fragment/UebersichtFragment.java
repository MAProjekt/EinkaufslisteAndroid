package com.fhswf.einkaufslisteandroid.fragment;

import android.os.Bundle;
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

    private ProductDataFetcher productDataFetcher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_uebersicht, container, false);

        //productDataFetcher = new ProductDataFetcher(getContext(), view.findViewById(R.id.productRecyclerView));

        EditText eingabeProdukt = view.findViewById(R.id.eingabeProduktEditText);

        eingabeProdukt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                    productDataFetcher = new ProductDataFetcher(getContext(), view.findViewById(R.id.productRecyclerView));
                    String productQuery = eingabeProdukt.getText().toString().trim();
                    if (!productQuery.isEmpty()) {
                        productDataFetcher.fetchProductData(productQuery); // Ausführen, wenn Enter gedrückt wird
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
}
