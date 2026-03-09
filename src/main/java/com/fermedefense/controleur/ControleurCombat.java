package com.fermedefense.controleur;

import java.util.Collections;
import java.util.List;

import com.fermedefense.modele.combat.AlienVisuel;
import com.fermedefense.modele.combat.Arme;
import com.fermedefense.modele.combat.Attaque;
import com.fermedefense.modele.combat.BossFinal;
import com.fermedefense.modele.combat.ResultatCombat;
import com.fermedefense.modele.joueur.Joueur;
import com.fermedefense.modele.progression.Niveau;

/**
 * Gère la logique du combat final (boss) en fin de niveau.
 *
 * Cycle visuel :
 *   INACTIF → APPROCHE (boss marche vers la ferme, ~2s)
 *           → COMBAT  (combat automatique)
 *           → DEPART  (boss fuit, ~1.5s)
 *           → TERMINE
 */
public class ControleurCombat {

    public enum PhaseBoss {
        INACTIF,
        APPROCHE,
        COMBAT,
        DEPART
    }

    private final Niveau niveau;
    private final Arme arme;
    private BossFinal boss;
    private Attaque attaqueBoss;
    private boolean actif;
    private boolean termine;
    private ResultatCombat resultat;
    private PhaseBoss phase;

    /** Alien visuel pour le boss. */
    private AlienVisuel bossVisuel;

    private int fermeX, fermeY, fermeW, fermeH;

    private static final double VITESSE_BOSS_VISUEL = 180;

    public ControleurCombat(Niveau niveau, Arme arme) {
        this.niveau = niveau;
        this.arme = arme;
        this.actif = false;
        this.termine = false;
        this.resultat = null;
        this.phase = PhaseBoss.INACTIF;
    }

    public void setZoneFerme(int x, int y, int w, int h) {
        this.fermeX = x;
        this.fermeY = y;
        this.fermeW = w;
        this.fermeH = h;
    }

    /**
     * Lance le combat final contre le boss du niveau.
     */
    public void lancerCombatFinal() {
        boss = niveau.creerBoss();
        attaqueBoss = new Attaque(boss);
        actif = true;
        termine = false;
        resultat = null;

        // Boss visuel : arrive du côté droit, cible le centre de la ferme
        double departX = fermeX + fermeW + 120;
        double cibleX = fermeX + fermeW / 2.0 - 20;
        double cibleY = fermeY + fermeH / 2.0;
        bossVisuel = new AlienVisuel(departX, cibleY, cibleX, cibleY, VITESSE_BOSS_VISUEL);
        phase = PhaseBoss.APPROCHE;
    }

    /**
     * Met à jour le combat du boss.
     */
    public void mettreAJour(long deltaMs, Joueur joueur) {
        if (!actif) return;

        switch (phase) {
            case APPROCHE:
                if (bossVisuel.mettreAJour(deltaMs)) {
                    phase = PhaseBoss.COMBAT;
                    bossVisuel.setEtat(AlienVisuel.EtatVisuel.COMBAT);
                }
                break;

            case COMBAT:
                attaqueBoss.mettreAJour(deltaMs, joueur, arme);
                bossVisuel.mettreAJour(deltaMs);

                if (attaqueBoss.isTerminee()) {
                    resultat = attaqueBoss.getResultat();
                    if (resultat == ResultatCombat.VICTOIRE) {
                        joueur.ajouterMonnaie(boss.getRecompense());
                        bossVisuel.setEtat(AlienVisuel.EtatVisuel.FUITE);
                    } else {
                        bossVisuel.setEtat(AlienVisuel.EtatVisuel.ENLEVEMENT);
                    }
                    phase = PhaseBoss.DEPART;
                }
                break;

            case DEPART:
                if (bossVisuel.mettreAJour(deltaMs)) {
                    actif = false;
                    termine = true;
                    phase = PhaseBoss.INACTIF;
                    bossVisuel = null;
                }
                break;

            default:
                break;
        }
    }

    // --- Accesseurs ---

    public boolean isActif() { return actif; }
    public boolean isTermine() { return termine; }
    public boolean isEnCombat() { return phase == PhaseBoss.COMBAT; }
    public ResultatCombat getResultat() { return resultat; }
    public BossFinal getBoss() { return boss; }
    public Attaque getAttaqueBoss() { return attaqueBoss; }
    public PhaseBoss getPhase() { return phase; }
    public AlienVisuel getBossVisuel() { return bossVisuel; }

    public List<AlienVisuel> getAliensVisuels() {
        if (bossVisuel == null) return Collections.emptyList();
        return List.of(bossVisuel);
    }
}
