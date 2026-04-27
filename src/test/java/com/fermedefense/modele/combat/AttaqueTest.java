package com.fermedefense.modele.combat;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fermedefense.modele.joueur.Joueur;

/**
 * Tests unitaires pour Attaque (vague de combat automatique).
 */
class AttaqueTest {

    private Joueur joueur;
    private Arme epee;

    @BeforeEach
    void setUp() {
        // Joueur avec 100 PV, 500 monnaie
        joueur = new Joueur(0, 0, 100, 100, 500);
        epee = Arme.EPEE;
    }

    // ===== Combat basique =====

    @Test
    void combatDebutEnCours() {
        Attaque a = new Attaque(new Extraterrestre("A1", 30, 5, 800, 10000L));
        assertEquals(ResultatCombat.EN_COURS, a.getResultat());
        assertFalse(a.isTerminee());
    }

    @Test
    void victoireSiAlienMeurt() {
        // Alien avec 10 PV, épée fait 15 dégâts → un coup suffit
        Extraterrestre alien = new Extraterrestre("Faible", 10, 5, 2000, 10000L);
        Attaque a = new Attaque(alien);

        // Simuler assez de temps pour que le joueur frappe
        a.mettreAJour(epee.getCooldownMs(), joueur);
        a.frapperManuel(epee);

        assertEquals(ResultatCombat.VICTOIRE, a.getResultat());
        assertTrue(a.isTerminee());
    }

    @Test
    void defaiteSiJoueurMeurt() {
        // Alien très puissant : 999 dégâts, cooldown 100ms
        Extraterrestre alien = new Extraterrestre("Tueur", 9999, 999, 100, 10000L);
        Attaque a = new Attaque(alien);

        // Joueur frappe d'abord, mais l'alien riposte et tue
        a.mettreAJour(100, joueur);
        a.frapperManuel(epee);

        assertEquals(ResultatCombat.DEFAITE, a.getResultat());
        assertFalse(joueur.isVivant());
    }

    // ===== Vague multi-aliens =====

    @Test
    void vagueDeDeuxAliens() {
        Extraterrestre a1 = new Extraterrestre("A1", 10, 3, 5000, 10000L); // faible, lent
        Extraterrestre a2 = new Extraterrestre("A2", 10, 3, 5000, 10000L);
        Attaque att = new Attaque(List.of(a1, a2));

        assertEquals(2, att.getNombreAliensRestants());

        // Premier coup tue A1
        att.mettreAJour(epee.getCooldownMs(), joueur);
        att.frapperManuel(epee);
        assertFalse(a1.isVivant());
        assertEquals(ResultatCombat.EN_COURS, att.getResultat());
        assertEquals(1, att.getNombreAliensRestants());

        // Second coup tue A2
        att.mettreAJour(epee.getCooldownMs(), joueur);
        att.frapperManuel(epee);
        assertEquals(ResultatCombat.VICTOIRE, att.getResultat());
        assertEquals(0, att.getNombreAliensRestants());
    }

    // ===== Statistiques =====

    @Test
    void statistiquesDegatsSontTrackes() {
        Extraterrestre alien = new Extraterrestre("Stats", 50, 5, 800, 10000L);
        Attaque a = new Attaque(alien);

        a.mettreAJour(epee.getCooldownMs(), joueur);
        a.frapperManuel(epee);

        assertTrue(a.getTotalDegatsInfliges() > 0);
        // L'alien a aussi eu le temps de frapper (son cd 800 < epee cd 1000)
        assertTrue(a.getTotalDegatsRecus() >= 0);
    }

    // ===== Combat ne progresse plus après fin =====

    @Test
    void mettreAJourApresVictoireNeChangePlusRien() {
        Extraterrestre alien = new Extraterrestre("Faible", 1, 5, 2000, 10000L);
        Attaque a = new Attaque(alien);
        a.mettreAJour(epee.getCooldownMs(), joueur);
        a.frapperManuel(epee);
        assertEquals(ResultatCombat.VICTOIRE, a.getResultat());

        int pvAvant = joueur.getPointsDeVie();
        a.mettreAJour(10000, joueur);
        a.frapperManuel(epee);
        assertEquals(pvAvant, joueur.getPointsDeVie()); // rien n'a changé
    }

    // ===== Boss dans une attaque =====

    @Test
    void bossCommeAlienDansAttaque() {
        BossFinal boss = BossFinal.pourNiveau(1);
        Attaque a = new Attaque(boss);
        assertEquals(1, a.getNombreAliensRestants());
        assertSame(boss, a.getAlienCourant());
    }

    // ===== Combat longue durée (simulation) =====

    @Test
    void combatSimulationFinitToujours() {
        // Alien résistant : 200 PV, 8 dégâts, cooldown 1s
        Extraterrestre alien = new Extraterrestre("Tank", 200, 8, 1000, 10000L);
        Attaque a = new Attaque(alien);

        // Simuler 60 secondes de combat (tick de 100ms)
        for (int i = 0; i < 600 && !a.isTerminee(); i++) {
            a.mettreAJour(100, joueur);
            a.frapperManuel(epee);
        }

        assertTrue(a.isTerminee(), "Le combat doit finir en 60 secondes");
    }

    @Test
    void alienTueParEffetExterneTermineLeCombat() {
        BossFinal boss = BossFinal.pourNiveau(2);
        Attaque a = new Attaque(boss);

        // Simule une bombe qui tue le boss hors frapperManuel.
        boss.subirDegats(10_000);
        assertFalse(boss.isVivant());

        // Le tick suivant doit constater la mort et finaliser l'attaque.
        a.mettreAJour(16, joueur);

        assertEquals(ResultatCombat.VICTOIRE, a.getResultat());
        assertTrue(a.isTerminee());
    }
}
