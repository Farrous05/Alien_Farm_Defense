package com.fermedefense.vue;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.fermedefense.controleur.ControleurAttaque;
import com.fermedefense.controleur.ControleurCombat;
import com.fermedefense.controleur.ControleurJeu;
import com.fermedefense.controleur.ControleurMarche;
import com.fermedefense.controleur.ControleurJoueur;
import com.fermedefense.modele.combat.Attaque;
import com.fermedefense.modele.ferme.Ferme;
import com.fermedefense.modele.jeu.Carte;
import com.fermedefense.modele.jeu.EtatJeu;
import com.fermedefense.modele.jeu.Partie;
import com.fermedefense.modele.jeu.Zone;
import com.fermedefense.modele.joueur.Action;
import com.fermedefense.modele.joueur.ActionDuree;
import com.fermedefense.modele.joueur.Joueur;
import com.fermedefense.modele.marche.ArticleMarche;
import com.fermedefense.modele.marche.Marche;
import com.fermedefense.modele.progression.BarreProgression;
import com.fermedefense.utilitaire.Constantes;

/**
 * Fenêtre principale du jeu.
 * Gère le PanneauJeu (viewport + sidebar), la caméra,
 * l'animation du joueur et les interactions avec les vendeurs du marché.
 */
public class VuePrincipale extends JFrame {

    // ── Sprites joueur (Blue Boy Adventure) ──────────────────────────────────
    // [direction][frame]  direction: 0=bas 1=haut 2=gauche 3=droite
    private static final Image[][] SPR_JOUEUR = new Image[4][2];
    private static final int PLAYER_SIZE = 48;

    static {
        String[][] paths = {
            {"/images/player/boy_down_1.png",  "/images/player/boy_down_2.png"},
            {"/images/player/boy_up_1.png",    "/images/player/boy_up_2.png"},
            {"/images/player/boy_left_1.png",  "/images/player/boy_left_2.png"},
            {"/images/player/boy_right_1.png", "/images/player/boy_right_2.png"},
        };
        for (int d = 0; d < 4; d++) {
            for (int f = 0; f < 2; f++) {
                try {
                    var is = VuePrincipale.class.getResourceAsStream(paths[d][f]);
                    SPR_JOUEUR[d][f] = (is != null) ? ImageIO.read(is) : null;
                } catch (Exception e) {
                    SPR_JOUEUR[d][f] = null;
                }
            }
        }
    }

    // ── État animation joueur ─────────────────────────────────────────────────
    private int   playerDir   = 0;   // 0=bas 1=haut 2=gauche 3=droite
    private int   playerFrame = 0;   // 0 ou 1
    private long  lastFrameMs = 0;
    private static final long ANIM_MS = 180;

    // ── Modèle ────────────────────────────────────────────────────────────────
    private final Joueur  joueur;
    private final Ferme   ferme;
    private final Carte   carte;
    private final Partie  partie;

    // ── Vues ──────────────────────────────────────────────────────────────────
    private final VueHUD             vueHUD;
    private final VueFerme           vueFerme;
    private final VueMarche          vueMarche;
    private final VueMarchePopup     vueMarchePopup;
    private final VueBarreProgression vueBarreProgression;
    private final VueCombat          vueCombat;
    private final VueActionJoueur    vueActionJoueur;
    private final VueAliens          vueAliens;
    private final VueInventaire      vueInventaire;
    private final VueEffetHit        vueEffetHit;
    private final VueEffetTexte      vueEffetTexte;
    private final VueIndicateurs     vueIndicateurs;
    private final MondeRenderer      mondeRenderer;
    private final Camera             camera;
    private final PanneauJeu         panneauJeu;

    // ── Contrôleurs ───────────────────────────────────────────────────────────
    private final ControleurJeu    controleurJeu;
    private final ControleurMarche controleurMarche;

    // ── État UI ───────────────────────────────────────────────────────────────
    private int    dernierHpJoueur  = -1;
    private int    derniereMonnaie  = -1;
    private long   shakeFinMs       = 0;
    private static final int  SHAKE_AMP    = 4;
    private static final long SHAKE_DUREE  = 220;

    private String messageFlash       = null;
    private long   messageFlashExpire = 0;
    private boolean aEstPresse        = false; // true pendant que A est maintenu

    // ── Constructeur ──────────────────────────────────────────────────────────

