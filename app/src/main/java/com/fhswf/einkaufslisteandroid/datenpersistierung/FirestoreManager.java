package com.fhswf.einkaufslisteandroid.datenpersistierung;

import com.fhswf.einkaufslisteandroid.models.Product;
import com.fhswf.einkaufslisteandroid.models.ProductList;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreManager {
    private final FirebaseFirestore db;

    public FirestoreManager() {
        db = FirebaseFirestore.getInstance();
    }


    public void saveList(String userId, String listName, List<Product> products,
                         OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        Map<String, Object> listData = new HashMap<>();
        listData.put("name", listName);
        listData.put("products", products);

        db.collection("users").document(userId).collection("lists").document(listName)
                .set(listData)
                .addOnSuccessListener(aVoid -> onSuccess.onSuccess("Liste gespeichert"))
                .addOnFailureListener(onFailure); // Exception direkt weitergeben
    }


    public void getLists(String userId,
                         OnSuccessListener<List<DocumentSnapshot>> onSuccess,
                         OnFailureListener onFailure) {
        db.collection("users").document(userId).collection("lists")
                .get() //mit get() erhält man eine Sammlung von QuerySnapshots, ist eine Sammlung von Dokumenten
                .addOnSuccessListener(queryDocumentSnapshots -> onSuccess.onSuccess(queryDocumentSnapshots.getDocuments()))
                .addOnFailureListener(onFailure); // Exception direkt weitergeben
    }

    public void addProductToList(String userId, String selectedList, Product newProduct,
                                 OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("users").document(userId).collection("lists").document(selectedList)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    ProductList productList = documentSnapshot.toObject(ProductList.class); // Liste initialisieren
                    List<Product> products = productList.getProducts();
                    products.add(newProduct);

                    // Liste aktualisieren
                    db.collection("users").document(userId).collection("lists").document(selectedList)
                            .update("products", products)
                            .addOnSuccessListener(onSuccess)
                            .addOnFailureListener(onFailure);
                })
                .addOnFailureListener(onFailure);
    }

    public void deleteProductFromList(String userId, String selectedList, Product productToDelete,
                                      OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("users").document(userId).collection("lists").document(selectedList)
                .update("products", FieldValue.arrayRemove(productToDelete))
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    //TODO: Methode für das aktualisieren des Gekauft booleans
    public void updateProductStatus(String userId, String listName, Product product, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("users").document(userId).collection("lists").document(listName)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    ProductList productList = documentSnapshot.toObject(ProductList.class);
                    if (productList != null) {
                        List<Product> products = productList.getProducts();
                        for (Product p : products) {
                            if (p.equals(product)) {
                                p.setGekauft(product.getGekauft());
                                break;
                            }
                        }
                        db.collection("users").document(userId).collection("lists").document(listName)
                                .update("products", products)
                                .addOnSuccessListener(onSuccess)
                                .addOnFailureListener(onFailure);
                    }
                })
                .addOnFailureListener(onFailure);
    }



}
