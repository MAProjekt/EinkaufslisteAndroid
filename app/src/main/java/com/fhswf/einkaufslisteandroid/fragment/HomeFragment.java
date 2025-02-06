package com.fhswf.einkaufslisteandroid.fragment;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
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

    private FirestoreManager firestoreManager;

    private String currentListId;


    public HomeFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestoreManager = new FirestoreManager();
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
            firestoreManager.getUserOrGroupLists(userId, false,
                    documents -> {   // documents ist eine Liste von DocumentSnapshots
                        List<String> listNames = new ArrayList<>();
                        for (DocumentSnapshot doc : documents) {
                            String listName = doc.getString("name");
                            if (listName != null) {
                                listNames.add(listName);
                            }
                        }
                        // ListAdapter mit Kontext, Listennamen und Click-Listener erstellen
                        ListAdapter adapter = new ListAdapter(listNames, HomeFragment.this::onEinkaufsListClicked);
                        recyclerView.setAdapter(adapter);
                    },
                    e -> Toast.makeText(getContext(), "Fehler beim Laden der Listen: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }

        return view;
    }

    /**
     * Methode wenn eine Liste angeklickt wird.
     * @param listName
     */
    private void onEinkaufsListClicked(String listName) {
        Toast.makeText(getContext(), "Liste ausgewählt: " + listName, Toast.LENGTH_SHORT).show();
        //Hier Produkte anzeigen lassen

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        firestoreManager.getListIdByName(listName, listId -> {
            db.collection("lists").document(listId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        ProductList productList = documentSnapshot.toObject(ProductList.class);
                        if (productList != null) {
                            List<Product> products = productList.getProducts();
                            showProductsDialog(listId, listName, products); // `listId` übergeben
                        } else {
                            Toast.makeText(getContext(), "Keine Produkte gefunden!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Fehler beim Abrufen: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }, e -> Toast.makeText(getContext(), "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Erstellt ein Dialog fenster um einen Benutzer hinzuzufügen.
     * Wird in showProductsDialog aufgerufen.
     * @param listId Id der Liste, zu dem der Benutzer hinzugefügt werden soll.
     */
    private void showAddUser(String listId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Benutzer zur Liste hinzufügen");

        // Layout für die Benutzer-Eingabe
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        // Eingabefeld für Benutzer-UID
        EditText userIdInput = new EditText(requireContext());
        userIdInput.setHint("Benutzer-UID eingeben");
        layout.addView(userIdInput);

        builder.setView(layout);

        builder.setPositiveButton("Hinzufügen", (dialog, which) -> {
            String userId = userIdInput.getText().toString().trim();
            if (userId.isEmpty()) {
                Toast.makeText(getContext(), "UID darf nicht leer sein!", Toast.LENGTH_SHORT).show();
                return;
            }
            firestoreManager.addUserToList(listId, userId,
                    aVoid -> {
                        Toast.makeText(getContext(), "Benutzer hinzugefügt!", Toast.LENGTH_SHORT).show();
                        refreshFragment(); // HomeFragment neu laden
                    },
                    e -> Toast.makeText(getContext(), "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss());

        // Dialog anzeigen
        builder.show();
    }

    /**
     * Aktualisiert die ProgressBar der HomeFragment.
     */
    public void updateProgressBar() {
        View view = getView();
        if (view != null) {
            RecyclerView recyclerView = view.findViewById(R.id.ViewLists);
            if (recyclerView != null && recyclerView.getAdapter() != null) {
                recyclerView.getAdapter().notifyDataSetChanged(); // Nur die Liste neu laden
            }
        }
    }



    /**
     * Methode zum Aktualisieren des HomeFragment.
     */
    private void refreshFragment() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_view_tag, new HomeFragment());
        transaction.addToBackStack(null); // Optional: Falls du zurück navigieren willst
        transaction.commit();
    }

    private void showProductsDialog(String listId, String listName, List<Product> products) {

        this.currentListId = listId;
        RecyclerView recyclerView = new RecyclerView(requireContext());  //Scroll Liste initialisieren
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        //requireContext() um sicherzustellen einen gültigen Kontext zu bekommen der nicht null ist
        //req.Cont: notwendig um Ressourcen wie Layouts etc. laden zu können
        ProductAdapter adapter = new ProductAdapter(requireContext(), products, true, listName);  //Im Adapter ist auch das Anzeigen des Popup-Fenster bei Klick auf Produkt enthalten
        recyclerView.setAdapter(adapter);


        // für das Swippen nach Links
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            // Wenn geswiped wurde
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int aktuellesProduct = viewHolder.getAdapterPosition();
                Product product = products.get(aktuellesProduct);

                firestoreManager.deleteProductFromList(listId, product,
                        aVoid -> {
                            products.remove(aktuellesProduct);
                            adapter.notifyItemRemoved(aktuellesProduct);
                            Toast.makeText(getContext(), "Produkt gelöscht!", Toast.LENGTH_SHORT).show();
                        },
                        e -> Toast.makeText(getContext(), "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            // Provisorisch müssen das nochmal druchgenen !!!!!!
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                if (isCurrentlyActive) {
                    // Zeichne den roten Hintergrund, wenn das Element nach links gewischt wird
                    View itemView = viewHolder.itemView;
                    Paint paint = new Paint();
                    paint.setColor(Color.RED); // Setzt die Farbe des Hintergrunds auf Rot
                    c.drawRect(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + dX, itemView.getBottom(), paint);

                    // Zeichne das Löschsymbol (delete_icon.xml als Vektor)
                    Drawable deleteIcon = ContextCompat.getDrawable(getContext(), R.drawable.delete_icon); // Vektor-Icon
                    int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                    int iconTop = itemView.getTop() + iconMargin;
                    int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
                    int iconLeft = itemView.getLeft() + iconMargin;
                    int iconRight = iconLeft + deleteIcon.getIntrinsicWidth();

                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    deleteIcon.draw(c);
                }
            }

        }).attachToRecyclerView(recyclerView);


        //Erstellt Dialog fenster
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Produkte in der Liste: " + listName)
                .setView(recyclerView) // RecyclerView in den Dialog einfügen
                .setPositiveButton("Schließen", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("Benutzer hinzufügen", (dialog, which) -> {
                    showAddUser(listId);
                })
                .setNeutralButton("Liste löschen", (dialog, which) -> {
                    AlertDialog.Builder builderJaOderNein = new AlertDialog.Builder(requireContext());
                    builderJaOderNein.setTitle("Willst du die Liste \"" + listName + "\" wirklich löschen?")
                            .setPositiveButton("Ja", (dialogJa, whichJa) -> {
                                firestoreManager.deleteList(listId, aVoid -> {
                                    Toast.makeText(getContext(), "Liste gelöscht!", Toast.LENGTH_SHORT).show();
                                    getParentFragmentManager().beginTransaction()  //Homefragment neuladen
                                            .replace(R.id.fragment_container_view_tag, new HomeFragment())
                                            .commit();
                                }, e -> {
                                    Toast.makeText(getContext(), "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            })
                            .setNegativeButton("Nein", (dialogJa, whichJa) -> dialogJa.dismiss())
                            .show();
                })
                .show();

    }
}