package com.fermedefense.modele.jeu;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Suivi des succès débloqués.
 *
 * {@link #verifier(Succes, int)} prend la VALEUR COURANTE (pas un incrément) :
 * le gestionnaire conserve le maximum observé et débloque quand
 * valeurMax >= seuil.
 *
 * Persisté dans ~/.alienfarm/succes.dat.
 */
public class GestionnaireSucces implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String FICHIER =
            System.getProperty("user.home") + "/.alienfarm/succes.dat";

    private final Set<Succes>          debloquees;
    private final Map<Succes, Integer> compteurs;

    /** Succès en attente de notification (transient : non sérialisé). */
    private transient Queue<Succes> pendants;
    /** Callback appelé quand un nouveau succès est débloqué. */
    private transient Runnable onDeblocage;

    public GestionnaireSucces() {
        this.debloquees = EnumSet.noneOf(Succes.class);
        this.compteurs  = new EnumMap<>(Succes.class);
        for (Succes s : Succes.values()) {
            compteurs.put(s, 0);
        }
    }

    /**
     * Met à jour le compteur avec la valeur courante (max observé) et
     * déclenche le déblocage si le seuil est atteint.
     *
     * @param s     le succès à vérifier
     * @param valeur valeur courante (ex. nombre total d'aliens tués)
     */
    public void verifier(Succes s, int valeur) {
        if (debloquees.contains(s)) return;
        int ancien = compteurs.getOrDefault(s, 0);
        if (valeur > ancien) {
            compteurs.put(s, valeur);
        }
        if (compteurs.get(s) >= s.seuil) {
            debloquees.add(s);
            getPendants().offer(s);
            if (onDeblocage != null) onDeblocage.run();
        }
    }

    public boolean isDebloque(Succes s) {
        return debloquees.contains(s);
    }

    public int getCompteur(Succes s) {
        return compteurs.getOrDefault(s, 0);
    }

    /**
     * Retire et retourne le prochain succès nouvellement débloqué
     * (null si la file est vide).
     */
    public Succes consommerPendant() {
        return getPendants().poll();
    }

    public void setOnDeblocage(Runnable r) {
        this.onDeblocage = r;
    }

    private Queue<Succes> getPendants() {
        if (pendants == null) pendants = new LinkedList<>();
        return pendants;
    }

    // ── Persistance ──────────────────────────────────────────────────────────

    public void sauvegarder() {
        try {
            Path p = Paths.get(FICHIER);
            Files.createDirectories(p.getParent());
            try (ObjectOutputStream oos =
                         new ObjectOutputStream(new FileOutputStream(p.toFile()))) {
                oos.writeObject(this);
            }
        } catch (IOException e) {
            System.err.println("GestionnaireSucces: erreur sauvegarde — " + e.getMessage());
        }
    }

    public static GestionnaireSucces charger() {
        try {
            File f = new File(FICHIER);
            if (!f.exists()) return new GestionnaireSucces();
            try (ObjectInputStream ois =
                         new ObjectInputStream(new FileInputStream(f))) {
                return (GestionnaireSucces) ois.readObject();
            }
        } catch (Exception e) {
            System.err.println("GestionnaireSucces: erreur chargement — " + e.getMessage());
            return new GestionnaireSucces();
        }
    }
}
