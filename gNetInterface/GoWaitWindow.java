package gNetInterface;

import gNetUtil.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class GoWaitWindow extends Window {
    private int sourceX = 0, sourceY = 0;
    
    public GoWaitWindow(Frame fOwner) {
        super(fOwner);

        JPanel waitPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        waitPanel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createRaisedBevelBorder(),
                            BorderFactory.createEmptyBorder(20, 12, 20, 12)));
        JLabel waitLabel1 = new JLabel("    The system is trying to connect to the server and ");
        JLabel waitLabel2 = new JLabel("waiting to your partner's connecting, please wait ...");
        waitLabel1.setFont(GoFont.labelFont);
        waitLabel2.setFont(GoFont.labelFont);
        waitPanel.add(waitLabel1);
        waitPanel.add(waitLabel2);
        add(waitPanel, BorderLayout.CENTER);

        addMouseListener(new MouseListener() {
            public void mouseEntered(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }

            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }

            public void mouseClicked(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {
                sourceX = e.getX();
                sourceY = e.getY();
            }
            public void mouseReleased(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
        });

        addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                Point p = getLocationOnScreen();
                int x = p.x + e.getX() - sourceX;
                int y = p.y + e.getY() - sourceY;

                setLocation(x, y);
            }

            public void mouseMoved(MouseEvent e) {}
        });
    }

    public void setVisible(boolean visible) {
		if (visible) {
			Thread runner = new Thread() {
				public void run() {
					GoWaitWindow.super.setVisible(true);    
				}
			};
			runner.start();

			synchronized(this) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}
		} else {
			synchronized(this) {
				notifyAll();
			}
			super.setVisible(false);
		}
    }

    public void dispose() {
        synchronized(this) {
            notifyAll();
        }
        super.dispose();
    }
}