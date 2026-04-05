package com.fermedefense.modele.ferme;

/**
 * Représente une vache avec sa progression de croissance.
 *
 * Cycle de vie :
 *   BEBE  ──(tempsBebeMs)──▸  ADULTE  ──(tempsAdulteMs)──▸  PRODUCTIVE
 *
 * Une fois PRODUCTIVE, la vache accumule de la monnaie toutes les
 * {@code cycleProdMs} millisecondes. Le joueur peut récolter le montant
 * accumulé via {@link #recolter()}.
 *
 * Classe modèle pure – pas de thread, pas de dépendance UI.
 */
import com.fermedefense.modele.joueur.ObjetInventaire;

public class Vache extends Animal implements ObjetInventaire {

    // ----- Constantes par défaut -----
    /** Durée de la phase bébé (ms). */
    public static final long TEMPS_BEBE_MS  = 10_000;
    /** Durée de la phase adulte avant d'être productive (ms). */
    public static final long TEMPS_ADULTE_MS = 15_000;
    /** Intervalle entre deux productions (ms). */
    public static final long CYCLE_PROD_MS   = 8_000;
    /** Monnaie produite par cycle. */
    public static final int  REVENU_PAR_CYCLE = 10;

    // ----- État -----
    private EtatVache etat;
    private long tempsEcoule;          // temps dans l'état courant
    private long tempsBebeMs;
    private long tempsAdulteMs;
    private long cycleProdMs;
    private int  revenuParCycle;

    /** Monnaie accumulée en attente de récolte. */
    private int monnaieAccumulee;

    // ----- Constructeurs -----

    /**
     * Crée une vache avec les paramètres par défaut.
     */
    public Vache(String nom, double x, double y) {
        this(nom, x, y, TEMPS_BEBE_MS, TEMPS_ADULTE_MS, CYCLE_PROD_MS, REVENU_PAR_CYCLE);
    }

    /**
     * Crée une vache avec des durées et revenus personnalisés.
     */
    public Vache(String nom, double x, double y,
                 long tempsBebeMs, long tempsAdulteMs,
                 long cycleProdMs, int revenuParCycle) {
        super(nom, x, y);
        this.etat            = EtatVache.BEBE;
        this.tempsEcoule     = 0;
        this.tempsBebeMs     = tempsBebeMs;
        this.tempsAdulteMs   = tempsAdulteMs;
        this.cycleProdMs     = cycleProdMs;
        this.revenuParCycle  = revenuParCycle;
        this.monnaieAccumulee = 0;
    }

    // ----- Mise à jour -----

    @Override
    public void mettreAJour(long deltaMs) {
        tempsEcoule += deltaMs;

        switch (etat) {
            case BEBE:
                if (tempsEcoule >= tempsBebeMs) {
                    long surplus = tempsEcoule - tempsBebeMs;
                    etat = EtatVache.ADULTE;
                    tempsEcoule = surplus;       // report du surplus
                }
                break;

            case ADULTE:
                if (tempsEcoule >= tempsAdulteMs) {
                    long surplus = tempsEcoule - tempsAdulteMs;
                    etat = EtatVache.PRODUCTIVE;
                    tempsEcoule = surplus;
                }
                break;

            case PRODUCTIVE:
                while (tempsEcoule >= cycleProdMs) {
                    monnaieAccumulee += revenuParCycle;
                    tempsEcoule -= cycleProdMs;
                }
                break;
        }
    }

    // ----- Production -----

    @Override
    public boolean isProductif() {
        return etat == EtatVache.PRODUCTIVE;
    }

    @Override
    public int getRevenusParCycle() {
        return revenuParCycle;
    }

    /**
     * Récolte la monnaie accumulée et remet le compteur à zéro.
     *
     * @return le montant récolté
     */
    public int recolter() {
        int montant = monnaieAccumulee;
        monnaieAccumulee = 0;
        return montant;
    }

    /**
     * @return la monnaie en attente de récolte (lecture seule).
     */
    public int getMonnaieAccumulee() {
        return monnaieAccumulee;
    }

    // ----- Upgrades -----

    /**
     * Accélère la croissance de la vache par un multiplicateur.
     * Divise les durées de croissance et le cycle de production par {@code multi}.
     * Appelé quand l'upgrade "Vitesse vache" est achetée.
     *
     * @param multi multiplicateur (ex. 1.2 pour +20 %)
     */
    public void appliquerMultiVitesse(double multi) {
        if (multi <= 0) return;
        tempsBebeMs   = Math.max(1, (long) (tempsBebeMs   / multi));
        tempsAdulteMs = Math.max(1, (long) (tempsAdulteMs / multi));
        cycleProdMs   = Math.max(1, (long) (cycleProdMs   / multi));
    }

    // ----- Accesseurs -----

    public EtatVache getEtat() {
        return etat;
    }

    public long getTempsEcoule() {
        return tempsEcoule;
    }

    @Override
    public String getDescription() {
        return "Vache (Revenu: " + revenuParCycle + ")";
    }

    /**
     * Progression de la phase courante, entre 0.0 et 1.0.
     */
    public double getProgression() {
        return switch (etat) {
            case BEBE       -> (double) tempsEcoule / tempsBebeMs;
            case ADULTE     -> (double) tempsEcoule / tempsAdulteMs;
            case PRODUCTIVE -> (double) tempsEcoule / cycleProdMs;
        };
    }

    @Override
    public String toString() {
        return getNom() + " [" + etat + " " + String.format("%.0f%%", getProgression() * 100)
                + ", accumulé=" + monnaieAccumulee + "]";
    }
}
