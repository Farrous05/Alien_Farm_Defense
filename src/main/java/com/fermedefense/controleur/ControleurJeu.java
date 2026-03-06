package com.fermedefense.controleur;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.Timer;

import com.fermedefense.modele.combat.Arme;
import com.fermedefense.modele.combat.ResultatCombat;
import com.fermedefense.modele.ferme.Ferme;
import com.fermedefense.modele.jeu.Carte;
import com.fermedefense.modele.jeu.EtatJeu;
import com.fermedefense.modele.jeu.Partie;
import com.fermedefense.modele.joueur.Joueur;
import com.fermedefense.modele.progression.BarreProgression;
import com.fermedefense.modele.progression.EvenementTemporel;
import com.fermedefense.modele.progression.Niveau;

/**
 * Contrôleur principal du jeu.
 * Pilote la boucle de mise à jour (Swing Timer, delta-time)
 * et coordonne les mises à jour du modèle et le repaint de la vue.
 *
 * Gère aussi la boucle de progression : barre de temps,
 * déclenchement des vagues intermédiaires, combat final,
 * et passage au niveau suivant.
 */
public class ControleurJeu {

    private final Joueur joueur;
    private final Ferme ferme;
    private final Carte carte;
    private final JPanel vue; // panneau principal à repeindre

    private Timer timer;
    private long dernierTick;
    private boolean enCours;

    // --- Progression ---
    private Partie partie;
    private Niveau niveauCourant;
    private BarreProgression barreProgression;
    private ControleurAttaque controleurAttaque;
    private ControleurCombat controleurCombat;
    private Arme arme;

    public ControleurJeu(Joueur joueur, Ferme ferme, Carte carte, JPanel vue) {
        this.joueur = joueur;
        this.ferme = ferme;
        this.carte = carte;
        this.vue = vue;
        this.arme = Arme.EPEE;
    }

    /**
     * Initialise la progression pour un nouveau niveau.
     */
    public void initialiserNiveau(Partie partie) {
        this.partie = partie;
        this.niveauCourant = new Niveau(partie.getNiveau());
        this.barreProgression = new BarreProgression(niveauCourant);
        this.controleurAttaque = new ControleurAttaque(niveauCourant, arme);
        this.controleurCombat = new ControleurCombat(niveauCourant, arme);
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

        // 3. Progression (si initialisée)
        if (partie != null && partie.getEtat() == EtatJeu.EN_COURS) {
            tickProgression(delta);
        }

        // 4. Repaint
        vue.repaint();
    }

    /**
     * Sous-tick dédié à la progression :
     * barre de temps, vagues intermédiaires, combat final.
     */
    private void tickProgression(long delta) {
        // Si une attaque intermédiaire est en cours, la faire avancer
        if (controleurAttaque != null && controleurAttaque.isActif()) {
            controleurAttaque.mettreAJour(delta, joueur);
            if (!controleurAttaque.isActif()) {
                // Vague terminée
                if (controleurAttaque.getResultat() == ResultatCombat.DEFAITE) {
                    partie.terminer(false);
                }
                // Sinon on continue la barre de progression
            }
            return; // la barre est en pause pendant un combat
        }

        // Si le combat final est en cours
        if (controleurCombat != null && controleurCombat.isActif()) {
            controleurCombat.mettreAJour(delta, joueur);
            if (controleurCombat.isTermine()) {
                boolean victoire = controleurCombat.getResultat() == ResultatCombat.VICTOIRE;
                partie.terminer(victoire);
            }
            return;
        }

        // Faire avancer la barre de progression
        if (barreProgression != null && !barreProgression.isTerminee()) {
            List<EvenementTemporel> evenements = barreProgression.mettreAJour(delta);
            for (EvenementTemporel evt : evenements) {
                switch (evt.getType()) {
                    case ATTAQUE_INTERMEDIAIRE:
                        controleurAttaque.declencherVague(evt.getIndexVague());
                        break;
                    case COMBAT_FINAL:
                        partie.mettreAJour(delta); // force COMBAT_FINAL state
                        controleurCombat.lancerCombatFinal();
                        break;
                }
            }
        }

        // Mettre à jour la Partie aussi (synchro état)
        partie.mettreAJour(delta);
    }

    // --- Accesseurs ---

    public boolean isEnCours() { return enCours; }
    public Partie getPartie() { return partie; }
    public Niveau getNiveauCourant() { return niveauCourant; }
    public BarreProgression getBarreProgression() { return barreProgression; }
    public ControleurAttaque getControleurAttaque() { return controleurAttaque; }
    public ControleurCombat getControleurCombat() { return controleurCombat; }
    public Arme getArme() { return arme; }
    public void setArme(Arme arme) { this.arme = arme; }
}
