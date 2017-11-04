package gNetInterface;

import gNetUtil.*;

import java.io.*;
import java.util.Vector;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

public class GoWordsConfig extends JDialog implements ActionListener, ListSelectionListener {
    private JList<String> goList;
    private DefaultListModel<String> goListModel;

    private GoPopupTextField wordsText;

    private String  addString = "Add";
    private String  removeString = "Remove";
    private String  okString = "OK";
    private String  resetString = "Reset";

    private JButton addButton, removeButton, okButton, resetButton;

    private File   wordsFile     = null;
    private String wordsDirName  = null;
    private String wordsFileName = null;

    private Vector<String>   wordsVect = new Vector<String>(10, 1);
    private String[] defaultWords = GoString.defaultWords;

    public GoWordsConfig(Frame fOwner, String str, boolean modal) {
        super(fOwner, str, modal); 

        //Set the wordsVect to the default condition.
        for (int i = 0; i < defaultWords.length; i++) wordsVect.addElement(defaultWords[i]);

        //Load the old information. If words have been configured, then wordsVect will be replaced.
        wordsDirName = System.getProperty("user.dir") + File.separator + "data" + File.separator + "logs";
        wordsFileName = wordsDirName + File.separator + "goUsefulWords";

        wordsFile = new File(wordsFileName);

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

        //Create listModel and other components.
        goListModel = new DefaultListModel<String>();
        for (int i = 0; i < wordsVect.size(); i++) goListModel.addElement(wordsVect.elementAt(i));

        goList = new JList<String>(goListModel);
        goList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        goList.setSelectedIndex(0);
        goList.addListSelectionListener(this);
        JScrollPane listScrollPane = new JScrollPane(goList);

        String words = goListModel.getElementAt(goList.getSelectedIndex()).toString();
        wordsText = new GoPopupTextField(words, 10);
        wordsText.addActionListener(this);
     
        addButton = new JButton(addString);
        addButton.setFont(GoFont.buttonFont);
        addButton.addActionListener(this);

        removeButton = new JButton(removeString);
        removeButton.setFont(GoFont.buttonFont);
        removeButton.addActionListener(this);

        okButton = new JButton(okString);
        okButton.setFont(GoFont.buttonFont);
        okButton.addActionListener(this);

        resetButton = new JButton(resetString);
        resetButton.setFont(GoFont.buttonFont);
        resetButton.addActionListener(this);

        //Lay out these components.
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        textPanel.add(wordsText);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);  buttonPanel.add(removeButton);
        buttonPanel.add(okButton);  buttonPanel.add(resetButton);

        JPanel downPanel = new JPanel(new GridLayout(2, 1));
        downPanel.add(textPanel);
        downPanel.add(buttonPanel);

        getContentPane().add(listScrollPane, BorderLayout.CENTER);
        getContentPane().add(downPanel, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent e) {
        if ((e.getSource() == wordsText) || (e.getSource() == addButton)) {
            String newWords = wordsText.getText();

            if (newWords.equals("")) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }

            int index = goList.getSelectedIndex();
            int size = goListModel.getSize();

            //If no selection or if item in last position is selected, add
            //the new words to the end of list, and select this new words.
            if (index == -1 || (index + 1 == size)) {
                goListModel.addElement(newWords);
                goList.setSelectedIndex(size);
                okButton.setEnabled(true);
            }
            //Otherwise insert the new words after the current selection,
            //and select this new words.
            else {
                goListModel.insertElementAt(newWords, index + 1);
                goList.setSelectedIndex(index + 1);
            }
        }
        else if (e.getSource() == removeButton) {
            int index = goList.getSelectedIndex();
            goListModel.remove(index);

            int size = goListModel.getSize();

            if (size == 0) {
                removeButton.setEnabled(false);
                okButton.setEnabled(false);
            } else {
                if (index == goListModel.getSize()) index--;
                goList.setSelectedIndex(index); 
            }

        }
        else if (e.getSource() == okButton) {
            wordsVect.removeAllElements();
            for (int i = 0; i < goListModel.getSize(); i++) {
                wordsVect.addElement(goListModel.getElementAt(i));
            }
            //remember the useful words configuration information.
            if (!wordsFile.exists()) {
                File wordsDirFile = new File(wordsDirName);
                if ((!wordsDirFile.exists()) || (!wordsDirFile.isDirectory())) {
                    if (!wordsDirFile.mkdir()) return;
                }
            }
            try {
                ObjectOutputStream outStream = new ObjectOutputStream(
                                               new BufferedOutputStream(new FileOutputStream(wordsFile)));

                GoUsefulWords wordsObject = new GoUsefulWords(wordsVect);
                outStream.writeObject(wordsObject);
                outStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            setVisible(false);
        }
        else if (e.getSource() == resetButton) {
            goListModel.removeAllElements();
            for (int i = 0; i < defaultWords.length; i++) goListModel.addElement(defaultWords[i]);
            goList.setSelectedIndex(0);
            removeButton.setEnabled(true);
            okButton.setEnabled(true);
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false) {
            if (goList.getSelectedIndex() == -1) { //No selection, disable remove button.
                removeButton.setEnabled(false);
                wordsText.setText("");
            } else { //Selection, update text field.
                removeButton.setEnabled(true);
                String words = goList.getSelectedValue().toString();
                wordsText.setText(words);
            }
        }
    }
}