# Conception — Déplacement du joueur

## 1. Sous-problèmes

| # | Sous-problème | Classe |
|---|--------------|--------|
| SP1 | Représenter les 4 directions de déplacement | `Action` |
| SP2 | Modéliser le joueur (position, vitesse, mouvement) | `Joueur` |

---

## 2. SP1 — Action

Enum représentant les directions possibles.

```
ÉNUMÉRATION Action:
    HAUT, BAS, GAUCHE, DROITE
```

---

## 3. SP2 — Joueur

Classe modèle pure (pas de thread, pas de Swing).

### Attributs

| Attribut | Type | Description |
|----------|------|-------------|
| `x`, `y` | `double` | Position du joueur |
| `vitesse` | `double` | Vitesse en pixels/seconde |
| `directionCourante` | `Action` | Direction active (ou `null`) |
| `enMouvement` | `boolean` | `true` si une touche est enfoncée |
| `TAILLE` | `int` (constante = 30) | Taille du joueur en pixels |

### Algorithme — mettreAJour(deltaMs)

```
SI enMouvement ET directionCourante ≠ null ALORS
    deplacement ← vitesse × deltaMs / 1000
    SELON directionCourante:
        HAUT   → y ← y - deplacement
        BAS    → y ← y + deplacement
        GAUCHE → x ← x - deplacement
        DROITE → x ← x + deplacement
FIN SI
```

### Algorithme — appuyerDirection / relacherDirection

```
appuyerDirection(direction):
    directionCourante ← direction
    enMouvement ← vrai

relacherDirection(direction):
    SI directionCourante == direction ALORS
        enMouvement ← faux
        directionCourante ← null
    FIN SI
```

---

## 4. Test — TestJoueur

`TestJoueur.java` est un programme de test qui utilise un **thread dédié** pour appeler `joueur.mettreAJour(delta)` en boucle, et un **Timer Swing** pour rafraîchir l'affichage.

```bash
javac *.java
java TestJoueur
```