    public VuePrincipale(Joueur joueur, Ferme ferme, Carte carte,
                         Marche marche, Partie partie) {
        super(Constantes.TITRE_FENETRE);
        this.joueur = joueur;
        this.ferme  = ferme;
        this.carte  = carte;
        this.partie = partie;

        // Caméra
        this.camera = new Camera(
                Constantes.LARGEUR_VIEWPORT, Constantes.HAUTEUR_VIEWPORT,
                Constantes.LARGEUR_CARTE,    Constantes.HAUTEUR_CARTE);

        // Vues
        this.vueFerme           = new VueFerme(ferme);
        this.vueMarche          = new VueMarche(marche);
        this.vueMarchePopup     = new VueMarchePopup();
        this.vueHUD             = new VueHUD(joueur, ferme, partie);
        this.vueBarreProgression = new VueBarreProgression();
        this.vueCombat          = new VueCombat();
        this.vueActionJoueur    = new VueActionJoueur();
        this.vueAliens          = new VueAliens();
        this.vueEffetHit        = new VueEffetHit();
        this.vueEffetTexte      = new VueEffetTexte();
        this.vueIndicateurs     = new VueIndicateurs();
        this.mondeRenderer      = new MondeRenderer();
        this.vueInventaire      = new VueInventaire(
                joueur, Constantes.LARGEUR_VIEWPORT + 10, 50);
        this.panneauJeu = new PanneauJeu();

        // Layout
        setLayout(new BorderLayout());
        add(vueHUD,      BorderLayout.NORTH);
        add(panneauJeu,  BorderLayout.CENTER);

        // Contrôleurs
        ControleurJoueur ctrlJoueur = new ControleurJoueur(joueur);
        panneauJeu.addKeyListener(ctrlJoueur);
        panneauJeu.addKeyListener(new ActionKeyListener());

        controleurJeu    = new ControleurJeu(joueur, ferme, carte, panneauJeu);
        controleurMarche = new ControleurMarche(joueur, ferme, marche, carte, controleurJeu);

        ControleurAttaque ctrlAtt = controleurJeu.getControleurAttaque();
        if (ctrlAtt != null) ctrlAtt.setPanneauJeu(panneauJeu);

        // Fenêtre
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        panneauJeu.setFocusable(true);
        panneauJeu.requestFocusInWindow();
    }

    public void lancer() {
        setVisible(true);
        partie.demarrer();
        controleurJeu.initialiserNiveau(partie);
        controleurJeu.demarrer();
    }

    private void flash(String msg) {
        messageFlash        = msg;
        messageFlashExpire  = System.currentTimeMillis() + 2500;
    }

    // ── PanneauJeu ────────────────────────────────────────────────────────────

    private class PanneauJeu extends JPanel {

