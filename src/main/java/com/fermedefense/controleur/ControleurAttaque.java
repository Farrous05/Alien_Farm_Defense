package com.fermedefense.controleur;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fermedefense.modele.combat.AlienVisuel;
import com.fermedefense.modele.combat.Arme;
import com.fermedefense.modele.combat.Attaque;
import com.fermedefense.modele.combat.Extraterrestre;
import com.fermedefense.modele.combat.ResultatCombat;
import com.fermedefense.modele.ferme.Ferme;
import com.fermedefense.modele.ferme.Vache;
import com.fermedefense.modele.joueur.Joueur;
import com.fermedefense.modele.progression.Niveau;

/**
 * Gère les attaques extraterrestres intermédiaires.
 *
 * Cycle visuel d'une vague :
 *   INACTIF → APPROCHE (aliens marchent vers la ferme, ~2s)
 *           → COMBAT  (combat automatique, overlay + aliens visibles)
 *           → DEPART  (aliens fuient ou enlèvent une vache, ~1.5s)
 *           → INACTIF
 */
public class ControleurAttaque {
    // Panneau de jeu pour notification visuelle
    private Object panneauJeu;

    /** Permet d'injecter le panneau de jeu pour notification visuelle */
    public void setPanneauJeu(Object panneauJeu) {
        this.panneauJeu = panneauJeu;
    }

    /** Phase de l'attaque en cours. */
    public enum PhaseAttaque {
        INACTIF,
        APPROCHE,
        COMBAT,
        DEPART
    }

    private final Niveau niveau;
    private final Arme arme;
    private final Ferme ferme;
    private Attaque attaqueCourante;
    private PhaseAttaque phase;
    private int vaguesTerminees;
    private Vache derniereVacheEnlevee;
    /** Total de vaches enlevées depuis le début du niveau (pour le score). */
    private int totalVachesEnlevees;

    /** Aliens visuels affichés sur la carte. */
    private List<AlienVisuel> aliensVisuels;

    /** Coordonnées de la zone ferme pour positionner les aliens. */
    private int fermeX, fermeY, fermeW, fermeH;

    /** Vitesse visuelle des aliens (pixels/sec). */
    private static final double VITESSE_ALIEN_VISUEL = 250;

    public ControleurAttaque(Niveau niveau, Arme arme, Ferme ferme) {
        this.niveau = niveau;
        this.arme = arme;
        this.ferme = ferme;
        this.attaqueCourante = null;
        this.phase = PhaseAttaque.INACTIF;
        this.vaguesTerminees = 0;
        this.derniereVacheEnlevee = null;
        this.aliensVisuels = Collections.emptyList();
    }

    /**
     * Définit les dimensions de la zone ferme (pour positionner les aliens).
     */
    public void setZoneFerme(int x, int y, int w, int h) {
        this.fermeX = x;
        this.fermeY = y;
        this.fermeW = w;
        this.fermeH = h;
    }

    /**
     * Déclenche une vague d'attaque intermédiaire.
     * Commence par la phase d'approche.
     *
     * @param indexVague index de la vague (0-based)
     */
    // Pour message flash
    private String messageFlash = null;
    public String getMessageFlash() { return messageFlash; }
    public void clearMessageFlash() { messageFlash = null; }

    private boolean attaqueSansDefense = false;

    public void declencherVague(int indexVague, com.fermedefense.modele.jeu.Carte carte, com.fermedefense.modele.joueur.Joueur joueur) {
        // Vérifier si le joueur est dans la zone ferme
        int jx = (int) joueur.getX();
        int jy = (int) joueur.getY();
        int jt = joueur.getTaille();
        com.fermedefense.modele.jeu.Zone zone = carte.getZoneA(jx + jt / 2, jy + jt / 2);
        attaqueSansDefense = (zone != com.fermedefense.modele.jeu.Zone.FERME);

        int nbCows = ferme.getNombreAnimaux();
        List<Extraterrestre> aliens = niveau.creerVagueDynamique(indexVague, Math.max(1, nbCows));
        attaqueCourante = attaqueSansDefense ? null : new Attaque(aliens);
        derniereVacheEnlevee = null;

        // Créer les aliens visuels : chaque alien cible la position d'une vache réelle
        aliensVisuels = new ArrayList<>();
        int departX = fermeX + fermeW + 100;
        java.util.List<Vache> vaches = ferme.getVaches();
        for (int i = 0; i < aliens.size(); i++) {
            double cibleX, cibleY;
            if (!vaches.isEmpty()) {
                // Chaque alien cible une vache différente (modulo si moins de vaches)
                Vache cible = vaches.get(i % vaches.size());
                cibleX = cible.getX() + 20;
                cibleY = cible.getY() + 20;
            } else {
                // Fallback : positions fixes dans la ferme
                cibleX = fermeX + 80 + (i % 3) * 120;
                cibleY = fermeY + 100 + (i / 3) * 100;
            }
            double departY = cibleY + (Math.random() - 0.5) * 60;
            aliensVisuels.add(new AlienVisuel(departX + i * 30, departY,
                    cibleX, cibleY, VITESSE_ALIEN_VISUEL, aliens.get(i).getType()));
        }

        phase = PhaseAttaque.APPROCHE;
    }

