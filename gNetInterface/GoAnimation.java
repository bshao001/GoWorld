package gNetInterface;

import gNetUtil.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.font.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import java.util.Vector;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;

import java.net.*;

public class GoAnimation extends JPanel {
    private GoASurface surface;

    public boolean isAnimating;

    public GoAnimation() {
        EmptyBorder eb = new EmptyBorder(50, 80, 10, 80);
        BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
        setBorder(new CompoundBorder(eb,bb));
        setLayout(new BorderLayout());
        setBackground(Color.gray);
        isAnimating = true;
        add(surface = new GoASurface(this));

        setToolTipText("click to stop animation");
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
               if (!surface.isShowing()) return;

               if ((isAnimating = !isAnimating)) {
                   setToolTipText("click to stop animation");
                   start();
               } else {
                   setToolTipText("click to start animation");
                   stop();
               }
            }
        });
    }

    public void start() {
        if (surface != null) surface.start();
    }

    public void stop() {
        if (surface != null) surface.stop();
    }
} // End GoAnimation class

/**
  * GoASurface is the stage where the GoADirector plays its scenes.
  */
class GoASurface extends JPanel implements Runnable {
    public static Image duck_director, java_logo;
    public static BufferedImage bimg;

    private static GoASurface surf;
    private GoADirector director;
    private int index;
    private long sleepAmt = 30;
    private Thread thread;
  
    private GoAnimation outer;

    public GoASurface(GoAnimation outPanel) {
        surf  = this;
        outer = outPanel;
        setBackground(GoAPart.black);
        setLayout(new BorderLayout());
        
        duck_director = GoImage.load("duck_director.gif", this);
        java_logo = GoImage.load("java_logo.gif", this);
        director = new GoADirector();
    }

    static FontMetrics getMetrics(Font font) {
        return surf.getFontMetrics(font);
    }

    public void paint(Graphics g) {
        Dimension d = getSize();
        if (bimg == null || bimg.getWidth() != d.width || bimg.getHeight() != d.height) {
            bimg = (BufferedImage)createImage(d.width, d.height);  

            // reset future scenes
            for (int i = index+1; i < director.size(); i++) {
                ((GoAScene)director.get(i)).reset(d.width, d.height);
            }
        }

        GoAScene scene = (GoAScene)director.get(index);
        if (scene.index <= scene.length) {
            if (thread != null) {
                scene.step(d.width, d.height);
            }

            Graphics2D g2 = bimg.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setBackground(getBackground());
            g2.clearRect(0, 0, d.width, d.height);

            scene.render(d.width, d.height, g2);

            if (thread != null) scene.index++; // increment scene.index after scene.render
            g2.dispose();
        }
        g.drawImage(bimg, 0, 0, this);
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setName("GoAnimation");
            thread.start();
        }
    }

    public synchronized void stop() {
        if (thread != null) {
            thread.interrupt();
        }
        thread = null;
        notifyAll();
    }

    public void reset() {
        index = 0;
        Dimension d = getSize();
        for (int i = 0; i < director.size(); i++) {
            ((GoAScene)director.get(i)).reset(d.width, d.height);
        }
    }

    public void run() {
        Thread me = Thread.currentThread();

        while (thread == me && !isShowing() || getSize().width <= 0) {
            try {
                thread.sleep(500);
            } catch (InterruptedException e) {
                return;
            }
        }

        if (index == 0) reset();

        while (thread == me) {
            GoAScene scene = (GoAScene)director.get(index);

            repaint();

            try {
                thread.sleep(sleepAmt);
            } catch (InterruptedException e) {
                break; 
            }
            if (scene.index > scene.length) {
                scene.pause(thread);
                if (++index >= director.size()) reset();
            }
        }
        thread = null;
    }
} // End GoASurface class

/**
  * GoAPart is a piece of the scene.  Classes must implement GoAPart
  * in order to participate in a scene.
  */
interface GoAPart {
    public static Color black  = new Color(20, 20, 20); 
    public static Color white  = new Color(240, 240, 255); 
    public static Color red    = new Color(149, 43, 42);
    public static Color blue   = new Color(94, 105, 176); 
    public static Color yellow = new Color(255, 255, 140);

    public void reset(int newwidth, int newheight);
    public void step(int w, int h);
    public void render(int w, int h, Graphics2D g2);
    public int getBegin();
    public int getEnd();
} // End GoAPart interface

/**
  * GoADirector is the holder of the scenes, their names & pause amounts
  * between scenes.
  */
