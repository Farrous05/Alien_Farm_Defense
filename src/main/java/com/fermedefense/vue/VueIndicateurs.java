package com.fermedefense.vue;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import com.fermedefense.modele.ferme.Vache;

/**
 * Dessine des flèches directionnelles pointant vers les vaches et les vendeurs
 * hors champ, et un marqueur flottant au-dessus des cibles visibles.
 *
 * Comportement :
 *  - Cible hors viewport → flèche sur le bord de l'écran (comme un radar)
 *  - Cible visible → petite flèche pulsante au-dessus de la cible
 *
 * Couleurs :
 *  - Vaches : vert (bébé), cyan (adulte), doré (productive)
 *  - Vendeurs : doré
 */
public class VueIndicateurs {

    private static final int MARGIN    = 28;   // distance bord écran → flèche
    private static final int ARR_SIZE  = 14;   // demi-taille de la flèche bord
    private static final int LABEL_OFF = 18;   // décalage label sous la flèche bord

    // ── Point d'entrée ───────────────────────────────────────────────────────

    /**
     * Dessine tous les indicateurs.
     *
     * @param g2      contexte graphique (espace écran)
     * @param vpW     largeur viewport (zone de jeu, sans sidebar)
     * @param vpH     hauteur viewport
     * @param camera  caméra courante
     * @param vaches  liste des vaches
     * @param vendeurs liste des vendeurs marché
     */
    public void dessiner(Graphics2D g2, int vpW, int vpH, Camera camera,
                         List<Vache> vaches, List<VendeurMarche> vendeurs) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        long now = System.currentTimeMillis();

        // ── Flèches vers les vaches ──────────────────────────────────────────
        for (Vache v : vaches) {
            Color col = switch (v.getEtat()) {
                case BEBE       -> new Color(130, 220, 100);
                case ADULTE     -> new Color(100, 200, 255);
                case PRODUCTIVE -> new Color(255, 215, 0);
            };
            String label = switch (v.getEtat()) {
                case BEBE       -> "Bébé";
                case ADULTE     -> "Adulte";
                case PRODUCTIVE -> "Productive";
            };
            // Centre de la vache à l'écran
            int sx = camera.toScreenX(v.getX() + VueFerme.COW_SIZE / 2.0);
            int sy = camera.toScreenY(v.getY() + VueFerme.COW_SIZE / 2.0);
            dessinerIndicateur(g2, sx, sy, vpW, vpH, col, label, now);
        }

