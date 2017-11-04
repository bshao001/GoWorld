package gNetUtil;

import java.awt.Color;
import java.util.*;

public class GoButton {
    public int        centerX;
    public int        centerY;
    public Color      buttonColor;
    public GoButton[] neighbors;   //Just Save Current neighbors
    public int        live;
    public boolean    isDeleted;
    public boolean    isDead;      //Used for Count Points Alive
    public boolean    isLastButton;
    public boolean    isCauseButton;
    public GoButton   becauseOf;   //This goButton is deleted becaue of the becauseOf goButton.
    
    private static boolean[][] isChecked = new boolean[19][19];

    public GoButton(int centerX, int centerY, Color buttonColor) {
        this.centerX     = centerX;
        this.centerY     = centerY;
        this.buttonColor = buttonColor;
        neighbors        = null;
        live             = 4;
        isDeleted        = false;
        isDead           = false;
        isLastButton     = false;
        isCauseButton    = false;
        becauseOf        = null;
    }

    public boolean canBePlayed() {
        boolean retB = false;

        if (isLive())  retB = true;
        else {
            for (int i = 0; i < 4; i++) {
                if ((neighbors[i] != null) && (neighbors[i].buttonColor != buttonColor)) {
                    if ((!(neighbors[i].isLastButton && neighbors[i].isCauseButton)) &&
                        (!neighbors[i].isLive())) { // Not Da Jie
                        retB = true;
                        break;
                    }
                } 
            }
        }
        return retB;
    }

    public boolean isLive() {
        for (int i = 0; i < 19; i++) for (int j = 0; j < 19; j++)
            isChecked[i][j] = false;

        return innerIsLive();
    }
  
    private boolean innerIsLive() {
        boolean retB = false;

        isChecked[centerX][centerY] = true;

        if (live > 0) retB = true;
        else {
            for (int i = 0; i < 4; i++) {
                if ((neighbors[i] != null) && (!isChecked[neighbors[i].centerX][neighbors[i].centerY])) {
                    if ((neighbors[i].buttonColor == buttonColor) && (neighbors[i].innerIsLive())) {
                        retB = true;
                        break;
                    }
                }  
            }
        }
        return retB;
    }

    public void selfDelete(Vector buttonVect, GoButton[][] playButtons, GoButton causeButton) {
        for (int i = 0; i < buttonVect.size(); i++) {
            GoButton tempButton = (GoButton)buttonVect.elementAt(i);
            if (equals(tempButton)) {
                //tempButton.live = 0;  unnecessary
                tempButton.isDeleted = true;
                tempButton.becauseOf = causeButton;
            }              
        }        
        playButtons[centerX][centerY] = null;
        for (int i = 0; i < 4; i++) {
            if (neighbors[i] != null) {
                for (int j = 0; j < 4; j++) {
                    if ((neighbors[i].neighbors[j] != null) &&
                        (equals(neighbors[i].neighbors[j]))) {
                        neighbors[i].neighbors[j] = null;
                        break;
                    }
                }
                if (neighbors[i].buttonColor != buttonColor) neighbors[i].live++;
            }
        }

        //Recursive Delete
        for (int i = 0; i < 4; i++) {
            if (neighbors[i] != null) {
                if ((neighbors[i].buttonColor == buttonColor) && (neighbors[i].live == 0)) {
                    neighbors[i].selfDelete(buttonVect, playButtons, causeButton);
                }
                neighbors[i] = null;
            }
        }
    }
}