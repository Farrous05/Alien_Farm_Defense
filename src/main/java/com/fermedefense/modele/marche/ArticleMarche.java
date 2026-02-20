package com.fermedefense.modele.marche;

/**
 * Représente un article disponible à l'achat au marché.
 */
public class ArticleMarche {
	private String id;
	private String nom;
	private TypeArticle type;
	private int prix;
	private int puissance;

	public ArticleMarche(String id, String nom, TypeArticle type, int prix, int puissance) {
		this.id = id;
		this.nom = nom;
		this.type = type;
		this.prix = prix;
		this.puissance = puissance;
	}

	public String getId() { return id; }
	public String getNom() { return nom; }
	public TypeArticle getType() { return type; }
	public int getPrix() { return prix; }
	public int getPuissance() { return puissance; }

	@Override
	public String toString() {
		return nom + " (" + type + ", prix: " + prix + ", puissance: " + puissance + ")";
	}
}