        PanneauJeu() {
            setPreferredSize(new Dimension(
                    Constantes.LARGEUR_FENETRE,
                    Constantes.HAUTEUR_VIEWPORT));
            setBackground(new Color(20, 30, 40));

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    handleInventaireClick(e.getX(), e.getY());
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            long now = System.currentTimeMillis();

            // ── Caméra ──────────────────────────────────────────────────────
            camera.centrerSur(joueur);

            // ── Screen shake ────────────────────────────────────────────────
            if (now < shakeFinMs) {
                int sx = (int)((Math.random() * 2 - 1) * SHAKE_AMP);
                int sy = (int)((Math.random() * 2 - 1) * SHAKE_AMP);
                g2.translate(sx, sy);
            }

            // ── Fond monde ──────────────────────────────────────────────────
            mondeRenderer.draw(g2, camera);

            // ── Récolte automatique ─────────────────────────────────────────
            int totalAuto = ferme.recolterTout();
            if (totalAuto > 0) joueur.ajouterMonnaie(totalAuto);

            // ── Ferme (vaches) ──────────────────────────────────────────────
            vueFerme.dessiner(g2, camera);

            // ── Indicateurs de navigation (flèches) ─────────────────────────
            vueIndicateurs.dessiner(g2,
                    Constantes.LARGEUR_VIEWPORT, Constantes.HAUTEUR_VIEWPORT,
                    camera, ferme.getVaches(), vueMarche.getVendeurs());

            // ── Vendeurs marché ─────────────────────────────────────────────
            VendeurMarche actif = vueMarche.getVendeurActif(
                    joueur.getX(), joueur.getY());
            for (VendeurMarche v : vueMarche.getVendeurs()) {
                int sx = camera.toScreenX(v.getWorldX());
                int sy = camera.toScreenY(v.getWorldY());
                // Afficher uniquement si dans le viewport
                if (sx > -60 && sx < getWidth() && sy > -60 && sy < getHeight()) {
                    vueMarchePopup.dessinerVendeur(g2, v, sx, sy, v == actif);
                }
            }

            // ── Séparateur sidebar ───────────────────────────────────────────
            g2.setColor(new Color(80, 80, 90));
            g2.drawLine(Constantes.LARGEUR_VIEWPORT, 0,
                    Constantes.LARGEUR_VIEWPORT, getHeight());

            // ── Joueur ──────────────────────────────────────────────────────
            updatePlayerAnim(now);
            int jsx = camera.toScreenX(joueur.getX());
            int jsy = camera.toScreenY(joueur.getY());
            Image spr = SPR_JOUEUR[playerDir][playerFrame];
            if (spr != null) {
                g2.drawImage(spr, jsx, jsy, PLAYER_SIZE, PLAYER_SIZE, null);
            } else {
                g2.setColor(new Color(220, 120, 50));
                g2.fillRoundRect(jsx, jsy, PLAYER_SIZE, PLAYER_SIZE, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                g2.drawString("J", jsx + 17, jsy + 28);
            }

            // ── Action progress bar ─────────────────────────────────────────
            ActionDuree action = controleurJeu.getActionEnCours();
            vueActionJoueur.dessiner(g2, action, jsx, jsy, PLAYER_SIZE);

            // ── Barre de progression ─────────────────────────────────────────
            BarreProgression barre = controleurJeu.getBarreProgression();
            if (barre != null) {
                vueBarreProgression.dessiner(g2, barre, 10,
                        Constantes.HAUTEUR_VIEWPORT - 24,
                        Constantes.LARGEUR_VIEWPORT - 20);
            }

            // ── Inventaire (sidebar) ─────────────────────────────────────────
            vueInventaire.dessiner(g2);

            // ── Tutoriel (sidebar) ───────────────────────────────────────────
            dessinerTutoriel(g2);

            // ── Aliens (vague intermédiaire) ─────────────────────────────────
            ControleurAttaque ctrlAtt = controleurJeu.getControleurAttaque();
            if (ctrlAtt != null && ctrlAtt.isActif()) {
                vueAliens.dessiner(g2, ctrlAtt.getAliensVisuels(), false, 1.0, camera);
                if (ctrlAtt.isEnCombat() && aEstPresse) {
                    Attaque att = ctrlAtt.getAttaqueCourante();
                    vueCombat.dessiner(g2, att, joueur, getWidth(), getHeight(), false);
                }
            }

            // ── Boss ─────────────────────────────────────────────────────────
            ControleurCombat ctrlCombat = controleurJeu.getControleurCombat();
            if (ctrlCombat != null && ctrlCombat.isActif()) {
                double hpRatio = ctrlCombat.getBoss() != null
                        ? ctrlCombat.getBoss().getRatioPv() : 1.0;
                vueAliens.dessiner(g2, ctrlCombat.getAliensVisuels(), true, hpRatio, camera);
                if (ctrlCombat.isEnCombat() && aEstPresse) {
                    Attaque att = ctrlCombat.getAttaqueBoss();
                    vueCombat.dessiner(g2, att, joueur, getWidth(), getHeight(), true);
                }
            }

            // ── Popup vendeur actif ──────────────────────────────────────────
            if (actif != null) {
                vueMarchePopup.dessinerPopup(g2, actif,
                        Constantes.LARGEUR_VIEWPORT,
                        Constantes.HAUTEUR_VIEWPORT,
                        partie.getNiveau());
            }

            // ── Effets visuels ───────────────────────────────────────────────
            int hpCourant = joueur.getPointsDeVie();
            if (dernierHpJoueur >= 0 && hpCourant < dernierHpJoueur) {
                vueEffetHit.trigger(jsx + PLAYER_SIZE / 2, jsy + PLAYER_SIZE / 2, false);
                ControleurCombat ccs = controleurJeu.getControleurCombat();
                if (ccs != null && ccs.isEnCombat())
                    shakeFinMs = now + SHAKE_DUREE;
            }
            dernierHpJoueur = hpCourant;

            int monnaieCourante = joueur.getMonnaie();
            if (derniereMonnaie >= 0 && monnaieCourante > derniereMonnaie) {
                vueEffetTexte.triggerMonnaie(
                        monnaieCourante - derniereMonnaie,
                        jsx + PLAYER_SIZE / 2, jsy - 10);
            }
            derniereMonnaie = monnaieCourante;

            vueEffetHit.draw(g2);
            vueEffetTexte.draw(g2);

            // ── Fin de partie ────────────────────────────────────────────────
            EtatJeu etat = partie.getEtat();
            if (etat == EtatJeu.VICTOIRE || etat == EtatJeu.DEFAITE) {
                dessinerFinDePartie(g2, etat);
            }

            // ── Message flash ────────────────────────────────────────────────
            ControleurAttaque ca2 = controleurJeu.getControleurAttaque();
            String msg = null;
            if (ca2 != null && ca2.getMessageFlash() != null) {
                msg = ca2.getMessageFlash();
                ca2.clearMessageFlash();
            } else if (messageFlash != null && now < messageFlashExpire) {
                msg = messageFlash;
            }
            if (msg != null) {
                g2.setColor(new Color(255, 255, 255, 200));
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg,
                        (Constantes.LARGEUR_VIEWPORT - fm.stringWidth(msg)) / 2,
                        Constantes.HAUTEUR_VIEWPORT / 2 - 50);
            }

            vueHUD.repaint();
        }