        // ── Flèches vers les vendeurs ────────────────────────────────────────
        Color vendCol = new Color(255, 200, 50);
        for (VendeurMarche v : vendeurs) {
            int sx = camera.toScreenX(v.getWorldX() + 24);
            int sy = camera.toScreenY(v.getWorldY() + 24);
            String label = shortLabel(v);
            dessinerIndicateur(g2, sx, sy, vpW, vpH, vendCol, label, now);
        }
    }

    // ── Logique d'un indicateur ──────────────────────────────────────────────

    private void dessinerIndicateur(Graphics2D g2, int sx, int sy,
                                    int vpW, int vpH,
                                    Color col, String label, long now) {
        boolean visible = sx > -20 && sx < vpW + 20 && sy > -20 && sy < vpH + 20;
        if (visible) {
            dessinerMarqueurFlottant(g2, sx, sy, col, now);
        } else {
            dessinerFlecheBord(g2, sx, sy, vpW, vpH, col, label, now);
        }
    }

    /**
     * Petite flèche pulsante au-dessus de la cible (quand elle est à l'écran).
     */
    private void dessinerMarqueurFlottant(Graphics2D g2, int sx, int sy,
                                          Color col, long now) {
        float alpha = 0.55f + 0.35f * (float) Math.sin(now * 0.005);
        int bob = (int)(4 * Math.sin(now * 0.004));
        int ax = sx;
        int ay = sy - 28 - bob;

        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        drawArrowDown(g2, ax, ay, 8, col);
        g2.setComposite(old);
    }

    /**
     * Grande flèche sur le bord de l'écran pointant vers la cible hors-champ.
     */
    private void dessinerFlecheBord(Graphics2D g2, int sx, int sy,
                                    int vpW, int vpH,
                                    Color col, String label, long now) {
        // Centre viewport
        int cx = vpW / 2, cy = vpH / 2;
        double dx = sx - cx, dy = sy - cy;
        double angle = Math.atan2(dy, dx);

        // Clamp sur le bord
        double cos = Math.cos(angle), sin = Math.sin(angle);
        int ex, ey;
        double tx = cos == 0 ? Double.MAX_VALUE : ((cos > 0 ? vpW - MARGIN : MARGIN) - cx) / cos;
        double ty = sin == 0 ? Double.MAX_VALUE : ((sin > 0 ? vpH - MARGIN : MARGIN) - cy) / sin;
        if (Math.abs(tx) < Math.abs(ty)) {
            ex = (int)(cx + tx * cos);
            ey = (int)(cy + tx * sin);
        } else {
            ex = (int)(cx + ty * cos);
            ey = (int)(cy + ty * sin);
        }
        ex = Math.max(MARGIN, Math.min(vpW - MARGIN, ex));
        ey = Math.max(MARGIN, Math.min(vpH - MARGIN, ey));

        // Pulse
        float alpha = 0.7f + 0.25f * (float) Math.sin(now * 0.006);
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // Halo de fond
        g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 60));
        g2.fillOval(ex - ARR_SIZE - 4, ey - ARR_SIZE - 4,
                (ARR_SIZE + 4) * 2, (ARR_SIZE + 4) * 2);

        // Flèche orientée
        drawRotatedArrow(g2, ex, ey, ARR_SIZE, angle, col);

        // Label
        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
        FontMetrics fm = g2.getFontMetrics();
        int lw = fm.stringWidth(label);
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(ex - lw / 2 - 2, ey + LABEL_OFF, lw + 4, 12, 3, 3);
        g2.setColor(col);
        g2.drawString(label, ex - lw / 2, ey + LABEL_OFF + 9);

        g2.setComposite(old);
    }

    // ── Primitives de dessin ─────────────────────────────────────────────────

    /** Flèche vers le bas (marqueur flottant sur-cible). */
    private void drawArrowDown(Graphics2D g2, int x, int y, int s, Color col) {
        int[] px = {x - s, x + s, x};
        int[] py = {y, y, y + s * 2};
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillPolygon(px, py, 3);
        g2.setColor(col);
        for (int i = 0; i < 3; i++) { px[i]--; py[i]--; }
        g2.fillPolygon(px, py, 3);
        g2.setStroke(new BasicStroke(1f));
        g2.drawPolygon(px, py, 3);
        g2.setStroke(new BasicStroke(1f));
    }

    /** Flèche orientée selon `angle` (bord de l'écran). */
    private void drawRotatedArrow(Graphics2D g2, int cx, int cy, int s,
                                   double angle, Color col) {
        // Pointe dans la direction angle, base opposée
        double tipX = cx + Math.cos(angle) * s;
        double tipY = cy + Math.sin(angle) * s;
        double perpAngle = angle + Math.PI / 2;
        double baseX = cx - Math.cos(angle) * (s / 2.0);
        double baseY = cy - Math.sin(angle) * (s / 2.0);
        int half = s / 2;
        int[] px = {
            (int) tipX,
            (int)(baseX + Math.cos(perpAngle) * half),
            (int)(baseX - Math.cos(perpAngle) * half)
        };
        int[] py = {
            (int) tipY,
            (int)(baseY + Math.sin(perpAngle) * half),
            (int)(baseY - Math.sin(perpAngle) * half)
        };
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillPolygon(new int[]{px[0]+1,px[1]+1,px[2]+1},
                        new int[]{py[0]+1,py[1]+1,py[2]+1}, 3);
        g2.setColor(col);
        g2.fillPolygon(px, py, 3);
        g2.setColor(col.brighter());
        g2.setStroke(new BasicStroke(1f));
        g2.drawPolygon(px, py, 3);
        g2.setStroke(new BasicStroke(1f));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String shortLabel(VendeurMarche v) {
        String n = v.getNom();
        if (n.contains("Arme") || n.contains("Forge")) return "Armes";
        if (n.contains("Vache") || n.contains("Élevage")) return "Vaches";
        if (n.contains("Potion") || n.contains("Apothicaire")) return "Potions";
        if (n.contains("Bombe") || n.contains("Armurerie")) return "Bombes";
        return n.length() > 8 ? n.substring(0, 8) : n;
    }
}
