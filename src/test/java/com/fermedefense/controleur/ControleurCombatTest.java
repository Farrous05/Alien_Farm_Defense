package com.fermedefense.controleur;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fermedefense.controleur.ControleurCombat.PhaseBoss;
import com.fermedefense.modele.combat.Arme;
import com.fermedefense.modele.combat.ResultatCombat;
import com.fermedefense.modele.joueur.Joueur;
import com.fermedefense.modele.progression.Niveau;

class ControleurCombatTest {

    private ControleurCombat ctrl;
    private Joueur joueur;

    @BeforeEach
    void setUp() {
        Niveau niveau = new Niveau(1);
        ctrl = new ControleurCombat(niveau, Arme.EPEE);
        ctrl.setZoneFerme(0, 0, 200, 200);
        joueur = new Joueur(100, 100, 180, 100, 200);
    }

    @Test
    void pasActifAvantLancement() {
        assertFalse(ctrl.isActif());
        assertFalse(ctrl.isTermine());
        assertFalse(ctrl.isEnCombat());
        assertEquals(PhaseBoss.INACTIF, ctrl.getPhase());
        assertNull(ctrl.getResultat());
    }

    @Test
    void lancerCombatFinalCommenceParApproche() {
        ctrl.lancerCombatFinal();
        assertTrue(ctrl.isActif());
        assertFalse(ctrl.isEnCombat());
        assertEquals(PhaseBoss.APPROCHE, ctrl.getPhase());
        assertNotNull(ctrl.getBoss());
        assertNotNull(ctrl.getAttaqueBoss());
        assertNotNull(ctrl.getBossVisuel());
    }

    @Test
    void bossNiveauUnCreation() {
        ctrl.lancerCombatFinal();
        // BossFinal.pourNiveau(1): PV=120, dég=12, cd=1100
        assertEquals(120, ctrl.getBoss().getPointsDeVieMax());
        assertEquals(12, ctrl.getBoss().getDegats());
    }

    @Test
    void victoireContreBossNiveauUn() {
        ctrl.lancerCombatFinal();
        int monnaieAvant = joueur.getMonnaie();

        // Boss: 120 PV, épée: 15 dég/1000ms → 8 coups
        for (int i = 0; i < 400; i++) {
            ctrl.mettreAJour(100, joueur);
            if (ctrl.isEnCombat()) ctrl.getAttaqueBoss().frapperManuel(Arme.EPEE);
            if (ctrl.isTermine()) break;
        }
        assertTrue(ctrl.isTermine());
        assertFalse(ctrl.isActif());
        assertEquals(ResultatCombat.VICTOIRE, ctrl.getResultat());

        // Récompense ajoutée (150 pour niveau 1)
        assertTrue(joueur.getMonnaie() > monnaieAvant);
    }

    @Test
    void defaiteContreBoss() {
        Joueur joueurFaible = new Joueur(100, 100, 180, 5, 200);
        ctrl.lancerCombatFinal();

        for (int i = 0; i < 400; i++) {
            ctrl.mettreAJour(100, joueurFaible);
            if (ctrl.isEnCombat()) ctrl.getAttaqueBoss().frapperManuel(Arme.EPEE);
            if (ctrl.isTermine()) break;
        }
        assertTrue(ctrl.isTermine());
        assertEquals(ResultatCombat.DEFAITE, ctrl.getResultat());
    }

    @Test
    void mettreAJourSansLancerNeRienFaire() {
        ctrl.mettreAJour(1000, joueur);
        assertFalse(ctrl.isActif());
        assertFalse(ctrl.isTermine());
    }
}
