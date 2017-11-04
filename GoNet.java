/*
 * Copyright (c) 2000, 2017, Bo Shao. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 
import gNetInterface.*;
import gNetMenu.*;
import gNetwork.*;
import gNetUtil.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import java.util.*;

public class GoNet extends JFrame implements ActionListener {
    /** 
     *  Optional LookAndFeel's Names:
     *    static String metalClassName = "javax.swing.plaf.metal.MetalLookAndFeel";
     *    static String motifClassName = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
     *    static final String windowsClassName = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
     */

    //MenuBar
    private JMenuBar goMB;
    
    //the handler of the contentPane
    private JPanel      contentPane;
    private GoInterface goInterface = null; //Is what in contentPane;

    //the handlers of the initial JComponents and related variables
    private static GoAnimation goAnimation = null;
    private static boolean     shouldAnimating = false;
    private static boolean     isWindowIconified = false;

    private JLabel dateLabel;
    private JPanel datePanel;

    //Network Connection, Informing Type Selection and Useful Words Configuration Dialogs
    private GoConnection goConnection = null;
    private GoWaitWindow goWaitWindow = null;
    private GoInform goInform = null;
    private GoWordsConfig goWordsConfig = null;

    //Help Frame GoHelpHowTo and GoHelpAboutGo Frames
    private GoHelpHowTo   goHelpHowTo   = null;
    private GoHelpAboutGo goHelpAboutGo = null;

    //The filechooser created for later usage.
    private JFileChooser goFileChooser = null;
    private File goFile = null;

    GoNet() throws InterruptedException {
        super("GoWorld(Network Version)");

        getAccessibleContext().setAccessibleDescription("A Friendly Go Game Client Part Program");

        Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dSize;
        int x, y;

        dSize = new Dimension(380, 200);
        setSize(dSize);
        x = (dScreen.width - dSize.width) / 2;
        y = (dScreen.height - dSize.height) / 2;
        setLocation(x, y);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        //add Window Listener
        addWindowListener(new WindowAdapter() {
            public void windowIconified(WindowEvent e) {
                isWindowIconified = true;
                if (shouldAnimating) goAnimation.stop();
                if (goInterface != null) {
                    goConnection.getNetClient().sendToServer(
                        new GoNetObject(GoNetConstants.CONFIRM, GoNetConstants.ICONFIEDSTR));
                }
            }
            public void windowDeiconified(WindowEvent e) {
                isWindowIconified = false;
                if (shouldAnimating) goAnimation.start();
                if (goInterface != null) {
                    goConnection.getNetClient().sendToServer(
                        new GoNetObject(GoNetConstants.CONFIRM, GoNetConstants.DEICONFIEDSTR));
                }
            }
            public void windowClosing(WindowEvent e) {
                goExit();
            }
        });

        JOptionPane.setRootFrame((Frame)this);

        Dimension labelSize = new Dimension(380, 20);
        JLabel progressLabel = new JLabel("Constructing, please wait...");
        progressLabel.setFont(GoFont.labelFont);
        progressLabel.setAlignmentX(CENTER_ALIGNMENT);
        progressLabel.setMaximumSize(labelSize);
        progressLabel.setPreferredSize(labelSize);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressLabel.setLabelFor(progressBar);
        progressBar.setAlignmentX(CENTER_ALIGNMENT);
        progressBar.setMinimum(0);
        progressBar.setMaximum(14); 
        progressBar.setValue(0);
        progressBar.getAccessibleContext().setAccessibleName("GoWorld Constructing progress");

        JPanel progressPanel = new JPanel() {
            public Insets getInsets() {
                return new Insets(40, 30, 20, 30);
            }
        };
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.add(progressLabel);
        progressPanel.add(Box.createRigidArea(new Dimension(1,20)));
        progressPanel.add(progressBar);
        setContentPane(progressPanel);
        setVisible(true);

        //Create Animation Part
        progressLabel.setText("Creating Animation Part");
        goAnimation = new GoAnimation(); 
        progressBar.setValue(progressBar.getValue() + 3);

        //Create Date Label
        progressLabel.setText("Creating Date Label and GoWaitWindow");
        String[] months = {"Jan.", "Feb.", "Mar.", "Apr.", "May", "June", 
                           "July", "Aug.", "Sep.", "Oct.", "Nov.", "Dec."};
        Calendar rightNow = Calendar.getInstance();
        int      year     = rightNow.get(Calendar.YEAR);
        String   month    = months[rightNow.get(Calendar.MONTH)];
        int      date     = rightNow.get(Calendar.DATE);
        String   dateStr  = month + "  " + date + ",  " + year;
        dateLabel = new JLabel(dateStr);
        dateLabel.setFont(GoFont.dateFont);
        datePanel = new JPanel(new BorderLayout()); 
        datePanel.setBackground(Color.gray);
        datePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 45));
        datePanel.setPreferredSize(new Dimension(820, 80));
        datePanel.add(dateLabel, BorderLayout.EAST);

        goWaitWindow = new GoWaitWindow((Frame)this);
        goWaitWindow.pack();
        dSize = goWaitWindow.getSize();
        x = (dScreen.width - dSize.width) / 2;
        y = (dScreen.height - dSize.height) / 2;
        goWaitWindow.setLocation(x, y); 
        Thread.sleep(100);

        progressBar.setValue(progressBar.getValue() + 1);

        /* 
         * new GoConnection, GoInform, GoHelpHowTo, GoHelpAboutGo, GoWordsConfig
         */
        //new goConnection
        progressLabel.setText("Creating GoConnection Dialog");
        goConnection = new GoConnection((Frame)this, "Connect to a Server", true, goWaitWindow);
        dSize = new Dimension(440, 210);
        goConnection.setSize(dSize);
        goConnection.setResizable(false);
        x = (dScreen.width - dSize.width) / 2;
        y = (dScreen.height - dSize.height) / 2;
        goConnection.setLocation(x, y);
        progressBar.setValue(progressBar.getValue() + 2);

        //new goInform
        progressLabel.setText("Creating GoInform Dialog");
        goInform = new GoInform((Frame)this, "How to notify me", true);
        dSize = new Dimension(460, 520);
        goInform.setSize(dSize);
        goInform.setResizable(false);
        x = (dScreen.width - dSize.width) / 2;
        y = (dScreen.height - dSize.height) / 2;
        goInform.setLocation(x, y);
        progressBar.setValue(progressBar.getValue() + 3);

        //new goHelpHowTo
        progressLabel.setText("Creating GoHelpHowTo Frame");
        goHelpHowTo = new GoHelpHowTo();
        goHelpHowTo.pack();
        dSize  = goHelpHowTo.getSize();
        x = (dScreen.width - dSize.width) / 2;
        y = (dScreen.height - dSize.height) / 2;
        goHelpHowTo.setLocation(x, y);
        progressBar.setValue(progressBar.getValue() + 3);

        //new goHelpAboutGo and goWordsConfig
        progressLabel.setText("Creating GoHelpAboutGo and GoWordsConfig");
        goHelpAboutGo = new GoHelpAboutGo();
        goHelpAboutGo.pack();
        dSize = goHelpAboutGo.getSize();
        x = (dScreen.width - dSize.width) / 2;
        y = (dScreen.height - dSize.height) / 2;
        goHelpAboutGo.setLocation(x, y);

        goWordsConfig = new GoWordsConfig((Frame)this, "Useful Words Configuration", true);
        dSize = new Dimension(360, 300);
        goWordsConfig.setSize(dSize);
        x = (dScreen.width - dSize.width) / 2;
        y = (dScreen.height - dSize.height) / 2;
        goWordsConfig.setLocation(x, y);

        Thread.sleep(100);
        progressBar.setValue(progressBar.getValue() + 1);

        //Create the MenuBar, Menus and contentPane.
        progressLabel.setText("Constructing Menus and Layout ContentPane");
        goMB = new JMenuBar();
        new GoMenu(goMB, this);
        setJMenuBar(goMB);

        contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(Color.white);
        contentPane.add(datePanel, BorderLayout.SOUTH);
        contentPane.add(goAnimation, BorderLayout.CENTER);
        Thread.sleep(200);
        progressBar.setValue(progressBar.getValue() + 1);

        getContentPane().removeAll();
        setContentPane(contentPane);
        dSize = new Dimension(830, 630);
        x = (dScreen.width - dSize.width) / 2;
        y = (dScreen.height - dSize.height) / 2;
        setSize(dSize);
        setResizable(false); 
        setLocation(x, y);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        validate();
        repaint();
    }

    public void actionPerformed(ActionEvent evt) {
        //Game Menu Operations
        if (evt.getActionCommand().equals(GoMenuConstants.NEWGAMESTR)) {
            if ((!goInterface.isChanged()) ||
                ((goInterface.isChanged()) && (goOptionConfirm("Confirmation of New a Game")))) {
                if (!GoMenu.getMIStatus(GoMenuConstants.NEWGAMESTR)) { //has been disabled already
                    showClashInformation();
                } else {
                    networkAsking(GoNetConstants.NEWGAMESTR);
                }
            }
        }
        else if (evt.getActionCommand().equals(GoMenuConstants.CONTINUESTR)) {
            if ((!goInterface.isChanged()) ||
                ((goInterface.isChanged()) && (goOptionConfirm("Confirmation of Continue a Former Game")))) {
                if (!GoMenu.getMIStatus(GoMenuConstants.CONTINUESTR)) { //has been disabled already
                    showClashInformation();
                } else {
                    networkAsking(GoNetConstants.CONTINUESTR);
                }
            }
        }
        else if (evt.getActionCommand().equals(GoMenuConstants.SAVESTR)) {
            if ((goFile != null) || ((goFile = goInterface.getFile()) != null)) goInterface.save(goFile);
            else goSaveAs();
        }
        else if (evt.getActionCommand().equals(GoMenuConstants.SAVEASSTR)) {
            goSaveAs();      
        }
        else if (evt.getActionCommand().equals(GoMenuConstants.EXITSTR)) {
            goExit();    
        }
        //Edit Menu Operations
        else if (evt.getActionCommand().equals(GoMenuConstants.BACKGOSTR)) {
            networkAsking(GoNetConstants.BACKGOSTR);
        }
        else if (evt.getActionCommand().equals(GoMenuConstants.COUNTSTR)) {
            int returnVal = JOptionPane.showConfirmDialog(this, GoString.askForCountStrings, "Count Confirmation", 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (returnVal == JOptionPane.YES_OPTION) {
                if (!GoMenu.getMIStatus(GoMenuConstants.COUNTSTR)) { //has been disabled already
                    showClashInformation();
                } else {
                    networkAsking(GoNetConstants.COUNTSTR);
                }
            }
        }
        else if (evt.getActionCommand().equals(GoMenuConstants.REFRESHSTR)) {
            contentPane.repaint();
        }
        //Server Menu Operations
        else if (evt.getActionCommand().equals(GoMenuConstants.CONNECTSTR)) {
            goConnect();
        }
        //Tools Menu Operations
        else if (evt.getActionCommand().equals(GoMenuConstants.INFORMSTR)) {
            goConnection.getNetClient().sendToServer(
                new GoNetObject(GoNetConstants.CONFIRM, GoNetConstants.INFORMTOCOMESTR));

            GoMenu.setMIStatus(GoMenuConstants.INFORMSTR, false);

            String str = "The system is now trying to notify your friend according to his or her preferred way ... ";
            goInterface.getSystemArea().append(str);
        }
        else if (evt.getActionCommand().equals(GoMenuConstants.INFORMTYPESTR)) {
            //Pop up a dialog to let the user choose his preferred informing method.
            goInform.setVisible(true);
        }
        else if (evt.getActionCommand().equals(GoMenuConstants.ENDINFORMSTR)) {
            GoMenu.setMIStatus(GoMenuConstants.ENDINFORMSTR, false);

            //Try to interrupt the calling.
            goInterface.endInform();

            //Inform the other player that you are here now.
            goConnection.getNetClient().sendToServer(
                new GoNetObject(GoNetConstants.CONFIRM, GoNetConstants.INFORMCOMINGSTR));

            String str = "The system has told your friend that you are here now.";
            goInterface.getSystemArea().append(str);
        }
        else if (evt.getActionCommand().equals(GoMenuConstants.EDITWORDSSTR)) {
            goWordsConfig.setVisible(true);
        }
        //Help Menu Operations
        else if (evt.getActionCommand().equals(GoMenuConstants.HOWTOSTR)) {
            goHelpHowTo.setVisible(true);
        }
        else if (evt.getActionCommand().equals(GoMenuConstants.ABOUTGOSTR)) {
            goHelpAboutGo.setVisible(true);
        }
    }

    private void showClashInformation() {
        String[] messages = {"You have chosen a network operation which is similar as that ",
                             "of your partner and this operation has been canceled by the ",
                             "system. But no problem and just answer your partner please! "};
        JOptionPane.showMessageDialog(this, messages, "Similar Network Operation Request Clash", 
                                      JOptionPane.INFORMATION_MESSAGE);
    }

    private void networkAsking(String askingWhat) {
        GoMenu.disableNetMenus();

        goConnection.getNetClient().sendToServer(new GoNetObject(GoNetConstants.ASKING, askingWhat));

        goInterface.getSystemArea().append(GoString.queryString);
    }

    private void goConnect() {
        goConnection.setVisible(true);

        if (goConnection.isConnectButtonSelected) {
            Thread runner = new Thread() {
                public void run() {
                    goWaitWindow.setVisible(true);

                    if (goConnection.isNetConnected()) {
                        if (goInterface == null) {
                            GoMenu.setMIStatus(GoMenuConstants.NEWGAMESTR, true);
                            GoMenu.setMIStatus(GoMenuConstants.CONTINUESTR, true);
                            GoMenu.setMIStatus(GoMenuConstants.INFORMSTR, true);
                            GoMenu.setMIStatus(GoMenuConstants.CONNECTSTR, false);
                            GoMenu.setMIStatus(GoMenuConstants.EDITWORDSSTR, false);

                            shouldAnimating = false;
                            goAnimation.stop();
                            setResizable(true);
                        
                            goInterface = new GoInterface(GoNet.this, contentPane, goConnection.getNetClient());
                            goConnection.getAcceptThread().setAccepter(goInterface);
                            goInterface.getGoTalkArea().setEditable(true);
                            goFileChooser = goInterface.getFileChooser();
                            goInterface.setGoInform(goInform);
                            contentPane.revalidate();
                            contentPane.repaint();
                        }
                        else { //reconnect to the server
                            goInterface.restoreNetworkOperation();
                            goInterface.setNetClient(goConnection.getNetClient());
                            goConnection.getAcceptThread().setAccepter(goInterface);

                            //tell the partner
                            goConnection.getNetClient().sendToServer(
                                new GoNetObject(GoNetConstants.CONFIRM, GoNetConstants.HENETRESTOREDSTR));

                            //tell the user
                            goInterface.isMySocketError = false;
                            String str = "You have connected to the server successfully and the network " + 
                                         "related operation has been restored.";
                            goInterface.getSystemArea().append(str); 
                        }
                    }
                }
            };    
            runner.start();    
        }
    }

    private boolean goOptionConfirm(String title) {
        int returnVal = JOptionPane.showConfirmDialog(this, GoString.askForSaveString, title, 
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (returnVal == JOptionPane.YES_OPTION) {
            if ((goFile != null) || ((goFile = goInterface.getFile()) != null)) goInterface.save(goFile);
            else goSaveAs();
        }
        else if (returnVal == JOptionPane.NO_OPTION) {
            //Just return true
        }
        else if (returnVal == JOptionPane.CANCEL_OPTION) {
            return false;
        }
        return true;
    }

    private void goSaveAs() {
        int returnVal = goFileChooser.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            goFile = goFileChooser.getSelectedFile();
            goInterface.save(goFile);
        }
    }

    private void goExit() {
        if (goInterface == null) System.exit(0);

        if ((!goInterface.isChanged()) ||
            ((goInterface.isChanged()) && (goOptionConfirm("Confirmation of Exit")))) {
            if ((goConnection != null) && (goConnection.isNetConnected())) {
                GoNetClient netClient = goConnection.getNetClient();
                if (!GoInterface.isMySocketError) {
                    if ((!GoInterface.isOtherPartExit) || (!GoInterface.isHeSocketError)) {
                        netClient.sendToServer(new GoNetObject(GoNetConstants.CONFIRM, GoNetConstants.EXITSTR));
                    }
                }
            }  
            System.exit(0);
        }
    }

    public static void main(String[] args) throws InterruptedException {
		try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception exc) {
			System.err.println("Error loading L&F: " + exc);
		}

        GoNet goNet = new GoNet();

        goNet.shouldAnimating = true;
        if (!goNet.isWindowIconified) goNet.goAnimation.start();
    }
}