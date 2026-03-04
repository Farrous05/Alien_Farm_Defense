package com.fermedefense.modele.combat;

/**
 * Résultat d'un combat.
 */
public enum ResultatCombat {
    /** Tous les aliens sont morts, joueur encore en vie. */
    VICTOIRE,
    /** Le joueur est mort (PV ≤ 0). */
    DEFAITE,
    /** Le combat est encore en cours. */
    EN_COURS
}
