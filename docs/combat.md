# Conception — Combat (attaques extraterrestres)

## 1. Sous-problèmes

| # | Sous-problème | Classe |
|---|--------------|--------|
| SP1 | Modéliser une arme (dégâts, cooldown) | `Arme` |
| SP2 | Modéliser un ennemi (PV, dégâts, cooldown) | `Extraterrestre` |
| SP3 | Modéliser le boss de fin de niveau | `BossFinal` |
| SP4 | Simuler un combat automatique (échange de coups) | `Attaque` |
| SP5 | Déterminer le résultat du combat | `ResultatCombat` |

---

## 2. Structures de données

### ResultatCombat (enum)

```
VICTOIRE, DEFAITE, EN_COURS
```

### Arme

| Attribut | Type | Description |
|----------|------|-------------|
| `nom` | `String` | Nom de l'arme |
| `degats` | `int` | Dégâts par coup |
| `cooldownMs` | `long` | Temps entre deux coups (ms) |

Constante statique : `Arme.EPEE` (dég=15, cd=1000ms).

### Extraterrestre

| Attribut | Type | Description |
|----------|------|-------------|
| `nom` | `String` | Nom de l'alien |
| `pointsDeVie` | `int` | PV actuels |
| `pointsDeVieMax` | `int` | PV maximum |
| `degats` | `int` | Dégâts infligés au joueur |
| `cooldownMs` | `long` | Intervalle entre coups (ms) |

### BossFinal (extends Extraterrestre)

| Attribut | Type | Description |
|----------|------|-------------|
| `recompense` | `int` | Monnaie gagnée si le boss est vaincu |

Fabrique statique : `BossFinal.pourNiveau(n)` — stats augmentent avec le niveau.

### Attaque (vague de combat)

| Attribut | Type | Description |
|----------|------|-------------|
| `aliens` | `List<Extraterrestre>` | Aliens dans cette vague |
| `indexAlienCourant` | `int` | Alien qu'on combat actuellement |
| `tempsCooldownJoueur` | `long` | Temps avant le prochain coup du joueur |
| `tempsCooldownAlien` | `long` | Temps avant le prochain coup de l'alien |
| `resultat` | `ResultatCombat` | État du combat |
| `totalDegatsInfliges` | `int` | Tracking des dégâts donnés |
| `totalDegatsRecus` | `int` | Tracking des dégâts reçus |

---

## 3. Algorithmes

### Attaque.mettreAJour(deltaMs, joueur, arme)

```
SI résultat ≠ EN_COURS ALORS RETOURNER

alien ← aliens[indexAlienCourant]
SI alien = null ALORS résultat ← VICTOIRE ; RETOURNER

tempsCooldownJoueur ← tempsCooldownJoueur - deltaMs
tempsCooldownAlien  ← tempsCooldownAlien  - deltaMs

// Le joueur frappe
SI tempsCooldownJoueur ≤ 0 ALORS
    alien.subirDegats(arme.degats)
    totalDegatsInfliges += arme.degats
    tempsCooldownJoueur ← arme.cooldownMs
    SI alien est mort ALORS
        indexAlienCourant++
        SI plus d'aliens ALORS résultat ← VICTOIRE ; RETOURNER
    FIN SI
FIN SI

// L'alien frappe le joueur
SI tempsCooldownAlien ≤ 0 ET alien vivant ALORS
    joueur.subirDegats(alien.degats)
    totalDegatsRecus += alien.degats
    tempsCooldownAlien ← alien.cooldownMs
    SI joueur mort ALORS résultat ← DEFAITE ; RETOURNER
FIN SI
```

### BossFinal.pourNiveau(n)

```
pv     ← 80 + n × 40
dégâts ← 8 + n × 4
cd     ← max(600, 1200 - n × 100)
récomp ← 100 + n × 50
RETOURNER new BossFinal(nom, pv, dégâts, cd, récomp)
```

---

## 4. Design : combat automatique

Le joueur ne contrôle **pas** le combat pour l'instant.
L'`Attaque` simule un échange de coups basé sur les cooldowns :

```
Joueur (Épée, cd=1s)        Alien (cd variable)
   │                              │
   ├──── frappe ──────────────────▶ -15 PV
   │          ◀───── frappe ──────┤ -X PV au joueur
   │                              │
   ├──── frappe ──────────────────▶ -15 PV
   ...                           ...
```

L'alien courant doit mourir avant de passer au suivant.
Le combat se termine quand tous les aliens sont morts (VICTOIRE)
ou quand le joueur tombe à 0 PV (DEFAITE).

---

## 5. Conditions limites

| Condition | Comportement |
|-----------|-------------|
| Dégâts ≤ 0 | Ignorés |
| PV alien < 0 | Plafonnés à 0 |
| Vague vide | Victoire immédiate |
| Combat terminé | `mettreAJour()` ne fait plus rien |
| Boss niveau 1 vs niveau 10 | Stats augmentent, cooldown descend (min 600ms) |

---

## 6. Tests (JUnit 5)

### CombatUnitTest (10 tests)

| Test | Vérifie |
|------|---------|
| `epeeParDefautADesStatsCoherentes` | Arme.EPEE a des stats > 0 |
| `dpsCalculCorrect` | DPS = dégâts × 1000 / cooldown |
| `alienPVInitiaux` | PV initiaux = max |
| `alienSubirDegats` | Dégâts correctement appliqués |
| `alienMeurtQuandPVZero` | isVivant() = false à 0 PV |
| `alienPVNePasNegatif` | PV plafonnés à 0 |
| `alienDegatsNegatifsIgnores` | Dégâts ≤ 0 sans effet |
| `alienReinitialiser` | PV remis au max |
| `alienRatioPv` | Ratio calculé correctement |
| `bossPourNiveauStatsAugmentent` | Boss nv3 > nv1 |
| `bossEstUnExtraterrestre` | Héritage correct |

### AttaqueTest (7 tests)

| Test | Vérifie |
|------|---------|
| `combatDebutEnCours` | Résultat initial = EN_COURS |
| `victoireSiAlienMeurt` | Alien faible → victoire |
| `defaiteSiJoueurMeurt` | Alien surpuissant → défaite |
| `vagueDeDeuxAliens` | Passe au suivant après kill |
| `statistiquesDegatsSontTrackes` | totalDegats > 0 |
| `mettreAJourApresVictoireNeChangePlusRien` | Combat terminé → immutable |
| `bossCommeAlienDansAttaque` | Boss fonctionne dans Attaque |
| `combatSimulationFinitToujours` | Combat finit en 60s |
