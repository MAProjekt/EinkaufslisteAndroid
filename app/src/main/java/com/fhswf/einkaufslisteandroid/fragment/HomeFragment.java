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
public class HomeFragment extends BaseFragment {

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


    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_home;
    }

    @Override
    protected boolean isGroupFragment() {
        return false;
    }




    @Override
    protected void showProductsDialog(String listId, String listName, List<Product> products) {
        this.currentListId = listId;

        firestoreManager.getCreator(listId, creatorId -> {
            boolean isCreator = FirebaseAuth.getInstance().getCurrentUser().getUid().equals(creatorId);
            showDialogOptionenHome(listId, listName, products, isCreator); // RecyclerView wird im Layout erstellt
        }, e -> Toast.makeText(getContext(), "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private RecyclerView createRecyclerView(List<Product> products, String listName) {
        RecyclerView recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new ProductAdapter(requireContext(), products, true, listName));
        return recyclerView;
    }

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

    private void drawSwipeBackground(Canvas c, RecyclerView.ViewHolder viewHolder, float dX) {
        View itemView = viewHolder.itemView;
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        c.drawRect(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + dX, itemView.getBottom(), paint);

        Drawable deleteIcon = ContextCompat.getDrawable(getContext(), R.drawable.delete_icon);
        if (deleteIcon != null) {
            int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + iconMargin;
            int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
            int iconLeft = itemView.getLeft() + iconMargin;
            int iconRight = iconLeft + deleteIcon.getIntrinsicWidth();

            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            deleteIcon.draw(c);
        }
    }

    private void showDialogOptionenHome(String listId, String listName, List<Product> products, boolean isCreator) {
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

        int maxHeight = 650; // Maximalste Höhe der RecycleView
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (recyclerView.getHeight() > maxHeight) {
                ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
                params.height = maxHeight;
                recyclerView.setLayoutParams(params);
            }
        });


        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Liste teilen", (d, which) -> showAddUser(listId));
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Schließen", (d, which) -> d.dismiss());

        if (isCreator) {
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Liste löschen", (d, which) -> deleteListBestaetigen(listId, listName));
        }

        dialog.show();
    }


    public void openProdukte(){
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_view_tag, new UebersichtFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
