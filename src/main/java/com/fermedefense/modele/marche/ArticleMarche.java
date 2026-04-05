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
	/** Niveau minimum requis pour débloquer cet article (1 = disponible dès le début). */
	private int niveauRequis;

	public ArticleMarche(String id, String nom, TypeArticle type, int prix, int puissance, int niveauRequis) {
		this.id = id;
		this.nom = nom;
		this.type = type;
		this.prix = prix;
		this.puissance = puissance;
		this.niveauRequis = niveauRequis;
	}

	public String getId() { return id; }
	public String getNom() { return nom; }
	public TypeArticle getType() { return type; }
	public int getPrix() { return prix; }
	public int getPuissance() { return puissance; }
	public int getNiveauRequis() { return niveauRequis; }
	public boolean isDebloque(int niveauActuel) { return niveauActuel >= niveauRequis; }

	@Override
	public String toString() {
		return nom + " (" + type + ", prix: " + prix + ", puissance: " + puissance + ")";
	}
}
