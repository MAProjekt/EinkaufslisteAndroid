package com.fhswf.einkaufslisteandroid.models;

public class Product {
    private String name;
    private String imageURL;
    private String marke;

    public Product(String name, String imageURL, String marke) {
        this.name = name;
        this.imageURL = imageURL;
        this.marke = marke;
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
}
