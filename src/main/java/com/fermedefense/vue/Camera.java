package com.fermedefense.vue;

import com.fermedefense.modele.joueur.Joueur;

/**
 * Caméra : suit le joueur et calcule l'offset viewport/monde.
 *
 * Le coin supérieur gauche du viewport (en coordonnées monde) est stocké
 * dans (x, y).  Pour convertir une coordonnée monde en coordonnée écran :
 *   screenX = worldX - camera.getX()
 *   screenY = worldY - camera.getY()
 */
public class Camera {

    private int x;
    private int y;

    private final int vpW;    // largeur du viewport en px
    private final int vpH;    // hauteur du viewport en px
    private final int worldW; // largeur du monde en px
    private final int worldH; // hauteur du monde en px

    public Camera(int vpW, int vpH, int worldW, int worldH) {
        this.vpW    = vpW;
        this.vpH    = vpH;
        this.worldW = worldW;
        this.worldH = worldH;
    }

    /**
     * Centre la caméra sur le joueur, en restant dans les limites du monde.
     */
    public void centrerSur(Joueur joueur) {
        int cx = (int)(joueur.getX() + joueur.getTaille() / 2.0);
        int cy = (int)(joueur.getY() + joueur.getTaille() / 2.0);
        x = cx - vpW / 2;
        y = cy - vpH / 2;
        x = Math.max(0, Math.min(worldW - vpW, x));
        y = Math.max(0, Math.min(worldH - vpH, y));
    }

    /** Convertit une coordonnée monde X → coordonnée écran X. */
    public int toScreenX(double worldX) { return (int)(worldX - x); }

    /** Convertit une coordonnée monde Y → coordonnée écran Y. */
    public int toScreenY(double worldY) { return (int)(worldY - y); }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getVpW() { return vpW; }
    public int getVpH() { return vpH; }
}
