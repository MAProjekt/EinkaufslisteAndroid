package com.fhswf.einkaufslisteandroid.logic;

import android.app.Activity;
import android.content.Context;
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