class GoADirector extends Vector<GoAScene> {
    GradientPaint gp = new GradientPaint(0, 40, GoAPart.blue, 38, 2, GoAPart.black);
    Font f1 = GoFont.animationFont1;
    Font f2 = GoFont.animationFont2;
    Font f3 = GoFont.animationFont3;
    Font f4 = GoFont.animationFont4;
    Object parts[][][] = {
       { { "W - scale text on gradient", "200" },
         { new GoAGpE(GoAGpE.BURI, GoAPart.black, GoAPart.blue, 0, 20),
           new GoATxE("G", f1, GoATxE.SCI, GoAPart.yellow, 2, 20) } },
       { { "Q - scale & rotate text on gradient" , "0" },
         { new GoAGpE(GoAGpE.BURI, GoAPart.blue, GoAPart.black, 0, 22),
           new GoATxE("O", f1, GoATxE.RI | GoATxE.SCI, GoAPart.yellow, 2, 22) } },
       { { "S - scale text on gradient", "0" },
         { new GoAGpE(GoAGpE.BURI, GoAPart.black, GoAPart.blue, 0, 20),
           new GoATxE("GO", f1, GoATxE.SCI, GoAPart.yellow, 2, 20) } },
       { { "J - scale & rotate text on gradient" , "0" },
         { new GoAGpE(GoAGpE.BURI, GoAPart.blue, GoAPart.black, 0, 22),
           new GoATxE("WORLD", f1, GoATxE.RI | GoATxE.SCI, GoAPart.yellow, 2, 22) } },
       { { "WQSJ - scale text on gradient", "0" },
         { new GoAGpE(GoAGpE.BURI, GoAPart.black, GoAPart.blue, 0, 20),
           new GoATxE("GOWORLD", f2, GoATxE.SCI, GoAPart.yellow, 2, 20) } },
       { { "WQSJ - scale & rotate text on gradient", "1000" },
         { new GoAGpE(GoAGpE.SIH, GoAPart.blue, GoAPart.black, 0, 40),
           new GoATxE("GOWORLD", f2, GoATxE.RI | GoATxE.SCI, GoAPart.yellow, 0, 40) } },
       { { "Previous scene dither dissolve out", "0"},
         { new GoADdE(0, 20, 1) } },
       { { "WQ Features 1", "999" },
         { new GoATemp(GoATemp.RECT, null, 0, 15),
           new GoATemp(GoATemp.IMG, GoASurface.java_logo, 2, 15),
           new GoATemp(GoATemp.RNA | GoATemp.INA, GoASurface.java_logo, 16, 195),
           new GoAFeatures(GoAFeatures.QIJU, 16, 195) } },
       { { "HYLDWQSJ - texture text on gradient", "1000"},
         { new GoAGpE(GoAGpE.WI, GoAPart.blue, GoAPart.black, 0, 20),
           new GoAGpE(GoAGpE.WD, GoAPart.blue, GoAPart.black, 21, 40),
           new GoATpE(GoATpE.OI | GoATpE.NF, GoAPart.black, GoAPart.yellow, 4, 0, 10),
           new GoATpE(GoATpE.OD | GoATpE.NF, GoAPart.black, GoAPart.yellow, 4, 11, 20),
           new GoATpE(GoATpE.OI | GoATpE.NF | GoATpE.HAF, GoAPart.black, GoAPart.yellow, 5, 21, 40),
           new GoATxE("WELCOME TO GOWORLD !", f4, 0, null, 0, 40) } },
       { { "Previous scene random close out", "0"},
         { new GoACoE(GoACoE.RAND, 0, 20) } },
       { { "WQ Features 2", "999" },
         { new GoATemp(GoATemp.RECT, null, 0, 15),
           new GoATemp(GoATemp.IMG, GoASurface.java_logo, 2, 15),
           new GoATemp(GoATemp.RNA | GoATemp.INA, GoASurface.java_logo, 16, 130),
           new GoAFeatures(GoAFeatures.DESUAN, 16, 130) } },
       { { "HYLDWQSJ - composite text on texture", "1000"},
         { new GoATpE(GoATpE.RI, GoAPart.black, gp, 40, 0, 20),
           new GoATpE(GoATpE.RD, GoAPart.black, gp, 40, 21, 40),
           new GoATpE(GoATpE.RI, GoAPart.black, gp, 40, 41, 60),
           new GoATxE("WELCOME TO GOWORLD !", f4, GoATxE.AC, GoAPart.yellow, 0, 60) } },
       { { "Previous scene dither dissolve out", "0"},
         { new GoADdE(0, 20, 4) } },
       { { "WQ Features 3", "999" },
         { new GoATemp(GoATemp.RECT, null, 0, 15),
           new GoATemp(GoATemp.IMG, GoASurface.java_logo, 2, 15),
           new GoATemp(GoATemp.RNA | GoATemp.INA, GoASurface.java_logo, 16, 145),
           new GoAFeatures(GoAFeatures.QUANYU, 16, 145) } },
       { { "HYLDWQSJ - text on gradient", "1000" },
         { new GoAGpE(GoAGpE.SDH, GoAPart.blue, GoAPart.black, 0, 20),
           new GoAGpE(GoAGpE.SIH, GoAPart.blue, GoAPart.black, 21, 40),
           new GoAGpE(GoAGpE.SDH, GoAPart.blue, GoAPart.black, 41, 50),
           new GoAGpE(GoAGpE.INC | GoAGpE.NF, GoAPart.red, GoAPart.yellow, 0, 50),
           new GoATxE("WELCOME TO GOWORLD !", f4, GoATxE.NOP, null, 0, 50) } },
       { { "Previous scene ellipse close out", "0"},
         { new GoACoE(GoACoE.OVAL, 0, 20) } },
       { { "WQ Features 4", "999" },
         { new GoATemp(GoATemp.RECT, null, 0, 15),
           new GoATemp(GoATemp.IMG, GoASurface.java_logo, 2, 15),
           new GoATemp(GoATemp.RNA | GoATemp.INA, GoASurface.java_logo, 16, 300),
           new GoAFeatures(GoAFeatures.HEZHAN, 16, 300) } },
       { { "HYLDWQSJ - composite and rotate text on paints", "2000" },
         { new GoAGpE(GoAGpE.BURI, GoAPart.black, GoAPart.blue, 0, 20),
           new GoAGpE(GoAGpE.BURD, GoAPart.black, GoAPart.blue, 21, 30),
           new GoATpE(GoATpE.OI | GoATpE.HAF, GoAPart.black, GoAPart.blue, 10, 31, 40),
           new GoATxE("WELCOME TO GOWORLD !", f4, GoATxE.AC | GoATxE.RI, GoAPart.yellow, 0, 40) }},
       { { "Previous scene subimage transform out", "0" },
         { new GoASiE(60, 60, 0, 40) } },
       { { "GOWORLD - transform in", "1000" },
         { new GoALnE(GoALnE.ACI | GoALnE.ZOOMI | GoALnE.RI, 0, 60),
           new GoATxE("GOWORLD", f3, GoATxE.AC | GoATxE.SCI, Color.red, 20, 30),
           new GoATxE("GOWORLD", f3, GoATxE.SCXD, Color.red, 31, 38),
           new GoATxE("GOWORLD", f3, GoATxE.SCXI, Color.red, 39, 48),
           new GoATxE("GOWORLD", f3, GoATxE.SCXD, Color.red, 49, 54),
           new GoATxE("GOWORLD", f3, GoATxE.SCXI, Color.red, 55, 60) } },
       { { "GOWORLD - transform out", "0" },
         { new GoALnE(GoALnE.ACD | GoALnE.ZOOMD | GoALnE.RD, 0, 45),
           new GoATxE("GOWORLD", f3, 0, Color.red, 0, 9),
           new GoATxE("GOWORLD", f3, GoATxE.SCD | GoATxE.RD, Color.red, 10, 30) } },
       { { "Contributors", "1000" },
         { new GoATemp(GoATemp.RECT, null, 0, 15),
           new GoATemp(GoATemp.IMG, GoASurface.duck_director, 4, 15),
           new GoATemp(GoATemp.RNA | GoATemp.INA, GoASurface.duck_director, 16, 195),
           new GoAContributors(16, 195) } },
    };

