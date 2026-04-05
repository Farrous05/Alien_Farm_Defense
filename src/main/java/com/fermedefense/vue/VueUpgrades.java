package com.fermedefense.vue;

import java.awt.*;

import com.fermedefense.modele.joueur.Joueur;

/**
 * Boutique d'améliorations affichée en overlay entre les niveaux.
 *
 * Contrôles :
 *   ← / →   — naviguer entre les cartes
 *   ENTRÉE  — acheter l'amélioration sélectionnée
 *   ESPACE  — passer (aller directement au niveau suivant)
 */
public class VueUpgrades {

    // ── Définitions des améliorations ────────────────────────────────────────

    public static final int NB = 4;

    private static final String[] NOMS = {
        "Max PV  +25",
        "Dégâts Arme  +10 %",
        "Croissance vache  +20 %",
        "Or de départ  +50 g"
    };

    private static final String[][] DETAILS = {
        {"Augmente vos PV maximum", "et soigne +25 immédiatement"},
        {"Multiplicateur de dégâts", "de toutes les armes +10 %"},
        {"Vos vaches grandissent", "20 % plus vite"},
        {"Recevez 50 g de plus", "au début de chaque niveau"}
    };

    private static final int[] COUTS = { 100, 150, 80, 75 };

    private static final Color[] COULEURS = {
        new Color( 60, 130, 220),   // HP    — bleu
        new Color(210,  70,  50),   // dégâts — rouge
        new Color( 50, 170,  80),   // vache  — vert
        new Color(210, 160,  40),   // or     — or
    };

    // ── État ─────────────────────────────────────────────────────────────────

    private int selectionIndex = 0;

    public void selectionPrecedente() {
        selectionIndex = (selectionIndex + NB - 1) % NB;
    }

    public void selectionSuivante() {
        selectionIndex = (selectionIndex + 1) % NB;
    }

    public int getSelectionIndex() { return selectionIndex; }
    public int getCoutSelectionne() { return COUTS[selectionIndex]; }
    public String getNomSelectionne() { return NOMS[selectionIndex]; }

    // ── Rendu ─────────────────────────────────────────────────────────────────

    public void dessiner(Graphics2D g2, int vpW, int vpH, Joueur joueur) {
        // Fond semi-transparent
        g2.setColor(new Color(0, 0, 0, 190));
        g2.fillRect(0, 0, vpW, vpH);

        // Titre
        g2.setFont(new Font("SansSerif", Font.BOLD, 30));
        g2.setColor(new Color(255, 210, 50));
        String titre = "BOUTIQUE D'AMÉLIORATIONS";
        FontMetrics fmT = g2.getFontMetrics();
        g2.drawString(titre, (vpW - fmT.stringWidth(titre)) / 2, 75);

        // Or disponible
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(new Color(200, 230, 255));
        String orStr = "\u2B50 Or disponible : " + joueur.getMonnaie() + " g";
        FontMetrics fmO = g2.getFontMetrics();
        g2.drawString(orStr, (vpW - fmO.stringWidth(orStr)) / 2, 105);

        // Instructions
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(new Color(160, 170, 190));
        String hint = "[ ← → ]  Choisir     [ ENTRÉE ]  Acheter     [ ESPACE ]  Passer";
        FontMetrics fmH = g2.getFontMetrics();
        g2.drawString(hint, (vpW - fmH.stringWidth(hint)) / 2, 125);

        // Cartes
        int cardW = 220, cardH = 190;
        int gap   = 18;
        int totalW = NB * cardW + (NB - 1) * gap;
        int startX = (vpW - totalW) / 2;
        int cardY  = vpH / 2 - cardH / 2 + 10;

        for (int i = 0; i < NB; i++) {
            int cx    = startX + i * (cardW + gap);
            boolean sel      = (i == selectionIndex);
            boolean canBuy   = (joueur.getMonnaie() >= COUTS[i]);
            Color   accent   = COULEURS[i];

            // Fond de la carte
            Color bg = sel
                    ? new Color(accent.getRed() / 5, accent.getGreen() / 5, accent.getBlue() / 5 + 20)
                    : new Color(18, 22, 42);
            g2.setColor(bg);
            g2.fillRoundRect(cx, cardY, cardW, cardH, 12, 12);

            // Barre colorée en haut
            g2.setColor(accent.darker());
            g2.fillRoundRect(cx, cardY, cardW, 6, 6, 6);
            g2.fillRect(cx, cardY + 3, cardW, 3);

            // Bordure
            g2.setStroke(new BasicStroke(sel ? 2.5f : 1.2f));
            g2.setColor(sel ? accent : new Color(60, 65, 90));
            g2.drawRoundRect(cx, cardY, cardW, cardH, 12, 12);
            g2.setStroke(new BasicStroke(1f));

            // Nom de l'amélioration
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.setColor(Color.WHITE);
            FontMetrics fmN = g2.getFontMetrics();
            g2.drawString(NOMS[i], cx + (cardW - fmN.stringWidth(NOMS[i])) / 2, cardY + 28);

            // Description (2 lignes)
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(new Color(180, 190, 210));
            for (int l = 0; l < DETAILS[i].length; l++) {
                FontMetrics fmD = g2.getFontMetrics();
                g2.drawString(DETAILS[i][l],
                        cx + (cardW - fmD.stringWidth(DETAILS[i][l])) / 2,
                        cardY + 58 + l * 16);
            }

            // Coût
            int costY = cardY + cardH - 28;
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            String cout = COUTS[i] + " g";
            FontMetrics fmC = g2.getFontMetrics();
            g2.setColor(canBuy ? new Color(255, 210, 50) : new Color(160, 70, 70));
            g2.drawString(cout, cx + (cardW - fmC.stringWidth(cout)) / 2, costY);

            if (!canBuy) {
                g2.setFont(new Font("SansSerif", Font.ITALIC, 9));
                g2.setColor(new Color(160, 70, 70));
                String nm = "Or insuffisant";
                FontMetrics fmM = g2.getFontMetrics();
                g2.drawString(nm, cx + (cardW - fmM.stringWidth(nm)) / 2, costY + 14);
            }

            // Indicateur de sélection (triangle en bas de la carte)
            if (sel) {
                int tx = cx + cardW / 2;
                int ty = cardY + cardH + 12;
                int[] xs = { tx - 8, tx + 8, tx };
                int[] ys = { ty,     ty,     ty + 10 };
                g2.setColor(accent);
                g2.fillPolygon(xs, ys, 3);
            }
        }
    }
}
