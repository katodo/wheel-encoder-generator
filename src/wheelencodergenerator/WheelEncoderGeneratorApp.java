/*
 * WheelEncoderGeneratorApp.java
 */

package wheelencodergenerator;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import java.awt.Toolkit;

/**
 * The main class of the application.
 */
public class WheelEncoderGeneratorApp extends SingleFrameApplication implements Application.ExitListener
{
    protected WheelEncoderGeneratorView view;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        addExitListener(this);
        com.botthoughts.Debug.println("added exit listener.");
        show(view = new WheelEncoderGeneratorView(this));
        //getMainFrame().setIconImage(Toolkit.getDefaultToolkit().getImage("wheelencodergenerator/resources/WheelEncoderGenerator.ico"));
        //view.setTaskBarIcon();
        view.fixButtonWidths();
    }

    @Override public boolean canExit(java.util.EventObject e) {
        com.botthoughts.Debug.println("calling appView.quit()");
        return view.quit();
    }

    @Override public void willExit(java.util.EventObject e) {
    }
 
    
    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of WheelEncoderGeneratorApp
     */
    public static WheelEncoderGeneratorApp getApplication() {
        return Application.getInstance(WheelEncoderGeneratorApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        // Mac Specific stuff handled within Info.plist

        System.setProperty("apple.laf.useScreenMenuBar","true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WheelEncoderGenerator");
        System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
        System.out.println("launching app...");
        launch(WheelEncoderGeneratorApp.class, args);
    }
}