    public GoADirector() {
        for (int i = 0; i < parts.length; i++) {
            Vector<GoAPart> v = new Vector<GoAPart>();
            for (int j = 0; j < parts[i][1].length; j++) {
                v.addElement((GoAPart)parts[i][1][j]);
            }
            addElement(new GoAScene(v, parts[i][0][0], parts[i][0][1]));
        }
    }
} // End GoADirector class
    
/**
  * GoAScene is the manager of the parts.
  */
class GoAScene extends Object {
    public Vector<GoAPart> parts;
    public Object name;
    public Object pauseAmt;
    public int index;
    public int length;

    public GoAScene(Vector<GoAPart> parts, Object name, Object pauseAmt) {
        this.parts = parts;
        this.name = name;
        this.pauseAmt = pauseAmt;
        for (int i = 0; i < parts.size(); i++) {
            if (((GoAPart)parts.get(i)).getEnd() > length) {
                length = ((GoAPart)parts.get(i)).getEnd();
            }
        }
    }

    public void reset(int w, int h) {
        index = 0;
        for (int i = 0; i < parts.size(); i++) {
            ((GoAPart)parts.get(i)).reset(w, h);
        }
    }

    public void step(int w, int h) {
        for (int i = 0; i < parts.size(); i++) {
            GoAPart part = (GoAPart)parts.get(i);
            if (index >= part.getBegin() && index <= part.getEnd()) {
                part.step(w, h);
            }
        }
    }

    public void render(int w, int h, Graphics2D g2) {
        for (int i = 0; i < parts.size(); i++) {
            GoAPart part = (GoAPart) parts.get(i);
            if (index >= part.getBegin() && index <= part.getEnd()) {
                part.render(w, h, g2);
            }
        }
    }

    public void pause(Thread thread) {
        try {
            thread.sleep(Long.parseLong((String) pauseAmt));
        } catch (Exception e) { }
        System.gc();
    }
} // End GoAScene class

/**
 * Text Effect. Transformation of characters. Clip or fill.
 */
class GoATxE implements GoAPart {
    static final int INC  = 1;
    static final int DEC  = 2;
    static final int R    = 4;            // rotate
    static final int RI   = R | INC;
    static final int RD   = R | DEC;
    static final int SC   = 8;            // scale
    static final int SCI  = SC | INC;
    static final int SCD  = SC | DEC;    
    static final int SCX  = 16;           // scale invert x
    static final int SCXI = SCX | SC | INC;
    static final int SCXD = SCX | SC | DEC; 
    static final int SCY  = 32;           // scale invert y
    static final int SCYI = SCY | SC | INC;
    static final int SCYD = SCY | SC | DEC; 
    static final int AC   = 64;           // AlphaComposite
    static final int CLIP = 128;          // Clipping
    static final int NOP  = 512;          // No Paint 

    private int beginning, ending;
    private int type;
    private double rIncr, sIncr;
    private double sx, sy, rotate;
    private Shape shapes[], txShapes[];
    private int sw;
    private int numRev;
    private Paint paint;

    public GoATxE(String text, Font font, int type, Paint paint, int beg, int end) {
        this.type = type;
        this.paint = paint;
        this.beginning = beg;
        this.ending = end;

        setIncrements(2);
        
        char[] chars = text.toCharArray();
        shapes = new Shape[chars.length];
        txShapes = new Shape[chars.length];
        FontRenderContext frc = new FontRenderContext(null, true, true);
        TextLayout tl = new TextLayout(text, font, frc);
        sw = (int)tl.getOutline(null).getBounds().getWidth();
        for (int j = 0; j < chars.length; j++) {
            String s = String.valueOf(chars[j]);
            shapes[j] = new TextLayout(s, font, frc).getOutline(null);
        }
    }

    public void setIncrements(double numRevolutions) {
        this.numRev = (int) numRevolutions;
        rIncr = 360.0 / ((ending - beginning) / numRevolutions);
        sIncr = 1.0 / (ending - beginning);
        if ((type & SCX) != 0 || (type & SCY) != 0) {
            sIncr *= 2;
        }
        if ((type & DEC) != 0) {
            rIncr = -rIncr;
            sIncr = -sIncr;
        }
    }

    public void reset(int w, int h) {
        if (type == SCXI) {
            sx = -1.0;  sy = 1.0;
        } else if (type == SCYI) {
            sx = 1.0;  sy = -1.0;
        } else {
            sx = sy = (type & DEC) != 0 ? 1.0 : 0.0;  
        }
        rotate = 0;
    }

    public void step(int w, int h) {
        float charWidth = w/2 - sw/2;

        for (int i = 0; i < shapes.length; i++) {
            AffineTransform at = new AffineTransform();
            Rectangle2D maxBounds = shapes[i].getBounds();
            at.translate(charWidth, h/2 + maxBounds.getHeight()/2);
            charWidth += (float)maxBounds.getWidth() + 1;
            Shape shape = at.createTransformedShape(shapes[i]);
            Rectangle2D b1 = shape.getBounds2D();

            if ((type & R) != 0) {
                at.rotate(Math.toRadians(rotate)); 
            }
            if ((type & SC) != 0) {
                at.scale(sx, sy);
            }
            shape = at.createTransformedShape(shapes[i]);
            Rectangle2D b2 = shape.getBounds2D();

            double xx = (b1.getX() + b1.getWidth()/2) - (b2.getX() + b2.getWidth()/2);
            double yy = (b1.getY() + b1.getHeight()/2) - (b2.getY() + b2.getHeight()/2);
            AffineTransform toCenterAT = new AffineTransform();
            toCenterAT.translate(xx, yy);
            toCenterAT.concatenate(at);
            txShapes[i] = toCenterAT.createTransformedShape(shapes[i]);
        }
        // avoid over rotation
        if (Math.abs(rotate) <= numRev * 360) {
            rotate += rIncr;
            if ((type & SCX) != 0) {
                sx += sIncr;
            } else if ((type & SCY) != 0) {
                sy += sIncr;
            } else {
                sx += sIncr; sy += sIncr;
            }
        }
    }

