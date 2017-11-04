package gNetInterface;

import gNetwork.*;
import gNetUtil.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import javax.swing.text.*;
import java.util.Vector;

public class GoTalkArea extends JPanel implements ActionListener {
    private String undoStr   = "Undo";
    private String redoStr   = "Redo";
    private String cutStr    = "Cut";
    private String copyStr   = "Copy";
    private String pasteStr  = "Paste";
    private String selectStr = "Select All";
    private String clearStr  = "Clear";
    private String sendStr   = "Send";
    private String wordsStr  = "Useful Words";
    private String configStr = "Color Configuration";
    private String recordStr = "Record";
    private String stopStr   = "End Record";

    private JLabel      upLabel;
    private JTextPane   textPane;
    private JScrollPane scrollPane;
    private JButton     sendButton, recordButton;

    private JPopupMenu  popup;
    private JMenuItem   undoMI, redoMI, cutMI, copyMI, pasteMI, selectMI, clearMI, sendMI, configMI;
    private JMenu       wordsMenu;

    private GoNetClient goNetClient;

    private CaretListener        talkListener     = null;
    private MouseListener        popupListener    = null;
    private DocumentListener     documentListener = null;
    private UndoableEditListener undoRedoListener = null;

    private UndoManager undo = new UndoManager();

    private String textStr = null;
    private SimpleAttributeSet[] attributes = new SimpleAttributeSet[4];
    private GoAttributeDocument goAttributeDocument = null;

    private JFrame dialogOwner = null;
    private GoColorSelection goColorSelection = null;
    private Color[] oldColors = {Color.black, Color.blue, Color.red, Color.green};  //Default configuration

    private File   textColorFile     = null;
    private String textColorDirName  = null;
    private String textColorFileName = null;

    private String[] defaultWords = GoString.defaultWords;
    private Vector<String> wordsVect = new Vector<String>(10, 1);
    private Vector<JMenuItem> menuitemVect = new Vector<JMenuItem>(10, 1);

    private String userDir = System.getProperty("user.dir");
    private String fileSep = System.getProperty("file.separator");

    private boolean sendVoice = false;
    private GoCapture goCapture;
    private byte[] audioBytes;

    public GoTalkArea(String labelStr, GoNetClient goNetClient, JFrame dialogOwner) {
        this.goNetClient = goNetClient;
        this.dialogOwner = dialogOwner;  
        
        upLabel = new JLabel(labelStr);
        upLabel.setFont(GoFont.labelFont);

        textPane = new JTextPane();

        textPane.setMargin(new Insets(5, 5, 5, 5));
        textPane.setSelectionColor(oldColors[3]);
        textPane.setFont(GoFont.talkAreaFont);
        textPane.setToolTipText("Please write to your friend here.");

        scrollPane = new JScrollPane(textPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        initAttributes();

        sendButton = new JButton(sendStr);
        sendButton.setFont(GoFont.buttonFont);
        sendButton.setActionCommand(sendStr);
        sendButton.setEnabled(false);
       
        recordButton = new JButton(recordStr);
        recordButton.setFont(GoFont.buttonFont);
        recordButton.setActionCommand(recordStr);

        sendButton.addActionListener(this);
        recordButton.addActionListener(this);

        talkListener = new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                int dot = e.getDot();
                int mark = e.getMark();
                if (dot == mark) { // no selection 
                    setButtonActive(false);
                } else { // some part selected
                    setButtonActive(true);
                }
            }
        };
        textPane.addCaretListener(talkListener);

        //Set the wordsVect to the default condition.
        String[] defaultWords = GoString.defaultWords;
        for (int i = 0; i < defaultWords.length; i++) wordsVect.addElement(defaultWords[i]);

        //Load the useful words information.
        String wordsDirName = userDir + fileSep + "data" + fileSep + "logs";
        String wordsFileName = wordsDirName + fileSep + "goUsefulWords";

        File wordsFile = new File(wordsFileName);

