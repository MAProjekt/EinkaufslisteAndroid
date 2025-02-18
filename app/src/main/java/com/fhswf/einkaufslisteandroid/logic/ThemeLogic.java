package com.fhswf.einkaufslisteandroid.logic;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.fhswf.einkaufslisteandroid.R;

/**
 * Klasse um Theme anzupassen
 */
public class ThemeLogic {
    /**
     * Methode um je nach Status des Darkmodes die Farbe einstellt der Fragmente, Menüs, etc.
     * @param context der Kontext, in dem die Einstellung vorgenommen werden soll.
     */
    public static void toggleDarkMode(Context context) {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Toast.makeText(context, "Darkmode deaktiviert", Toast.LENGTH_SHORT).show();
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Toast.makeText(context, "Darkmode aktiviert", Toast.LENGTH_SHORT).show();
        }
    }
}
