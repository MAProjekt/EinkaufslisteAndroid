package com.fhswf.einkaufslisteandroid.logic;

import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.fhswf.einkaufslisteandroid.Login;
import com.fhswf.einkaufslisteandroid.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Verwaltet die Authentifizierung und stellt Methoden dafür bereit
 */
public class AuthService {
    private static AuthService instance;
    private FirebaseAuth mAuth;

    /**
     * Holt die Firebase-Auth-Instanz
     */
    private AuthService() {
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Singleton-Instanz abrufen
     * @return Instanz von AuthService
     */
    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Gibt den aktuell eingeloggten Benutzer zurück
     * @return eingeloggter User oder null, wenn keiner eingeloggt ist
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    /**
     * Meldet den Benutzer ab und leitet zur Login-Seite weiter
     * @param context aktueller Kontext (wird für den Intent benötigt)
     */
    public void signOut(Context context) {
        mAuth.signOut();
        context.startActivity(new Intent(context, Login.class));
    }

    /**
     * Aktualisiert den Anzeigenamen des Benutzers in Firebase
     * @param newDisplayName neuer Name
     * @param listener Callback für das Ergebnis der Aktualisierung
     */
    public void updateDisplayName(String newDisplayName, OnCompleteListener<Void> listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(newDisplayName).build())
                    .addOnCompleteListener(listener);
        }
    }

    /**
     * Lädt das Profilbild des aktuell eingeloggten Benutzers in das übergebene ImageView.
     * Falls kein Profilbild vorhanden ist, wird ein Standardbild angezeigt.
     *
     * @param context   Kontext für Glide
     * @param imageView Das ImageView, in dem das Profilbild angezeigt werden soll
     */
    public void loadUserProfileImage(Context context, ImageView imageView) {
        FirebaseUser user = getCurrentUser();
        if (user != null && user.getPhotoUrl() != null) {
            Glide.with(context)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.default_user_icon)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.default_user_icon);
        }
    }
}