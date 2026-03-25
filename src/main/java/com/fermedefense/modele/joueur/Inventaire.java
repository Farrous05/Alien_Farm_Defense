package com.fermedefense.modele.joueur;

/**
 * Représente l'inventaire du joueur sous la forme d'une grille (5x5 par défaut).
 */
public class Inventaire {
    private final ObjetInventaire[][] grille;
    private final int lignes;
    private final int colonnes;

    public Inventaire(int lignes, int colonnes) {
        this.lignes = lignes;
        this.colonnes = colonnes;
        this.grille = new ObjetInventaire[lignes][colonnes];
    }
    
    public Inventaire() {
        this(5, 5);
    }

    /**
     * Ajoute un objet dans la première case vide disponible.
     * @param objet l'objet à ajouter
     * @return true si l'objet a été ajouté, false si l'inventaire est plein
     */
    public boolean ajouterObjet(ObjetInventaire objet) {
        for (int i = 0; i < lignes; i++) {
            for (int j = 0; j < colonnes; j++) {
                if (grille[i][j] == null) {
                    grille[i][j] = objet;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retire et retourne l'objet à la position donnée.
     * @return l'objet retiré, ou null si la case est vide ou invalide
     */
    public ObjetInventaire retirerObjet(int ligne, int colonne) {
        if (ligne >= 0 && ligne < lignes && colonne >= 0 && colonne < colonnes) {
            ObjetInventaire res = grille[ligne][colonne];
            grille[ligne][colonne] = null;
            return res;
        }
        return null;
    }

    /**
     * Obtient l'objet à la position donnée sans le retirer.
     */
    public ObjetInventaire getObjet(int ligne, int colonne) {
        if (ligne >= 0 && ligne < lignes && colonne >= 0 && colonne < colonnes) {
            return grille[ligne][colonne];
        }
        return null;
    }

    /**
     * @return true si toutes les cases sont occupées
     */
    public boolean isPlein() {
        for (int i = 0; i < lignes; i++) {
            for (int j = 0; j < colonnes; j++) {
                if (grille[i][j] == null) return false;
            }
        }
        return true;
    }

    public int getLignes() { return lignes; }
    public int getColonnes() { return colonnes; }
}
