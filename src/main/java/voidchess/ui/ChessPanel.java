package voidchess.ui;

import voidchess.board.ChessGame;
import voidchess.board.MoveResult;
import voidchess.helper.ChessGameSupervisorDummy;
import voidchess.player.HumanPlayer;
import voidchess.player.PlayerInterface;
import voidchess.player.ki.ComputerPlayer;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class ChessPanel
        extends JPanel
        implements ActionListener {
    final private static String startString = "start";
    final private static String resignString = "resign";
    final private static String switchString = "change seats";

    private PlayerInterface humanPlayer;
    private PlayerInterface computerPlayer;
    private Table table;
    private JButton startButton;
    private JButton switchButton;
    private ChessboardComponent chessboardComponent;
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
        chessboardComponent = new ChessboardComponent(game, this);
        panel960 = new Chess960Panel(game, chessboardComponent);
        table = new Table(game, chessboardComponent, this, panel960);
        game.useSupervisor(table);
        humanPlayer = new HumanPlayer(table, true, chessboardComponent, game);
        ComputerPlayerComponent computerPlayerUI = new ComputerPlayerComponent();
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
        gameUIPanel.add(chessboardComponent);

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

        if (!coresPanel.hasOptions()) {
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
        switch (com) {
            case startString:
                start();
                break;
            case switchString:
                switchPlayer();
                break;
            case resignString:
                table.stopGame(MoveResult.RESIGN);
                break;
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

    void gameover(MoveResult endoption) {
        switch (endoption) {
            case DRAW:
                JOptionPane.showMessageDialog(this, "draw");
                break;
            case STALEMATE:
                JOptionPane.showMessageDialog(this, "stalemate");
                break;
            case CHECKMATE:
                JOptionPane.showMessageDialog(this, "checkmate");
                break;
            case THREE_TIMES_SAME_POSITION:
                JOptionPane.showMessageDialog(this, "draw because of\nthreefold repetition");
                break;
            case FIFTY_MOVES_NO_HIT:
                JOptionPane.showMessageDialog(this, "draw because of\nfifty-move rule");
                break;
            case RESIGN:
                JOptionPane.showMessageDialog(this, "player has resigned");
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
        chessboardComponent.setViewPoint(humanPlaysWhite);
    }
}
