package com.fermedefense.modele.jeu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PartieTest {

    private Partie partie;

    @BeforeEach
    void setUp() {
        // 800x600 carte, 120s niveau, 100 monnaie de départ
        partie = new Partie(800, 600, 120_000, 100);
    }

    // ─── État initial ───

    @Test
    void etatInitialEstMenu() {
        assertEquals(EtatJeu.MENU, partie.getEtat());
    }

    @Test
    void niveauInitialEst1() {
        assertEquals(1, partie.getNiveau());
    }

    @Test
    void monnaieInitiale() {
        assertEquals(100, partie.getMonnaie());
    }

    @Test
    void tempsEcouleInitialZero() {
        assertEquals(0, partie.getTempsEcoule());
    }

    @Test
    void pasPauseInitialement() {
        assertFalse(partie.isEnPause());
    }

    // ─── Démarrage ───

    @Test
    void demarrerPasseEnCours() {
        partie.demarrer();
        assertEquals(EtatJeu.EN_COURS, partie.getEtat());
    }

    @Test
    void demarrerResetTemps() {
        partie.demarrer();
        partie.mettreAJour(5000);
        partie.demarrer(); // relance
        assertEquals(0, partie.getTempsEcoule());
    }

    // ─── Mise à jour du temps ───

    @Test
    void mettreAJourAvanceTemps() {
        partie.demarrer();
        partie.mettreAJour(3000);
        assertEquals(3000, partie.getTempsEcoule());
    }

    @Test
    void mettreAJourEnMenuNeFaitRien() {
        partie.mettreAJour(5000);
        assertEquals(0, partie.getTempsEcoule());
    }

    @Test
    void mettreAJourEnPauseNeFaitRien() {
        partie.demarrer();
        partie.basculerPause();
        partie.mettreAJour(5000);
        assertEquals(0, partie.getTempsEcoule());
    }

    @Test
    void tempsRestant() {
        partie.demarrer();
        partie.mettreAJour(20_000);
        assertEquals(100_000, partie.getTempsRestant());
    }

    @Test
    void progressionTemps() {
        partie.demarrer();
        partie.mettreAJour(60_000);
        assertEquals(0.5, partie.getProgressionTemps(), 0.01);
    }

    // ─── Combat final au temps écoulé ───

    @Test
    void combatFinalQuandTempsEcoule() {
        partie.demarrer();
        partie.mettreAJour(120_000);
        assertEquals(EtatJeu.COMBAT_FINAL, partie.getEtat());
    }

    @Test
    void tempsCappeAuTotal() {
        partie.demarrer();
        partie.mettreAJour(200_000);
        assertEquals(120_000, partie.getTempsEcoule());
    }

    // ─── Terminer ───

    @Test
    void terminerVictoire() {
        partie.demarrer();
        partie.terminer(true);
        assertEquals(EtatJeu.VICTOIRE, partie.getEtat());
    }

    @Test
    void terminerDefaite() {
        partie.demarrer();
        partie.terminer(false);
        assertEquals(EtatJeu.DEFAITE, partie.getEtat());
    }

    // ─── Niveau suivant ───

    @Test
    void niveauSuivantIncremente() {
        partie.niveauSuivant(135_000);
        assertEquals(2, partie.getNiveau());
    }

    @Test
    void niveauSuivantResetTemps() {
        partie.demarrer();
        partie.mettreAJour(50_000);
        partie.niveauSuivant(135_000);
        assertEquals(0, partie.getTempsEcoule());
        assertEquals(135_000, partie.getTempsTotal());
    }

    @Test
    void niveauSuivantVideAttaques() {
        partie.ajouterMomentAttaque(30_000);
        partie.niveauSuivant(135_000);
        assertTrue(partie.getMomentsAttaques().isEmpty());
    }

    // ─── Moments d'attaque ───

    @Test
    void ajouterMomentAttaque() {
        partie.ajouterMomentAttaque(40_000);
        partie.ajouterMomentAttaque(80_000);
        assertEquals(2, partie.getMomentsAttaques().size());
    }

    @Test
    void doitDeclencherAttaqueDansTolerances() {
        partie.ajouterMomentAttaque(30_000);
        partie.demarrer();
        partie.mettreAJour(30_500);
        assertTrue(partie.doitDeclencherAttaque(1000));
    }

    @Test
    void doitDeclencherAttaqueHorsTolerances() {
        partie.ajouterMomentAttaque(30_000);
        partie.demarrer();
        partie.mettreAJour(35_000);
        assertFalse(partie.doitDeclencherAttaque(1000));
    }

    // ─── Monnaie ───

    @Test
    void ajouterMonnaie() {
        partie.ajouterMonnaie(50);
        assertEquals(150, partie.getMonnaie());
    }

    @Test
    void depenserMonnaie() {
        assertTrue(partie.depenser(40));
        assertEquals(60, partie.getMonnaie());
    }

    @Test
    void depenserTropMonnaie() {
        assertFalse(partie.depenser(200));
        assertEquals(100, partie.getMonnaie());
    }

    // ─── Pause ───

    @Test
    void basculerPause() {
        partie.basculerPause();
        assertTrue(partie.isEnPause());
        partie.basculerPause();
        assertFalse(partie.isEnPause());
    }

    // ─── Carte intégrée ───

    @Test
    void carteNonNull() {
        assertNotNull(partie.getCarte());
        assertEquals(800, partie.getCarte().getLargeur());
    }
}
