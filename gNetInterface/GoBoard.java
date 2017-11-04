package gNetInterface;

import gNetUtil.*;
import gNetMenu.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.Vector;

public class GoBoard extends JPanel implements ActionListener { //You can "extends Canvas" here to learn the differenece.
    private final int    boardThick    = 6;
    private final int    boardStart    = 20;
    private final int    initOuterGap  = 12;
    private final int    initPaneWidth = 24;

    private Vector<GoButton> buttonVect;   //Save history and Status.
    private GoButton[][] playButtons;  //Just Save Current Status.
    private GoButton     currentButton;

    private int          startX, startY;
    private int          outerGap, paneWidth, boardWidth;
    private double       starRadius;
    private Color        buttonColor;
    private int          handicapNum;
   
    private boolean      isChanged;
    private boolean      isMyTurn, oldMyTurn;

    private Timer        timer;
    private int          timeNum = 0;

    private Point        currentPosition = null;
    private Color        currentPositionColor = null;

    private Image        blackButImage, whiteButImage;

    private static boolean[][] isChecked = new boolean[19][19];
    private static int   blankNumber, blockType;

    public GoBoard() {
        startX      = boardStart;
        startY      = boardStart + boardThick;
        outerGap    = initOuterGap;
        paneWidth   = initPaneWidth;
        boardWidth  = outerGap * 2 + initPaneWidth * 18 + 1;

        starRadius  = initPaneWidth / 7;
        buttonColor = Color.black;
        handicapNum = 0;

        buttonVect  = new Vector<GoButton>(100, 10);
        playButtons = new GoButton[19][19];        
        for (int i = 0; i < 19; i++)  for (int j = 0; j < 19; j++) {
            playButtons[i][j] = null;   //is not played.
        } 
        currentButton = null;
        isChanged = false;
        isMyTurn = false;
      
        timer = new Timer(100, this);

        blackButImage = GoImage.load("go_black.jpg", this);
        whiteButImage = GoImage.load("go_white.jpg", this);
    }

    public void reset() {
        buttonColor = Color.black;
        handicapNum = 0;

        buttonVect.removeAllElements();
        for (int i = 0; i < 19; i++)  for (int j = 0; j < 19; j++) {
            playButtons[i][j] = null;   //is not played.
        } 
        currentButton = null;
        isChanged = false;
        isMyTurn = false;
        revalidate();
        repaint();
    }

    public Vector getButtonVect() {
        return buttonVect;
    }

    public boolean isChanged() {
        return isChanged;
    }

    public void setChanged(boolean changed) {
        isChanged = changed;
    }

    public boolean isMyTurn() {
        return isMyTurn;
    }

    public void setMyTurn(boolean myTurn) {
        oldMyTurn = isMyTurn;
        isMyTurn = myTurn;
    }

    public void restoreMyTurn() {
        isMyTurn = oldMyTurn;
    }

    private void playHandiButton(int x, int y) {
        Color goButtonColor = Color.black;
                       
        GoButton tempButton = new GoButton(x, y, goButtonColor);

        tempButton.neighbors = new GoButton[4];
        if (currentButton != null) currentButton.isLastButton = false;
        tempButton.isLastButton = true;
        tempButton.isDeleted = false;
        currentButton = tempButton;
        buttonVect.addElement(tempButton);
        playButtons[x][y] = tempButton;
    }
   
    public void setHandiNum(int hNum) {
        handicapNum = hNum;

        switch (handicapNum) {
            case 17 :
                playHandiButton(2, 9);  playHandiButton(16, 9);
                playHandiButton(9, 2);  playHandiButton(9, 16);
   
            case 13 :
                playHandiButton(2, 2);  playHandiButton(2, 16);
                playHandiButton(16, 2); playHandiButton(16, 16);

            case 9  :
                playHandiButton(9, 9);

            case 8  :
                playHandiButton(9, 3);  playHandiButton(9, 15);

            case 6  :
                playHandiButton(3, 9);  playHandiButton(15, 9);

            case 4  :
                playHandiButton(3, 15);

            case 3  :
                playHandiButton(15, 3);

            case 2  :
                playHandiButton(3, 3);  playHandiButton(15, 15);
                break;

            case 5  :
                playHandiButton(3, 3);  playHandiButton(3, 15);
                playHandiButton(15, 3); playHandiButton(15, 15);
                playHandiButton(9, 9);
                break;

            case 1  :
                break;

            default :
                break;
        }
        revalidate();
        repaint();
    }    

