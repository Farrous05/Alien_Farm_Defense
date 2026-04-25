package com.fermedefense.modele.marche;

import java.util.ArrayList;
import java.util.List;

public class Marche {
	private List<ArticleMarche> articlesDisponibles;

	public Marche() {
		articlesDisponibles = new ArrayList<>();
		//                       id        nom            type               prix  puissance  niveauRequis
		articlesDisponibles.add(new ArticleMarche("vache1",  "Vache",       TypeArticle.VACHE,  50,   0,  1));
		articlesDisponibles.add(new ArticleMarche("arme1",   "Rayon laser", TypeArticle.ARME,  120,  25,  1));
		articlesDisponibles.add(new ArticleMarche("potion1", "Potion",      TypeArticle.POTION, 30,   0,  1));
		articlesDisponibles.add(new ArticleMarche("bombe1",  "Bombe",       TypeArticle.BOMBE, 100,   0,  2));
		articlesDisponibles.add(new ArticleMarche("arme2",   "Minigun",     TypeArticle.ARME,  300,   8,  3));
	}

	public List<ArticleMarche> getArticles() {
		return articlesDisponibles;
	}
}
