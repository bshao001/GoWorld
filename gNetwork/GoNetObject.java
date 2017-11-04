package gNetwork;

import java.io.*;
import java.awt.Point;

public class GoNetObject implements Externalizable, GoNetConstants {
    public int     status        = -1;
    public String  talkString    = null;
    public int     playPart      = -1;
    public int     handiNO       = -1;
    public boolean isSelected;
    public Point   playPoint     = null;
    public boolean isAgree;
    public String  answerWhat    = null;
    public boolean isDead;
    public int     blockNumber   = -1;
    public int     blockNO       = -1;
    public byte[]  speakingBytes = null;

    /*
     * construction part 
     */
    public GoNetObject() {
    }

    public GoNetObject(int status) {
        this.status = status;  //PING  
    }

    public GoNetObject(int status, int playPart, int handiNO, boolean isSelected) {
        this.status     = status;    //CONFIG and RECONFIG
        this.playPart   = playPart;
        this.handiNO    = handiNO;
        this.isSelected = isSelected;
    }

    public GoNetObject(int status, Point playPoint) {
        this.status    = status;   //PLAYING
        this.playPoint = playPoint;
    }

    // TALKING + talkString; will be appended in GoTalkArea
    // ASKING  + askingString : such as GoMenuConstants.BACKGOSTR
    // CONFIRM + confirmString; will be apppended in GoSystemArea
    public GoNetObject(int status, String talkString) {
        this.status     = status;  //TALKING and ASKING and CONFIRM
        this.talkString = talkString;
    }

    public GoNetObject(int status, boolean isAgree, String answerWhat) {
        this.status     = status;   //ANSWER
        this.isAgree    = isAgree;  // false: disagree; true: agree.
        this.answerWhat = answerWhat;
    }

    public GoNetObject(int status, Point playPoint, boolean isDead) {
        this.status    = status;   //MARKING
        this.playPoint = playPoint;
        this.isDead    = isDead;
    }

    public GoNetObject(int status, int blockNumber, int blockNO, byte[] speakingBytes) {
        this.status        = status;  //SPEAKING
        this.blockNumber   = blockNumber;
        this.blockNO       = blockNO;
        this.speakingBytes = speakingBytes;
    }

    /*
     * implements two methods
     */
    public void writeExternal(ObjectOutput out) {
        try {
            out.writeInt(status);
            if (status == PING) return;
            if ((status == CONFIG) || (status == RECONFIG)) {
                out.writeInt(playPart);
                out.writeInt(handiNO);
                out.writeBoolean(isSelected);
            } 
            else if (status == PLAYING) {
                out.writeObject(playPoint);
            }
            else if ((status == TALKING) || (status == ASKING) || (status == CONFIRM)) {
                out.writeObject(talkString);
            }
            else if (status == ANSWER) {
                out.writeBoolean(isAgree);
                out.writeObject(answerWhat);
            }
            else if (status == MARKING) {
                out.writeObject(playPoint);
                out.writeBoolean(isDead);
            }
            else if (status == SPEAKING) {
                out.writeInt(blockNumber);
                out.writeInt(blockNO);
                int len;
                if (blockNO == blockNumber) {
                    len = java.lang.reflect.Array.getLength(speakingBytes);
                } else {
                    len = BLOCKLEN;
                }
                out.writeInt(len);
                out.writeObject(speakingBytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readExternal(ObjectInput in) {
        try {
            status = in.readInt();
            if (status == PING) return;
            if ((status == CONFIG) || (status == RECONFIG)) {
                playPart = in.readInt();
                handiNO = in.readInt();
                isSelected = in.readBoolean();
            } 
            else if (status == PLAYING) {
                playPoint = (Point)in.readObject();
            }
            else if ((status == TALKING) || (status == ASKING) || (status == CONFIRM)) {
                talkString = (String)in.readObject();
            }
            else if (status == ANSWER) {
                isAgree = in.readBoolean();
                answerWhat = (String)in.readObject();
            }
            else if (status == MARKING) {
                playPoint = (Point)in.readObject();
                isDead = in.readBoolean();
            }
            else if (status == SPEAKING) {
                blockNumber = in.readInt();
                blockNO = in.readInt();
                int len = in.readInt();
                byte[] tmpBytes = (byte[])in.readObject();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(tmpBytes, 0, len);
                speakingBytes = baos.toByteArray();
                baos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}