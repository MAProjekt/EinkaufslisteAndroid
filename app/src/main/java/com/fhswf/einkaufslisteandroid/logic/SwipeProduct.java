// Quelle: https://stackoverflow.com/questions/33985719/android-swipe-to-delete-recyclerview
// https://www.youtube.com/watch?v=eEonjkmox-0

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

/**
 * Klasse um Produkte aus der Liste zu entfernen, indem man nach links wischt.
 */
public class SwipeProduct extends ItemTouchHelper.SimpleCallback {
    private final RecyclerView.Adapter adapter;
    private final List<Product> products;
    private final FirestoreManager firestoreManager;
    private final String listId;
    private final Context context;
    private final boolean performLocalDeletion; // Gibt an, ob die lokale Entfernung durchgeführt

    /**
     * Konstruktor für SwipeProduct.
     * @param context der Kontext, der für den Zugriff auf Ressourcen der Activity benötigt wird.
     * @param adapter der RecyclerView-Adapter, der die Produkte verwaltet.
     * @param products die Liste der Produkte, die angezeigt werden.
     * @param firestoreManager der FirestoreManager, der für die Kommunikation mit der Datenbank
     *                         zuständig ist.
     * @param listId die ID der Liste, aus der das Produkt gelöscht werden soll.
     * @param performLocalDeletion  true, wenn die lokale Löschung (products.remove(...)) erfolgen soll
     *                              false, wenn auf die Aktualisierung per Snapshot Listener vertraut werden soll.
     */
    public SwipeProduct(Context context, RecyclerView.Adapter adapter, List<Product> products, FirestoreManager firestoreManager, String listId, boolean performLocalDeletion) {
        super(0, ItemTouchHelper.LEFT);
        this.context = context;
        this.adapter = adapter;
        this.products = products;
        this.firestoreManager = firestoreManager;
        this.listId = listId;
        this.performLocalDeletion = performLocalDeletion;
    }

    /**
     * Wird für Drag&Drop-Aktionen verwendet. Da in diesem Fall keine Drag-Aktionen unterstützt
     * werden, wird immer false zurückgegeben.
     * @param recyclerView die RecyclerView, in dem der Drag-Vorgang stattfindet.
     * @param viewHolder der aktuelle ViewHolder.
     * @param target der Ziel-ViewHolder.
     * @return false, da Drag-Aktionen nicht unterstützt werden.
     */
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    /**
     * Diese Methode wird aufgerufen, wenn ein Produkt durch einen Swipe entfernt wird.
     * Es wird versucht, das entsprechende Produkt aus der Firestore zu löschen.
     * Bei Erfolg wird das Produkt aus der lokalen Liste entfernt und der dazugehörige Adapter
     * informiert.
     * @param viewHolder der ViewHolder, der geswiped wurde.
     * @param direction die Richtung, in die geswiped wurde (hier ist nur links erlaubt).
     */
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();

        if (position == RecyclerView.NO_POSITION || position >= products.size()) {
            adapter.notifyDataSetChanged();
            return;
        }

        Product product = products.get(position);

        firestoreManager.deleteProductFromList(listId, product, aVoid -> {
            if (performLocalDeletion) {
                if (position < products.size()) {
                    products.remove(position);
                    adapter.notifyItemRemoved(position);
                }
            } else {
                adapter.notifyDataSetChanged();
            }

            Toast.makeText(context, "Produkt gelöscht!", Toast.LENGTH_SHORT).show();
        }, e -> {
            adapter.notifyItemChanged(position);
            Toast.makeText(context, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    /**
     * Methode, um das Aussehen des geswipeten Elements während des Swipes anzupassen.
     * Hier wird geprüft, ob nach links geswiped wird. Falls ja, wird der Swipe-Hintergrund
     * gezeichnet.
     * @param c die Canvas, auf der gezeichnet wird.
     * @param recyclerView der RecyclerView, in dem der Swipe stattfindet.
     * @param viewHolder der ViewHolder des geswipeten Elements.
     * @param dX die horizontale Verschiebung des Elements während des Swipes.
     * @param dY die vertikale Verschiebung des Elements während des Swipes.
     * @param actionState der aktuelle Aktionszustand (hier Swipe).
     * @param isCurrentlyActive gibt an, ob das Element aktuell aktiv geswiped/genutzt wird.
     */
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (dX < 0) {
            drawSwipeBackground(c, viewHolder, dX);
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    /**
     * Zeichnet den Hintergrund (rot) und das Symbol (Mülleimer), während der Swipe-Geste.
     * @param c die Canvas (Leinwandbereich), auf der gezeichnet wird.
     * @param viewHolder der ViewHolder des geswipeten Elements.
     * @param dX die horizontale Verschiebung des Elements während des Swipes.
     */
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
