package gNetUtil;

import java.io.*;
import javax.sound.sampled.*;

public class GoPlayback implements Runnable {
    private final int        bufSize = 16384;
    private AudioInputStream audioInputStream;
    private String           errStr;
    private SourceDataLine   line;
    private Thread           thread;
    private AudioFormat      format;
    private byte[]           audioBytes;

    public GoPlayback(byte[] speakingBytes) {
        errStr = null;
        thread = new Thread(this);
        thread.setName("GoPlayback");

        audioBytes = speakingBytes;

        //use the same default format with the Capture.
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

    public void stop() {
        thread = null;
    }
        
    private void shutDown(String message) {
        if ((errStr = message) != null) System.err.println(errStr);
        if (thread != null) thread = null;
    }

    public void run() {
        ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
        int frameSizeInBytes = format.getFrameSize();
        audioInputStream = new AudioInputStream(bais, format, audioBytes.length / frameSizeInBytes);

        // make sure we have something to play
        if (audioInputStream == null) {
            shutDown("No loaded audio to play back");
            return;
        }
        // reset to the beginnning of the stream
        try {
            audioInputStream.reset();
        } catch (Exception e) {
            shutDown("Unable to reset the stream\n" + e);
            return;
        }

        // define the required attributes for our line, and make sure a compatible line is supported.
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            shutDown("Line matching " + info + " not supported.");
            return;
        }

        // get and open the source data line for playback.
        try {
            line = (SourceDataLine)AudioSystem.getLine(info);
            line.open(format, bufSize);
        } catch (LineUnavailableException ex) { 
            shutDown("Unable to open the line: " + ex);
            return;
        }

        // play back the captured audio data
        int bufferLengthInFrames = line.getBufferSize() / 8;
        int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
        byte[] data = new byte[bufferLengthInBytes];
        int numBytesRead = 0;

        // start the source data line
        line.start();

        while (thread != null) {
            try {
                if ((numBytesRead = audioInputStream.read(data)) == -1) break;
                int numBytesRemaining = numBytesRead;
                while (numBytesRemaining > 0 ) {
                    numBytesRemaining -= line.write(data, 0, numBytesRemaining);
                }
            } catch (Exception e) {
                shutDown("Error during playback: " + e);
                break;
            }
        }
   
        // We reached the end of the stream. Let the data play out, then stop and close the line.
        if (thread != null) line.drain();
        line.stop();
        line.close();
        line = null;
        shutDown(null);
    }
}