    public void render(int w, int h, Graphics2D g2) {
        Composite saveAC = null;
        if ((type & AC) != 0 && sx > 0 && sx < 1) {
            saveAC = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)sx));
        }
        GeneralPath path = null;
        if ((type & CLIP) != 0) {
            path = new GeneralPath();
        }
        if (paint != null) {
            g2.setPaint(paint);
        }
        for (int i = 0; i < txShapes.length; i++) {
            if ((type & CLIP) != 0) {
               path.append(txShapes[i], false);
            } else {
               g2.fill(txShapes[i]);
            }
        }
        if ((type & CLIP) != 0) {
            g2.clip(path);
        }
        if (saveAC != null) {
           g2.setComposite(saveAC);
        }
    }

    public int getBegin() {
        return beginning;
    }

    public int getEnd() {
        return ending;
    }
} // End GoATxE class

/**
 * GradientPaint Effect. Burst, split, horizontal and vertical gradient fill effects.
 */
class GoAGpE implements GoAPart {
    static final int INC = 1;             // increasing
    static final int DEC = 2;             // decreasing
    static final int CNT = 4;             // center
    static final int WID = 8;             // width 
    static final int WI  = WID | INC;             
    static final int WD  = WID | DEC;            
    static final int HEI = 16;            // height
    static final int HI  = HEI | INC;            
    static final int HD  = HEI | DEC;            
    static final int SPL = 32 | CNT;      // split 
    static final int SIW = SPL | INC | WID;
    static final int SDW = SPL | DEC | WID;
    static final int SIH = SPL | INC | HEI;
    static final int SDH = SPL | DEC | HEI;
    static final int BUR = 64 | CNT;     // burst 
    static final int BURI = BUR | INC;    
    static final int BURD = BUR | DEC;   
    static final int NF = 128;           // no fill

    private Color c1, c2;
    private int beginning, ending;
    private float incr, index;
    private Vector<Rectangle2D.Float> rect = new Vector<Rectangle2D.Float>();
    private Vector<GradientPaint> grad = new Vector<GradientPaint>();
    private int type;

    public GoAGpE(int type, Color c1, Color c2, int beg, int end) {
        this.type = type;
        this.c1 = c1;
        this.c2 = c2;
        this.beginning = beg;
        this.ending = end;
    }

    public void reset(int w, int h) {
        incr = 1.0f / (ending - beginning);
        if ((type & CNT) != 0) {
            incr /= 2.3f;
        }
        if ((type & CNT) != 0 && (type & INC) != 0) {
            index = 0.5f;
        } else if ((type & DEC) != 0) {
            index = 1.0f;
            incr = -incr;
        } else {
            index = 0.0f;
        }
        index += incr;
    }

    public void step(int w, int h) {
        rect.clear();
        grad.clear();

        if ((type & WID) != 0) {
            float w2 = 0, x1 = 0, x2 = 0;
            if ((type & SPL) != 0) {
                w2 = w * 0.5f;
                x1 = w * (1.0f - index);
                x2 = w * index;
            } else {
                w2 = w * index;
                x1 = x2 = w2;
            }
            rect.addElement(new Rectangle2D.Float(0, 0, w2, h));
            rect.addElement(new Rectangle2D.Float(w2, 0, w-w2, h));
            grad.addElement(new GradientPaint(0,0,c1,x1,0,c2));
            grad.addElement(new GradientPaint(x2,0,c2,w,0,c1));
        } else if ((type & HEI) != 0) {
            float h2 = 0, y1 = 0, y2 = 0;
            if ((type & SPL) != 0) {
                h2 = h * 0.5f;
                y1 = h * (1.0f - index);
                y2 = h * index;
            } else {
                h2 = h * index;
                y1 = y2 = h2;
            }
            rect.addElement(new Rectangle2D.Float(0, 0, w, h2));
            rect.addElement(new Rectangle2D.Float(0, h2, w, h-h2));
            grad.addElement(new GradientPaint(0,0,c1,0,y1,c2));
            grad.addElement(new GradientPaint(0,y2,c2,0,h,c1));
        } else if ((type & BUR) != 0) {
            float w2 = w/2;
            float h2 = h/2;

            rect.addElement(new Rectangle2D.Float(0, 0, w2, h2));
            rect.addElement(new Rectangle2D.Float(w2, 0, w2, h2));
            rect.addElement(new Rectangle2D.Float(0, h2, w2, h2));
            rect.addElement(new Rectangle2D.Float(w2, h2, w2, h2));

            float x1 = w * (1.0f - index);
            float x2 = w * index;
            float y1 = h * (1.0f - index);
            float y2 = h * index;

            grad.addElement(new GradientPaint(0,0,c1,x1,y1,c2));
            grad.addElement(new GradientPaint(w,0,c1,x2,y1,c2));
            grad.addElement(new GradientPaint(0,h,c1,x1,y2,c2));
            grad.addElement(new GradientPaint(w,h,c1,x2,y2,c2));
        } else if ((type & NF) != 0) {
            float x = w * index;
            float y = h * index;
            grad.addElement(new GradientPaint(0,0,c1,0,y,c2));
        }

        if ((type & INC) != 0 || (type & DEC) != 0) {
            index += incr;
        }
    }