        if (wordsFile.exists()) {
            try {
                ObjectInputStream inStream = new ObjectInputStream(
                                             new BufferedInputStream(new FileInputStream(wordsFile)));

                GoUsefulWords wordsObject = (GoUsefulWords)inStream.readObject();
                if (wordsObject != null) {
                    wordsVect = wordsObject.wordsVect;
                }
                inStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        //Create the popup menu.
        popup = new JPopupMenu();

        undoMI = popup.add(undoStr);
        undoMI.setFont(GoFont.buttonFont);
        undoMI.addActionListener(this);
        undoMI.setEnabled(false);

        redoMI = popup.add(redoStr);
        redoMI.setFont(GoFont.buttonFont);
        redoMI.addActionListener(this);
        redoMI.setEnabled(false);

        popup.addSeparator();
        cutMI = popup.add(cutStr);
        cutMI.setFont(GoFont.buttonFont);
        cutMI.addActionListener(this);
        cutMI.setEnabled(false);

        copyMI = popup.add(copyStr);
        copyMI.setFont(GoFont.buttonFont);
        copyMI.addActionListener(this);
        copyMI.setEnabled(false);

        pasteMI = popup.add(pasteStr);
        pasteMI.setFont(GoFont.buttonFont);
        pasteMI.addActionListener(this);

        popup.addSeparator();
        selectMI = popup.add(selectStr);
        selectMI.setFont(GoFont.buttonFont);
        selectMI.addActionListener(this);
        selectMI.setEnabled(false);

        clearMI = popup.add(clearStr);
        clearMI.setFont(GoFont.buttonFont);
        clearMI.addActionListener(this);
        clearMI.setEnabled(false);
     
        popup.addSeparator();
        sendMI = popup.add(sendStr);
        sendMI.setFont(GoFont.buttonFont);
        sendMI.addActionListener(this);
        sendMI.setEnabled(false);
        
        popup.addSeparator();
        for (int i = 0; i < wordsVect.size(); i++) {
            JMenuItem mi = new JMenuItem((String)wordsVect.elementAt(i));
            mi.setFont(GoFont.buttonFont);
            mi.setActionCommand(wordsStr);  // To avoid the condition that useful words
                                            // equals to the other menuitems' names.
            mi.addActionListener(this);
            menuitemVect.addElement(mi);
        }
        wordsMenu = new JMenu(wordsStr);
        wordsMenu.setFont(GoFont.buttonFont);
        for (int i = 0; i < menuitemVect.size(); i++) {
            wordsMenu.add((JMenuItem)menuitemVect.elementAt(i));
        }
        popup.add(wordsMenu);

        popup.addSeparator();
        configMI = popup.add(configStr);
        configMI.setFont(GoFont.buttonFont);
        configMI.addActionListener(this);

        popupListener = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };
        textPane.addMouseListener(popupListener);
  
        documentListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                String input = textPane.getText();
                if ((input != null) && (!input.equals(""))) {
                    selectMI.setEnabled(true);
                    clearMI.setEnabled(true);
                }
            }

            public void removeUpdate(DocumentEvent e) {
                String input = textPane.getText();
                if ((input == null) || ((input != null) && (input.equals("")))) {
                    selectMI.setEnabled(false);
                    clearMI.setEnabled(false);
                }
            }

