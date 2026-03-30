package com.fermedefense.utilitaire;

/**
 * Constantes globales du jeu.
 */
public final class Constantes {

    private Constantes() {}

    // --- Fenêtre & Carte ---
    public static final int LARGEUR_CARTE = 900;
    public static final int HAUTEUR_FENETRE = 700;
    public static final String TITRE_FENETRE = "Alien Farm Defense";
    
    public static final int LARGEUR_SIDEBAR = 220;
    public static final int LARGEUR_FENETRE = LARGEUR_CARTE + LARGEUR_SIDEBAR;
    public static final int HAUTEUR_CARTE = HAUTEUR_FENETRE - 60; // place pour le HUD

    // --- Joueur ---
    public static final double VITESSE_JOUEUR = 180;
    public static final int PV_MAX_JOUEUR = 100;
    public static final int MONNAIE_INIT = 200;

    // --- Partie ---
    public static final long TEMPS_NIVEAU_MS = 120_000; // 2 min

    // --- Game loop ---
    public static final int FPS_CIBLE = 60;
    public static final long MS_PAR_FRAME = 1000 / FPS_CIBLE;
}
