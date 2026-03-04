package com.fermedefense.modele.ferme;

/**
 * Classe abstraite représentant un animal de la ferme.
 * 
 * Chaque animal a un nom, une position dans la ferme,
 * et un cycle de mise à jour piloté par la boucle de jeu externe.
 */
public abstract class Animal {

    private final String nom;
    private double x;
    private double y;

    protected Animal(String nom, double x, double y) {
        this.nom = nom;
        this.x = x;
        this.y = y;
    }

    // ---- Cycle de vie ----

    /**
     * Met à jour l'état interne de l'animal (croissance, production…).
     * Appelé chaque tick par Ferme.mettreAJour().
     *
     * @param deltaMs temps écoulé depuis le dernier tick, en millisecondes
     */
    public abstract void mettreAJour(long deltaMs);

    /**
     * @return true si l'animal produit actuellement des revenus.
     */
    public abstract boolean isProductif();

    /**
     * @return le montant de monnaie généré par cycle de production.
     */
    public abstract int getRevenusParCycle();

    // ---- Accesseurs ----

    public String getNom() { return nom; }

    public double getX() { return x; }
    public double getY() { return y; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
}
