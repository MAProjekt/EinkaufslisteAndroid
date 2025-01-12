package com.fhswf.einkaufslisteandroid.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

        productDataFetcher = new ProductDataFetcher(getContext(), view.findViewById(R.id.productRecyclerView));
        productDataFetcher.fetchProductData("Frucht Butter Milch Zitrone"); // Beispiel-Suchbegriff

        return view;
    }
}
