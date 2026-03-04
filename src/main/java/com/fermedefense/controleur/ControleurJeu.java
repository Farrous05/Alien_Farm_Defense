package com.fermedefense.controleur;

import javax.swing.JPanel;
import javax.swing.Timer;

import com.fermedefense.modele.ferme.Ferme;
import com.fermedefense.modele.jeu.Carte;
import com.fermedefense.modele.joueur.Joueur;

/**
 * Contrôleur principal du jeu.
 * Pilote la boucle de mise à jour (Swing Timer, delta-time)
 * et coordonne les mises à jour du modèle et le repaint de la vue.
 */
public class ControleurJeu {

    private final Joueur joueur;
    private final Ferme ferme;
    private final Carte carte;
    private final JPanel vue; // panneau principal à repeindre

    private Timer timer;
    private long dernierTick;
    private boolean enCours;

    public ControleurJeu(Joueur joueur, Ferme ferme, Carte carte, JPanel vue) {
        this.joueur = joueur;
        this.ferme = ferme;
        this.carte = carte;
        this.vue = vue;
    }

    /** Démarre la boucle de jeu. */
    public void demarrer() {
        dernierTick = System.currentTimeMillis();
        enCours = true;
        timer = new Timer(16, e -> tick()); // ~60 FPS
        timer.start();
    }

    /** Arrête la boucle. */
    public void arreter() {
        enCours = false;
        if (timer != null) timer.stop();
    }

    private void tick() {
        if (!enCours) return;
        long maintenant = System.currentTimeMillis();
        long delta = maintenant - dernierTick;
        dernierTick = maintenant;

        // 1. Joueur
        joueur.mettreAJour(delta);
        carte.clampJoueur(joueur);

        // 2. Ferme (croissance + production)
        ferme.mettreAJour(delta);

        // 3. Repaint
        vue.repaint();
    }

    public boolean isEnCours() { return enCours; }
}
