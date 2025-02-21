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

/**
 * Klasse für den Umgang mit Firestore, um Listen und Gruppenlisten zu verwalten
 */
public class FirestoreManager {
    private final FirebaseFirestore db;

    /**
     * Der Konstruktor der die Verbindung zur Datenbank aufbaut
     */
    public FirestoreManager() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Methode um registrierenden Nutzer abzuspeichern in der FirestoreDB
     * @param uid des Nutzers
     * @param email des Nutzers
     */
    public void saveUser(String uid, String email){
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        db.collection("benutzer").document(uid).set(userData)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Benutzer erfolgreich gespeichert");
                })
                .addOnFailureListener(e -> {System.err.println("Fehler beim Speichern des Benutzers: " + e.getMessage());
                });
    }

    /**
     * Erstellt eine neue Einkaufsliste mit einer automatisch generierten ID.
     * @param userId ID des Nutzers der die Einkaufsliste erstellt.
     * @param listName Name der zu erstellenden Liste.
     * @param products eine Liste die später mit Instanzen der Klasse Product gefüllt werden soll.
     * @param onSuccess Callback mit der erzeugten listId.
     * @param onFailure Callback bei Fehlern.
     */
    public void saveList(String userId, String listName, List<Product> products,
                         OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        db.collection("lists")
                .whereArrayContains("members", userId)
                .whereEqualTo("name", listName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Falls eine Liste mit demselben Namen für diesen User existiert
                        onFailure.onFailure(new Exception("Eine Liste mit diesem Namen existiert bereits."));
                    } else {
                        // Neue Liste erstellen, da noch keine existiert
                        String listId = db.collection("lists").document().getId();

                        Map<String, Object> listData = new HashMap<>();
                        listData.put("name", listName);
                        listData.put("products", products);
                        listData.put("members", Arrays.asList(userId));

                        db.collection("lists").document(listId)
                                .set(listData)
                                .addOnSuccessListener(aVoid -> onSuccess.onSuccess(listId))
                                .addOnFailureListener(onFailure);
                    }
                })
                .addOnFailureListener(onFailure);
    }


    /**
     * Fügt einen neuen Benutzer zu einer bestehenden Liste hinzu.
     * @param listId ID der jeweiligen Liste.
     * @param newUserId die ID des Nutzers der einer Liste hinzugefügt werden soll.
     * @param onSuccess Callback bei Erfolg.
     * @param onFailure Callback bei Fehlern.
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
     * @param userId ID des Benutzers.
     * @param onSuccess Callback mit einer Liste der DokumentSnapshots.
     * @param onFailure Callback bei Fehlern.
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
     * Methode die alle Listen aufruft, die mehr als einen Member haben (also Gruppen-Liste)
     * @param userId ID des Nutzers der gerade die App verwendet.
     * @param group true, wenn es Gruppen-Listen sind, false für Einzel-Listen.
     * @param onSuccess Callback mit einer Liste der DokumentSnapshots.
     * @param onFailure Callback bei Fehlern.
     */
    public void getUserOrGroupLists(String userId, boolean group,
                                            OnSuccessListener<List<DocumentSnapshot>> onSuccess, OnFailureListener onFailure) {

        db.collection("lists")
                .whereArrayContains("members", userId)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        onFailure.onFailure(error);
                        return;
                    }
                    List<DocumentSnapshot> groupLists = new ArrayList<>();
                    List<DocumentSnapshot> singleLists = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        List<String> members = (List<String>) doc.get("members");
                        if (members != null) {
                            if (members.size() > 1) {
                                groupLists.add(doc);
                            } else {
                                singleLists.add(doc);
                            }
                        }
                    }
                    onSuccess.onSuccess(group ? groupLists : singleLists);
                });
    }


    /**
     * Methode die den Ersteller einer Liste zurückgeben soll (über onSuccess)
     * @param listId ID der Liste dessen Ersteller ausgegeben werden soll.
     * @param onSuccess Callback mit der ErstellerID.
     * @param onFailure Callback-Funktion bei Fehlern.
     */
    public void getCreator(String listId, OnSuccessListener<String> onSuccess, OnFailureListener onFailure){
        db.collection("lists").document(listId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> members = (List<String>) documentSnapshot.get("members");
                    onSuccess.onSuccess(members.get(0));
                }).addOnFailureListener(onFailure);
    }

    /**
     * Entfernt einen Benutzer aus einer Liste.
     * @param listId ID der Liste aus der ein Nutzer entfernt werden soll.
     * @param userID ID des Nutzers der aus der Liste entfernt werden soll.
     * @param onSuccess Callback bei Erfolg.
     * @param onFailure Callback bei Fehlern.
     */
    public void leaveList(String listId, String userID, OnSuccessListener<String> onSuccess, OnFailureListener onFailure){
        db.collection("lists").document(listId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> members = (List<String>) documentSnapshot.get("members");
                    members.remove(userID);

                    db.collection("lists").document(listId)
                            .update("members", members)
                            .addOnSuccessListener(aVoid -> onSuccess.onSuccess(null))
                            .addOnFailureListener(onFailure);
                }).addOnFailureListener(onFailure);
    }

    /**
     * Fügt ein Produkt zu einer Liste hinzu.
     * @param listId ID der Liste zu der ein Produkt hinzuzufügen ist.
     * @param context wird benötigt, um eine Toast-Meldung anzuzeigen (in der Activity)
     * @param newProduct das ausgewählte Produkt.
     * @param onSuccess Callback bei Erfolg.
     * @param onFailure Callback bei Fehlern.
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
     * @param listId ID der Liste aus der ein Produkt entfernt werden soll.
     * @param productToDelete das zu löschende Produkt.
     * @param onSuccess Callback bei Erfolg.
     * @param onFailure Callback bei Fehlern.
     */
    public void deleteProductFromList(String listId, Product productToDelete,
                                      OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("lists").document(listId)
                .update("products", FieldValue.arrayRemove(productToDelete))
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Ermittelt die "listId" anhand des "listName".
     * @param listName Name der Liste.
     * @param onSuccess Callback mit der gefundenen listId.
     * @param onFailure Callback bei Fehlern.
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

    /**
     * Aktualisiert den Gekauft-Status eines Produkts in einer Liste.
     * @param listId ID der Liste.
     * @param product Das Produkt mit dem neuen Status.
     * @param onSuccess Callback bei Erfolg.
     * @param onFailure Callback bei Fehlern.
     */
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

    /**
     * Löscht eine Liste.
     * @param listId ID der zu löschenden Liste.
     * @param onSuccess Callback bei Erfolg.
     * @param onFailure Callback bei Fehlern.
     */
    public void deleteList(String listId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("lists").document(listId)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Holt die E-Mail-Adressen aller Mitglieder einer Liste.
     *
     * @param listId die ID der Liste.
     * @param onSuccess Callback-Funktion mit einer Liste der E-Mails.
     * @param onFailure Callback-Funktion bei Fehlern.
     */
    public void getUserEmailsByListId(String listId, OnSuccessListener<List<String>> onSuccess, OnFailureListener onFailure) {
        db.collection("lists").document(listId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> userIds = (List<String>) documentSnapshot.get("members");

                    List<String> emails = new ArrayList<>();
                    for (String userId : userIds) {
                        db.collection("benutzer").document(userId).get()
                                .addOnSuccessListener(userDoc -> {
                                    String email = userDoc.getString("email");
                                    emails.add(email);
                                    if (emails.size() == userIds.size()) {
                                        onSuccess.onSuccess(emails);
                                    }
                                })
                                .addOnFailureListener(onFailure);
                    }
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Entfernt einen Benutzer aus einer Liste basierend auf dessen E-Mail.
     *
     * @param listId die ID der Liste.
     * @param userEmail Die E-Mail des zu entfernenden Benutzers.
     * @param onSuccess Callback bei Erfolg.
     * @param onFailure Callback bei Fehlern.
     */
    public void removeUserFromList(String listId, String userEmail,
                                   OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        // Suche in der "benutzer"-Collection nach dem Benutzer mit der angegebenen E-Mail.
        db.collection("benutzer")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        // Wir nehmen den ersten Treffer (es sollte nur einen Benutzer mit dieser E-Mail geben)
                        String userId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Entferne den gefundenen Benutzer aus dem Array-Feld "members" der Liste
                        db.collection("lists").document(listId)
                                .update("members", FieldValue.arrayRemove(userId))
                                .addOnSuccessListener(onSuccess)
                                .addOnFailureListener(onFailure);
                    } else {
                        onFailure.onFailure(new Exception("Benutzer nicht gefunden!"));
                    }
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Hilfsfunktion für das Wischen der User in GroupsFragment, beim Creator kann nicht gewischt
     * werden.
     * @param listId die ID der Liste.
     * @param onSuccess Callback-Funktion mit der E-Mail des Erstellers.
     * @param onFailure Callback-Funktion bei Fehlern.
     */
    public void getOwnerEmail(String listId, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        getCreator(listId, creatorId -> {
            db.collection("benutzer").document(creatorId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String email = documentSnapshot.getString("email");
                        onSuccess.onSuccess(email);
                    })
                    .addOnFailureListener(onFailure);
        }, onFailure);
    }
}