        // ── Animation joueur ─────────────────────────────────────────────────

        private void updatePlayerAnim(long now) {
            Action dir = joueur.getDirectionCourante();
            if (dir != null) {
                playerDir = switch (dir) {
                    case BAS    -> 0;
                    case HAUT   -> 1;
                    case GAUCHE -> 2;
                    case DROITE -> 3;
                    default     -> playerDir;
                };
                if (now - lastFrameMs >= ANIM_MS) {
                    playerFrame  = 1 - playerFrame;
                    lastFrameMs  = now;
                }
            } else {
                playerFrame = 0; // immobile → frame repos
            }
        }

        // ── Clic inventaire ──────────────────────────────────────────────────

        private void handleInventaireClick(int mx, int my) {
            int viX  = vueInventaire.getX();
            int viY  = vueInventaire.getY();
            int viTC = vueInventaire.getTailleCase();
            int viW  = joueur.getInventaire().getColonnes() * viTC;
            int viH  = joueur.getInventaire().getLignes()   * viTC;

            if (mx < viX || mx >= viX + viW || my < viY || my >= viY + viH) return;

            int col = (mx - viX) / viTC;
            int lig = (my - viY) / viTC;
            vueInventaire.setSelection(lig, col);

            com.fermedefense.modele.joueur.ObjetInventaire obj =
                    joueur.getInventaire().getObjet(lig, col);

            if (obj instanceof com.fermedefense.modele.ferme.Vache) {
                deployerVache((com.fermedefense.modele.ferme.Vache) obj, lig, col);
            } else if (obj instanceof com.fermedefense.modele.combat.Arme) {
                attaquerAvec((com.fermedefense.modele.combat.Arme) obj);
                vueInventaire.setSelection(-1, -1);
            } else if (obj instanceof com.fermedefense.modele.joueur.Potion) {
                joueur.soigner(50);
                joueur.getInventaire().retirerObjet(lig, col);
                vueInventaire.setSelection(-1, -1);
                flash("Potion bue ! (+50 PV)");
            } else if (obj instanceof com.fermedefense.modele.combat.Bombe) {
                utiliserBombe(lig, col);
            }
            repaint();
        }

        private void deployerVache(com.fermedefense.modele.ferme.Vache v, int lig, int col) {
            Zone zone = carte.getZoneA(
                    (int) joueur.getX() + joueur.getTaille() / 2,
                    (int) joueur.getY() + joueur.getTaille() / 2);
            if (zone == Zone.FERME) {
                if (!ferme.estPleine()) {
                    joueur.getInventaire().retirerObjet(lig, col);
                    int[] zf = carte.getZoneFerme();
                    double vx = zf[0] + 60 + Math.random() * (zf[2] - 120);
                    double vy = zf[1] + 100 + Math.random() * (zf[3] - 200);
                    v.setX(vx); v.setY(vy);
                    ferme.ajouterVache(v);
                    vueInventaire.setSelection(-1, -1);
                    flash("Vache déployée !");
                } else {
                    flash("La ferme est pleine !");
                }
            } else {
                flash("Allez à la ferme pour déployer !");
            }
        }

