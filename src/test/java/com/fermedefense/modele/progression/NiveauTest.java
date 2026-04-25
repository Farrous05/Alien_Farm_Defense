package com.fermedefense.modele.progression;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.fermedefense.modele.combat.BossFinal;
import com.fermedefense.modele.combat.Extraterrestre;

class NiveauTest {

    @Test
    void niveauUnParametresDeBase() {
        Niveau niv = new Niveau(1);
        assertEquals(1, niv.getNumero());
        assertEquals(120_000L, niv.getDureeMs());
        assertEquals(2, niv.getNombreVagues());  // 1 + 1
        assertEquals(1, niv.getAliensParVague()); // 1 + 1/2 = 1
        assertEquals(20, niv.getAlienPv());
        assertEquals(10, niv.getAlienDegats());
    }

    @Test
    void niveauTroisParametresCroissants() {
        Niveau niv = new Niveau(3);
        assertEquals(3, niv.getNumero());
        assertEquals(150_000L, niv.getDureeMs()); // 120k + 2*15k
        assertEquals(4, niv.getNombreVagues());   // 1 + 3
        assertEquals(2, niv.getAliensParVague()); // 1 + 3/2 = 2
        assertEquals(40, niv.getAlienPv());       // 20 + 2*10
        assertEquals(16, niv.getAlienDegats());    // 10 + 2*3
    }

    @Test
    void niveauMaxVaguesPlafonneASix() {
        Niveau niv = new Niveau(10);
        assertEquals(6, niv.getNombreVagues());
    }

    @Test
    void cooldownAlienMinimum600() {
        Niveau niv = new Niveau(20);
        assertEquals(600, niv.getAlienCooldownMs());
    }

    @Test
    void momentsAttaquesRepartisUniformement() {
        Niveau niv = new Niveau(1);
        List<Long> moments = niv.getMomentsAttaques();
        assertEquals(2, moments.size());
        // Durée 120000, 2 vagues → intervalle = 40000
        assertEquals(40_000L, moments.get(0));
        assertEquals(80_000L, moments.get(1));
    }

    @Test
    void creerVagueRetourneBonNombreAliens() {
        Niveau niv = new Niveau(3);
        List<Extraterrestre> vague = niv.creerVagueDynamique(0, 2);
        assertEquals(2, vague.size()); // aliensParVague = 2
        for (Extraterrestre alien : vague) {
            assertEquals(16, alien.getDegats());
            assertTrue(alien.isVivant());
        }
    }

    @Test
    void creerVagueDifferentsIndex() {
        Niveau niv = new Niveau(2);
        List<Extraterrestre> v0 = niv.creerVagueDynamique(0, 2);
        List<Extraterrestre> v1 = niv.creerVagueDynamique(1, 2);
        // Noms différents selon l'index de vague
        assertNotEquals(v0.get(0).getNom(), v1.get(0).getNom());
    }

    @Test
    void creerBossRetourneBossDuNiveau() {
        Niveau niv = new Niveau(2);
        BossFinal boss = niv.creerBoss();
        assertNotNull(boss);
        assertEquals(160, boss.getPointsDeVieMax()); // 80 + 2*40
        assertEquals(13, boss.getDegats());           // 7 + 2*3
    }

    @Test
    void niveauZeroInterdit() {
        assertThrows(IllegalArgumentException.class, () -> new Niveau(0));
    }

    @Test
    void niveauNegatifInterdit() {
        assertThrows(IllegalArgumentException.class, () -> new Niveau(-1));
    }

    @Test
    void toStringContientNumero() {
        Niveau niv = new Niveau(5);
        assertTrue(niv.toString().contains("5"));
    }
}
