package gNetInterface;

import gNetwork.*;
import gNetUtil.GoFont;
import gNetMenu.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class GoSystemArea extends JPanel implements ActionListener {
    private String agreeStr    = "Agree";
    private String disagreeStr = "Disagree";

    private JLabel      upLabel;    
    private JTextArea   textArea;
    private JScrollPane textPane;
    private JButton     agreeButton, disagreeButton;

    private GoNetClient goNetClient;
    private GoBoard     goBoard;
    private GoConfig    goConfig;
    private String      answerWhat = null;

    public GoSystemArea(String labelStr, int xNum, int yNum, 
        GoNetClient goNetClient, GoBoard goBoard, GoConfig goConfig) {
        this.goNetClient = goNetClient;
        this.goBoard     = goBoard;
        this.goConfig    = goConfig; 
        
        upLabel = new JLabel(labelStr);
        upLabel.setFont(GoFont.labelFont);

        textArea = new JTextArea(xNum, yNum);
        textArea.setMargin(new Insets(5,5,5,5));
        textArea.setEditable(false);
        textArea.setFont(GoFont.systemAreaFont);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(Color.gray);
        textArea.setSelectionColor(Color.gray);  //To remove some undesirable effect.
        textArea.setSelectedTextColor(Color.black);  //To remove some undesirable effect.
        textPane = new JScrollPane(textArea);

        agreeButton  = new JButton(agreeStr);
        agreeButton.setFont(GoFont.buttonFont);
        agreeButton.setActionCommand(agreeStr);
        agreeButton.setEnabled(false);
       
        disagreeButton = new JButton(disagreeStr);
        disagreeButton.setFont(GoFont.buttonFont);
        disagreeButton.setActionCommand(disagreeStr);
        disagreeButton.setEnabled(false);

        agreeButton.addActionListener(this);
        disagreeButton.addActionListener(this);

        setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(agreeButton);  buttonPanel.add(new JPanel());  buttonPanel.add(disagreeButton);
        add(upLabel, BorderLayout.NORTH);
        add(textPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(agreeStr)) {
            GoNetObject sentObject = new GoNetObject(GoNetConstants.ANSWER, GoNetConstants.AGREE, answerWhat);
            goNetClient.sendToServer(sentObject);

            setButtonActive(false);
            if (sentObject.answerWhat.equals(GoNetConstants.COUNTSTR)) {
                String str = "Please wait your partner to mark the dead buttons.";
                append(str);
            } else {
                agree(sentObject);

                if (sentObject.answerWhat.equals(GoNetConstants.CONTINUESTR)) {
                    GoMenu.setMIStatus(GoMenuConstants.NEWGAMESTR, false);
                    GoMenu.setMIStatus(GoMenuConstants.CONTINUESTR, false);
                    GoMenu.setMIStatus(GoMenuConstants.BACKGOSTR, false);
                    GoMenu.setMIStatus(GoMenuConstants.COUNTSTR, false);

                    String str = "Please wait your partner to open the file.";
                    append(str);            
                }
            }
        }
        else if (e.getActionCommand().equals(disagreeStr)) {
            GoMenu.restoreNetMenus();

            GoNetObject sentObject = new GoNetObject(GoNetConstants.ANSWER, GoNetConstants.DISAGREE, answerWhat);
            goNetClient.sendToServer(sentObject);

            setButtonActive(false);
        }
    }

    public void agree(GoNetObject netObject) {  //being used for two parts
        GoMenu.restoreNetMenus();  // Pay attention to this line's position in this method.

        if (netObject.answerWhat.equals(GoNetConstants.NEWGAMESTR)) {
            GoMenu.setMIStatus(GoMenuConstants.NEWGAMESTR, false);
            GoMenu.setMIStatus(GoMenuConstants.BACKGOSTR, false);
            goConfig.setButtonActive(true);
            goBoard.reset();
        }
        else if (netObject.answerWhat.equals(GoNetConstants.CONTINUESTR)) {
            GoMenu.setMIStatus(GoMenuConstants.BACKGOSTR, false);
            goBoard.reset();
        }
        else if (netObject.answerWhat.equals(GoNetConstants.BACKGOSTR)) {
            int i = goBoard.oneStepBack();
            if (i == 0) {  // No buttons in the board 
                GoMenu.setMIStatus(GoMenuConstants.BACKGOSTR, false);
                GoMenu.setMIStatus(GoMenuConstants.SAVEASSTR, false);
            }
        }
        else if (netObject.answerWhat.equals(GoNetConstants.COUNTSTR)) {
            /* Do nothing special here */
        }
    }

    public void disagree(GoNetObject netObject) {  //only being used by the other part
        GoMenu.restoreNetMenus();
       
        //GoNetConstants.NEWGAMESTR, CONTINUESTR, BACKGOSTR, COUNTSTR
        String str = "Sorry, your partner don't agree to " + netObject.answerWhat + ".";
        append(str);
    }

    public void setAnswerWhat(String answerWhat) {
        this.answerWhat = answerWhat; 
    }

    public void setNetClient(GoNetClient goNetClient) {
        this.goNetClient = goNetClient;
    }

    public void setButtonActive(boolean isActive) {
        agreeButton.setEnabled(isActive);
        disagreeButton.setEnabled(isActive);
    }

    public void append(String str) {
        textArea.setEditable(true);
        textArea.selectAll();
        textArea.replaceSelection(str);
        textArea.setEditable(false);
    }
}