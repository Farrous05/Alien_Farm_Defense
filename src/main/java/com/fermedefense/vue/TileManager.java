package com.fermedefense.vue;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Charge les 132 tuiles du pack Kenney Tiny Town (16×16 px, CC0)
 * et dessine une carte définie par un fichier texte de tile IDs.
 *
 * Format map : lignes de nombres séparés par des espaces.
 *   -1  → case vide (rien dessiné)
 *   0-131 → ID de tuile correspondant à tile_XXXX.png
 *   Lignes commençant par # → commentaires ignorés
 */
public class TileManager {

    /** Nombre de tuiles dans le pack Tiny Town. */
    private static final int TILE_COUNT = 132;

    private final BufferedImage[] tiles = new BufferedImage[TILE_COUNT];

    public TileManager() {
        loadTiles();
    }

    // ── Chargement ────────────────────────────────────────────────────────────

    private void loadTiles() {
        for (int i = 0; i < TILE_COUNT; i++) {
            String path = String.format("/images/tiles/tt/tile_%04d.png", i);
            try (InputStream is = TileManager.class.getResourceAsStream(path)) {
                if (is != null) {
                    tiles[i] = ImageIO.read(is);
                }
            } catch (Exception e) {
                // tuile absente → tiles[i] reste null, rien ne sera dessiné
            }
        }
    }

    /**
     * Parse un fichier de carte texte.
     *
     * @param resourcePath chemin absolu dans le classpath, ex: "/maps/farm_map.txt"
     * @return tableau 2D de tile IDs, ou tableau vide en cas d'erreur
     */
    public int[][] loadMap(String resourcePath) {
        List<int[]> rows = new ArrayList<>();
        try (InputStream is = TileManager.class.getResourceAsStream(resourcePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\s+");
                int[] row = new int[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    try {
                        row[i] = Integer.parseInt(parts[i]);
                    } catch (NumberFormatException e) {
                        row[i] = -1;
                    }
                }
                rows.add(row);
            }
        } catch (Exception e) {
            System.err.println("TileManager: impossible de charger " + resourcePath);
        }
        return rows.toArray(new int[0][]);
    }

    // ── Dessin ────────────────────────────────────────────────────────────────

    /**
     * Dessine une carte de tuiles avec support caméra.
     * Seules les tuiles visibles dans [0, vpW) × [0, vpH) sont dessinées.
     *
     * @param g2       contexte graphique
     * @param map      tableau 2D de tile IDs (-1 = case vide)
     * @param offsetX  position écran du coin supérieur gauche de la tuile (0,0) du monde
     *                 (= worldOriginX - camera.getX())
     * @param offsetY  position écran du coin supérieur gauche de la tuile (0,0) du monde
     * @param tileSize taille de rendu d'une tuile en pixels
     * @param vpW      largeur du viewport (px)
     * @param vpH      hauteur du viewport (px)
     */
    public void draw(Graphics2D g2, int[][] map,
                     int offsetX, int offsetY,
                     int tileSize, int vpW, int vpH) {
        if (map == null || map.length == 0) return;

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Calcul de la plage de tuiles visibles (optimisation : on ne dessine que ce qui est à l'écran)
        int firstCol = Math.max(0, (-offsetX) / tileSize);
        int firstRow = Math.max(0, (-offsetY) / tileSize);

        for (int row = firstRow; row < map.length; row++) {
            int py = offsetY + row * tileSize;
            if (py >= vpH) break;
            if (py + tileSize <= 0) continue;

            for (int col = firstCol; col < map[row].length; col++) {
                int px = offsetX + col * tileSize;
                if (px >= vpW) break;
                if (px + tileSize <= 0) continue;

                int id = map[row][col];
                if (id < 0 || id >= TILE_COUNT) continue;
                BufferedImage tile = tiles[id];
                if (tile != null) {
                    g2.drawImage(tile, px, py, tileSize, tileSize, null);
                }
            }
        }
    }
}
