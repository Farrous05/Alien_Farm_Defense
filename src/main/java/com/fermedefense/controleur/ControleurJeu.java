package com.fermedefense.controleur;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.Timer;

import com.fermedefense.modele.combat.Arme;
import com.fermedefense.modele.combat.ResultatCombat;
import com.fermedefense.modele.ferme.Ferme;
import com.fermedefense.modele.jeu.Carte;
import com.fermedefense.modele.jeu.EtatJeu;
import com.fermedefense.modele.jeu.GestionnaireSucces;
import com.fermedefense.modele.jeu.Partie;
import com.fermedefense.modele.jeu.Succes;
import com.fermedefense.modele.joueur.ActionDuree;
import com.fermedefense.modele.joueur.Joueur;
import com.fermedefense.modele.joueur.Upgrades;
import com.fermedefense.modele.progression.BarreProgression;
import com.fermedefense.modele.progression.EvenementTemporel;
import com.fermedefense.modele.progression.Niveau;
import com.fermedefense.utilitaire.SoundManager;

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
    private ActionDuree actionEnCours;

    // --- Succès & Upgrades ---
    private GestionnaireSucces gestionnaireSucces;
    private Upgrades upgrades;
    /** Compteur cumulatif d'aliens tués sur toute la partie. */
    private int totalAliensElimines;

    public ControleurJeu(Joueur joueur, Ferme ferme, Carte carte, JPanel vue) {
        this.joueur = joueur;
        this.ferme = ferme;
        this.carte = carte;
        this.vue = vue;
        this.arme = Arme.EPEE;
    }

    /** Injecte le gestionnaire de succès (appelé depuis VuePrincipale). */
    public void setGestionnaireSucces(GestionnaireSucces g) {
        this.gestionnaireSucces = g;
    }

    /** Injecte les upgrades du joueur (appelé depuis VuePrincipale). */
    public void setUpgrades(Upgrades u) {
        this.upgrades = u;
    }

    /**
     * Initialise la progression pour un nouveau niveau.
     * Applique le bonus d'or de départ si des upgrades sont actives.
     */
    public void initialiserNiveau(Partie partie) {
        this.partie = partie;
        this.niveauCourant = new Niveau(partie.getNiveau());
        this.barreProgression = new BarreProgression(niveauCourant);
        this.controleurAttaque = new ControleurAttaque(niveauCourant, arme, ferme);
        this.controleurCombat = new ControleurCombat(niveauCourant, arme);

        // Passer les coordonnées de la zone ferme pour le positionnement visuel des aliens
        int[] zf = carte.getZoneFerme();
        controleurAttaque.setZoneFerme(zf[0], zf[1], zf[2], zf[3]);
        controleurCombat.setZoneFerme(zf[0], zf[1], zf[2], zf[3]);

        // Bonus d'or de départ (upgrade)
        if (upgrades != null && upgrades.startingGoldBonus > 0) {
            joueur.ajouterMonnaie(upgrades.startingGoldBonus);
        }
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

        // 2b. Action en cours du joueur
        if (actionEnCours != null) {
            actionEnCours.mettreAJour(delta);
        }

        // 3. Progression (si initialisée)
        // Le combat final doit continuer à être mis à jour même quand l'état est COMBAT_FINAL.
        if (partie != null && (partie.getEtat() == EtatJeu.EN_COURS
            || partie.getEtat() == EtatJeu.COMBAT_FINAL)) {
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
            SoundManager.jouerThemeCombat();
            controleurAttaque.mettreAJour(delta, joueur);
            if (!controleurAttaque.isActif()) {
                // Vague terminée
                if (controleurAttaque.getResultat() == ResultatCombat.DEFAITE) {
                    partie.terminer(false);
                } else if (controleurAttaque.getResultat() == ResultatCombat.VICTOIRE) {
                    SoundManager.jouerJingle();
                    // Mettre à jour le score de la vague
                    com.fermedefense.modele.combat.Attaque att = controleurAttaque.getAttaqueCourante();
                    int nbAliens = att != null ? att.getAliens().size() : 0;
                    int degats   = att != null ? att.getTotalDegatsInfliges() : 0;
                    partie.getScoreNiveau().enregistrerVagueGagnee(nbAliens, degats);

                    // Succès : kills cumulatifs
                    totalAliensElimines += nbAliens;
                    if (gestionnaireSucces != null) {
                        gestionnaireSucces.verifier(Succes.PREMIER_SANG,  totalAliensElimines);
                        gestionnaireSucces.verifier(Succes.EXTERMINATEUR, totalAliensElimines);
                    }
                }
            }
            return; // la barre est en pause pendant un combat
        }

        // Si le combat final est en cours
        if (controleurCombat != null && controleurCombat.isActif()) {
            SoundManager.jouerThemeCombat();
            controleurCombat.mettreAJour(delta, joueur);
            if (controleurCombat.isTermine()) {
                boolean victoire = controleurCombat.getResultat() == ResultatCombat.VICTOIRE;
                if (victoire) {
                    SoundManager.jouerJingle();
                    com.fermedefense.modele.combat.Attaque attBoss = controleurCombat.getAttaqueBoss();
                    int degats = attBoss != null ? attBoss.getTotalDegatsInfliges() : 0;
                    partie.getScoreNiveau().enregistrerBossVaincu(degats);

                    // Succès boss : kills + INDESTRUCTIBLE
                    totalAliensElimines++;
                    if (gestionnaireSucces != null) {
                        gestionnaireSucces.verifier(Succes.PREMIER_SANG,  totalAliensElimines);
                        gestionnaireSucces.verifier(Succes.EXTERMINATEUR, totalAliensElimines);
                        if (joueur.getPointsDeVie() == joueur.getPointsDeVieMax()) {
                            gestionnaireSucces.verifier(Succes.INDESTRUCTIBLE, 1);
                        }
                    }
                }
                // Enregistrer les vaches perdues pendant toute la vague
                int vachesPerdues = controleurAttaque != null ? controleurAttaque.getTotalVachesEnlevees() : 0;
                for (int i = 0; i < vachesPerdues; i++) {
                    partie.getScoreNiveau().enregistrerVachePerdue();
                }
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
                        controleurAttaque.declencherVague(evt.getIndexVague(), carte, joueur);
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
    public ActionDuree getActionEnCours() { return actionEnCours; }
    public void setActionEnCours(ActionDuree action) { this.actionEnCours = action; }
    public int getTotalAliensElimines() { return totalAliensElimines; }

    /** Remet à zéro le compteur d'aliens (appelé au redémarrage d'une partie). */
    public void resetTotalAliensElimines() { totalAliensElimines = 0; }
}
