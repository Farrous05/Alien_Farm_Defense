package com.fermedefense.vue;

import java.awt.*;
import java.util.List;

import com.fermedefense.modele.jeu.TableauScores;

/**
 * Rendu du tableau des meilleurs scores.
 * Utilisé dans l'overlay de fin de partie et dans la modale du menu principal.
 */
public class VueTableauScores {

    /**
     * Dessine le tableau centré autour de (cx, cy).
     */
    public void dessiner(Graphics2D g2, TableauScores tableau, int cx, int cy) {
        List<TableauScores.Entree> entrees = tableau.getEntrees();
        int rows = Math.max(1, entrees.size());
        int panW = 360;
        int panH = 54 + rows * 20 + 16;
        int px = cx - panW / 2;
        int py = cy - panH / 2;

        // Fond
        g2.setColor(new Color(10, 12, 30, 235));
        g2.fillRoundRect(px, py, panW, panH, 14, 14);

        // Bordure or
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(200, 170, 50));
        g2.drawRoundRect(px, py, panW, panH, 14, 14);
        g2.setStroke(new BasicStroke(1f));

        // Titre
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.setColor(new Color(255, 210, 50));
        String titre = "MEILLEURS SCORES";
        FontMetrics fmT = g2.getFontMetrics();
        g2.drawString(titre, cx - fmT.stringWidth(titre) / 2, py + 22);

        // En-têtes colonnes
        int hy = py + 40;
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(new Color(160, 160, 180));
        g2.drawString("#",    px + 14, hy);
        g2.drawString("NOM", px + 36, hy);
        g2.drawString("NIV",  px + 120, hy);
        g2.drawString("SCORE", px + 200, hy);

        g2.setColor(new Color(80, 80, 100));
        g2.drawLine(px + 10, hy + 3, px + panW - 10, hy + 3);

        if (entrees.isEmpty()) {
            g2.setFont(new Font("SansSerif", Font.ITALIC, 11));
            g2.setColor(new Color(140, 140, 160));
            String vide = "Aucun score enregistré";
            FontMetrics fmV = g2.getFontMetrics();
            g2.drawString(vide, cx - fmV.stringWidth(vide) / 2, hy + 22);
        } else {
            for (int i = 0; i < entrees.size(); i++) {
                TableauScores.Entree e = entrees.get(i);
                int ly = hy + 18 + i * 20;

                Color c = switch (i) {
                    case 0 -> new Color(255, 215, 0);
                    case 1 -> new Color(200, 200, 200);
                    case 2 -> new Color(205, 127, 50);
                    default -> new Color(170, 180, 200);
                };
                g2.setColor(c);
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.drawString((i + 1) + ".", px + 14, ly);
                g2.drawString(e.initiales, px + 36, ly);

                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g2.drawString(String.valueOf(e.niveau), px + 120, ly);
                g2.drawString(String.valueOf(e.score), px + 200, ly);
            }
        }
    }
}
