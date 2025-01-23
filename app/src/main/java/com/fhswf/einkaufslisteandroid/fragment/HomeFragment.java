package com.fhswf.einkaufslisteandroid.fragment;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

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
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
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


    }



}