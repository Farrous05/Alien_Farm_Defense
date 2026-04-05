package com.fermedefense.modele.marche;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fermedefense.controleur.ControleurMarche;
import com.fermedefense.modele.ferme.Ferme;
import com.fermedefense.modele.jeu.Carte;
import com.fermedefense.modele.joueur.Joueur;

class MarcheTest {

    private Marche marche;
    private Joueur joueur;
    private Ferme ferme;
    private ControleurMarche controleur;

    @BeforeEach
    void setUp() {
        marche = new Marche();
        joueur = new Joueur(0, 0, 100, 100, 500);
        ferme = new Ferme();
        controleur = new ControleurMarche(joueur, ferme, marche, new Carte(800, 600), null);
    }

    // ─── Articles du marché ───

    @Test
    void marcheContientCinqArticles() {
        assertEquals(5, marche.getArticles().size());
    }

    @Test
    void premierArticleEstVache() {
        ArticleMarche vache = marche.getArticles().get(0);
        assertEquals("Vache", vache.getNom());
        assertEquals(TypeArticle.VACHE, vache.getType());
        assertEquals(50, vache.getPrix());
    }

    @Test
    void deuxiemeArticleEstRayonLaser() {
        ArticleMarche laser = marche.getArticles().get(1);
        assertEquals("Rayon laser", laser.getNom());
        assertEquals(TypeArticle.ARME, laser.getType());
        assertEquals(120, laser.getPrix());
    }

    // ─── Achat réussi ───

    @Test
    void acheterVacheReussi() {
        ControleurMarche.ResultatAchat r = controleur.acheter(0);
        assertEquals(ControleurMarche.ResultatAchat.OK, r);
        assertEquals(450, joueur.getMonnaie());
    }

    @Test
    void acheterRayonLaserReussi() {
        ControleurMarche.ResultatAchat r = controleur.acheter(1);
        assertEquals(ControleurMarche.ResultatAchat.OK, r);
        assertEquals(380, joueur.getMonnaie());
    }

    // ─── Fonds insuffisants ───

    @Test
    void acheterSansFonds() {
        Joueur pauvre = new Joueur(0, 0, 100, 100, 10);
        ControleurMarche ctrlPauvre = new ControleurMarche(pauvre, ferme, marche, new Carte(800, 600), null);
        ControleurMarche.ResultatAchat r = ctrlPauvre.acheter(0);
        assertEquals(ControleurMarche.ResultatAchat.FONDS_INSUFFISANTS, r);
    }

    // ─── Aucune sélection ───

    @Test
    void acheterSansSelection() {
        ControleurMarche.ResultatAchat r = controleur.acheter(-1);
        assertEquals(ControleurMarche.ResultatAchat.AUCUNE_SELECTION, r);
    }

    // ─── Message dernier achat ───

    @Test
    void dernierMessageNonNull() {
        controleur.acheter(0);
        assertNotNull(controleur.getDernierMessage());
    }

    // ─── Achats multiples ───

    @Test
    void acheterPlusieursArticles() {
        controleur.acheter(0); // Vache  50 -> 450
        controleur.acheter(0); // Vache  50 -> 400
        controleur.acheter(1); // Rayon laser 120 -> 280
        assertEquals(280, joueur.getMonnaie());
    }
}
