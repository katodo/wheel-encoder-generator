/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wheelencodergenerator;

import com.botthoughts.Debug;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.fest.swing.fixture.JButtonFixture;
import org.fest.swing.fixture.JFileChooserFixture;
import org.fest.swing.fixture.JOptionPaneFixture;
import org.jdesktop.application.SingleFrameApplication;
import javax.swing.JFrame;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.finder.JFileChooserFinder;
import org.fest.swing.finder.JOptionPaneFinder;
import org.junit.AfterClass;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
/**
 *
 * @author Michael Shimniok
 */
public class WheelEncoderGeneratorViewTest {
    private FrameFixture window;
    private JFrame app;

    public WheelEncoderGeneratorViewTest() {
    }


    private static void delay() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ImageExportChooserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    @BeforeClass
    public static void setUpOnce() {
        FailOnThreadViolationRepaintManager.install();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        String[] args = new String[0];
        SingleFrameApplication.launch(WheelEncoderGeneratorApp.class, args);
        app = GuiActionRunner.execute(new GuiQuery<JFrame>() {
            @Override
            protected JFrame executeInEDT() {
                System.out.println("setUp(): creating window, showing");
                Debug.println(Thread.currentThread().getName());
                JFrame myApp = WheelEncoderGeneratorApp.getApplication().getMainFrame();
                window = new FrameFixture(myApp);
                window.show(); // shows the frame to test
                return myApp;
            }
        });


    }


    @After
    public void tearDown() {
        window.cleanUp();
    }

    @Test
    public void quitWithoutSavingCancel() {
        Debug.println("enter");
        window.menuItem("exitMenuItem").click();
        JOptionPaneFinder.findOptionPane().using( window.robot ).buttonWithText("Cancel" ).requireEnabled().click();
    }

    @Test
    public void quitWithoutSavingNo() {
        window.menuItem("exitMenuItem").click();
        JOptionPaneFinder.findOptionPane().using( window.robot ).buttonWithText("No" ).requireEnabled().click();
        window.requireEnabled();
    }

    @Test
    public void saveAndQuit() {
/*
        File f = new File("test.weg");
        if (f.exists()) {
            f.delete();
        }
        Debug.println("enter");
        window.menuItem("exitMenuItem").click();
        JOptionPaneFinder.findOptionPane().using( window.robot ).buttonWithText("Yes" ).requireEnabled().click();
        delay();
        JFileChooserFixture chooser = JFileChooserFinder.findFileChooser().using( window.robot );
        delay();
        chooser.selectFile(f);
        delay();
        chooser.approve();
 * 
 */
    }

}