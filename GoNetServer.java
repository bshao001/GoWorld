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
import gNetwork.GoNetObject;
import gNetwork.GoNetConstants;
import gNetUtil.GoFont;
import gNetUtil.GoPortTextField;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.Border;

public class GoNetServer extends Frame implements ActionListener {
    private String portString  = null;
    private String startString = "Start this Server";
    private String exitString  = "End this Server";

    private JLabel          serverLabel;
    private GoPortTextField serverText;
    private JButton         actionButton;

    private File    portFile     = null;
    private String  portDirName  = null;
    private String  portFileName = null;

    private GoNetServerProcess goServerProc = null;

    public GoNetServer() {
        super("GoWorld Server");

        //load the old information
        portDirName = System.getProperty("user.dir") + File.separator + "data" + File.separator + "logs";
        portFileName = portDirName + File.separator + "goNetServerInformation";

        portFile = new File(portFileName);

        if (portFile.exists()) {
            try {
                ObjectInputStream inStream = new ObjectInputStream(
                                             new BufferedInputStream(new FileInputStream(portFile)));

                GoNetServerInformation portObject = (GoNetServerInformation)inStream.readObject();
                if (portObject != null) portString = portObject.serverPort;
                inStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        //Create components and lay out them.
        serverLabel = new JLabel("Server Port NO.: ");
        serverLabel.setFont(GoFont.labelFont);
      
        serverText = new GoPortTextField(portString, 8);
     
        actionButton = new JButton(startString);
        actionButton.setFont(GoFont.buttonFont);
        actionButton.setActionCommand(startString);
        actionButton.setEnabled(false);
        
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new GridLayout(2, 1, 0, 5));

        JPanel panel1 = new JPanel();
        panel1.add(serverLabel);
        panel1.add(serverText);

        JPanel panel2 = new JPanel();
        panel2.add(actionButton);

        contentPane.setBorder(BorderFactory.createEmptyBorder(18, 10, 18, 10));
        contentPane.add(panel1);
        contentPane.add(panel2);

        add(contentPane);

        if ((portString != null) && (!portString.equals(""))) {
            actionButton.setEnabled(true);
        }

        // Add Document Listeners to the serverText.
        serverText.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                portString = serverText.getText();
                if ((portString != null) && (!portString.equals(""))) {
                    actionButton.setEnabled(true);
                }
            }

            public void removeUpdate(DocumentEvent e) {
                portString = serverText.getText();
                if ((portString == null) || 
                    ((portString != null) && (portString.equals("")))) {
                    actionButton.setEnabled(false);
                }
            }

            public void changedUpdate(DocumentEvent e) {/*This will not happen here*/}
        });

        // Add ActionListeners to the actionButton.
        actionButton.addActionListener(this);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                serverExit();
            }
        });
    }
 
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(startString)) {
            Thread startThread = new Thread() {
                public void run() {
                    try {
                        goServerProc = new GoNetServerProcess(Integer.parseInt(portString));
                    } catch (IOException ioEx) {
                        System.err.println("IOException in line 128 of GoNetServer.java");
                    }
                }
            };
            startThread.start();
            actionButton.setText(exitString);
            actionButton.setActionCommand(exitString);

            if (!portFile.exists()) {
                File portDirFile = new File(portDirName);
                if ((!portDirFile.exists()) || (!portDirFile.isDirectory())) {
                    if (!portDirFile.mkdir()) return;
                }
            }
            try {
                ObjectOutputStream outStream = new ObjectOutputStream(
                                               new BufferedOutputStream(new FileOutputStream(portFile)));

                GoNetServerInformation portObject = new GoNetServerInformation(portString);
                outStream.writeObject(portObject);
                outStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        else if (e.getActionCommand().equals(exitString)) {
            serverExit();                    
        }
    }

    private void serverExit() {
        int returnVal = JOptionPane.showConfirmDialog(this, "Are you sure to end the server process?", "Exit Confirmation", 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (returnVal == JOptionPane.YES_OPTION) {
            if (goServerProc != null) {
                try {
                    goServerProc.destroy();
                } catch (IOException ioEx) {
                    System.err.println("IOException in line 166 in GoNetServer.java");
                }
            }
            System.exit(0);
        } else if (returnVal == JOptionPane.NO_OPTION) {
            return;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	} catch (Exception exc) {
	    System.err.println("Error loading L&F: " + exc);
	}

        GoNetServer serverDialog = new GoNetServer();

        Dimension dDialog = new Dimension(260, 160);
        serverDialog.setSize(dDialog);

        Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (dScreen.width - dDialog.width) / 2;
        int y = (dScreen.height - dDialog.height) / 2;
        serverDialog.setLocation(x, y);

        serverDialog.setVisible(true);
    }
}

class GoNetServerInformation implements Serializable {
    String serverPort = null;

    GoNetServerInformation(String serverPort) {
        this.serverPort = serverPort;
    }
} 

class GoNetServerProcess {
    private static ServerSocket         serverSocket  = null;
    private static Socket[]             clientSockets = new Socket[2];
    private static ServerThread[]       serverThreads = new ServerThread[2];
    private static ObjectInputStream[]  goNetIns      = new ObjectInputStream[2];
    private static ObjectOutputStream[] goNetOuts     = new ObjectOutputStream[2];
    private static boolean[]            shouldAccept  = {true, true};

    GoNetServerProcess(int serverPort) throws IOException {
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + serverPort + ".");
            System.exit(1);
        }

        for (int i = 0; i < 2; i++) {
            clientSockets[i] = null;
            serverThreads[i] = null;
            goNetIns[i] = null;
            goNetOuts[i] = null;          
        }

        while (true) {
            while ((!shouldAccept[0]) && (!shouldAccept[1]));

            for (int i = 0; i < 2; i++) {
                if (serverThreads[i] != null) serverThreads[i].stopTimer();
            }

            //This loop ensures that both clients are connected when break from this loop because
            //there are occasions that shouldAccept[0] changes while accept clientSockets[1]. 
            while (shouldAccept[0] || shouldAccept[1]) {
                boolean[] accepted = {false, false}; 

                for (int i = 0; i < 2; i++) {        
                    try {
                        if (shouldAccept[i]) {
                            clientSockets[i] = serverSocket.accept();
                            shouldAccept[i] = false;
                            accepted[i] = true;
                        }
                    } catch (IOException e) {
                        System.err.println("Client accept failed.");
                        System.exit(1);
                    }
                }

                for (int i = 0; i < 2; i++) {
                    if (accepted[i]) {
                        goNetIns[i]  = new ObjectInputStream(clientSockets[i].getInputStream());
                        goNetOuts[i] = new ObjectOutputStream(clientSockets[i].getOutputStream());
                        accepted[i] = false;
                    }
                }
            }

            for (int i = 0; i < 2; i++) {
                serverThreads[i] = new ServerThread(goNetIns[i], goNetOuts[i], goNetOuts[1 - i], i);
                serverThreads[i].start();
            }
        }
    }

    public synchronized void destroy() throws IOException {
        if ((serverThreads[0] != null) && (serverThreads[1] != null)) {
            if ((serverThreads[0].isAlive()) || (serverThreads[1].isAlive())) {
                for (int i = 0; i < 2; i++) {
                    if (serverThreads[i].isAlive()) {
                        serverThreads[i].stopTimer();
                        serverThreads[i].interrupt();
                        serverThreads[i] = null;
                    }
                }
                notifyAll();
            }
            for (int i = 0; i < 2; i++) {
                goNetIns[i].close();
                goNetOuts[i].close();
            }     
            for (int i = 0; i < 2; i++) clientSockets[i].close();
            serverSocket.close();
        }
    }

    class ServerThread extends Thread implements ActionListener {
        private ObjectInputStream  goNetIn  = null;
        private ObjectOutputStream goNetOut1 = null, goNetOut2 = null;
        private int threadID = -1;
        private Timer timer;
    
        public ServerThread(ObjectInputStream in, ObjectOutputStream out1, ObjectOutputStream out2, int i) {
            super("ServerThread" + i);
            threadID  = i;
    	    goNetIn   = in;
            goNetOut1 = out1;
            goNetOut2 = out2;
            timer = new Timer(1000, this);
        }
    
        public void run() {
            if (!timer.isRunning()) timer.start();

    	    try {
                GoNetObject fromClient;

                while (true) {
                    if ((fromClient = (GoNetObject)goNetIn.readObject()) != null) {
                        if ((fromClient.status == GoNetConstants.CONFIRM) && 
                            (fromClient.talkString.equals(GoNetConstants.EXITSTR))) {
                            goNetOut2.writeObject(fromClient);
                            break;
                        }
                        goNetOut2.writeObject(fromClient);
                    }
                }
            } catch (Exception e) {
                // e.printStackTrace();
                // System.err.println("Exception in line 321 of GoNetServer.java");
    	    }
        }

        public void actionPerformed(ActionEvent e) {
            try {
                goNetOut1.writeObject(new GoNetObject(GoNetConstants.PING));
            } catch (SocketException ex) {
                shouldAccept[threadID] = true;
                try {
                    goNetOut2.writeObject(new GoNetObject(GoNetConstants.CONFIRM, GoNetConstants.HESOCKETERRORSTR));
                } catch (IOException ioEx) {
                    //because the writeObject in the other thread be blocked by this one.
                    shouldAccept[1 - threadID] = true;  
                    
                    // System.err.println("IOException in line 336 of GoNetServer.java");
                }                 
                // System.err.println("SocketException in line 338 of GoNetServer.java");
            } catch (IOException ex) {
                // System.err.println("IOException in line 340 of GoNetServer.java");
            }
        }
 
        public void stopTimer() {
            if (timer.isRunning()) timer.stop();
        }
    }
}