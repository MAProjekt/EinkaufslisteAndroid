package com.fhswf.einkaufslisteandroid.models;

public class Produkt {
    private String produktnname;
    private String produktid;

    public Produkt(String produktnname, String produktid){
        this.produktnname = produktnname;
        this.produktid = produktid;
    }

    public String getProduktid() {
        return produktid;
    }

    public String getProduktnname() {
        return produktnname;
    }
}
