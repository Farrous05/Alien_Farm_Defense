package com.fermedefense.modele.joueur;

/**
 * Représente le fermier contrôlé par le joueur.
 * Le joueur a une position (x, y) et peut se déplacer
 * dans une direction donnée.
 * 
 * Classe modèle pure : pas de thread, pas de dépendance graphique.
 * La boucle de mise à jour est gérée à l'extérieur.
 */
public class Joueur {

    private double x;
    private double y;
    private double vitesse; // pixels par seconde
    private Action directionCourante;
    private boolean enMouvement;
    private static final int TAILLE = 30;

    // --- Points de vie ---
    private int pointsDeVie;
    private int pointsDeVieMax;

    // --- Monnaie ---
    private int monnaie;

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
        this.directionCourante = null;
        this.enMouvement = false;
        this.pointsDeVieMax = pointsDeVieMax;
        this.pointsDeVie = pointsDeVieMax;
        this.monnaie = monnaieInit;
    }

    /**
     * Met à jour la position du joueur selon le temps écoulé.
     * Appelé à chaque tick par la boucle de jeu externe.
     *
     * @param deltaMs temps écoulé depuis le dernier appel en millisecondes
     */
    public void mettreAJour(long deltaMs) {
        if (enMouvement && directionCourante != null) {
            double deplacement = vitesse * deltaMs / 1000.0;
            deplacer(directionCourante, deplacement);
        }
    }

    /**
     * Déplace le joueur dans la direction donnée.
     */
    private void deplacer(Action direction, double deplacement) {
        switch (direction) {
            case HAUT:   y -= deplacement; break;
            case BAS:    y += deplacement; break;
            case GAUCHE: x -= deplacement; break;
            case DROITE: x += deplacement; break;
        }
    }

    /**
     * Appelé quand une touche directionnelle est pressée.
     */
    public void appuyerDirection(Action direction) {
        this.directionCourante = direction;
        this.enMouvement = true;
    }

    /**
     * Appelé quand la touche directionnelle est relâchée.
     */
    public void relacherDirection(Action direction) {
        if (this.directionCourante == direction) {
            this.enMouvement = false;
            this.directionCourante = null;
        }
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
        return enMouvement;
    }

    public Action getDirectionCourante() {
        return directionCourante;
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

    // --- Monnaie ---

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
     * Ajoute de la monnaie au joueur.
     *
     * @param montant montant à ajouter (ignoré si ≤ 0)
     */
    public void ajouterMonnaie(int montant) {
        if (montant <= 0) return;
        this.monnaie += montant;
    }

    public int getMonnaie() {
        return monnaie;
    }
}
