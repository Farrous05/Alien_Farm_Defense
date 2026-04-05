package com.fermedefense.modele.jeu;

/**
 * Succès débloquables au cours d'une partie.
 * Chaque succès a un nom, une description et un seuil de déclenchement.
 */
public enum Succes {

    PREMIER_SANG    ("Premier sang",      "Tuer un premier alien",              1),
    FERMIER_PROSPERE("Riche fermier",     "Gagner 500g au total",             500),
    EXTERMINATEUR   ("Exterminateur",     "Tuer 50 aliens au total",           50),
    INDESTRUCTIBLE  ("Indestructible",    "Finir un niveau à PV maximum",       1),
    RANCHER         ("Éleveur",           "Avoir 3 vaches simultanément",       3);

    public final String nom;
    public final String description;
    public final int    seuil;

    Succes(String nom, String description, int seuil) {
        this.nom         = nom;
        this.description = description;
        this.seuil       = seuil;
    }
}
