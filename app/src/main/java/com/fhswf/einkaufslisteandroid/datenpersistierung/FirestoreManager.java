package com.fhswf.einkaufslisteandroid.datenpersistierung;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreManager {
    private final FirebaseFirestore db;

    public FirestoreManager() {
        db = FirebaseFirestore.getInstance();
    }

    public void saveList(String userId, String listName, FirestoreCallback callback) {
        Map<String, Object> listData = new HashMap<>();
        listData.put("name", listName);

        db.collection("users").document(userId).collection("lists").document(listName)
                .set(listData)
                .addOnSuccessListener(aVoid -> callback.onSuccess("Liste gespeichert"))  //Toast Nachricht wenn Liste gespeichert wurde
                .addOnFailureListener(e -> callback.onFailure("Fehler beim Speichern: " + e.getMessage()));
    }

    public void getLists(String userId, FirestoreCallbackList callback) {
        db.collection("users").document(userId).collection("lists")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> callback.onSuccess(queryDocumentSnapshots.getDocuments()))
                .addOnFailureListener(e -> callback.onFailure("Fehler beim Laden: " + e.getMessage()));


    }


    //Ohne Interfaces===?
    public interface FirestoreCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }

    public interface FirestoreCallbackList {
        void onSuccess(List<DocumentSnapshot> documents);
        void onFailure(String errorMessage);
    }
}