            public void changedUpdate(DocumentEvent e) {}
        };
        goAttributeDocument.addDocumentListener(documentListener);

        undoRedoListener = new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent e) {
                //Remember the edit and update the menus.
                undo.addEdit(e.getEdit());
                updateUndoRedoState();       
            }
        };
        goAttributeDocument.addUndoableEditListener(undoRedoListener);

        setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(sendButton);  buttonPanel.add(new JPanel());  buttonPanel.add(recordButton);
        add(upLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void updateUndoRedoState() {
        if (undo.canUndo()) undoMI.setEnabled(true);
        else undoMI.setEnabled(false);
        if (undo.canRedo()) redoMI.setEnabled(true);
        else redoMI.setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(undoStr)) {
            try {
                undo.undo();
            } catch (CannotUndoException cuex) {
                cuex.printStackTrace();
            }
            updateUndoRedoState();
        } 
        else if (e.getActionCommand().equals(redoStr)) {
            try {
                undo.redo();
            } catch (CannotRedoException crex) {
                crex.printStackTrace();
            }
            updateUndoRedoState();
        } 
        else if (e.getActionCommand().equals(cutStr)) {
            textPane.cut();
            setButtonActive(false);  //don't know why sometime this line is needed.
        } 
        else if (e.getActionCommand().equals(copyStr)) {
            textPane.copy();
        } 
        else if (e.getActionCommand().equals(pasteStr)) {
            textPane.paste();
        } 
        else if (e.getActionCommand().equals(selectStr)) {
            textPane.selectAll();
        } 
        else if (e.getActionCommand().equals(clearStr)) {
            textPane.selectAll();
            textPane.cut();
            setButtonActive(false);  //don't know why sometime this line is needed.
        } 
        else if (e.getActionCommand().equals(sendStr)) {
            if (!sendVoice) {
                String selectedText;
                if ((selectedText = textPane.getSelectedText()) != null) {
                    GoNetObject sentObject = new GoNetObject(GoNetConstants.TALKING, selectedText);
                    goNetClient.sendToServer(sentObject);
                    textPane.setCharacterAttributes(attributes[2], true);
                    textPane.setCaretPosition(textPane.getSelectionEnd());
                    setButtonActive(false);  //don't know why sometime this line is needed.
                }
            } else {
                if ((audioBytes = goCapture.getAudioBytes()) != null) {
                    final int byteLen = java.lang.reflect.Array.getLength(audioBytes);
                    System.out.println("length = " + byteLen);

                    Thread runner;
                    if (byteLen > GoNetConstants.BLOCKLEN) {
                        runner = new Thread() {
                            public void run() {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                int blockNumber;
                                if (byteLen % GoNetConstants.BLOCKLEN == 0) {
                                    blockNumber = byteLen / GoNetConstants.BLOCKLEN;
                                }
                                else blockNumber = byteLen / GoNetConstants.BLOCKLEN + 1;
        
                                for (int i = 0; i < blockNumber - 1; i++) {
                                    baos.write(audioBytes, i * GoNetConstants.BLOCKLEN, GoNetConstants.BLOCKLEN);
                                    byte[] tmpBytes = baos.toByteArray();
                                    baos.reset();
                                    GoNetObject sentObject = new GoNetObject(GoNetConstants.SPEAKING, blockNumber, 
                                                                             i + 1, tmpBytes);
                                    goNetClient.sendToServer(sentObject);
                                }
                                int start = (blockNumber - 1) * GoNetConstants.BLOCKLEN;
                                baos.write(audioBytes, start, byteLen - start); 
                                byte[] tmpBytes = baos.toByteArray();
                                try {
                                    baos.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                GoNetObject sentObject = new GoNetObject(GoNetConstants.SPEAKING, blockNumber, 
                                                                         blockNumber, tmpBytes);
                                goNetClient.sendToServer(sentObject);
                            }
                        };
                    } else {
                        runner = new Thread() {
                            public void run() {
                                GoNetObject sentObject = new GoNetObject(GoNetConstants.SPEAKING, 1, 1, audioBytes);
                                goNetClient.sendToServer(sentObject);
                            }
                        };
                    }
                    runner.start();
                    sendButton.setEnabled(false);
                    sendMI.setEnabled(false);
                    sendVoice = false;
                } 
            }
        }
        else if (e.getActionCommand().equals(wordsStr)) {
            for (int i = 0; i < menuitemVect.size(); i++) {
                if (e.getSource() == (JMenuItem)menuitemVect.elementAt(i)) {
                    int len = goAttributeDocument.getLength();
                    String str = (String)wordsVect.elementAt(i);
                    if (len > 0)  str = System.getProperty("line.separator") + str;
                    try {
                        goAttributeDocument.insertString(len, str, attributes[1]);
                    } catch (BadLocationException ble) {
                        ble.printStackTrace();
                    }
                    textPane.setCaretPosition(goAttributeDocument.getLength());
                    break;
                }
            }
        }
        else if (e.getActionCommand().equals(configStr)) {
            wordConfig();
        }
        else if (e.getActionCommand().equals(recordStr)) {
            recordButton.setText(stopStr);
            recordButton.setActionCommand(stopStr);

            goCapture = new GoCapture();
            goCapture.start();
        }
        else if (e.getActionCommand().equals(stopStr)) {
            recordButton.setText(recordStr);
            recordButton.setActionCommand(recordStr);

            goCapture.stop();

            sendButton.setEnabled(true);
            sendMI.setEnabled(true);
            sendVoice = true;
        }
    }

    private void wordConfig() {
        if (goColorSelection == null) {
       	    goColorSelection = new GoColorSelection(dialogOwner, "Color Configuration Table", true, oldColors);
            goColorSelection.pack();
            goColorSelection.setResizable(false);
            Dimension dSize = goColorSelection.getSize();
            Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (dScreen.width - dSize.width) / 2;
            int y = (dScreen.height - dSize.height) / 2;
            goColorSelection.setLocation(x, y);
        }
        goColorSelection.setVisible(true);
        if (goColorSelection.isOKSelected) {
            if (!oldColors[3].equals(goColorSelection.newColors[3])) {
                textPane.setSelectionColor(goColorSelection.newColors[3]);
            }
            for (int i = 0; i < 3; i++) {
                StyleConstants.setForeground(attributes[i + 1], goColorSelection.newColors[i]);   
            }
            for (int i = 0; i < goAttributeDocument.getLength(); i++) {
                textPane.setCaretPosition(i);
                AttributeSet attr = textPane.getCharacterAttributes();
                textPane.select(i, i + 1);
                if (attr != null) {
                    Color charColor = StyleConstants.getForeground(attr);
                    for (int j = 0; j < 3; j++) {
                        if (charColor.equals(oldColors[j])) {
                            textPane.setCharacterAttributes(attributes[j + 1], true);
                            break;
                        }
                    }
                }
            }
            textPane.setCaretPosition(goAttributeDocument.getLength());

            //save the new configuration information in the memory.
            for (int i = 0; i < 4; i++) {
                oldColors[i] = goColorSelection.newColors[i];
            }

            //save the new configuration information into disk.
            if (!textColorFile.exists()) {
                File textColorDirFile = new File(textColorDirName);
                if ((!textColorDirFile.exists()) || (!textColorDirFile.isDirectory())) {
                    if (!textColorDirFile.mkdir()) return;
                }
            }
            try {
                ObjectOutputStream outStream = new ObjectOutputStream(
                                           new BufferedOutputStream(new FileOutputStream(textColorFile)));

                GoTextColorInformation textColorObject = new GoTextColorInformation(oldColors);
                outStream.writeObject(textColorObject);
                outStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void setButtonActive(boolean isActive) {
        cutMI.setEnabled(isActive);
        copyMI.setEnabled(isActive);
        sendMI.setEnabled(isActive);
        sendButton.setEnabled(isActive);
    }

    public void append(String str) {
        textStr = str;
        SwingUtilities.invokeLater(new Runnable() { 
            public void run() {
                if (textPane.getCaretPosition() > 0)  textStr = System.getProperty("line.separator") + textStr;
                try {
                    goAttributeDocument.insertAttributeString(
                        goAttributeDocument.getLength(), textStr, attributes[3]);
                } catch (BadLocationException ble) {
                    ble.printStackTrace();
                }
                textPane.setCaretPosition(goAttributeDocument.getLength()); 
            }
        });
    }

    private void initAttributes() {
        //load the old color information.
        textColorDirName = userDir + fileSep + "data" + fileSep + "logs";
        textColorFileName = textColorDirName + fileSep + "goTextColorInformation";

        textColorFile = new File(textColorFileName);

        if (textColorFile.exists()) {
            try {
                ObjectInputStream inStream = new ObjectInputStream(
                                             new BufferedInputStream(new FileInputStream(textColorFile)));

                GoTextColorInformation textColorObject = (GoTextColorInformation)inStream.readObject();
                if (textColorObject != null) {
                    oldColors = textColorObject.textColors;
                }
                inStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        //init attributes.
        attributes[0] = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attributes[0], GoFont.taffName);
        StyleConstants.setFontSize(attributes[0], GoFont.taffInt);

        for (int i = 0; i < 3; i++) {
            attributes[i + 1] = new SimpleAttributeSet(attributes[0]);
            StyleConstants.setForeground(attributes[i + 1], oldColors[i]);
        }

        goAttributeDocument = new GoAttributeDocument();
        textPane.setDocument(goAttributeDocument);
    }

    public void setEditable(boolean isActive) {
        textPane.setEditable(isActive); 
    }

    public void setNetClient(GoNetClient goNetClient) {
        this.goNetClient = goNetClient;
    }

    public void removeListeners() {
        textPane.removeCaretListener(talkListener);
        textPane.removeMouseListener(popupListener);   
        goAttributeDocument.removeDocumentListener(documentListener); 
        goAttributeDocument.removeUndoableEditListener(undoRedoListener); 
    }

    public void restoreListeners() {
        textPane.addCaretListener(talkListener);
        textPane.addMouseListener(popupListener);   
        goAttributeDocument.addDocumentListener(documentListener); 
        goAttributeDocument.addUndoableEditListener(undoRedoListener);   
    }

    class GoAttributeDocument extends DefaultStyledDocument {
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            super.insertString(offs, str, attributes[1]);
        }
 
        public void insertAttributeString(int offs, String str, AttributeSet a) throws BadLocationException {
            super.insertString(offs, str, a);
        }
    }
}

class GoTextColorInformation implements java.io.Serializable {
    public Color[] textColors;

    public GoTextColorInformation(Color[] textColors) {
        this.textColors = textColors;
    }
}