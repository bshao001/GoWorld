package gNetUtil;

import java.awt.Font;

public interface GoFont {
    //Used in initial animation
    public static final Font animationFont1 = new Font("Serif", Font.PLAIN, 120);
    public static final Font animationFont2 = new Font("Serif", Font.PLAIN, 72);
    public static final Font animationFont3 = new Font("Serif", Font.PLAIN, 48);
    public static final Font animationFont4 = new Font("Serif", Font.PLAIN, 36);
    public static final Font goFeatureFont1 = new Font("Serif", Font.BOLD, 24);
    public static final Font goFeatureFont2 = new Font("Serif", Font.ITALIC & Font.BOLD, 18);
    public static final Font aniContriFont  = new Font("Serif", Font.PLAIN, 26);
    public static final Font dateFont       = new Font("Serif", Font.ITALIC, 30);

    //Used later
    public static final Font   titleFont      = new Font("SansSerif", Font.PLAIN, 18);
    public static final Font   buttonFont     = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font   labelFont      = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font   menuFont       = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font   talkAreaFont   = new Font("SansSerif", Font.PLAIN, 14);
    public static final String taffName       = "SansSerif";
    public static final int    taffInt        = 14;
    public static final Font   systemAreaFont = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font   helpPaneFont   = new Font("SansSerif", Font.PLAIN, 14);
}
