package com.fermedefense.controleur;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fermedefense.controleur.ControleurAttaque.PhaseAttaque;
import com.fermedefense.modele.combat.Arme;
import com.fermedefense.modele.combat.ResultatCombat;
import com.fermedefense.modele.ferme.Ferme;
import com.fermedefense.modele.ferme.Vache;
import com.fermedefense.modele.joueur.Joueur;
import com.fermedefense.modele.progression.Niveau;

class ControleurAttaqueTest {

    private ControleurAttaque ctrl;
    private Joueur joueur;
    private Ferme ferme;

    @BeforeEach
    void setUp() {
        Niveau niveau = new Niveau(1); // aliens: 20 PV, 5 dég, 1200ms cd
        ferme = new Ferme();
        ctrl = new ControleurAttaque(niveau, Arme.EPEE, ferme);
        ctrl.setZoneFerme(0, 0, 200, 200);
        joueur = new Joueur(100, 100, 180, 100, 200);
    }

    @Test
    void pasActifAvantDeclenchement() {
        assertFalse(ctrl.isActif());
        assertFalse(ctrl.isEnCombat());
        assertEquals(PhaseAttaque.INACTIF, ctrl.getPhase());
        assertNull(ctrl.getResultat());
        assertEquals(0, ctrl.getVaguesTerminees());
    }

    @Test
    void declencherVagueCommenceParApproche() {
        ctrl.declencherVague(0);
        assertTrue(ctrl.isActif());
        assertFalse(ctrl.isEnCombat());
        assertEquals(PhaseAttaque.APPROCHE, ctrl.getPhase());
        assertNotNull(ctrl.getAttaqueCourante());
        assertFalse(ctrl.getAliensVisuels().isEmpty());
    }

    @Test
    void victoireContreVagueNiveauUn() {
        ctrl.declencherVague(0);
        // Alien: 20 PV, épée: 15 dég/1000ms → 2 coups suffisent
        // Simuler assez de temps pour tuer l'alien
        for (int i = 0; i < 200; i++) {
            ctrl.mettreAJour(100, joueur);
            if (!ctrl.isActif()) break;
        }
        assertFalse(ctrl.isActif());
        assertEquals(ResultatCombat.VICTOIRE, ctrl.getResultat());
        assertEquals(1, ctrl.getVaguesTerminees());
    }

    @Test
    void joueurMortPendantVague() {
        // Joueur avec très peu de PV
        Joueur joueurFaible = new Joueur(100, 100, 180, 1, 200);
        ctrl.declencherVague(0);
        // Alien: 5 dég → un coup suffit à tuer le joueur (1 PV)
        for (int i = 0; i < 200; i++) {
            ctrl.mettreAJour(100, joueurFaible);
            if (!ctrl.isActif()) break;
        }
        assertFalse(ctrl.isActif());
        assertEquals(ResultatCombat.DEFAITE, ctrl.getResultat());
    }

    @Test
    void mettreAJourSansVagueNeRienFaire() {
        // Pas de vague déclenchée → ne doit pas planter
        ctrl.mettreAJour(1000, joueur);
        assertFalse(ctrl.isActif());
    }

    @Test
    void deuxVaguesSuccessives() {
        ctrl.declencherVague(0);
        for (int i = 0; i < 200; i++) {
            ctrl.mettreAJour(100, joueur);
            if (!ctrl.isActif()) break;
        }
        assertEquals(1, ctrl.getVaguesTerminees());

        ctrl.declencherVague(1);
        for (int i = 0; i < 200; i++) {
            ctrl.mettreAJour(100, joueur);
            if (!ctrl.isActif()) break;
        }
        assertEquals(2, ctrl.getVaguesTerminees());
    }

    // --- Abduction tests ---

    @Test
    void defaiteEnleveUneVache() {
        ferme.ajouterVache(new Vache("V1", 10, 10));
        ferme.ajouterVache(new Vache("V2", 20, 20));
        assertEquals(2, ferme.getNombreAnimaux());

        Joueur joueurFaible = new Joueur(100, 100, 180, 1, 200);
        ctrl.declencherVague(0);
        for (int i = 0; i < 200; i++) {
            ctrl.mettreAJour(100, joueurFaible);
            if (!ctrl.isActif()) break;
        }
        assertEquals(ResultatCombat.DEFAITE, ctrl.getResultat());
        assertEquals(1, ferme.getNombreAnimaux());
        assertNotNull(ctrl.getDerniereVacheEnlevee());
    }

    @Test
    void victoireNeEnlevePasDeVache() {
        ferme.ajouterVache(new Vache("V1", 10, 10));
        ctrl.declencherVague(0);
        for (int i = 0; i < 200; i++) {
            ctrl.mettreAJour(100, joueur);
            if (!ctrl.isActif()) break;
        }
        assertEquals(ResultatCombat.VICTOIRE, ctrl.getResultat());
        assertEquals(1, ferme.getNombreAnimaux());
        assertNull(ctrl.getDerniereVacheEnlevee());
    }

    @Test
    void defaiteFermeVidePasErreur() {
        assertEquals(0, ferme.getNombreAnimaux());
        Joueur joueurFaible = new Joueur(100, 100, 180, 1, 200);
        ctrl.declencherVague(0);
        for (int i = 0; i < 200; i++) {
            ctrl.mettreAJour(100, joueurFaible);
            if (!ctrl.isActif()) break;
        }
        assertEquals(ResultatCombat.DEFAITE, ctrl.getResultat());
        assertEquals(0, ferme.getNombreAnimaux());
        assertNull(ctrl.getDerniereVacheEnlevee());
    }
}
