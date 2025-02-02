package com.fhswf.einkaufslisteandroid.datenpersistierung;

import android.content.Context;
import android.widget.Toast;

import com.fhswf.einkaufslisteandroid.models.Product;
import com.fhswf.einkaufslisteandroid.models.ProductList;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreManager {
    private final FirebaseFirestore db;

    public FirestoreManager() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Erstellt eine neue Einkaufsliste mit einer automatisch generierten ID.
     */
    public void saveList(String userId, String listName, List<Product> products,
                         OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        String listId = db.collection("lists").document().getId();  // Generiere eindeutige ID

        Map<String, Object> listData = new HashMap<>();
        listData.put("name", listName);
        listData.put("products", products);
        listData.put("members", Arrays.asList(userId));  // Der Ersteller wird als erstes Mitglied gespeichert

        db.collection("lists").document(listId)
                .set(listData)
                .addOnSuccessListener(aVoid -> onSuccess.onSuccess(listId))
                .addOnFailureListener(onFailure);
    }

    /**
     * Fügt einen neuen Benutzer zu einer bestehenden Liste hinzu.
     */
    public void addUserToList(String listId, String newUserId,
                              OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("lists").document(listId)
                .update("members", FieldValue.arrayUnion(newUserId))  // Nutzer hinzufügen (arrayUnion : keine Duplikate)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Ruft alle Listen ab, die ein Benutzer besitzt oder mit ihm geteilt wurden.
     */
    public void getLists(String userId, OnSuccessListener<List<DocumentSnapshot>> onSuccess,
                         OnFailureListener onFailure) {
        db.collection("lists")
                .whereArrayContains("members", userId)  // Listen filtern, in denen userId in members ist
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        onSuccess.onSuccess(queryDocumentSnapshots.getDocuments()))
                .addOnFailureListener(onFailure);
    }

    /**
     * Fügt ein Produkt zu einer Liste hinzu.
     */
    public void addProductToList(String listId, Context context, Product newProduct,
                                 OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("lists").document(listId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    ProductList productList = documentSnapshot.toObject(ProductList.class);

                    if (productList != null) {
                        List<Product> products = productList.getProducts();

                        if (products == null) {
                            products = new ArrayList<>();
                        }

                        // Prüfen, ob das Produkt bereits in der Liste existiert
                        for (Product p : products) {
                            if (newProduct.getName().equalsIgnoreCase(p.getName())) {
                                Toast.makeText(context, "Produkt existiert bereits", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        products.add(newProduct);

                        db.collection("lists").document(listId)
                                .update("products", products)
                                .addOnSuccessListener(onSuccess)
                                .addOnFailureListener(onFailure);
                    } else {
                        onFailure.onFailure(new Exception("Liste nicht gefunden"));
                    }
                }).addOnFailureListener(onFailure);
    }



    /**
     * Entfernt ein Produkt aus einer Liste.
     */
    public void deleteProductFromList(String listId, Product productToDelete,
                                      OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("lists").document(listId)
                .update("products", FieldValue.arrayRemove(productToDelete))
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Ermittelt die `listId` anhand des `listName`.
     */
    public void getListIdByName(String listName, OnSuccessListener<String> onSuccess,
                                OnFailureListener onFailure) {
        db.collection("lists")
                .whereEqualTo("name", listName)  // nach Listennamen suche
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String listId = querySnapshot.getDocuments().get(0).getId();
                        onSuccess.onSuccess(listId);
                    } else {
                        onFailure.onFailure(new Exception("Liste nicht gefunden"));
                    }
                })
                .addOnFailureListener(onFailure);
    }

    public void updateProductStatus(String listId, Product product,
                                    OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("lists").document(listId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    ProductList productList = documentSnapshot.toObject(ProductList.class);

                    if (productList == null) {
                        onFailure.onFailure(new Exception("Produktliste nicht gefunden"));
                        return;
                    }

                    List<Product> products = productList.getProducts();

                    for (Product p : products) {
                        if (p.getName().equalsIgnoreCase(product.getName())) {
                            p.setGekauft(product.getGekauft());  // Setze den neuen Status
                            break;
                        }
                    }

                    db.collection("lists").document(listId)
                            .update("products", products)
                            .addOnSuccessListener(onSuccess)
                            .addOnFailureListener(onFailure);

                })
                .addOnFailureListener(onFailure);
    }

    public void deleteList(String listId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("lists").document(listId)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }



}