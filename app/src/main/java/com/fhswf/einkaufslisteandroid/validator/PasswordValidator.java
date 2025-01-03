package com.fhswf.einkaufslisteandroid.validator;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;



public class PasswordValidator extends AppCompatActivity {

    private final TextView checkLength;
    private final TextView checkUppercase;
    private final TextView checkSpecialChar;
    private final TextView checkNumber;
    private final Context context;
    private LinearLayout linearLayoutAnforderung;

    public PasswordValidator(Context context, TextView checkLength, TextView checkUppercase, TextView checkSpecialChar, TextView checkNumber) {
        this.context = context;
        this.checkLength = checkLength;
        this.checkUppercase = checkUppercase;
        this.checkSpecialChar = checkSpecialChar;
        this.checkNumber = checkNumber;

        this.linearLayoutAnforderung = ((AppCompatActivity) context).findViewById(com.fhswf.einkaufslisteandroid.R.id.validation_password);
    }

    public TextWatcher getPasswordTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

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
