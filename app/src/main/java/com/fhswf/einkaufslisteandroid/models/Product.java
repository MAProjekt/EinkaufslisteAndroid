package com.fhswf.einkaufslisteandroid.models;

public class Product {
    private String name;
    private String imageURL;
    private String marke;
    private String allergene;
    private String herkunft;
    private String zutaten;
    private String naehrwerte;

    public Product(String name, String imageURL, String marke) {
        this.name = name;
        this.imageURL = imageURL;
        this.marke = marke;
    }

    public Product(String name, String imageURL, String marke, String allergene, String herkunft, String zutaten, String naehrwerte) {
        this.name = name;
        this.imageURL = imageURL;
        this.marke = marke;
        this.allergene = allergene;
        this.herkunft = herkunft;
        this.zutaten = zutaten;
        this.naehrwerte = naehrwerte;
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

    public String getHerkunft() {
        return herkunft;
    }

    public String getNaehrwerte() {
        return naehrwerte;
    }

    public String getZutaten() {
        return zutaten;
    }
}
