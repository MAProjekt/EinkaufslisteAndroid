package com.fhswf.einkaufslisteandroid.fragment;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fhswf.einkaufslisteandroid.R;
import com.fhswf.einkaufslisteandroid.datenpersistierung.FirestoreManager;
import com.fhswf.einkaufslisteandroid.logic.ListAdapter;
import com.fhswf.einkaufslisteandroid.logic.ProductAdapter;
import com.fhswf.einkaufslisteandroid.models.Product;
import com.fhswf.einkaufslisteandroid.models.ProductList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 *Zeigt die Einkaufslisten an, welche die Produkte speichern, zudem kann man auf die Einkaufsliste klicken,
 * um die Produkte anzuzeigen.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_PARAM1 = "param1"; // vordef. Parameter
    private static final String ARG_PARAM2 = "param2";// vordef. Parameter

    // TODO: Rename and change types of parameters
    private String mParam1; // vordef. Parameter
    private String mParam2; // vordef. Parameter

    public HomeFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /**
     * Methode wurde ergänzt, dient als Gegenstück liest die JSON aus in eine Liste der Produkte
     * und listet die Listen dann in der App auf.
     *
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.ViewLists);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirestoreManager firestoreManager = new FirestoreManager();

        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            firestoreManager.getLists(userId, new FirestoreManager.FirestoreCallbackList() {  //Lädt die Einkaufslisten aus der Datenbank
                @Override
                public void onSuccess(List<DocumentSnapshot> documents) {
                    List<String> listNames = new ArrayList<>();
                    for (DocumentSnapshot doc : documents) {
                        listNames.add(doc.getString("name"));  //Zeigt die
                    }
                    ListAdapter adapter = new ListAdapter(listNames, HomeFragment.this::onEinkaufsListClicked);  //Wenn Liste geklickt wird, wird die Methode aufgerufen
                    recyclerView.setAdapter(adapter);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }

        return view;
    }

    /**
     * Hilfsmethode für die onCreateView + sollte noch ausgelagert werden
     * @return
     */

    /**
     * Methode wenn eine Liste angeklickt wird.
     * @param listName
     */
    private void onEinkaufsListClicked(String listName) {
        Toast.makeText(getContext(), "Liste ausgewählt: " + listName, Toast.LENGTH_SHORT).show();
        //Hier Produkte anzeigen lassen
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Produkte aus Firestore abrufen
        db.collection("users").document(userId).collection("lists").document(listName)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    ProductList productList = documentSnapshot.toObject(ProductList.class);
                    if (productList != null){
                        List<Product> products = productList.getProducts();
                        // Dialog anzeigen
                        showProductsDialog(listName, products);
                    }else {
                        Toast.makeText(getContext(), "Keine Produkte gefunden!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Fehler beim Abrufen: " + e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    private void showProductsDialog(String listName, List<Product> products) {
        RecyclerView recyclerView = new RecyclerView(requireContext());  //Scroll Liste initialisieren
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        //requireContext() um sicherzustellen einen gültigen Kontext zu bekommen der nicht null ist
        //req.Cont: notwendig um Ressourcen wie Layouts etc. laden zu können
        ProductAdapter adapter = new ProductAdapter(requireContext(), products);  //Im Adapter ist auch das Anzeigen des Popup-Fenster bei Klick auf Produkt enthalten
        recyclerView.setAdapter(adapter);

        //Erstellt Dialog fenster
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Produkte in der Liste: " + listName)
                .setView(recyclerView) // RecyclerView in den Dialog einfügen
                .setPositiveButton("Schließen", (dialog, which) -> dialog.dismiss())
                .show();
    }




}