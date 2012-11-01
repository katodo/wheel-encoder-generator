/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wheelencodergenerator;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;

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

    public static void createFile(File f) {
        if (!f.exists()) {
            System.out.println("saveAndQuit() creating "+f.getName());
            try {
                if (!f.createNewFile()) {
                    Assert.fail();
                }
            } catch (IOException ex) {
                Assert.fail();
            }
        }
    }

    public static void deleteFile(File f) {
        if (f.exists()) {
            System.out.println("saveAndQuit() Deleting "+f.getName());
            f.delete();
        }
    }

}
