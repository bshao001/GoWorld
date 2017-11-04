package gNetInterface;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

public class GoPlayFilter extends FileFilter {
    // Accept all directories and all go files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = GoFilterUtils.getExtension(f);
	if (extension != null) {
            if (extension.equals(GoFilterUtils.go)) {
                return true;
            } else {
                return false;
            }
    	}

        return false;
    }
    
    // The description of this filter
    public String getDescription() {
        return "Go Game Files";
    }
}