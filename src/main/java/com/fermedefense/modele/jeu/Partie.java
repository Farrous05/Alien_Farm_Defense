package com.fermedefense.modele.jeu;

import java.util.ArrayList;
import java.util.List;

/**
 * Gère la logique globale d'une partie.
 * Contient l'état du jeu, le timer, la carte, le niveau actuel,
 * et coordonne le déroulement de la partie.
 */
public class Partie {

    private EtatJeu etat;
    private final Carte carte;
    private int niveau;
    private int monnaie;

    // Temps en millisecondes
    private long tempsTotal;
    private long tempsEcoule;
    private boolean enPause;

    // Moments des attaques intermédiaires (en ms depuis le début)
    private final List<Long> momentsAttaques;

    /**
     * Crée une nouvelle partie.
     *
     * @param largeurCarte largeur de la carte
     * @param hauteurCarte hauteur de la carte
     * @param tempsTotal   durée totale du niveau en millisecondes
     * @param monnaieInit  monnaie de départ
     */
    public Partie(int largeurCarte, int hauteurCarte, long tempsTotal, int monnaieInit) {
        this.carte = new Carte(largeurCarte, hauteurCarte);
        this.etat = EtatJeu.MENU;
        this.niveau = 1;
        this.monnaie = monnaieInit;
        this.tempsTotal = tempsTotal;
        this.tempsEcoule = 0;
        this.enPause = false;
        this.momentsAttaques = new ArrayList<>();
    }

    /**
     * Démarre la partie : passe en état EN_COURS.
     */
    public void demarrer() {
        this.etat = EtatJeu.EN_COURS;
        this.tempsEcoule = 0;
        this.enPause = false;
    }

    /**
     * Met à jour la partie d'un tick (appelé par la boucle de jeu).
     *
     * @param deltaMs temps écoulé depuis le dernier tick en millisecondes
     */
    public void mettreAJour(long deltaMs) {
        if (etat != EtatJeu.EN_COURS || enPause) {
            return;
        }

        tempsEcoule += deltaMs;

        // Vérifier si le temps est écoulé → combat final
        if (tempsEcoule >= tempsTotal) {
            tempsEcoule = tempsTotal;
            etat = EtatJeu.COMBAT_FINAL;
        }
    }

    /**
     * Vérifie si une attaque doit se déclencher au temps actuel.
     *
     * @param toleranceMs fenêtre de tolérance en ms
     * @return true si une attaque doit se déclencher
     */
    public boolean doitDeclencherAttaque(long toleranceMs) {
        for (Long moment : momentsAttaques) {
            if (Math.abs(tempsEcoule - moment) <= toleranceMs) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ajoute un moment d'attaque programmée.
     *
     * @param momentMs moment de l'attaque en ms depuis le début
     */
    public void ajouterMomentAttaque(long momentMs) {
        momentsAttaques.add(momentMs);
    }

    /**
     * Termine la partie avec le résultat du combat final.
     *
     * @param victoire true si le joueur a gagné
     */
    public void terminer(boolean victoire) {
        this.etat = victoire ? EtatJeu.VICTOIRE : EtatJeu.DEFAITE;
    }

    /**
     * Passe au niveau suivant : augmente la difficulté.
     *
     * @param nouveauTemps nouveau temps total en ms
     */
    public void niveauSuivant(long nouveauTemps) {
        this.niveau++;
        this.tempsTotal = nouveauTemps;
        this.tempsEcoule = 0;
        this.momentsAttaques.clear();
        this.etat = EtatJeu.MENU;
    }

    // --- Gestion de la monnaie ---

    public void ajouterMonnaie(int montant) {
        this.monnaie += montant;
    }

    public boolean depenser(int montant) {
        if (montant > monnaie) {
            return false;
        }
        this.monnaie -= montant;
        return true;
    }

    // --- Pause ---

    public void basculerPause() {
        this.enPause = !this.enPause;
    }

    // --- Getters ---

    public EtatJeu getEtat() {
        return etat;
    }

    public Carte getCarte() {
        return carte;
    }

    public int getNiveau() {
        return niveau;
    }

    public int getMonnaie() {
        return monnaie;
    }

    public long getTempsTotal() {
        return tempsTotal;
    }

    public long getTempsEcoule() {
        return tempsEcoule;
    }

    public long getTempsRestant() {
        return Math.max(0, tempsTotal - tempsEcoule);
    }

    public double getProgressionTemps() {
        return (double) tempsEcoule / tempsTotal;
    }

    public boolean isEnPause() {
        return enPause;
    }

    public List<Long> getMomentsAttaques() {
        return momentsAttaques;
    }
}
