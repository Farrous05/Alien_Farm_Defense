package com.fermedefense.modele.joueur;

/**
 * Une action du joueur qui prend du temps à s'exécuter.
 *
 * L'action démarre, accumule du temps via {@link #mettreAJour(long)},
 * et se termine quand le temps accumulé atteint la durée requise.
 * La progression (0.0 → 1.0) peut être lue pour afficher une barre.
 *
 * Classe modèle pure.
 */
public class ActionDuree {

    /** Types d'actions à durée. */
    public enum TypeAction {
        RECOLTE,
        ACHAT
    }

    private final TypeAction type;
    private final long dureeMs;
    private long tempsEcoule;
    private boolean terminee;

    public ActionDuree(TypeAction type, long dureeMs) {
        this.type = type;
        this.dureeMs = dureeMs;
        this.tempsEcoule = 0;
        this.terminee = false;
    }

    /**
     * Fait avancer l'action d'un tick.
     *
     * @param deltaMs temps écoulé
     * @return true si l'action vient de se terminer pendant ce tick
     */
    public boolean mettreAJour(long deltaMs) {
        if (terminee) return false;
        tempsEcoule += deltaMs;
        if (tempsEcoule >= dureeMs) {
            terminee = true;
            return true;
        }
        return false;
    }

    /** Progression entre 0.0 et 1.0. */
    public double getProgression() {
        return Math.min(1.0, (double) tempsEcoule / dureeMs);
    }

    public TypeAction getType() { return type; }
    public long getDureeMs() { return dureeMs; }
    public boolean isTerminee() { return terminee; }

    /** Label lisible pour la barre. */
    public String getLabel() {
        return switch (type) {
            case RECOLTE -> "Récolte...";
            case ACHAT   -> "Achat...";
        };
    }
}
