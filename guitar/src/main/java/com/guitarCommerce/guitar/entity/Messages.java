package com.guitarCommerce.guitar.entity;

public class Messages {
    private String generic; // Messaggio generico
    private String specific; // Messaggio specifico (es. errorUsername, successPhone)
    private String specificKey; // Chiave specifica (es. "Username", "Phone")
    private boolean isError; // Indica se è un errore o un successo

    public Messages(String message, String specificKey, boolean isError) {
        this.generic = message;
        this.specific = message;
        this.specificKey = specificKey;
        this.isError = isError;
    }

    // Getter
    public String getGeneric() { return generic; }
    public String getSpecific() { return specific; }
    public String getSpecificKey() { return specificKey; }
    public boolean isError() { return isError; }
}
