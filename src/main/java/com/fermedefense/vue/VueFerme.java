package com.fermedefense.vue;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

import com.fermedefense.modele.ferme.EtatVache;
import com.fermedefense.modele.ferme.Ferme;
import com.fermedefense.modele.ferme.Vache;

import java.awt.Image;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Dessine les vaches de la ferme à leurs positions monde, traduites via caméra.
 */
public class VueFerme {

    private static Image imgBebe;
    private static Image imgAdulte;
    private static Image imgProductive;

    static {
        try {
            imgBebe       = ImageIO.read(VueFerme.class.getResource("/images/vache_bebe.png"));
            imgAdulte     = ImageIO.read(VueFerme.class.getResource("/images/vache_adulte.png"));
            imgProductive = ImageIO.read(VueFerme.class.getResource("/images/vache_productive.png"));
        } catch (Exception e) {
            System.err.println("Erreur chargement images vaches : " + e.getMessage());
        }
    }

    private final Ferme ferme;

    public VueFerme(Ferme ferme) {
        this.ferme = ferme;
    }

    /**
     * Dessine toutes les vaches à leurs coordonnées monde, projetées à l'écran
     * via la caméra.
     */
    public void dessiner(Graphics2D g2, Camera camera) {
        List<Vache> vaches = ferme.getVaches();
        for (Vache v : vaches) {
            int sx = camera.toScreenX(v.getX());
            int sy = camera.toScreenY(v.getY());
            dessinerVache(g2, v, sx, sy);
        }
    }

    /** Rétrocompatibilité sans caméra (affichage en grille fixe, legacy). */
    public void dessiner(Graphics2D g2, int zx, int zy, int zw, int zh) {
        List<Vache> vaches = ferme.getVaches();
        int cols  = 3;
        int cellW = zw / cols;
        int startY = zy + 40;
        for (int i = 0; i < vaches.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            int cx  = zx + col * cellW + 10;
            int cy  = startY + row * 80;
            dessinerVache(g2, vaches.get(i), cx, cy);
        }
    }

    // ── Rendu d'une vache ────────────────────────────────────────────────────

    public static final int COW_SIZE = 96;

    private void dessinerVache(Graphics2D g2, Vache v, int x, int y) {
        EtatVache etat = v.getEtat();

        Image img = switch (etat) {
            case BEBE       -> imgBebe;
            case ADULTE     -> imgAdulte;
            case PRODUCTIVE -> imgProductive;
        };

        if (img != null) {
            g2.drawImage(img, x, y, COW_SIZE, COW_SIZE, null);
        } else {
            Color c = switch (etat) {
                case BEBE       -> new Color(210, 175, 120);
                case ADULTE     -> new Color(230, 230, 230);
                case PRODUCTIVE -> new Color(245, 210, 80);
            };
            g2.setColor(c);
            g2.fillRoundRect(x, y, 48, 38, 8, 8);
            g2.setColor(Color.BLACK);
            g2.drawRoundRect(x, y, 48, 38, 8, 8);
        }

        // Nom de l'état au-dessus de la vache
        String etatLabel = switch (etat) {
            case BEBE       -> "Bébé";
            case ADULTE     -> "Adulte";
            case PRODUCTIVE -> "Productive";
        };
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        java.awt.FontMetrics fm = g2.getFontMetrics();
        int lw = fm.stringWidth(etatLabel);
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(x + (COW_SIZE - lw) / 2 - 3, y - 16, lw + 6, 14, 4, 4);
        Color labelColor = switch (etat) {
            case BEBE       -> new Color(210, 175, 120);
            case ADULTE     -> new Color(210, 210, 230);
            case PRODUCTIVE -> new Color(255, 215, 0);
        };
        g2.setColor(labelColor);
        g2.drawString(etatLabel, x + (COW_SIZE - lw) / 2, y - 4);

        // Barre de progression sous la vache
        int barW = COW_SIZE;
        double prog = Math.min(1.0, v.getProgression());
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRect(x, y + COW_SIZE + 2, barW, 5);
        Color barColor = etat == EtatVache.PRODUCTIVE
                ? new Color(255, 215, 0) : new Color(80, 200, 80);
        g2.setColor(barColor);
        g2.fillRect(x, y + COW_SIZE + 2, (int)(barW * prog), 5);

        // Badge monnaie accumulée
        if (etat == EtatVache.PRODUCTIVE && v.getMonnaieAccumulee() > 0) {
            String badge = "+" + v.getMonnaieAccumulee() + "g";
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.setColor(new Color(255, 220, 0));
            g2.drawString(badge, x + COW_SIZE + 2, y + 14);
        }
    }
}
