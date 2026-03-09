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
    public enum ResultatAchat { OK, FONDS_INSUFFISANTS, FERME_PLEINE, AUCUNE_SELECTION }

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

        if (article.getType() == TypeArticle.VACHE && ferme.estPleine()) {
            dernierMessage = "Ferme pleine !";
            return ResultatAchat.FERME_PLEINE;
        }

        joueur.depenser(article.getPrix());

        if (article.getType() == TypeArticle.VACHE) {
            int[] zf = carte.getZoneFerme();
            double vx = zf[0] + 20 + Math.random() * (zf[2] - 60);
            double vy = zf[1] + 50 + Math.random() * (zf[3] - 100);
            ferme.ajouterVache(new Vache(
                    article.getNom() + "#" + (ferme.getNombreAnimaux() + 1), vx, vy));
            dernierMessage = "Vache achetée !";
        } else if (article.getType() == TypeArticle.ARME) {
            controleurJeu.setArme(RAYON_LASER);
            dernierMessage = "Arme équipée : " + article.getNom();
        }

        return ResultatAchat.OK;
    }

    public String getDernierMessage() {
        return dernierMessage;
    }
}
