package com.fhswf.einkaufslisteandroid.logic;

import android.content.Context;
import android.content.Intent;

import com.fhswf.einkaufslisteandroid.Login;
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
}