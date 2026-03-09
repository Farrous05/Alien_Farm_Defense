package com.fermedefense.vue;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import com.fermedefense.modele.joueur.ActionDuree;

/**
 * Dessine une barre de progression au-dessus du joueur
 * pendant qu'une action à durée est en cours (récolte, achat).
 */
public class VueActionJoueur {

    private static final int BAR_W = 60;
    private static final int BAR_H = 8;
    private static final int OFFSET_Y = 12;

    /**
     * Dessine la barre d'action au-dessus du joueur.
     *
     * @param g2     contexte graphique
     * @param action l'action en cours (peut être null ou terminée → ne dessine rien)
     * @param jx     position X du joueur
     * @param jy     position Y du joueur
     * @param jt     taille du sprite joueur
     */
    public void dessiner(Graphics2D g2, ActionDuree action, int jx, int jy, int jt) {
        if (action == null || action.isTerminee()) return;

        int barX = jx + (jt - BAR_W) / 2;
        int barY = jy - OFFSET_Y - BAR_H;

        // Fond
        g2.setColor(new Color(40, 40, 40, 200));
        g2.fillRoundRect(barX, barY, BAR_W, BAR_H, 4, 4);

        // Remplissage
        double prog = action.getProgression();
        g2.setColor(new Color(100, 220, 255));
        g2.fillRoundRect(barX, barY, (int) (BAR_W * prog), BAR_H, 4, 4);

        // Cadre
        g2.setColor(new Color(150, 150, 150));
        g2.drawRoundRect(barX, barY, BAR_W, BAR_H, 4, 4);

        // Label
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        FontMetrics fm = g2.getFontMetrics();
        String label = action.getLabel();
        g2.drawString(label, barX + (BAR_W - fm.stringWidth(label)) / 2, barY - 2);
    }
}
