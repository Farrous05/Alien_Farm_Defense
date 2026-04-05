package com.fermedefense.modele.joueur;

/**
 * Améliorations permanentes achetées dans la boutique entre les niveaux.
 *
 * Les multiplicateurs s'appliquent de manière cumulative : chaque achat
 * de "Dégâts +10 %" multiplie {@code dommageMulti} par 1.10, etc.
 *
 * Le bonus {@code startingGoldBonus} est ajouté au joueur au début de
 * chaque niveau dans {@code ControleurJeu.initialiserNiveau}.
 */
public class Upgrades {

    /** Multiplicateur global des dégâts des armes (défaut 1.0 = ×100 %). */
    public double dommageMulti    = 1.0;

    /** Multiplicateur de vitesse de croissance des vaches (défaut 1.0). */
    public double cowSpeedMulti   = 1.0;

    /** Or supplémentaire reçu au début de chaque niveau (cumulatif). */
    public int    startingGoldBonus = 0;

    // Les PV max sont appliqués directement sur Joueur lors de l'achat
    // → pas de champ ici.
}
