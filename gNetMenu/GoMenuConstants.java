package gNetMenu;

public interface GoMenuConstants {
    /**
     *  All Menu Item Strings
     */
    public static final String NEWGAMESTR    = "New";
    public static final String CONTINUESTR   = "Open(Continue)";
    public static final String SAVESTR       = "Save";
    public static final String SAVEASSTR     = "Save As...";
    public static final String EXITSTR       = "Exit";
    public static final String BACKGOSTR     = "Undo(Back)";
    public static final String COUNTSTR      = "Count Points Alive";
    public static final String REFRESHSTR    = "Refresh";
    public static final String CONNECTSTR    = "Connect";
    public static final String INFORMSTR     = "Notify My Friend";
    public static final String INFORMTYPESTR = "How To Notify Me";
    public static final String ENDINFORMSTR  = "I Am Here";
    public static final String EDITWORDSSTR  = "Edit Useful Words";
    public static final String HOWTOSTR      = "How To...";
    public static final String ABOUTGOSTR    = "About GoWorld(Net Version)";
    public static final String SEPARATORSTR  = "--";

    /**
     *  Menus and MenuItems
     */
    public static final String[]   goMenuStrs = {"Game", "Operation", "Server", "Tool", "Help"};

    public static final String[]   gameMIStrs = {NEWGAMESTR, "--", CONTINUESTR, "--", SAVESTR, SAVEASSTR, "--", EXITSTR};
    public static final String[]   editMIStrs = {BACKGOSTR, "--", COUNTSTR, "--", REFRESHSTR};
    public static final String[]   servMIStrs = {CONNECTSTR};
    public static final String[]   toolMIStrs = {INFORMSTR, INFORMTYPESTR, ENDINFORMSTR, "--", EDITWORDSSTR};
    public static final String[]   helpMIStrs = {HOWTOSTR, ABOUTGOSTR};
    public static final String[][] goMIStrs   = {gameMIStrs, editMIStrs, servMIStrs, toolMIStrs, helpMIStrs};

    /**
     *  Mnemonic Description
     */
    static final String[]   goMenuMne = {"G", "O", "S", "T", "H"};

    static final String[]   gameMIMne = {"N", "", "O", "", "S", "A", "", "E"};
    static final String[]   editMIMne = {"B", "", "C", "", "R"};
    static final String[]   servMIMne = {"C"};
    static final String[]   toolMIMne = {"N", "H", "I", "", "E"};
    static final String[]   helpMIMne = {"H", "A"};
    static final String[][] goMIMne   = {gameMIMne, editMIMne, servMIMne, toolMIMne, helpMIMne};

    /**
     *  Accelerator Description
     */
    static final String[]   gameMIAcc = {"N", "", "O", "", "S", "A", "", ""};
    static final String[]   editMIAcc = {"B", "", "", "", "R"};
    static final String[]   servMIAcc = {""};
    static final String[]   toolMIAcc = {"", "", "", "", ""};
    static final String[]   helpMIAcc = {"", ""};
    static final String[][] goMIAcc   = {gameMIAcc, editMIAcc, servMIAcc, toolMIAcc, helpMIAcc};

    /**
     *  Menu Items' Initial Status : -1 separator; 0 false; 1 true.
     */
    //                                  新局, --, 续盘, --, 存储, 存作, --, 退出;
    public static final int[]   game  = {0,   -1,  0,  -1,  0,    0,  -1,  1};

    //                                  悔棋, --, 数目, --, 刷新;
    public static final int[]   edit  = {0,   -1,  0,  -1,  1};
  
    //                                  连接
    public static final int[]   serv  = {1};

    //                                  通知对方, 通知方式, 接到通知, --, 编辑常用会话语句;
    public static final int[]   tool  = {0,         1,       0,    -1,       1};

    //                                  如何使用, 关于围棋世界;
    public static final int[]   help   = {1,           1};
    
    public static final int[][] goMenu = {game, edit, serv, tool, help};
}