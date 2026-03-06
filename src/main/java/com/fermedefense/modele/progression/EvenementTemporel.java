package com.fermedefense.modele.progression;

/**
 * Événement déclenché à un moment précis sur la barre de progression.
 *
 * Chaque événement a un moment (en ms), un type, et un flag
 * indiquant s'il a déjà été déclenché (pour éviter les doublons).
 */
public class EvenementTemporel {

    /** Types d'événements possibles sur la timeline. */
    public enum TypeEvenement {
        ATTAQUE_INTERMEDIAIRE,
        COMBAT_FINAL
    }

    private final long momentMs;
    private final TypeEvenement type;
    private final int indexVague; // -1 pour COMBAT_FINAL
    private boolean declenche;

    public EvenementTemporel(long momentMs, TypeEvenement type, int indexVague) {
        this.momentMs = momentMs;
        this.type = type;
        this.indexVague = indexVague;
        this.declenche = false;
    }

    /**
     * Marque cet événement comme déclenché.
     */
    public void declencher() {
        this.declenche = true;
    }

    /**
     * Réinitialise l'événement (pour rejouer un niveau).
     */
    public void reinitialiser() {
        this.declenche = false;
    }

    // --- Accesseurs ---

    public long getMomentMs() { return momentMs; }
    public TypeEvenement getType() { return type; }
    public int getIndexVague() { return indexVague; }
    public boolean isDeclenche() { return declenche; }

    @Override
    public String toString() {
        return type + " @" + (momentMs / 1000) + "s"
                + (declenche ? " [fait]" : " [en attente]");
    }
}
