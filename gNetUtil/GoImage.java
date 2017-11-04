package gNetUtil;

import java.awt.*;

public class GoImage {
    public static Image load(String name, Component cmp) {
        Image img = null;
        img = cmp.getToolkit().createImage(System.getProperty("user.dir") + 
                                           System.getProperty("file.separator") + "data" + 
										   System.getProperty("file.separator") + "images" +
                                           System.getProperty("file.separator") + name);
        MediaTracker tracker = new MediaTracker(cmp);
        tracker.addImage(img, 0);
        try {
            tracker.waitForID(0);
            if (tracker.isErrorAny()) {
                System.out.println("Error loading image " + name);
            }
        } catch (Exception ex) { 
            ex.printStackTrace(); 
        }
        return img;
    }
}