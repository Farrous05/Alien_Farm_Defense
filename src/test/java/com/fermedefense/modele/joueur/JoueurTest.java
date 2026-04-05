package com.fermedefense.modele.joueur;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JoueurTest {

    private Joueur joueur;

    @BeforeEach
    void setUp() {
        // position (100, 200), vitesse 200 px/s, 100 PV, 50 monnaie
        joueur = new Joueur(100, 200, 200, 100, 50);
    }

    // ─── Position initiale ───

    @Test
    void positionInitiale() {
        assertEquals(100, joueur.getX());
        assertEquals(200, joueur.getY());
    }

    @Test
    void tailleConstante() {
        assertEquals(45, joueur.getTaille());
    }

    // ─── Déplacement ───

    @Test
    void immobileSansDirection() {
        joueur.mettreAJour(1000);
        assertEquals(100, joueur.getX());
        assertEquals(200, joueur.getY());
    }

    @Test
    void deplacementHaut() {
        joueur.appuyerDirection(Action.HAUT);
        joueur.mettreAJour(500); // 200 px/s * 0.5s = 100 px
        assertEquals(200 - 100, joueur.getY(), 0.01);
    }

    @Test
    void deplacementBas() {
        joueur.appuyerDirection(Action.BAS);
        joueur.mettreAJour(500);
        assertEquals(200 + 100, joueur.getY(), 0.01);
    }

    @Test
    void deplacementGauche() {
        joueur.appuyerDirection(Action.GAUCHE);
        joueur.mettreAJour(500);
        assertEquals(100 - 100, joueur.getX(), 0.01);
    }

    @Test
    void deplacementDroite() {
        joueur.appuyerDirection(Action.DROITE);
        joueur.mettreAJour(500);
        assertEquals(100 + 100, joueur.getX(), 0.01);
    }

    @Test
    void relacherDirectionArrete() {
        joueur.appuyerDirection(Action.DROITE);
        joueur.relacherDirection(Action.DROITE);
        assertFalse(joueur.isEnMouvement());
        joueur.mettreAJour(1000);
        assertEquals(100, joueur.getX(), 0.01);
    }

    @Test
    void relacherAutreDirectionNeArretePas() {
        joueur.appuyerDirection(Action.DROITE);
        joueur.relacherDirection(Action.GAUCHE); // wrong key
        assertTrue(joueur.isEnMouvement());
    }

    // ─── Points de vie ───

    @Test
    void pvInitiaux() {
        assertEquals(100, joueur.getPointsDeVie());
        assertEquals(100, joueur.getPointsDeVieMax());
        assertTrue(joueur.isVivant());
    }

    @Test
    void subirDegats() {
        joueur.subirDegats(30);
        assertEquals(70, joueur.getPointsDeVie());
        assertTrue(joueur.isVivant());
    }

    @Test
    void subirDegatsNegatifIgnore() {
        joueur.subirDegats(-5);
        assertEquals(100, joueur.getPointsDeVie());
    }

    @Test
    void subirDegatsZeroIgnore() {
        joueur.subirDegats(0);
        assertEquals(100, joueur.getPointsDeVie());
    }

    @Test
    void pvNeDescendentPasSousZero() {
        joueur.subirDegats(150);
        assertEquals(0, joueur.getPointsDeVie());
        assertFalse(joueur.isVivant());
    }

    @Test
    void soigner() {
        joueur.subirDegats(50);
        joueur.soigner(20);
        assertEquals(70, joueur.getPointsDeVie());
    }

    @Test
    void soignerNeDepassePasMax() {
        joueur.subirDegats(10);
        joueur.soigner(999);
        assertEquals(100, joueur.getPointsDeVie());
    }

    @Test
    void soignerNegatifIgnore() {
        joueur.subirDegats(20);
        joueur.soigner(-10);
        assertEquals(80, joueur.getPointsDeVie());
    }

    // ─── Monnaie ───

    @Test
    void monnaieInitiale() {
        assertEquals(50, joueur.getMonnaie());
    }

    @Test
    void depenser() {
        assertTrue(joueur.depenser(30));
        assertEquals(20, joueur.getMonnaie());
    }

    @Test
    void depenserTropRetourneFalse() {
        assertFalse(joueur.depenser(51));
        assertEquals(50, joueur.getMonnaie());
    }

    @Test
    void depenserMontantNegatifRetourneFalse() {
        assertFalse(joueur.depenser(-1));
        assertEquals(50, joueur.getMonnaie());
    }

    @Test
    void depenserZeroRetourneFalse() {
        assertFalse(joueur.depenser(0));
        assertEquals(50, joueur.getMonnaie());
    }

    @Test
    void ajouterMonnaie() {
        joueur.ajouterMonnaie(25);
        assertEquals(75, joueur.getMonnaie());
    }

    @Test
    void ajouterMonnaieNegatifIgnore() {
        joueur.ajouterMonnaie(-10);
        assertEquals(50, joueur.getMonnaie());
    }

    @Test
    void ajouterMonnaieZeroIgnore() {
        joueur.ajouterMonnaie(0);
        assertEquals(50, joueur.getMonnaie());
    }
}
