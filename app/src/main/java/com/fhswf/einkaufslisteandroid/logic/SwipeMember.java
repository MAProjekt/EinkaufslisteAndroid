package com.fhswf.einkaufslisteandroid.logic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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
import com.fhswf.einkaufslisteandroid.fragment.GroupsFragment;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class SwipeMember extends ItemTouchHelper.SimpleCallback {
    private MemberAdapter adapter;
    private List<String> emails;
    private String ownerEmail;
    private FirestoreManager firestoreManager;
    private String listId;
    private Context context;

    public SwipeMember(MemberAdapter adapter, List<String> emails, String ownerEmail, FirestoreManager firestoreManager, String listId, Context context){
        super(0, ItemTouchHelper.LEFT);
        this.adapter = adapter;
        this.emails = emails;
        this.ownerEmail = ownerEmail;
        this.firestoreManager = firestoreManager;
        this.listId = listId;
        this.context = context;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // Hole die E-Mail des aktuell angemeldeten Benutzers
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        // Wenn der aktuelle Benutzer nicht der Creator ist, deaktiviere den Swipe (alle Swipe-Aktionen)
        if (currentUserEmail == null || !currentUserEmail.equals(ownerEmail)) {
            return 0;
        }

        int position = viewHolder.getAdapterPosition();
        // Verhindere, dass der Creator sich selbst swipen kann
        if (emails.get(position).equals(ownerEmail)) {
            return 0;
        }
        return super.getMovementFlags(recyclerView, viewHolder);
    }


    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        String emailToRemove = emails.get(position);

        firestoreManager.removeUserFromList(listId, emailToRemove, aVoid -> {
            emails.remove(position);
            adapter.notifyItemRemoved(position);
            Toast.makeText(context, "Mitglied entfernt", Toast.LENGTH_SHORT).show();
        }, e -> {
            adapter.notifyItemChanged(position);
            Toast.makeText(context, "Fehler beim Entfernen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    public void drawSwipeBackground(Canvas c, RecyclerView.ViewHolder viewHolder, float dX) {
        View itemView = viewHolder.itemView;
        Paint paint = new Paint();
        paint.setColor(Color.RED);

        if (dX < 0) {  // Nur beim Swipe nach links
            float left = itemView.getRight() + dX;  // dX ist negativ
            float right = itemView.getRight();
            c.drawRect(left, itemView.getTop(), right, itemView.getBottom(), paint);

            Drawable deleteIcon = ContextCompat.getDrawable(context, R.drawable.delete_icon);
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

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        int position = viewHolder.getAdapterPosition();

        // Prüfe, ob die Position noch gültig ist
        if (position == RecyclerView.NO_POSITION || position >= emails.size()) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }

        // bei Besitzer kein Swipe zeichnen
        if (emails.get(position).equals(ownerEmail)) {
            super.onChildDraw(c, recyclerView, viewHolder, 0, dY, actionState, isCurrentlyActive);
            return;
        }

        drawSwipeBackground(c, viewHolder, dX);
    }
}
