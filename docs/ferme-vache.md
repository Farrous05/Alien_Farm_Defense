# Conception — Ferme et Vaches

## 1. Sous-problèmes

| # | Sous-problème | Classe |
|---|--------------|--------|
| SP1 | Modéliser le cycle de vie d'un animal (abstraction) | `Animal` |
| SP2 | Gérer la croissance d'une vache (BEBE → ADULTE → PRODUCTIVE) | `Vache`, `EtatVache` |
| SP3 | Accumuler de la monnaie quand la vache est productive | `Vache` |
| SP4 | Récolter la monnaie accumulée | `Vache`, `Ferme` |
| SP5 | Gérer le troupeau (ajout, retrait, capacité) | `Ferme` |
| SP6 | Propager le tick de mise à jour à toutes les vaches | `Ferme` |

---

## 2. Structures de données

### EtatVache (enum)

```
BEBE, ADULTE, PRODUCTIVE
```

### Animal (classe abstraite)

| Attribut | Type | Description |
|----------|------|-------------|
| `nom` | `String` | Identifiant de l'animal |
| `x`, `y` | `double` | Position dans la ferme |

| Méthode abstraite | Description |
|-------------------|-------------|
| `mettreAJour(deltaMs)` | Mise à jour par tick |
| `isProductif()` | L'animal produit-il des revenus ? |
| `getRevenusParCycle()` | Montant produit par cycle |

### Vache (extends Animal)

| Attribut | Type | Défaut | Description |
|----------|------|--------|-------------|
| `etat` | `EtatVache` | `BEBE` | Phase de croissance actuelle |
| `tempsEcoule` | `long` | `0` | Temps écoulé dans la phase courante (ms) |
| `tempsBebeMs` | `long` | `10 000` | Durée de la phase bébé |
| `tempsAdulteMs` | `long` | `15 000` | Durée de la phase adulte |
| `cycleProdMs` | `long` | `8 000` | Intervalle entre deux productions |
| `revenuParCycle` | `int` | `10` | Monnaie produite par cycle |
| `monnaieAccumulee` | `int` | `0` | Monnaie en attente de récolte |

### Ferme

| Attribut | Type | Défaut | Description |
|----------|------|--------|-------------|
| `vaches` | `List<Vache>` | `[]` | Troupeau |
| `capaciteMax` | `int` | `10` | Nombre max de vaches |

---

## 3. Algorithmes

### Vache.mettreAJour(deltaMs)

```
tempsEcoule ← tempsEcoule + deltaMs

SELON etat:
    BEBE:
        SI tempsEcoule ≥ tempsBebeMs ALORS
            surplus ← tempsEcoule - tempsBebeMs
            etat ← ADULTE
            tempsEcoule ← surplus
        FIN SI

    ADULTE:
        SI tempsEcoule ≥ tempsAdulteMs ALORS
            surplus ← tempsEcoule - tempsAdulteMs
            etat ← PRODUCTIVE
            tempsEcoule ← surplus
        FIN SI

    PRODUCTIVE:
        TANT QUE tempsEcoule ≥ cycleProdMs FAIRE
            monnaieAccumulee ← monnaieAccumulee + revenuParCycle
            tempsEcoule ← tempsEcoule - cycleProdMs
        FIN TANT QUE
FIN SELON
```

Le **report de surplus** garantit qu'un gros delta (par ex. lag) ne fait pas
perdre de temps de croissance.

### Vache.recolter()

```
montant ← monnaieAccumulee
monnaieAccumulee ← 0
RETOURNER montant
```

### Ferme.mettreAJour(deltaMs)

```
POUR CHAQUE vache DANS vaches FAIRE
    vache.mettreAJour(deltaMs)
FIN POUR
```

### Ferme.recolterTout()

```
total ← 0
POUR CHAQUE vache DANS vaches FAIRE
    total ← total + vache.recolter()
FIN POUR
RETOURNER total
```

### Ferme.ajouterVache(vache)

```
SI vache = null OU |vaches| ≥ capaciteMax ALORS
    RETOURNER false
FIN SI
vaches.ajouter(vache)
RETOURNER true
```

---

## 4. Conditions limites

| Condition | Comportement |
|-----------|-------------|
| Ferme pleine | `ajouterVache()` retourne `false` |
| Vache `null` | `ajouterVache()` retourne `false` |
| Récolte sans production | Retourne `0` |
| Delta très grand | Surplus reporté, pas de perte |
| Vache pas encore productive | `recolter()` retourne `0`, `isProductif()` retourne `false` |
| Liste exposée | `getVaches()` retourne une vue non modifiable |

---

## 5. Tests (JUnit 5)

### VacheTest (12 tests)

| Test | Vérifie |
|------|---------|
| `vacheCommenceEnEtatBebe` | État initial = BEBE |
| `vachePasseAdulteApresTempsRequis` | Transition BEBE → ADULTE |
| `vacheResteBebeAvantTempsRequis` | Pas de transition prématurée |
| `vachePasseProductiveApresDeuxPhases` | Transition ADULTE → PRODUCTIVE |
| `croissanceEnUnSeulTickGeant` | Cumul correct sur gros delta |
| `surplusTempsReporteCorrectement` | Surplus reporté dans la phase suivante |
| `productionAccumuleMonnaie` | N cycles → N × revenu |
| `recolterRetourneMonnaieEtRemet` | Récolte correcte + remise à zéro |
| `recolterSansProductionRetourneZero` | Récolte = 0 si rien produit |
| `pasDeProductionAvantEtatProductif` | Pas de monnaie avant PRODUCTIVE |
| `progressionBebeMidway` | Progression = 0.5 à mi-chemin |
| `constructeurPersonnalise` | Paramètres custom fonctionnent |

### FermeTest (12 tests)

| Test | Vérifie |
|------|---------|
| `ajouterVacheReussit` | Ajout normal |
| `ajouterVacheRefuseQuandPleine` | Capacité respectée |
| `ajouterVacheNullRefuse` | null rejeté |
| `retirerVacheReussit` | Retrait par référence |
| `retirerVacheInexistanteRetourneFalse` | Vache absente |
| `estPleineRetourneTrueQuandPleine` | Détection ferme pleine |
| `capaciteParDefaut` | `CAPACITE_MAX_DEFAUT = 10` |
| `mettreAJourPropageAToutesLesVaches` | Tick propagé |
| `recolterToutSommeDesVachesProductives` | Somme correcte |
| `recolterToutFermeVideRetourneZero` | Ferme vide → 0 |
| `getNombreProductivesCorrecte` | Comptage correct |
| `getVachesRetourneListeNonModifiable` | Immutabilité |
