package com.fermedefense.vue;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.fermedefense.controleur.ControleurJeu;
import com.fermedefense.controleur.ControleurJoueur;
import com.fermedefense.modele.ferme.Ferme;
import com.fermedefense.modele.ferme.Vache;
import com.fermedefense.modele.jeu.Carte;
import com.fermedefense.modele.jeu.Zone;
import com.fermedefense.modele.joueur.Joueur;
import com.fermedefense.modele.marche.ArticleMarche;
import com.fermedefense.modele.marche.Marche;
import com.fermedefense.modele.marche.TypeArticle;
import com.fermedefense.utilitaire.Constantes;

/**
 * Fenêtre principale du jeu.
 * Contient le panneau de jeu (carte + joueur) et le HUD.
 */
public class VuePrincipale extends JFrame {

    private final Joueur joueur;
    private final Ferme ferme;
    private final Carte carte;
    private final Marche marche;

    private final VueHUD vueHUD;
    private final VueFerme vueFerme;
    private final VueMarche vueMarche;
    private final PanneauJeu panneauJeu;

    private final ControleurJeu controleurJeu;

    /** Message temporaire affiché à l'écran. */
    private String messageFlash = null;
    private long messageFlashExpire = 0;

    public VuePrincipale(Joueur joueur, Ferme ferme, Carte carte, Marche marche) {
        super(Constantes.TITRE_FENETRE);
        this.joueur = joueur;
        this.ferme = ferme;
        this.carte = carte;
        this.marche = marche;

        // Vues
        this.vueFerme = new VueFerme(ferme);
        this.vueMarche = new VueMarche(marche);
        this.vueHUD = new VueHUD(joueur, ferme);
        this.panneauJeu = new PanneauJeu();

        // Layout
        setLayout(new BorderLayout());
        add(vueHUD, BorderLayout.NORTH);
        add(panneauJeu, BorderLayout.CENTER);

        // Contrôleurs
        ControleurJoueur ctrlJoueur = new ControleurJoueur(joueur);
        panneauJeu.addKeyListener(ctrlJoueur);
        panneauJeu.addKeyListener(new ActionKeyListener());

        controleurJeu = new ControleurJeu(joueur, ferme, carte, panneauJeu);

        // Fenêtre
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        panneauJeu.setFocusable(true);
        panneauJeu.requestFocusInWindow();
    }

    /** Démarre le jeu. */
    public void lancer() {
        setVisible(true);
        controleurJeu.demarrer();
    }

    private void flash(String msg) {
        messageFlash = msg;
        messageFlashExpire = System.currentTimeMillis() + 2000;
    }

    // ─────────────────────────────────────────────
    // Panneau de jeu (dessine carte + joueur)
    // ─────────────────────────────────────────────
    private class PanneauJeu extends JPanel {

        PanneauJeu() {
            setPreferredSize(new Dimension(Constantes.LARGEUR_CARTE, Constantes.HAUTEUR_CARTE));
            setBackground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int[] zf = carte.getZoneFerme();
            int[] zm = carte.getZoneMarche();

            // Zones
            vueFerme.dessiner(g2, zf[0], zf[1], zf[2], zf[3]);
            vueMarche.dessiner(g2, zm[0], zm[1], zm[2], zm[3]);

            // Séparateur
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(zm[0], 0, zm[0], getHeight());

            // Joueur
            int jx = (int) joueur.getX();
            int jy = (int) joueur.getY();
            int jt = joueur.getTaille();
            g2.setColor(new Color(220, 120, 50));
            g2.fillRoundRect(jx, jy, jt, jt, 8, 8);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString("J", jx + 10, jy + 20);

            // Zone indicator
            Zone zone = carte.getZoneA(jx + jt / 2, jy + jt / 2);
            if (zone != null) {
                g2.setColor(new Color(255, 255, 255, 150));
                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g2.drawString("Zone: " + zone, 10, getHeight() - 10);
            }

            // Message flash
            if (messageFlash != null && System.currentTimeMillis() < messageFlashExpire) {
                g2.setColor(new Color(255, 255, 255, 200));
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(messageFlash);
                g2.drawString(messageFlash, (getWidth() - tw) / 2, getHeight() / 2);
            }

            // Repaint HUD too
            vueHUD.repaint();
        }
    }

    // ─────────────────────────────────────────────
    // Actions spéciales (acheter, récolter)
    // ─────────────────────────────────────────────
    private class ActionKeyListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            Zone zone = carte.getZoneA(
                    (int) joueur.getX() + joueur.getTaille() / 2,
                    (int) joueur.getY() + joueur.getTaille() / 2);

            switch (e.getKeyCode()) {
                // --- Marché : sélectionner article ---
                case KeyEvent.VK_1:
                    if (zone == Zone.MARCHE) vueMarche.setSelection(0);
                    break;
                case KeyEvent.VK_2:
                    if (zone == Zone.MARCHE) vueMarche.setSelection(1);
                    break;
                case KeyEvent.VK_TAB:
                    if (zone == Zone.MARCHE) vueMarche.selectionSuivante();
                    break;

                // --- Marché : acheter ---
                case KeyEvent.VK_ENTER:
                    if (zone == Zone.MARCHE) acheter();
                    break;

                // --- Ferme : récolter ---
                case KeyEvent.VK_R:
                    if (zone == Zone.FERME) recolter();
                    break;
            }
        }
    }

    private void acheter() {
        int idx = vueMarche.getSelection();
        if (idx < 0) {
            flash("Sélectionnez un article (1/2)");
            return;
        }
        ArticleMarche article = marche.getArticles().get(idx);
        if (joueur.getMonnaie() < article.getPrix()) {
            flash("Fonds insuffisants !");
            return;
        }
        if (article.getType() == TypeArticle.VACHE && ferme.estPleine()) {
            flash("Ferme pleine !");
            return;
        }
        // Effectuer l'achat
        joueur.depenser(article.getPrix());
        if (article.getType() == TypeArticle.VACHE) {
            // Place la vache à une position aléatoire dans la zone ferme
            int[] zf = carte.getZoneFerme();
            double vx = zf[0] + 20 + Math.random() * (zf[2] - 60);
            double vy = zf[1] + 50 + Math.random() * (zf[3] - 100);
            ferme.ajouterVache(new Vache(article.getNom() + "#" + (ferme.getNombreAnimaux() + 1), vx, vy));
            flash("Vache achetée !");
        } else if (article.getType() == TypeArticle.ARME) {
            flash("Arme achetée : " + article.getNom());
            // TODO: ajouter au joueur quand le module combat sera prêt
        }
    }

    private void recolter() {
        int total = ferme.recolterTout();
        if (total > 0) {
            joueur.ajouterMonnaie(total);
            flash("Récolté " + total + " pièces !");
        } else {
            flash("Rien à récolter.");
        }
    }
}
