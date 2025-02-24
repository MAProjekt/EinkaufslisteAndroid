// Quelle: https://stackoverflow.com/questions/33985719/android-swipe-to-delete-recyclerview
// https://www.youtube.com/watch?v=eEonjkmox-0

package com.fhswf.einkaufslisteandroid.logic;

import static com.fhswf.einkaufslisteandroid.services.PushNotificationSender.sendPushNotification;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.fhswf.einkaufslisteandroid.R;
import com.fhswf.einkaufslisteandroid.datenpersistierung.FirestoreManager;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/**
 * Klasse um Member aus der Liste zu entfernen, indem man nach links wischt.
 */
public class SwipeMember extends ItemTouchHelper.SimpleCallback {
    private MemberAdapter adapter;
    private List<String> emails;
    private String ownerEmail;
    private FirestoreManager firestoreManager;
    private String listId;
    private Context context;

    /**
     * Konstruktor
     * @param adapter der Adapter, der die Mitgliedsdaten im RecyclerView verwaltet.
     * @param emails Liste der E-Mail-Adressen der Mitglieder.
     * @param ownerEmail Die E-Mail-Adresse des Erstellers der Liste.
     * @param firestoreManager der FirestoreManager, der für Datenbankoperationen zuständig ist.
     * @param listId die ID der Liste, aus der Mitglieder entfernt werden sollen.
     * @param context der Kontext, der für UI-Operationen und zum Abrufen von Ressourcen benötigt
     *                wird.
     */
    public SwipeMember(MemberAdapter adapter, List<String> emails, String ownerEmail, FirestoreManager firestoreManager, String listId, Context context){
        super(0, ItemTouchHelper.LEFT); // Swippen nach links
        this.adapter = adapter;
        this.emails = emails;
        this.ownerEmail = ownerEmail;
        this.firestoreManager = firestoreManager;
        this.listId = listId;
        this.context = context;
    }

    /**
     * Diese Methode bestimmt, welche Swipe- oder Drag-Aktionen für einen bestimmten
     * ViewHolder erlaubt sind.
     * Hier wird geprüft, ob der aktuell angemeldete Benutzer der Listen-Ersteller ist.
     * Falls dies nicht der Fall ist, werden alle Swipe-Aktionen deaktiviert.
     * @param recyclerView die RecyclerView, an die der ItemTouchHelper gebunden ist.
     * @param viewHolder der ViewHolder, für den die Bewegungsinformationen benötigt werden.
     * @return erlaubte Bewegungsflags.
     */
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
     * Diese Methode wird aufgerufen, wenn ein Element durch einen Swipe entfernt wurde.
     * Es wird versucht, das entsprechende Mitglied aus der Datenbank zu entfernen.
     * Bei Erfolg wird das Element aus der Liste gelöscht und der dazugehörige Adapter informiert.
     * @param viewHolder der ViewHolder, der geswiped wurde.
     * @param direction die Richtung, in die geswiped wurde (hier ist nur links erlaubt).
     */
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

        firestoreManager.getUserToken(emailToRemove, token -> {
            if (token != null && !token.isEmpty()) {
                sendPushNotification(context, token, "Listen-Update",
                        "Du wurdest aus einer Liste entfernt.");
            }
        }, e -> Log.e("FCM", "Fehler beim Abrufen des Tokens: " + e.getMessage()));
    }

    /**
     * Zeichnet den Hintergrund (rot) und das Symbol (Mülleimer), während der Swipe-Geste.
     * @param c die Canvas (Leinwandbereich), auf der gezeichnet wird.
     * @param viewHolder der ViewHolder des geswipeten Elements.
     * @param dX die horizontale Verschiebung des Elements während des Swipes.
     */
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

    /**
     * Passt das Aussehen des geswipeten Elements während der Swipe-Geste an.
     * Diese Methode sorgt dafür, dass der Swipe-Hintergrund nur gezeichnet wird, wenn das Element
     * eine gültige Position hat und nicht der Creator selbst ist.
     * @param c die Canvas (Leinwandbereich), auf der gezeichnet wird.
     * @param recyclerView der RecyclerView, der das Element enthält.
     * @param viewHolder der ViewHolder des geswipeten Elements.
     * @param dX die horizontale Verschiebung des Elements während des Swipes.
     * @param dY die vertikale Verschiebung des Elements während des Swipes.
     * @param actionState der aktuelle Aktionszustand (hier Swipe).
     * @param isCurrentlyActive gibt an, ob das Element aktuell aktiv geswiped/genutzt wird.
     */
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
        // Hier wird das geswipte Element bzw View verschoben, sodass der Name auch nach links
        // gewischt wird
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
