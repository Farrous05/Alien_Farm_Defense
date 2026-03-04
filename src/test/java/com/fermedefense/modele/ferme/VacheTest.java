package com.fermedefense.modele.ferme;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe Vache.
 */
class VacheTest {

    // ===== Croissance =====

    @Test
    void vacheCommenceEnEtatBebe() {
        Vache v = new Vache("Marguerite", 0, 0);
        assertEquals(EtatVache.BEBE, v.getEtat());
        assertFalse(v.isProductif());
    }

    @Test
    void vachePasseAdulteApresTempsRequis() {
        Vache v = new Vache("Marguerite", 0, 0);
        v.mettreAJour(Vache.TEMPS_BEBE_MS);
        assertEquals(EtatVache.ADULTE, v.getEtat());
        assertFalse(v.isProductif());
    }

    @Test
    void vacheResteBebeAvantTempsRequis() {
        Vache v = new Vache("Marguerite", 0, 0);
        v.mettreAJour(Vache.TEMPS_BEBE_MS - 1);
        assertEquals(EtatVache.BEBE, v.getEtat());
    }

    @Test
    void vachePasseProductiveApresDeuxPhases() {
        Vache v = new Vache("Marguerite", 0, 0);
        v.mettreAJour(Vache.TEMPS_BEBE_MS);
        v.mettreAJour(Vache.TEMPS_ADULTE_MS);
        assertEquals(EtatVache.PRODUCTIVE, v.getEtat());
        assertTrue(v.isProductif());
    }

    @Test
    void croissanceEnUnSeulTickGeant() {
        // Un seul tick couvrant bébé + adulte + un cycle de production
        Vache v = new Vache("Marguerite", 0, 0);
        long totalMs = Vache.TEMPS_BEBE_MS + Vache.TEMPS_ADULTE_MS + Vache.CYCLE_PROD_MS;
        v.mettreAJour(Vache.TEMPS_BEBE_MS);  // -> ADULTE
        v.mettreAJour(Vache.TEMPS_ADULTE_MS); // -> PRODUCTIVE
        v.mettreAJour(Vache.CYCLE_PROD_MS);   // 1 cycle
        assertEquals(EtatVache.PRODUCTIVE, v.getEtat());
        assertEquals(Vache.REVENU_PAR_CYCLE, v.getMonnaieAccumulee());
    }

    @Test
    void surplusTempsReporteCorrectement() {
        // Bébé avec surplus -> le surplus est reporté en phase adulte
        Vache v = new Vache("Test", 0, 0, 100, 200, 300, 5);
        v.mettreAJour(150); // 100 bébé + 50 surplus
        assertEquals(EtatVache.ADULTE, v.getEtat());
        assertEquals(50, v.getTempsEcoule());
    }

    // ===== Production =====

    @Test
    void productionAccumuleMonnaie() {
        Vache v = creerVacheProductive();
        v.mettreAJour(Vache.CYCLE_PROD_MS * 3);
        assertEquals(Vache.REVENU_PAR_CYCLE * 3, v.getMonnaieAccumulee());
    }

    @Test
    void recolterRetourneMonnaieEtRemet() {
        Vache v = creerVacheProductive();
        v.mettreAJour(Vache.CYCLE_PROD_MS * 2);
        int recolte = v.recolter();
        assertEquals(Vache.REVENU_PAR_CYCLE * 2, recolte);
        assertEquals(0, v.getMonnaieAccumulee());
    }

    @Test
    void recolterSansProductionRetourneZero() {
        Vache v = creerVacheProductive();
        assertEquals(0, v.recolter());
    }

    @Test
    void pasDeProductionAvantEtatProductif() {
        Vache v = new Vache("Marguerite", 0, 0);
        v.mettreAJour(5000); // still BEBE
        assertEquals(0, v.getMonnaieAccumulee());
        assertEquals(0, v.recolter());
    }

    // ===== Progression =====

    @Test
    void progressionBebeMidway() {
        Vache v = new Vache("Marguerite", 0, 0);
        v.mettreAJour(Vache.TEMPS_BEBE_MS / 2);
        assertEquals(0.5, v.getProgression(), 0.01);
    }

    // ===== Paramètres personnalisés =====

    @Test
    void constructeurPersonnalise() {
        Vache v = new Vache("Custom", 10, 20, 50, 100, 200, 25);
        assertEquals("Custom", v.getNom());
        assertEquals(10.0, v.getX());
        assertEquals(20.0, v.getY());

        v.mettreAJour(50);  // -> ADULTE
        assertEquals(EtatVache.ADULTE, v.getEtat());
        v.mettreAJour(100); // -> PRODUCTIVE
        assertEquals(EtatVache.PRODUCTIVE, v.getEtat());
        v.mettreAJour(200); // 1 cycle
        assertEquals(25, v.recolter());
    }

    // ===== Utilitaire =====

    /** Crée une vache déjà en état PRODUCTIVE. */
    private Vache creerVacheProductive() {
        Vache v = new Vache("Prod", 0, 0);
        v.mettreAJour(Vache.TEMPS_BEBE_MS);
        v.mettreAJour(Vache.TEMPS_ADULTE_MS);
        assertEquals(EtatVache.PRODUCTIVE, v.getEtat(), "Précondition : doit être PRODUCTIVE");
        return v;
    }
}
