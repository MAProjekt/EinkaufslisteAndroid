package com.fhswf.einkaufslisteandroid.fragment;

import static com.fhswf.einkaufslisteandroid.services.PushNotificationSender.sendPushNotification;

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Eine Klasse die von BaseFragment erbt.
 * Dient für die Anzeige von Gruppen-Listen unter dem Menü-Punkt "Gruppeninhalte".
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
     * Um zwischen HomeFragment und GroupFragment zu unterscheiden.
     * Wichtig für die Anzeige der Listen, also ob sie in einem HomeFragment oder in einem GroupFragment angezeigt werden sollen.
     * @return gibt true zurück, da das GroupFragment ein Gruppen-Fragment ist.
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


    /**
     * Zeigt für die Gruppenlisten ein Dialog mit den Optionen an.
     * @param listId Die ListId von der Liste in der man sich gerade befindet.
     * @param listName Name der Liste in der man sich gerade befindet.
     * @param products Die Liste der Produkte in der akutellen Liste.
     * @param isCreator Gibt an, ob der aktuelle Nutzer der Ersteller der Liste ist.
     */
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
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Gruppe verlassen", (d, which) -> gruppeVerlassen(listId ,listName));
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Benutzer anzeigen", (d, which) -> showMemberListDialog(listId));

        if(isCreator){
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Liste löschen", (d, which) -> deleteListBestaetigen(listId, listName));
        }

        new ItemTouchHelper(new SwipeProduct(requireContext(), recyclerView.getAdapter(), products, firestoreManager, listId)).attachToRecyclerView(recyclerView);

        dialog.show();
    }


    /**
     * Ermöglicht es einem Benutzer die Gruppe zu verlassen.
     * @param listId Die ID der Liste, welcher der Benutzer verlassen will.
     */
    private void gruppeVerlassen(String listId, String listname ) {
        Log.d("DEBUG", "gruppeVerlassen() wurde aufgerufen mit listId: " + listId);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userName = (currentUser != null && currentUser.getDisplayName() != null) ? currentUser.getDisplayName() : "Ein Mitglied";

        firestoreManager.leaveList(listId, currentUserId, aVoid -> {
            firestoreManager.getListMemberTokens(listId, tokens -> {
                for (String token : tokens) {
                    sendPushNotification(requireContext(),token, "Listen-Update", userName + " hat die Liste "+ listname + " verlassen.");
                }
            });

            refreshFragment();
        }, e -> Log.e("ERROR", "Fehler beim Verlassen der Liste: " + e.getMessage()));
    }



    /**
     * Zeigt eine RecyclerView mit den Mitgliedern (User-IDs) in der Liste an.
     * Lädt dazu die Mitglieder aus der Firestore-Datenbank und stellt sie in einer RecyclerView dar.
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