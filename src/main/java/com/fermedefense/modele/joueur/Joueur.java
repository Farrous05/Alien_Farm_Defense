package com.fermedefense.modele.joueur;

import java.util.EnumSet;
import java.util.Set;

/**
 * Représente le fermier contrôlé par le joueur.
 * Le joueur a une position (x, y) et peut se déplacer
 * dans une ou plusieurs directions simultanément (mouvement diagonal).
 *
 * Classe modèle pure : pas de thread, pas de dépendance graphique.
 * La boucle de mise à jour est gérée à l'extérieur.
 */
public class Joueur {

    private double x;
    private double y;
    private double vitesse; // pixels par seconde
    private final Set<Action> directionsActives = EnumSet.noneOf(Action.class);
    private static final int TAILLE = 45;

    // --- Points de vie ---
    private int pointsDeVie;
    private int pointsDeVieMax;

    // --- Monnaie ---
    private int monnaie;

    // --- Inventaire ---
    private Inventaire inventaire;

    /**
     * Crée un joueur à la position donnée.
     *
     * @param x             position x initiale
     * @param y             position y initiale
     * @param vitesse       vitesse en pixels par seconde
     * @param pointsDeVieMax points de vie maximum
     * @param monnaieInit   monnaie de départ
     */
    public Joueur(double x, double y, double vitesse, int pointsDeVieMax, int monnaieInit) {
        this.x = x;
        this.y = y;
        this.vitesse = vitesse;
        this.pointsDeVieMax = pointsDeVieMax;
        this.pointsDeVie = pointsDeVieMax;
        this.monnaie = monnaieInit;
        this.inventaire = new Inventaire();
    }

    /**
     * Met à jour la position du joueur selon le temps écoulé.
     * Supporte le mouvement diagonal : si deux directions orthogonales sont
     * actives simultanément, le vecteur est normalisé pour conserver la vitesse.
     *
     * @param deltaMs temps écoulé depuis le dernier appel en millisecondes
     */
    public void mettreAJour(long deltaMs) {
        if (directionsActives.isEmpty()) return;
        double dx = 0, dy = 0;
        if (directionsActives.contains(Action.HAUT))   dy -= 1;
        if (directionsActives.contains(Action.BAS))    dy += 1;
        if (directionsActives.contains(Action.GAUCHE)) dx -= 1;
        if (directionsActives.contains(Action.DROITE)) dx += 1;
        if (dx == 0 && dy == 0) return;
        // Normalize diagonal so speed stays constant
        double len = Math.sqrt(dx * dx + dy * dy);
        double deplacement = vitesse * deltaMs / 1000.0;
        x += (dx / len) * deplacement;
        y += (dy / len) * deplacement;
    }

    /**
     * Appelé quand une touche directionnelle est pressée.
     */
    public void appuyerDirection(Action direction) {
        directionsActives.add(direction);
    }

    /**
     * Appelé quand une touche directionnelle est relâchée.
     */
    public void relacherDirection(Action direction) {
        directionsActives.remove(direction);
    }

    // --- Getters ---

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getTaille() {
        return TAILLE;
    }

    public boolean isEnMouvement() {
        return !directionsActives.isEmpty();
    }

    /** Returns the last-pressed direction, or null if no key is held. */
    public Action getDirectionCourante() {
        if (directionsActives.isEmpty()) return null;
        return directionsActives.iterator().next();
    }

    // --- Points de vie ---

    /**
     * Inflige des dégâts au joueur.
     *
     * @param degats quantité de dégâts (ignoré si ≤ 0)
     */
    public void subirDegats(int degats) {
        if (degats <= 0) return;
        this.pointsDeVie = Math.max(0, this.pointsDeVie - degats);
    }

    /**
     * Soigne le joueur sans dépasser le maximum.
     *
     * @param montant quantité de soin (ignoré si ≤ 0)
     */
    public void soigner(int montant) {
        if (montant <= 0) return;
        this.pointsDeVie = Math.min(this.pointsDeVieMax, this.pointsDeVie + montant);
    }

    /**
     * @return true si le joueur est encore en vie (PV > 0)
     */
    public boolean isVivant() {
        return pointsDeVie > 0;
    }

    public int getPointsDeVie() {
        return pointsDeVie;
    }

    public int getPointsDeVieMax() {
        return pointsDeVieMax;
    }

    /**
     * Augmente les PV maximum (achat d'upgrade HP).
     * Soigne aussi le joueur du même montant.
     */
    public void addPvMax(int bonus) {
        if (bonus <= 0) return;
        this.pointsDeVieMax += bonus;
        this.pointsDeVie = Math.min(this.pointsDeVie + bonus, this.pointsDeVieMax);
    }

    // --- Monnaie ---

    /** Total cumulatif de monnaie gagnée (pour le succès FERMIER_PROSPERE). */
    private int totalMonnaieGagnee;

    /**
     * Dépense de la monnaie si le joueur en a assez.
     *
     * @param montant montant à dépenser
     * @return true si la dépense a réussi, false sinon
     */
    public boolean depenser(int montant) {
        if (montant <= 0 || montant > monnaie) return false;
        this.monnaie -= montant;
        return true;
    }

    /**
     * Ajoute de la monnaie au joueur et accumule le total gagné.
     *
     * @param montant montant à ajouter (ignoré si ≤ 0)
     */
    public void ajouterMonnaie(int montant) {
        if (montant <= 0) return;
        this.monnaie += montant;
        this.totalMonnaieGagnee += montant;
    }

    public int getMonnaie() {
        return monnaie;
    }

    public int getTotalMonnaieGagnee() {
        return totalMonnaieGagnee;
    }

    public Inventaire getInventaire() {
        return inventaire;
    }

    // --- Arme Equipée ---
    
    private int indexArmeEquipee = 0;

    public com.fermedefense.modele.combat.Arme getArmeEquipee() {
        java.util.List<com.fermedefense.modele.combat.Arme> armes = getToutesLesArmes();
        if (armes.isEmpty()) return null;
        if (indexArmeEquipee >= armes.size()) indexArmeEquipee = 0;
        return armes.get(indexArmeEquipee);
    }

    public void cycleArme() {
        java.util.List<com.fermedefense.modele.combat.Arme> armes = getToutesLesArmes();
        if (armes.isEmpty()) return;
        indexArmeEquipee = (indexArmeEquipee + 1) % armes.size();
    }

    public java.util.List<com.fermedefense.modele.combat.Arme> getToutesLesArmes() {
        java.util.List<com.fermedefense.modele.combat.Arme> list = new java.util.ArrayList<>();
        for (int i = 0; i < inventaire.getLignes(); i++) {
            for (int j = 0; j < inventaire.getColonnes(); j++) {
                com.fermedefense.modele.joueur.ObjetInventaire obj = inventaire.getObjet(i, j);
                if (obj instanceof com.fermedefense.modele.combat.Arme) {
                    list.add((com.fermedefense.modele.combat.Arme) obj);
                }
            }
        }
        return list;
    }
}