    public Point inValidArea(Point pressPoint) {
         int x = pressPoint.x - startX;
         int y = pressPoint.y - startY;
         int r = paneWidth / 3 - 1;
         int w = paneWidth;
 
         if (((x >= -r) && (x <= boardWidth - 2 * outerGap - 1 + r)) &&
             ((y >= -r) && (y <= boardWidth - 2 * outerGap - 1 + r))) {
             x = Math.abs(x);
             y = Math.abs(y);
             int remainX = x % w;
             int remainY = y % w;
             int quotX = remainX > w / 2 ? x / w + 1 : x / w;
             int quotY = remainY > w / 2 ? y / w + 1 : y / w;
      
             int x1 = x - quotX * w;
             int y1 = y - quotY * w;
   
             int area = x1 * x1 + y1 * y1;
                    
             if (area <= r * r) return (new Point(quotX, quotY));
        }
        return null;
    } 

    public void indicateCurrentPosition() {
        currentPosition = null;
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        repaint();
    }

    public void indicateCurrentPosition(Point pressPoint, int whoseTurn) {
        int x = pressPoint.x;
        int y = pressPoint.y;

        if (playButtons[x][y] == null) {
            Color goButtonColor;
            if (whoseTurn == 0) goButtonColor = Color.black;
            else goButtonColor = Color.white;
                       
            //This goButton still can't be decided to be useful, so tempButton is constructed.
            GoButton tempButton = createTempButton(x, y, goButtonColor);            

            currentPosition = new Point(x, y);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            if (tempButton.canBePlayed()) {
                currentPositionColor = Color.green;
                repaint();
            } else {
                currentPositionColor = Color.red;
                repaint();
            }

            restorePlayButtons(x, y);
        }
        else indicateCurrentPosition();
    }

    public void indicateCurrentPositionForCount(Point pressPoint, boolean isDead) {
        int x = pressPoint.x;
        int y = pressPoint.y;

        currentPosition = new Point(x, y);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if ((playButtons[x][y] != null) && (playButtons[x][y].isDead != isDead)) {
            currentPositionColor = Color.green;
        } else {
            currentPositionColor = Color.red;
        }
        repaint();
    }

    public boolean oneStepPlay(Point pressPoint, int whoseTurn) {
        boolean retB = false;

        int x = pressPoint.x;
        int y = pressPoint.y;
        if (playButtons[x][y] == null) {
            Color goButtonColor;
            if (whoseTurn == 0) goButtonColor = Color.black;
            else goButtonColor = Color.white;
                       
            //This goButton still can't be decided to be useful, so tempButton is constructed.
            GoButton tempButton = createTempButton(x, y, goButtonColor);

            if (tempButton.canBePlayed()) {
                retB = true;
                isMyTurn = !isMyTurn;
                isChanged = true;
                GoMenu.setMIStatus(GoMenuConstants.BACKGOSTR, true);
                GoMenu.setMIStatus(GoMenuConstants.SAVESTR, true);
                GoMenu.setMIStatus(GoMenuConstants.SAVEASSTR, true);

                if (currentButton != null) currentButton.isLastButton = false;
                tempButton.isLastButton = true;
                tempButton.isDeleted = false;
                currentButton = tempButton;
                buttonVect.addElement(tempButton);
                if (buttonVect.size() > 20) GoMenu.setMIStatus(GoMenuConstants.COUNTSTR, true);
                playButtons[x][y] = tempButton;
                currentPosition = null;  //do not indicate position at this time.
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                for (int i = 0; i < 4; i++) {
                    if (tempButton.neighbors[i] != null) {
                        GoButton neighborB = tempButton.neighbors[i]; 
                        if ((neighborB.buttonColor != tempButton.buttonColor) && (!neighborB.isLive())) {
                            neighborB.selfDelete(buttonVect, playButtons, tempButton);
                            tempButton.isCauseButton = true;  
                        }
                    }
                }

                if (!isMyTurn) stopTimer();
                else startTimer();
                repaint();
            }
            else restorePlayButtons(x, y);
        }
        return retB;
    }