    public void render(int w, int h, Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        for (int i = 0; i < grad.size(); i++) {
            g2.setPaint((GradientPaint) grad.get(i));
            if ((type & NF) == 0) {
                g2.fill((Rectangle2D) rect.get(i));
            }
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public int getBegin() {
        return beginning;
    }

    public int getEnd() {
        return ending;
    }
} // End GoAGpE class

/**
 * TexturePaint Effect. Expand and collapse a texture. 
 */
class GoATpE implements GoAPart {
    static final int INC = 1;             // increasing
    static final int DEC = 2;             // decreasing
    static final int OVAL = 4;            // oval
    static final int RECT = 8;            // rectangle 
    static final int HAF = 16;            // half oval or rect size
    static final int OI = OVAL | INC; 
    static final int OD = OVAL | DEC;
    static final int RI = RECT | INC;
    static final int RD = RECT | DEC;
    static final int NF = 32;             // no fill 
    private Paint p1, p2;
    private int beginning, ending;
    private float incr, index;
    private TexturePaint texture;
    private int type;
    private int size;
    private BufferedImage bimg;
    private Rectangle rect;

    public GoATpE(int type, Paint p1, Paint p2, int size, int beg, int end) {
        this.type = type;
        this.p1 = p1;
        this.p2 = p2;
        this.beginning = beg;
        this.ending = end;
        setTextureSize(size);
    }

    public void setTextureSize(int size) {
        this.size = size;
        bimg = new BufferedImage(size,size,BufferedImage.TYPE_INT_RGB);
        rect = new Rectangle(0,0,size,size);
    }

    public void reset(int w, int h) {
        incr = (float) (size) / (float) (ending - beginning);
        if ((type & HAF) != 0) {
           incr /= 2;
        }
        if ((type & DEC) != 0) {
            index = size;
            if ((type & HAF) != 0) {
               index /= 2;
            }
            incr = -incr;
        } else {
            index = 0.0f;
        }
        index += incr;
    }

    public void step(int w, int h) {
        Graphics2D g2 = bimg.createGraphics();
        g2.setPaint(p1);
        g2.fillRect(0,0,size,size);
        g2.setPaint(p2);
        if ((type & OVAL) != 0) {
            g2.fill(new Ellipse2D.Float(0, 0, index, index));
        } else if ((type & RECT) != 0) {
            g2.fill(new Rectangle2D.Float(0, 0, index, index));
        }
        texture = new TexturePaint(bimg, rect);
        g2.dispose();
        index += incr;
    }

    public void render(int w, int h, Graphics2D g2) {
        g2.setPaint(texture);
        if ((type & NF) == 0) {
            g2.fillRect(0, 0, w, h);
        }
    }

    public int getBegin() {
        return beginning;
    }

    public int getEnd() {
        return ending;
    }
} // End GoATpE class

/**
 * Close out effect. Close out the buffered image with different geometry shapes.
 */
class GoACoE implements GoAPart {
    static final int WID  = 1;            
    static final int HEI  = 2;           
    static final int OVAL = 4;            
    static final int RECT = 8;           
    static final int RAND = 16;           
    static final int ARC  = 32;           
    private int type;
    private int beginning, ending;
    private BufferedImage bimg;
    private Shape shape;
    private double zoom, extent;
    private double zIncr, eIncr;
    private boolean doRandom;

    public GoACoE(int type, int beg, int end) {
        this.type = type;
        this.beginning = beg;
        this.ending = end;
        zIncr = -(2.0 / (ending - beginning));
        eIncr = 360.0 / (ending - beginning);
        doRandom = (type & RAND) != 0;
    }

    public void reset(int w, int h) {
        if (doRandom) {
            int num = (int) (Math.random() * 5.0);
            switch (num) {
                case 0 : type = OVAL; break;
                case 1 : type = RECT; break;
                case 2 : type = RECT | WID; break;
                case 3 : type = RECT | HEI; break;
                case 4 : type = ARC; break;
                default : type = OVAL; 
            }
        }
        shape = null;
        bimg = null;
        extent = 360.0;
        zoom = 2.0;
    }

    public void step(int w, int h) {
        if (bimg == null) {
            int biw = GoASurface.bimg.getWidth();
            int bih = GoASurface.bimg.getHeight();
            bimg = new BufferedImage(biw, bih, BufferedImage.TYPE_INT_RGB);
            Graphics2D big = bimg.createGraphics();
            big.drawImage(GoASurface.bimg, 0, 0, null);
        }
        double z = Math.min(w, h) * zoom;
        if ((type & OVAL) != 0) {
            shape = new Ellipse2D.Double(w/2 - z/2, h/2 - z/2, z, z);
        } else if ((type & ARC) != 0) {
            shape = new Arc2D.Double(-100, -100, w + 200, h + 200, 90, extent, Arc2D.PIE);
            extent -= eIncr;
        } else if ((type & RECT) != 0) {
            if ((type & WID) != 0) {
                shape = new Rectangle2D.Double(w/2 - z/2, 0, z, h);
            } else if ((type & HEI) != 0) {
                shape = new Rectangle2D.Double(0, h/2 - z/2, w, z);
            } else {
                shape = new Rectangle2D.Double(w/2 - z/2, h/2 - z/2, z, z);
            }
        }
        zoom += zIncr;
    }

    public void render(int w, int h, Graphics2D g2) {
        g2.clip(shape);
        g2.drawImage(bimg, 0, 0, null);
    }

    public int getBegin() {
        return beginning;
    }

    public int getEnd() {
        return ending;
    }
} // End GoACoE class

/**
 * Dither Dissolve Effect. For each successive step in the animation, a pseudo-random starting
 * horizontal position is chosen using list, and then the corresponding points created from xlist
 * and ylist are blacked out for the current "chunk".  The x and y chunk starting positions are 
 * each incremented by the associated chunk size, and this process is repeated for the number of 
 * "steps" in the animation, causing an equal number of pseudo-randomly picked "blocks" to be 
 * blacked out during each step of the animation.
 */
class GoADdE implements GoAPart {
    private int beginning, ending;
    private BufferedImage bimg;
    private Graphics2D big;
    private List list, xlist, ylist;
    private int xeNum, yeNum;    // element number
    private int xcSize, ycSize;  // chunk size
    private int inc;
    private int blocksize;

    public GoADdE(int beg, int end, int blocksize) {
        this.beginning = beg;
        this.ending = end;
        this.blocksize = blocksize; 
    }

    private void createShuffledLists() {
        int width = bimg.getWidth();
        int height = bimg.getHeight();
        Integer xarray[] = new Integer[width];
        Integer yarray[] = new Integer[height];
        Integer array[] = new Integer[ending - beginning + 1];
        for (int i = 0; i < xarray.length; i++) {
            xarray[i] = new Integer(i);
        }
        for (int j = 0; j < yarray.length; j++) {
            yarray[j] = new Integer(j);
        }
        for (int k = 0; k < array.length; k++) {
            array[k] = new Integer(k);
        } 
        Collections.shuffle(xlist = Arrays.asList(xarray));
        Collections.shuffle(ylist = Arrays.asList(yarray));
        Collections.shuffle(list = Arrays.asList(array));
    }

    public void reset(int w, int h) {
        bimg = null;
    }

    public void step(int w, int h) {
        if (bimg == null) {
            int biw = GoASurface.bimg.getWidth();
            int bih = GoASurface.bimg.getHeight();
            bimg = new BufferedImage(biw, bih, BufferedImage.TYPE_INT_RGB);
            createShuffledLists();
            big = bimg.createGraphics();
            big.drawImage(GoASurface.bimg, 0, 0, null);
            xcSize = (xlist.size() / (ending - beginning)) + 1;
            ycSize = (ylist.size() / (ending - beginning)) + 1;
            xeNum = 0;
            inc = 0;
        }
        xeNum = xcSize * ((Integer)list.get(inc)).intValue();
        yeNum = -ycSize;
        inc++;
    }

    public void render(int w, int h, Graphics2D g2) {
        big.setColor(GoAPart.black); 

        for (int k = 0; k <= (ending - beginning); k++) {
            if ((xeNum + xcSize) > xlist.size()) {
                xeNum = 0;
            } else {
                xeNum += xcSize;
            }
            yeNum += ycSize;

            for (int i = xeNum; i < xeNum+xcSize && i < xlist.size(); i++) {
                for (int j = yeNum; j < yeNum+ycSize && j < ylist.size(); j++) {   
                    int xval = ((Integer)xlist.get(i)).intValue();
                    int yval = ((Integer)ylist.get(j)).intValue();
                    if (((xval % blocksize) == 0) &&
                        ((yval % blocksize) == 0)) {
                        big.fillRect(xval, yval, blocksize, blocksize);
                    }
                }
            }
        }
        g2.drawImage(bimg, 0, 0, null);
    }

    public int getBegin() {
        return beginning;
    }

    public int getEnd() {
        return ending;
    }
} // End GoADdE class

/**
 * Subimage effect. Subimage the scene's buffered
 * image then rotate and scale down the subimages.
 */
class GoASiE implements GoAPart {
    private int beginning, ending;
    private BufferedImage bimg;
    private double rIncr, sIncr;
    private double scale, rotate;
    private int siw, sih;
    private Vector<BufferedImage> subs = new Vector<BufferedImage>(20);
    private Vector<Point> pts = new Vector<Point>(20);

    public GoASiE(int siw, int sih, int beg, int end) {
        this.siw = siw;
        this.sih = sih;
        this.beginning = beg;
        this.ending = end;
        rIncr = 360.0 / (ending - beginning);
        sIncr = 1.0 / (ending - beginning);
    }

    public void reset(int w, int h) {
        scale = 1.0;  
        rotate = 0.0;
        bimg = null;
        subs.clear();
        pts.clear();
    }

    public void step(int w, int h) {
        if (bimg == null) {
            int biw = GoASurface.bimg.getWidth();
            int bih = GoASurface.bimg.getHeight();
            bimg = new BufferedImage(biw, bih, BufferedImage.TYPE_INT_RGB);
            Graphics2D big = bimg.createGraphics();
            big.drawImage(GoASurface.bimg, 0, 0, null);
            for (int x = 0; x < w && scale > 0.0; x+=siw) {
                int ww = x+siw < w ? siw : w-x;
                for (int y = 0; y < h; y += sih) {
                    int hh = y+sih < h ? sih : h-y;
                    subs.addElement(bimg.getSubimage(x, y, ww, hh));    
                    pts.addElement(new Point(x, y));
                }
            }
        }
        
        rotate += rIncr;
        scale -= sIncr;
    }

    public void render(int w, int h, Graphics2D g2) {
        AffineTransform saveTx = g2.getTransform();
        g2.setColor(GoAPart.blue);
        for (int i = 0; i < subs.size() && scale > 0.0; i++) {
            BufferedImage bi = (BufferedImage)subs.get(i);
            Point p = (Point) pts.get(i);
            int ww = bi.getWidth();
            int hh = bi.getHeight();
            AffineTransform at = new AffineTransform();
            at.rotate(Math.toRadians(rotate), p.x + ww/2, p.y + hh/2); 
            at.translate(p.x, p.y);
            at.scale(scale, scale);

            Rectangle b1 = new Rectangle(0, 0, ww, hh);
            Shape shape = at.createTransformedShape(b1);
            Rectangle2D b2 = shape.getBounds2D();
            double xx = (p.x + ww/2) - (b2.getX() + b2.getWidth()/2);
            double yy = (p.y + hh/2) - (b2.getY() + b2.getHeight()/2);
            AffineTransform toCenterAT = new AffineTransform();
            toCenterAT.translate(xx, yy);
            toCenterAT.concatenate(at);

            g2.setTransform(toCenterAT);
            g2.drawImage(bi, 0, 0, null);
            g2.draw(b1);
        }
        g2.setTransform(saveTx);
    }

    public int getBegin() {
        return beginning;
    }

    public int getEnd() {
        return ending;
    }
} // End GoASiE class

/**
 * Line Effect. Flattened ellipse with lines from the center to the edge.
 * Expand or collapse the ellipse. Fade in or out the lines.
 */
class GoALnE implements GoAPart {
    static final int INC   = 1;
    static final int DEC   = 2;
    static final int R     = 4;              // rotate
    static final int RI    = R | INC;
    static final int RD    = R | DEC;
    static final int ZOOM  = 8;              // zoom
    static final int ZOOMI = ZOOM | INC;
    static final int ZOOMD = ZOOM | DEC;    
    static final int AC    = 32;             // AlphaComposite
    static final int ACI   = AC | INC;
    static final int ACD   = AC | DEC; 

    private int beginning, ending;
    private double rIncr, rotate;
    private double zIncr, zoom;
    private Vector<Point2D.Double> pts = new Vector<Point2D.Double>();
    private float alpha, aIncr;
    private int type;

    public GoALnE(int type, int beg, int end) {
        this.type = type;
        this.beginning = beg;
        this.ending = end;
        rIncr = 360.0 / (ending - beginning);
        aIncr = 0.9f / (ending - beginning);
        zIncr = 2.0 / (ending - beginning);
        if ((type & DEC) != 0) {
            rIncr = -rIncr;
            aIncr = -aIncr;
            zIncr = -zIncr;
        }
    }

    public void generatePts(int w, int h, double sizeF) {
        pts.clear();
        double size = Math.min(w, h) * sizeF;
        Ellipse2D ellipse = new Ellipse2D.Double(w/2 - size/2, h/2 - size/2, size, size);
        PathIterator pi = ellipse.getPathIterator(null, 0.8);
        while ( !pi.isDone() ) {
            double[] pt = new double[6];
            switch ( pi.currentSegment(pt) ) {
                case FlatteningPathIterator.SEG_MOVETO:
                case FlatteningPathIterator.SEG_LINETO:
                    pts.addElement(new Point2D.Double(pt[0], pt[1]));
            }
            pi.next();
        }
    }

    public void reset(int w, int h) {
        if ((type & DEC) != 0) {
            rotate = 360;
            alpha = 1.0f;
            zoom = 2.0;
        } else {
            rotate = alpha = 0;
            zoom = 0;
        }
        if ((type & ZOOM) == 0) {
            generatePts(w, h, 0.5);
        }
    }

    public void step(int w, int h) {
        if ((type & ZOOM) != 0) {
            generatePts(w, h, zoom += zIncr);
        }

        if ((type & RI) != 0 || (type & RI) != 0) {
           rotate += rIncr;
        }

        if ((type & ACI) != 0 || (type & ACD) != 0) {
           alpha += aIncr;
        }
    }

    public void render(int w, int h, Graphics2D g2) {
        Composite saveAC = null;
        if ((type & AC) != 0 && alpha >= 0 && alpha <= 1) {
            saveAC = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }
        AffineTransform saveTx = null;
        if ((type & R) != 0) {
            saveTx = g2.getTransform();
            AffineTransform at = new AffineTransform();
            at.rotate(Math.toRadians(rotate), w/2, h/2); 
            g2.setTransform(at);
        }
        Point2D p1 = new Point2D.Double(w/2, h/2);
        g2.setColor(Color.yellow);
        for (int i = 0; i < pts.size()-1; i++) {
            g2.draw(new Line2D.Float(p1, (Point2D)pts.get(i)));
        }
        if (saveTx != null) {
           g2.setTransform(saveTx);
        }
        if (saveAC != null) {
           g2.setComposite(saveAC);
        }
    }

    public int getBegin() {
        return beginning;
    }

    public int getEnd() {
        return ending;
    }
} // End GoALnE class

/**
 * Template for GoAFeatures & GoAContributors consisting of translating
 * blue and red rectangles and an image going from transparent to opaque.
 */
class GoATemp implements GoAPart {
    static final int NOANIM = 1;
    static final int RECT   = 2;
    static final int RNA    = RECT | NOANIM;
    static final int IMG    = 4;
    static final int INA    = IMG | NOANIM;
    private int beginning, ending;
    private float alpha, aIncr;
    private int type;
    private Rectangle rect1, rect2;
    private int x, y, xIncr, yIncr;
    private Image img;

    public GoATemp(int type, Image img, int beg, int end) {
        this.type = type;
        this.img = img;
        this.beginning = beg;
        this.ending = end;
        aIncr = 0.9f / (ending - beginning);
        if ((type & NOANIM) != 0) {
            alpha = 1.0f;
        } 
    }

    public void reset(int w, int h) {
        rect1 = new Rectangle(8, 20, w - 20, 30);
        rect2 = new Rectangle(20, 8, 30, h - 20);
        if ((type & NOANIM) == 0) {
            alpha = 0.0f;
            xIncr = w / (ending - beginning);
            yIncr = h / (ending - beginning);
            x = w + (int)(xIncr * 1.4);
            y = h + (int)(yIncr * 1.4);
        }
    }

    public void step(int w, int h) {
        if ((type & NOANIM) != 0) {
           return;
        }
        if ((type & RECT) != 0) {
            rect1.setLocation(x -= xIncr, 20);
            rect2.setLocation(20, y -= yIncr);
        }
        if ((type & IMG) != 0) {
            alpha += aIncr;
        }
    }

    public void render(int w, int h, Graphics2D g2) {
        if ((type & RECT) != 0) {
            g2.setColor(GoAPart.blue);
            g2.fill(rect1);
            g2.setColor(GoAPart.red);
            g2.fill(rect2);
        }
        if ((type & IMG) != 0) {
            Composite saveAC = g2.getComposite();
            if (alpha >= 0 && alpha <= 1) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            }
            g2.drawImage(img, 27, 33, null);
            g2.setComposite(saveAC);
        }
    }

    public int getBegin() {
        return beginning;
    }

    public int getEnd() {
        return ending;
    }
} // End GoATemp class

/**
 * GoAFeatures of Go Game.  Single character advancement effect.
 */
class GoAFeatures implements GoAPart {
    static final int QIJU   = 0;
    static final int DESUAN = 1;
    static final int QUANYU = 2;
    static final int HEZHAN = 3;

