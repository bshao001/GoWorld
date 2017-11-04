package gNetInterface;

import java.net.URL;
import java.io.*;
import java.awt.*;
import javax.swing.*;

public class GoHelpAboutGo extends JFrame {
    private JEditorPane htmlPane;
    private URL         helpURL;

    public GoHelpAboutGo() {
        super("About the GoWorld(Net Version)");

        htmlPane = new JEditorPane();
        htmlPane.setEditable(false);

        String s = null;
        try {
            s = getHelpPath() + "GoHelpAboutGo.html";
            helpURL = new URL(s);
            displayURL(helpURL);
        } catch (Exception e) {
            System.err.println("Couldn't create help URL: " + s);
        }

        JScrollPane scrollPane = new JScrollPane(htmlPane);
        scrollPane.setPreferredSize(new Dimension(500, 350));

        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    private String getHelpPath() {
        String prefix = "file:///" + System.getProperty("user.dir") + File.separator + "data" + 
						File.separator + "helpFiles" + File.separator;
        return prefix;
    } 

    private void displayURL(URL url) {
        try {
            htmlPane.setPage(url);
        } catch (IOException e) {
            System.err.println("Attempted to read a bad URL: " + url);
        }
    }
}