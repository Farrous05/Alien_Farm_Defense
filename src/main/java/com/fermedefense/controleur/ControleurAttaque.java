package com.fermedefense.controleur;

import java.util.List;

import com.fermedefense.modele.combat.Arme;
import com.fermedefense.modele.combat.Attaque;
import com.fermedefense.modele.combat.Extraterrestre;
import com.fermedefense.modele.combat.ResultatCombat;
import com.fermedefense.modele.joueur.Joueur;
import com.fermedefense.modele.progression.Niveau;

/**
 * Gère les attaques extraterrestres intermédiaires.
 *
 * Quand la BarreProgression déclenche un événement ATTAQUE_INTERMEDIAIRE,
 * le ControleurJeu crée ou délègue à ce contrôleur. Il :
 *   1. Génère la vague d'aliens via le Niveau
 *   2. Fait tourner l'Attaque tick par tick
 *   3. Signale quand la vague est terminée (victoire ou défaite)
 *
 * Pendant une attaque en cours, le jeu continue de tourner
 * (la ferme produit, le joueur se déplace) mais la barre
 * de progression est en pause.
 */
public class ControleurAttaque {

    private final Niveau niveau;
    private final Arme arme;
    private Attaque attaqueCourante;
    private boolean actif;
    private int vaguesTerminees;

    public ControleurAttaque(Niveau niveau, Arme arme) {
        this.niveau = niveau;
        this.arme = arme;
        this.attaqueCourante = null;
        this.actif = false;
        this.vaguesTerminees = 0;
    }

    /**
     * Déclenche une vague d'attaque intermédiaire.
     *
     * @param indexVague index de la vague (0-based)
     */
    public void declencherVague(int indexVague) {
        List<Extraterrestre> aliens = niveau.creerVague(indexVague);
        attaqueCourante = new Attaque(aliens);
        actif = true;
    }

    /**
     * Met à jour le combat en cours.
     *
     * @param deltaMs temps écoulé
     * @param joueur  le joueur
     */
    public void mettreAJour(long deltaMs, Joueur joueur) {
        if (!actif || attaqueCourante == null) return;

        attaqueCourante.mettreAJour(deltaMs, joueur, arme);

        if (attaqueCourante.isTerminee()) {
            actif = false;
            if (attaqueCourante.getResultat() == ResultatCombat.VICTOIRE) {
                vaguesTerminees++;
            }
        }
    }

    /**
     * Vérifie si une attaque est en cours.
     */
    public boolean isActif() {
        return actif;
    }

    /**
     * Retourne le résultat de la dernière attaque (ou EN_COURS si active).
     */
    public ResultatCombat getResultat() {
        if (attaqueCourante == null) return null;
        return attaqueCourante.getResultat();
    }

    public Attaque getAttaqueCourante() { return attaqueCourante; }
    public int getVaguesTerminees() { return vaguesTerminees; }
}
