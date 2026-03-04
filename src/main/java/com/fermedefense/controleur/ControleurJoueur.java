package com.fermedefense.controleur;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import com.fermedefense.modele.joueur.Action;
import com.fermedefense.modele.joueur.Joueur;

/**
 * Gère les entrées clavier pour déplacer le joueur.
 */
public class ControleurJoueur extends KeyAdapter {

    private final Joueur joueur;

    public ControleurJoueur(Joueur joueur) {
        this.joueur = joueur;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        Action a = mapTouche(e.getKeyCode());
        if (a != null) joueur.appuyerDirection(a);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Action a = mapTouche(e.getKeyCode());
        if (a != null) joueur.relacherDirection(a);
    }

    private Action mapTouche(int keyCode) {
        return switch (keyCode) {
            case KeyEvent.VK_UP,    KeyEvent.VK_Z -> Action.HAUT;
            case KeyEvent.VK_DOWN,  KeyEvent.VK_S -> Action.BAS;
            case KeyEvent.VK_LEFT,  KeyEvent.VK_Q -> Action.GAUCHE;
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> Action.DROITE;
            default -> null;
        };
    }
}
