/*
 * Created on 01.11.2006
 */

package ui;

import board.ChessGameInterface;
import player.ki.ComputerPlayer;
import player.ki.SimplePruner;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DifficultyPanel
        extends JPanel
        implements ActionListener {
    final private static String LEVEL1_TEXT = "Stufe 1";
    final private static String LEVEL2_TEXT = "Stufe 2";
    final private static String LEVEL3_TEXT = "Stufe 3";

    final private ComputerPlayer player;
    final private SimplePruner level_1_pruner, level_2_pruner, level_3_pruner;

    final private JComboBox<String> comboBox;

    public DifficultyPanel(ComputerPlayer player) {
        //it's a good idea to have figureHitRadius bigger than chessRadius
        //else the ai tries to prevent material loss through bad chesses
        level_1_pruner = new SimplePruner(1, 4, 3);
        level_2_pruner = new SimplePruner(2, 3, 2);
        level_3_pruner = new SimplePruner(2, 4, 3);
        this.player = player;
        player.setSearchTreePruner(level_1_pruner);

        comboBox = new JComboBox<>();
        designLayout();
        //preselect the second option
        comboBox.setSelectedIndex(1);
    }

    private void designLayout() {
        setBackground(Color.WHITE);
//        setBorder(new LineBorder(Color.LIGHT_GRAY));

        comboBox.addItem(LEVEL1_TEXT);
        comboBox.addItem(LEVEL2_TEXT);
        comboBox.addItem(LEVEL3_TEXT);
        comboBox.setEditable(false);
        comboBox.addActionListener(this);

//        add(new JLabel("Schwierigkeitsgrad:"));
        add(comboBox);
    }

    public void setEnabled(boolean enable) {
        comboBox.setEnabled(enable);
    }

    public void actionPerformed(ActionEvent event) {
        if (comboBox.getSelectedItem().equals(LEVEL1_TEXT)) {
            player.setSearchTreePruner(level_1_pruner);
        } else if (comboBox.getSelectedItem().equals(LEVEL2_TEXT)) {
            player.setSearchTreePruner(level_2_pruner);
        } else if (comboBox.getSelectedItem().equals(LEVEL3_TEXT)) {
            player.setSearchTreePruner(level_3_pruner);
        }
    }

    public JLabel getLabel() {
        return new JLabel("Schwierigkeitsgrad:");
    }
}
