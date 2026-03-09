package com.fermedefense.vue;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

import com.fermedefense.modele.progression.BarreProgression;
import com.fermedefense.modele.progression.EvenementTemporel;

/**
 * Vue de la barre de progression temporelle.
 *
 * Dessine une barre horizontale en bas du panneau de jeu indiquant :
 *   - le temps écoulé (remplissage de gauche à droite)
 *   - les marqueurs de vagues intermédiaires (triangles)
 *   - le marqueur de combat final (tête d'alien)
 *   - le texte du temps restant
 */
public class VueBarreProgression {

    private static final int HAUTEUR_BARRE = 18;
    private static final Color COULEUR_FOND = new Color(40, 40, 40);
    private static final Color COULEUR_REMPLISSAGE = new Color(70, 180, 230);
    private static final Color COULEUR_REMPLISSAGE_DANGER = new Color(230, 80, 60);
    private static final Color COULEUR_MARQUEUR_VAGUE = new Color(255, 200, 50);
    private static final Color COULEUR_MARQUEUR_BOSS = new Color(255, 60, 60);

    /**
     * Dessine la barre de progression dans la zone donnée.
     *
     * @param g2    contexte graphique
     * @param barre modèle de la barre de progression
     * @param x     position x
     * @param y     position y
     * @param w     largeur disponible
     */
    public void dessiner(Graphics2D g2, BarreProgression barre, int x, int y, int w) {
        if (barre == null) return;

        double progression = barre.getProgression();
        long restant = barre.getTempsRestant();

        // Fond de la barre
        g2.setColor(COULEUR_FOND);
        g2.fillRoundRect(x, y, w, HAUTEUR_BARRE, 8, 8);

        // Remplissage (bleu normal, rouge si < 20%)
        Color couleurFill = progression > 0.8 ? COULEUR_REMPLISSAGE_DANGER : COULEUR_REMPLISSAGE;
        int fillW = (int) (w * progression);
        if (fillW > 0) {
            g2.setColor(couleurFill);
            g2.fillRoundRect(x, y, fillW, HAUTEUR_BARRE, 8, 8);
        }

        // Marqueurs d'événements
        List<EvenementTemporel> evenements = barre.getEvenements();
        for (EvenementTemporel evt : evenements) {
            double pos = (double) evt.getMomentMs() / barre.getDureeMs();
            int mx = x + (int) (w * pos);

            if (evt.getType() == EvenementTemporel.TypeEvenement.COMBAT_FINAL) {
                // Boss marker — red diamond
                g2.setColor(evt.isDeclenche() ? COULEUR_MARQUEUR_BOSS.darker() : COULEUR_MARQUEUR_BOSS);
                int[] polyX = {mx - 5, mx, mx + 5, mx};
                int[] polyY = {y + HAUTEUR_BARRE / 2, y, y + HAUTEUR_BARRE / 2, y + HAUTEUR_BARRE};
                g2.fillPolygon(polyX, polyY, 4);
            } else {
                // Wave marker — yellow triangle
                g2.setColor(evt.isDeclenche() ? COULEUR_MARQUEUR_VAGUE.darker() : COULEUR_MARQUEUR_VAGUE);
                int[] triX = {mx - 4, mx, mx + 4};
                int[] triY = {y + HAUTEUR_BARRE, y + 2, y + HAUTEUR_BARRE};
                g2.fillPolygon(triX, triY, 3);
            }
        }

        // Cadre
        g2.setColor(new Color(100, 100, 100));
        g2.drawRoundRect(x, y, w, HAUTEUR_BARRE, 8, 8);

        // Texte temps restant
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        long sec = restant / 1000;
        String texte = String.format("%d:%02d", sec / 60, sec % 60);
        g2.drawString(texte, x + w / 2 - 15, y + 14);
    }

    public int getHauteur() {
        return HAUTEUR_BARRE;
    }
}
