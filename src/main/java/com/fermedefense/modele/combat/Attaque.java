package com.fermedefense.modele.combat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fermedefense.modele.joueur.Joueur;

/**
 * Représente une vague d'attaque extraterrestre.
 *
 * Le combat est automatique : à chaque tick, l'arme du joueur
 * frappe l'alien courant et l'alien frappe le joueur, selon
 * leurs cooldowns respectifs. Le joueur ne contrôle rien
 * pendant le combat (on pourra ajouter l'interactivité plus tard).
 *
 * Usage :
 *   1. Créer une Attaque avec une liste d'aliens
 *   2. Appeler mettreAJour(deltaMs, joueur, arme) à chaque tick
 *   3. Vérifier getResultat() pour savoir si c'est fini
 *
 * Classe modèle pure.
 */
public class Attaque {

    private final List<Extraterrestre> aliens;
    private int indexAlienCourant;

    private long tempsCooldownJoueur;  // temps restant avant le prochain coup du joueur
    private long tempsCooldownAlien;   // temps restant avant le prochain coup de l'alien

    private ResultatCombat resultat;

    /** Dégâts totaux infligés aux aliens pendant cette vague. */
    private int totalDegatsInfliges;
    /** Dégâts totaux reçus par le joueur pendant cette vague. */
    private int totalDegatsRecus;

    public Attaque(List<Extraterrestre> aliens) {
        this.aliens = new ArrayList<>(aliens);
        this.indexAlienCourant = 0;
        this.tempsCooldownJoueur = 0;
        this.tempsCooldownAlien = 0;
        this.resultat = ResultatCombat.EN_COURS;
        this.totalDegatsInfliges = 0;
        this.totalDegatsRecus = 0;
    }

    /**
     * Crée une attaque avec un seul alien.
     */
    public Attaque(Extraterrestre alien) {
        this(List.of(alien));
    }

    /**
     * Met à jour le combat d'un tick.
     *
     * @param deltaMs temps écoulé
     * @param joueur  le joueur (subit les dégâts de l'alien)
     * @param arme    l'arme du joueur (inflige les dégâts à l'alien)
     */
    public void mettreAJour(long deltaMs, Joueur joueur, Arme arme) {
        if (resultat != ResultatCombat.EN_COURS) return;

        Extraterrestre alien = getAlienCourant();
        if (alien == null) {
            resultat = ResultatCombat.VICTOIRE;
            return;
        }

        // Réduire les cooldowns
        tempsCooldownJoueur -= deltaMs;
        tempsCooldownAlien -= deltaMs;

        // Le joueur attaque
        if (tempsCooldownJoueur <= 0) {
            alien.subirDegats(arme.getDegats());
            totalDegatsInfliges += arme.getDegats();
            tempsCooldownJoueur = arme.getCooldownMs();

            // Alien mort ? Passer au suivant
            if (!alien.isVivant()) {
                indexAlienCourant++;
                if (indexAlienCourant >= aliens.size()) {
                    resultat = ResultatCombat.VICTOIRE;
                    return;
                }
                // Reset cooldowns pour le nouvel alien
                tempsCooldownAlien = 0;
            }
        }

        // L'alien attaque le joueur
        if (tempsCooldownAlien <= 0 && alien.isVivant()) {
            joueur.subirDegats(alien.getDegats());
            totalDegatsRecus += alien.getDegats();
            tempsCooldownAlien = alien.getCooldownMs();

            if (!joueur.isVivant()) {
                resultat = ResultatCombat.DEFAITE;
                return;
            }
        }
    }

    // --- Accesseurs ---

    public Extraterrestre getAlienCourant() {
        if (indexAlienCourant >= aliens.size()) return null;
        return aliens.get(indexAlienCourant);
    }

    public ResultatCombat getResultat() {
        return resultat;
    }

    public boolean isTerminee() {
        return resultat != ResultatCombat.EN_COURS;
    }

    public List<Extraterrestre> getAliens() {
        return Collections.unmodifiableList(aliens);
    }

    public int getIndexAlienCourant() {
        return indexAlienCourant;
    }

    public int getNombreAliensRestants() {
        int count = 0;
        for (int i = indexAlienCourant; i < aliens.size(); i++) {
            if (aliens.get(i).isVivant()) count++;
        }
        return count;
    }

    public int getTotalDegatsInfliges() { return totalDegatsInfliges; }
    public int getTotalDegatsRecus() { return totalDegatsRecus; }

    @Override
    public String toString() {
        return "Attaque[" + resultat + ", aliens restants=" + getNombreAliensRestants()
                + ", dégâts infligés=" + totalDegatsInfliges
                + ", dégâts reçus=" + totalDegatsRecus + "]";
    }
}
