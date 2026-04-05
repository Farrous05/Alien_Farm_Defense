package com.fermedefense.vue;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import com.fermedefense.modele.marche.ArticleMarche;

/**
 * Dessine les vendeurs dans le monde et leur popup d'achat.
 */
public class VueMarchePopup {

    // ── Sprite du vendeur dans le monde ──────────────────────────────────────

    private static final int SPRITE_SIZE = 48;
    private static final int LABEL_H     = 18;

    /**
     * Dessine le sprite + nom du vendeur à sa position écran.
     * Un anneau de proximité pulse quand le joueur est proche.
     */
    public void dessinerVendeur(Graphics2D g2, VendeurMarche v,
                                int screenX, int screenY,
                                boolean proche) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Anneau pulsant (si joueur dans le rayon)
        if (proche) {
            float glow = 0.35f + 0.25f * (float) Math.sin(
                    System.currentTimeMillis() * 0.006);
            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, glow));
            g2.setColor(new Color(255, 220, 60));
            g2.fillOval(screenX - 6, screenY - 6,
                    SPRITE_SIZE + 12, SPRITE_SIZE + 12);
            g2.setComposite(old);
        }

        // Sprite
        if (v.getSprite() != null) {
            g2.drawImage(v.getSprite(),
                    screenX, screenY, SPRITE_SIZE, SPRITE_SIZE, null);
        } else {
            // Fallback carré coloré
            g2.setColor(new Color(200, 160, 60));
            g2.fillRoundRect(screenX, screenY, SPRITE_SIZE, SPRITE_SIZE, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 22));
            g2.drawString("?", screenX + 16, screenY + 32);
        }

        // Ombre nom
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(screenX - 2, screenY + SPRITE_SIZE + 2,
                SPRITE_SIZE + 4, LABEL_H, 4, 4);

        // Nom
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();
        String nom = v.getNom();
        g2.setColor(new Color(255, 230, 100));
        g2.drawString(nom,
                screenX + (SPRITE_SIZE - fm.stringWidth(nom)) / 2,
                screenY + SPRITE_SIZE + LABEL_H - 3);

        // Petite bulle "[R]" quand proche
        if (proche) {
            g2.setColor(new Color(255, 255, 255, 220));
            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            g2.drawString("[R] acheter",
                    screenX - 2, screenY - 5);
        }
    }

    // ── Popup d'achat ────────────────────────────────────────────────────────

    private static final int POP_W  = 340;
    private static final int POP_H_BASE = 100;
    private static final int ROW_H  = 44;

    /**
     * Dessine le popup de boutique en bas au centre du viewport.
     */
    public void dessinerPopup(Graphics2D g2, VendeurMarche vendeur,
                              int vpW, int vpH, int niveau) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        List<ArticleMarche> articles = vendeur.getArticles();
        int popH = POP_H_BASE + articles.size() * ROW_H;
        int px   = (vpW - POP_W) / 2;
        int py   = vpH - popH - 20;

        // Fond
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 0.92f));
        g2.setColor(new Color(12, 14, 32));
        g2.fillRoundRect(px, py, POP_W, popH, 14, 14);
        g2.setComposite(old);

        // Bordure dorée
        g2.setColor(new Color(200, 160, 50));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(px, py, POP_W, popH, 14, 14);
        g2.setStroke(new BasicStroke(1f));

        // Titre vendeur
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(new Color(255, 215, 60));
        FontMetrics fmT = g2.getFontMetrics();
        String titre = "⚑  " + vendeur.getNom();
        g2.drawString(titre, px + (POP_W - fmT.stringWidth(titre)) / 2, py + 22);

        // Ligne de séparation
        g2.setColor(new Color(200, 160, 50, 120));
        g2.drawLine(px + 12, py + 30, px + POP_W - 12, py + 30);

        // Articles
        int sel = vendeur.getSelection();
        for (int i = 0; i < articles.size(); i++) {
            ArticleMarche a  = articles.get(i);
            boolean locked   = !a.isDebloque(niveau);
            boolean selected = (i == sel);
            int ry = py + 36 + i * ROW_H;

            // Fond ligne sélectionnée
            if (selected && !locked) {
                float pulse = 0.15f + 0.10f * (float) Math.sin(
                        System.currentTimeMillis() * 0.007);
                Composite o2 = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, pulse));
                g2.setColor(new Color(255, 215, 60));
                g2.fillRoundRect(px + 8, ry, POP_W - 16, ROW_H - 4, 8, 8);
                g2.setComposite(o2);

                // Flèche sélection
                g2.setColor(new Color(255, 215, 60));
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                g2.drawString("▶", px + 10, ry + 22);
            }

            // Nom article
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.setColor(locked ? new Color(120, 120, 135)
                    : (selected ? Color.WHITE : new Color(200, 200, 220)));
            g2.drawString(a.getNom(), px + 30, ry + 16);

            // Description / type
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(locked ? new Color(100, 100, 115)
                    : new Color(160, 160, 180));
            String sub = locked ? ("Niveau " + a.getNiveauRequis() + " requis")
                    : a.getType().toString();
            g2.drawString(sub, px + 30, ry + 30);

            // Prix
            if (!locked) {
                String prix = a.getPrix() + " g";
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(new Color(255, 215, 60));
                g2.drawString(prix, px + POP_W - fm.stringWidth(prix) - 14, ry + 22);
            }
        }

        // Instructions en bas
        int iy = py + popH - 22;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(new Color(160, 160, 180));
        String hint = "↑↓ naviguer   [R] acheter   (éloignez-vous pour fermer)";
        FontMetrics fmH = g2.getFontMetrics();
        g2.drawString(hint, px + (POP_W - fmH.stringWidth(hint)) / 2, iy);
    }
}
