/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wheelencodergenerator;

import com.botthoughts.Debug;
import com.botthoughts.PlatformUtilities;
import java.io.File;
import javax.swing.JDialog;
import javax.swing.JFrame;
import junit.framework.Assert;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.finder.DialogFinder;
import org.fest.swing.finder.JFileChooserFinder;
import org.fest.swing.finder.JOptionPaneFinder;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.JButtonFixture;
import org.fest.swing.fixture.JFileChooserFixture;
import org.fest.swing.fixture.JOptionPaneFixture;
import org.junit.AfterClass;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
/**
 *
 * @author Michael Shimniok
 */
public class MainFrameTest {
    private FrameFixture window;
    private JFrame app;
    private static File existingFile;
    private static File toBeCreatedFile;
    private static File existingImage;
    private static File toBeCreatedImage;

    public MainFrameTest() {
    }

    @BeforeClass
    public static void setUpOnce() {
        FailOnThreadViolationRepaintManager.install();
        if (PlatformUtilities.isOSX() || PlatformUtilities.isLinux()) {
            existingFile = new File("/tmp/test1.png");
            toBeCreatedFile = new File("/tmp/test2.png");
            existingImage = new File("/tmp/test1.png");
            toBeCreatedImage = new File("/tmp/test2.png");
        } else {
            existingFile = new File("C:\\tmp\\test1.weg");
            toBeCreatedFile = new File("C:\\tmp\\test2.weg");
            existingImage = new File("C:\\tmp\\test1.png");
            toBeCreatedImage = new File("C:\\tmp\\test2.png");
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        TestUtil.createFile(existingFile);
        TestUtil.deleteFile(toBeCreatedFile);
        TestUtil.createFile(existingImage);
        TestUtil.deleteFile(toBeCreatedImage);
        app = GuiActionRunner.execute(new GuiQuery<JFrame>() {
            @Override
            protected JFrame executeInEDT() {
                System.out.println("setUp(): creating window, showing");
                Debug.println(Thread.currentThread().getName());
                JFrame myApp = new MainFrame();
                return myApp;
            }
        });
        window = new FrameFixture(app);
        window.show(); // shows the frame to test
    }


    @After
    public void tearDown() {
        window.cleanUp();
    }


    @Test
    public void quitPromptSaveCancel() {
        Debug.println("enter");
        window.menuItem("exitMenuItem").click();
        JOptionPaneFinder.findOptionPane().using( window.robot ).requireVisible().requireTitle("Save?").buttonWithText("Cancel" ).click();
        window.requireVisible();
    }


    @Test
    public void quitPromptSaveNo() {
        window.menuItem("exitMenuItem").click();
        JOptionPaneFinder.findOptionPane().using( window.robot ).requireVisible().requireTitle("Save?").buttonWithText("No" ).click();
        window.requireNotVisible();
    }


    @Test
    public void quitPromptSaveYes() {
        window.menuItem("exitMenuItem").click();
        JOptionPaneFinder.findOptionPane().using( window.robot ).requireVisible().requireTitle("Save?").buttonWithText("Yes").click();
        JFileChooserFixture chooser = JFileChooserFinder.findFileChooser().using( window.robot );
        chooser.selectFile(toBeCreatedFile).approve();
        if (!toBeCreatedFile.exists()) {
            Assert.fail();
        }
        window.requireNotVisible();
    }


    @Test
    public void saveAndReplace() {
        window.button("saveButton").requireEnabled();
        window.menuItem("saveMenuItem").click();
        //JOptionPaneFinder.findOptionPane().using( window.robot ).requireVisible().buttonWithText("Yes").click();
        JFileChooserFixture chooser = JFileChooserFinder.findFileChooser().using( window.robot );
        chooser.selectFile(existingFile).approve();
        JOptionPaneFinder.findOptionPane().using( window.robot ).requireVisible().requireTitle("Replace?").buttonWithText("Yes").click();
        window.button("saveButton").requireDisabled();
        if (!existingFile.exists()) {
            Assert.fail();
        }
    }


    @Test
    public void saveChangeQuitPromptSaveNo() {
        window.menuItem("saveMenuItem").click();
        JFileChooserFixture chooser = JFileChooserFinder.findFileChooser().using( window.robot ).requireVisible();
        chooser.selectFile(toBeCreatedFile).approve();
        if (!toBeCreatedFile.exists()) {
            Assert.fail();
        } else {
            window.button("saveButton").requireDisabled();
            window.spinner("resolutionSpinner").enterTextAndCommit("128");
            window.button("saveButton").requireEnabled();
            window.menuItem("exitMenuItem").click();
            JOptionPaneFinder.findOptionPane().using( window.robot ).requireVisible().requireTitle("Save?").buttonWithText("No" ).click();
            window.requireNotVisible();
        }
    }


    @Test
    public void openQuitNoPrompt() {
        window.button("saveButton").requireEnabled();
        window.menuItem("saveMenuItem").click();
        JFileChooserFixture chooser = JFileChooserFinder.findFileChooser().using( window.robot ).requireVisible();
        chooser.selectFile(toBeCreatedFile).approve();
        if (!toBeCreatedFile.exists()) {
            Assert.fail();
        } else {
            window.button("saveButton").requireDisabled();
            window.menuItem("openMenuItem").click();
            chooser = JFileChooserFinder.findFileChooser().using( window.robot ).requireVisible();
            chooser.selectFile(toBeCreatedFile).approve();
            window.button("saveButton").requireDisabled();
            window.menuItem("exitMenuItem").click();
            window.requireNotVisible();
        }
    }


    @Test
    public void openPromptSaveNo() {
        window.menuItem("openMenuItem").click();
        JOptionPaneFinder.findOptionPane().using( window.robot ).requireVisible().buttonWithText("No").click();
        JFileChooserFinder.findFileChooser().using( window.robot ).requireVisible();
    }


    @Test
    public void saveChangeOpenPromptSave() {
        window.button("saveButton").requireEnabled();
        window.menuItem("saveMenuItem").click();
        JFileChooserFixture chooser = JFileChooserFinder.findFileChooser().using( window.robot ).requireVisible();
        chooser.selectFile(toBeCreatedFile).approve();
        if (!toBeCreatedFile.exists()) {
            Assert.fail();
        } else {
            window.button("saveButton").requireDisabled();
            window.checkBox("quadratureCheckBox").check();
            window.button("saveButton").requireEnabled();
            window.menuItem("openMenuItem").click();
            JOptionPaneFinder.findOptionPane().using( window.robot ).requireVisible().requireTitle("Save?");
        }
    }


    @Test
    public void exportSave() {
        window.button("exportButton").click();
        DialogFixture dialog = window.dialog();
        dialog.requireVisible().button("exportButton").click();
        //window.requireDisabled();
        JFileChooserFixture chooser = JFileChooserFinder.findFileChooser().using( window.robot ).requireVisible();
        chooser.selectFile(toBeCreatedImage).approve();
        if (!toBeCreatedImage.exists()) {
            Assert.fail();
        }
    }


    @Test
    public void exportCancel() {
        window.button("exportButton").click();
        DialogFixture dialog = window.dialog();
        dialog.requireVisible().button("exportButton").click();
        //window.requireDisabled();
        JFileChooserFixture chooser = JFileChooserFinder.findFileChooser().using( window.robot ).requireVisible();
        chooser.selectFile(toBeCreatedImage).cancel();
        dialog.button("cancelButton").click();
        if (toBeCreatedImage.exists()) {
            Assert.fail();
        }
    }

    
    @Test
    public void exportSaveReplace() {
        window.button("exportButton").click();
        DialogFixture dialog = window.dialog();
        dialog.requireVisible().button("exportButton").click();
        //window.requireDisabled();
        JFileChooserFixture chooser = JFileChooserFinder.findFileChooser().using( window.robot ).requireVisible();
        chooser.selectFile(existingImage).approve();
        JOptionPaneFixture option = JOptionPaneFinder.findOptionPane().using(window.robot).requireVisible();
        option.buttonWithText("No").click();
    }
}
