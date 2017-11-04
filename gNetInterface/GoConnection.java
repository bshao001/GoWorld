package gNetInterface;

import gNetwork.*;
import gNetUtil.*;
import gNetMenu.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class GoConnection extends JDialog implements ActionListener {
    private String addressStr = "";
    private String portStr    = "";
    private String connectStr = "Connect to this Server";

    private String serverAddress = null, serverPort = null;

    private JLabel           addressLabel, portLabel;
    private GoPopupTextField addressText; 
    private GoPortTextField  portText;
    private JButton          connectButton;

    private boolean        isNetConnected = false;
    private GoNetClient    goNetClient    = null;
    private GoAcceptThread goAcceptThread = null;

    private File   connectFile     = null;
    private String connectDirName  = null;
    private String connectFileName = null;

    private Frame         owner;
    private GoWaitWindow  goWaitWin;

    public boolean isConnectButtonSelected = false; //only used in GoNet.java

    public GoConnection(Frame fOwner, String str, boolean modal, GoWaitWindow waitWin) {
        super(fOwner, str, modal);
        
        owner     = fOwner;
        goWaitWin = waitWin;

        //load the old information
        connectDirName = System.getProperty("user.dir") + File.separator + "data" + 
				File.separator + "logs";
        connectFileName = connectDirName + File.separator + "goConnectInformation";

        connectFile = new File(connectFileName);

        if (connectFile.exists()) {
            try {
                ObjectInputStream inStream = new ObjectInputStream(
                                             new BufferedInputStream(new FileInputStream(connectFile)));

                GoConnectInformation connectObject = (GoConnectInformation)inStream.readObject();
                if (connectObject != null) {
                    serverAddress = connectObject.serverAddress;
                    serverPort = connectObject.serverPort;
                }
                inStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Create the components.
        addressLabel = new JLabel("Name or Address of the Server: ");
        addressLabel.setFont(GoFont.labelFont);
        portLabel    = new JLabel("Port NO. of the Server Process: ");
        portLabel.setFont(GoFont.labelFont);

        addressText = new GoPopupTextField(serverAddress, 15);
        portText = new GoPortTextField(serverPort, 15);

        connectButton = new JButton(connectStr);
        connectButton.setFont(GoFont.buttonFont);
        connectButton.setEnabled(false);

        if ((serverAddress != null) && (!serverAddress.equals("") &&
            (serverPort != null)) && (!serverPort.equals(""))) {
            connectButton.setEnabled(true);
        }

        // Add Document Listeners to them.
        addressText.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                serverAddress = addressText.getText();
                if ((serverAddress != null) && (!serverAddress.equals(""))) { 
                    if ((serverPort != null) && (!serverPort.equals(""))) {
                        connectButton.setEnabled(true);
                    }
                }
            }

            public void removeUpdate(DocumentEvent e) {
                serverAddress = addressText.getText();
                if ((serverAddress == null) || 
                    ((serverAddress != null) && (serverAddress.equals("")))) {
                    connectButton.setEnabled(false);
                }
            }

            public void changedUpdate(DocumentEvent e) {/*This will not happen here*/}
        });

        portText.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                serverPort = portText.getText();
                if ((serverPort != null) && (!serverPort.equals(""))) {
                    if ((serverAddress != null) && (!serverAddress.equals(""))) {
                        connectButton.setEnabled(true);
                    }
                }
            }

            public void removeUpdate(DocumentEvent e) {
                serverPort = portText.getText();
                if ((serverPort == null) || 
                    ((serverPort != null) && (serverPort.equals("")))) {
                    connectButton.setEnabled(false);
                }
            }

            public void changedUpdate(DocumentEvent e) {/*This will not happen here*/}
        });

        connectButton.addActionListener(this);

        //Put these components into the contentPane
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new GridLayout(3, 1, 0, 8));
        JPanel panel1 = new JPanel();
        panel1.add(addressLabel);  panel1.add(addressText);
        JPanel panel2 = new JPanel();
        panel2.add(portLabel);  panel2.add(portText);
        JPanel panel3 = new JPanel();
        panel3.add(connectButton);
        contentPane.add(panel1);
        contentPane.add(panel2);
        contentPane.add(panel3); 
        contentPane.setBorder(BorderFactory.createEmptyBorder(18, 15, 18, 15));
        setContentPane(contentPane);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(connectStr)) {
            isConnectButtonSelected = true;
            GoMenu.setMIStatus(GoMenuConstants.CONNECTSTR, false);
            setVisible(false);
            Thread runner = new Thread() {
                public void run() {
                    goNetClient = new GoNetClient(serverAddress, Integer.parseInt(serverPort));
                    if (goNetClient.retValue == 0) isNetConnected = true;
                    else isNetConnected = false;
                    if (!isNetConnected) {
                        goWaitWin.setVisible(false);
                        GoMenu.setMIStatus(GoMenuConstants.CONNECTSTR, true);
                        isConnectButtonSelected = false;  //reinitilize this parameter.
                        JOptionPane.showMessageDialog(null, GoString.connectFailedString, 
                            "Server Connection Error", JOptionPane.ERROR_MESSAGE);
                    } else { // isNetConnected
                        goAcceptThread = new GoAcceptThread(goNetClient);
                        goAcceptThread.start();

                        goWaitWin.dispose();
                    }
                }
            };
            runner.start();

            //remember the IP address and the port NO.
            if (!connectFile.exists()) {
                File connectDirFile = new File(connectDirName);
                if ((!connectDirFile.exists()) || (!connectDirFile.isDirectory())) {
                    if (!connectDirFile.mkdir()) return;
                }
            }
            try {
                ObjectOutputStream outStream = new ObjectOutputStream(
                                               new BufferedOutputStream(new FileOutputStream(connectFile)));

                GoConnectInformation connectObject = new GoConnectInformation(serverAddress, serverPort);
                outStream.writeObject(connectObject);
                outStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean isNetConnected() {
        return isNetConnected;
    }

    public GoNetClient getNetClient() {
        if (goNetClient != null) return goNetClient;
        else return null;
    }

    public GoAcceptThread getAcceptThread() {
        if (goAcceptThread != null) return goAcceptThread;
        else return null;
    }
}

class GoConnectInformation implements Serializable {
    String  serverAddress = null;
    String  serverPort    = null;

    GoConnectInformation(String serverAddress, String serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort    = serverPort;
    }
}