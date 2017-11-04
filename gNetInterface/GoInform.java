package gNetInterface;

import gNetUtil.*;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

public class GoInform extends JDialog implements ActionListener {
    private String   beepStr         = "The System Beep";
    private String   audioStr        = "Play a Piece of Music:";
    private String   browseStr       = "Browse";
    private String   emailStr        = "Send an Email to Me: ";
    private String   emailDefaultStr = "Use the Default Topic and Content";
    private String   emailDefineStr  = "Define My Own Topic and Content";
    private String   okStr           = "OK";
    private String   cancelStr       = "Cancel";
    private String   preplayStr      = "Preplay";
    private String   endPreplayStr   = "End Preplay";
   
    private JRadioButton     beepButton, audioButton, emailButton;
    private GoPopupTextField audioText;
    private JButton          audioBrowseButton;
    private GoPopupTextField emailAddressText;
    private JLabel           domainLabel;
    private GoPopupTextField domainText;
    private JRadioButton     emailDefaultButton, emailDefineButton;
    private JLabel           emailTopicLabel;
    private GoPopupTextField emailTopicText;
    private JLabel           emailContentLabel;
    private GoPopupTextArea  emailContentText;
    private JScrollPane      emailContentTextPane;

    private JButton          okButton, cancelButton, preplayButton;

    private JFileChooser     goFileChooser;
    private Frame            dialogOwner;

    private GoInformInformation informInformation = new GoInformInformation(0);

    // The GoInformInformation related information
    private int     informType = 0;
    private String  audioFileName = null;
    private String  emailAddress = null;
    private String  domainName = null;
    private int     emailType = 0;
    private String  emailTopic = null;
    private String  emailContent = null;

    private String  informDirName, informFileName;
    private File    informFile;

    private boolean shouldBeep = false;
    private GoSound goSound = null;

