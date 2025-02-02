package com.fhswf.einkaufslisteandroid.datenpersistierung;

import android.content.Context;
import android.widget.Toast;

import com.fhswf.einkaufslisteandroid.models.Group;
import com.fhswf.einkaufslisteandroid.models.Product;
import com.fhswf.einkaufslisteandroid.models.ProductList;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    public void addProductToList(String userId, Context context, String selectedList, Product newProduct,
                                 OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("users").document(userId).collection("lists").document(selectedList)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    ProductList productList = documentSnapshot.toObject(ProductList.class); // Liste initialisieren
                    List<Product> products = productList.getProducts();

                    for(Product p : products){
                        if(newProduct.getName().toLowerCase().equals(p.getName().toLowerCase())){
                            Toast.makeText(context, "Produkt existiert bereits", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

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


    // Gruppen

    public void saveGroupList(String groupId, String listName, List<Product> products,
                         OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        Map<String, Object> listData = new HashMap<>();
        listData.put("name", listName);
        listData.put("products", products);

        db.collection("groups").document(groupId).collection("lists").document(listName)
                .set(listData)
                .addOnSuccessListener(aVoid -> onSuccess.onSuccess("Liste gespeichert"))
                .addOnFailureListener(onFailure);
    }

    /**
     * Methode um Listen einer Gruppe zu erhalten
     * @param groupId
     * @param onSuccess
     * @param onFailure
     */
    public void getGroupLists(String groupId,
                         OnSuccessListener<List<DocumentSnapshot>> onSuccess,
                         OnFailureListener onFailure) {
        db.collection("groups").document(groupId).collection("lists")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> onSuccess.onSuccess(queryDocumentSnapshots.getDocuments()))
                .addOnFailureListener(onFailure);
    }

    /**
     * Methode Produkt zur Gruppen-Liste hinzufügen
     * @param groupId
     * @param context
     * @param selectedList
     * @param newProduct
     * @param onSuccess
     * @param onFailure
     */
    public void addProductToGroupList(String groupId, Context context, String selectedList, Product newProduct,
                                 OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("groups").document(groupId).collection("lists").document(selectedList)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    ProductList productList = documentSnapshot.toObject(ProductList.class); // Liste initialisieren
                    List<Product> products = productList.getProducts();

                    for(Product p : products){
                        if(newProduct.getName().toLowerCase().equals(p.getName().toLowerCase())){
                            Toast.makeText(context, "Produkt existiert bereits", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    products.add(newProduct);

                    // Liste aktualisieren
                    db.collection("groups").document(groupId).collection("lists").document(selectedList)
                            .update("products", products)
                            .addOnSuccessListener(onSuccess)
                            .addOnFailureListener(onFailure);
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Methode zum Löschen von Produkten aus der Gruppenliste
     * @param groupId
     * @param selectedList
     * @param productToDelete
     * @param onSuccess
     * @param onFailure
     */
    public void deleteProductFromGroupList(String groupId, String selectedList, Product productToDelete,
                                      OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("group").document(groupId).collection("lists").document(selectedList)
                .update("products", FieldValue.arrayRemove(productToDelete))
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Methode für das aktualisieren des Gekauft booleans
     * @param groupId
     * @param listName
     * @param product
     * @param onSuccess
     * @param onFailure
     */
    public void updateGroupProductStatus(String groupId, String listName, Product product, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("group").document(groupId).collection("lists").document(listName)
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
                        db.collection("group").document(groupId).collection("lists").document(listName)
                                .update("products", products)
                                .addOnSuccessListener(onSuccess)
                                .addOnFailureListener(onFailure);
                    }
                })
                .addOnFailureListener(onFailure);
    }


    /**
     * Methode zum Erstellen einer Gruppe ohne Einkaufsliste
     * @param groupId Gruppen-ID
     * @param groupName Gruppenname
     * @param createdBy von wem Sie erstellt wurde
     * @param onSuccess Erfolgsfall
     * @param onFailure Erfolgslos
     */
    public void createGroup(String groupId, String groupName, String createdBy,
                            OnSuccessListener<String> onSuccess,
                            OnFailureListener onFailure) {
        Map<String, Object> groupData = new HashMap<>();
        groupData.put("groupName", groupName);
        groupData.put("createdBy", createdBy);

        // Speichere das Dokument in der Collection "groups" unter der Dokument-ID groupId
        db.collection("groups").document(groupId)
                .set(groupData)
                .addOnSuccessListener(aVoid -> onSuccess.onSuccess("Gruppe erstellt"))
                .addOnFailureListener(onFailure);
    }

    /**
     * Methode die die Gruppen eines Users nutzt
     * Notiz: Später vlt nicht mit userID, muss ja auch von anderen Usern in der Gruppe genutzt werden
     * @param userId Id des Nutzers
     * @param onSuccess Erfolgsfall
     * @param onFailure Erfolgslos
     */
    public void getGroupsForUser(String userId,
                                 OnSuccessListener<List<Group>> onSuccess,
                                 OnFailureListener onFailure) {
        db.collection("groups")
                .whereEqualTo("createdBy", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Group> groups = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Group group = doc.toObject(Group.class);
                        if (group != null) {
                            // Setze die Dokument-ID als Gruppen-ID
                            group.setGroupId(doc.getId());
                            groups.add(group);
                        }
                    }
                    onSuccess.onSuccess(groups);
                })
                .addOnFailureListener(onFailure);
    }


}
