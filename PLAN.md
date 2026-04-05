# Alien Farm Defense — Feature Plan (PCII 2026)

## Status: Build is CLEAN. All items below are NOT yet started.

---

## 1. Local Leaderboard

**Files to create:**
- `modele/jeu/TableauScores.java` — data model
- `vue/VueTableauScores.java` — rendering

**Spec:**
- Stores top 10 entries: `{String initials (3 chars), int score, int niveau}`
- Saved/loaded from `~/.alienfarm/scores.dat` via `ObjectOutputStream` / `ObjectInputStream`
- `TableauScores.ajouter(String initiales, int score)` — inserts and trims to top 10
- `VueTableauScores.dessiner(Graphics2D g2, int cx, int cy)` — draws gold-bordered table

**Hooks:**
- In `VuePrincipale.dessinerFinDePartie()`: after the score panel, prompt 3-char initials (simple `JOptionPane.showInputDialog`), then call `tableauScores.ajouter(initiales, score)` and `tableauScores.sauvegarder()`
- On main menu (`VueMenuPrincipal`): add a "Meilleurs Scores" button that shows the leaderboard overlay

---

## 2. Achievement System

**Files to create:**
- `modele/jeu/Succes.java` — enum with metadata
- `modele/jeu/GestionnaireSucces.java` — tracks unlocked state
- `vue/VueSucces.java` — sidebar badge rendering

**Achievements (enum `Succes`):**
```
PREMIER_SANG    — "Premier sang"    — Kill first alien
FERMIER_PROSPERE — "Riche fermier"  — Earn 500g total (cumulative)
EXTERMINATEUR   — "Exterminateur"   — Kill 50 aliens total
INDESTRUCTIBLE  — "Indestructible"  — Complete a level at full HP
RANCHER         — "Éleveur"         — Have 3 cows on the farm simultaneously
```

**Spec:**
- `GestionnaireSucces` has `Set<Succes> debloquees` and `Map<Succes, Integer> compteurs`
- `verifier(Succes s, int valeur)` — checks threshold and fires unlock
- Saved alongside leaderboard in same file
- `VueSucces.dessiner(g2, x, y)` — draws a row of 20×20 colored icons in the sidebar (gray if locked, gold/colored if unlocked); tooltip on hover

**Hooks:**
- `ControleurAttaque`: on alien kill → `gestionnaire.verifier(PREMIER_SANG, 1)` and increment EXTERMINATEUR counter
- `ControleurJeu.tickProgression`: check INDESTRUCTIBLE at level end
- `VuePrincipale.paintComponent`: check RANCHER when `ferme.getNombreAnimaux() >= 3`
- `Joueur.ajouterMonnaie`: accumulate toward FERMIER_PROSPERE

---

## 3. Upgrade Shop (between levels)

**Files to create:**
- `vue/VueUpgrades.java` — full-screen shop overlay
- `modele/joueur/Upgrades.java` — multiplier storage

**Upgrades available:**
| Upgrade | Effect | Cost |
|---------|--------|------|
| Max HP +25 | `joueur.pvMax += 25` | 100g |
| Arme DMG +10% | multiplier stored in `Upgrades.dommageMulti` | 150g |
| Cow speed +20% | multiplier stored in `Upgrades.cowSpeedMulti` | 80g |
| Starting gold +50 | added at next level start | 75g |

**Spec:**
- `Upgrades` class stores multipliers, referenced by `Joueur` and `Vache`
- `VueUpgrades.dessiner(g2, vpW, vpH)` — semi-transparent overlay, 4 cards, [ENTER] to buy highlighted item, [SPACE] to skip
- Triggered: new `EtatJeu.UPGRADE_SHOP` state, entered after VICTOIRE before the next level starts

**Hooks:**
- In `VuePrincipale.ActionKeyListener.keyPressed(SPACE)` on VICTOIRE: instead of going directly to next level, transition to `EtatJeu.UPGRADE_SHOP`
- In upgrade shop: SPACE skips to next level, ENTER buys + deducts gold
- In `VuePrincipale.paintComponent`: if `etat == UPGRADE_SHOP`, draw `VueUpgrades` overlay instead of game content
- `ControleurJeu.initialiserNiveau()`: apply `upgrades.startingGoldBonus` at level start

---

## Suggested order

1. **Leaderboard first** — self-contained, no gameplay changes
2. **Upgrade Shop** — motivates replayability immediately
3. **Achievements** — polish layer on top of working game
