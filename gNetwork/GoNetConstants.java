package gNetwork;

public interface GoNetConstants {
    public static final int PING     = 1;
    public static final int CONFIG   = 2;
    public static final int RECONFIG = 3;
    public static final int PLAYING  = 4;
    public static final int TALKING  = 5;
    public static final int ASKING   = 6;
    public static final int CONFIRM  = 7;
    public static final int ANSWER   = 8;
    public static final int MARKING  = 9;
    public static final int SPEAKING = 10;

    public static final int END      = 100;

    //CONFIRM what?
    public static final String ICONFIEDSTR       = "ICONFIED";
    public static final String DEICONFIEDSTR     = "DEICONFIED";
    public static final String FILEOPENOKSTR     = "FILEOPENOK";
    public static final String FILEOPENCANCELSTR = "FILEOPENCANCEL";
    public static final String FILEOPENERRORSTR  = "FILEOPENERROR";  
    public static final String INFORMTOCOMESTR   = "AREYOUTHERE";
    public static final String INFORMCOMINGSTR   = "IAMHERENOW";
    public static final String EXITSTR           = "Exit";
    public static final String MYSOCKETERRORSTR  = "MYSOCKETEXCEPTION";
    public static final String HESOCKETERRORSTR  = "HESOCKETEXCEPTION";
    public static final String HENETRESTOREDSTR  = "HESOCKETRESTORED";

    //ASKING what?
    public static final String NEWGAMESTR  = "new a game";
    public static final String CONTINUESTR = "open(continue) a former game";
    public static final String BACKGOSTR   = "back a step";
    public static final String COUNTSTR    = "count points alive";
    public static final String ENDMARKSTR  = "end marking";

    //ANSWER what?
    public static final boolean DISAGREE = false;
    public static final boolean AGREE    = true;

    //Byte Length of one block of speaking data
    public static final int BLOCKLEN = 200000; 
}