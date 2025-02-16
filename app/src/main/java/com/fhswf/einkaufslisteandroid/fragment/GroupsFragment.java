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
        return R.layout.fragment_universal;
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
            showDialogOptionenGroup(listId, listName, products, isCreator);
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



    private void showDialogOptionenGroup(String listId, String listName, List<Product> products, boolean isCreator) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Produkte in der Liste: " + listName);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_optionen, null);

        AlertDialog dialog = builder.setView(dialogView).create();

        LinearLayout layout = dialogView.findViewById(R.id.addProductLinearLayout);
        layout.setOnClickListener(v -> {
            dialog.dismiss();
            openProdukte();
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

        swipeToDelete(recyclerView, listId, products);

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

                // ItemTouchHelper für Swipe-to-Delete (außer für den Besitzer)
                new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                    @Override
                    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                        int position = viewHolder.getAdapterPosition();
                        // Falls das aktuelle Element dem Besitzer entspricht, wird kein Swipe erlaubt.
                        if (emails.get(position).equals(ownerEmail)) {
                            return 0;
                        }
                        return super.getMovementFlags(recyclerView, viewHolder);
                    }

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        String emailToRemove = emails.get(position);

                        // Aufruf der Methode zum Entfernen des Benutzers
                        firestoreManager.removeUserFromList(listId, emailToRemove, aVoid -> {
                            emails.remove(position);
                            adapter.notifyItemRemoved(position);
                            Toast.makeText(getContext(), "Mitglied entfernt", Toast.LENGTH_SHORT).show();
                        }, e -> {
                            adapter.notifyItemChanged(position);
                            Toast.makeText(getContext(), "Fehler beim Entfernen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                            int actionState, boolean isCurrentlyActive) {
                        int position = viewHolder.getAdapterPosition();
                        // bei Besitzer kein Swipe zeichnen
                        if (emails.get(position).equals(ownerEmail)) {
                            super.onChildDraw(c, recyclerView, viewHolder, 0, dY, actionState, isCurrentlyActive);
                            return;
                        }

                        // Zeichne roten Hintergrund und Mülleimer-Icon
                        View itemView = viewHolder.itemView;
                        Paint paint = new Paint();
                        paint.setColor(Color.RED);

                        if (dX < 0) { // Swipe nach links
                            float left = itemView.getRight() + dX;
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

                                if (iconLeft < left) {
                                    iconLeft = (int) left;
                                    iconRight = iconLeft + intrinsicWidth;
                                }

                                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconTop + intrinsicHeight);
                                deleteIcon.draw(c);
                            }
                        }
                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }
                }).attachToRecyclerView(recyclerView);

                memberDialog.setView(view);
                memberDialog.setPositiveButton("Schließen", (dialog, which) -> dialog.dismiss());
                memberDialog.show();
            }, e -> Toast.makeText(getContext(), "Fehler beim Laden der Mitglieder: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }, e -> Toast.makeText(getContext(), "Fehler beim Laden des Besitzers: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }









}