    public GoInform(Frame fOwner, String str, boolean modal) {
        super(fOwner, str, modal);

        dialogOwner = fOwner;
        
        // Construct the goFileChooser.
        String goAudioDirName = System.getProperty("user.dir") + File.separator + "data" + 
				File.separator + "sounds";
        File goAudioDir = new File(goAudioDirName);

        if ((!goAudioDir.exists()) || (!goAudioDir.isDirectory())) {
            goFileChooser = new JFileChooser(System.getProperty("user.dir"));
        }
        else goFileChooser = new JFileChooser(goAudioDirName);

        goFileChooser.addChoosableFileFilter(new GoAudioFilter());

        //load the old information
        informDirName = System.getProperty("user.dir") + File.separator + "data" + 
				File.separator + "logs";
        informFileName = informDirName + File.separator + "goInformInformation";

        informFile = new File(informFileName);

        boolean isSaved = false;
        if (informFile.exists()) {
            try {
                ObjectInputStream inStream = new ObjectInputStream(
                                             new BufferedInputStream(new FileInputStream(informFile)));

                GoInformInformation informObject = (GoInformInformation)inStream.readObject();

                if (informObject != null) {            
                    informInformation = informObject;
                    initTextComponentContent(informObject);
                    isSaved = true;                   
                }
                inStream.close();
            } catch (EOFException eofEx) {
            } catch (ClassNotFoundException cnfEx) {
                cnfEx.printStackTrace();
            } catch (FileNotFoundException fnfEx) {
                fnfEx.printStackTrace();
            } catch (IOException ioEx) {
                ioEx.printStackTrace();
            }
        }

        // Create the components.
        beepButton = new JRadioButton(beepStr);
        beepButton.setFont(GoFont.buttonFont);
        beepButton.setSelected(true);

        audioButton = new JRadioButton(audioStr);
        audioButton.setFont(GoFont.buttonFont);

        audioText = new GoPopupTextField(audioFileName, 10);
        audioText.setEnabled(false);
        
        audioBrowseButton = new JButton(browseStr);
        audioBrowseButton.setFont(GoFont.buttonFont);
        audioBrowseButton.setEnabled(false);

        emailButton = new JRadioButton(emailStr);
        emailButton.setFont(GoFont.buttonFont);

        emailAddressText = new GoPopupTextField(emailAddress, 18);
        emailAddressText.setEnabled(false);

        domainLabel = new JLabel("Name of SMTP Server:");
        domainLabel.setFont(GoFont.labelFont);
        domainLabel.setEnabled(false);

        domainText = new GoPopupTextField(domainName, 18);
        domainText.setEnabled(false);

        emailDefaultButton = new JRadioButton(emailDefaultStr);
        emailDefaultButton.setFont(GoFont.buttonFont);
        emailDefaultButton.setSelected(true);
        emailDefaultButton.setEnabled(false);

        emailDefineButton = new JRadioButton(emailDefineStr);
        emailDefineButton.setFont(GoFont.buttonFont);
        emailDefineButton.setEnabled(false);

        emailTopicLabel = new JLabel("Topic:  ");
        emailTopicLabel.setFont(GoFont.labelFont);
        emailTopicLabel.setEnabled(false);

        emailTopicText = new GoPopupTextField(emailTopic, 25);
        emailTopicText.setEnabled(false);

        emailContentLabel = new JLabel("Content: ");
        emailContentLabel.setFont(GoFont.labelFont);
        emailContentLabel.setEnabled(false);

        emailContentText = new GoPopupTextArea(emailContent, 3, 30);

        emailContentTextPane = new JScrollPane(emailContentText);

        okButton = new JButton(okStr);
        okButton.setFont(GoFont.buttonFont);

        cancelButton = new JButton(cancelStr);
        cancelButton.setFont(GoFont.buttonFont);

        preplayButton = new JButton(preplayStr);
        preplayButton.setFont(GoFont.buttonFont);

        // Group the radio buttons.
        ButtonGroup group1 = new ButtonGroup();
        group1.add(beepButton);
        group1.add(audioButton);
        group1.add(emailButton);

        ButtonGroup group2 = new ButtonGroup();
        group2.add(emailDefaultButton);
        group2.add(emailDefineButton);

        /** 
         *  Add Listeners to them. 
         */
        // Add ActionListeners
        beepButton.addActionListener(this);
        audioButton.addActionListener(this);
        audioBrowseButton.addActionListener(this);
        emailButton.addActionListener(this);
        emailDefaultButton.addActionListener(this);
        emailDefineButton.addActionListener(this);
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);
        preplayButton.addActionListener(this);

