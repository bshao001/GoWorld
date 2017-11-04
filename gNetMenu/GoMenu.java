package gNetMenu;

import gNetUtil.GoFont;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

public class GoMenu {
    //Menus
    private JMenu[] goMenu;

    //Menu Items Vector
    private static Vector<JMenuItem[]> goMIVect;

    //Menu Listener
    private ActionListener listener;

    //Status used for store old status of net related menus.
    private static boolean newgameStatus    = false;
    private static boolean continueStatus   = false;
    private static boolean backgoStatus     = false;
    private static boolean countStatus      = false;
    private static boolean informStatus     = false;
    private static boolean informtypeStatus = false;
    private static boolean endinformStatus  = false;

    public GoMenu(JMenuBar goMB, ActionListener menuListener) {
        listener = menuListener;

        int j    = GoMenuConstants.goMenuStrs.length;
        goMenu   = new JMenu[j];
        goMIVect = new Vector<JMenuItem[]>(j, 1);

        for (int i = 0; i < j; i++) {
            goMenu[i] = new JMenu(GoMenuConstants.goMenuStrs[i]);
            goMenu[i].setFont(GoFont.menuFont);
            goMenu[i].setMnemonic((new StringBuffer(GoMenuConstants.goMenuMne[i])).charAt(0));
            goMIVect.add(goMenuAddItems(i));   //Inner method is defined as follows.   
            goMB.add(goMenu[i]);
        }
    }

    private JMenuItem[] goMenuAddItems(int num) {
        int j = GoMenuConstants.goMIStrs[num].length;
        
        JMenuItem[] goMIs = new JMenuItem[j];
        
        for (int i = 0; i < j; i++) {
            if (GoMenuConstants.goMIStrs[num][i].equals(GoMenuConstants.SEPARATORSTR)) {
                goMenu[num].addSeparator();
            }
            else {
                goMIs[i] = goMenu[num].add(GoMenuConstants.goMIStrs[num][i]);
                goMIs[i].setFont(GoFont.menuFont);
                goMIs[i].addActionListener(listener);
                if (!GoMenuConstants.goMIMne[num][i].equals("")) {
                    goMIs[i].setMnemonic((new StringBuffer(GoMenuConstants.goMIMne[num][i])).charAt(0));
                }
                if (!GoMenuConstants.goMIAcc[num][i].equals("")) {
                    if (GoMenuConstants.goMIAcc[num][i].equals("N")) {
                        goMIs[i].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
                    } 
                    else if (GoMenuConstants.goMIAcc[num][i].equals("O")) {
                        goMIs[i].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
                    }
                    else if (GoMenuConstants.goMIAcc[num][i].equals("S")) {
                        goMIs[i].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
                    }
                    else if (GoMenuConstants.goMIAcc[num][i].equals("A")) {
                        goMIs[i].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
                    }
                    else if (GoMenuConstants.goMIAcc[num][i].equals("B")) {
                        goMIs[i].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
                    }
                    else if (GoMenuConstants.goMIAcc[num][i].equals("R")) {
                        goMIs[i].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
                    }
                }
                if (GoMenuConstants.goMenu[num][i] == 0)
                    goMIs[i].setEnabled(false);
            }
        }        
        return goMIs;
    }

    public static boolean getMIStatus(String miStr) {
        boolean retB = false;
        int len1 = GoMenuConstants.goMIStrs.length;

        for (int i = 0; i < len1; i++) {
            int len2 = GoMenuConstants.goMIStrs[i].length;
            for (int j = 0; j < len2; j++) {
                if (GoMenuConstants.goMIStrs[i][j].equals(miStr)) {
                    JMenuItem[] tempMIs = (JMenuItem[])goMIVect.elementAt(i);
                    retB = tempMIs[j].isEnabled();
                    i = len1;  //To exit the outer for loop
                    break;
                }
            }
        }
        return retB;
    }

    public static void setMIStatus(String miStr, boolean isActive) {
        int len1 = GoMenuConstants.goMIStrs.length;

        for (int i = 0; i < len1; i++) {
            int len2 = GoMenuConstants.goMIStrs[i].length;
            for (int j = 0; j < len2; j++) {
                if (GoMenuConstants.goMIStrs[i][j].equals(miStr)) {
                    JMenuItem[] tempMIs = (JMenuItem[])goMIVect.elementAt(i);
                    tempMIs[j].setEnabled(isActive);
                    return;
                }
            }
        }
    }

    public static void disableNetMenus() {
        newgameStatus   = getMIStatus(GoMenuConstants.NEWGAMESTR);
        continueStatus  = getMIStatus(GoMenuConstants.CONTINUESTR);
        backgoStatus    = getMIStatus(GoMenuConstants.BACKGOSTR);
        countStatus     = getMIStatus(GoMenuConstants.COUNTSTR);
        informStatus    = getMIStatus(GoMenuConstants.INFORMSTR);
        endinformStatus = getMIStatus(GoMenuConstants.ENDINFORMSTR);
        setMIStatus(GoMenuConstants.NEWGAMESTR, false);
        setMIStatus(GoMenuConstants.CONTINUESTR, false);
        setMIStatus(GoMenuConstants.BACKGOSTR, false);
        setMIStatus(GoMenuConstants.COUNTSTR, false);
        setMIStatus(GoMenuConstants.INFORMSTR, false);
        setMIStatus(GoMenuConstants.ENDINFORMSTR, false);
    }

    public static void restoreNetMenus() {
        setMIStatus(GoMenuConstants.NEWGAMESTR, newgameStatus);
        setMIStatus(GoMenuConstants.CONTINUESTR, continueStatus);
        setMIStatus(GoMenuConstants.BACKGOSTR, backgoStatus);
        setMIStatus(GoMenuConstants.COUNTSTR, countStatus);
        setMIStatus(GoMenuConstants.INFORMSTR, informStatus);
        setMIStatus(GoMenuConstants.ENDINFORMSTR, endinformStatus);
    }
}