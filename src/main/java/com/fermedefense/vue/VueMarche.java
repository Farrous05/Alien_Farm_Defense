package com.fermedefense.vue;

import java.util.List;

import com.fermedefense.modele.marche.Marche;
import com.fermedefense.modele.marche.TypeArticle;
import com.fermedefense.utilitaire.Constantes;

/**
 * Crée et expose les vendeurs physiques placés dans la zone Marché du monde.
 *
 * Chaque vendeur est un objet à une position monde fixe.
 * Le rendu est délégué à {@link VueMarchePopup}.
 *
 * Positions des vendeurs (coordonnées monde, zone marché x = LARGEUR_CARTE/2) :
 *   Armes    → (1350, 300)
 *   Vaches   → (1700, 550)
 *   Potions  → (2000, 300)
 *   Bombes   → (1550, 850)
 */
public class VueMarche {

    private final List<VendeurMarche> vendeurs;

    public VueMarche(Marche marche) {
        int split = Constantes.LARGEUR_CARTE / 2; // 1200

        vendeurs = List.of(
            new VendeurMarche(
                split + 120, 260,
                "Forge — Armes",
                "/images/market/sword_normal.png",
                marche,
                TypeArticle.ARME
            ),
            new VendeurMarche(
                split + 400, 460,
                "Élevage — Vaches",
                "/images/vache_bebe.png",
                marche,
                TypeArticle.VACHE
            ),
            new VendeurMarche(
                split + 680, 260,
                "Apothicaire",
                "/images/market/potion_red.png",
                marche,
                TypeArticle.POTION
            ),
            new VendeurMarche(
                split + 280, 700,
                "Armurerie — Bombes",
                "/images/market/chest.png",
                marche,
                TypeArticle.BOMBE
            )
        );
    }

    public List<VendeurMarche> getVendeurs() {
        return vendeurs;
    }

    /**
     * Retourne le vendeur le plus proche du joueur, ou null si aucun n'est
     * dans le rayon d'interaction.
     */
    public VendeurMarche getVendeurActif(double jx, double jy) {
        for (VendeurMarche v : vendeurs) {
            if (v.estProche(jx, jy)) return v;
        }
        return null;
    }
}