        // Add Document Listeners
        audioText.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                audioFileName = audioText.getText();
                if ((audioFileName != null) && (!audioFileName.equals(""))) {
                    setOKButtonsActive(true);
                }               
            }

            public void removeUpdate(DocumentEvent e) {
                audioFileName = audioText.getText();
                if ((audioFileName == null) ||
                    ((audioFileName != null) && (audioFileName.equals("")))) {
                    setOKButtonsActive(false);
                } 
            }

            public void changedUpdate(DocumentEvent e) {/*This will not happen here*/}
        });

        emailAddressText.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                emailAddress = emailAddressText.getText();
                if (((emailAddress != null) && (!emailAddress.equals(""))) &&
                    ((domainName != null) && (!domainName.equals("")))) {
                    emailDefaultButton.setEnabled(true);
                    emailDefineButton.setEnabled(true);
           
                    if (emailDefaultButton.isSelected()) {
                        setOKButtonsActive(true);
                        setEmailDefineComponentsActive(false);
                    }
                    else if (emailDefineButton.isSelected()) {
                        setEmailDefineComponentsActive(true);

                        String str1 = emailTopicText.getText();
                        String str2 = emailContentText.getText();

                        if (((str1 != null) && (!str1.equals(""))) ||
                            ((str2 != null) && (!str2.equals("")))) {
                            setOKButtonsActive(true);
                        }
                        else setOKButtonsActive(false);
                    }
                }  
            }

            public void removeUpdate(DocumentEvent e) {
                emailAddress = emailAddressText.getText();
                if ((emailAddress == null) ||
                    ((emailAddress != null) && (emailAddress.equals("")))) {
                    emailDefaultButton.setEnabled(false);
                    emailDefineButton.setEnabled(false);
                    setEmailDefineComponentsActive(false);
                    setOKButtonsActive(false);
                } 
            }

            public void changedUpdate(DocumentEvent e) {/*This will not happen here*/}
        });

        domainText.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                domainName = domainText.getText();
                if (((emailAddress != null) && (!emailAddress.equals(""))) &&
                    ((domainName != null) && (!domainName.equals("")))) {
                    emailDefaultButton.setEnabled(true);
                    emailDefineButton.setEnabled(true);
           
                    if (emailDefaultButton.isSelected()) {
                        setOKButtonsActive(true);
                        setEmailDefineComponentsActive(false);
                    }
                    else if (emailDefineButton.isSelected()) {
                        setEmailDefineComponentsActive(true);

                        String str1 = emailTopicText.getText();
                        String str2 = emailContentText.getText();

                        if (((str1 != null) && (!str1.equals(""))) ||
                            ((str2 != null) && (!str2.equals("")))) {
                            setOKButtonsActive(true);
                        }
                        else setOKButtonsActive(false);
                    }
                }  
            }

            public void removeUpdate(DocumentEvent e) {
                domainName = domainText.getText();
                if ((domainName == null) ||
                    ((domainName != null) && (domainName.equals("")))) {
                    emailDefaultButton.setEnabled(false);
                    emailDefineButton.setEnabled(false);
                    setEmailDefineComponentsActive(false);
                    setOKButtonsActive(false);
                } 
            }

            public void changedUpdate(DocumentEvent e) {/*This will not happen here*/}
        });

        emailTopicText.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                emailTopic = emailTopicText.getText();
                if ((emailTopic != null) && (!emailTopic.equals(""))) {
                    setOKButtonsActive(true);
                }               
            }

            public void removeUpdate(DocumentEvent e) {
                emailTopic = emailTopicText.getText();
                emailContent = emailContentText.getText();
                if (((emailTopic == null) || ((emailTopic != null) && (emailTopic.equals("")))) && 
                    ((emailContent == null) || ((emailContent != null) && (emailContent.equals(""))))) {
                    setOKButtonsActive(false);
                } 
            }

            public void changedUpdate(DocumentEvent e) {/*This will not happen here*/}
        });

        emailContentText.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                emailContent = emailContentText.getText();
                if ((emailContent != null) && (!emailContent.equals(""))) {
                    setOKButtonsActive(true);
                }  
            }

            public void removeUpdate(DocumentEvent e) {
                emailTopic = emailTopicText.getText();
                emailContent = emailContentText.getText();
                if (((emailTopic == null) || ((emailTopic != null) && (emailTopic.equals("")))) && 
                    ((emailContent == null) || ((emailContent != null) && (emailContent.equals(""))))) {
                    setOKButtonsActive(false);
                } 
            }

            public void changedUpdate(DocumentEvent e) {/*This will not happen here*/}
        });

        // Put these components into the contentPane.
        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel1.add(beepButton);

        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel2.add(audioButton);  panel2.add(audioText);  panel2.add(audioBrowseButton);

        JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel3.add(emailButton);  panel3.add(emailAddressText);

        JPanel panel4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel4.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
        panel4.add(domainLabel);  panel4.add(domainText);

        JPanel panel5 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel5.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
        panel5.add(emailDefaultButton);

        JPanel panel6 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel6.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
        panel6.add(emailDefineButton);

        JPanel panel7 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel7.setBorder(BorderFactory.createEmptyBorder(0, 36, 0, 0));
        panel7.add(emailTopicLabel);  panel7.add(emailTopicText);

        JPanel panel8 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel8.setBorder(BorderFactory.createEmptyBorder(0, 36, 0, 0)); 
        panel8.add(emailContentLabel);

        JPanel innerUpPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        innerUpPanel.add(panel1);
        innerUpPanel.add(panel2);
        innerUpPanel.add(panel3);
        innerUpPanel.add(panel4);
        innerUpPanel.add(panel5);
        innerUpPanel.add(panel6);
        innerUpPanel.add(panel7);
        innerUpPanel.add(panel8);

        JPanel innerDownPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        innerDownPanel.setBorder(BorderFactory.createEmptyBorder(0, 36, 0, 0)); 
        innerDownPanel.add(emailContentTextPane);

        JPanel upPanel = new JPanel(new BorderLayout());
        upPanel.setBorder(BorderFactory.createCompoundBorder(
                          BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
                                                           " Use the Following Way to Notify Me ", TitledBorder.LEADING,
                                                           TitledBorder.TOP, GoFont.labelFont),
                          BorderFactory.createEmptyBorder(0, 10, 12, 10)));
        upPanel.add(innerUpPanel, BorderLayout.CENTER);
        upPanel.add(innerDownPanel, BorderLayout.SOUTH);

        JPanel downPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        downPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 20));
        downPanel.add(okButton);  downPanel.add(cancelButton);  downPanel.add(preplayButton);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        contentPane.add(upPanel, BorderLayout.CENTER);
        contentPane.add(downPanel, BorderLayout.SOUTH);
        setContentPane(contentPane);

        //restore the old information to the component to init their status.
        if (isSaved) restoreComponentStatus(informInformation);                   
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(beepStr)) {
            informType = 0;

            setAudioComponentsActive(false);
            setEmailTopComponentsActive(false);
            setEmailDefineComponentsActive(false);
            
            setOKButtonsActive(true);
        }
        else if (e.getActionCommand().equals(audioStr)) {
            informType = 1;
          
            setEmailTopComponentsActive(false);
            setEmailDefineComponentsActive(false);

            setAudioComponentsActive(true);
            
            String str = audioText.getText();
            if ((str != null) && (!str.equals(""))) {
                setOKButtonsActive(true);
            } 
            else setOKButtonsActive(false);
        }
        else if (e.getActionCommand().equals(browseStr)) {
             int returnVal = goFileChooser.showOpenDialog(dialogOwner);
      
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 audioFileName = goFileChooser.getSelectedFile().getAbsolutePath();
                 audioText.setText(audioFileName);
             }
        }
        else if (e.getActionCommand().equals(emailStr)) {
            informType = 2;
         
            setAudioComponentsActive(false);

            emailAddressText.setEnabled(true);
            domainLabel.setEnabled(true);
            domainText.setEnabled(true);
 
            String str1 = emailAddressText.getText();
            String str2 = domainText.getText();

            if (((str1 != null) && (!str1.equals(""))) &&
                ((str2 != null) && (!str2.equals("")))) {
                emailDefaultButton.setEnabled(true);
                emailDefineButton.setEnabled(true);
           
                if (emailDefaultButton.isSelected()) {
                    setOKButtonsActive(true);
                    setEmailDefineComponentsActive(false);
                }
                else if (emailDefineButton.isSelected()) {
                    setEmailDefineComponentsActive(true);

                    String str3 = emailTopicText.getText();
                    String str4 = emailContentText.getText();

                    if (((str3 != null) && (!str3.equals(""))) ||
                        ((str4 != null) && (!str4.equals("")))) {
                        setOKButtonsActive(true);
                    }
                    else setOKButtonsActive(false);
                }
            }  
            else setOKButtonsActive(false);          
        }
        else if (e.getActionCommand().equals(emailDefaultStr)) {
            emailType = 0;
            setEmailDefineComponentsActive(false);
            setOKButtonsActive(true);
        }
        else if (e.getActionCommand().equals(emailDefineStr)) {
            emailType = 1;
            setEmailDefineComponentsActive(true);
         
            String str1 = emailTopicText.getText();
            String str2 = emailContentText.getText();

            if (((str1 != null) && (!str1.equals(""))) ||
                ((str2 != null) && (!str2.equals("")))) {
                setOKButtonsActive(true);
            }
            else setOKButtonsActive(false);
        }
        else if (e.getActionCommand().equals(okStr)) {
            if (informType == 0) informInformation = new GoInformInformation(0);
            else if (informType == 1) informInformation = new GoInformInformation(1, audioFileName);
            else if (informType == 2) {
                if (emailType == 0) {
                    informInformation = new GoInformInformation(2, emailAddress, domainName, 0);
                }
                else if (emailType == 1) {
                    informInformation = new GoInformInformation(2, emailAddress, domainName, 1, emailTopic, emailContent); 
                }
            }

            // Save the information.
            if (!informFile.exists()) {
                File informDirFile = new File(informDirName);
                if ((!informDirFile.exists()) || (!informDirFile.isDirectory())) {
                    if (!informDirFile.mkdir()) return;
                }
            }
            try {
                ObjectOutputStream outStream = new ObjectOutputStream(
                                               new BufferedOutputStream(new FileOutputStream(informFile)));

                outStream.writeObject(informInformation);
                outStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            setVisible(false);
        }
        else if (e.getActionCommand().equals(cancelStr)) {
            if (informInformation != null) {
                initTextComponentContent(informInformation);
                restoreComponentStatus(informInformation);
            }
            setVisible(false);
        }
        else if (e.getActionCommand().equals(preplayStr)) {
            if (informType != 2) preplayButton.setText(endPreplayStr);
            Thread preplayThread = new Thread() {
                public void run() {
                    beginInform();
                }
            };
            preplayThread.start();
        }
        else if (e.getActionCommand().equals(endPreplayStr)) {
            if (informType != 2) preplayButton.setText(preplayStr);
            endInform();
        }
    }

    public void beginInform() {
        if (informType == 0) goBeep();
        else if (informType == 1) goAudioPlay(audioFileName);
        else if (informType == 2) {
            String address = emailAddress;
            String domain = domainName;
            String topic, content;
            topic = "From the GoWorld Game: You Friend Is Waiting For You There.";
            content = "This is an automatically generated email from the GoWorld (Net Version) game to you. " + 
                      "You friend is now waiting for you in the other end of the network and he(she) " +
                      "wants to inform you about this. So go back to the game soon. " +
                      "Wish you good luck and enjoy!";
            if (emailType == 1) {
                if ((emailTopic != null) && (!emailTopic.equals(""))) topic = emailTopic;
                if ((emailContent != null) && (!emailContent.equals(""))) content = emailContent;
            }
            goSendEmail(address, domain, topic, content);
        }
    }

    public synchronized void endInform() {
        if (informType == 0) {
            shouldBeep = false;
            notifyAll();
        } else if ((informType == 1) && (goSound != null)) goSound.stop();
    }

    private synchronized void goBeep() {
        shouldBeep = true;

        while (shouldBeep) {
            getToolkit().beep();
            try {
                wait(2000);
            } catch (InterruptedException iex) {
            }
        }
    }

    private void goAudioPlay(String audioFN) {
        goSound = new GoSound(audioFN);
        goSound.loop();
    }

    private void goSendEmail(String address, String domain, String topic, String content) {
        try {
            if (!(new GoSMTP(domain)).send(topic, content, address, address)) 
                System.err.println("Error in sending Email.");
        } catch (UnknownHostException uhex) {
            System.err.println("Unknown Host.");
        } catch (IOException ioex) {
            System.err.println("IO error.");
        }
    }

    private void initTextComponentContent(GoInformInformation informObject) {
        if (informObject.informType == 1) {
            String s1;
            if ((s1 = informObject.audioFileName) != null) audioFileName = s1;
        }
        else if (informObject.informType == 2) {
            String s1, s2;
            if ((s1 = informObject.emailAddress) != null) emailAddress = s1;
            if ((s2 = informObject.domainName) != null) domainName = s2;
            if ((emailAddress != null) && (domainName != null)) {
                if (informObject.emailType == 1) {
                    String s3, s4;
                    if ((s3 = informObject.emailTopic) != null) emailTopic = s3;
                    if ((s4 = informObject.emailContent) != null) emailContent = s4;
                }
            }
        }
    }

    private void restoreComponentStatus(GoInformInformation informObject) {
        if (informObject.informType == 0) {
            informType = 0;
            beepButton.setSelected(true);

            setAudioComponentsActive(false);
            setEmailTopComponentsActive(false);
            setEmailDefineComponentsActive(false);

            setOKButtonsActive(true);
        }
        else if (informObject.informType == 1) {
            informType = 1;
            audioButton.setSelected(true);

            setEmailTopComponentsActive(false);
            setEmailDefineComponentsActive(false);

            setAudioComponentsActive(true);

            if ((audioFileName != null) && (!audioFileName.equals(""))) setOKButtonsActive(true);
            else setOKButtonsActive(false);
        }
        else if (informObject.informType == 2) {
            informType = 2;
            emailButton.setSelected(true);
         
            setAudioComponentsActive(false);

            emailAddressText.setEnabled(true);
            domainLabel.setEnabled(true);
            domainText.setEnabled(true);
 
            if (((emailAddress != null) && (!emailAddress.equals(""))) &&
                ((domainName != null) && (!domainName.equals("")))) {
                emailDefaultButton.setEnabled(true);
                emailDefineButton.setEnabled(true);
                if (informObject.emailType == 0) {
                    emailType = 0;
                    emailDefaultButton.setSelected(true);
                    
                    setOKButtonsActive(true);
                    setEmailDefineComponentsActive(false);
                }
                else if (informObject.emailType == 1) {
                    emailType = 1;
                    emailDefineButton.setSelected(true);

                    setEmailDefineComponentsActive(true);

                    if (((emailTopic != null) && (!emailTopic.equals(""))) ||
                        ((emailContent != null) && (!emailContent.equals(""))))
                        setOKButtonsActive(true);
                    else setOKButtonsActive(false);
                }
            }
            else setOKButtonsActive(false);  
        }
    }

    private void setOKButtonsActive(boolean isActive) {
        okButton.setEnabled(isActive);
        preplayButton.setEnabled(isActive);
    }

    private void setAudioComponentsActive(boolean isActive) {
        audioText.setEnabled(isActive);
        audioBrowseButton.setEnabled(isActive);
    }

    private void setEmailTopComponentsActive(boolean isActive) {
        emailAddressText.setEnabled(isActive);
        domainLabel.setEnabled(isActive);
        domainText.setEnabled(isActive);
        emailDefaultButton.setEnabled(isActive);
        emailDefineButton.setEnabled(isActive);
    }

    private void setEmailDefineComponentsActive(boolean isActive) {
        emailTopicLabel.setEnabled(isActive);
        emailTopicText.setEnabled(isActive);
        emailContentLabel.setEnabled(isActive);
        emailContentText.setEnabled(isActive);
    }

    public GoInformInformation getInformInformation() {
        if (informInformation != null) return informInformation;
        else return null;
    }
}

class GoInformInformation implements java.io.Serializable {
    public int     informType = 0;
    public String  audioFileName = null;
    public String  emailAddress = null;
    public String  domainName = null;
    public int     emailType = 0;
    public String  emailTopic = null;
    public String  emailContent = null;

    public GoInformInformation(int informType) {
        this.informType = informType;
    }

    public GoInformInformation(int informType, String audioName) {
        this.informType = informType;
        audioFileName   = audioName;
    }

    public GoInformInformation(int informType, String emailAddress, String domainName, int emailType) {
        this.informType   = informType;
        this.emailAddress = emailAddress;
        this.domainName   = domainName;
        this.emailType    = emailType;
    }

    public GoInformInformation(int informType, String emailAddress, String domainName,
                               int emailType, String emailTopic, String emailContent) {
        this.informType   = informType;
        this.emailAddress = emailAddress;
        this.domainName   = domainName;
        this.emailType    = emailType;
        this.emailTopic   = emailTopic;
        this.emailContent = emailContent;
    }
}