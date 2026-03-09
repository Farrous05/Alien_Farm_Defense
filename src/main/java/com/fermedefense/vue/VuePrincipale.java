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

import com.fermedefense.controleur.ControleurAttaque;
import com.fermedefense.controleur.ControleurCombat;
import com.fermedefense.controleur.ControleurJeu;
import com.fermedefense.controleur.ControleurJoueur;
import com.fermedefense.controleur.ControleurMarche;
import com.fermedefense.modele.combat.Attaque;
import com.fermedefense.modele.ferme.Ferme;
import com.fermedefense.modele.jeu.Carte;
import com.fermedefense.modele.jeu.EtatJeu;
import com.fermedefense.modele.jeu.Partie;
import com.fermedefense.modele.jeu.Zone;
import com.fermedefense.modele.joueur.ActionDuree;
import com.fermedefense.modele.joueur.Joueur;
import com.fermedefense.modele.marche.Marche;
import com.fermedefense.modele.progression.BarreProgression;
import com.fermedefense.utilitaire.Constantes;

/**
 * Fenêtre principale du jeu.
 * Contient le panneau de jeu (carte + joueur), le HUD,
 * la barre de progression, et les overlays de combat.
 */
public class VuePrincipale extends JFrame {

    private final Joueur joueur;
    private final Ferme ferme;
    private final Carte carte;
    private final Marche marche;
    private final Partie partie;

    private final VueHUD vueHUD;
    private final VueFerme vueFerme;
    private final VueMarche vueMarche;
    private final VueBarreProgression vueBarreProgression;
    private final VueCombat vueCombat;
    private final VueActionJoueur vueActionJoueur;
    private final VueAliens vueAliens;
    private final PanneauJeu panneauJeu;

    private final ControleurJeu controleurJeu;
    private final ControleurMarche controleurMarche;

    /** Durée de l'action de récolte (ms). */
    private static final long DUREE_RECOLTE = 2000;
    /** Durée de l'action d'achat (ms). */
    private static final long DUREE_ACHAT = 1500;

    /** Message temporaire affiché à l'écran. */
    private String messageFlash = null;
    private long messageFlashExpire = 0;

    public VuePrincipale(Joueur joueur, Ferme ferme, Carte carte, Marche marche, Partie partie) {
        super(Constantes.TITRE_FENETRE);
        this.joueur = joueur;
        this.ferme = ferme;
        this.carte = carte;
        this.marche = marche;
        this.partie = partie;

        // Vues
        this.vueFerme = new VueFerme(ferme);
        this.vueMarche = new VueMarche(marche);
        this.vueHUD = new VueHUD(joueur, ferme);
        this.vueBarreProgression = new VueBarreProgression();
        this.vueCombat = new VueCombat();
        this.vueActionJoueur = new VueActionJoueur();
        this.vueAliens = new VueAliens();
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
        controleurMarche = new ControleurMarche(joueur, ferme, marche, carte, controleurJeu);

        // Fenêtre
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        panneauJeu.setFocusable(true);
        panneauJeu.requestFocusInWindow();
    }

    /** Démarre le jeu : initialise le niveau et lance la boucle. */
    public void lancer() {
        setVisible(true);
        partie.demarrer();
        controleurJeu.initialiserNiveau(partie);
        controleurJeu.demarrer();
    }

    private void flash(String msg) {
        messageFlash = msg;
        messageFlashExpire = System.currentTimeMillis() + 2000;
    }

    // ─────────────────────────────────────────────
    // Panneau de jeu (dessine carte + joueur + progression + combat)
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

            // Action progress bar above player
            ActionDuree action = controleurJeu.getActionEnCours();
            vueActionJoueur.dessiner(g2, action, jx, jy, jt);

            // Zone indicator
            Zone zone = carte.getZoneA(jx + jt / 2, jy + jt / 2);
            if (zone != null) {
                g2.setColor(new Color(255, 255, 255, 150));
                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g2.drawString("Zone: " + zone, 10, getHeight() - 30);
            }

            // Barre de progression (en bas du panneau)
            BarreProgression barre = controleurJeu.getBarreProgression();
            if (barre != null) {
                vueBarreProgression.dessiner(g2, barre, 10, getHeight() - 24,
                        getWidth() - 20);
            }

            // Niveau indicator
            g2.setColor(new Color(255, 255, 255, 200));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString("Niveau " + partie.getNiveau(), getWidth() - 80, 18);

            // Aliens visuels sur la carte (vague intermédiaire)
            ControleurAttaque ctrlAttaque = controleurJeu.getControleurAttaque();
            if (ctrlAttaque != null && ctrlAttaque.isActif()) {
                vueAliens.dessiner(g2, ctrlAttaque.getAliensVisuels(), false);
                // Combat overlay uniquement pendant la phase combat
                if (ctrlAttaque.isEnCombat()) {
                    Attaque att = ctrlAttaque.getAttaqueCourante();
                    vueCombat.dessiner(g2, att, joueur, getWidth(), getHeight(), false);
                }
            }

