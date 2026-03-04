package com.fermedefense.vue;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

import com.fermedefense.modele.ferme.EtatVache;
import com.fermedefense.modele.ferme.Ferme;
import com.fermedefense.modele.ferme.Vache;

/**
 * Dessine le contenu de la zone Ferme :
 * fond vert, vaches avec barre de croissance/production.
 */
public class VueFerme {

    private final Ferme ferme;

    public VueFerme(Ferme ferme) {
        this.ferme = ferme;
    }

    /**
     * Dessine la ferme dans la zone délimitée par (zx, zy, zw, zh).
     */
    public void dessiner(Graphics2D g2, int zx, int zy, int zw, int zh) {
        // Fond
        g2.setColor(new Color(60, 140, 60));
        g2.fillRect(zx, zy, zw, zh);

        // Bordure
        g2.setColor(new Color(40, 100, 40));
        g2.drawRect(zx, zy, zw - 1, zh - 1);

        // Label
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString("FERME", zx + 10, zy + 24);

        // Vaches
        List<Vache> vaches = ferme.getVaches();
        int cols = 3;
        int cellW = zw / cols;
        int cellH = 80;
        int startY = zy + 40;

        for (int i = 0; i < vaches.size(); i++) {
            Vache v = vaches.get(i);
            int col = i % cols;
            int row = i / cols;
            int cx = zx + col * cellW + 10;
            int cy = startY + row * cellH;

            dessinerVache(g2, v, cx, cy, cellW - 20);
        }
    }

    private void dessinerVache(Graphics2D g2, Vache v, int x, int y, int w) {
        EtatVache etat = v.getEtat();

        // Icône
        Color couleur = switch (etat) {
            case BEBE       -> new Color(180, 220, 180);
            case ADULTE     -> new Color(230, 230, 150);
            case PRODUCTIVE -> new Color(255, 255, 255);
        };
        g2.setColor(couleur);
        g2.fillRoundRect(x, y, 30, 25, 8, 8);
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(x, y, 30, 25, 8, 8);

        // Nom + état
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(Color.WHITE);
        String label = v.getNom() + " (" + etat.name().charAt(0) + ")";
        g2.drawString(label, x + 35, y + 12);

        // Barre de progression
        int barX = x + 35, barY = y + 18, barW = w - 40, barH = 8;
        double prog = v.getProgression();
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(barX, barY, barW, barH);
        Color barColor = etat == EtatVache.PRODUCTIVE ? new Color(255, 215, 0) : new Color(100, 200, 100);
        g2.setColor(barColor);
        g2.fillRect(barX, barY, (int) (barW * Math.min(1.0, prog)), barH);

        // Monnaie accumulée (si productive)
        if (etat == EtatVache.PRODUCTIVE && v.getMonnaieAccumulee() > 0) {
            g2.setColor(new Color(255, 215, 0));
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString("+" + v.getMonnaieAccumulee(), x + 35 + barW + 4, y + 26);
        }
    }
}
