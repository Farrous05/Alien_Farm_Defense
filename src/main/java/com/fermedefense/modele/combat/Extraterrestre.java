package com.fermedefense.modele.combat;

/**
 * Représente un extraterrestre qui attaque la ferme.
 *
 * Chaque alien a des PV, des dégâts qu'il inflige au joueur/à la ferme,
 * et une vitesse d'attaque. Lors d'un combat (automatique),
 * joueur et alien échangent des coups en tour par tour accéléré.
 *
 * Classe modèle pure.
 */
public class Extraterrestre {

    private final String nom;
    private int pointsDeVie;
    private final int pointsDeVieMax;
    private final int degats;
    private final long cooldownMs; // temps entre deux attaques (ms)

    public Extraterrestre(String nom, int pointsDeVieMax, int degats, long cooldownMs) {
        this.nom = nom;
        this.pointsDeVieMax = pointsDeVieMax;
        this.pointsDeVie = pointsDeVieMax;
        this.degats = degats;
        this.cooldownMs = cooldownMs;
    }

    /**
     * Inflige des dégâts à cet alien.
     *
     * @param montant dégâts reçus
     */
    public void subirDegats(int montant) {
        if (montant <= 0) return;
        this.pointsDeVie = Math.max(0, this.pointsDeVie - montant);
    }

    /**
     * Remet l'alien à pleine vie (pour réutilisation dans une vague).
     */
    public void reinitialiser() {
        this.pointsDeVie = pointsDeVieMax;
    }

    public boolean isVivant() {
        return pointsDeVie > 0;
    }

    // --- Accesseurs ---

    public String getNom() { return nom; }
    public int getPointsDeVie() { return pointsDeVie; }
    public int getPointsDeVieMax() { return pointsDeVieMax; }
    public int getDegats() { return degats; }
    public long getCooldownMs() { return cooldownMs; }

    public double getRatioPv() {
        return (double) pointsDeVie / pointsDeVieMax;
    }

    @Override
    public String toString() {
        return nom + " [PV=" + pointsDeVie + "/" + pointsDeVieMax
                + ", dég=" + degats + ", cd=" + cooldownMs + "ms]";
    }
}
