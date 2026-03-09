package com.fermedefense.controleur;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fermedefense.modele.combat.Arme;
import com.fermedefense.modele.ferme.Ferme;
import com.fermedefense.modele.jeu.Carte;
import com.fermedefense.modele.joueur.Joueur;
import com.fermedefense.modele.marche.Marche;

class ControleurMarcheTest {

    private Joueur joueur;
    private Ferme ferme;
    private Carte carte;
    private Marche marche;
    private ControleurJeu controleurJeu;
    private ControleurMarche controleurMarche;

    @BeforeEach
    void setUp() {
        joueur = new Joueur(100, 100, 180, 100, 200);
        ferme = new Ferme(10);
        carte = new Carte(800, 600);
        marche = new Marche();
        // ControleurJeu needs a JPanel; use a simple dummy panel
        javax.swing.JPanel panel = new javax.swing.JPanel();
        controleurJeu = new ControleurJeu(joueur, ferme, carte, panel);
        controleurMarche = new ControleurMarche(joueur, ferme, marche, carte, controleurJeu);
    }

    // ─── Achat vache ───

    @Test
    void acheterVacheReussi() {
        ControleurMarche.ResultatAchat r = controleurMarche.acheter(0); // Vache laitière @50
        assertEquals(ControleurMarche.ResultatAchat.OK, r);
        assertEquals(150, joueur.getMonnaie());
        assertEquals(1, ferme.getNombreAnimaux());
    }

    @Test
    void acheterVacheMessage() {
        controleurMarche.acheter(0);
        assertNotNull(controleurMarche.getDernierMessage());
        assertTrue(controleurMarche.getDernierMessage().contains("Vache"));
    }

    // ─── Achat arme ───

    @Test
    void acheterArmeEquipeRayonLaser() {
        ControleurMarche.ResultatAchat r = controleurMarche.acheter(1); // Rayon laser @120
        assertEquals(ControleurMarche.ResultatAchat.OK, r);
        assertEquals(80, joueur.getMonnaie());
        // L'arme doit être mise à jour dans le contrôleur de jeu
        assertEquals(ControleurMarche.RAYON_LASER, controleurJeu.getArme());
    }

    @Test
    void acheterArmeMessage() {
        controleurMarche.acheter(1);
        assertTrue(controleurMarche.getDernierMessage().contains("Rayon laser"));
    }

    // ─── Fonds insuffisants ───

    @Test
    void acheterSansFonds() {
        Joueur pauvre = new Joueur(100, 100, 180, 100, 10);
        javax.swing.JPanel panel = new javax.swing.JPanel();
        ControleurJeu cj = new ControleurJeu(pauvre, ferme, carte, panel);
        ControleurMarche cm = new ControleurMarche(pauvre, ferme, marche, carte, cj);

        ControleurMarche.ResultatAchat r = cm.acheter(0); // Vache @50
        assertEquals(ControleurMarche.ResultatAchat.FONDS_INSUFFISANTS, r);
        assertEquals(10, pauvre.getMonnaie());
    }

    // ─── Aucune sélection ───

    @Test
    void acheterSansSelection() {
        ControleurMarche.ResultatAchat r = controleurMarche.acheter(-1);
        assertEquals(ControleurMarche.ResultatAchat.AUCUNE_SELECTION, r);
    }

    @Test
    void acheterIndexHorsLimites() {
        ControleurMarche.ResultatAchat r = controleurMarche.acheter(99);
        assertEquals(ControleurMarche.ResultatAchat.AUCUNE_SELECTION, r);
    }

    // ─── Ferme pleine ───

    @Test
    void acheterVacheFermePleine() {
        // Remplir la ferme (capacité 10)
        for (int i = 0; i < 10; i++) {
            ferme.ajouterVache(new com.fermedefense.modele.ferme.Vache("v" + i, 50, 50));
        }
        // Joueur a assez d'argent
        ControleurMarche.ResultatAchat r = controleurMarche.acheter(0);
        assertEquals(ControleurMarche.ResultatAchat.FERME_PLEINE, r);
        assertEquals(200, joueur.getMonnaie()); // pas dépensé
    }

    // ─── Arme par défaut est l'épée ───

    @Test
    void armeParDefautEstEpee() {
        assertEquals(Arme.EPEE, controleurJeu.getArme());
    }

    // ─── Achats multiples ───

    @Test
    void acheterPlusieursVachesPuisArme() {
        controleurMarche.acheter(0); // -50 → 150
        controleurMarche.acheter(0); // -50 → 100
        controleurMarche.acheter(1); // -120 → pas assez

        assertEquals(ControleurMarche.ResultatAchat.FONDS_INSUFFISANTS,
                controleurMarche.acheter(1));
        assertEquals(2, ferme.getNombreAnimaux());
    }
}
