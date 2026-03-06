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
        assertEquals(5, niv.getAlienDegats());
    }

    @Test
    void niveauTroisParametresCroissants() {
        Niveau niv = new Niveau(3);
        assertEquals(3, niv.getNumero());
        assertEquals(150_000L, niv.getDureeMs()); // 120k + 2*15k
        assertEquals(4, niv.getNombreVagues());   // 1 + 3
        assertEquals(2, niv.getAliensParVague()); // 1 + 3/2 = 2
        assertEquals(40, niv.getAlienPv());       // 20 + 2*10
        assertEquals(9, niv.getAlienDegats());    // 5 + 2*2
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
        List<Extraterrestre> vague = niv.creerVague(0);
        assertEquals(2, vague.size()); // aliensParVague = 2
        for (Extraterrestre alien : vague) {
            assertEquals(40, alien.getPointsDeVieMax());
            assertEquals(9, alien.getDegats());
            assertTrue(alien.isVivant());
        }
    }

    @Test
    void creerVagueDifferentsIndex() {
        Niveau niv = new Niveau(2);
        List<Extraterrestre> v0 = niv.creerVague(0);
        List<Extraterrestre> v1 = niv.creerVague(1);
        // Mêmes stats mais noms différents
        assertNotEquals(v0.get(0).getNom(), v1.get(0).getNom());
        assertEquals(v0.get(0).getPointsDeVieMax(), v1.get(0).getPointsDeVieMax());
    }

    @Test
    void creerBossRetourneBossDuNiveau() {
        Niveau niv = new Niveau(2);
        BossFinal boss = niv.creerBoss();
        assertNotNull(boss);
        assertEquals(160, boss.getPointsDeVieMax()); // 80 + 2*40
        assertEquals(16, boss.getDegats());           // 8 + 2*4
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
