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
    private final VueInventaire vueInventaire;
    private final PanneauJeu panneauJeu;

    private final ControleurJeu controleurJeu;
    private final ControleurMarche controleurMarche;

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
        this.vueInventaire = new VueInventaire(joueur.getInventaire(), Constantes.LARGEUR_CARTE + 10, 50);
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

        // Injection du panneau de jeu dans ControleurAttaque (pour notification visuelle)
        ControleurAttaque ctrlAttaque = controleurJeu.getControleurAttaque();
        if (ctrlAttaque != null) {
            ctrlAttaque.setPanneauJeu(panneauJeu);
        }

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
        messageFlashExpire = System.currentTimeMillis() + 32000; // 32 secondes
    }

    // ─────────────────────────────────────────────
    // Panneau de jeu (dessine carte + joueur + progression + combat)
    // ─────────────────────────────────────────────
    private class PanneauJeu extends JPanel {

        PanneauJeu() {
            setPreferredSize(new Dimension(Constantes.LARGEUR_FENETRE, Constantes.HAUTEUR_CARTE));
            setBackground(new Color(20, 30, 40)); // Darker background for the sidebar area
            

            // MouseListener :
            // - Clic droit : sélectionne l'article
            // - Clic gauche : achète l'article sélectionné si le joueur est dans la zone marché
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    int mx = e.getX();
                    int my = e.getY();
                    int[] zm = carte.getZoneMarche();
                    // Vérifie si le clic est dans la zone graphique du marché
                    if (mx >= zm[0] && mx <= zm[0] + zm[2] && my >= zm[1] && my <= zm[1] + zm[3]) {
                        int jx = (int) joueur.getX();
                        int jy = (int) joueur.getY();
                        int jt = joueur.getTaille();
                        Zone zone = carte.getZoneA(jx + jt / 2, jy + jt / 2);
                        // Condition : actions possibles uniquement si le joueur est dans la zone marché
                        if (zone == Zone.MARCHE) {
                            int startY = zm[1] + 45;
                            int itemH = 50;
                            int idx = (my - startY) / itemH;
                            if (idx >= 0 && idx < marche.getArticles().size()) {
                                if (e.getButton() == java.awt.event.MouseEvent.BUTTON3) {
                                    // Clic droit : sélectionne l'article
                                    vueMarche.setSelection(idx);
                                    repaint();
                                } else if (e.getButton() == java.awt.event.MouseEvent.BUTTON1) {
                                    // Clic gauche : achète l'article instantanément
                                    vueMarche.setSelection(idx);
                                    repaint();
                                    acheter();
                                }
                            }
                        }
                    }
                    
                    // Vérifie si le clic est dans l'inventaire
                    int viX = vueInventaire.getX();
                    int viY = vueInventaire.getY();
                    int viTC = vueInventaire.getTailleCase();
                    int viW = joueur.getInventaire().getColonnes() * viTC;
                    int viH = joueur.getInventaire().getLignes() * viTC;

                    if (mx >= viX && mx < viX + viW && my >= viY && my < viY + viH) {
                        int col = (mx - viX) / viTC;
                        int lig = (my - viY) / viTC;
                        vueInventaire.setSelection(lig, col);
                        
                        com.fermedefense.modele.joueur.ObjetInventaire obj = joueur.getInventaire().getObjet(lig, col);
                        if (obj instanceof com.fermedefense.modele.ferme.Vache) {
                            int jx = (int) joueur.getX();
                            int jy = (int) joueur.getY();
                            int jt = joueur.getTaille();
                            Zone zone = carte.getZoneA(jx + jt / 2, jy + jt / 2);
                            
                            if (zone == Zone.FERME) {
                                if (!ferme.estPleine()) {
                                    joueur.getInventaire().retirerObjet(lig, col);
                                    int[] zf = carte.getZoneFerme();
                                    double vx = zf[0] + 20 + Math.random() * (zf[2] - 60);
                                    double vy = zf[1] + 50 + Math.random() * (zf[3] - 100);
                                    com.fermedefense.modele.ferme.Vache v = (com.fermedefense.modele.ferme.Vache) obj;
                                    v.setX(vx);
                                    v.setY(vy);
                                    ferme.ajouterVache(v);
                                    vueInventaire.setSelection(-1, -1);
                                    flash("Vache déployée !");
                                } else {
                                    flash("La ferme est pleine !");
                                }
                            } else {
                                flash("Allez à la ferme pour déployer");
                            }
                        } else if (obj instanceof com.fermedefense.modele.combat.Arme) {
                            com.fermedefense.modele.combat.Arme arme = (com.fermedefense.modele.combat.Arme) obj;
                            ControleurAttaque ctrlAttaque = controleurJeu.getControleurAttaque();
                            ControleurCombat ctrlCombat = controleurJeu.getControleurCombat();
                            
                            if (ctrlAttaque != null && ctrlAttaque.isEnCombat()) {
                                ctrlAttaque.getAttaqueCourante().frapperManuel(arme);
                                vueInventaire.setSelection(-1, -1);
                            } else if (ctrlCombat != null && ctrlCombat.isEnCombat()) {
                                ctrlCombat.getAttaqueBoss().frapperManuel(arme);
                                vueInventaire.setSelection(-1, -1);
                            } else if ((ctrlAttaque != null && ctrlAttaque.isActif()) || (ctrlCombat != null && ctrlCombat.isActif())) {
                                flash("Alien trop loin ! Laissez-les approcher.");
                            } else {
                                flash("Aucun alien à combattre !");
                            }
                        }
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int[] zf = carte.getZoneFerme();
            int[] zm = carte.getZoneMarche();

            // Background de la carte
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, Constantes.LARGEUR_CARTE, getHeight());

            // Récolte automatique de la monnaie produite par les vaches
            int totalAuto = ferme.recolterTout();
            if (totalAuto > 0) {
                joueur.ajouterMonnaie(totalAuto);
            }

            // Zones
            vueFerme.dessiner(g2, zf[0], zf[1], zf[2], zf[3]);
            vueMarche.dessiner(g2, zm[0], zm[1], zm[2], zm[3]);

            // Séparateur entre marché et ferme
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(zm[0], 0, zm[0], getHeight());

            // Séparateur sidebar (fin de la carte)
            g2.setColor(new Color(80, 80, 90));
            g2.drawLine(Constantes.LARGEUR_CARTE, 0, Constantes.LARGEUR_CARTE, getHeight());

            // Joueur
            int jx = (int) joueur.getX();
            int jy = (int) joueur.getY();
            int jt = joueur.getTaille();
            Zone zone = carte.getZoneA(jx + jt / 2, jy + jt / 2);

            // Survol du marché par le personnage
            if (zone == Zone.MARCHE) {
                int startY = zm[1] + 45;
                int itemH = 50;
                int py = jy + jt / 2; // centre y du joueur
                int idx = (py - startY) / itemH;
                if (idx >= 0 && idx < marche.getArticles().size()) {
                    vueMarche.setSelection(idx);
                } else {
                    vueMarche.setSelection(-1);
                }
            } else {
                vueMarche.setSelection(-1);
            }

            g2.setColor(new Color(220, 120, 50));
            g2.fillRoundRect(jx, jy, jt, jt, 8, 8);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString("J", jx + 10, jy + 20);

            // Action progress bar above player
            ActionDuree action = controleurJeu.getActionEnCours();
            vueActionJoueur.dessiner(g2, action, jx, jy, jt);

            // Zone indicator
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
            
            // Inventaire
            vueInventaire.dessiner(g2);

            // Tutorial
            dessinerTutoriel(g2);

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

            // Message flash (priorité à celui du controleurAttaque)
            ControleurAttaque ctrlAttaque2 = controleurJeu.getControleurAttaque();
            String msg = null;
            if (ctrlAttaque2 != null && ctrlAttaque2.getMessageFlash() != null) {
                msg = ctrlAttaque2.getMessageFlash();
                ctrlAttaque2.clearMessageFlash();
            } else if (messageFlash != null && System.currentTimeMillis() < messageFlashExpire) {
                msg = messageFlash;
            }
            if (msg != null) {
                g2.setColor(new Color(255, 255, 255, 200));
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(msg);
                g2.drawString(msg, (getWidth() - tw) / 2, getHeight() / 2 - 50);
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

        private void dessinerTutoriel(Graphics2D g2) {
            int x = Constantes.LARGEUR_CARTE + 10;
            int y = 300;
            
            g2.setColor(new Color(255, 255, 255, 200));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString("TUTORIEL & CONTRÔLES", x, y);
            
            y += 20;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(new Color(200, 200, 200));
            
            String[] lignes = {
                "Deplacements : Z Q S D (ou flèches)",
                "Marché : Alignez votre personnage",
                "         avec un objet et appuyez",
                "         sur [R] pour l'acheter",
                "         instantanément.",
                "",
                "Inventaire :",
                " - Clic sur Vache (Ferme) : Déploie",
                "",
                "Combat :",
                " - Appuyez sur [A] pour utiliser",
                "   votre arme et attaquer !",
                "",
                "Divers :",
                " [P] : Pause",
                " [Espace] : Avancer après victoire",
                "            (ou recommencer)",
                "",
                "Récolte : Automatique !"
            };
            
            for (String ligne : lignes) {
                g2.drawString(ligne, x, y);
                y += 15;
            }
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
                // --- Marché : acheter ---
                case KeyEvent.VK_R:
                    if (zone == Zone.MARCHE) acheter();
                    break;
                    
                // --- Attaque : utiliser la première arme ---
                case KeyEvent.VK_A:
                    attaquerAvecPremiereArme();
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

    private void acheter() {
        controleurMarche.acheter(vueMarche.getSelection());
        flash(controleurMarche.getDernierMessage());
    }

    private void attaquerAvecPremiereArme() {
        ControleurAttaque ctrlAttaque = controleurJeu.getControleurAttaque();
        ControleurCombat ctrlCombat = controleurJeu.getControleurCombat();

        com.fermedefense.modele.combat.Arme arme = null;
        com.fermedefense.modele.joueur.Inventaire inv = joueur.getInventaire();
        for (int i = 0; i < inv.getLignes(); i++) {
            for (int j = 0; j < inv.getColonnes(); j++) {
                com.fermedefense.modele.joueur.ObjetInventaire obj = inv.getObjet(i, j);
                if (obj instanceof com.fermedefense.modele.combat.Arme) {
                    arme = (com.fermedefense.modele.combat.Arme) obj;
                    break;
                }
            }
            if (arme != null) break;
        }

        if (arme == null) {
            flash("Aucune arme dans l'inventaire !");
            return;
        }

        if (ctrlAttaque != null && ctrlAttaque.isEnCombat()) {
            ctrlAttaque.getAttaqueCourante().frapperManuel(arme);
        } else if (ctrlCombat != null && ctrlCombat.isEnCombat()) {
            ctrlCombat.getAttaqueBoss().frapperManuel(arme);
        } else if ((ctrlAttaque != null && ctrlAttaque.isActif()) || (ctrlCombat != null && ctrlCombat.isActif())) {
            flash("Alien trop loin ! Laissez-les approcher.");
        } else {
            flash("Aucun alien à combattre !");
        }
    }
}
