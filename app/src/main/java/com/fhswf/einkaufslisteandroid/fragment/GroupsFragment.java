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
import com.fhswf.einkaufslisteandroid.logic.MemberAdapter;
import com.fhswf.einkaufslisteandroid.logic.ProductAdapter;
import com.fhswf.einkaufslisteandroid.logic.SwipeMember;
import com.fhswf.einkaufslisteandroid.logic.SwipeProduct;
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
public class GroupsFragment extends BaseFragment {
    // FirestoreManager zur Verwaltung der Datenbankoperationen mit Firestore.
    private FirestoreManager firestoreManager;
    // Die Liste die man im Fragment auswählt.
    private String currentListId;

    /**
     * Konstruktor der das Gruppen-Fragment initialisiert.
     */
    public GroupsFragment() {
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
     *
     * @return
     */
    @Override
    protected boolean isGroupFragment() {
        return true;
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
     * Zeigt die Produkte einer Liste an.
     * Zudem gibt es unter dem Dialog die Möglichkeit einen Benutzer hinzuzufügen oder die Liste zu löschen.
     * @param listId
     * @param listName
     * @param products
     */
    @Override
    protected void showProductsDialog(String listId, String listName, List<Product> products) {
        this.currentListId = listId;

        firestoreManager.getCreator(listId, creatorId -> {
            boolean isCreator = FirebaseAuth.getInstance().getCurrentUser().getUid().equals(creatorId);
            showDialogOptionenGroup(listId, listName, products, isCreator);
        }, e -> Toast.makeText(getContext(), "Fehler" + e.getMessage(), Toast.LENGTH_SHORT).show());
    }




    private void showDialogOptionenGroup(String listId, String listName, List<Product> products, boolean isCreator) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Produkte in der Liste: " + listName);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_optionen, null);

        AlertDialog dialog = builder.setView(dialogView).create();

        LinearLayout layout = dialogView.findViewById(R.id.addProductLinearLayout);
        layout.setOnClickListener(v -> {
            dialog.dismiss();
            openProdukte(listId);
        });

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new ProductAdapter(requireContext(), products, true, listName));

        int maxHeight = 650; // Maximale Höhe des RecyclerViews in Pixeln
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (recyclerView.getHeight() > maxHeight) {
                ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
                params.height = maxHeight;
                recyclerView.setLayoutParams(params);
            }
        });

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Liste teilen", (d, which) -> showAddUser(listId));
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Gruppe verlassen", (d, which) -> gruppeVerlassen(listId));
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Benutzer anzeigen", (d, which) -> showMemberListDialog(listId));

        if(isCreator){
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Liste löschen", (d, which) -> deleteListBestaetigen(listId, listName));
        }

        new ItemTouchHelper(new SwipeProduct(requireContext(), recyclerView.getAdapter(), products, firestoreManager, listId)).attachToRecyclerView(recyclerView);

        dialog.show();
    }



    private void gruppeVerlassen(String listId) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestoreManager.leaveList(listId, currentUserId, aVoid -> {
            Toast.makeText(getContext(), "Gruppe verlassen!", Toast.LENGTH_SHORT).show();
            refreshFragment();
        }, e -> Toast.makeText(getContext(), "Fehler" + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    /**
     * Zeigt eine RecyclerView mit den Mitgliedern (User-IDs) in der Liste an.
     * Die E-Mail-Adressen werden dynamisch aus Firestore geladen.
     *
     * @param listId Die ID der Liste, deren Mitglieder angezeigt werden sollen.
     */
    private void showMemberListDialog(String listId) {
        // Zuerst die Besitzer-E-Mail abrufen
        firestoreManager.getOwnerEmail(listId, ownerEmail -> {
            // Danach die E-Mails aller Mitglieder laden
            firestoreManager.getUserEmailsByListId(listId, emails -> {
                AlertDialog.Builder memberDialog = new AlertDialog.Builder(requireContext());
                View view = LayoutInflater.from(requireContext()).inflate(R.layout.member_list_dialog, null);
                RecyclerView recyclerView = view.findViewById(R.id.recyclerViewMembers);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

                final MemberAdapter adapter = new MemberAdapter(requireContext(), emails);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                new ItemTouchHelper(new SwipeMember(adapter, emails, ownerEmail, firestoreManager, listId, requireContext())).attachToRecyclerView(recyclerView);

                memberDialog.setView(view);
                memberDialog.setPositiveButton("Schließen", (dialog, which) -> dialog.dismiss());
                memberDialog.show();

            }, e -> Toast.makeText(getContext(), "Fehler beim Laden der Mitglieder: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }, e -> Toast.makeText(getContext(), "Fehler beim Laden des Besitzers: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}