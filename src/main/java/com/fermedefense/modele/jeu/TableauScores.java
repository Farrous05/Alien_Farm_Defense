package com.fermedefense.modele.jeu;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Tableau des meilleurs scores — top 10, persisté dans ~/.alienfarm/scores.dat.
 */
public class TableauScores implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final int MAX_ENTREES = 10;
    private static final String FICHIER =
            System.getProperty("user.home") + "/.alienfarm/scores.dat";

    // ── Entrée ──────────────────────────────────────────────────────────────

    public static class Entree implements Serializable, Comparable<Entree> {
        @Serial
        private static final long serialVersionUID = 1L;

        public final String initiales;
        public final int    score;
        public final int    niveau;

        public Entree(String initiales, int score, int niveau) {
            this.initiales = initiales;
            this.score     = score;
            this.niveau    = niveau;
        }

        @Override
        public int compareTo(Entree o) {
            return Integer.compare(o.score, this.score); // décroissant
        }
    }

    // ── Données ──────────────────────────────────────────────────────────────

    private final List<Entree> entrees;

    public TableauScores() {
        this.entrees = new ArrayList<>();
    }

    /**
     * Ajoute une entrée et maintient le classement top-10.
     */
    public void ajouter(String initiales, int score, int niveau) {
        String ini = (initiales == null ? "???" : initiales.toUpperCase().trim());
        if (ini.length() > 3) ini = ini.substring(0, 3);
        if (ini.isEmpty())    ini = "???";
        entrees.add(new Entree(ini, score, niveau));
        Collections.sort(entrees);
        if (entrees.size() > MAX_ENTREES) {
            entrees.subList(MAX_ENTREES, entrees.size()).clear();
        }
    }

    public List<Entree> getEntrees() {
        return Collections.unmodifiableList(entrees);
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
            System.err.println("TableauScores: erreur sauvegarde — " + e.getMessage());
        }
    }

    public static TableauScores charger() {
        try {
            File f = new File(FICHIER);
            if (!f.exists()) return new TableauScores();
            try (ObjectInputStream ois =
                         new ObjectInputStream(new FileInputStream(f))) {
                return (TableauScores) ois.readObject();
            }
        } catch (Exception e) {
            System.err.println("TableauScores: erreur chargement — " + e.getMessage());
            return new TableauScores();
        }
    }
}
