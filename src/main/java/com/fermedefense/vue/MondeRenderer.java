package com.fermedefense.vue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import com.fermedefense.utilitaire.Constantes;

/**
 * Dessine le monde en vue de dessus via le TileManager.
 * La caméra est prise en compte pour le décalage des tuiles.
 *
 * Éditez les fichiers de carte pour modifier le décor :
 *   src/main/resources/maps/farm_map.txt
 *   src/main/resources/maps/market_map.txt
 */
public class MondeRenderer {

    /** Taille de rendu d'une tuile : 16 px × 2.5 = 40 px. */
    private static final int TILE_SIZE = 40;

    /** Abscisse de séparation ferme/marché dans le monde. */
    private static final int SPLIT_X = Constantes.LARGEUR_CARTE / 2; // 1200

    private final TileManager tileManager;
    private final int[][] farmMap;
    private final int[][] marketMap;

    public MondeRenderer() {
        this.tileManager = new TileManager();
        this.farmMap   = tileManager.loadMap("/maps/farm_map.txt");
        this.marketMap = tileManager.loadMap("/maps/market_map.txt");
    }

    // ── Point d'entrée ───────────────────────────────────────────────────────

    /**
     * Dessine le monde entier avec la caméra courante.
     *
     * @param g2     contexte graphique
     * @param camera caméra courante
     */
    public void draw(Graphics2D g2, Camera camera) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int vpW = camera.getVpW();
        int vpH = camera.getVpH();

        // Fond de secours
        g2.setColor(new Color(60, 110, 55));
        g2.fillRect(0, 0, vpW, vpH);

        // Ferme : origine monde (0,0) → offset écran = (0 - cam.x, 0 - cam.y)
        tileManager.draw(g2, farmMap,
                -camera.getX(), -camera.getY(),
                TILE_SIZE, vpW, vpH);

        // Marché : origine monde (SPLIT_X, 0) → offset écran = (SPLIT_X - cam.x, -cam.y)
        tileManager.draw(g2, marketMap,
                SPLIT_X - camera.getX(), -camera.getY(),
                TILE_SIZE, vpW, vpH);

        // Séparateur animé (si visible)
        int divX = SPLIT_X - camera.getX();
        if (divX >= 0 && divX < vpW) {
            drawDivider(g2, divX, vpH);
        }
    }

    // ── Rétrocompatibilité (ancienne signature sans caméra) ──────────────────

    /** @deprecated Utiliser {@link #draw(Graphics2D, Camera)} avec caméra. */
    @Deprecated
    public void draw(Graphics2D g2, int totalW, int totalH, int splitX) {
        // fallback sans défilement
        int vpW = totalW;
        int vpH = totalH;
        tileManager.draw(g2, farmMap, 0, 0, TILE_SIZE, vpW, vpH);
        tileManager.draw(g2, marketMap, splitX, 0, TILE_SIZE, vpW, vpH);
        drawDivider(g2, splitX, vpH);
    }

    // ── Séparateur ───────────────────────────────────────────────────────────

    private void drawDivider(Graphics2D g2, int x, int h) {
        g2.setColor(new Color(42, 38, 52));
        g2.fillRect(x - 2, 0, 4, h);
        for (int py = 30; py < h; py += 65) {
            float glow = 0.45f + 0.4f * (float) Math.sin(
                    System.currentTimeMillis() * 0.003 + py * 0.1);
            g2.setColor(new Color(80, 140, 255, (int) (175 * glow)));
            g2.fillOval(x - 3, py - 3, 6, 6);
        }
    }
}
