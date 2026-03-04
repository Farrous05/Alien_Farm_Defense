package com.fermedefense.modele.ferme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Représente la ferme où les vaches sont placées et évoluent.
 *
 * Responsabilités :
 *  • stocker les vaches (avec une capacité maximale)
 *  • propager le tick de mise à jour à chaque vache
 *  • récolter la monnaie produite par toutes les vaches productives
 *
 * Classe modèle pure – pas de thread, pas de dépendance UI.
 */
public class Ferme {

    /** Capacité par défaut. */
    public static final int CAPACITE_MAX_DEFAUT = 10;

    private final List<Vache> vaches;
    private final int capaciteMax;

    public Ferme() {
        this(CAPACITE_MAX_DEFAUT);
    }

    public Ferme(int capaciteMax) {
        this.capaciteMax = capaciteMax;
        this.vaches = new ArrayList<>();
    }

    // ---- Gestion des vaches ----

    /**
     * Ajoute une vache à la ferme.
     *
     * @param vache la vache à ajouter
     * @return true si l'ajout a réussi, false si la ferme est pleine
     */
    public boolean ajouterVache(Vache vache) {
        if (vache == null || vaches.size() >= capaciteMax) return false;
        vaches.add(vache);
        return true;
    }

    /**
     * Retire une vache de la ferme.
     */
    public boolean retirerVache(Vache vache) {
        return vaches.remove(vache);
    }

    // ---- Cycle de jeu ----

    /**
     * Met à jour toutes les vaches (croissance + production).
     * Appelé par la boucle de jeu externe.
     *
     * @param deltaMs temps écoulé depuis le dernier tick en ms
     */
    public void mettreAJour(long deltaMs) {
        for (Vache v : vaches) {
            v.mettreAJour(deltaMs);
        }
    }

    /**
     * Récolte la monnaie accumulée par toutes les vaches productives.
     *
     * @return le total récolté
     */
    public int recolterTout() {
        int total = 0;
        for (Vache v : vaches) {
            total += v.recolter();
        }
        return total;
    }

    // ---- Accesseurs ----

    public int getNombreAnimaux() {
        return vaches.size();
    }

    public int getCapaciteMax() {
        return capaciteMax;
    }

    public boolean estPleine() {
        return vaches.size() >= capaciteMax;
    }

    /**
     * @return liste non modifiable des vaches.
     */
    public List<Vache> getVaches() {
        return Collections.unmodifiableList(vaches);
    }

    /**
     * @return le nombre de vaches actuellement productives.
     */
    public int getNombreProductives() {
        int n = 0;
        for (Vache v : vaches) {
            if (v.isProductif()) n++;
        }
        return n;
    }
}
