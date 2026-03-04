package com.fermedefense.modele.ferme;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe Ferme.
 */
class FermeTest {

    private Ferme ferme;

    @BeforeEach
    void setUp() {
        ferme = new Ferme(3); // capacité max = 3
    }

    // ===== Ajout / Retrait =====

    @Test
    void ajouterVacheReussit() {
        assertTrue(ferme.ajouterVache(new Vache("V1", 0, 0)));
        assertEquals(1, ferme.getNombreAnimaux());
    }

    @Test
    void ajouterVacheRefuseQuandPleine() {
        ferme.ajouterVache(new Vache("V1", 0, 0));
        ferme.ajouterVache(new Vache("V2", 0, 0));
        ferme.ajouterVache(new Vache("V3", 0, 0));
        assertFalse(ferme.ajouterVache(new Vache("V4", 0, 0)));
        assertEquals(3, ferme.getNombreAnimaux());
    }

    @Test
    void ajouterVacheNullRefuse() {
        assertFalse(ferme.ajouterVache(null));
        assertEquals(0, ferme.getNombreAnimaux());
    }

    @Test
    void retirerVacheReussit() {
        Vache v = new Vache("V1", 0, 0);
        ferme.ajouterVache(v);
        assertTrue(ferme.retirerVache(v));
        assertEquals(0, ferme.getNombreAnimaux());
    }

    @Test
    void retirerVacheInexistanteRetourneFalse() {
        assertFalse(ferme.retirerVache(new Vache("Fantome", 0, 0)));
    }

    // ===== Capacité =====

    @Test
    void estPleineRetourneTrueQuandPleine() {
        ferme.ajouterVache(new Vache("V1", 0, 0));
        ferme.ajouterVache(new Vache("V2", 0, 0));
        assertFalse(ferme.estPleine());
        ferme.ajouterVache(new Vache("V3", 0, 0));
        assertTrue(ferme.estPleine());
    }

    @Test
    void capaciteParDefaut() {
        Ferme defaut = new Ferme();
        assertEquals(Ferme.CAPACITE_MAX_DEFAUT, defaut.getCapaciteMax());
    }

    // ===== Mise à jour =====

    @Test
    void mettreAJourPropageAToutesLesVaches() {
        Vache v1 = new Vache("V1", 0, 0);
        Vache v2 = new Vache("V2", 0, 0);
        ferme.ajouterVache(v1);
        ferme.ajouterVache(v2);

        ferme.mettreAJour(Vache.TEMPS_BEBE_MS);
        assertEquals(EtatVache.ADULTE, v1.getEtat());
        assertEquals(EtatVache.ADULTE, v2.getEtat());
    }

    // ===== Récolte =====

    @Test
    void recolterToutSommeDesVachesProductives() {
        Vache v1 = creerVacheProductive("V1");
        Vache v2 = creerVacheProductive("V2");
        Vache v3 = new Vache("V3", 0, 0); // bébé, pas de production
        ferme.ajouterVache(v1);
        ferme.ajouterVache(v2);
        ferme.ajouterVache(v3);

        // Faire produire les vaches productives
        ferme.mettreAJour(Vache.CYCLE_PROD_MS * 2);

        int total = ferme.recolterTout();
        assertEquals(Vache.REVENU_PAR_CYCLE * 2 * 2, total); // 2 vaches × 2 cycles
        assertEquals(0, v1.getMonnaieAccumulee());
        assertEquals(0, v2.getMonnaieAccumulee());
    }

    @Test
    void recolterToutFermeVideRetourneZero() {
        assertEquals(0, ferme.recolterTout());
    }

    // ===== Nombre productives =====

    @Test
    void getNombreProductivesCorrecte() {
        ferme.ajouterVache(creerVacheProductive("V1"));
        ferme.ajouterVache(new Vache("V2", 0, 0)); // bébé
        ferme.ajouterVache(creerVacheProductive("V3"));
        assertEquals(2, ferme.getNombreProductives());
    }

    // ===== Iste non modifiable =====

    @Test
    void getVachesRetourneListeNonModifiable() {
        ferme.ajouterVache(new Vache("V1", 0, 0));
        assertThrows(UnsupportedOperationException.class, () -> {
            ferme.getVaches().add(new Vache("Hack", 0, 0));
        });
    }

    // ===== Utilitaire =====

    private Vache creerVacheProductive(String nom) {
        Vache v = new Vache(nom, 0, 0);
        v.mettreAJour(Vache.TEMPS_BEBE_MS);
        v.mettreAJour(Vache.TEMPS_ADULTE_MS);
        return v;
    }
}
