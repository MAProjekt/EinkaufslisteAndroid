package com.fhswf.einkaufslisteandroid.models;

import java.util.List;


/**
 * Wrapper-Klasse damit Firestore deserialsieren kann, weil Firestore keine generischen Typen wie List<Product> eknnt.
 * Da Firestore die Liste als List<Map<String, Object>> abspeichert und nicht weiß dass die Elemente Product-Objekte sind.
 * Zusammengefasst sagt die ProductList Klasse Firestore, dass die Liste Elemente des Typs Product hat und stellte einen
 * leeren Konstruktor bereit, damit Firestore die Daten korrekt laden kann.
 */
public class ProductList {
    private List<Product> products;

    /**
     * konstruktor muss leer sein, weil Firestore
     * beim Laden der Daten Objekte automatisch erstellt und dabei keine Parameter übergeben kann
     */
    public ProductList() {
        // Leerer Konstruktor für Firestore
    }

    /**
     * Gibt die Liste der Produkte zurück.
     * @return Liste der Produkte.
     */
    public List<Product> getProducts() {
        return products;
    }
}

