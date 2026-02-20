
package com.fermedefense.modele.marche;

import java.util.ArrayList;
import java.util.List;

public class Marche implements IMarche {
	private List<ArticleMarche> articlesDisponibles;

	public Marche() {
		articlesDisponibles = new ArrayList<>();
		articlesDisponibles.add(new ArticleMarche("vache1", "Vache laitière", TypeArticle.VACHE, 50, 0));
		articlesDisponibles.add(new ArticleMarche("arme1", "Rayon laser", TypeArticle.ARME, 120, 10));
	}

	@Override
	public List<ArticleMarche> getArticles() {
		return articlesDisponibles;
	}

	@Override
	public AchatResult acheter(JoueurMarche joueur, ArticleMarche article, FermeMarche ferme) {
		if (article == null) {
			return new AchatResult(AchatResult.Type.ARTICLE_INTROUVABLE, "Article introuvable.", 0, joueur.getMonnaie(), null);
		}
		if (joueur.isAchatEnCours()) {
			return new AchatResult(AchatResult.Type.DEJA_EN_COURS, "Achat déjà en cours.", article.getPrix(), joueur.getMonnaie(), article.getType());
		}
		if (joueur.getMonnaie() < article.getPrix()) {
			return new AchatResult(AchatResult.Type.FONDS_INSUFFISANTS, "Fonds insuffisants.", article.getPrix(), joueur.getMonnaie(), article.getType());
		}
		joueur.setAchatEnCours(true);
		boolean depense = joueur.depenser(article.getPrix());
		if (!depense) {
			joueur.setAchatEnCours(false);
			return new AchatResult(AchatResult.Type.FONDS_INSUFFISANTS, "Fonds insuffisants.", article.getPrix(), joueur.getMonnaie(), article.getType());
		}
		if (article.getType() == TypeArticle.VACHE) {
			ferme.ajouterAnimal(article.getNom());
		} else if (article.getType() == TypeArticle.ARME) {
			joueur.ajouterArme(article.getNom());
		}
		joueur.setAchatEnCours(false);
		return new AchatResult(AchatResult.Type.OK, "Achat réussi.", article.getPrix(), joueur.getMonnaie(), article.getType());
	}
}

class AchatResult {
	public enum Type { OK, FONDS_INSUFFISANTS, ARTICLE_INTROUVABLE, DEJA_EN_COURS, ERREUR }
	private final Type type;
	private final String message;
	private final int cout;
	private final int monnaieRestante;
	private final TypeArticle typeArticle;

	public AchatResult(Type type, String message, int cout, int monnaieRestante, TypeArticle typeArticle) {
		this.type = type;
		this.message = message;
		this.cout = cout;
		this.monnaieRestante = monnaieRestante;
		this.typeArticle = typeArticle;
	}

	public Type getType() { return type; }
	public String getMessage() { return message; }
	public int getCout() { return cout; }
	public int getMonnaieRestante() { return monnaieRestante; }
	public TypeArticle getTypeArticle() { return typeArticle; }
}