            // Boss visuel sur la carte (combat final)
            ControleurCombat ctrlCombat = controleurJeu.getControleurCombat();
            if (ctrlCombat != null && ctrlCombat.isActif()) {
                vueAliens.dessiner(g2, ctrlCombat.getAliensVisuels(), true);
                if (ctrlCombat.isEnCombat()) {
                    Attaque att = ctrlCombat.getAttaqueBoss();
                    vueCombat.dessiner(g2, att, joueur, getWidth(), getHeight(), true);
                }
            }

            // Game over / victoire overlay
            EtatJeu etat = partie.getEtat();
            if (etat == EtatJeu.VICTOIRE || etat == EtatJeu.DEFAITE) {
                dessinerFinDePartie(g2, etat);
            }

            // Message flash
            if (messageFlash != null && System.currentTimeMillis() < messageFlashExpire) {
                g2.setColor(new Color(255, 255, 255, 200));
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(messageFlash);
                g2.drawString(messageFlash, (getWidth() - tw) / 2, getHeight() / 2 - 50);
            }

            // Repaint HUD too
            vueHUD.repaint();
        }

        private void dessinerFinDePartie(Graphics2D g2, EtatJeu etat) {
            // Dim overlay
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, getWidth(), getHeight());

            String texte;
            Color couleur;
            if (etat == EtatJeu.VICTOIRE) {
                texte = "NIVEAU TERMINÉ !";
                couleur = new Color(80, 255, 80);
            } else {
                texte = "GAME OVER";
                couleur = new Color(255, 80, 80);
            }

            g2.setColor(couleur);
            g2.setFont(new Font("SansSerif", Font.BOLD, 36));
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(texte);
            g2.drawString(texte, (getWidth() - tw) / 2, getHeight() / 2);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
            String sub = etat == EtatJeu.VICTOIRE
                    ? "Appuyez sur [ESPACE] pour le niveau suivant"
                    : "Appuyez sur [ESPACE] pour recommencer";
            FontMetrics fm2 = g2.getFontMetrics();
            g2.drawString(sub, (getWidth() - fm2.stringWidth(sub)) / 2, getHeight() / 2 + 30);
        }
    }

    // ─────────────────────────────────────────────
    // Actions spéciales (acheter, récolter, espace)
    // ─────────────────────────────────────────────
    private class ActionKeyListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            // Handle space for game state transitions
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                EtatJeu etat = partie.getEtat();
                if (etat == EtatJeu.VICTOIRE) {
                    // Niveau suivant
                    partie.niveauSuivant(Constantes.TEMPS_NIVEAU_MS + (partie.getNiveau() - 1) * 15_000L);
                    partie.demarrer();
                    joueur.soigner(joueur.getPointsDeVieMax());
                    controleurJeu.initialiserNiveau(partie);
                    flash("Niveau " + partie.getNiveau() + " !");
                    return;
                } else if (etat == EtatJeu.DEFAITE) {
                    // Recommencer au niveau 1
                    partie.niveauSuivant(Constantes.TEMPS_NIVEAU_MS);
                    partie.demarrer();
                    joueur.soigner(joueur.getPointsDeVieMax());
                    controleurJeu.initialiserNiveau(partie);
                    flash("Recommençons !");
                    return;
                }
            }

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
                    if (zone == Zone.MARCHE) lancerAchat();
                    break;

                // --- Ferme : récolter ---
                case KeyEvent.VK_R:
                    if (zone == Zone.FERME) lancerRecolte();
                    break;

                // --- Pause ---
                case KeyEvent.VK_P:
                    if (partie.getEtat() == EtatJeu.EN_COURS) {
                        partie.basculerPause();
                        flash(partie.isEnPause() ? "PAUSE" : "Reprise !");
                    }
                    break;
            }
        }
    }

    private void lancerAchat() {
        ActionDuree current = controleurJeu.getActionEnCours();
        if (current != null && !current.isTerminee()) return; // already busy
        ActionDuree action = new ActionDuree(ActionDuree.TypeAction.ACHAT, DUREE_ACHAT) {
            @Override
            public boolean mettreAJour(long deltaMs) {
                boolean done = super.mettreAJour(deltaMs);
                if (done) acheter();
                return done;
            }
        };
        controleurJeu.setActionEnCours(action);
    }

    private void lancerRecolte() {
        ActionDuree current = controleurJeu.getActionEnCours();
        if (current != null && !current.isTerminee()) return; // already busy
        ActionDuree action = new ActionDuree(ActionDuree.TypeAction.RECOLTE, DUREE_RECOLTE) {
            @Override
            public boolean mettreAJour(long deltaMs) {
                boolean done = super.mettreAJour(deltaMs);
                if (done) recolter();
                return done;
            }
        };
        controleurJeu.setActionEnCours(action);
    }

    private void acheter() {
        controleurMarche.acheter(vueMarche.getSelection());
        flash(controleurMarche.getDernierMessage());
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
