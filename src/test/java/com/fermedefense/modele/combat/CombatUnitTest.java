package com.fermedefense.modele.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour Arme et Extraterrestre.
 */
class CombatUnitTest {

    // ===== Arme =====

    @Test
    void epeeParDefautADesStatsCoherentes() {
        Arme epee = Arme.EPEE;
        assertEquals("Épée", epee.getNom());
        assertTrue(epee.getDegats() > 0);
        assertTrue(epee.getCooldownMs() > 0);
    }

    @Test
    void dpsCalculCorrect() {
        Arme a = new Arme("Test", 20, 500);
        assertEquals(40.0, a.getDps(), 0.01);
    }

    // ===== Extraterrestre =====

    @Test
    void alienPVInitiaux() {
        Extraterrestre e = new Extraterrestre("Alien", 50, 10, 800);
        assertEquals(50, e.getPointsDeVie());
        assertEquals(50, e.getPointsDeVieMax());
        assertTrue(e.isVivant());
    }

    @Test
    void alienSubirDegats() {
        Extraterrestre e = new Extraterrestre("Alien", 50, 10, 800);
        e.subirDegats(20);
        assertEquals(30, e.getPointsDeVie());
        assertTrue(e.isVivant());
    }

    @Test
    void alienMeurtQuandPVZero() {
        Extraterrestre e = new Extraterrestre("Alien", 30, 10, 800);
        e.subirDegats(30);
        assertFalse(e.isVivant());
        assertEquals(0, e.getPointsDeVie());
    }

    @Test
    void alienPVNePasNegatif() {
        Extraterrestre e = new Extraterrestre("Alien", 20, 10, 800);
        e.subirDegats(999);
        assertEquals(0, e.getPointsDeVie());
    }

    @Test
    void alienDegatsNegatifsIgnores() {
        Extraterrestre e = new Extraterrestre("Alien", 50, 10, 800);
        e.subirDegats(-10);
        assertEquals(50, e.getPointsDeVie());
    }

    @Test
    void alienReinitialiser() {
        Extraterrestre e = new Extraterrestre("Alien", 50, 10, 800);
        e.subirDegats(30);
        e.reinitialiser();
        assertEquals(50, e.getPointsDeVie());
        assertTrue(e.isVivant());
    }

    @Test
    void alienRatioPv() {
        Extraterrestre e = new Extraterrestre("Alien", 100, 10, 800);
        e.subirDegats(25);
        assertEquals(0.75, e.getRatioPv(), 0.01);
    }

    // ===== BossFinal =====

    @Test
    void bossPourNiveauStatsAugmentent() {
        BossFinal b1 = BossFinal.pourNiveau(1);
        BossFinal b3 = BossFinal.pourNiveau(3);
        assertTrue(b3.getPointsDeVieMax() > b1.getPointsDeVieMax());
        assertTrue(b3.getDegats() > b1.getDegats());
        assertTrue(b3.getRecompense() > b1.getRecompense());
    }

    @Test
    void bossEstUnExtraterrestre() {
        BossFinal b = BossFinal.pourNiveau(1);
        assertInstanceOf(Extraterrestre.class, b);
        assertTrue(b.isVivant());
    }
}
