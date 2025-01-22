package com.fhswf.einkaufslisteandroid.datenpersistierung;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirestoreManager {

    private FirebaseFirestore db;
    private Context context;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    public FirestoreManager(Context context) {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        this.context = context;
        this.user = FirebaseAuth.getInstance().getCurrentUser();
    }


    /**
     * Speichert nur eine Liste und zwar die die man grad erstellt zur Firebase Datenbank
     * Problem: Man will gesamte bzw. alle listen speichern
     * @param gesamtListe
     */
    public void saveListToFirestore(String gesamtListe) {
        if (user == null) {
            Toast.makeText(context, "Bitte melden Sie sich an!", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = user.getUid();  // UID des angemeldeten Nutzers

        Map<String, Object> listData = new HashMap<>();
        listData.put("listName", gesamtListe);
        listData.put("products", new HashMap<String, Object>()); // Leere Produktliste erstmal

        db.collection("Einkaufslisten").document(uid)
                .set(listData)
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Liste erfolgreich gespeichert mit ID: " );
                    Toast.makeText(this.context, "Erfolgreich gespeichert!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    System.err.println("Fehler beim Speichern der Liste: " + e.getMessage());
                    Toast.makeText(this.context, "Bitte überprüfen Sie Ihre Internetverbindung!", Toast.LENGTH_SHORT).show();
                });
    }

    public HashMap<String, Object> getListFromFirestore(){
        db.collection("Einkaufslisten")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                Log.d("Firestore", document.getId() + " => " + document.getData());

                            }
                        }else {
                            Log.w("Firestore", "Fehler beim Erhalt der Daten!", task.getException());
                        }
                    }
                });
        HashMap<String, Object> h = new HashMap<>();
        return h;
    }
}
