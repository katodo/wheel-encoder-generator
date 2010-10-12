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

import java.awt.*;
import java.awt.geom.*;
import java.awt.print.*;

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

    public int print(Graphics g, PageFormat pf, int pi)
                       throws PrinterException {
        if (pi >= 1) {
          return Printable.NO_SUCH_PAGE;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.translate(pf.getImageableX(),
                     pf.getImageableY());
        Font  f = new Font("Monospaced", Font.PLAIN,12);
        g2.setFont (f);
        Dimension size = getSize();
        double d = (double) Math.min(size.width, size.height); // diameter
        // Outer diameter is in mm. Convert to inches then to dots based on 72dpi
        double scale = e.getOuterDiameter() * 72 * 0.0393700787 / d;
        g2.scale(scale, scale);
        paint(g2);

        return Printable.PAGE_EXISTS;
    }

    public void paint(Graphics g) {
        // Dynamically calculate size information
        // (the canvas may have been resized externally...)
        Graphics2D g2D = (Graphics2D) g;

        if (e != null && e.getOuterDiameter() > 0 && e.getInnerDiameter() < e.getOuterDiameter()) {

            Dimension size = getSize();
            double d = (double) Math.min(size.width, size.height); // diameter
            double ratio = (double) e.getInnerDiameter() / (double) e.getOuterDiameter();
            double id = d * ratio;
            double x = (size.width - d)/2;
            double y = 0; // align top
            double trackWidth = (d - id)/2;
            double offset = 0;
            int maxTrack = 0;

            /*
             *
            // Calculate 1/2 diameter
            double d2 = id + trackWidth;
            double x2 = x + trackWidth/2;
            double y2 = y + trackWidth/2;
            // Calculate 1/3 diameter
            double d3a = id + 4*trackWidth/3;
            double x3a = x + trackWidth/3;
            double y3a = y + trackWidth/3;
            // Calculate 2/3 diameter
            double d3b = id + 2*trackWidth/3;
            double x3b = x + 2*trackWidth/3;
            double y3b = y + 2*trackWidth/3;
             *
             */
            
            maxTrack = e.getTrackCount();

            if (e.getType() == e.ABSOLUTE) {

                for (int track = 0; track < maxTrack; track++) {
                    //System.out.println("Absolute: Track " + Integer.toString(track) + " of " + Integer.toString(maxTrack));
                    double degree = e.getDegree(track);
                    // gray code = degree/2, binary = 0
                    if (e.getNumbering() == e.GRAY)
                        offset = degree/2;
                    else if (e.getNumbering() == e.BINARY)
                        offset = 0;

                    double dA = id + (maxTrack-track) * (d - id) / maxTrack;
                    double xA = x + track * trackWidth / maxTrack;
                    double yA = y + track * trackWidth / maxTrack;
                    //System.out.println("entering loop 2");
                    for (double i=offset; degree > 0 && i < (360.0+offset); i += 2 * degree) {
                        // always start with white (0)
                        g2D.setColor( Color.white );
                        g2D.fill( new Arc2D.Double(xA, yA, dA, dA, i, degree, Arc2D.PIE) );
                        g2D.setColor( Color.black );
                        g2D.fill( new Arc2D.Double(xA, yA, dA, dA, i + degree, degree, Arc2D.PIE) );
                    }
                    g2D.setColor(Color.black);
                    g2D.drawOval((int) Math.round(xA), (int) Math.round(yA), (int) Math.round(dA), (int) Math.round(dA));
                }

            }
            else if (e.getType() == e.STANDARD) {

                for (int track = 0; track < maxTrack; track++) {

                    double degree = e.getDegree(track);
                    double dA = id + (maxTrack-track) * (d - id) / maxTrack;
                    double xA = x + track * trackWidth / maxTrack;
                    double yA = y + track * trackWidth / maxTrack;

                    if (track == e.getIndexTrack()) {
                        g2D.setColor( Color.black );
                        g2D.fill( new Arc2D.Double(xA, yA, dA, dA, 0, degree, Arc2D.PIE) );
                        g2D.setColor( Color.white );
                        g2D.fill( new Arc2D.Double(xA, yA, dA, dA, degree, 360-degree, Arc2D.PIE) );
                    } else {
                        // Quadrature
                        if (track == e.getQuadratureTrack()) {
                            offset = degree/2;
                        }

                        for (double i=offset; degree > 0 && i < (360.0+offset); i += 2 * degree) {
                            // always start with white (0)
                            g2D.setColor( Color.white );
                            g2D.fill( new Arc2D.Double(xA, yA, dA, dA, i, degree, Arc2D.PIE) );
                            g2D.setColor( Color.black );
                            g2D.fill( new Arc2D.Double(xA, yA, dA, dA, i + degree, degree, Arc2D.PIE) );
                        }
                    }
                    g2D.setColor(Color.black);
                    g2D.drawOval((int) Math.round(xA), (int) Math.round(yA), (int) Math.round(dA), (int) Math.round(dA));

                }

            }


            // Draw inner circle
            g2D.setColor(Color.white);
            g2D.fillOval((int) Math.round(x+trackWidth), (int) Math.round(y+trackWidth), (int) Math.round(id), (int) Math.round(id));
            g2D.setColor(Color.black);
            g2D.drawOval((int) Math.round(x+trackWidth), (int) Math.round(y+trackWidth), (int) Math.round(id), (int) Math.round(id));
            // Draw crosshairs
            g2D.drawLine((int) Math.round(x+trackWidth), (int) Math.round(y+d/2), (int) Math.round(x+(d+id)/2), (int) Math.round(y+d/2));
            g2D.drawLine((int) Math.round(x+d/2), (int) Math.round(y+trackWidth), (int) Math.round(x+d/2), (int) Math.round(y+(d+id)/2));
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

        setName("Form"); // NOI18N

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