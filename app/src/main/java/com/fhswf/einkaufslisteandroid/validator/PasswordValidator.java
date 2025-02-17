package com.fhswf.einkaufslisteandroid.validator;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


/**
 * Klasse für die Validation von Passwörtern, die bei der Registrierung erstellt werden.
 */
public class PasswordValidator extends AppCompatActivity {

    private final TextView checkLength;
    private final TextView checkUppercase;
    private final TextView checkSpecialChar;
    private final TextView checkNumber;
    private final Context context;
    private LinearLayout linearLayoutAnforderung;

    /**
     * Konstruktor für die passwort Validation.
     * @param context der Activity, hier Register.
     * @param checkLength TextView für Feedback über Länge des Passwortes.
     * @param checkUppercase TextView für Feedback über enthaltene Großbuchstaben.
     * @param checkSpecialChar TextView für Feedback über enthaltene Sonderzeichen.
     * @param checkNumber TextView für Feedback über enthaltene Zahlen.
     */
    public PasswordValidator(Context context, TextView checkLength, TextView checkUppercase, TextView checkSpecialChar, TextView checkNumber) {
        this.context = context;
        this.checkLength = checkLength;
        this.checkUppercase = checkUppercase;
        this.checkSpecialChar = checkSpecialChar;
        this.checkNumber = checkNumber;

        this.linearLayoutAnforderung = ((AppCompatActivity) context).findViewById(com.fhswf.einkaufslisteandroid.R.id.validation_password);
    }

    /**
     * Textwatcher Komponente die das eingegebene Passwort, bei jeder eingabe kontrolliert bzw. jede
     * Änderung registriert.
     * @return einen TextWatcher, der onTextChanged überschreibt, um das eingegebene Passwort zu
     * validieren.
     */
    public TextWatcher getPasswordTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            /**
             * Diese Methode wird während der Textänderung aufgerufen.
             * Hier wird das eingegebene Passwort überprüft und das UI-Feedback entsprechend
             * aktualisiert.
             * @param s Das aktuelle Text-CharsSequence.
             * @param start Der Startindex der Änderung.
             * @param before Anzahl der Zeichen vor der Änderung.
             * @param count Anzahl der geänderten Zeichen.
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();

                // Sichtbarkeit des LinearLayouts ändern, wenn die Eingabe nicht leer ist
                if (!password.isEmpty()) {
                    linearLayoutAnforderung.setVisibility(View.VISIBLE);
                } else {
                    linearLayoutAnforderung.setVisibility(View.GONE);
                }

                // mind. 8 Zeichen prüfen
                if (password.length() >= 8) {
                    checkLength.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                } else {
                    checkLength.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                }

                // Großbuchstaben prüfen
                if (password.matches(".*[A-Z].*")) {
                    checkUppercase.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                } else {
                    checkUppercase.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                }

                // Sonderzeichen prüfen
                if (password.matches(".*[_!@#$%^&*(),.?\":{}|<>].*")) {
                    checkSpecialChar.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                } else {
                    checkSpecialChar.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                }

                // Zahl prüfen
                if (password.matches(".*\\d.*")) { // Mindestens eine Zahl
                    checkNumber.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                } else {
                    checkNumber.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }
}
