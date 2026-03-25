package com.fermedefense.vue;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

import com.fermedefense.modele.combat.AlienVisuel;

/**
 * Dessine les aliens visuels (vagues et boss) sur la carte.
 */
public class VueAliens {

    private static final Color ALIEN_BODY = new Color(80, 220, 80);
    private static final Color ALIEN_EYE = new Color(20, 20, 20);
    private static final Color ALIEN_HURT = new Color(255, 80, 80, 180);
    private static final Color COW_ICON = new Color(250, 250, 250);

    /**
     * Dessine une liste d'aliens sur le panneau.
     *
     * @param g2    contexte graphique
     * @param aliens liste d'aliens visuels (peut être vide)
     * @param boss   true si c'est le boss (dessin plus grand)
     */
    public void dessiner(Graphics2D g2, List<AlienVisuel> aliens, boolean boss) {
        if (aliens == null || aliens.isEmpty()) return;
        for (AlienVisuel av : aliens) {
            dessinerAlien(g2, av, boss);
        }
    }

    private void dessinerAlien(Graphics2D g2, AlienVisuel av, boolean boss) {
        int size = boss ? 40 : 24;
        int ax = (int) (av.getX() + av.getOffsetCombat());
        int ay = (int) av.getY();

        // Body (oval)
        if (av.getType() == com.fermedefense.modele.combat.Extraterrestre.TypeAlien.RUNNER) {
            g2.setColor(new Color(255, 200, 50));
            size = (int)(size * 0.8);
        } else if (av.getType() == com.fermedefense.modele.combat.Extraterrestre.TypeAlien.TANK) {
            g2.setColor(new Color(80, 80, 255));
            size = (int)(size * 1.3);
        } else {
            g2.setColor(ALIEN_BODY);
        }
        g2.fillOval(ax, ay, size, (int) (size * 1.2));

        // Eyes
        int eyeSize = boss ? 6 : 4;
        int eyeY = ay + size / 4;
        g2.setColor(ALIEN_EYE);
        g2.fillOval(ax + size / 4 - eyeSize / 2, eyeY, eyeSize, eyeSize);
        g2.fillOval(ax + 3 * size / 4 - eyeSize / 2, eyeY, eyeSize, eyeSize);

        // Flash red during combat hits
        if (av.getEtat() == AlienVisuel.EtatVisuel.COMBAT && av.getTempsEtat() % 600 < 100) {
            g2.setColor(ALIEN_HURT);
            g2.fillOval(ax, ay, size, (int) (size * 1.2));
        }

        // Cow icon when abducting
        if (av.getEtat() == AlienVisuel.EtatVisuel.ENLEVEMENT) {
            g2.setColor(COW_ICON);
            g2.setFont(new Font("SansSerif", Font.BOLD, boss ? 16 : 11));
            g2.drawString("\uD83D\uDC04", ax + size / 2 - 4, ay + size + 14);
        }
    }
}