    static Font font1 = GoFont.goFeatureFont1;
    static Font font2 = GoFont.goFeatureFont2;
    static FontMetrics fm1 = GoASurface.getMetrics(font1);
    static FontMetrics fm2 = GoASurface.getMetrics(font2);
    static String table[][] = 
        {{ "棋局篇", 
           "　　夫万物之数，从一而起．局之路，三百六十有一．一者，",
           "生数之主，据其极而运四方也．三百六十以象周天之数．分而",
           "为四隅，隅各九十路，以象其日．外周七十二路，以象其侯．",
           "枯棋三百六十，白黑相半，以法阴阳．局之线道谓之枰，线道",
           "以间谓之挂．局方而静，棋圆而动．自古及今，弈者无同局．",
           "传曰：＂日日新＂．故宜用意深而存虑精，以求其胜负之由，",
           "则至其所未至矣．"},
         { "得算篇", 
           "　　棋者，以正合其势，以权制其敌．故计定于内，而势成于", 
           "外．战未合而算胜者，得算多也．算不胜者，得算少也．战已",  
           "合而不知胜负者，无算也．兵法曰：＂多算胜，少算不胜，而",
           "况于无算乎？＂由此观之，胜负见矣．" },
         { "权舆篇",
           "　　权舆者，弈棋布置，务守纲格．先于四隅分定势子，然后",
           "拆二斜飞，下势子一等．立二可以拆三，立三可以拆四，与势", 
           "子相望，可以拆五．近不必比，远不必乖．此皆古人之论，后",
           "学之规．舍此改作，未之或知．（然否？游戏作者注）诗曰：",
           "＂靡不有初，鲜克有终.＂"},
         { "合战篇", 
           "　　博弈之道，贵乎谨严．高者在腹，下者在边，中者占角，", 
           "此棋家之常然．法曰：＂宁输一子，勿失一先．＂有先而后，",
           "有后而先．击左则视右，攻右则瞻前．两生勿断，皆活勿连．",
           "阔不可太疏，密不可太促．与其恋子而求生不若弃子而取势；",
           "与其无事而强行，不若因之而自补．彼众我寡，先谋其生．我",
           "众敌寡，务张其势．善胜者不争．善阵者不战．善战者不败．",
           "善败者不乱．夫棋始以正合，终以奇胜，必也四顾其地，牢不",
           "可破，方可出人不意，掩人不备．凡敌无事而自补者，有侵袭", 
           "之意也．弃小而不就者，有图大之心也，随手而下者，无谋之",
           "人也，不思而应者，取败之道也．诗云：＂惴惴小心，如临于",
           "谷．＂"} };
    private String list[];
    private int beginning, ending;
    private int strH;
    private int endIndex, listIndex;
    private int type;
    private Vector<String> v = new Vector<String>();

