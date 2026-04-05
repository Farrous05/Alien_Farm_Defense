package com.fermedefense.vue;

import java.awt.Image;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

import com.fermedefense.modele.marche.ArticleMarche;
import com.fermedefense.modele.marche.Marche;
import com.fermedefense.modele.marche.TypeArticle;

/**
 * Objet vendeur placé dans le monde.
 * Quand le joueur s'approche, un popup de boutique s'affiche.
 */
public class VendeurMarche {

    /** Rayon d'interaction en pixels monde (le popup s'ouvre dans ce rayon). */
    public static final double RAYON = 90;

    private final double worldX;
    private final double worldY;
    private final String nom;
    private final Image sprite;
    private final List<ArticleMarche> articles;
    private int selection = 0;

    public VendeurMarche(double worldX, double worldY, String nom,
                         String spritePath, Marche marche, TypeArticle... types) {
        this.worldX   = worldX;
        this.worldY   = worldY;
        this.nom      = nom;
        this.sprite   = loadImage(spritePath);
        List<TypeArticle> typeList = List.of(types);
        this.articles = marche.getArticles().stream()
                .filter(a -> typeList.contains(a.getType()))
                .collect(Collectors.toList());
    }

    // ── Interaction ──────────────────────────────────────────────────────────

    /** @return true si le joueur est dans le rayon d'interaction. */
    public boolean estProche(double jx, double jy) {
        double dx = jx - worldX;
        double dy = jy - worldY;
        return dx * dx + dy * dy <= RAYON * RAYON;
    }

    public void selectionSuivante() {
        if (!articles.isEmpty())
            selection = (selection + 1) % articles.size();
    }

    public void selectionPrecedente() {
        if (!articles.isEmpty())
            selection = (selection - 1 + articles.size()) % articles.size();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public double getWorldX()    { return worldX; }
    public double getWorldY()    { return worldY; }
    public String getNom()       { return nom; }
    public Image  getSprite()    { return sprite; }
    public List<ArticleMarche> getArticles() { return articles; }
    public int    getSelection() { return selection; }

    public ArticleMarche getArticleSelectionne() {
        if (articles.isEmpty() || selection < 0 || selection >= articles.size()) return null;
        return articles.get(selection);
    }

    // ── Utilitaire ───────────────────────────────────────────────────────────

    private static Image loadImage(String path) {
        try (InputStream is = VendeurMarche.class.getResourceAsStream(path)) {
            return (is != null) ? ImageIO.read(is) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
