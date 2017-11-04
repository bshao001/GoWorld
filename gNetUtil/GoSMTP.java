package gNetUtil;

import java.io.*;
import java.net.*;

public class GoSMTP {
    private final static int SMTP_PORT = 25;
    private InetAddress mailHost;
    private BufferedReader in;
    private PrintWriter out;

    public GoSMTP(String host) throws UnknownHostException {
        mailHost = InetAddress.getByName(host);
    }

    public boolean send(String topic, String content, String from, String to) throws IOException {
        Socket       smtpPipe;
        InputStream  inn;
        OutputStream outt;
        boolean      returnVal = true;

        if ((smtpPipe = new Socket(mailHost, SMTP_PORT)) == null) return false;
        inn = smtpPipe.getInputStream();
        outt = smtpPipe.getOutputStream();

        in = new BufferedReader(new InputStreamReader(inn));
        out = new PrintWriter(new OutputStreamWriter(outt), true);

        System.out.println(in.readLine());
        out.println("HELO " + from);
        System.out.println(in.readLine());
        out.println("MAIL FROM:<" + from + ">");
        System.out.println(in.readLine());
        out.println("RCPT TO:<" + to + ">");
        System.out.println(in.readLine());
        out.println("DATA");
        System.out.println(in.readLine());
        out.println("From: " + from);
        out.println("To: " + to);
        out.println("Subject: " + topic);
        out.println();

        if (content.startsWith(".")) content = "." + content;
        out.println(content);
        out.println();
        out.println(".");
        System.out.println(in.readLine());
        out.println("QUIT");
        String str = in.readLine();
        System.out.println(str);
        if (!str.startsWith("221")) returnVal = false;

        in.close();
        out.close();
        return returnVal;
    }
}