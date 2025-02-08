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
public class GroupsFragment extends BaseFragment {
    private FirestoreManager firestoreManager;

    private String currentListId;

    public GroupsFragment() {
    }

    /**
     * Methode, um das GroupFragment XML-Layout bereitzustellen.
     * @return gibt das fragment_groups XML Layout zurück.
     */
    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_groups;
    }

    @Override
    protected boolean isGroupFragment() {
        return true;
    }


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

        RecyclerView recyclerView = createRecyclerView(products, listName);
        swipeToDelete(recyclerView, listId, products);

        firestoreManager.getCreator(listId, creatorId -> {
            boolean isCreator = FirebaseAuth.getInstance().getCurrentUser().getUid().equals(creatorId);
            showDialogOptionenGroup(listId, listName, recyclerView, isCreator);
        }, e -> Toast.makeText(getContext(), "Fehler" + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    //TODO: In BaseFragment auslagern
    private RecyclerView createRecyclerView(List<Product> products, String listName) {
        RecyclerView recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new ProductAdapter(requireContext(), products, true, listName));
        return recyclerView;
    }

    //TODO: In BaseFragment auslagern
    private void swipeToDelete(RecyclerView recyclerView, String listId, List<Product> products) {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Product product = products.get(position);

                firestoreManager.deleteProductFromList(listId, product, aVoid -> {
                    products.remove(position);
                    recyclerView.getAdapter().notifyItemRemoved(position);
                    refreshFragment();
                    Toast.makeText(getContext(), "Produkt gelöscht!", Toast.LENGTH_SHORT).show();
                }, e -> Toast.makeText(getContext(), "Fehler" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                drawSwipeBackground(c, viewHolder, dX);
            }
        }).attachToRecyclerView(recyclerView);
    }


    /**
     * Zeichnet beim Linkswischen den roten Hintergrund und das Mülleimer-Icon an der rechten Seite.
     * Dabei wird nur gezeichnet, wenn dX negativ ist (Linkswisch).
     */
    private void drawSwipeBackground(Canvas c, RecyclerView.ViewHolder viewHolder, float dX) {
        View itemView = viewHolder.itemView;
        Paint paint = new Paint();
        paint.setColor(Color.RED);

        if (dX < 0) {  // Nur beim Swipe nach links
            float left = itemView.getRight() + dX;  // dX ist negativ
            float right = itemView.getRight();
            c.drawRect(left, itemView.getTop(), right, itemView.getBottom(), paint);

            Drawable deleteIcon = ContextCompat.getDrawable(getContext(), R.drawable.delete_icon);
            if (deleteIcon != null) {
                int intrinsicWidth = deleteIcon.getIntrinsicWidth();
                int intrinsicHeight = deleteIcon.getIntrinsicHeight();
                int iconMargin = (itemView.getHeight() - intrinsicHeight) / 2;
                int iconTop = itemView.getTop() + iconMargin;
                int iconRight = itemView.getRight() - iconMargin;
                int iconLeft = iconRight - intrinsicWidth;

                // Falls iconLeft kleiner als backgroundLeft (also der linke Rand des sichtbaren Hintergrunds) ist,
                // wird iconLeft auf backgroundLeft gesetzt, sodass der Eimer im Hintergrund bleibt.
                if (iconLeft < left) {
                    iconLeft = (int) left;
                    iconRight = iconLeft + intrinsicWidth;
                }

                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconTop + intrinsicHeight);
                deleteIcon.draw(c);
            }
        }
    }


    private void showDialogOptionenGroup(String listId, String listName, RecyclerView recyclerView, boolean isCreator) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Produkte in der Liste: " + listName)
                .setView(recyclerView)
                .setPositiveButton("Schließen", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("Benutzer hinzufügen", (dialog, which) -> showAddUser(listId))
                .setNeutralButton("Gruppe verlassen", (dialog, which) -> gruppeVerlassen(listId));

        if (isCreator) {
            builder.setNeutralButton("Liste löschen", (dialog, which) -> deleteListBestaetigen(listId, listName));
        }

        builder.show();
    }

    private void gruppeVerlassen(String listId) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestoreManager.leaveList(listId, currentUserId, aVoid -> {
            Toast.makeText(getContext(), "Gruppe verlassen!", Toast.LENGTH_SHORT).show();
            refreshFragment();
        }, e -> Toast.makeText(getContext(), "Fehler" + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    //TODO:In BaseFragment auslagern
//    private void deleteListBestaetigen(String listId, String listName) {
//        AlertDialog.Builder confirmDialog = new AlertDialog.Builder(requireContext());
//        confirmDialog.setTitle("Willst du die Liste \"" + listName + "\" wirklich löschen?")
//                .setPositiveButton("Ja", (dialog, which) -> {
//                    firestoreManager.deleteList(listId, aVoid -> {
//                        Toast.makeText(getContext(), "Liste gelöscht!", Toast.LENGTH_SHORT).show();
//                        refreshFragment();
//                    }, e -> Toast.makeText(getContext(), "Fehler" + e.getMessage(), Toast.LENGTH_SHORT).show());
//                })
//                .setNegativeButton("Nein", (dialog, which) -> dialog.dismiss())
//                .show();
//    }


}