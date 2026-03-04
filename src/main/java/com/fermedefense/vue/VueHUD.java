package com.fermedefense.vue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import com.fermedefense.modele.ferme.Ferme;
import com.fermedefense.modele.joueur.Joueur;
import com.fermedefense.utilitaire.Constantes;

/**
 * Affichage tête haute : PV, monnaie, nombre de vaches.
 * Barre horizontale en haut de la fenêtre.
 */
public class VueHUD extends JPanel {

    private final Joueur joueur;
    private final Ferme ferme;

    public VueHUD(Joueur joueur, Ferme ferme) {
        this.joueur = joueur;
        this.ferme = ferme;
        setPreferredSize(new Dimension(Constantes.LARGEUR_FENETRE, 50));
        setBackground(new Color(30, 30, 30));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int y = 32;

        // --- PV (barre + texte) ---
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("PV", 15, y);

        int barX = 45, barW = 150, barH = 16;
        double ratio = (double) joueur.getPointsDeVie() / joueur.getPointsDeVieMax();
        g2.setColor(Color.DARK_GRAY);
        g2.fillRoundRect(barX, y - 13, barW, barH, 6, 6);
        g2.setColor(ratio > 0.5 ? new Color(50, 200, 50) : ratio > 0.25 ? Color.ORANGE : Color.RED);
        g2.fillRoundRect(barX, y - 13, (int) (barW * ratio), barH, 6, 6);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString(joueur.getPointsDeVie() + "/" + joueur.getPointsDeVieMax(), barX + barW + 8, y);

        // --- Monnaie ---
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(new Color(255, 215, 0)); // gold
        g2.drawString("\u2B50 " + joueur.getMonnaie(), 310, y);

        // --- Vaches ---
        g2.setColor(Color.WHITE);
        g2.drawString("\uD83D\uDC04 " + ferme.getNombreAnimaux() + "/" + ferme.getCapaciteMax()
                + "  (prod: " + ferme.getNombreProductives() + ")", 450, y);
    }
}