    private GoButton createTempButton(int x, int y, Color goButtonColor) {
        GoButton tempButton = new GoButton(x, y, goButtonColor);

        tempButton.neighbors = new GoButton[4];
        if (x == 0) tempButton.live--;
        else if (playButtons[x - 1][y] != null) {
            tempButton.neighbors[0] = playButtons[x - 1][y];
            tempButton.live--;
            playButtons[x - 1][y].neighbors[1] = tempButton;
            playButtons[x - 1][y].live--;
        } 
        if (x == 18) tempButton.live--;
        else if (playButtons[x + 1][y] != null) {
            tempButton.neighbors[1] = playButtons[x + 1][y];
            tempButton.live--;
            playButtons[x + 1][y].neighbors[0] = tempButton;
            playButtons[x + 1][y].live--;
        }
        if (y == 0) tempButton.live--;
        else if (playButtons[x][y - 1] != null) {
            tempButton.neighbors[2] = playButtons[x][y - 1];
            tempButton.live--;
            playButtons[x][y - 1].neighbors[3] = tempButton;
            playButtons[x][y - 1].live--;
        } 
        if (y == 18) tempButton.live--;
        else if (playButtons[x][y + 1] != null) {
            tempButton.neighbors[3] = playButtons[x][y + 1];
            tempButton.live--;
            playButtons[x][y + 1].neighbors[2] = tempButton;
            playButtons[x][y + 1].live--;
        }

        return tempButton;
    }

    private void restorePlayButtons(int x, int y) {
        if ((x - 1 >= 0) && (playButtons[x - 1][y] != null)) {
            playButtons[x - 1][y].neighbors[1] = null;
            playButtons[x - 1][y].live++;
        } 
        if ((x + 1 <= 18) && (playButtons[x + 1][y] != null)) {
            playButtons[x + 1][y].neighbors[0] = null;
            playButtons[x + 1][y].live++;
        }
        if ((y - 1 >= 0) && (playButtons[x][y - 1] != null)) {
            playButtons[x][y - 1].neighbors[3] = null;
            playButtons[x][y - 1].live++;
        } 
        if ((y + 1 <= 18) && (playButtons[x][y + 1] != null)) {
            playButtons[x][y + 1].neighbors[2] = null;
            playButtons[x][y + 1].live++;
        }
    }

    public int oneStepBack() {
        GoButton lastButton = (GoButton)buttonVect.lastElement();
        buttonVect.remove(lastButton);
        playButtons[lastButton.centerX][lastButton.centerY] = null;
        isMyTurn = !isMyTurn;
        isChanged = true;

        GoMenu.setMIStatus(GoMenuConstants.SAVESTR, true);
        GoMenu.setMIStatus(GoMenuConstants.SAVEASSTR, true);
        if (buttonVect.size() <= 20) GoMenu.setMIStatus(GoMenuConstants.COUNTSTR, false);
        
        for (int i = 0; i < 4; i++) {
            if (lastButton.neighbors[i] != null) {
                GoButton tempButton = lastButton.neighbors[i];
                for (int j = 0; j < 4; j++) {
                    if ((tempButton.neighbors[j] != null) &&
                        (tempButton.neighbors[j].equals(lastButton))) {
                        tempButton.neighbors[j] = null;
                        break;
                    }
                }
                tempButton.live++;
            }
        }

        //Restore the deleted part
        if (lastButton.isCauseButton) {
            for (int i = 0; i < buttonVect.size(); i++) {
                GoButton tempButton = (GoButton)buttonVect.elementAt(i);
                if ((tempButton.becauseOf != null) && 
                    (tempButton.becauseOf.equals(lastButton))) {
                    tempButton.isDeleted = false;
                    tempButton.becauseOf = null;
                    playButtons[tempButton.centerX][tempButton.centerY] = tempButton;
                    int x = tempButton.centerX;
                    int y = tempButton.centerY;
                    tempButton.live = 4;
                    if (x == 0) tempButton.live--;
                    else if (playButtons[x - 1][y] != null) {
                        tempButton.neighbors[0] = playButtons[x - 1][y];
                        tempButton.live--;
                        playButtons[x - 1][y].neighbors[1] = tempButton;
                        playButtons[x - 1][y].live--;
                    } 
                    if (x == 18) tempButton.live--;
                    else if (playButtons[x + 1][y] != null) {
                        tempButton.neighbors[1] = playButtons[x + 1][y];
                        tempButton.live--;
                        playButtons[x + 1][y].neighbors[0] = tempButton;
                        playButtons[x + 1][y].live--;
                    }
                    if (y == 0) tempButton.live--;
                    else if (playButtons[x][y - 1] != null) {
                        tempButton.neighbors[2] = playButtons[x][y - 1];
                        tempButton.live--;
                        playButtons[x][y - 1].neighbors[3] = tempButton;
                        playButtons[x][y - 1].live--;
                    } 
                    if (y == 18) tempButton.live--;
                    else if (playButtons[x][y + 1] != null) {
                        tempButton.neighbors[3] = playButtons[x][y + 1];
                        tempButton.live--;
                        playButtons[x][y + 1].neighbors[2] = tempButton;
                        playButtons[x][y + 1].live--;
                    }
                } 
            }             
        }   

        if (!isMyTurn) stopTimer();
        else startTimer();
        repaint();

        int returnVal = 1;
        //Restore the currentButton
        if (!buttonVect.isEmpty()) {
            if ((handicapNum > 1) && (buttonVect.size() <= handicapNum)) returnVal = 0;
            currentButton = (GoButton)buttonVect.lastElement();  //The last element shouldn't be in isDeleted status.
            currentButton.isLastButton = true;
        } else {
            currentButton = null;
            returnVal = 0;
        }
        return returnVal;
    }

