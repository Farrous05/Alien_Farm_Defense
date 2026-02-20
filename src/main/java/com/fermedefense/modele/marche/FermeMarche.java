package com.fermedefense.modele.marche;

import java.util.ArrayList;
import java.util.List;

/**
 * Version simplifiée de Ferme pour le module marché autonome.
 */
public class FermeMarche {
    private List<String> animaux = new ArrayList<>();

    public void ajouterAnimal(String nom) {
        animaux.add(nom);
    }

    public int getNombreAnimaux() {
        return animaux.size();
    }

    public List<String> getVaches() {
        return animaux;
    }
}