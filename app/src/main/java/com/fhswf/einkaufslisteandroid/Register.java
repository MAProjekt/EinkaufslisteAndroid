package com.fhswf.einkaufslisteandroid;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fhswf.einkaufslisteandroid.validator.PasswordValidator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;


public class Register extends AppCompatActivity {
    TextInputEditText editTextEmail, editTextPassword;
    Button buttonreg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Systemleisten ausblenden
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Hides the navigation bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // Hides the status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY); // Keeps immersive mode active

        setContentView(R.layout.activity_register);
        editTextEmail = findViewById(R.id.emailText);
        editTextPassword = findViewById(R.id.passwordText);
        buttonreg = findViewById(R.id.registerBtn);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar); //Ladeschleife wenn man auf BUtton clickt
        textView = findViewById(R.id.loginNow);


        //Attribute für Passwort-Validator
        TextView checkLength = findViewById(R.id.checkLength);
        TextView checkUppercase = findViewById(R.id.checkUppercase);
        TextView checkSpecialChar = findViewById(R.id.checkSpecialChar);
        TextView checkNumber = findViewById(R.id.checkNumber);
        PasswordValidator psv = new PasswordValidator(this, checkLength, checkUppercase, checkSpecialChar, checkNumber);
        editTextPassword.addTextChangedListener(psv.getPasswordTextWatcher());


        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
        buttonreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(Register.this, "Gebe eine Mail ein", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(Register.this, "Gebe ein Passwort ein", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (!password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$")) {
                    Toast.makeText(Register.this, "Passwort muss mind. 8 Zeichen, eine Zahl, ein Sonderzeichen und ein Großbuchstaben haben ", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (!Pattern.matches("^[^@]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", email)) {
                    Toast.makeText(Register.this, "Keine gültige E-Mail-Adresse!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null){
                                        user.sendEmailVerification()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            Toast.makeText(Register.this, "Verifizierungs E-Mail gesendet. Bitte bestÃ¤tigen Sie ihre E-Mail!",
                                                                    Toast.LENGTH_SHORT).show();
                                                            progressBar.setVisibility(View.GONE);
                                                            //Zur Login-Seite weiterleiten
                                                            Intent intent = new Intent(getApplicationContext(), Login.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }else {
                                                            Toast.makeText(Register.this, "Fehler beim senden der E-Mail",
                                                                    Toast.LENGTH_SHORT).show();
                                                            progressBar.setVisibility(View.GONE);
                                                        }
                                                    }
                                                });
                                    }
                                }else{
                                    //Fehler bei Erstellung
                                    Toast.makeText(Register.this, "Fehler bei der Registrierung!",
                                            Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });
    }
}