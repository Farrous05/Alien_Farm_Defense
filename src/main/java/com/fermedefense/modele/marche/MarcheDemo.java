
package com.fermedefense.modele.marche;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class MarcheDemo {
    public static void main(String[] args) {
        // Test console : achat réussi
        Marche marche = new Marche();
        JoueurMarche joueur = new JoueurMarche(100);
        FermeMarche ferme = new FermeMarche();
        ArticleMarche article = marche.getArticles().get(0);
        AchatResult res = marche.acheter(joueur, article, ferme);
        System.out.println("Test achat réussi : " + res.getMessage());
        // Test console : fonds insuffisants
        JoueurMarche joueur2 = new JoueurMarche(10);
        AchatResult res2 = marche.acheter(joueur2, article, ferme);
        System.out.println("Test fonds insuffisants : " + res2.getMessage());
        // Test console : article inexistant
        AchatResult res3 = marche.acheter(joueur, null, ferme);
        System.out.println("Test article inexistant : " + res3.getMessage());

        // Interface Swing
        SwingUtilities.invokeLater(() -> new MarcheUI(marche, joueur, ferme));
    }
}

class MarcheUI extends JFrame {
    private final Marche marche;
    private final JoueurMarche joueur;
    private final FermeMarche ferme;
    private final JLabel monnaieLabel;
    private final DefaultListModel<ArticleMarche> articleListModel;
    private final JList<ArticleMarche> articleJList;
    private final JButton acheterButton;
    private final JTextArea logArea;
    private final JProgressBar progressBar;

    public MarcheUI(Marche marche, JoueurMarche joueur, FermeMarche ferme) {
        super("Marché Autonome");
        this.marche = marche;
        this.joueur = joueur;
        this.ferme = ferme;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        monnaieLabel = new JLabel("Monnaie : " + joueur.getMonnaie());
        monnaieLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(monnaieLabel, BorderLayout.NORTH);

        articleListModel = new DefaultListModel<>();
        for (ArticleMarche a : marche.getArticles()) articleListModel.addElement(a);
        articleJList = new JList<>(articleListModel);
        articleJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        articleJList.setCellRenderer(new ArticleRenderer());
        JScrollPane scrollPane = new JScrollPane(articleJList);
        add(scrollPane, BorderLayout.CENTER);

        acheterButton = new JButton("Acheter");
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(acheterButton, BorderLayout.NORTH);

        progressBar = new JProgressBar(0, 100);
        progressBar.setVisible(false);
        rightPanel.add(progressBar, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        logArea = new JTextArea(6, 40);
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        add(logScroll, BorderLayout.SOUTH);

        acheterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int idx = articleJList.getSelectedIndex();
                if (idx < 0) {
                    log("Sélectionnez un article.");
                    return;
                }
                ArticleMarche article = articleListModel.get(idx);
                acheterButton.setEnabled(false);
                progressBar.setVisible(true);
                progressBar.setValue(0);
                log("Achat en cours...");
                new SwingWorker<Void, Integer>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        for (int i = 0; i <= 100; i += 10) {
                            Thread.sleep(100);
                            publish(i);
                        }
                        return null;
                    }
                    @Override
                    protected void process(List<Integer> chunks) {
                        progressBar.setValue(chunks.get(chunks.size() - 1));
                    }
                    @Override
                    protected void done() {
                        AchatResult res = marche.acheter(joueur, article, ferme);
                        monnaieLabel.setText("Monnaie : " + joueur.getMonnaie());
                        log(res.getMessage() + " (Monnaie restante : " + res.getMonnaieRestante() + ")");
                        log("Inventaire armes : " + joueur.getArmes());
                        log("Vaches : " + ferme.getVaches());
                        acheterButton.setEnabled(true);
                        progressBar.setVisible(false);
                    }
                }.execute();
            }
        });

        setVisible(true);
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    static class ArticleRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            ArticleMarche a = (ArticleMarche) value;
            String txt = a.getNom() + " | " + a.getType() + " | Prix: " + a.getPrix() + " | Puissance: " + a.getPuissance();
            return super.getListCellRendererComponent(list, txt, index, isSelected, cellHasFocus);
        }
    }
}
