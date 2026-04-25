package com.fermedefense.utilitaire;

/**
 * Constantes globales du jeu.
 */
public final class Constantes {

    private Constantes() {}

    // --- Monde (espace total dans lequel le joueur se déplace) ---
    // 2 zones × 25 cols × 40 px = 2000 px largeur, 35 rows × 40 px = 1400 px hauteur
    public static final int LARGEUR_CARTE = 2000;   // largeur du monde en pixels
    public static final int HAUTEUR_CARTE = 1400;   // hauteur du monde en pixels

    // --- Viewport (zone visible à l'écran, sans sidebar ni HUD) ---
    public static final int LARGEUR_VIEWPORT = 1200;
    public static final int HAUTEUR_VIEWPORT = 840;

    // --- Fenêtre ---
    public static final String TITRE_FENETRE = "Alien Farm Defense";
    public static final int LARGEUR_SIDEBAR  = 240;
    public static final int HAUTEUR_HUD      = 60;
    public static final int LARGEUR_FENETRE  = LARGEUR_VIEWPORT + LARGEUR_SIDEBAR; // 1440
    public static final int HAUTEUR_FENETRE  = HAUTEUR_VIEWPORT + HAUTEUR_HUD;    // 900

    // --- Joueur ---
    public static final double VITESSE_JOUEUR = 180;
    public static final int PV_MAX_JOUEUR = 100;
    public static final int MONNAIE_INIT = 120;

    // --- Partie ---
    public static final long TEMPS_NIVEAU_MS = 120_000; // 2 min

    // --- Game loop ---
    public static final int FPS_CIBLE = 60;
    public static final long MS_PAR_FRAME = 1000 / FPS_CIBLE;
}
