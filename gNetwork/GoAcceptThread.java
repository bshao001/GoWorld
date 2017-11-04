package gNetwork;

import java.io.*;
import java.net.*;

public class GoAcceptThread extends Thread {
    GoNetClient      goNetClient;
    GoAcceptListener accepter = null;
    GoNetObject      acceptObject;

    public GoAcceptThread(GoNetClient goNetClient) {
        super("GoAcceptThread");
        this.goNetClient = goNetClient;
    }
    
    public void run() {
        while (true) {
            try {
                acceptObject = goNetClient.acceptFromServer();
            } catch (SocketException soEx) {
                System.err.println("Socket Exception in line 28 of GoAcceptThread.java");

                acceptObject = new GoNetObject(GoNetConstants.CONFIRM, GoNetConstants.MYSOCKETERRORSTR);
                if (accepter != null) accepter.accept(acceptObject);
                break;
            } catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
            
            if (accepter != null) accepter.accept(acceptObject);
        }
    }

    /*public GoAcceptListener getAccepter() {
        if (accepter != null) return accepter;
        else return null;
    }*/

    public void setAccepter(GoAcceptListener accepter) {
        this.accepter = accepter;
    }
}