    /**
     * Met à jour la vague en cours (approche, combat, ou départ).
     *
     * @param deltaMs temps écoulé
     * @param joueur  le joueur
     */
    public void mettreAJour(long deltaMs, Joueur joueur) {
        if (phase == PhaseAttaque.INACTIF) return;


        switch (phase) {
            case APPROCHE:
                boolean tousArrives = true;
                for (AlienVisuel av : aliensVisuels) {
                    if (!av.mettreAJour(deltaMs)) {
                        tousArrives = false;
                    }
                }
                if (tousArrives) {
                    if (attaqueSansDefense) {
                        // Pas de défense : animation d'enlèvement directe
                        for (AlienVisuel av : aliensVisuels) {
                            av.setEtat(AlienVisuel.EtatVisuel.ENLEVEMENT);
                        }
                        phase = PhaseAttaque.DEPART;
                    } else {
                        // Transition vers le combat
                        phase = PhaseAttaque.COMBAT;
                        for (AlienVisuel av : aliensVisuels) {
                            av.setEtat(AlienVisuel.EtatVisuel.COMBAT);
                        }
                    }
                }
                break;

            case COMBAT:
                // Faire avancer le combat côté alien et cooldowns
                attaqueCourante.mettreAJour(deltaMs, joueur);
                // Animer les aliens visuels (tremblements)
                for (AlienVisuel av : aliensVisuels) {
                    av.mettreAJour(deltaMs);
                }

                int abductions = attaqueCourante.getVachesAbducteesCeTick();
                if (abductions > 0) {
                    for (int i = 1; i <= abductions; i++) {
                        ferme.enleverDerniereVache();
                        totalVachesEnlevees++;
                        int idxFui = attaqueCourante.getIndexAlienCourant() - i;
                        if (idxFui >= 0 && idxFui < aliensVisuels.size()) {
                            aliensVisuels.get(idxFui).setEtat(AlienVisuel.EtatVisuel.ENLEVEMENT);
                        }
                    }
                    messageFlash = "Alerte : Un alien a volé une vache !";
                }

                if (attaqueCourante.isTerminee()) {
                    // Transition vers le départ
                    phase = PhaseAttaque.DEPART;
                    if (attaqueCourante.getResultat() == ResultatCombat.VICTOIRE) {
                        vaguesTerminees++;
                        derniereVacheEnlevee = null;
                        for (AlienVisuel av : aliensVisuels) {
                            if (av.getEtat() == AlienVisuel.EtatVisuel.COMBAT) {
                                av.setEtat(AlienVisuel.EtatVisuel.FUITE);
                            }
                        }
                    } else {
                        derniereVacheEnlevee = ferme.enleverDerniereVache();
                        if (derniereVacheEnlevee != null) totalVachesEnlevees++;
                        for (AlienVisuel av : aliensVisuels) {
                            if (av.getEtat() == AlienVisuel.EtatVisuel.COMBAT) {
                                av.setEtat(AlienVisuel.EtatVisuel.ENLEVEMENT);
                            }
                        }
                        messageFlash = "Défaite : Une vache a été enlevée !";
                    }
                }
                break;

            case DEPART:
                boolean tousSortis = true;
                for (AlienVisuel av : aliensVisuels) {
                    if (!av.mettreAJour(deltaMs)) {
                        tousSortis = false;
                    }
                }
                if (tousSortis) {
                    if (attaqueSansDefense) {
                        derniereVacheEnlevee = ferme.enleverDerniereVache();
                        if (derniereVacheEnlevee != null) totalVachesEnlevees++;
                        messageFlash = "Une vache a été enlevée !";
                    }
                    phase = PhaseAttaque.INACTIF;
                    aliensVisuels = Collections.emptyList();
                    attaqueSansDefense = false;
                }
                break;

            default:
                break;
        }
    }

    /**
     * Vérifie si une attaque est encore visible (approche, combat, ou départ).
     */
    public boolean isActif() {
        return phase != PhaseAttaque.INACTIF;
    }

    /**
     * Vérifie si le combat proprement dit est en cours (pour l'overlay).
     */
    public boolean isEnCombat() {
        return phase == PhaseAttaque.COMBAT;
    }

    /**
     * Retourne le résultat de la dernière attaque (ou EN_COURS si active).
     */
    public ResultatCombat getResultat() {
        if (attaqueCourante == null) return null;
        return attaqueCourante.getResultat();
    }

    public Attaque getAttaqueCourante() { return attaqueCourante; }
    public int getVaguesTerminees() { return vaguesTerminees; }
    public Vache getDerniereVacheEnlevee() { return derniereVacheEnlevee; }
    public PhaseAttaque getPhase() { return phase; }
    public List<AlienVisuel> getAliensVisuels() { return aliensVisuels; }
    public int getTotalVachesEnlevees() { return totalVachesEnlevees; }
}
