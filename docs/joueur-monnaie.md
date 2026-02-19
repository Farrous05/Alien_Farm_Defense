# Conception — Monnaie du joueur

## 1. Sous-problèmes

| # | Sous-problème | Classe |
|---|--------------|--------|
| SP1 | Stocker la monnaie du joueur | `Joueur` |
| SP2 | Dépenser (vérifier si assez, déduire) | `Joueur` |
| SP3 | Gagner de la monnaie (vente de vaches, etc.) | `Joueur` |

---

## 2. Nouveaux attributs dans Joueur

| Attribut | Type | Description |
|----------|------|-------------|
| `monnaie` | `int` | Monnaie actuelle du joueur |

---

## 3. Algorithmes

### depenser(montant)

```
SI montant ≤ 0 ALORS RETOURNER faux
SI montant > monnaie ALORS RETOURNER faux
monnaie ← monnaie - montant
RETOURNER vrai
```

### ajouterMonnaie(montant)

```
SI montant ≤ 0 ALORS RETOURNER
monnaie ← monnaie + montant
```

---

## 4. Conditions limites

| Condition | Comportement |
|-----------|-------------|
| Pas assez de monnaie pour dépenser | Retourne `false`, monnaie inchangée |
| Montant ≤ 0 | Ignoré |
