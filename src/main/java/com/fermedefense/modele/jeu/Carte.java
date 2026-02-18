package com.fermedefense.modele.jeu;

/**
 * Carte du jeu contenant les zones (ferme, marché).
 * Définit les dimensions de la carte et la position des zones.
 */
public class Carte {

    private final int largeur;
    private final int hauteur;

    // Positions des zones sur la carte (x, y, largeur, hauteur)
    private final int[] zoneFerme;
    private final int[] zoneMarche;

    /**
     * Crée une carte avec les dimensions données.
     *
     * @param largeur largeur totale de la carte en pixels
     * @param hauteur hauteur totale de la carte en pixels
     */
    public Carte(int largeur, int hauteur) {
        this.largeur = largeur;
        this.hauteur = hauteur;

        // La ferme occupe la moitié gauche, le marché la moitié droite
        this.zoneFerme = new int[]{0, 0, largeur / 2, hauteur};
        this.zoneMarche = new int[]{largeur / 2, 0, largeur / 2, hauteur};
    }

    /**
     * Détermine dans quelle zone se trouve un point (x, y).
     *
     * @param x position horizontale
     * @param y position verticale
     * @return la zone correspondante, ou null si hors carte
     */
    public Zone getZoneA(int x, int y) {
        if (x < 0 || x >= largeur || y < 0 || y >= hauteur) {
            return null;
        }
        if (x < largeur / 2) {
            return Zone.FERME;
        }
        return Zone.MARCHE;
    }

    /**
     * Retourne le centre (x, y) d'une zone donnée.
     */
    public int[] getCentreZone(Zone zone) {
        switch (zone) {
            case FERME:
                return new int[]{zoneFerme[0] + zoneFerme[2] / 2, zoneFerme[1] + zoneFerme[3] / 2};
            case MARCHE:
                return new int[]{zoneMarche[0] + zoneMarche[2] / 2, zoneMarche[1] + zoneMarche[3] / 2};
            default:
                return new int[]{largeur / 2, hauteur / 2};
        }
    }

    public int getLargeur() {
        return largeur;
    }

    public int getHauteur() {
        return hauteur;
    }

    public int[] getZoneFerme() {
        return zoneFerme;
    }

    public int[] getZoneMarche() {
        return zoneMarche;
    }
}
