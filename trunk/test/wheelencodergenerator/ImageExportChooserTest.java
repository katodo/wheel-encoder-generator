/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wheelencodergenerator;

import com.botthoughts.PlatformUtilities;
import java.util.regex.Pattern;
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
    
    public ImageExportChooserTest() {
    }

    @BeforeClass
    public static void setUpOnce() {
        FailOnThreadViolationRepaintManager.install();
    }

    @Before
    public void setUp() {
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

    // todo: make these text strings properties that can be shared by source and test harnesses
    @Test
    public void noFilenameEntered() {
        window.textBox("filenameTextField").deleteText().enterText("test.png").pressKey(KeyEvent.VK_ENTER);
        window.button("exportButton").requireEnabled();
        window.textBox("filenameTextField").deleteText().pressKey(KeyEvent.VK_ENTER).requireText("");
        window.button("exportButton").requireDisabled();
    }

    @Test
    public void invalidAndValidFileameEntered() {
        // Test entry of invalid extension
        window.textBox("filenameTextField").deleteText().enterText("test.txt").pressKey(KeyEvent.VK_ENTER);
        window.button("exportButton").requireDisabled();
        window.textBox("filenameTextField").deleteText().enterText("test.png").pressKey(KeyEvent.VK_ENTER);
        window.button("exportButton").requireEnabled();
        window.comboBox("fileTypeComboBox").requireSelection(Pattern.compile("PNG.*")); // PNG
    }

    @Test
    public void enterFileWithoutExtensionPng() {
        window.textBox("filenameTextField").deleteText();
        window.comboBox("fileTypeComboBox").selectItem(Pattern.compile("PNG.*")); // PNG
        window.textBox("filenameTextField").deleteText().enterText("test").pressKey(KeyEvent.VK_ENTER).requireText("test.png");
        window.button("exportButton").requireEnabled();
        window.comboBox("fileTypeComboBox").requireSelection(Pattern.compile("PNG.*"));
    }

    @Test
    public void enterFileWithoutExtensionGif() {
        window.textBox("filenameTextField").deleteText();
        window.comboBox("fileTypeComboBox").selectItem(Pattern.compile("GIF.*")); // GIF
        window.textBox("filenameTextField").deleteText().enterText("test").pressKey(KeyEvent.VK_ENTER).requireText("test.gif");
        window.button("exportButton").requireEnabled();
        window.comboBox("fileTypeComboBox").requireSelection(Pattern.compile("GIF.*"));
    }

    @Test
    public void enterFileWithoutExtensionJpg() {
        window.textBox("filenameTextField").deleteText();
        window.comboBox("fileTypeComboBox").selectItem(Pattern.compile("JPEG.*")); // JPEG
        window.textBox("filenameTextField").deleteText().enterText("test").pressKey(KeyEvent.VK_ENTER).requireText("test.jpg");
        window.button("exportButton").requireEnabled();
        window.comboBox("fileTypeComboBox").requireSelection(2);
    }

    @Test
    public void invalidAndValidDirectoryEntered() {
        String bogus;
        String aok;
        if (PlatformUtilities.isOSX()) {
            bogus="/blah";
            aok="/tmp";
        } else {
            bogus="C:\\blah";
            aok="C:\\";
        }
        window.textBox("filenameTextField").deleteText().enterText("test.png").pressKey(KeyEvent.VK_ENTER);
        window.textBox("directoryTextField").deleteText().enterText(bogus).pressKey(KeyEvent.VK_ENTER);;
        window.button("exportButton").requireDisabled();
        window.textBox("directoryTextField").deleteText().enterText(aok).pressKey(KeyEvent.VK_ENTER);
        window.button("exportButton").requireEnabled();
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
    public void pressExportButton() {
        String filename="test.png";
        String directory;
        if (PlatformUtilities.isOSX()) {
            directory = "/tmp";
        } else {
            directory = "C:\\";
        }
        window.textBox("filenameTextField").deleteText().enterText(filename).pressKey(KeyEvent.VK_ENTER);
        window.textBox("directoryTextField").deleteText().enterText(directory).pressKey(KeyEvent.VK_ENTER);
        window.button("exportButton").click();
        assertEquals(ImageExportChooser.getOption(),ImageExportChooser.APPROVE_OPTION);
        assertEquals(ImageExportChooser.getSelectedFile().getParent(), directory);
    }

    @Test
    public void enterInvalidResolution() {
        // TODO test focus change and enter separately
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

    /**
     * Test of getBasename method, of class ImageExportChooser.
     */
    @Test
    public void testGetBasename() {
        System.out.println("getBasename");
        String filename = "test.txt";
        String expResult = "test";
        String result = PlatformUtilities.getBasename(filename);
        assertEquals(expResult, result);
        filename = "test";
        expResult = "test";
        result = PlatformUtilities.getBasename(filename);
        assertEquals(expResult, result);
        filename = "test.txt.jpg";
        expResult = "test.txt";
        result = PlatformUtilities.getBasename(filename);
        assertEquals(expResult, result);
    }

    /**
     * Test of getExtension method, of class ImageExportChooser.
     */
    @Test
    public void testGetExtension() {
        System.out.println("getExtension");
        String filename = "test.txt";
        String expResult = ".txt";
        String result = PlatformUtilities.getExtension(filename);
        assertEquals(expResult, result);
        filename = "test";
        expResult = "";
        result = PlatformUtilities.getExtension(filename);
        assertEquals(expResult, result);
        filename = "test.txt.jpg";
        expResult = ".jpg";
        result = PlatformUtilities.getExtension(filename);
        assertEquals(expResult, result);
    }

    @Test
    public void testTypeComboBox() {
        String filename = "test.png";
        window.textBox("filenameTextField").deleteText().enterText(filename).pressKey(KeyEvent.VK_ENTER);
        String result = PlatformUtilities.getExtension(filename);
        assertEquals(".png", result);
        window.comboBox("fileTypeComboBox").selectItem(Pattern.compile("GIF.*")); // JPEG
        window.textBox("filenameTextField").requireText("test.gif");
        window.comboBox("fileTypeComboBox").selectItem(Pattern.compile("JPEG.*")); // JPEG
        window.textBox("filenameTextField").requireText("test.jpg");
        window.comboBox("fileTypeComboBox").selectItem(Pattern.compile("PNG.*")); // JPEG
        window.textBox("filenameTextField").requireText("test.png");
    }

}