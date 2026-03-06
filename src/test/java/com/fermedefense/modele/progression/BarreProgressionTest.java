package com.fermedefense.modele.progression;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BarreProgressionTest {

    private Niveau niveau;
    private BarreProgression barre;

    @BeforeEach
    void setUp() {
        niveau = new Niveau(1); // 120s, 2 vagues à 40s et 80s
        barre = new BarreProgression(niveau);
    }

    @Test
    void etatInitial() {
        assertEquals(0.0, barre.getProgression(), 0.001);
        assertEquals(0, barre.getTempsEcoule());
        assertEquals(120_000L, barre.getDureeMs());
        assertFalse(barre.isTerminee());
    }

    @Test
    void progressionAuMilieu() {
        barre.mettreAJour(60_000);
        assertEquals(0.5, barre.getProgression(), 0.001);
        assertEquals(60_000, barre.getTempsRestant());
    }

    @Test
    void evenementsContientVaguesEtBoss() {
        List<EvenementTemporel> evts = barre.getEvenements();
        // 2 vagues + 1 combat final = 3
        assertEquals(3, evts.size());
        assertEquals(EvenementTemporel.TypeEvenement.ATTAQUE_INTERMEDIAIRE, evts.get(0).getType());
        assertEquals(EvenementTemporel.TypeEvenement.ATTAQUE_INTERMEDIAIRE, evts.get(1).getType());
        assertEquals(EvenementTemporel.TypeEvenement.COMBAT_FINAL, evts.get(2).getType());
    }

    @Test
    void premierEvenementDeclencheA40s() {
        // Avancer jusque 39s → rien
        List<EvenementTemporel> r1 = barre.mettreAJour(39_000);
        assertTrue(r1.isEmpty());

        // Avancer de 1s supplémentaire → 40s → vague 1
        List<EvenementTemporel> r2 = barre.mettreAJour(1_000);
        assertEquals(1, r2.size());
        assertEquals(EvenementTemporel.TypeEvenement.ATTAQUE_INTERMEDIAIRE, r2.get(0).getType());
        assertEquals(0, r2.get(0).getIndexVague());
    }

    @Test
    void deuxiemeEvenementDeclencheA80s() {
        barre.mettreAJour(40_000); // déclenche vague 0
        barre.mettreAJour(39_000); // 79s, rien

        List<EvenementTemporel> r = barre.mettreAJour(1_000); // 80s
        assertEquals(1, r.size());
        assertEquals(1, r.get(0).getIndexVague());
    }

    @Test
    void combatFinalDeclencheA120s() {
        barre.mettreAJour(40_000);  // vague 0
        barre.mettreAJour(40_000);  // vague 1
        List<EvenementTemporel> r = barre.mettreAJour(40_000); // 120s
        assertEquals(1, r.size());
        assertEquals(EvenementTemporel.TypeEvenement.COMBAT_FINAL, r.get(0).getType());
        assertTrue(barre.isTerminee());
    }

    @Test
    void evenementNonDeclencheTwoFois() {
        barre.mettreAJour(40_000); // déclenche vague 0
        // Avancer encore → ne doit pas re-déclencher
        List<EvenementTemporel> r = barre.mettreAJour(1);
        assertTrue(r.isEmpty());
    }

    @Test
    void granDeltaDeclenchtPlusieurs() {
        // Un seul delta de 80s doit déclencher vague 0 et vague 1
        List<EvenementTemporel> r = barre.mettreAJour(80_000);
        assertEquals(2, r.size());
    }

    @Test
    void tempsPlafonne() {
        barre.mettreAJour(200_000);
        assertEquals(120_000, barre.getTempsEcoule());
        assertEquals(1.0, barre.getProgression(), 0.001);
        assertTrue(barre.isTerminee());
    }

    @Test
    void reinitialiserRemettreAZero() {
        barre.mettreAJour(120_000);
        assertTrue(barre.isTerminee());

        barre.reinitialiser();
        assertEquals(0, barre.getTempsEcoule());
        assertFalse(barre.isTerminee());
        // Événements sont aussi réinitialisés
        for (EvenementTemporel evt : barre.getEvenements()) {
            assertFalse(evt.isDeclenche());
        }
    }

    @Test
    void apresTermineePasNouveauxEvenements() {
        barre.mettreAJour(120_000);
        assertTrue(barre.isTerminee());

        List<EvenementTemporel> r = barre.mettreAJour(10_000);
        assertTrue(r.isEmpty());
    }

    @Test
    void evenementsListeNonModifiable() {
        assertThrows(UnsupportedOperationException.class,
                () -> barre.getEvenements().add(null));
    }
}
