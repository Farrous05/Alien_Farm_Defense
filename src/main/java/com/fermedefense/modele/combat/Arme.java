package com.fermedefense.modele.combat;

/**
 * Représente une arme utilisable en combat.
 *
 * Pour l'instant, seule l'épée (EPEE) existe.
 * Le joueur ne combat pas activement — l'arme détermine
 * les dégâts infligés automatiquement lors d'une attaque.
 *
 * Classe modèle pure.
 */
import com.fermedefense.modele.joueur.ObjetInventaire;

public class Arme implements ObjetInventaire {

    /** Épée par défaut disponible dès le départ. */
    public static final Arme EPEE = new Arme("Épée", 15, 1000);
    public static final Arme SHOTGUN = new Arme("Shotgun", 50, 2000);
    public static final Arme MINIGUN = new Arme("Minigun", 8, 250);

    private final String nom;
    private final int degats;
    private final long cooldownMs; // temps entre deux coups (ms)

    public Arme(String nom, int degats, long cooldownMs) {
        this.nom = nom;
        this.degats = degats;
        this.cooldownMs = cooldownMs;
    }

    @Override
    public String getNom() { return nom; }

    @Override
    public String getDescription() { return "Dégâts: " + degats; }

    public int getDegats() { return degats; }
    public long getCooldownMs() { return cooldownMs; }

    /**
     * Calcule le DPS (dégâts par seconde) de l'arme.
     */
    public double getDps() {
        return (degats * 1000.0) / cooldownMs;
    }

    @Override
    public String toString() {
        return nom + " (dég=" + degats + ", cd=" + cooldownMs + "ms)";
    }
}
