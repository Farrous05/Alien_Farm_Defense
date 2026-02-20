package com.fermedefense.modele.ferme;

/**
 * Représente la ferme où les vaches sont placées et évoluent.
 */
public class Ferme {
	private java.util.List<Vache> vaches = new java.util.ArrayList<>();

	public void ajouterVache(Vache vache) {
		vaches.add(vache);
	}

	public int getNombreAnimaux() {
		return vaches.size();
	}
}
