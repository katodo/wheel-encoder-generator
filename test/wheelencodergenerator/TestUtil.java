/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wheelencodergenerator;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Shimniok
 */
public class TestUtil {

    public static void delay() {
        delay(5000);
    }
    
    public static void delay(int d) {
        try {
            Thread.sleep(d);
        } catch (InterruptedException ex) {
            Logger.getLogger(ImageExportChooserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
