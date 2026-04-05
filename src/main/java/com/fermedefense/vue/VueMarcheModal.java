package com.fermedefense.vue;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import com.fermedefense.modele.marche.ArticleMarche;
import com.fermedefense.modele.marche.Marche;
import com.fermedefense.modele.marche.TypeArticle;

/**
 * Popup modal du marché dessiné en overlay sur le panneau de jeu.
 *
 * Affiché uniquement quand le joueur est dans la zone MARCHE.
 * Dessine une fenêtre centrée avec une carte par article, met
 * en évidence la sélection courante et affiche un message de retour.
 *
 * Usage dans VuePrincipale.PanneauJeu.paintComponent :
 *   if (vueMarcheModal.isVisible()) vueMarcheModal.dessiner(g2, panelW, panelH);
 */
public class VueMarcheModal {

    // --- Layout ---
    private static final int MODAL_W  = 520;
    private static final int MODAL_H  = 420;
    private static final int CARD_H   = 62;
    private static final int CARD_GAP = 8;
    private static final int PADDING  = 20;

    // --- Couleurs ---
    private static final Color BG_OVERLAY   = new Color(0, 0, 0, 160);
    private static final Color BG_PANEL     = new Color(18, 22, 45, 245);
    private static final Color BORDER       = new Color(80, 130, 220);
    private static final Color CARD_DEFAULT = new Color(30, 38, 70);
    private static final Color CARD_HOVER   = new Color(50, 80, 160);
    private static final Color CARD_BORDER  = new Color(60, 100, 200);
    private static final Color GOLD         = new Color(255, 210, 50);
    private static final Color TEXT_MAIN    = Color.WHITE;
    private static final Color TEXT_SUB     = new Color(180, 190, 210);
    private static final Color TEXT_HINT    = new Color(120, 140, 180);
    private static final Color MSG_OK       = new Color(80, 230, 100);
    private static final Color MSG_ERR      = new Color(230, 80, 80);

    private final Marche marche;

    private boolean visible = false;
    private int selection = 0;
    /** true si l'utilisateur a fermé manuellement le modal (empêche la réouverture auto). */
    private boolean userDismissed = false;

    /** Dernier message de feedback (achat réussi / raison d'échec). */
    private String feedbackMsg = null;
    private boolean feedbackOk = true;
    private long feedbackExpire = 0;

    public VueMarcheModal(Marche marche) {
        this.marche = marche;
    }

    // ── Visibilité ────────────────────────────────────────────────────────