    public GoAFeatures(int type, int beg, int end) {
        this.type = type;
		String[] tmp = new String[table[type].length];
		for (int i=0; i<table[type].length; i++) {
			try {
				byte[] bys = table[type][i].getBytes("ISO8859-1");
				tmp[i] = new String(bys, "GBK");
			} catch (Exception e) {
			}
		}
        list = tmp;
        this.beginning = beg;
        this.ending = end;
    }

    public void reset(int w, int h) {
        strH = (int)(fm2.getAscent() + fm2.getDescent());
        endIndex = 1;
        listIndex = 0;
        v.clear();
        v.addElement(list[listIndex].substring(0, endIndex));
    }

    public void step(int w, int h) {
        if (listIndex < list.length) {
            if (++endIndex > list[listIndex].length()) {
                if (++listIndex < list.length) {
                    endIndex = 1;
                    v.addElement(list[listIndex].substring(0, endIndex));
                }
            } else {
                v.set(listIndex, list[listIndex].substring(0, endIndex));
            }
        }
    }

    public void render(int w, int h, Graphics2D g2) {
        g2.setColor(GoAPart.white);
        g2.setFont(font1);
        g2.drawString((String)v.get(0), 90, 85);

        g2.setFont(font2);
        int gap = 0;
        if (type == QIJU) gap = 11;
        else if (type == DESUAN) gap = 15;
        else if (type == QUANYU) gap = 14;
        else if (type == HEZHAN) gap = 5; 
        for (int i = 1, y = 95; i < v.size(); i++) {
            g2.drawString((String)v.get(i), 120, y += strH + gap);
        }
    }

