package com.fhswf.einkaufslisteandroid;

//Quellen:
//https://www.youtube.com/watch?v=QAKq8UBv4GI&list=LL&index=44&t=3s
//https://stackoverflow.com/questions/54485656/how-do-i-link-auth-users-to-collection-in-firestore
//https://www.youtube.com/watch?v=KiCluB_vijA&list=LL&index=41
//https://firebase.google.com/docs/auth?hl=de
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fhswf.einkaufslisteandroid.datenpersistierung.FirestoreManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.Logger;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.regex.Pattern;

/**
 * Verwaltet die Benutzer Anmeldung mit E-Mail/Passwort und Google-Signin.
 * Prüft, ob ein Benutzer bereits eingeloggt ist, ermöglicht auch das Zurücksetzen des Passworts.
 */
public class Login extends AppCompatActivity {

    EditText editTextEmail, editTextPasswort;
    Button buttonLogin;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textViewRegister;
    SignInButton googleBtn;
    GoogleSignInClient googleSignInClient;
    TextView textViewForgotPassword;
    private static final int RC_SIGN_IN = 2000;

    /**
     * Prüft beim Starten der App, ob Benutzer bereits eingeloggt ist.
     * Wenn er bereits eingeloggt sein sollte, dann wird die MainActivity gestartet.
     */
    @Override
    protected void onStart() { //Wenn man bereits eingeloggt ist
        super.onStart();
        FirebaseUser currUser = mAuth.getCurrentUser();
        if(currUser != null){
            //Intent -> eine Art Nachricht/Befehl, wird verwendet um Aktionen zw. Komponenten auszulösen
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish(); //Beendet aktuelle Activity
        }
    }

    /**
     * Setzt die Benutzeroberfläche für die Anmeldung.
     * Blendet zusätzlich die Systemleisten aus.
     * @param savedInstanceState Der zuvor gespeicherte Zustand der Activity.
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        uiHide();

        editTextEmail = findViewById(R.id.emailText);
        editTextPasswort = findViewById(R.id.passwordText);
        buttonLogin = findViewById(R.id.loginBtn);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        textViewRegister = findViewById(R.id.registerText);
        googleBtn = findViewById(R.id.googleBtn);
        textViewForgotPassword = findViewById(R.id.forgotPasswordText);

        setupGoogleLogin();

        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        //TODO Hier Methode checUServeri
        checkUserVerif();

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
                finish();
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        // Passwort vergessen
        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePasswordForgot();
            }
        });

    }

    /**
     * Verarbeitet die Anmeldung des Benutzers.
     */
    private void handleLogin(){
        progressBar.setVisibility(View.VISIBLE);
        String email, password;
        email = String.valueOf(editTextEmail.getText());
        password = String.valueOf(editTextPasswort.getText());

        if(email.isEmpty()){
            Toast.makeText(Login.this, "Gebe bitte eine E-Mail-Adresse ein!", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if(password.isEmpty()){
            Toast.makeText(Login.this, "Gebe bitte ein Passwort ein!", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (!Pattern.matches("^[^@]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", email)) {
            Toast.makeText(Login.this, "Keine gültige E-Mail-Adresse!", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Login erfolgreich", Toast.LENGTH_SHORT).show();

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                saveFcmToken(user.getUid());
                            }

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();  // Das schließt das Login-Fenster
                        } else {
                            Toast.makeText(Login.this, "Authentifizierung fehlgeschlagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Speichert das Firebase Cloud Messaging (FCM) Token des Benutzers in Firestore.
     *
     * @param userId Die ID des angemeldeten Benutzers.
     */
    private void saveFcmToken(String userId) {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    if (token != null && !token.isEmpty()) {
                        FirebaseFirestore.getInstance().collection("benutzer")
                                .document(userId)
                                .update("fcmToken", token)
                                .addOnSuccessListener(aVoid -> Log.d("DEBUGFCM", "FCM-Token gespeichert: " + token))
                                .addOnFailureListener(e -> Log.e("ERRORFCM", "Fehler beim Speichern des FCM-Tokens", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("ERRORFCM", "Fehler beim Abrufen des FCM-Tokens", e));
    }

    /**
     * Verarbeitet das Zurücksetzen des Passworts.
     */
    private void handlePasswordForgot(){
        String email = String.valueOf(editTextEmail.getText());
        if (email.isEmpty()) {
            Toast.makeText(Login.this, "Gebe bitte eine E-Mail-Adresse ein!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Pattern.matches("^[^@]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", email)) {
            Toast.makeText(Login.this, "Keine gültige E-Mail-Adresse!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Passwort Zurücksetzen E-Mail senden
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Login.this, "E-Mail zum Zurücksetzen des Passworts wurde gesendet.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Login.this, "Fehler beim Senden der E-Mail.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Versteckt die Systemleisten.
     */
    private void uiHide(){
        // Systemleisten ausblenden
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void checkUserVerif(){
        FirebaseUser user = mAuth.getCurrentUser();

        //Prüfen, ob bereits verifiziert
        if(user != null && user.isEmailVerified()){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }else{
            mAuth.signOut(); //Bessere Sicherheit, kann aber vllt weg
        }
    }

    /**
     * Initialisiert die Google-Sign-In-Optionen für die Anmeldung mit Google.
     */
    private void setupGoogleLogin(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    /**
     * Startet den Google Sign-In-Prozess.
     */
    private void signIn(){
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    /**
     * Verarbeitet das Ergebnis des Google Sign-Ins.
     * Falls die Anmeldung erfolgreich war, wird das Google-Token zur Firebase-Authentifizierung weitergeleitet.
     *
     * @param requestCode Der Anfragecode, um den Sign-In-Vorgang zu identifizieren.
     * @param resultCode  Das Ergebnis des Sign-Ins (z. B. erfolgreich oder abgebrochen).
     * @param data        Die zurückgegebenen Daten des Sign-In-Vorgangs.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Anmeldung ist fehlgeschlagen: " + e, Toast.LENGTH_SHORT).show();
                Log.e("Fehler: ", e.getMessage());
                Log.e("GoogleSignIn", "Fehler", task.getException());
            }
        }
    }

    /**
     * Authentifiziert den Benutzer mit Firebase basierend auf dem Google-ID-Token.
     * Nach erfolgreicher Authentifizierung wird der Benutzer in der Firestore-Datenbank gespeichert
     * und zur MainActivity weitergeleitet.
     *
     * @param idToken Das Google-ID-Token, das für die Anmeldung bei Firebase verwendet wird.
     */
    private void firebaseAuth(String idToken){
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirestoreManager firestoreManager = new FirestoreManager();
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();
                    firestoreManager.saveUser(user.getUid(), user.getEmail());  //Zu Benutzern hinzufügen
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    Toast.makeText(Login.this, "Login ist fehlgeschlagen!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}