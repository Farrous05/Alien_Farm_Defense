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

    /**
     * Vérifie si le joueur est considéré dans la ferme avec une tolérance
     * autour de la frontière verticale ferme/marché.
     */
    public boolean estJoueurDansFerme(com.fermedefense.modele.joueur.Joueur joueur, int margePx) {
        if (joueur == null) return false;
        int xRepere = (int) joueur.getX() + joueur.getTaille() / 2;
        int yRepere = (int) joueur.getY() + joueur.getTaille() - 2;
        return estPositionDansFermeToleree(xRepere, yRepere, margePx);
    }

    /**
     * Variante positionnelle de la vérification de zone ferme avec tolérance.
     */
    public boolean estPositionDansFermeToleree(int x, int y, int margePx) {
        if (x < 0 || x >= largeur || y < 0 || y >= hauteur) {
            return false;
        }
        int split = largeur / 2;
        int marge = Math.max(0, margePx);
        return x <= split + marge;
    }

    /**
     * Contraint la position d'un joueur dans les limites de la carte.
     *
     * @param joueur le joueur à contraindre
     */
    public void clampJoueur(com.fermedefense.modele.joueur.Joueur joueur) {
        double x = Math.max(0, Math.min(largeur - joueur.getTaille(), joueur.getX()));
        double y = Math.max(0, Math.min(hauteur - joueur.getTaille(), joueur.getY()));
        joueur.setX(x);
        joueur.setY(y);
    }
}
