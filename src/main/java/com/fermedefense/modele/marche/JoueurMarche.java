package com.fermedefense.modele.marche;

/**
 * Version simplifiée de Joueur pour le module marché autonome.
 */
public class JoueurMarche {
    private int monnaie;
    private java.util.List<String> armes = new java.util.ArrayList<>();
    private boolean achatEnCours = false;

    public JoueurMarche(int monnaieInit) {
        this.monnaie = monnaieInit;
    }

    public boolean depenser(int montant) {
        if (montant <= 0 || montant > monnaie) return false;
        this.monnaie -= montant;
        return true;
    }

    public int getMonnaie() {
        return monnaie;
    }

    public void ajouterMonnaie(int montant) {
        if (montant > 0) this.monnaie += montant;
    }

    public void ajouterArme(String nom) {
        armes.add(nom);
    }

    public java.util.List<String> getArmes() {
        return armes;
    }

    public boolean isAchatEnCours() {
        return achatEnCours;
    }
    public void setAchatEnCours(boolean enCours) {
        this.achatEnCours = enCours;
    }
}