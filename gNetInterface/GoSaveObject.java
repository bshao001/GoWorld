package gNetInterface;

import java.io.Serializable;
import java.awt.Point;

public class GoSaveObject implements Serializable {
    public int     playPart   = -1;
    public int     handiNO    = -1;
    public boolean isSelected;
    public Point   playPoint  = null;

    public GoSaveObject(int playPart, int handiNO, boolean isSelected) {
        this.playPart   = playPart;
        this.handiNO    = handiNO;
        this.isSelected = isSelected;
    }

    public GoSaveObject(Point playPoint) {
        this.playPoint = playPoint;
    }
}