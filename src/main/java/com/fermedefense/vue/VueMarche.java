package com.fermedefense.vue;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

import com.fermedefense.modele.marche.ArticleMarche;
import com.fermedefense.modele.marche.Marche;

import java.awt.Image;
import javax.imageio.ImageIO;

/**
 * Dessine le contenu de la zone Marché :
 * fond bleu, liste d'articles avec prix.
 */
public class VueMarche {

    private static Image imgBgMarche;

    static {
        try {
            imgBgMarche = ImageIO.read(VueMarche.class.getResource("/images/bg_marche.png"));
        } catch (Exception e) {
            System.err.println("Erreur chargement fond marché : " + e.getMessage());
        }
    }

    private final Marche marche;
    /** Index de l'article actuellement sélectionné (-1 = aucun). */
    private int selection = -1;

    public VueMarche(Marche marche) {
        this.marche = marche;
    }

    /**
     * Dessine le marché dans la zone (zx, zy, zw, zh).
     */
    public void dessiner(Graphics2D g2, int zx, int zy, int zw, int zh) {
        // Fond
        if (imgBgMarche != null) {
            g2.drawImage(imgBgMarche, zx, zy, zw, zh, null);
        } else {
            g2.setColor(new Color(50, 70, 130));
            g2.fillRect(zx, zy, zw, zh);
        }

        // Bordure
        g2.setColor(new Color(35, 50, 100));
        g2.drawRect(zx, zy, zw - 1, zh - 1);

        // Label
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString("MARCHÉ", zx + 10, zy + 24);

        // Articles
        List<ArticleMarche> articles = marche.getArticles();
        int startY = zy + 45;
        int itemH = 50;

        for (int i = 0; i < articles.size(); i++) {
            ArticleMarche a = articles.get(i);
            int iy = startY + i * itemH;
            boolean selected = (i == selection);

            // Fond item
            g2.setColor(selected ? new Color(80, 110, 180) : new Color(60, 80, 145));
            g2.fillRoundRect(zx + 10, iy, zw - 20, itemH - 5, 8, 8);

            // Nom
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.drawString(a.getNom(), zx + 20, iy + 20);

            // Type + prix
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(new Color(200, 200, 200));
            g2.drawString(a.getType() + "  |  Prix: " + a.getPrix(), zx + 20, iy + 36);

            // Prix highlight
            g2.setColor(new Color(255, 215, 0));
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString(String.valueOf(a.getPrix()), zx + zw - 55, iy + 25);
        }

        // Instructions
        g2.setColor(new Color(180, 180, 180));
        g2.setFont(new Font("SansSerif", Font.ITALIC, 11));
        g2.drawString("[1/2] sélectionner  [ENTER] acheter", zx + 10, zy + zh - 12);
    }

    public int getSelection() { return selection; }

    public void setSelection(int idx) {
        List<ArticleMarche> articles = marche.getArticles();
        if (idx >= 0 && idx < articles.size()) {
            this.selection = idx;
        }
    }

    public void selectionSuivante() {
        int max = marche.getArticles().size();
        selection = (selection + 1) % max;
    }

    public void selectionPrecedente() {
        int max = marche.getArticles().size();
        selection = (selection - 1 + max) % max;
    }
}
