/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wheelencodergenerator;

import com.botthoughts.Debug;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import junit.framework.Assert;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.finder.JFileChooserFinder;
import org.fest.swing.finder.JOptionPaneFinder;
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
    private File existingFile = new File("c:\\tmp\\test1.weg");
    private File toBeCreatedFile = new File("c:\\tmp\\test2.weg");

    public MainFrameTest() {
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
        createFile(existingFile);
        deleteFile(toBeCreatedFile);
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

    public void createFile(File f) {
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

    public void deleteFile(File f) {
        if (f.exists()) {
            System.out.println("saveAndQuit() Deleting "+f.getName());
            f.delete();
        }
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
}