    public boolean markDeadButtons(Point pressPoint, boolean isDead) {
        int x = pressPoint.x;
        int y = pressPoint.y;

        if (playButtons[x][y] != null) {
            markSelfAndNeighbors(playButtons[x][y], isDead);
            currentPosition = null;  //do not indicate position in this time.
            repaint();
            return true;
        } else return false;
    }

    private void markSelfAndNeighbors(GoButton seedButton, boolean isDead) {
        seedButton.isDead = isDead;
        for (int i = 0; i < 4; i++) {
            if ((seedButton.neighbors[i] != null) && 
                (seedButton.neighbors[i].buttonColor == seedButton.buttonColor) &&
                (seedButton.neighbors[i].isDead != isDead)) {
                markSelfAndNeighbors(seedButton.neighbors[i], isDead);
            }
        }
    }

    public GoPointsAlive countPointsAlive() {
        Vector<BlankBlock> blockVect = new Vector<BlankBlock>(10, 1);
        float blackPoints = 0.0f;
        float whitePoints = 0.0f;

        for (int i = 0; i < 19; i++) for (int j = 0; j < 19; j++) isChecked[i][j] = false;
        for (int i = 0; i < 19; i++) for (int j = 0; j < 19; j++) {
            if ((!isChecked[i][j]) &&
                ((playButtons[i][j] == null) ||
                 ((playButtons[i][j] != null) && (playButtons[i][j].isDead)))) {
                blockVect.addElement(createBlankBlock(i, j));
            }
        }

        for (int i = 0; i < blockVect.size(); i++) {
            BlankBlock blankBlock = (BlankBlock)blockVect.elementAt(i);
            if (blankBlock.blockType == 1) {
                blackPoints += blankBlock.blankNumber;
            } else if (blankBlock.blockType == 2) {
                whitePoints += blankBlock.blankNumber;
            } else if (blankBlock.blockType == 3) {
                float points = (float)(blankBlock.blankNumber / 2.0);
                blackPoints += points;
                whitePoints += points;
            }
        }

        for (int i = 0; i < buttonVect.size(); i++) {
            GoButton goButton = (GoButton)buttonVect.elementAt(i);
            if ((goButton.isDeleted) || (goButton.isDead)) {
                if (goButton.buttonColor == Color.black) {
                    whitePoints += 1;
                } else if (goButton.buttonColor == Color.white) {
                    blackPoints += 1;
                }
            }
        }

        return new GoPointsAlive(blackPoints, whitePoints);
    }

    private BlankBlock createBlankBlock(int i, int j) { 
    //isDead is treated as if blank, the same as isDeleted
        BlankBlock blankBlock = new BlankBlock(i, j);

        blankNumber = 0;
        blockType = 0;
        countSelfAndNeighbors(i, j);
        blankBlock.blockType = blockType;
        blankBlock.blankNumber = blankNumber;

        return blankBlock;
    }

