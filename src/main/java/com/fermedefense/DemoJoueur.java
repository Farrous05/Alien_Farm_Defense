package com.fermedefense;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.fermedefense.modele.jeu.Carte;
import com.fermedefense.modele.joueur.Action;
import com.fermedefense.modele.joueur.Joueur;

/**
 * Test simple : un carré se déplace avec les flèches.
 * Un Thread met à jour la position du joueur en boucle.
 */
public class DemoJoueur extends JPanel implements KeyListener {

    private final Joueur joueur;
    private final Carte carte;
    private volatile boolean actif;

    public DemoJoueur() {
        setPreferredSize(new Dimension(600, 400));
        setBackground(Color.BLACK);

        carte = new Carte(600, 400);
        joueur = new Joueur(285, 185, 150, 100, 500);
        actif = true;

        // Thread qui met à jour la position du joueur
        Thread t = new Thread(() -> {
            long dernier = System.currentTimeMillis();
            while (actif) {
                long now = System.currentTimeMillis();
                joueur.mettreAJour(now - dernier);
                carte.clampJoueur(joueur);
                dernier = now;
                try { Thread.sleep(16); } catch (InterruptedException e) { break; }
            }
        }, "Thread-Joueur");
        t.setDaemon(true);
        t.start();

        setFocusable(true);
        addKeyListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Dessiner le joueur
        g.setColor(Color.BLUE);
        g.fillRect((int) joueur.getX(), (int) joueur.getY(), joueur.getTaille(), joueur.getTaille());

        // Afficher la position
        g.setColor(Color.WHITE);
        g.drawString("Pos: (" + (int) joueur.getX() + ", " + (int) joueur.getY() + ")", 10, 20);
        g.drawString("Fleches pour bouger", 10, 390);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:    joueur.appuyerDirection(Action.HAUT);    break;
            case KeyEvent.VK_DOWN:  joueur.appuyerDirection(Action.BAS);     break;
            case KeyEvent.VK_LEFT:  joueur.appuyerDirection(Action.GAUCHE);  break;
            case KeyEvent.VK_RIGHT: joueur.appuyerDirection(Action.DROITE);  break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Action dir = null;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:    dir = Action.HAUT;    break;
            case KeyEvent.VK_DOWN:  dir = Action.BAS;     break;
            case KeyEvent.VK_LEFT:  dir = Action.GAUCHE;  break;
            case KeyEvent.VK_RIGHT: dir = Action.DROITE;  break;
        }
        if (dir != null) joueur.relacherDirection(dir);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Test Joueur");
            DemoJoueur demo = new DemoJoueur();
            f.add(demo);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            new Timer(16, e -> demo.repaint()).start();
            f.setVisible(true);
        });
    }
}