        private void utiliserBombe(int lig, int col) {
            ControleurAttaque ca = controleurJeu.getControleurAttaque();
            ControleurCombat  cc = controleurJeu.getControleurCombat();
            if (ca != null && ca.isEnCombat()) {
                com.fermedefense.modele.combat.Extraterrestre alien =
                        ca.getAttaqueCourante().getAlienCourant();
                if (alien != null) alien.subirDegats(150);
                joueur.getInventaire().retirerObjet(lig, col);
                vueInventaire.setSelection(-1, -1);
                flash("BOUM ! −150 PV sur l'alien !");
            } else if (cc != null && cc.isEnCombat()) {
                cc.getBoss().subirDegats(150);
                joueur.getInventaire().retirerObjet(lig, col);
                vueInventaire.setSelection(-1, -1);
                flash("BOUM ! −150 PV sur le boss !");
            } else {
                flash("Gardez la bombe pour un combat !");
            }
        }

        // ── Overlay fin de partie ────────────────────────────────────────────

        private void dessinerFinDePartie(Graphics2D g2, EtatJeu etat) {
            g2.setColor(new Color(0, 0, 0, 175));
            g2.fillRect(0, 0, Constantes.LARGEUR_VIEWPORT, Constantes.HAUTEUR_VIEWPORT);

            int cx = Constantes.LARGEUR_VIEWPORT / 2;
            int cy = Constantes.HAUTEUR_VIEWPORT / 2 - 80;
            boolean victoire = (etat == EtatJeu.VICTOIRE);
            String titre = victoire ? "NIVEAU TERMINÉ !" : "GAME OVER";
            Color couleur = victoire ? new Color(80, 255, 80) : new Color(255, 80, 80);

            g2.setColor(couleur);
            g2.setFont(new Font("SansSerif", Font.BOLD, 38));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(titre, cx - fm.stringWidth(titre) / 2, cy);

            com.fermedefense.modele.jeu.ScorePartie sc = partie.getScoreNiveau();
            int panW = 340, panH = 150;
            int px = cx - panW / 2, py = cy + 20;
            g2.setColor(new Color(15, 18, 40, 220));
            g2.fillRoundRect(px, py, panW, panH, 14, 14);
            g2.setColor(victoire ? new Color(60, 120, 60) : new Color(120, 40, 40));
            g2.setStroke(new java.awt.BasicStroke(1.5f));
            g2.drawRoundRect(px, py, panW, panH, 14, 14);
            g2.setStroke(new java.awt.BasicStroke(1f));

            int ly = py + 24, col1 = px + 18, col2 = px + panW - 18;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            String[] labels = {"Aliens éliminés ×100","Vagues gagnées ×300",
                    "Boss vaincu ×2000×niv.","Vaches perdues −150","Dégâts infligés ÷5"};
            int[] values = {
                sc.getAliensElimines() * 100, sc.getVaguesGagnees() * 300,
                sc.isBossVaincu() ? 2000 * sc.getNiveau() : 0,
                -sc.getVachesPerdues() * 150, sc.getTotalDegatsInfliges() / 5};
            for (int i = 0; i < labels.length; i++) {
                g2.setColor(values[i] < 0 ? new Color(230,100,100) : new Color(190,210,255));
                g2.drawString(labels[i], col1, ly);
                String vs = (values[i] >= 0 ? "+" : "") + values[i];
                g2.setColor(values[i] < 0 ? new Color(230,100,100) : new Color(180,255,160));
                g2.drawString(vs, col2 - g2.getFontMetrics().stringWidth(vs), ly);
                ly += 18;
            }
            g2.setColor(new Color(255,210,50));
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            String total = "SCORE: " + sc.calculerScore();
            g2.drawString(total, cx - g2.getFontMetrics().stringWidth(total)/2, py+panH-10);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            String sub = victoire
                    ? "Appuyez sur [ESPACE] pour le niveau suivant"
                    : "Appuyez sur [ESPACE] pour recommencer";
            g2.drawString(sub, cx - g2.getFontMetrics().stringWidth(sub)/2, py+panH+30);
        }

        // ── Tutoriel sidebar ─────────────────────────────────────────────────

        private void dessinerTutoriel(Graphics2D g2) {
            int x = Constantes.LARGEUR_VIEWPORT + 10;
            int y = 310;
            g2.setColor(new Color(255, 255, 255, 200));
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString("CONTRÔLES", x, y);
            y += 18;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setColor(new Color(190, 190, 190));
            for (String l : new String[]{
                    "Déplacer : Z Q S D / ↑↓←→",
                    "","Marché : approchez un vendeur",
                    " ↑↓ : naviguer  [R] : acheter",
                    "","Inventaire : clic gauche",
                    " Vache → déployer en ferme",
                    " Arme  → attaquer",
                    " Potion → soigner",
                    "","[A] : attaquer arme équipée",
                    "[E] : changer arme",
                    "[P] : pause",
                    "[Espace] : niveau suivant",
                    "","Récolte : automatique !"
            }) {
                g2.drawString(l, x, y);
                y += 14;
            }
        }
    }

