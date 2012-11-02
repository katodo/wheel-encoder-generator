/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wheelencodergenerator;

import com.botthoughts.Debug;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JFrame;
import org.fest.swing.core.ComponentFinder;
import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.core.KeyPressInfo;
import org.fest.swing.core.matcher.JButtonMatcher;
import org.fest.swing.core.matcher.JTextComponentMatcher;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.finder.DialogFinder;
import org.fest.swing.finder.JFileChooserFinder;
import org.fest.swing.finder.JOptionPaneFinder;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.JButtonFixture;
import org.fest.swing.fixture.JFileChooserFixture;
import org.fest.swing.fixture.JOptionPaneFixture;
import org.fest.swing.util.Platform;
import org.junit.AfterClass;
import org.junit.After;
import org.junit.Assert;
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
        if (Platform.isOSX() || Platform.isLinux()) {
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
        TestUtil.createFile(existingImage);
        TestUtil.deleteFile(toBeCreatedFile);
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
        if (window != null)
            window.cleanUp();
    }


    @Test
    public void quitPromptSaveCancel() {
        Debug.println("enter");
        if (Platform.isOSX()) {
            KeyPressInfo ki = KeyPressInfo.keyCode(KeyEvent.VK_Q);
            window.pressAndReleaseKey(ki.modifiers(Platform.controlOrCommandMask()));
        } else {
            window.menuItemWithPath("File|Exit").click();
        }
        JOptionPaneFinder.findOptionPane().using( window.robot ).requireVisible().requireTitle("Save?").buttonWithText("Cancel" ).click();
        window.requireVisible();
    }


    @Test
    public void quitPromptSaveNo() {
        if (Platform.isOSX()) {
            KeyPressInfo ki = KeyPressInfo.keyCode(KeyEvent.VK_Q);
            window.pressAndReleaseKey(ki.modifiers(Platform.controlOrCommandMask()));
        } else {
            window.menuItemWithPath("File|Exit").click();
        }
        JOptionPaneFinder.findOptionPane().using( window.robot ).requireVisible().requireTitle("Save?").buttonWithText("No" ).click();
        window.requireNotVisible();
    }


    @Test
    public void quitPromptSaveYes() {
        if (Platform.isOSX()) {
            KeyPressInfo ki = KeyPressInfo.keyCode(KeyEvent.VK_Q);
            window.pressAndReleaseKey(ki.modifiers(Platform.controlOrCommandMask()));
        } else {
            window.menuItemWithPath("File|Exit").click();
        }
        JOptionPaneFinder.findOptionPane().using( window.robot ).requireVisible().requireTitle("Save?").buttonWithText("Yes").click();
        JFileChooserFixture chooser = JFileChooserFinder.findFileChooser().using( window.robot );
        chooser.selectFile(toBeCreatedFile).approve();
        Assert.assertTrue(toBeCreatedFile.exists());
    }


    @Test
    public void saveAndReplace() {
        window.button("saveButton").requireEnabled();
        window.menuItem("saveMenuItem").click();
        //JOptionPaneFinder.findOptionPane().using( window.robot ).requireVisible().buttonWithText("Yes").click();
        if (Platform.isOSX()) {
            DialogFixture dialog = WindowFinder.findDialog(FileDialog.class).withTimeout(10000).using(window.robot);
            dialog.requireVisible();
        } else {
            JFileChooserFixture chooser = JFileChooserFinder.findFileChooser().using( window.robot );
            chooser.selectFile(existingFile).approve();
            JOptionPaneFinder.findOptionPane().using( window.robot ).requireVisible().requireTitle("Replace?").buttonWithText("Yes").click();
            window.button("saveButton").requireDisabled();
        }
        Assert.assertTrue(existingFile.exists());
    }


    @Test
    public void saveChangeQuitPromptSaveNo() {
        window.menuItem("saveMenuItem").click();
        JFileChooserFixture chooser = JFileChooserFinder.findFileChooser().using( window.robot ).requireVisible();
        chooser.selectFile(toBeCreatedFile).approve();
        Assert.assertTrue(toBeCreatedFile.exists());
        window.button("saveButton").requireDisabled();
        window.spinner("resolutionSpinner").enterTextAndCommit("128");
        window.button("saveButton").requireEnabled();
        window.menuItemWithPath("File|Exit").click();
        JOptionPaneFinder.findOptionPane().using( window.robot ).requireVisible().requireTitle("Save?").buttonWithText("No" ).click();
        window.requireNotVisible();
        TestUtil.deleteFile(toBeCreatedFile);
    }

    
    @Test
    public void openQuitNoPrompt() {
        window.button("saveButton").requireEnabled();
        window.menuItem("saveMenuItem").click();
        JFileChooserFixture chooser = JFileChooserFinder.findFileChooser().using( window.robot ).requireVisible();
        chooser.selectFile(toBeCreatedFile).approve();
        Assert.assertTrue(toBeCreatedFile.exists());
        window.button("saveButton").requireDisabled();
        window.menuItem("openMenuItem").click();
        chooser = JFileChooserFinder.findFileChooser().using( window.robot ).requireVisible();
        chooser.selectFile(toBeCreatedFile).approve();
        window.button("saveButton").requireDisabled();
        window.menuItemWithPath("File|Exit").click();
        window.requireNotVisible();
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
        Assert.assertTrue(toBeCreatedFile.exists());
        window.button("saveButton").requireDisabled();
        window.checkBox("quadratureCheckBox").check();
        window.button("saveButton").requireEnabled();
        window.menuItem("openMenuItem").click();
        JOptionPaneFinder.findOptionPane().using( window.robot ).requireVisible().requireTitle("Save?");
    }


    @Test
    public void exportSave() {
        window.button("exportButton").click();
        DialogFixture dialog = window.dialog();
        dialog.requireVisible().button("exportButton").click();
        //window.requireDisabled();
        JFileChooserFixture chooser = JFileChooserFinder.findFileChooser().using( window.robot ).requireVisible();
        chooser.selectFile(toBeCreatedImage).approve();
        Assert.assertTrue(toBeCreatedImage.exists());
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
        Assert.assertFalse(toBeCreatedImage.exists());
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
