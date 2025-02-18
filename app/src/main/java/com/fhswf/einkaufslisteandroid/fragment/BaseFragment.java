package com.fhswf.einkaufslisteandroid.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fhswf.einkaufslisteandroid.R;
import com.fhswf.einkaufslisteandroid.datenpersistierung.FirestoreManager;
import com.fhswf.einkaufslisteandroid.logic.ListAdapter;
import com.fhswf.einkaufslisteandroid.models.Product;
import com.fhswf.einkaufslisteandroid.models.ProductList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Klasse um Gemeinsamkeiten des Home- und GroupFragments auszulagern
 */
public abstract class BaseFragment extends Fragment {
    // FirestoreManager zur Verwaltung der Datenbankoperationen mit Firestore
    protected FirestoreManager firestoreManager;


    /**
     * Wird beim Erstellen des Fragments aufgerufen. Hier wird der FirestoreManager initialisiert,
     * um später Datenbankabfragen durchführen zu können.
     *
     * @param savedInstanceState Falls vorhanden, der zuvor gespeicherte Zustand des Fragments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestoreManager = new FirestoreManager();
    }

    // Abstrakte Methoden, die von den abgeleiteten Klassen implementiert werden müssen.
    protected abstract int getLayoutResourceId();
    protected abstract boolean isGroupFragment();
    protected abstract void showProductsDialog(String listId, String listName, List<Product> products);

    /**
     * Wird aufgerufen, um die Benutzeroberfläche des Fragments zu erstellen.
     * Hier wird das Layout inflatiert, die RecyclerView initialisiert und die Listen
     * (Benutzer- oder Gruppenlisten) aus Firestore geladen.
     *
     * @param inflater LayoutInflater zum Erzeugen der View.
     * @param container Container in den das Fragment eingefügt wird.
     * @param savedInstanceState Falls vorhanden, der zuvor gespeicherte Zustand des Fragments,
     *                           damit das ganze nicht erneut geladen werden muss.
     * @return Die erstellte View des Fragments.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResourceId(), container, false);
        RecyclerView recyclerView = view.findViewById(R.id.ViewLists);
        if (recyclerView == null) {
            recyclerView = view.findViewById(R.id.ViewLists); // Fallback für GroupsFragment
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            RecyclerView finalRecyclerView = recyclerView;
            firestoreManager.getUserOrGroupLists(userId, isGroupFragment(),
                    documents -> {
                        List<String> listNames = new ArrayList<>();
                        for (DocumentSnapshot doc : documents) {
                            String listName = doc.getString("name");
                            if (listName != null) {
                                listNames.add(listName);
                            }
                        }
                        ListAdapter adapter = new ListAdapter(listNames, this::onEinkaufsListClicked);
                        finalRecyclerView.setAdapter(adapter);
                    },
                    e -> Toast.makeText(getContext(), "Fehler beim Laden der Listen: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }
        return view;
    }

    /**
     * Erstellt ein Dialog-Fenster um einen Benutzer hinzuzufügen.
     * Wird in showProductsDialog aufgerufen.
     * @param listId Id der Liste, zu dem der Benutzer hinzugefügt werden soll.
     */
    protected void showAddUser(String listId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Benutzer zur Liste hinzufügen");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        EditText emailInput = new EditText(requireContext());
        emailInput.setHint("E-Mail des Benutzers eingeben");
        layout.addView(emailInput);

        builder.setView(layout);

        builder.setPositiveButton("Hinzufügen", (dialog, which) -> {
            String email = emailInput.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(getContext(), "E-Mail darf nicht leer sein!", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("benutzer")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String userId = queryDocumentSnapshots.getDocuments().get(0).getId();

                            firestoreManager.addUserToList(listId, userId,
                                    aVoid -> {
                                        Toast.makeText(getContext(), "Benutzer hinzugefügt!", Toast.LENGTH_SHORT).show();
                                        refreshFragment();
                                    },
                                    e -> Toast.makeText(getContext(), "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                        } else {
                            Toast.makeText(getContext(), "Benutzer nicht gefunden!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Fehler beim Suchen der E-Mail!", Toast.LENGTH_SHORT).show();
                    });
        });

        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /**
     * Methode um den Nutzer auf die jeweiligen Listen klicken zu können, bzw. die Liste und ihre
     * Inhalte anzeigen zu lassen.
     * @param listName Name der Liste die ausgewählt wurde.
     */
    protected void onEinkaufsListClicked(String listName) {
        Toast.makeText(getContext(), "Liste ausgewählt: " + listName, Toast.LENGTH_SHORT).show();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        firestoreManager.getListIdByName(listName, listId -> {
            db.collection("lists").document(listId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        ProductList productList = documentSnapshot.toObject(ProductList.class);
                        if (productList != null) {
                            List<Product> products = productList.getProducts();
                            showProductsDialog(listId, listName, products);
                        } else {
                            Toast.makeText(getContext(), "Keine Produkte gefunden!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }, e -> Toast.makeText(getContext(), "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Methode um die Progress-Bar zu aktualisieren
     */
    public void updateProgressBar() {
        View view = getView();
        Fragment currentFragment = this;
        RecyclerView recyclerView = null;

        if(currentFragment instanceof HomeFragment){
            recyclerView = view.findViewById(R.id.ViewLists);
            recyclerView.getAdapter().notifyDataSetChanged();
        }else if(currentFragment instanceof GroupsFragment){
            recyclerView = view.findViewById(R.id.ViewLists);
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    /**
     * Methode um das gerade ausgewählte Fragment zu aktualisieren.
     */
    public void refreshFragment() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        Fragment currentFragment = this;

        if (currentFragment instanceof HomeFragment) {  //Abfrage ob der aktuelle Fragment ein HomeFragment oder GroupFragment ist
            transaction.replace(R.id.fragment_container_view_tag, new HomeFragment());
        } else if (currentFragment instanceof GroupsFragment) {
            transaction.replace(R.id.fragment_container_view_tag, new GroupsFragment());
        }

        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Methode um ein Bestätigungsfenster zum Bestätigen vor dem Löschen einer Liste.
     * @param listId die Id der zu löschenden Liste.
     * @param listName Name der zu löschenden Liste für den Dialog.
     */
    protected void deleteListBestaetigen(String listId, String listName) {
        AlertDialog.Builder confirmDialog = new AlertDialog.Builder(requireContext());
        confirmDialog.setTitle("Willst du die Liste \"" + listName + "\" wirklich löschen?")
                .setPositiveButton("Ja", (dialog, which) -> {
                    firestoreManager.deleteList(listId, aVoid -> {
                        Toast.makeText(getContext(), "Liste gelöscht!", Toast.LENGTH_SHORT).show();
                        refreshFragment();
                    }, e -> Toast.makeText(getContext(), "Fehler" + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Nein", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     *
     * @param listId Id der Liste in denen das Produkt das man hinzufügen möchte hinzugefügt wird.
     */
    public void openProdukte(String listId) {
        UebersichtFragment fragment = new UebersichtFragment();
        Bundle args = new Bundle();
        args.putString("listId", listId); // listId übergeben
        fragment.setArguments(args);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_view_tag, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }




}
