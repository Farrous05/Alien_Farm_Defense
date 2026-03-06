package com.fermedefense.modele.progression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Barre de progression temporelle qui rythme la partie.
 *
 * Remplit trois rôles :
 *   1. Compteur de temps (progression de 0 % à 100 %)
 *   2. Détection des événements programmés (vagues, boss)
 *   3. Source de données pour la vue (VueBarreProgression)
 *
 * Initialisée à partir d'un Niveau.
 */
public class BarreProgression {

    private final long dureeMs;
    private long tempsEcoule;
    private final List<EvenementTemporel> evenements;
    private boolean terminee;

    /**
     * Crée une barre de progression pour un niveau donné.
     */
    public BarreProgression(Niveau niveau) {
        this.dureeMs = niveau.getDureeMs();
        this.tempsEcoule = 0;
        this.terminee = false;
        this.evenements = new ArrayList<>();

        // Programmer les vagues intermédiaires
        List<Long> moments = niveau.getMomentsAttaques();
        for (int i = 0; i < moments.size(); i++) {
            evenements.add(new EvenementTemporel(
                    moments.get(i),
                    EvenementTemporel.TypeEvenement.ATTAQUE_INTERMEDIAIRE,
                    i
            ));
        }

        // Programmer le combat final à la fin
        evenements.add(new EvenementTemporel(
                dureeMs,
                EvenementTemporel.TypeEvenement.COMBAT_FINAL,
                -1
        ));
    }

    /**
     * Met à jour le temps écoulé et retourne la liste
     * des événements qui viennent de se déclencher.
     *
     * @param deltaMs temps écoulé depuis le dernier tick
     * @return événements déclenchés pendant ce tick (peut être vide)
     */
    public List<EvenementTemporel> mettreAJour(long deltaMs) {
        if (terminee) return Collections.emptyList();

        long avant = tempsEcoule;
        tempsEcoule += deltaMs;
        if (tempsEcoule > dureeMs) {
            tempsEcoule = dureeMs;
        }

        List<EvenementTemporel> declenches = new ArrayList<>();
        for (EvenementTemporel evt : evenements) {
            if (!evt.isDeclenche() && evt.getMomentMs() > avant && evt.getMomentMs() <= tempsEcoule) {
                evt.declencher();
                declenches.add(evt);
            }
        }

        if (tempsEcoule >= dureeMs) {
            terminee = true;
        }

        return declenches;
    }

    /**
     * Réinitialise la barre (pour rejouer un niveau).
     */
    public void reinitialiser() {
        tempsEcoule = 0;
        terminee = false;
        for (EvenementTemporel evt : evenements) {
            evt.reinitialiser();
        }
    }

    // --- Accesseurs ---

    public double getProgression() {
        return (double) tempsEcoule / dureeMs;
    }

    public long getTempsEcoule() { return tempsEcoule; }
    public long getDureeMs() { return dureeMs; }
    public long getTempsRestant() { return Math.max(0, dureeMs - tempsEcoule); }
    public boolean isTerminee() { return terminee; }

    public List<EvenementTemporel> getEvenements() {
        return Collections.unmodifiableList(evenements);
    }
}
