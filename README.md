# Alien Farm Defense

Jeu de stratégie en temps réel où un fermier doit gérer sa ferme, acheter et vendre des vaches, acquérir des armes au marché, et défendre son troupeau contre des attaques extraterrestres.

## Description

Le joueur incarne un fermier qui se déplace entre deux zones :
- **La Ferme** : les vaches achetées y apparaissent et grandissent automatiquement jusqu'à être prêtes à la vente.
- **Le Marché** : permet d'acheter de nouvelles vaches et des armes de défense.

Une barre de progression temporelle rythme la partie et annonce des attaques extraterrestres. Le joueur doit anticiper ces attaques pour protéger son troupeau. À la fin du temps imparti, un combat final contre un boss extraterrestre détermine la victoire ou la défaite.

## Architecture

Le projet suit une architecture **MVC** (Modèle-Vue-Contrôleur) :

```
src/main/java/com/fermedefense/
├── Main.java
├── modele/
│   ├── joueur/       # Fermier, inventaire, actions
│   ├── ferme/        # Vaches, croissance, ferme
│   ├── marche/       # Boutique, articles
│   ├── combat/       # Extraterrestres, armes, boss
│   ├── progression/  # Barre de temps, niveaux
│   └── jeu/          # État global, carte, partie
├── vue/              # Interfaces graphiques
├── controleur/       # Logique de contrôle
└── utilitaire/       # Constantes, gestion du temps
```

## Prérequis

- Java 17+
- Maven 3.8+

## Compilation & Exécution

```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.fermedefense.Main"
```

## Équipe

Projet PCII — 2026
