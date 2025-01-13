package com.fhswf.einkaufslisteandroid.models;

public class Product {
    private String name;
    private String imageURL;
    private String marke;
    private String allergene;
    private String store;
    private String zutaten;
    private String naehrwerte;

    public Product(String name, String imageURL, String marke, String store, String nutrients, String zutaten, String allergene) {
        this.name = name;
        this.imageURL = imageURL;
        this.marke = marke;
        this.store = store;
        this.naehrwerte = nutrients;
        this.zutaten = zutaten;
        this.allergene = allergene;
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
}
