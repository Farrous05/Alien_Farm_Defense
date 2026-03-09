package com.fermedefense.modele.marche;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MarcheTest {

    private Marche marche;
    private JoueurMarche joueur;
    private FermeMarche ferme;

    @BeforeEach
    void setUp() {
        marche = new Marche();
        joueur = new JoueurMarche(200);
        ferme = new FermeMarche();
    }

    // ─── Articles du marché ───

    @Test
    void marcheContientDeuxArticles() {
        assertEquals(2, marche.getArticles().size());
    }

    @Test
    void premierArticleEstVache() {
        ArticleMarche vache = marche.getArticles().get(0);
        assertEquals("Vache laitière", vache.getNom());
        assertEquals(TypeArticle.VACHE, vache.getType());
        assertEquals(50, vache.getPrix());
    }

    @Test
    void deuxiemeArticleEstArme() {
        ArticleMarche arme = marche.getArticles().get(1);
        assertEquals("Rayon laser", arme.getNom());
        assertEquals(TypeArticle.ARME, arme.getType());
        assertEquals(120, arme.getPrix());
    }

    // ─── Achat réussi ───

    @Test
    void acheterVacheReussi() {
        ArticleMarche vache = marche.getArticles().get(0);
        AchatResult r = marche.acheter(joueur, vache, ferme);

        assertEquals(AchatResult.Type.OK, r.getType());
        assertEquals(150, r.getMonnaieRestante());
        assertEquals(1, ferme.getNombreAnimaux());
    }

    @Test
    void acheterArmeReussi() {
        ArticleMarche arme = marche.getArticles().get(1);
        AchatResult r = marche.acheter(joueur, arme, ferme);

        assertEquals(AchatResult.Type.OK, r.getType());
        assertEquals(80, r.getMonnaieRestante());
        assertEquals(TypeArticle.ARME, r.getTypeArticle());
        assertEquals(1, joueur.getArmes().size());
    }

    // ─── Fonds insuffisants ───

    @Test
    void acheterSansFonds() {
        JoueurMarche pauvre = new JoueurMarche(10);
        ArticleMarche vache = marche.getArticles().get(0);
        AchatResult r = marche.acheter(pauvre, vache, ferme);

        assertEquals(AchatResult.Type.FONDS_INSUFFISANTS, r.getType());
        assertEquals(0, ferme.getNombreAnimaux());
    }

    // ─── Article null ───

    @Test
    void acheterArticleNull() {
        AchatResult r = marche.acheter(joueur, null, ferme);
        assertEquals(AchatResult.Type.ARTICLE_INTROUVABLE, r.getType());
    }

    // ─── Achat déjà en cours ───

    @Test
    void acheterDejaEnCours() {
        joueur.setAchatEnCours(true);
        ArticleMarche vache = marche.getArticles().get(0);
        AchatResult r = marche.acheter(joueur, vache, ferme);

        assertEquals(AchatResult.Type.DEJA_EN_COURS, r.getType());
        assertEquals(200, joueur.getMonnaie());
    }

    // ─── Achats multiples ───

    @Test
    void acheterPlusieursVaches() {
        ArticleMarche vache = marche.getArticles().get(0);
        marche.acheter(joueur, vache, ferme);
        marche.acheter(joueur, vache, ferme);
        marche.acheter(joueur, vache, ferme);

        assertEquals(3, ferme.getNombreAnimaux());
        assertEquals(50, joueur.getMonnaie());
    }

    @Test
    void acheterJusquaEpuisement() {
        ArticleMarche vache = marche.getArticles().get(0);
        marche.acheter(joueur, vache, ferme); // 150 restant
        marche.acheter(joueur, vache, ferme); // 100 restant
        marche.acheter(joueur, vache, ferme); // 50 restant
        marche.acheter(joueur, vache, ferme); // 0 restant
        AchatResult r = marche.acheter(joueur, vache, ferme); // pas assez

        assertEquals(AchatResult.Type.FONDS_INSUFFISANTS, r.getType());
        assertEquals(4, ferme.getNombreAnimaux());
    }

    // ─── AchatResult ───

    @Test
    void achatResultCout() {
        ArticleMarche vache = marche.getArticles().get(0);
        AchatResult r = marche.acheter(joueur, vache, ferme);
        assertEquals(50, r.getCout());
    }

    @Test
    void achatResultMessage() {
        ArticleMarche vache = marche.getArticles().get(0);
        AchatResult r = marche.acheter(joueur, vache, ferme);
        assertNotNull(r.getMessage());
        assertFalse(r.getMessage().isEmpty());
    }
}
