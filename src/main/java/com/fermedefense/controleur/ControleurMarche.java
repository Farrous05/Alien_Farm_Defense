package com.fermedefense.controleur;

import com.fermedefense.modele.combat.Arme;
import com.fermedefense.modele.ferme.Ferme;
import com.fermedefense.modele.ferme.Vache;
import com.fermedefense.modele.jeu.Carte;
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
    public enum ResultatAchat { OK, FONDS_INSUFFISANTS, INVENTAIRE_PLEIN, AUCUNE_SELECTION }

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

        if (joueur.getMonnaie() < article.getPrix()) {
            dernierMessage = "Fonds insuffisants !";
            return ResultatAchat.FONDS_INSUFFISANTS;
        }

        if (joueur.getInventaire().isPlein()) {
            dernierMessage = "Inventaire plein !";
            return ResultatAchat.INVENTAIRE_PLEIN;
        }

        joueur.depenser(article.getPrix());

        if (article.getType() == TypeArticle.VACHE) {
            joueur.getInventaire().ajouterObjet(new Vache(
                    article.getNom() + "#" + (ferme.getNombreAnimaux() + 1), 0, 0));
            dernierMessage = "Vache achetée !";
        } else if (article.getType() == TypeArticle.ARME) {
            joueur.getInventaire().ajouterObjet(new Arme(article.getNom(), RAYON_LASER.getDegats(), RAYON_LASER.getCooldownMs()));
            dernierMessage = "Arme ajoutée à l'inventaire : " + article.getNom();
        }

        return ResultatAchat.OK;
    }

    public String getDernierMessage() {
        return dernierMessage;
    }
}
