package com.fhswf.einkaufslisteandroid.datenpersistierung;

import com.fhswf.einkaufslisteandroid.models.Product;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreManager {
    private final FirebaseFirestore db;

    public FirestoreManager() {
        db = FirebaseFirestore.getInstance();
    }

    public void saveList(String userId, String listName, List<Product> products , FirestoreCallback callback) {
        Map<String, Object> listData = new HashMap<>();
        listData.put("name", listName);
        listData.put("products", products);

        //Speichert in der collection lists den ListenNamen "listName" ab dabei hat dann dieses Document die fields "name" und "products"
        db.collection("users").document(userId).collection("lists").document(listName)
                .set(listData)
                .addOnSuccessListener(aVoid -> callback.onSuccess("Liste gespeichert"))  //Toast Nachricht wenn Liste gespeichert wurde
                .addOnFailureListener(e -> callback.onFailure("Fehler beim Speichern: " + e.getMessage()));
    }



    //Ohne Interface
//    public void saveList(String userId, String listName, List<Product> products,
//                         OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
//        Map<String, Object> listData = new HashMap<>();
//        listData.put("name", listName);
//        listData.put("products", products);
//
//        db.collection("users").document(userId).collection("lists").document(listName)
//                .set(listData)
//                .addOnSuccessListener(aVoid -> onSuccess.onSuccess("Liste gespeichert"))
//                .addOnFailureListener(e -> onFailure.onFailure("Fehler beim Speichern: " + e.getMessage()));
//    }

    public void getLists(String userId, FirestoreCallbackList callback) {
        db.collection("users").document(userId).collection("lists")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> callback.onSuccess(queryDocumentSnapshots.getDocuments()))
                .addOnFailureListener(e -> callback.onFailure("Fehler beim Laden: " + e.getMessage()));


    }

    //Ohne Interface
//    public void getLists(String userId,
//                         OnSuccessListener<List<DocumentSnapshot>> onSuccess,
//                         OnFailureListener onFailure) {
//        db.collection("users").document(userId).collection("lists")
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> onSuccess.onSuccess(queryDocumentSnapshots.getDocuments()))
//                .addOnFailureListener(e -> onFailure.onFailure("Fehler beim Laden: " + e.getMessage()));
//    }



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
