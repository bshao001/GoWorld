package gNetUtil;

import java.io.*;
import javax.sound.sampled.*;

public class GoCapture implements Runnable {
    private String         errStr;
    private TargetDataLine line;
    private Thread         thread;
    private AudioFormat    format;
    private byte[]         audioBytes = null;

    public GoCapture() {
        errStr = null;
        thread = new Thread(this);
        thread.setName("GoCapture");

        //use a default format
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        float rate = 8000.00f;
        int sampleSize = 8;
        String signedString = "signed";
        boolean bigEndian = false;
        int channels = 1;
        format = new AudioFormat(encoding, rate, sampleSize, 
                                 channels, (sampleSize/8)*channels, rate, bigEndian);
    }

    public void start() {
        thread.start();
    }

    public byte[] getAudioBytes() {
        return audioBytes;
    }

    public void stop() {
        thread = null;
    }

    private void shutDown(String message) {
        if ((errStr = message) != null && thread != null) {
            thread = null;
            System.err.println(errStr);
        }
    }
    
    /** 
     * Reads data from the input channel and writes to the output stream
     */
    public void run() {
        // define the required attributes for our line, and make sure a compatible line is supported.
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                        
        if (!AudioSystem.isLineSupported(info)) {
            shutDown("Line matching " + info + " not supported.");
            return;
        }

        // get and open the target data line for capture.
        try {
            line = (TargetDataLine)AudioSystem.getLine(info);
            line.open(format, line.getBufferSize());
        } catch (LineUnavailableException ex) { 
            shutDown("Unable to open the line: " + ex);
            return;
        } catch (Exception ex) { 
            shutDown(ex.toString());
            return;
        }

        // capture the audio data
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int frameSizeInBytes = format.getFrameSize();
        int bufferLengthInFrames = line.getBufferSize() / 8;
        int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
        byte[] data = new byte[bufferLengthInBytes];
        int numBytesRead;
            
        line.start();

        while (thread != null) {
            if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) break;
            out.write(data, 0, numBytesRead);
        }

        // we reached the end of the stream.  stop and close the line.
        line.stop();
        line.close();
        line = null;

        // stop and close the output stream
        try {
            out.flush();
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // prepare audio bytes for later usage
        audioBytes = out.toByteArray();
    }
} 