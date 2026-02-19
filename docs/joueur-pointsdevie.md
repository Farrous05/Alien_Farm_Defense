# Conception — Points de vie du joueur

## 1. Sous-problèmes

| # | Sous-problème | Classe |
|---|--------------|--------|
| SP1 | Stocker les points de vie (actuels et max) | `Joueur` |
| SP2 | Subir des dégâts et vérifier si le joueur est vivant | `Joueur` |
| SP3 | Se soigner (récupérer des PV) | `Joueur` |

---

## 2. Nouveaux attributs dans Joueur

| Attribut | Type | Description |
|----------|------|-------------|
| `pointsDeVie` | `int` | Points de vie actuels |
| `pointsDeVieMax` | `int` | Points de vie maximum |

---

## 3. Algorithmes

### subirDegats(degats)

```
SI degats ≤ 0 ALORS RETOURNER
pointsDeVie ← pointsDeVie - degats
SI pointsDeVie < 0 ALORS
    pointsDeVie ← 0
FIN SI
```

### soigner(montant)

```
SI montant ≤ 0 ALORS RETOURNER
pointsDeVie ← pointsDeVie + montant
SI pointsDeVie > pointsDeVieMax ALORS
    pointsDeVie ← pointsDeVieMax
FIN SI
```

### isVivant()

```
RETOURNER pointsDeVie > 0
```

---

## 4. Conditions limites

| Condition | Comportement |
|-----------|-------------|
| Dégâts > PV restants | PV mis à 0 (pas de négatif) |
| Soin dépasse le max | PV plafonné à `pointsDeVieMax` |
| Dégâts ≤ 0 | Ignoré |
| Soin ≤ 0 | Ignoré |
| Joueur mort (PV = 0) | `isVivant()` retourne `false` |
