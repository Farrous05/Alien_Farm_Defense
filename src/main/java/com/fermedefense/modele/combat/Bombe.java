package com.fermedefense.modele.combat;

import com.fermedefense.modele.joueur.ObjetInventaire;

public class Bombe implements ObjetInventaire {
    @Override
    public String getNom() { return "Bombe"; }

    @Override
    public String getDescription() { return "Inflige 150 dégâts à un alien"; }
}