    private void countSelfAndNeighbors(int x, int y) {
        isChecked[x][y] = true;
        blankNumber++;

        if ((x != 0) && !isChecked[x - 1][y]) {
            if ((playButtons[x - 1][y] == null) ||
                ((playButtons[x - 1][y] != null) && (playButtons[x - 1][y].isDead))) countSelfAndNeighbors(x - 1, y);
            else {
                if (playButtons[x - 1][y].buttonColor == Color.black) {
                    if ((blockType == 0) || (blockType == 1)) blockType = 1;
                    else if (blockType == 2) blockType = 3;
                }
                else if (playButtons[x - 1][y].buttonColor == Color.white) {
                    if ((blockType == 0) || (blockType == 2)) blockType = 2;
                    else if (blockType == 1) blockType = 3;
                }
            }
        }
        if ((x != 18) && !isChecked[x + 1][y]) {
            if ((playButtons[x + 1][y] == null) ||
                ((playButtons[x + 1][y] != null) && (playButtons[x + 1][y].isDead))) countSelfAndNeighbors(x + 1, y);
            else {
                if (playButtons[x + 1][y].buttonColor == Color.black) {
                    if ((blockType == 0) || (blockType == 1)) blockType = 1;
                    else if (blockType == 2) blockType = 3;
                }
                else if (playButtons[x + 1][y].buttonColor == Color.white) {
                    if ((blockType == 0) || (blockType == 2)) blockType = 2;
                    else if (blockType == 1) blockType = 3;
                }
            }
        }    
        if ((y != 0) && !isChecked[x][y - 1]) {
            if ((playButtons[x][y - 1] == null) ||
                ((playButtons[x][y - 1] != null) && (playButtons[x][y - 1].isDead))) countSelfAndNeighbors(x, y - 1);
            else {
                if (playButtons[x][y - 1].buttonColor == Color.black) {
                    if ((blockType == 0) || (blockType == 1)) blockType = 1;
                    else if (blockType == 2) blockType = 3;
                }
                else if (playButtons[x][y - 1].buttonColor == Color.white) {
                    if ((blockType == 0) || (blockType == 2)) blockType = 2;
                    else if (blockType == 1) blockType = 3;
                }
            }
        }
        if ((y != 18) && !isChecked[x][y + 1]) {
            if ((playButtons[x][y + 1] == null) ||
                ((playButtons[x][y + 1] != null) && (playButtons[x][y + 1].isDead))) countSelfAndNeighbors(x, y + 1);
            else {
                if (playButtons[x][y + 1].buttonColor == Color.black) {
                    if ((blockType == 0) || (blockType == 1)) blockType = 1;
                    else if (blockType == 2) blockType = 3;
                }
                else if (playButtons[x][y + 1].buttonColor == Color.white) {
                    if ((blockType == 0) || (blockType == 2)) blockType = 2;
                    else if (blockType == 1) blockType = 3;
                }
            }
        }
    }

    private void setParameters() {
        int x   = getWidth();
        int y   = getHeight();
        int len = y < x ? y : x;
        int pW  = (len - 2 * boardStart - boardThick - 2 - 1) / 19;
        
        starRadius = pW / 7;
        paneWidth  = pW;
        outerGap   = pW / 2;
        boardWidth = pW * 18 + 2 * outerGap + 1;
        startX     = (x - (boardWidth + 2 + boardThick)) / 2 + outerGap + 1;
        startY     = (y - (boardWidth + 2 + boardThick)) / 2 + boardThick + outerGap + 1;
    }
    
    public void actionPerformed(ActionEvent e) {
        timeNum = (timeNum + 1) % 31;
        repaint();
    }

    public void stopTimer() {
        if (timer.isRunning()) timer.stop();
    }

