package com.fermedefense.controleur;

import com.fermedefense.modele.combat.Arme;
import com.fermedefense.modele.ferme.Ferme;
import com.fermedefense.modele.ferme.Vache;
import com.fermedefense.modele.jeu.Carte;
import com.fermedefense.modele.joueur.ActionDuree;
import com.fermedefense.modele.joueur.Joueur;
import com.fermedefense.modele.marche.ArticleMarche;
import com.fermedefense.modele.marche.Marche;
import com.fermedefense.modele.marche.TypeArticle;

/**
 * Gère les interactions avec le marché :
 * achat de vaches (ajoutées à la ferme) et achat d'armes
 * (équipées dans le contrôleur de jeu).
 */
public class ControleurMarche {

    /** Résultat d'un achat, contient un message à afficher. */
    public enum ResultatAchat { OK, FONDS_INSUFFISANTS, INVENTAIRE_PLEIN, AUCUNE_SELECTION, ARTICLE_VERROUILLE }

    private final Joueur joueur;
    private final Ferme ferme;
    private final Marche marche;
    private final Carte carte;
    private final ControleurJeu controleurJeu;

    /** Arme « Rayon laser » : 25 dégâts, 800 ms cooldown. */
    public static final Arme RAYON_LASER = new Arme("Rayon laser", 25, 800);

    private String dernierMessage;

    public ControleurMarche(Joueur joueur, Ferme ferme, Marche marche,
                            Carte carte, ControleurJeu controleurJeu) {
        this.joueur = joueur;
        this.ferme = ferme;
        this.marche = marche;
        this.carte = carte;
        this.controleurJeu = controleurJeu;
    }

    /**
     * Tente d'acheter l'article à l'index donné.
     *
     * @param indexArticle index dans la liste des articles du marché, ou -1 si aucun
     * @return le résultat de l'achat
     */
    public ResultatAchat acheter(int indexArticle) {
        if (indexArticle < 0 || indexArticle >= marche.getArticles().size()) {
            dernierMessage = "Sélectionnez un article (1/2)";
            return ResultatAchat.AUCUNE_SELECTION;
        }

        ArticleMarche article = marche.getArticles().get(indexArticle);

        int niveauActuel = (controleurJeu != null && controleurJeu.getPartie() != null)
                ? controleurJeu.getPartie().getNiveau() : 1;
        if (!article.isDebloque(niveauActuel)) {
            dernierMessage = "Niveau " + article.getNiveauRequis() + " requis !";
            return ResultatAchat.ARTICLE_VERROUILLE;
        }

        if (joueur.getMonnaie() < article.getPrix()) {
            dernierMessage = "Fonds insuffisants !";
            return ResultatAchat.FONDS_INSUFFISANTS;
        }

        if (joueur.getInventaire().isPlein()) {
            dernierMessage = "Inventaire plein !";
            return ResultatAchat.INVENTAIRE_PLEIN;
        }

        joueur.depenser(article.getPrix());
        if (controleurJeu != null) {
            controleurJeu.setActionEnCours(new ActionDuree(ActionDuree.TypeAction.ACHAT, 800));
        }

        if (article.getType() == TypeArticle.VACHE) {
            joueur.getInventaire().ajouterObjet(new Vache(
                    "Vache#" + (ferme.getNombreAnimaux() + 1), 0, 0));
            dernierMessage = "Vache achetée !";
        } else if (article.getType() == TypeArticle.ARME) {
            Arme a = Arme.EPEE;
            if (article.getNom().equals("Shotgun")) a = Arme.SHOTGUN;
            if (article.getNom().equals("Minigun")) a = Arme.MINIGUN;
            if (article.getNom().equals("Rayon laser")) a = RAYON_LASER;
            joueur.getInventaire().ajouterObjet(a);
            dernierMessage = "Arme achetée : " + article.getNom();
        } else if (article.getType() == TypeArticle.POTION) {
            joueur.getInventaire().ajouterObjet(new com.fermedefense.modele.joueur.Potion());
            dernierMessage = "Potion achetée !";
        } else if (article.getType() == TypeArticle.BOMBE) {
            joueur.getInventaire().ajouterObjet(new com.fermedefense.modele.combat.Bombe());
            dernierMessage = "Bombe achetée !";
        }

        return ResultatAchat.OK;
    }

    /**
     * Surcharge pratique pour acheter directement un ArticleMarche.
     */
    public ResultatAchat acheter(com.fermedefense.modele.marche.ArticleMarche article) {
        if (article == null) {
            dernierMessage = "Sélectionnez un article";
            return ResultatAchat.AUCUNE_SELECTION;
        }
        int idx = marche.getArticles().indexOf(article);
        return acheter(idx);
    }

    public String getDernierMessage() {
        return dernierMessage;
    }
}
