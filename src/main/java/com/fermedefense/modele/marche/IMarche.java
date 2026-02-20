package com.fermedefense.modele.marche;

import java.util.List;

public interface IMarche {
    List<ArticleMarche> getArticles();
    AchatResult acheter(JoueurMarche joueur, ArticleMarche article, FermeMarche ferme);
}
