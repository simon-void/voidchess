/*
 * Created on 11.09.2006
 */

package ui;

import board.ChessGame;
import board.ChessGameInterface;
import helper.ChessGameSupervisorDummy;
import player.HumanPlayer;
import player.PlayerInterface;
import player.ki.ComputerPlayer;
import player.ki.ComputerPlayerUI;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChessPanel
        extends JPanel
        implements ActionListener {
    final private static String startString = "Start";
    final private static String resignString = "Aufgeben";
    final private static String switchString = "Pl\u00E4tze tauschen";

    private PlayerInterface humanPlayer;
    private PlayerInterface computerPlayer;
    private Table table;
    private JButton startButton;
    private JButton switchButton;
    private ChessGameUI chessGameUI;
    private Chess960Panel panel960;
    private DifficultyPanel difficultyPanel;
    private CoresPanel coresPanel;
    private boolean humanPlaysWhite;

    ChessPanel() {
        designLayout();
    }

    private void designLayout() {
        startButton = new JButton(resignString);
        Dimension prefSize = startButton.getPreferredSize();
        startButton.setText(startString);
        startButton.setPreferredSize(prefSize);
        startButton.addActionListener(this);
        switchButton = new JButton(switchString);
        switchButton.addActionListener(this);
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(startButton);
        buttonPanel.add(switchButton);

        ChessGame game = new ChessGame(ChessGameSupervisorDummy.INSTANCE);
        chessGameUI = new ChessGameUI(game, this);
        panel960 = new Chess960Panel(game, chessGameUI);
        table = new Table(game, chessGameUI, this, panel960);
        game.useSupervisor(table);
        humanPlayer = new HumanPlayer(table, true, chessGameUI, game);
        ComputerPlayerUI computerPlayerUI = new ComputerPlayerUI();
        ComputerPlayer kiPlayer = new ComputerPlayer(table, game, computerPlayerUI);
        computerPlayer = kiPlayer;
        table.setWhitePlayer(humanPlayer);
        table.setBlackPlayer(computerPlayer);
        humanPlaysWhite = true;

        JPanel computerPlayerOptionsPanel = getComputerPlayerSettingsPanel(kiPlayer);

        JPanel computerUIPanel = new JPanel(new BorderLayout());
        computerUIPanel.setBackground(Color.WHITE);
        computerUIPanel.add(computerPlayerUI, BorderLayout.CENTER);
        computerUIPanel.add(computerPlayerOptionsPanel, BorderLayout.SOUTH);

        JPanel gameUIPanel = new JPanel();
        gameUIPanel.setBackground(Color.WHITE);
        gameUIPanel.add(chessGameUI);

        setBackground(Color.WHITE);
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        add(buttonPanel, layout, 1, 1);
        add(panel960, layout, 2, 1);
        add(computerUIPanel, layout, 1, 2);
        add(gameUIPanel, layout, 2, 2);
    }

    private JPanel getComputerPlayerSettingsPanel(ComputerPlayer computerPlayer) {
        difficultyPanel = new DifficultyPanel(computerPlayer);
        coresPanel = new CoresPanel(computerPlayer);

        if(!coresPanel.hasOptions()) {
            return difficultyPanel;
        }

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new LineBorder(Color.LIGHT_GRAY));
        panel.setBackground(Color.WHITE);
        panel.add(difficultyPanel.getLabel());
        panel.add(difficultyPanel);
        panel.add(coresPanel.getLabel());
        panel.add(coresPanel);

        return panel;
    }

    private void add(JComponent component, GridBagLayout layout, int x, int y) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        layout.setConstraints(component, constraints);
        add(component);
    }

    public void actionPerformed(ActionEvent e) {
        String com = e.getActionCommand();
        if (com.equals(startString)) {
            start();
        } else if (com.equals(switchString)) {
            switchPlayer();
        } else if (com.equals(resignString)) {
            stop(ChessGameInterface.RESIGN);
        }
    }

    private void start() {
        startButton.setText(resignString);
        switchButton.setEnabled(false);
        panel960.setEnabled(false);
        difficultyPanel.setEnabled(false);
        coresPanel.setEnabled(false);
        table.startGame();
    }

    void stop(int endoption) {
        table.stopGame(endoption);
    }

    void gameover(int endoption) {
        switch (endoption) {
            case ChessGameInterface.DRAW:
                JOptionPane.showMessageDialog(this, "Unentschieden");
                break;
            case ChessGameInterface.PATT:
                JOptionPane.showMessageDialog(this, "Patt");
                break;
            case ChessGameInterface.MATT:
                JOptionPane.showMessageDialog(this, "Matt");
                break;
            case ChessGameInterface.THREE_TIMES_SAME_POSITION:
                JOptionPane.showMessageDialog(this, "Unentschieden wegen \ndreimaliger Stellungswiederholung");
                break;
            case ChessGameInterface.FIFTY_MOVES_NO_HIT:
                JOptionPane.showMessageDialog(this, "Unentschieden,\nda 50 Züge keine Figure geschlagen wurde");
                break;
            case ChessGameInterface.RESIGN:
                JOptionPane.showMessageDialog(this, "Spieler hat aufgegeben");
                break;
            case ChessApplet.APPLET_STOPPED:
                break;
        }
        startButton.setText(startString);
        switchButton.setEnabled(true);
        panel960.setEnabled(true);
        difficultyPanel.setEnabled(true);
        coresPanel.setEnabled(true);
    }

    private void switchPlayer() {
        humanPlaysWhite = !humanPlaysWhite;
        if (humanPlaysWhite) {
            table.setWhitePlayer(humanPlayer);
            table.setBlackPlayer(computerPlayer);
        } else {
            table.setWhitePlayer(computerPlayer);
            table.setBlackPlayer(humanPlayer);
        }
        humanPlayer.setColor(humanPlaysWhite);
        computerPlayer.setColor(!humanPlaysWhite);
        chessGameUI.setView(humanPlaysWhite);
    }
}
