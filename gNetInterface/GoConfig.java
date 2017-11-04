package gNetInterface;

import gNetMenu.*;
import gNetwork.*;
import gNetUtil.GoFont;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class GoConfig extends JPanel implements ActionListener {
    private String   handiStr    = " Handicap";
    private String[] handiNOStrs = {" Other Part First", " 2 Stones", " 3 Stones", " 4 Stones", " 5 Stones", 
                                    " 6 Stones", " 8 Stones", " 9 Stones", " 13 Stones", " 17 Stones"};
    private int[]    handiNOInts = {1, 2, 3, 4, 5, 6, 8, 9, 13, 17};
    private String   blackStr    = " As Black Part";
    private String   whiteStr    = " As White Part";
    private String   submitStr   = "Submit";

    private JRadioButton blackButton, whiteButton;
    private JCheckBox    handiButton;
    private JComboBox<String> handiNOBox;
    private JButton      submitButton;
    private boolean      oldActive;
  
    private int handiNum  = 0;  // no handi
    private int playIndex = 0;  // 0: black; 1; white.
    private int handiIndex = -1;
    private boolean isSelected = false;

    private boolean     isChanged = false;
    private GoBoard     goBoard;
    private GoNetClient goNetClient;

    public GoConfig(GoBoard goBoard, GoNetClient goNetClient) {
        this.goBoard     = goBoard;
        this.goNetClient = goNetClient;

        // Step 1: Create all the components.
        blackButton = new JRadioButton(blackStr);
        blackButton.setFont(GoFont.buttonFont);
        blackButton.setSelected(true);
        blackButton.setEnabled(false);
       
        whiteButton = new JRadioButton(whiteStr);
        whiteButton.setFont(GoFont.buttonFont);
        whiteButton.setEnabled(false);
     
        handiButton = new JCheckBox(handiStr);
        handiButton.setFont(GoFont.buttonFont);
        handiButton.setEnabled(false);

        handiNOBox = new JComboBox<String>(handiNOStrs);
        handiNOBox.setFont(GoFont.buttonFont);
        handiNOBox.setMaximumRowCount(4);
        handiNOBox.setSelectedIndex(0);
        handiNOBox.setEnabled(false);

        submitButton = new JButton(submitStr);
        submitButton.setFont(GoFont.buttonFont);
        submitButton.setActionCommand(submitStr);
        submitButton.setEnabled(false);

        // Step 2: Group the radio buttons.
        ButtonGroup group1 = new ButtonGroup();
        group1.add(blackButton);
        group1.add(whiteButton);

        // Step 3: Register a listener for all the components.
        blackButton.addActionListener(this);
        whiteButton.addActionListener(this);
 
        handiButton.addActionListener(this);
        handiNOBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                handiIndex = cb.getSelectedIndex();
            }
        });
 
        submitButton.addActionListener(this);

        // Step 4: Lay all the components in this panel
        setLayout(new GridLayout(3, 1, 0, 8));
        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel1.add(blackButton);  panel1.add(whiteButton);
        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel2.add(handiButton);  panel2.add(handiNOBox);
        JPanel panel3 = new JPanel();
        panel3.add(submitButton);
        add(panel1);
        add(panel2);
        add(panel3);
    }

    // Listens to the components.
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(blackStr)) {
            playIndex = 0;
        }
        else if (e.getActionCommand().equals(whiteStr)) {
            playIndex = 1;
        }
        else if (e.getActionCommand().equals(handiStr)) {
            isSelected = !isSelected;
            handiNOBox.setEnabled(!handiNOBox.isEnabled());
        }
        else if (e.getActionCommand().equals(submitStr)) {
            configSubmit();    
        }
        isChanged = true;
    }

    public boolean isChanged() {
        return isChanged;
    }

    public void setChanged(boolean changed) {
        isChanged = changed;
    }

    private void configSubmit() {
        if (isSelected) {
            if (handiIndex != -1) {
                handiNum = handiNOInts[handiIndex];
                goBoard.setHandiNum(handiNum);
            }
            else handiNum = 0;
        }
        else handiNum = 0;

        if ((handiNum == 0) || (handiNum == 1)) {
            if (playIndex == 0) goBoard.setMyTurn(true);
            else goBoard.setMyTurn(false);
        }
        else {
            if (playIndex == 0) goBoard.setMyTurn(false);
            else goBoard.setMyTurn(true);
        }

        GoNetObject sentObject = new GoNetObject(GoNetConstants.CONFIG, playIndex, handiNum, isSelected);
        goNetClient.sendToServer(sentObject);

        GoMenu.setMIStatus(GoMenuConstants.NEWGAMESTR, true);
        setButtonActive(false);
    }

    public int getPlayPart() {
        return playIndex;
    }

    public int getHandiNum() {
        return handiNum;  
    }

    public boolean getSelected() {
        return isSelected;
    }

    public void setHandiNum(int i) {
        handiNum = i;
    }

    public void setPlayPart(int i) {
        playIndex = i;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public void selectBlackButton(boolean selected) {
        blackButton.setSelected(selected);
    }

    public void selectWhiteButton(boolean selected) {
        whiteButton.setSelected(selected);
    }

    public void selectHandiButton(boolean selected) {
        handiButton.setSelected(isSelected);
    }

    public void selectHandiNOBox(int i) {
        handiNOBox.setSelectedIndex(i);
    }

    public void setNetClient(GoNetClient goNetClient) {
        this.goNetClient = goNetClient;
    }

    public void setButtonActive(boolean isActive) {
        oldActive = blackButton.isEnabled();
        blackButton.setEnabled(isActive);
        whiteButton.setEnabled(isActive);
        handiButton.setEnabled(isActive);
        submitButton.setEnabled(isActive);
        if (isActive && handiButton.isSelected()) handiNOBox.setEnabled(true);
        else handiNOBox.setEnabled(false);
    }

    public void restoreButtonStatus() {
        blackButton.setEnabled(oldActive);
        whiteButton.setEnabled(oldActive);
        handiButton.setEnabled(oldActive);
        submitButton.setEnabled(oldActive);
        if (oldActive && handiButton.isSelected()) handiNOBox.setEnabled(true);
        else handiNOBox.setEnabled(false);
    }
}