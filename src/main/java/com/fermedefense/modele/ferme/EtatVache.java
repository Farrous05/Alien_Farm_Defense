package com.fermedefense.modele.ferme;

/**
 * États de croissance d'une vache.
 *
 * BEBE       → la vache vient d'être achetée, elle grandit.
 * ADULTE     → la vache a fini de grandir, pas encore productive.
 * PRODUCTIVE → la vache produit des revenus à intervalles réguliers.
 */
public enum EtatVache {
    BEBE,
    ADULTE,
    PRODUCTIVE
}
