package com.fermedefense.modele.joueur;

public class Potion implements ObjetInventaire {
    @Override
    public String getNom() { return "Potion de vie"; }

    @Override
    public String getDescription() { return "Restaure 50 PV"; }
}
