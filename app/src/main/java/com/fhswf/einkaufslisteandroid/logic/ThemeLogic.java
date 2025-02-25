package com.fhswf.einkaufslisteandroid.logic;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.fhswf.einkaufslisteandroid.R;

/**
 * Klasse um Theme anzupassen
 */
public class ThemeLogic {
    /**
     * Liest den Darkmode-Zustand aus den SharedPreferences aus und setzt ihn.
     * Diese Methode solltest du zu Beginn der App aufrufen, z.B. in der MainActivity,
     * bevor setContentView() aufgerufen wird.
     *
     * @param context der aktuelle Kontext
     */
    public static void applyDarkModeFromPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("darkMode", false);
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Methode um je nach Status des Darkmodes die Farbe einstellt der Fragmente, Menüs, etc.
     * @param context der Kontext, in dem die Einstellung vorgenommen werden soll.
     */
    public static void toggleDarkMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("darkMode", false);

        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            prefs.edit().putBoolean("darkMode", false).apply();
            Toast.makeText(context, "Darkmode deaktiviert", Toast.LENGTH_SHORT).show();
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            prefs.edit().putBoolean("darkMode", true).apply();
            Toast.makeText(context, "Darkmode aktiviert", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Methode um die Farbe der NavigationBar zu ändern.
     * @param activity die Activity, in der die NavigationBar geändert werden soll.
     */
    public static void updateNavigationBarColor(Activity activity) {
        int nightModeFlags = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.black_from_fragment));
        } else {
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.white_from_fragment));
        }
    }
}
