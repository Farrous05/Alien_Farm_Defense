package com.fermedefense.modele.joueur;

/**
 * Interface pour tout objet pouvant être stocké dans l'inventaire.
 */
public interface ObjetInventaire {
    /**
     * @return le nom de l'objet
     */
    String getNom();
    
    /**
     * @return une courte description de l'objet
     */
    String getDescription();
}
