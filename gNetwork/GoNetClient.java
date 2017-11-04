package gNetwork;

import java.io.*;
import java.net.*;

public class GoNetClient {
    private Socket              goSocket = null;
    private ObjectOutputStream  goNetOut = null;
    private ObjectInputStream   goNetIn  = null;

    public  int                 retValue;

    public GoNetClient(String serverName, int serverPort) {
        try {
            goSocket = new Socket(InetAddress.getByName(serverName), serverPort);
            goNetOut = new ObjectOutputStream(goSocket.getOutputStream());
            goNetIn  = new ObjectInputStream(goSocket.getInputStream());
            retValue = 0;
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + serverName + ".");
            retValue = -1;
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + serverName + ".");
            retValue = -2;
        }
    }

    public GoNetObject acceptFromServer() throws OptionalDataException, ClassNotFoundException, IOException {
        GoNetObject fromServer;
        if ((fromServer = (GoNetObject)goNetIn.readObject()) != null) {
            return fromServer;
        }
        return null;
    }

    public boolean sendToServer(GoNetObject fromUser) {
        try {
            goNetOut.writeObject(fromUser);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            return false;
        }
        return true;
    }
}