package com.fermedefense.vue;

import java.awt.*;

import com.fermedefense.modele.jeu.GestionnaireSucces;
import com.fermedefense.modele.jeu.Succes;

/**
 * Affichage des succès dans la barre latérale.
 *
 * Dessine une rangée d'icônes 18×18 px :
 *   • gris foncé + initiale grisée si non débloqué
 *   • couleur vive + initiale blanche si débloqué
 *
 * Affiche également une notification en haut du viewport quand un
 * nouveau succès est débloqué.
 */
public class VueSucces {

    private static final Color[] COULEURS = {
        new Color(220,  60,  60),   // PREMIER_SANG    — rouge
        new Color(255, 200,  50),   // FERMIER_PROSPERE — or
        new Color(255,  80, 200),   // EXTERMINATEUR   — magenta
        new Color( 80, 200, 255),   // INDESTRUCTIBLE  — cyan
        new Color( 80, 220,  80),   // RANCHER         — vert
    };

    private String notifMessage = null;
    private long   notifExpire  = 0;

    /** Appelé quand un succès vient d'être débloqué. */
    public void notifier(Succes s) {
        notifMessage = "Succès débloqué : " + s.nom + " !";
        notifExpire  = System.currentTimeMillis() + 3500;
    }

    /**
     * Dessine les icônes de succès dans la sidebar.
     *
     * @param g2         contexte graphique
     * @param gestionnaire état des succès
     * @param x          coin gauche de la sidebar
     * @param y          ordonnée (base-line du label "SUCCÈS")
     * @param vpW        largeur du viewport (pour centrer la notification)
     */
    public void dessiner(Graphics2D g2,
                         GestionnaireSucces gestionnaire,
                         int x, int y, int vpW) {
        Succes[] all = Succes.values();

        // Libellé
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(new Color(190, 190, 200));
        g2.drawString("SUCCÈS", x, y);

        int iconSize = 18, gap = 3;
        int iconY = y + 5;

        for (int i = 0; i < all.length; i++) {
            Succes s  = all[i];
            boolean ok = gestionnaire.isDebloque(s);
            int ix = x + i * (iconSize + gap);

            // Fond
            g2.setColor(ok ? COULEURS[i] : new Color(50, 52, 62));
            g2.fillRoundRect(ix, iconY, iconSize, iconSize, 4, 4);

            // Bordure
            g2.setColor(ok ? COULEURS[i].brighter() : new Color(90, 90, 100));
            g2.drawRoundRect(ix, iconY, iconSize, iconSize, 4, 4);

            // Initiale du nom du succès
            g2.setFont(new Font("SansSerif", Font.BOLD, 8));
            g2.setColor(ok ? Color.WHITE : new Color(100, 100, 110));
            String ini = s.nom.substring(0, 1);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(ini,
                    ix + (iconSize - fm.stringWidth(ini)) / 2,
                    iconY + iconSize - 4);
        }

        // ── Notification overlay ──────────────────────────────────────────────
        long now = System.currentTimeMillis();
        if (notifMessage != null && now < notifExpire) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            int nw = fm.stringWidth(notifMessage) + 24;
            int nx = (vpW - nw) / 2;
            int ny = 70;

            g2.setColor(new Color(25, 28, 55, 210));
            g2.fillRoundRect(nx, ny - fm.getAscent() - 2, nw, fm.getHeight() + 8, 8, 8);
            g2.setColor(new Color(200, 170, 50));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(nx, ny - fm.getAscent() - 2, nw, fm.getHeight() + 8, 8, 8);
            g2.setStroke(new BasicStroke(1f));

            g2.setColor(new Color(255, 220, 80));
            g2.drawString(notifMessage, nx + 12, ny + fm.getDescent());
        }
    }
}
