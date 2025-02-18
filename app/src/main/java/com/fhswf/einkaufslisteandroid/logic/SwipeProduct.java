package com.fhswf.einkaufslisteandroid.logic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.fhswf.einkaufslisteandroid.R;
import com.fhswf.einkaufslisteandroid.datenpersistierung.FirestoreManager;
import com.fhswf.einkaufslisteandroid.models.Product;

import java.util.List;

public class SwipeProduct extends ItemTouchHelper.SimpleCallback {
    private final RecyclerView.Adapter adapter;
    private final List<Product> products;
    private final FirestoreManager firestoreManager;
    private final String listId;
    private final Context context;

    public SwipeProduct(Context context, RecyclerView.Adapter adapter, List<Product> products, FirestoreManager firestoreManager, String listId) {
        super(0, ItemTouchHelper.LEFT);
        this.context = context;
        this.adapter = adapter;
        this.products = products;
        this.firestoreManager = firestoreManager;
        this.listId = listId;
    }


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
            adapter.notifyItemRemoved(position);
            Toast.makeText(context, "Produkt gelöscht!", Toast.LENGTH_SHORT).show();
        }, e -> {
            adapter.notifyItemChanged(position);
            Toast.makeText(context, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (dX < 0) {
            drawSwipeBackground(c, viewHolder, dX);
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private void drawSwipeBackground(Canvas c, RecyclerView.ViewHolder viewHolder, float dX) {
        View itemView = viewHolder.itemView;
        Paint paint = new Paint();
        int customColor = ContextCompat.getColor(context, R.color.red_for_delete_swipe);
        paint.setColor(customColor);

        if (dX < 0) { // Nur wenn nach links geswiped wird
            float left = itemView.getRight() + dX; // dX ist negativ
            float right = itemView.getRight();
            c.drawRect(left, itemView.getTop(), right, itemView.getBottom(), paint);

            Drawable deleteIcon = ContextCompat.getDrawable(context, R.drawable.delete_icon);
            if (deleteIcon != null) {
                int intrinsicWidth = deleteIcon.getIntrinsicWidth();
                int intrinsicHeight = deleteIcon.getIntrinsicHeight();
                int iconMargin = (itemView.getHeight() - intrinsicHeight) / 2;
                int iconTop = itemView.getTop() + iconMargin;
                int iconBottom = iconTop + intrinsicHeight;
                int iconRight = itemView.getRight() - iconMargin;
                int iconLeft = iconRight - intrinsicWidth;

                // Falls iconLeft kleiner als backgroundLeft (also der linke Rand des sichtbaren Hintergrunds) ist,
                // wird iconLeft auf backgroundLeft gesetzt, sodass der Eimer im Hintergrund bleibt.
                if (iconLeft < left) {
                    iconLeft = (int) left;
                    iconRight = iconLeft + intrinsicWidth;
                }

                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                deleteIcon.draw(c);
            }
        }
    }
}
