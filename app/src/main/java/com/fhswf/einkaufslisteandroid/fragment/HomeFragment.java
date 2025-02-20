package com.fhswf.einkaufslisteandroid.fragment;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fhswf.einkaufslisteandroid.R;
import com.fhswf.einkaufslisteandroid.datenpersistierung.FirestoreManager;
import com.fhswf.einkaufslisteandroid.logic.ProductAdapter;
import com.fhswf.einkaufslisteandroid.logic.SwipeMember;
import com.fhswf.einkaufslisteandroid.logic.SwipeProduct;
import com.fhswf.einkaufslisteandroid.models.Product;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/**
 * Zeigt die Einkaufslisten an, welche die Produkte speichern,
 * zudem kann man auf die Einkaufsliste klicken, um die Produkte anzuzeigen.
 * Hier sind nur die eigenen Listen anzuzeigen.
 */
public class HomeFragment extends BaseFragment {
    // FirestoreManager zur Verwaltung der Datenbankoperationen mit Firestore.
    private FirestoreManager firestoreManager;
    // Die Liste die man im Fragment auswählt.
    private String currentListId;

    /**
     * Konstruktor für das Home-Fragment.
     */
    public HomeFragment() {
        // Leerer Konstruktor
    }

    /**
     * Wird beim Erstellen des Fragments aufgerufen. Hier wird der FirestoreManager initialisiert,
     * um später Datenbankabfragen durchführen zu können.
     * @param savedInstanceState Falls vorhanden, der zuvor gespeicherte Zustand des Fragments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestoreManager = new FirestoreManager();
    }

    /**
     * Methode, um das GroupFragment XML-Layout bereitzustellen.
     * @return gibt das fragment_groups XML Layout zurück.
     */
    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_universal;
    }

    /**
     * Wert für die Methode aus BaseFragment, die prüft bzw. angibt, ob es ein Gruppen-Fragment ist.
     * @return immer false, da es nur die eigenen Listen sind im HomeFragment.
     */
    @Override
    protected boolean isGroupFragment() {
        return false;
    }

    /**
     * Methode um Inhalte der Produkte als Dialog anzeigen zu lassen.
     * @param listId ID der ausgewählten Liste.
     * @param listName Name der Liste die ausgewählt worden ist.
     * @param products Liste der Produkte die in der Liste enthalten sind.
     */
    @Override
    protected void showProductsDialog(String listId, String listName, List<Product> products) {
        this.currentListId = listId;

        firestoreManager.getCreator(listId, creatorId -> {
            boolean isCreator = FirebaseAuth.getInstance().getCurrentUser().getUid().equals(creatorId);
            showDialogOptionenHome(listId, listName, products, isCreator);
        }, e -> Toast.makeText(getContext(), "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    /**
     * Zeigt den Dialog mit den Produkt-Optionen an und bindet hier auch den
     * Swipe-to-Delete-Mechanismus ein.
     * @param listId ID der Liste.
     * @param listName Name der Liste.
     * @param products Liste der Produkte.
     * @param isCreator Ersteller der Liste.
     */
    private void showDialogOptionenHome(String listId, String listName, List<Product> products, boolean isCreator) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Produkte in der Liste: " + listName);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_optionen, null);

        AlertDialog dialog = builder.setView(dialogView).create();

        LinearLayout layout = dialogView.findViewById(R.id.addProductLinearLayout);
        layout.setOnClickListener(v -> {
            dialog.dismiss();
            openProdukte(listId);
            Log.d("DEBUG", "Liste ID aus HomeFragment: " + listId);
        });

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewDialog);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new ProductAdapter(requireContext(), products, true, listName));

        //Debugger für die RecyclerView Breite
//        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
//            int recyclerViewWidth = recyclerView.getWidth();
//            Log.d("RecyclerViewSize", "Breite der RecyclerView: " + recyclerViewWidth + "px");
//        });

        int maxHeight = 650; // Maximale Höhe des RecyclerViews in Pixeln
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (recyclerView.getHeight() > maxHeight) {
                LayoutParams params = recyclerView.getLayoutParams();
                params.height = maxHeight;
                recyclerView.setLayoutParams(params);
            }
        });

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Liste teilen", (d, which) -> showAddUser(listId));
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Schließen", (d, which) -> d.dismiss());

        if (isCreator) {
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Liste löschen", (d, which) -> deleteListBestaetigen(listId, listName));
        }

        new ItemTouchHelper(new SwipeProduct(requireContext(), recyclerView.getAdapter(), products, firestoreManager, listId)).attachToRecyclerView(recyclerView);


        dialog.show();
    }
}
