/*
 * Created on 20.10.2006
 */

package voidchess.ui;

import voidchess.board.ChessGameInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Chess960Panel
        extends JPanel
        implements ActionListener {
    private static final int CLASSIC_CHESS_POSITION = 518;
    private JButton classicButton, random960Button;
    private JTextField positionIndexField;
    private int positionIndex;

    private ChessGameInterface game;
    private ChessboardComponent gameUI;

    Chess960Panel(ChessGameInterface game, ChessboardComponent gameUI) {
        this.game = game;
        this.gameUI = gameUI;
        designLayout();

        setPosition(CLASSIC_CHESS_POSITION);
    }

    private void designLayout() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(Color.WHITE);

        classicButton = new JButton("classic setup");
        random960Button = new JButton("shuffle setup");
        positionIndexField = new JTextField(Integer.toString(positionIndex), 3);

        classicButton.addActionListener(this);
        random960Button.addActionListener(this);
        positionIndexField.addActionListener(this);

        add(classicButton);
        add(random960Button);
        add(new JLabel(" current setup: "));
        add(positionIndexField);
    }

    int getPositionCode() {
        return positionIndex;
    }

    public void setEnabled(boolean enabled) {
        classicButton.setEnabled(enabled && positionIndex != CLASSIC_CHESS_POSITION);
        random960Button.setEnabled(enabled);
        positionIndexField.setEditable(enabled);
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == classicButton) {
            setPosition(CLASSIC_CHESS_POSITION);
        } else if (source == random960Button) {
            setPosition(getRandomPosition());
        } else if (source == positionIndexField) {
            setPosition(positionIndexField.getText());
        }
    }

    private void setPosition(int position) {
        positionIndex = position;
        positionIndexField.setText(Integer.toString(position));
        pullPosition(position);
    }

    private void setPosition(String position) {
        try {
            positionIndex = Integer.parseInt(position);
            if (positionIndex < 0) {
                setPosition(0);
            } else if (positionIndex > 959) {
                setPosition(959);
            } else {
                pullPosition(positionIndex);
            }
        } catch (NumberFormatException e) {
            setPosition(CLASSIC_CHESS_POSITION);
        }
    }

    private void pullPosition(int position) {
        assert position < 960 && position >= 0;

        if (position == CLASSIC_CHESS_POSITION) {
            classicButton.setEnabled(false);
        } else {
            classicButton.setEnabled(true);
        }

        game.initGame(position);
        gameUI.repaintAtOnce();
    }

    private int getRandomPosition() {
        return (int) Math.floor(Math.random() * 960);
    }
}
