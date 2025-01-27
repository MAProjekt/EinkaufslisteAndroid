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
import androidx.recyclerview.widget.ItemTouchHelper;
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

    private FirestoreManager firestoreManager;

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
            firestoreManager.getLists(userId,
                    documents -> {
                        List<String> listNames = new ArrayList<>();
                        for (DocumentSnapshot doc : documents) {
                            String listName = doc.getString("name");
                            if (listName != null) {
                                listNames.add(listName);
                            }
                        }
                        ListAdapter adapter = new ListAdapter(listNames, HomeFragment.this::onEinkaufsListClicked);
                        recyclerView.setAdapter(adapter);
                    },
                    // Fehlerhandler
                    e -> Toast.makeText(getContext(), "Fehler beim Laden der Listen: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
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
                firestoreManager.deleteProductFromList(FirebaseAuth.getInstance().getCurrentUser().getUid(), listName, product,
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
                .show();
    }




}