    public void startTimer() {
        timeNum = 0;
        if (timer.isRunning()) timer.restart();
        else timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D gr = (Graphics2D)g;

        setParameters();

        //Set goBoard inner part background.
        gr.setColor(Color.orange);
        gr.fillRect(startX - outerGap + 1, startY - outerGap + 1, boardWidth - 2, boardWidth - 2);
        
        //Draw the lines of the board.
        gr.setColor(Color.black);
        for (int i = 0; i < 19; i++) {
            int eX = startX + boardWidth - (2 * outerGap) - 1;
            int y  = startY + i * paneWidth;
            gr.drawLine(startX, y, eX, y);
        }      

        for (int i = 0; i < 19; i++) {
            int x  = startX + i * paneWidth;
            int eY = startY + boardWidth - (2 * outerGap) - 1;
            gr.drawLine(x, startY, x, eY);
        }
       
        //Paint the nine star points.
        for (int i = 3; i <= 15 ; i += 6) {
            for (int j = 3; j <= 15; j += 6) {
                double x = startX + i * paneWidth - starRadius;
                double y = startY + j * paneWidth - starRadius;
                double d = 2 * starRadius + 1;
                gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                gr.fill(new java.awt.geom.Ellipse2D.Double(x, y, d, d));
            }
        }

        //Draw the outer lines of the goBoard.
        gr.drawRect(startX - outerGap - 1, startY - outerGap - 1, boardWidth + 1, boardWidth + 1); 
        gr.drawRect(startX - outerGap, startY - outerGap, boardWidth - 1, boardWidth - 1);
        
        gr.setColor(Color.gray);
        for (int i = 1; i <= boardThick; i++) {  //Make the board 3Dimensional.
            int x = startX - outerGap - 1 + i;
            int y = startY - outerGap - 1 - i; 
            gr.drawLine(x, y, x + boardWidth + 1, y);
            gr.drawLine(x + boardWidth + 1, y, x + boardWidth + 1, y + boardWidth + 1);
        }

        //Draw all the Go Buttons.
        for (int i = 0; i < buttonVect.size(); i++) {
            GoButton goButton = (GoButton)buttonVect.elementAt(i);
            if (!goButton.isDeleted) {
                int r1 = paneWidth / 3 + 1;
                int r2 = paneWidth / 2 - 1;
                int r;
                if (r1 > r2) r = r1; else r = r2;
                int x = startX + goButton.centerX * paneWidth - r;
                int y = startY + goButton.centerY * paneWidth - r;
                int d = 2 * r + 1;

                if (goButton.isDead) {
                    if (goButton.buttonColor == Color.black) {
                        gr.drawImage(blackButImage, x, y, d, d, null, null);
                    } else {
                        gr.drawImage(whiteButImage, x, y, d, d, null, null);
                    }

                    int rectR = paneWidth / 6 + 2;
                    int rectX = startX + goButton.centerX * paneWidth - rectR;
                    int rectY = startY + goButton.centerY * paneWidth - rectR;
                    int rectD = 2 * rectR;
                    gr.setColor(Color.red);
                    gr.fillRect(rectX, rectY, rectD, rectD);
                } else {
                    if (isMyTurn && goButton.equals(currentButton)) {
                        if ((timeNum <= 20) || 
                            (timeNum == 22) || 
                            (timeNum == 24) || (timeNum == 25) || (timeNum == 26) || (timeNum == 27) || 
                            (timeNum == 29)) {
                            if (goButton.buttonColor == Color.black) {
                                gr.drawImage(blackButImage, x, y, d, d, null, null);
                            } else {
                                gr.drawImage(whiteButImage, x, y, d, d, null, null);
                            }
                        } 
                    } else {
                        if (goButton.buttonColor == Color.black) {
                            gr.drawImage(blackButImage, x, y, d, d, null, null);
                        } else {
                            gr.drawImage(whiteButImage, x, y, d, d, null, null);
                        }
                    }
                }
            }
        }

        //Draw current position for playing and for counting.
        if (currentPosition != null) {
            int r = paneWidth / 6 + 2;
            int x = startX + currentPosition.x * paneWidth - r;
            int y = startY + currentPosition.y * paneWidth - r;
            int d = 2 * r;

            gr.setColor(currentPositionColor);
            gr.drawRect(x, y, d, d);
        }
    }

    class BlankBlock {
        int pointerX;     //a random blank X in this block
        int pointerY;     //a random blank Y in this block
        int blankNumber;  //total number of this block
        int blockType;    //0: initialize; 1: in black; 2: in white; 3: between black and white.

        BlankBlock(int x, int y) {
            pointerX = x;
            pointerY = y;
            blankNumber = 0;
            blockType   = 0;
        }
    }
}