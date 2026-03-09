package com.fermedefense.modele.joueur;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.fermedefense.modele.joueur.ActionDuree.TypeAction;

class ActionDureeTest {

    @Test
    void creationInitialeNonTerminee() {
        ActionDuree a = new ActionDuree(TypeAction.RECOLTE, 2000);
        assertFalse(a.isTerminee());
        assertEquals(0.0, a.getProgression(), 0.001);
        assertEquals(TypeAction.RECOLTE, a.getType());
        assertEquals(2000, a.getDureeMs());
    }

    @Test
    void progressionMiChemin() {
        ActionDuree a = new ActionDuree(TypeAction.RECOLTE, 2000);
        assertFalse(a.mettreAJour(1000));
        assertEquals(0.5, a.getProgression(), 0.001);
        assertFalse(a.isTerminee());
    }

    @Test
    void termineeQuandDureeAtteinte() {
        ActionDuree a = new ActionDuree(TypeAction.ACHAT, 1500);
        assertFalse(a.mettreAJour(1000));
        assertTrue(a.mettreAJour(500));
        assertTrue(a.isTerminee());
        assertEquals(1.0, a.getProgression(), 0.001);
    }

    @Test
    void termineeQuandDureeDepassee() {
        ActionDuree a = new ActionDuree(TypeAction.RECOLTE, 1000);
        assertTrue(a.mettreAJour(5000));
        assertTrue(a.isTerminee());
    }

    @Test
    void mettreAJourApresTermineeRetourneFalse() {
        ActionDuree a = new ActionDuree(TypeAction.RECOLTE, 100);
        assertTrue(a.mettreAJour(200));
        assertFalse(a.mettreAJour(100));
    }

    @Test
    void progressionNeDépassePasUn() {
        ActionDuree a = new ActionDuree(TypeAction.ACHAT, 500);
        a.mettreAJour(1000);
        assertTrue(a.getProgression() <= 1.0);
    }

    @Test
    void labelRecolte() {
        ActionDuree a = new ActionDuree(TypeAction.RECOLTE, 1000);
        assertEquals("Récolte...", a.getLabel());
    }

    @Test
    void labelAchat() {
        ActionDuree a = new ActionDuree(TypeAction.ACHAT, 1000);
        assertEquals("Achat...", a.getLabel());
    }

    @Test
    void progressionIncrementale() {
        ActionDuree a = new ActionDuree(TypeAction.RECOLTE, 4000);
        a.mettreAJour(1000);
        assertEquals(0.25, a.getProgression(), 0.001);
        a.mettreAJour(1000);
        assertEquals(0.50, a.getProgression(), 0.001);
        a.mettreAJour(1000);
        assertEquals(0.75, a.getProgression(), 0.001);
        a.mettreAJour(1000);
        assertTrue(a.isTerminee());
    }

    @Test
    void premierTickZeroDelta() {
        ActionDuree a = new ActionDuree(TypeAction.RECOLTE, 1000);
        assertFalse(a.mettreAJour(0));
        assertEquals(0.0, a.getProgression(), 0.001);
        assertFalse(a.isTerminee());
    }
}