    public int getBegin() {
        return beginning;
    }

    public int getEnd() {
        return ending;
    }
} // End GoAFeatures class

/**
 * Scrolling text of contributors.
 */
class GoAContributors implements GoAPart {
    static String members[] = { 
        "Sun Microsystems, Inc.",
        "Brian Lichtenwalter",  
        "Dou Changzhong",
        "Wang Haifeng",
        "Gu Haifeng",
        "Guo Wei", 
        "Shao Bo",
    };
    static Font font = GoFont.aniContriFont;
    static FontMetrics fm = GoASurface.getMetrics(font);

    private int beginning, ending;
    private int nStrs, strH, index, yh, height;
    private Vector<String> v = new Vector<String>();
    private Vector<String> cast = new Vector<String>(members.length + 3);
    private int counter, cntMod;
    private GradientPaint gp;

    public GoAContributors(int beg, int end) {
        this.beginning = beg;
        this.ending = end;
        cast.addElement("CONTRIBUTORS");
        cast.addElement(" ");
        for (int i = 0; i < members.length; i++) {
            cast.addElement(members[i]);
        }
        cast.addElement(" "); 
        cast.addElement(" ");
        cntMod = (ending - beginning) / cast.size() - 5;
    }

    public void reset(int w, int h) {
        v.clear();
        strH = (int)(fm.getAscent() + fm.getDescent());
        nStrs = (h - 40) / strH + 1;
        height = strH * (nStrs - 1) + 48;
        index = 0;
        gp = new GradientPaint(0, h/2, Color.white, 0, h + 20, Color.black);
        counter = 0;
    }

    public void step(int w, int h) {
        if (counter++%cntMod == 0) {
            if (index < cast.size()) {
                v.addElement(cast.get(index));
            }
            if ((v.size() == nStrs || index >= cast.size()) && v.size() != 0) {
                v.removeElementAt(0);
            }
            index++;
        }
    }

    public void render(int w, int h, Graphics2D g2) {
        g2.setPaint(gp);
        g2.setFont(font);
        double remainder = counter%cntMod;
        double incr = 1.0 - remainder/cntMod;
        incr = incr == 1.0 ? 0 : incr;
        int y = (int) (incr * strH);

        if (index >= cast.size()) {
            y = yh + y; 
        } else {
            y = yh = height - v.size() * strH + y;
        }

        for (int i = 0; i < v.size(); i++) {
            String s = (String)v.get(i);
            g2.drawString(s, w/2 - fm.stringWidth(s)/2, y += strH);
        }
    }

    public int getBegin() {
        return beginning;
    }

    public int getEnd() {
        return ending;
    }
} // End GoAContributors class
