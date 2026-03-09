package com.fermedefense.modele.jeu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fermedefense.modele.joueur.Joueur;

class CarteTest {

    private Carte carte;

    @BeforeEach
    void setUp() {
        carte = new Carte(800, 600);
    }

    // ─── Dimensions ───

    @Test
    void dimensions() {
        assertEquals(800, carte.getLargeur());
        assertEquals(600, carte.getHauteur());
    }

    // ─── Zones ───

    @Test
    void zoneFermeOccupeMoitieGauche() {
        int[] zf = carte.getZoneFerme();
        assertEquals(0, zf[0]);
        assertEquals(0, zf[1]);
        assertEquals(400, zf[2]);
        assertEquals(600, zf[3]);
    }

    @Test
    void zoneMarcheOccupeMoitieDroite() {
        int[] zm = carte.getZoneMarche();
        assertEquals(400, zm[0]);
        assertEquals(0, zm[1]);
        assertEquals(400, zm[2]);
        assertEquals(600, zm[3]);
    }

    // ─── getZoneA ───

    @Test
    void pointDansFerme() {
        assertEquals(Zone.FERME, carte.getZoneA(100, 300));
    }

    @Test
    void pointDansMarche() {
        assertEquals(Zone.MARCHE, carte.getZoneA(500, 300));
    }

    @Test
    void pointSurLimiteEstMarche() {
        assertEquals(Zone.MARCHE, carte.getZoneA(400, 300));
    }

    @Test
    void pointOrigineEstFerme() {
        assertEquals(Zone.FERME, carte.getZoneA(0, 0));
    }

    @Test
    void pointHorsCarteXNegatif() {
        assertNull(carte.getZoneA(-1, 300));
    }

    @Test
    void pointHorsCarteYNegatif() {
        assertNull(carte.getZoneA(100, -1));
    }

    @Test
    void pointHorsCarteTropADroite() {
        assertNull(carte.getZoneA(800, 300));
    }

    @Test
    void pointHorsCarteTropBas() {
        assertNull(carte.getZoneA(100, 600));
    }

    // ─── getCentreZone ───

    @Test
    void centreFerme() {
        int[] c = carte.getCentreZone(Zone.FERME);
        assertEquals(200, c[0]);
        assertEquals(300, c[1]);
    }

    @Test
    void centreMarche() {
        int[] c = carte.getCentreZone(Zone.MARCHE);
        assertEquals(600, c[0]);
        assertEquals(300, c[1]);
    }

    // ─── clampJoueur ───

    @Test
    void clampJoueurDansLimites() {
        Joueur j = new Joueur(100, 200, 100, 50, 0);
        carte.clampJoueur(j);
        assertEquals(100, j.getX(), 0.01);
        assertEquals(200, j.getY(), 0.01);
    }

    @Test
    void clampJoueurNegatifX() {
        Joueur j = new Joueur(-50, 200, 100, 50, 0);
        carte.clampJoueur(j);
        assertEquals(0, j.getX(), 0.01);
    }

    @Test
    void clampJoueurNegatifY() {
        Joueur j = new Joueur(100, -30, 100, 50, 0);
        carte.clampJoueur(j);
        assertEquals(0, j.getY(), 0.01);
    }

    @Test
    void clampJoueurDepasseXMax() {
        Joueur j = new Joueur(900, 200, 100, 50, 0);
        carte.clampJoueur(j);
        assertEquals(800 - 30, j.getX(), 0.01); // largeur - taille
    }

    @Test
    void clampJoueurDepasseYMax() {
        Joueur j = new Joueur(100, 700, 100, 50, 0);
        carte.clampJoueur(j);
        assertEquals(600 - 30, j.getY(), 0.01);
    }
}