    // ── Attaque avec arme équipée ─────────────────────────────────────────────

    private void attaquerAvecArmeEquipee() {
        attaquerAvec(joueur.getArmeEquipee());
    }

    private void attaquerAvec(com.fermedefense.modele.combat.Arme arme) {
        if (arme == null) { flash("Aucune arme équipée !"); return; }
        ControleurAttaque ca = controleurJeu.getControleurAttaque();
        ControleurCombat  cc = controleurJeu.getControleurCombat();
        if (ca != null && ca.isEnCombat()) {
            ca.getAttaqueCourante().frapperManuel(arme);
            triggerHitEffet(ca.getAliensVisuels());
        } else if (cc != null && cc.isEnCombat()) {
            cc.getAttaqueBoss().frapperManuel(arme);
            if (cc.getBossVisuel() != null) {
                com.fermedefense.modele.combat.AlienVisuel bv = cc.getBossVisuel();
                vueEffetHit.trigger(camera.toScreenX(bv.getX()) + 48,
                        camera.toScreenY(bv.getY()) + 48, true);
            }
        } else if ((ca != null && ca.isActif()) || (cc != null && cc.isActif())) {
            flash("Alien trop loin ! Laissez-les approcher.");
        } else {
            flash("Aucun alien à combattre !");
        }
    }

    private void triggerHitEffet(List<com.fermedefense.modele.combat.AlienVisuel> vis) {
        if (vis != null && !vis.isEmpty()) {
            com.fermedefense.modele.combat.AlienVisuel av = vis.get(0);
            vueEffetHit.trigger(camera.toScreenX(av.getX()) + 32,
                    camera.toScreenY(av.getY()) + 32, true);
        }
    }

    // ── Clavier ───────────────────────────────────────────────────────────────

    private class ActionKeyListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            // ── Transitions d'état (ESPACE) ──────────────────────────────────
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                EtatJeu etat = partie.getEtat();
                if (etat == EtatJeu.VICTOIRE) {
                    partie.niveauSuivant(
                            Constantes.TEMPS_NIVEAU_MS + (partie.getNiveau() - 1) * 15_000L);
                    partie.demarrer();
                    joueur.soigner(joueur.getPointsDeVieMax());
                    controleurJeu.initialiserNiveau(partie);
                    flash("Niveau " + partie.getNiveau() + " !");
                    return;
                } else if (etat == EtatJeu.DEFAITE) {
                    partie.niveauSuivant(Constantes.TEMPS_NIVEAU_MS);
                    partie.demarrer();
                    joueur.soigner(joueur.getPointsDeVieMax());
                    controleurJeu.initialiserNiveau(partie);
                    flash("Recommençons !");
                    return;
                }
            }

            // ── Vendeur actif ─────────────────────────────────────────────────
            VendeurMarche actif = vueMarche.getVendeurActif(
                    joueur.getX(), joueur.getY());

            switch (e.getKeyCode()) {

                case KeyEvent.VK_UP:
                    if (actif != null) { actif.selectionPrecedente(); return; }
                    break;
                case KeyEvent.VK_DOWN:
                    if (actif != null) { actif.selectionSuivante(); return; }
                    break;

                case KeyEvent.VK_R:
                    if (actif != null) {
                        ArticleMarche article = actif.getArticleSelectionne();
                        controleurMarche.acheter(article);
                        flash(controleurMarche.getDernierMessage());
                    }
                    break;

                case KeyEvent.VK_A:
                    aEstPresse = true;
                    attaquerAvecArmeEquipee();
                    break;

                case KeyEvent.VK_E:
                    joueur.cycleArme();
                    com.fermedefense.modele.combat.Arme eq = joueur.getArmeEquipee();
                    flash("Arme : " + (eq != null ? eq.getNom() : "Aucune"));
                    break;

                case KeyEvent.VK_P:
                    if (partie.getEtat() == EtatJeu.EN_COURS) {
                        partie.basculerPause();
                        flash(partie.isEnPause() ? "PAUSE" : "Reprise !");
                    }
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_A) {
                aEstPresse = false;
            }
        }
    }
}
