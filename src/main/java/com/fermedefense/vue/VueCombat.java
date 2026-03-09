package com.fermedefense.vue;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import com.fermedefense.modele.combat.Attaque;
import com.fermedefense.modele.combat.Extraterrestre;
import com.fermedefense.modele.combat.ResultatCombat;
import com.fermedefense.modele.joueur.Joueur;

/**
 * Vue du combat (vagues intermédiaires et combat final).
 *
 * Dessine un overlay semi-transparent au centre de l'écran avec :
 *   - Le nom et la barre de PV de l'alien courant
 *   - La barre de PV du joueur
 *   - Une animation simple d'épée (flash)
 *   - Le résultat si le combat est terminé
 */
public class VueCombat {

    private static final int OVERLAY_W = 400;
    private static final int OVERLAY_H = 200;
    private static final Color BG_OVERLAY = new Color(0, 0, 0, 180);
    private static final Color COULEUR_ALIEN = new Color(120, 255, 80);
    private static final Color COULEUR_JOUEUR = new Color(80, 180, 255);

    /**
     * Dessine le combat en overlay sur le panneau de jeu.
     *
     * @param g2       contexte graphique
     * @param attaque  le combat en cours
     * @param joueur   le joueur
     * @param panelW   largeur du panneau
     * @param panelH   hauteur du panneau
     * @param isBoss   true si c'est le combat final
     */
    public void dessiner(Graphics2D g2, Attaque attaque, Joueur joueur,
                         int panelW, int panelH, boolean isBoss) {
        if (attaque == null) return;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int ox = (panelW - OVERLAY_W) / 2;
        int oy = (panelH - OVERLAY_H) / 2;

        // Fond semi-transparent
        g2.setColor(BG_OVERLAY);
        g2.fillRoundRect(ox, oy, OVERLAY_W, OVERLAY_H, 16, 16);
        g2.setColor(isBoss ? new Color(255, 60, 60) : new Color(100, 255, 100));
        g2.drawRoundRect(ox, oy, OVERLAY_W, OVERLAY_H, 16, 16);

        // Titre
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        String titre = isBoss ? "COMBAT FINAL" : "ATTAQUE ALIEN !";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(titre, ox + (OVERLAY_W - fm.stringWidth(titre)) / 2, oy + 28);

        Extraterrestre alien = attaque.getAlienCourant();

        if (attaque.isTerminee()) {
            // Résultat
            dessinerResultat(g2, attaque.getResultat(), ox, oy);
            return;
        }

        if (alien == null) return;

        int baseY = oy + 50;
        int barW = 250;
        int barH = 20;
        int barX = ox + (OVERLAY_W - barW) / 2;

        // --- Alien ---
        g2.setColor(COULEUR_ALIEN);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString(alien.getNom(), barX, baseY);
        // Alien icon
        g2.setColor(new Color(100, 220, 60));
        g2.fillOval(ox + 40, baseY + 5, 30, 30);
        g2.setColor(Color.BLACK);
        g2.fillOval(ox + 47, baseY + 12, 5, 5); // eye
        g2.fillOval(ox + 58, baseY + 12, 5, 5); // eye

        // Alien HP bar
        dessinerBarrePV(g2, barX, baseY + 15, barW, barH,
                alien.getPointsDeVie(), alien.getPointsDeVieMax(), COULEUR_ALIEN);

        // Sword flash (simple animation)
        long t = System.currentTimeMillis() % 1000;
        if (t < 200) {
            g2.setColor(new Color(255, 255, 200, 180));
            g2.setFont(new Font("SansSerif", Font.BOLD, 28));
            g2.drawString("\u2694", ox + OVERLAY_W / 2 - 10, baseY + 65); // sword emoji
        }

        // --- Joueur ---
        int joueurY = baseY + 80;
        g2.setColor(COULEUR_JOUEUR);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString("Fermier", barX, joueurY);
        // Player icon
        g2.setColor(new Color(220, 120, 50));
        g2.fillRoundRect(ox + 40, joueurY + 5, 25, 25, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.drawString("J", ox + 49, joueurY + 22);

        // Player HP bar
        dessinerBarrePV(g2, barX, joueurY + 15, barW, barH,
                joueur.getPointsDeVie(), joueur.getPointsDeVieMax(), COULEUR_JOUEUR);

        // Aliens remaining
        g2.setColor(new Color(180, 180, 180));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString("Aliens restants : " + attaque.getNombreAliensRestants(),
                ox + 15, oy + OVERLAY_H - 12);
    }

    private void dessinerBarrePV(Graphics2D g2, int x, int y, int w, int h,
                                 int pv, int pvMax, Color couleur) {
        double ratio = (double) pv / pvMax;

        // Fond
        g2.setColor(new Color(50, 50, 50));
        g2.fillRoundRect(x, y, w, h, 6, 6);

        // Remplissage
        g2.setColor(ratio > 0.5 ? couleur : ratio > 0.25 ? Color.ORANGE : Color.RED);
        g2.fillRoundRect(x, y, (int) (w * ratio), h, 6, 6);

        // Cadre
        g2.setColor(new Color(100, 100, 100));
        g2.drawRoundRect(x, y, w, h, 6, 6);

        // Texte
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString(pv + "/" + pvMax, x + w / 2 - 20, y + 14);
    }

    private void dessinerResultat(Graphics2D g2, ResultatCombat resultat, int ox, int oy) {
        String texte;
        Color couleur;
        if (resultat == ResultatCombat.VICTOIRE) {
            texte = "VICTOIRE !";
            couleur = new Color(80, 255, 80);
        } else {
            texte = "DÉFAITE...";
            couleur = new Color(255, 80, 80);
        }

        g2.setColor(couleur);
        g2.setFont(new Font("SansSerif", Font.BOLD, 28));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(texte, ox + (OVERLAY_W - fm.stringWidth(texte)) / 2, oy + OVERLAY_H / 2 + 10);
    }
}
