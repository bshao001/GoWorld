package gNetInterface;

import gNetUtil.*;

import java.net.URL;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.undo.*;

public class GoHelpHowTo extends JFrame implements MouseListener, ActionListener, 
                                                   HyperlinkListener, TreeSelectionListener {
    private JEditorPane    htmlPane;
    private URL            helpURL, currentURL;
    private JToolBar       toolBar;
    private JButton        backButton, forwardButton, printButton;
    private JTree          tree;
    private URLUndoManager undo = new URLUndoManager();

    public GoHelpHowTo() {
        super("GoWorld Help");

        //Create the toolBar
        createToolBar();

        //Create the top and down nodes.
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(new BookInfo("Go World", "GoHelpHowTo.html"));
     
        DefaultMutableTreeNode menu = new DefaultMutableTreeNode(new BookInfo("Menu Operations", "GoHelpHowTo.html"));
        top.add(menu);
        createMenuNodes(menu);
   
        DefaultMutableTreeNode face = new DefaultMutableTreeNode(new BookInfo("Interfaces", "GoHelpHowTo.html"));
        top.add(face);
        createFaceNodes(face);

        DefaultMutableTreeNode event = new DefaultMutableTreeNode(new BookInfo("Window Events", "GoHelpHowTo.html"));
        top.add(event);
        createEventNodes(event); 

        DefaultMutableTreeNode server = new DefaultMutableTreeNode(new BookInfo("About the Server", "aboutserver.html"));
        top.add(server);

        //Create a tree that allows one selection at a time and listen for the selection changes.
        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);

        //Create the scroll pane and add the tree to it. 
        JScrollPane treeView = new JScrollPane(tree);

        //Create the HTML viewing pane.
        htmlPane = new JEditorPane();
        htmlPane.setEditorKit(new HTMLEditorKit());
        htmlPane.setEditable(false);
        htmlPane.addHyperlinkListener(this);

        String s = null;
        try {
            s = getHelpPath() + "GoHelpHowTo.html";
            helpURL = new URL(s);
            currentURL = helpURL;
            displayURL(helpURL);
        } catch (Exception e) {
            System.err.println("Couldn't create help URL: " + s);
        }

        JScrollPane htmlView = new JScrollPane(htmlPane);

        //Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(treeView);
        splitPane.setRightComponent(htmlView);
        Dimension minimumSize = new Dimension(100, 150);
        htmlView.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(200); 
        splitPane.setPreferredSize(new Dimension(720, 400));

        //Add the toolBar and the split pane to this frame.
        getContentPane().add(toolBar, BorderLayout.NORTH);
        getContentPane().add(splitPane, BorderLayout.CENTER);
    }

    //MouseListener
    public void mouseEntered(MouseEvent e) {
        JButton button = (JButton)e.getSource();
        if (button.isEnabled()) button.setBorderPainted(true);
    }
    
    public void mouseExited(MouseEvent e) {
        ((JButton)e.getSource()).setBorderPainted(false);
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}

    //ActionListener
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == backButton) {
            try {
                URL url = undo.swapURL(currentURL);
                undo.undo();
                displayURL(url);
            } catch (CannotUndoException exc) {
                System.err.println("can not undo.");
            } catch (Exception ex) {
                System.err.println("Exception in backButton.");
            } finally {
                updateUndoRedoStatus();
            }
        }
        else if (e.getSource() == forwardButton) {
            try {
                undo.redo();
                displayURL(undo.swapURL(currentURL));
            } catch (CannotRedoException exc) {
                System.err.println("Can not redo.");
            } finally {
                updateUndoRedoStatus();
            }
        }
        else if (e.getSource() == printButton) {
            GoComponentVista vista = new GoComponentVista(htmlPane, new PageFormat()); 
            vista.scaleToFit(false);
            PrinterJob pj = PrinterJob.getPrinterJob(); 
            pj.setPageable(vista); 
            try { 
                if (pj.printDialog()) { 
                    pj.print();
                }
            } catch (PrinterException ex) {
                ex.printStackTrace();
            }
        }
    }

    //HyperLinkListener
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            final String dest = e.getURL().toString();
            Thread runner = new Thread() {
                public void run() {
                    try {
                        displayWithUndoRecord(new URL(dest));
                    } catch (Exception exc) {
                        System.err.println("Can not create Hyperlink URL.");
                    }
                }
            };
            runner.start();
        }
    }

    //TreeSelectionListener
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();

        if (node != null) {
            displayWithUndoRecord(((BookInfo)(node.getUserObject())).bookURL);
        }
    }

    private void createToolBar() {
        toolBar = new JToolBar();

        String path = System.getProperty("user.dir") + File.separator + "data" + 
				File.separator + "images" + File.separator;
   
        backButton = new JButton("Back", new ImageIcon(path + "back.gif"));
        backButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        backButton.setHorizontalTextPosition(SwingConstants.CENTER);
        backButton.setEnabled(false);
        backButton.setBorderPainted(false);
        backButton.setRequestFocusEnabled(false);
        backButton.addMouseListener(this);
        backButton.addActionListener(this);
        toolBar.add(backButton);
        
        forwardButton = new JButton("Forward", new ImageIcon(path + "forward.gif"));
        forwardButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        forwardButton.setHorizontalTextPosition(SwingConstants.CENTER);
        forwardButton.setEnabled(false);
        forwardButton.setBorderPainted(false);
        forwardButton.setRequestFocusEnabled(false);
        forwardButton.addMouseListener(this);
        forwardButton.addActionListener(this);
        toolBar.add(forwardButton);

        printButton = new JButton("Print", new ImageIcon(path + "print.gif"));
        printButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        printButton.setHorizontalTextPosition(SwingConstants.CENTER);
        printButton.setBorderPainted(false);
        printButton.setRequestFocusEnabled(false);
        printButton.addMouseListener(this);
        printButton.addActionListener(this);
        toolBar.add(printButton);
    }

    private String getHelpPath() {
        String prefix = "file:///" + System.getProperty("user.dir") + 
				File.separator + "data" + File.separator + "helpFiles" + File.separator;
        return prefix;
    } 
   
    private void displayURL(URL url) {
        try {
            currentURL = url;
            htmlPane.setPage(url);
        } catch (IOException e) {
            System.err.println("Attempted to read a bad URL: " + url);
        }
    }

    private void displayWithUndoRecord(URL url) {
        if (currentURL != url) {
            undo.addURL(currentURL);
            updateUndoRedoStatus();
            displayURL(url);
        }
    }

    private void updateUndoRedoStatus() {
        if (undo.canUndo()) {
            backButton.setEnabled(true);
            backButton.setToolTipText(undo.getUndoPresentationName());
        } else {
            backButton.setEnabled(false);
            backButton.setToolTipText(null);
        }
    
        if (undo.canRedo()) {
            forwardButton.setEnabled(true);
            forwardButton.setToolTipText(undo.getRedoPresentationName());
        } else {
            forwardButton.setEnabled(false);
            forwardButton.setToolTipText(null);
        }
    }

    private void createMenuNodes(DefaultMutableTreeNode top) {
        DefaultMutableTreeNode topcate  = null;
        DefaultMutableTreeNode category = null;
        DefaultMutableTreeNode leafNode = null;

        /**
         *  The Popup Menus
         */
        leafNode = new DefaultMutableTreeNode(new BookInfo("Popup Menus", "popupmenu.html"));
        top.add(leafNode);

        /**
         *  The Push Menus
         */
        topcate = new DefaultMutableTreeNode(new BookInfo("Push Menus", "pushmenu.html"));
        top.add(topcate);

        //The First Menu
        category = new DefaultMutableTreeNode(new BookInfo("Game", "game.html"));
        topcate.add(category);

        leafNode = new DefaultMutableTreeNode(new BookInfo("New", "newgame.html"));
        category.add(leafNode);

        leafNode = new DefaultMutableTreeNode(new BookInfo("Open(Continue)", "continue.html"));
        category.add(leafNode);

        leafNode = new DefaultMutableTreeNode(new BookInfo("Save", "save.html"));
        category.add(leafNode);

        leafNode = new DefaultMutableTreeNode(new BookInfo("Save As...", "saveas.html"));
        category.add(leafNode);

        leafNode = new DefaultMutableTreeNode(new BookInfo("Exit", "exit.html"));
        category.add(leafNode);

        //The Second Menu
        category = new DefaultMutableTreeNode(new BookInfo("Operation", "operation.html"));
        topcate.add(category);

        leafNode = new DefaultMutableTreeNode(new BookInfo("Undo(Back)", "backgo.html"));
        category.add(leafNode);

        leafNode = new DefaultMutableTreeNode(new BookInfo("Count Points Alive", "countpoints.html"));
        category.add(leafNode);

        leafNode = new DefaultMutableTreeNode(new BookInfo("Refresh", "refresh.html"));
        category.add(leafNode);
        
        //The Third Menu
        category = new DefaultMutableTreeNode(new BookInfo("Server", "server.html"));
        topcate.add(category);

        leafNode = new DefaultMutableTreeNode(new BookInfo("Connect", "connect.html"));
        category.add(leafNode);

        //The Forth Menu
        category = new DefaultMutableTreeNode(new BookInfo("Tool", "tool.html"));
        topcate.add(category);

        leafNode = new DefaultMutableTreeNode(new BookInfo("Notify My Friend", "notify.html"));
        category.add(leafNode);

        leafNode = new DefaultMutableTreeNode(new BookInfo("How To Notify Me", "notifyme.html"));
        category.add(leafNode);
 
        leafNode = new DefaultMutableTreeNode(new BookInfo("I Am Here", "iamhere.html"));
        category.add(leafNode);

        leafNode = new DefaultMutableTreeNode(new BookInfo("Edit Useful Words","editwords.html"));
        category.add(leafNode);

        //The Fifth Menu
        category = new DefaultMutableTreeNode(new BookInfo("Help", "help.html"));
        topcate.add(category);

        leafNode = new DefaultMutableTreeNode(new BookInfo("How To ...", "howto.html"));
        category.add(leafNode);

        leafNode = new DefaultMutableTreeNode(new BookInfo("About GoWorld(Net Version)", "aboutgo.html"));
        category.add(leafNode);
    }

    private void createFaceNodes(DefaultMutableTreeNode top) {
        DefaultMutableTreeNode category = null;
        DefaultMutableTreeNode leafNode = null;

        //The Initial Interface
        leafNode = new DefaultMutableTreeNode(new BookInfo("Animation Interface", "animation.html"));
        top.add(leafNode);

        //The Main Interface
        category = new DefaultMutableTreeNode(new BookInfo("Game Interface", "interface.html"));
        top.add(category);

        leafNode = new DefaultMutableTreeNode(new BookInfo("Go Board", "goboard.html"));
        category.add(leafNode);
        
        leafNode = new DefaultMutableTreeNode(new BookInfo("Configuration", "goconfig.html"));
        category.add(leafNode);

        leafNode = new DefaultMutableTreeNode(new BookInfo("Talking Area", "gotalkarea.html"));
        category.add(leafNode);

        leafNode = new DefaultMutableTreeNode(new BookInfo("System Information Area", "gosystemarea.html"));
        category.add(leafNode);
    }

    private void createEventNodes(DefaultMutableTreeNode top) {
        DefaultMutableTreeNode leafNode = null;

        leafNode = new DefaultMutableTreeNode(new BookInfo("Iconfied Event", "iconfied.html"));
        top.add(leafNode);

        leafNode = new DefaultMutableTreeNode(new BookInfo("Deiconfied Event", "deiconfied.html"));
        top.add(leafNode);
        
        leafNode = new DefaultMutableTreeNode(new BookInfo("Window Closing Event", "windowclosing.html"));
        top.add(leafNode);
    }

    private class BookInfo {
        public String bookName;
        public URL bookURL;
        public String prefix = getHelpPath();
        
        public BookInfo(String book, String filename) {
            bookName = book;
            try {
                bookURL = new URL(prefix + filename);
            } catch (java.net.MalformedURLException exc) {
                System.err.println("Attempted to create a BookInfo with a bad URL: " + bookURL);
                bookURL = null;
            }
        }

        public String toString() {
            return bookName;
        }
    }

    private class UndoableURL extends AbstractUndoableEdit {
        private URL url;
    
        public UndoableURL(URL url) {
            this.url = url;
        }
    
        public String getPresentationName() {
            return url.toString();
        }
    }

    private class URLUndoManager extends CompoundEdit {
        private int indexAdd = 0;
    
        public String getUndoPresentationName() {
            return ((UndoableURL)edits.elementAt(indexAdd - 1)).getPresentationName();
        }
    
        public String getRedoPresentationName() {
            return ((UndoableURL)edits.elementAt(indexAdd)).getPresentationName();
        }
    
        public void addURL(URL newURL) {
            if (edits.size() > indexAdd) {
                edits.setElementAt(new UndoableURL(newURL), indexAdd++);
                for (int i = indexAdd; i < edits.size(); i++) {
                    edits.removeElementAt(i);
                }
            } else {
                edits.addElement(new UndoableURL(newURL));
                indexAdd++;
            }
        }
    
        public URL swapURL(URL newURL) {
            URL oldURL = null;
            try {
                oldURL = new URL(getUndoPresentationName());
            } catch (Exception e) {
                System.err.println("Couldn't create help URL: " + oldURL);
            }
            edits.setElementAt(new UndoableURL(newURL), indexAdd - 1);
            return oldURL;
        }
    
        public synchronized boolean canUndo() {
            if (indexAdd > 0) {
                UndoableURL edit = (UndoableURL)edits.elementAt(indexAdd - 1);
                return (edit != null) && (edit.canUndo());
            }
            return false;
        }
    
        public synchronized boolean canRedo() {
            if ( edits.size() > indexAdd ) {
                UndoableURL edit = (UndoableURL)edits.elementAt(indexAdd);
                return (edit != null) && (edit.canRedo());
            }
            return false;
        }
    
        public synchronized void undo() throws CannotUndoException {
            ((UndoableURL)edits.elementAt(--indexAdd)).undo();
        }
    
        public synchronized void redo() throws CannotRedoException {
            ((UndoableURL)edits.elementAt(indexAdd++)).redo();
        }
    }
}