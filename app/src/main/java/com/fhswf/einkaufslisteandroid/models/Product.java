package com.fhswf.einkaufslisteandroid.models;

/**
 * Klasse zum darstellen eines Produkts.
 * Enthält alle relevanten Informationen zu einem Produkt.
 */
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


    /**
     * Standard-Konstruktor für die Firestore-Deserialisierung.
     */
    public Product() {

    }

    /**
     * Konstruktor für ein Produkt.
     * @param name Name des Produkts.
     * @param imageUrl Produktbild eines Produkts.
     * @param zutaten Zutaten eines Produkts.
     * @param nutriments Nähwerte eines Produkts.
     * @param store Verkaufsstelle des Produkts.
     * @param allergene Allergene eines Produkts.
     * @param gekauft Status, ob das Produkt gekauft wurde.
     * @param menge Menge des Produkts.
     */
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

    /**
     * Konstruktor für ein Produkt, aber ohne Mengenangabe.
     * @param name Name des Produkts.
     * @param imageURL Produktbild eines Produkts.
     * @param marke Marke des Produkts.
     * @param store Verkaufsstelle des Produkts.
     * @param nutrients Nähwerte eines Produkts.
     * @param zutaten Zutaten eines Produkts.
     * @param allergene Allergene eines Produkts.
     */
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
        if (obj == null || getClass() != obj.getClass()){
            return false;
        }
        Product product = (Product) obj;
        return name != null && name.equals(product.name); // Vergleiche nach dem Namen (oder einer anderen eindeutigen Eigenschaft)
    }


    /**
     * Gibt den Namen des Produkts zurück.
     * @return Name des Produkts.
     */
    public String getName() {
        return name;
    }

    /**
     * Gibt das Produktbild zurück.
     * @return Produktbild des Produkts.
     */
    public String getImageURL() {
        return imageURL;
    }

    /**
     * Gibt die Marke des Produkts zurück.
     * @return Marke des Produkts.
     */
    public String getMarke() {
        return marke;
    }

    /**
     * Gibt die Allergene des Produkts zurück.
     * @return Allergene des Produkts.
     */
    public String getAllergene() {
        return allergene;
    }

    /**
     * Gibt die Verkaufsstelle des Produkts zurück.
     * @return Verkaufsstelle des Produkts.
     */
    public String getStore() {
        return store;
    }

    /**
     * Gibt die Zutaten des Produkts zurück.
     * @return Zutaten des Produkts.
     */
    public String getNaehrwerte() {
        return naehrwerte;
    }

    /**
     * Gibt die Zutaten des Produkts zurück.
     * @return Zutaten des Produkts.
     */
    public String getZutaten() {
        return zutaten;
    }

    /**
     * Gibt den Status des Produkts zurück, ob das Produkt gekauft wurde.
     * @return Status des Produkts.
     */
    public boolean getGekauft() {
        return gekauft;
    }

    /**
     * Setzt den Status eines Produktes, ob gekauft oder nicht gekauft.
     * @param gekauft Status des Produktes.
     */
    public void setGekauft(boolean gekauft) {
        this.gekauft = gekauft;
    }

    /**
     * Gibt die Menge des Produkts zurück.
     * @return Menge des Produkts.
     */
    public String getMenge() {
        return menge;
    }
}