    public boolean isVisible() { return visible; }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (!visible) {
            feedbackMsg = null;
            userDismissed = true;
        }
    }

    /**
     * Appelé quand le joueur quitte la zone marché.
     * Réinitialise le flag "fermé manuellement" pour que le modal
     * puisse se rouvrir automatiquement à la prochaine entrée.
     */
    public void reinitialiserDismiss() {
        userDismissed = false;
    }

    /**
     * Ouvre automatiquement le modal seulement si l'utilisateur
     * ne l'a pas fermé manuellement depuis la dernière entrée dans la zone.
     */
    public void ouvrirSiAutorise() {
        if (!userDismissed) {
            visible = true;
        }
    }

    // ── Sélection ─────────────────────────────────────────────────────────

    public int getSelection() { return selection; }

    public void selectionSuivante() {
        int max = marche.getArticles().size();
        selection = (selection + 1) % max;
    }

    public void selectionPrecedente() {
        int max = marche.getArticles().size();
        selection = (selection - 1 + max) % max;
    }

    /** Définit la sélection en cliquant sur un item par coordonnées dans le modal. */
    public int indexDepuisClick(int mx, int my, int panelW, int panelH) {
        int ox = (panelW - MODAL_W) / 2;
        int oy = (panelH - MODAL_H) / 2;
        int listStartY = oy + 60;
        for (int i = 0; i < marche.getArticles().size(); i++) {
            int iy = listStartY + i * (CARD_H + CARD_GAP);
            if (mx >= ox + PADDING && mx <= ox + MODAL_W - PADDING
                    && my >= iy && my <= iy + CARD_H) {
                return i;
            }
        }
        return -1;
    }

    public void setSelection(int idx) {
        List<ArticleMarche> articles = marche.getArticles();
        if (idx >= 0 && idx < articles.size()) {
            selection = idx;
        }
    }

    // ── Feedback ──────────────────────────────────────────────────────────

    public void afficherFeedback(String msg, boolean ok) {
        feedbackMsg = msg;
        feedbackOk = ok;
        feedbackExpire = System.currentTimeMillis() + 2200;
    }

    // ── Dessin ────────────────────────────────────────────────────────────

    /**
     * Dessine le modal en overlay centré sur le panneau de jeu.
     *
     * @param g2     contexte graphique
     * @param panelW largeur totale du panneau
     * @param panelH hauteur totale du panneau
     * @param monnaie monnaie actuelle du joueur (pour griser les articles trop chers)
     */
    public void dessiner(Graphics2D g2, int panelW, int panelH, int monnaie) {
        dessiner(g2, panelW, panelH, monnaie, 1);
    }

    public void dessiner(Graphics2D g2, int panelW, int panelH, int monnaie, int niveauActuel) {
        if (!visible) return;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fond assombri derrière le modal
        g2.setColor(BG_OVERLAY);
        g2.fillRect(0, 0, panelW, panelH);

        int ox = (panelW - MODAL_W) / 2;
        int oy = (panelH - MODAL_H) / 2;

        // Panneau principal
        g2.setColor(BG_PANEL);
        g2.fillRoundRect(ox, oy, MODAL_W, MODAL_H, 18, 18);

        g2.setColor(BORDER);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(ox, oy, MODAL_W, MODAL_H, 18, 18);
        g2.setStroke(new BasicStroke(1f));

        // Titre
        g2.setColor(TEXT_MAIN);
        g2.setFont(new Font("SansSerif", Font.BOLD, 20));
        String titre = "\uD83C\uDFEA  MARCHÉ";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(titre, ox + (MODAL_W - fm.stringWidth(titre)) / 2, oy + 36);

        // Séparateur sous le titre
        g2.setColor(BORDER);
        g2.drawLine(ox + PADDING, oy + 46, ox + MODAL_W - PADDING, oy + 46);

        // Liste d'articles
        List<ArticleMarche> articles = marche.getArticles();
        int listStartY = oy + 60;

        for (int i = 0; i < articles.size(); i++) {
            ArticleMarche a = articles.get(i);
            boolean selected = (i == selection);
            boolean debloque = a.isDebloque(niveauActuel);
            boolean abordable = debloque && monnaie >= a.getPrix();
            int iy = listStartY + i * (CARD_H + CARD_GAP);
            dessinerCarte(g2, a, ox + PADDING, iy, MODAL_W - 2 * PADDING, CARD_H, selected, abordable, debloque, niveauActuel);
        }

        // Message de feedback
        if (feedbackMsg != null && System.currentTimeMillis() < feedbackExpire) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.setColor(feedbackOk ? MSG_OK : MSG_ERR);
            FontMetrics fmMsg = g2.getFontMetrics();
            int mw = fmMsg.stringWidth(feedbackMsg);
            g2.drawString(feedbackMsg, ox + (MODAL_W - mw) / 2, oy + MODAL_H - 38);
        }

        // Aide clavier
        g2.setColor(TEXT_HINT);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        String hint = "↑/↓ naviguer   [R] acheter   [M] fermer";
        FontMetrics fmHint = g2.getFontMetrics();
        g2.drawString(hint, ox + (MODAL_W - fmHint.stringWidth(hint)) / 2, oy + MODAL_H - 14);
    }

    private void dessinerCarte(Graphics2D g2, ArticleMarche article, int cx, int cy,
                               int cw, int ch, boolean selected, boolean abordable,
                               boolean debloque, int niveauActuel) {
        // Fond de carte
        if (!debloque) {
            g2.setColor(new Color(20, 20, 30));
        } else if (selected) {
            GradientPaint gp = new GradientPaint(cx, cy, CARD_HOVER,
                    cx + cw, cy + ch, new Color(30, 60, 130));
            g2.setPaint(gp);
        } else {
            g2.setColor(CARD_DEFAULT);
        }
        g2.fillRoundRect(cx, cy, cw, ch, 10, 10);

        // Bordure
        if (selected && debloque) {
            g2.setColor(CARD_BORDER);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(cx, cy, cw, ch, 10, 10);
            g2.setStroke(new BasicStroke(1f));
        } else if (!debloque) {
            g2.setColor(new Color(60, 60, 70));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(cx, cy, cw, ch, 10, 10);
        }

        if (!debloque) {
            // Cadenas à gauche
            g2.setFont(new Font("SansSerif", Font.BOLD, 22));
            g2.setColor(new Color(120, 120, 140));
            g2.drawString("\uD83D\uDD12", cx + 10, cy + ch / 2 + 8);

            // Nom grisé
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.setColor(new Color(90, 90, 100));
            g2.drawString(article.getNom(), cx + 50, cy + 22);

            // "Débloqué au niveau X"
            g2.setFont(new Font("SansSerif", Font.ITALIC, 11));
            g2.setColor(new Color(140, 100, 60));
            g2.drawString("Débloqué au niveau " + article.getNiveauRequis()
                    + "  (actuel : " + niveauActuel + ")", cx + 50, cy + 40);

            // Prix grisé
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.setColor(new Color(80, 80, 90));
            String prixStr = "\u2B50 " + article.getPrix();
            FontMetrics fmP = g2.getFontMetrics();
            g2.drawString(prixStr, cx + cw - fmP.stringWidth(prixStr) - 12, cy + ch / 2 + 6);
            return;
        }

        // Icône de type
        String icone = iconePourType(article.getType());
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2.setColor(TEXT_MAIN);
        g2.drawString(icone, cx + 10, cy + ch / 2 + 8);

        // Nom de l'article
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(abordable ? TEXT_MAIN : new Color(130, 130, 130));
        g2.drawString(article.getNom(), cx + 50, cy + 22);

        // Description
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(TEXT_SUB);
        String desc = descriptionArticle(article);
        g2.drawString(desc, cx + 50, cy + 38);

        // Prix à droite
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.setColor(abordable ? GOLD : new Color(180, 100, 100));
        String prixStr = "\u2B50 " + article.getPrix();
        FontMetrics fmPrix = g2.getFontMetrics();
        g2.drawString(prixStr, cx + cw - fmPrix.stringWidth(prixStr) - 12, cy + ch / 2 + 6);

        // Badge "trop cher"
        if (!abordable) {
            g2.setFont(new Font("SansSerif", Font.ITALIC, 10));
            g2.setColor(new Color(220, 80, 80));
            g2.drawString("Fonds insuffisants", cx + cw - 120, cy + ch / 2 + 20);
        }
    }

    private String iconePourType(TypeArticle type) {
        return switch (type) {
            case VACHE  -> "\uD83D\uDC04";  // 🐄
            case ARME   -> "\u2694";         // ⚔
            case POTION -> "\uD83E\uDDEA";   // 🧪
            case BOMBE  -> "\uD83D\uDCA3";   // 💣
        };
    }

    private String descriptionArticle(ArticleMarche article) {
        return switch (article.getType()) {
            case VACHE  -> "Génère des revenus au fil du temps";
            case ARME   -> article.getPuissance() + " dégâts par coup";
            case POTION -> "Restaure 50 PV instantanément";
            case BOMBE  -> "Inflige 150 dégâts à l'alien ciblé";
        };
    }
}
