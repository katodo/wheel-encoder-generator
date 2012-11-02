/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * EncoderPreview.java
 *
 * Created on Oct 7, 2010, 9:39:10 AM
 */

package wheelencodergenerator;

import java.awt.print.*;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.geom.*;
import java.awt.print.Printable;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

/**
 *
 * @author Michael Shimniok
 */
public class EncoderPanel extends javax.swing.JPanel implements Printable {

    private WheelEncoder e;

    /** Creates new form EncoderPreview */
    public EncoderPanel() {
        initComponents();
    }


    public void setWheelEncoder(WheelEncoder encoder)
    {
        e = encoder;
    }

    /* doPaint()
     * 
     * Does the actual painting of encoder pattern onto a Graphics2D context
     * using the supplied (x,y) start offset and d diameter with the specified
     * background color (-1 for no background)
     */
    private void doPaint(Graphics2D g2D, double x, double y, double width, double height, double diameter, Color background)
    {
        double ratio = (double) e.getInnerDiameter() / (double) e.getOuterDiameter();
        double id = diameter * ratio;
        double trackWidth = (diameter - id)/2;
        double degree = 0;
        double offset = 0;
        int maxTrack = 0;
        Color color;

        // Background
        if (background != null) {
            g2D.setColor(background);
            g2D.fillRect(0, 0, (int) Math.round(width), (int) Math.round(height));
        }

        maxTrack = e.getTrackCount();

        for (int track = 0; track < maxTrack; track++) {
            offset = e.getOffset(track);
            double dA = id + (maxTrack-track) * (diameter - id - 1) / maxTrack;
            double xA = x + track * trackWidth / maxTrack;
            double yA = y + track * trackWidth / maxTrack;

            if (e.isInverted())
                color = Color.white;
            else
                color = Color.black;
            int stripe = 0;
            for (double i=offset; i < (360.0+offset); i += degree) {
                degree = e.getDegree(track, stripe++);
                g2D.setColor( color );
                g2D.fill( new Arc2D.Double(xA, yA, dA, dA, i, degree, Arc2D.PIE) );
                if (color == Color.white)
                    color = Color.black;
                else
                    color = Color.white;
            }
            g2D.setColor(Color.black);
            g2D.drawOval((int) Math.round(xA), (int) Math.round(yA), (int) Math.round(dA), (int) Math.round(dA));
        }

        // Draw inner circle
        g2D.setColor(Color.white);
        g2D.fillOval((int) Math.round(x+trackWidth), (int) Math.round(y+trackWidth), (int) Math.round(id), (int) Math.round(id));
        g2D.setColor(Color.black);
        g2D.drawOval((int) Math.round(x+trackWidth), (int) Math.round(y+trackWidth), (int) Math.round(id), (int) Math.round(id));
        // Draw crosshairs
        g2D.drawLine((int) Math.round(x+trackWidth), (int) Math.round(y+diameter/2), (int) Math.round(x+(diameter+id)/2), (int) Math.round(y+diameter/2));
        g2D.drawLine((int) Math.round(x+diameter/2), (int) Math.round(y+trackWidth), (int) Math.round(x+diameter/2), (int) Math.round(y+(diameter+id)/2));
    }

    /* export()
     *
     * Exports this graphic to the specified image file in the specified
     * image format, width pixels square
     * formatName - a String containg the informal name of the format.
     */
    public void export(File file, String formatName, int width) throws IOException
    {
        int height = width;
        // Create a buffered image in which to draw
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // Create a graphics contents on the buffered image
        Graphics2D g2D = bufferedImage.createGraphics();
        double d = (double) width; // diameter

        if (e != null && e.getOuterDiameter() > 0 && e.getInnerDiameter() < e.getOuterDiameter()) {
            doPaint(g2D, 0, 0, width, height, d, Color.white);
            // TODO catch export failures
            if (!ImageIO.write(bufferedImage, formatName, file)) {
                // TODO do something with false return from write()
            }

        }
    }

    // TODO: convert print() to directly call doPaint()
    @Override
    public int print(Graphics g, PageFormat pf, int pi)
                       throws PrinterException {
        if (pi >= 1) {
          return Printable.NO_SUCH_PAGE;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.translate(pf.getImageableX(),
                     pf.getImageableY());
        Dimension size = getSize();
        double d = (double) Math.min(size.width, size.height); // diameter
        // Outer diameter is in mm. Convert to inches then to dots based on 72dpi
        // TODO change conversion to inches to support in/mm
        double scale = e.getOuterDiameter() * 72 * 0.0393700787 / d;
        g2.scale(scale, scale);
        paint(g2);

        return Printable.PAGE_EXISTS;
    }

    @Override
    public void paint(Graphics g) {
        // Dynamically calculate size information
        // (the canvas may have been resized externally...)
        Graphics2D g2D = (Graphics2D) g;
        Dimension size = getSize();
        double d = (double) Math.min(size.width, size.height); // diameter

        if (e != null && e.getOuterDiameter() > 0 && e.getInnerDiameter() < e.getOuterDiameter()) {
            double x = (size.width - d)/2; // align center
            double y = 0; // align top
            // get default background color from uimanager
            UIDefaults defaults = UIManager.getLookAndFeel().getDefaults();
            doPaint(g2D, x, y, size.width, size.height, d, defaults.getColor("control"));
        }
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setMinimumSize(new java.awt.Dimension(600, 550));
        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(600, 550));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
