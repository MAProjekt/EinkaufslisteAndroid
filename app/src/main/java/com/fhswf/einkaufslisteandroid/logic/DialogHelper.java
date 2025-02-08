package com.fhswf.einkaufslisteandroid.logic;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Klasse um DialogFenster für Listen zu erstellen
 */
public class DialogHelper {
    /**
     * Zeigt einen Dialog an, um eine neue Liste zu erstellen.
     *
     * @param context  der Kontext, in dem der Dialog angezeigt werden soll.
     * @param listener Callback, der aufgerufen wird, wenn der Nutzer eine Liste erstellt.
     */
    public static void showCreateListDialog(Context context, OnListCreatedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Neue Liste erstellen");

        EditText input = new EditText(context);
        input.setHint("Listenname eingeben");
        builder.setView(input);

        builder.setPositiveButton("Erstellen", (dialog, which) -> {
            String listName = input.getText().toString().trim();
            if (!listName.isEmpty()) {
                listener.onListCreated(listName);
            } else {
                Toast.makeText(context, "Listenname darf nicht leer sein!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Interface für den Callback, um den erstellten Listenname weiterzugeben.
     */
    public interface OnListCreatedListener {
        void onListCreated(String listName);
    }
}