/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wheelencodergenerator;

import java.io.File;
import org.fest.swing.finder.JFileChooserFinder;
import org.fest.swing.fixture.JFileChooserFixture;
import com.botthoughts.PlatformUtilities;
import java.awt.event.KeyEvent;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.fixture.DialogFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michael Shimniok
 */
public class ImageExportChooserTest {

    private DialogFixture window;
    private ImageExportChooser dialog;
    private static File existingFile;
    private static File toBeCreatedFile;

    public ImageExportChooserTest() {
    }

    @BeforeClass
    public static void setUpOnce() {
        FailOnThreadViolationRepaintManager.install();
        if (PlatformUtilities.isOSX() || PlatformUtilities.isLinux()) {
            existingFile = new File("/tmp/test1.png");
            toBeCreatedFile = new File("/tmp/test2.png");
        } else {
            existingFile = new File("C:\\tmp\\test1.png");
            toBeCreatedFile = new File("C:\\tmp\\test2.png");
        }
    }

    @Before
    public void setUp() {
        TestUtil.createFile(existingFile);
        TestUtil.deleteFile(toBeCreatedFile);
        dialog = GuiActionRunner.execute(new GuiQuery<ImageExportChooser>() {
            @Override
            protected ImageExportChooser executeInEDT() {
                return new ImageExportChooser();
            }
        });

        window = new DialogFixture(dialog);
        window.show(); // shows the frame to test
    }

    @After
    public void tearDown() {
        window.cleanUp();
    }

    @Test
    public void closeWindow() {
        window.close();
        assertEquals(ImageExportChooser.getOption(),ImageExportChooser.CANCEL_OPTION);
    }

    @Test
    public void pressCancelButton() {
        window.button("cancelButton").click();
        assertEquals(ImageExportChooser.getOption(),ImageExportChooser.CANCEL_OPTION);
    }

    @Test
    public void pressExportButtonCancel() {
        window.button("exportButton").click();
        JFileChooserFixture chooser = JFileChooserFinder.findFileChooser().using( window.robot ).requireVisible();
        chooser.selectFile(toBeCreatedFile);
        window.requireDisabled();
        chooser.cancel();
        window.requireEnabled();
    }

    @Test
    public void pressExportButtonSave() {
        window.button("exportButton").click();
        JFileChooserFixture chooser = JFileChooserFinder.findFileChooser().using( window.robot ).requireVisible();
        chooser.selectFile(toBeCreatedFile);
        window.requireDisabled();
        chooser.approve();
        window.requireEnabled();
    }

    @Test
    public void enterInvalidResolution() {
        // TODO test use of focus change and enter separately
        window.textBox("resolutionTextField").deleteText().enterText("-600").pressKey(KeyEvent.VK_ENTER);
        window.button("cancelButton").focus();
        window.textBox("resolutionTextField").requireText("0");
        window.textBox("resolutionTextField").deleteText().enterText("99999").pressKey(KeyEvent.VK_ENTER);
        window.button("cancelButton").focus();
        window.textBox("resolutionTextField").requireText("8192");
        window.textBox("resolutionTextField").deleteText().enterText("d600").pressKey(KeyEvent.VK_ENTER);
        window.button("cancelButton").focus();
        window.textBox("resolutionTextField").requireText("600");
    }

}