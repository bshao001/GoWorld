package gNetUtil;

public interface GoString {
    public static final String   queryString = 
        "The system is now asking whether your partner agrees to your request.";
    public static final String   askForSaveString = 
        "The game has been changed. Do you want to save it?";
    public static final String[] askForCountStrings = 
        {"The game will be stopped if you and your partner confirm to ",
         "count the points alive. Do you really want to do so?"};
    public static final String   connectFailedString = 
        "Connetion failed and the game will not be continued successfully.";
    public static final String[] defaultWords = {"Hi.",
                                                 "Long time no see.",
                                                 "Glad to play with you again.",
                                                 "Play next time.",
                                                 "See you."};
}