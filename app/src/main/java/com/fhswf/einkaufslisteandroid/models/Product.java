package com.fhswf.einkaufslisteandroid.models;

public class Product {
    private String name;
    private String imageURL;
    private String marke;
    private String allergene;
    private String store;
    private String zutaten;
    private String naehrwerte;

    private boolean gekauft = false;
    private String menge;


    //Für Firestore, damit deserialisiert werden kann
    public Product() {

    }

    public Product(String name, String imageUrl, String zutaten, String nutriments, String store, String allergene, boolean gekauft, String menge){
        this.name = name;
        this.imageURL = imageUrl;
        this.store = store;
        this.naehrwerte = nutriments;
        this.zutaten = zutaten;
        this.allergene = allergene;
        this.gekauft = gekauft;
        this.menge = menge;
    }

    public Product(String name, String imageURL, String marke, String store, String nutrients, String zutaten, String allergene) {
        this.name = name;
        this.imageURL = imageURL;
        this.marke = marke;
        this.store = store;
        this.naehrwerte = nutrients;
        this.zutaten = zutaten;
        this.allergene = allergene;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Product product = (Product) obj;
        return name != null && name.equals(product.name); // Vergleiche nach dem Namen (oder einer anderen eindeutigen Eigenschaft)
    }


    public String getName() {
        return name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getMarke() {
        return marke;
    }

    public String getAllergene() {
        return allergene;
    }

    public String getStore() {
        return store;
    }

    public String getNaehrwerte() {
        return naehrwerte;
    }

    public String getZutaten() {
        return zutaten;
    }

    public boolean getGekauft() {
        return gekauft;
    }

    public void setGekauft(boolean gekauft) {
        this.gekauft = gekauft;
    }

    public String getMenge() {
        return menge;
    }
}
