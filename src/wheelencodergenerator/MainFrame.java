/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TestFrame.java
 *
 * Created on Oct 26, 2012, 3:23:26 PM
 */

package wheelencodergenerator;

import com.apple.OSXAdapter;
import org.jdesktop.application.Action;
import com.botthoughts.JFileChooser;
import com.botthoughts.JFileFilter;
import com.botthoughts.UpdateChecker;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;


/**
 *
 * @author Michael Shimniok
 */
public class MainFrame extends javax.swing.JFrame {

    // TODO: make these URLs properties
    private String versionUrl;
    private String downloadUrl;
    private String issueUrl;
    private String donateUrl;
    private WheelEncoder encoder;
    private File encoderFile; // TODO: Med: encapsulate in encoder?
    private JFileFilter wegFileFilter = new JFileFilter();
    private JFileFilter imageFileFilter = new JFileFilter();
    private ArrayList<JFileFilter> imageFileFilterList = new ArrayList<JFileFilter>();
    private DiameterInputVerifier numVerifier = new DiameterInputVerifier();
    private HelpSet hs;
    private HelpBroker hb;
    private CSH.DisplayHelpFromSource helpHandler;
    public static boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x")); // TODO: use OperatingSystemValidator
    public static int MENU_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    public static String NEW_FILE = "";
    private UpdateChecker updateChecker;
    private static boolean VERBOSE=true;
    private static boolean QUIET=false;
    private ImageExportChooser exporter = new ImageExportChooser();
    private SpinnerNumberModel resolutionSpinnerModel = new SpinnerNumberModel(16, 4, 36000, 2);
    private String appTitle;
    private String appVersion;
    private JDialog aboutBox;
    private FrameView parentView = null;

    public MainFrame(FrameView parentView) {
        this();
        this.parentView = parentView;
    }

    /** Creates new form TestFrame */
    public MainFrame() {
        System.out.println("Initializing help...");
        // Setup help system
        hs = getHelpSet("wheelencodergenerator/resources/help/WEGHelp_en.hs");
        if (hs != null) {
            hb = hs.createHelpBroker();
            helpHandler = new CSH.DisplayHelpFromSource(hb);
        }

        System.out.println("Initializing URLs...");
        Properties prop = new Properties();
        ApplicationContext c = Application.getInstance(wheelencodergenerator.WheelEncoderGeneratorApp.class).getContext();
        ResourceMap rm = c.getResourceMap(WheelEncoderGeneratorApp.class);
        appVersion = rm.getString("Application.version");
        appTitle = rm.getString("Application.title");

        try {
            prop.load(getClass().getResourceAsStream("/wheelencodergenerator/resources/app.properties"));
            versionUrl = prop.getProperty("version.url");
            downloadUrl = prop.getProperty("download.url");
            donateUrl = prop.getProperty("donate.url");
            issueUrl = prop.getProperty("issue.url");
        } catch (Exception ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Initializing components...");
        initComponents();
        registerForMacOSXEvents(); // OSX-specific setup
        // Setup file filters for weg and image files
        wegFileFilter.setDescription("Wheel Encoder Generator files (*.weg)");
        wegFileFilter.addType(".weg");
        imageFileFilter.setDescription("Images (*.png, *.gif, *.jpg)");
        imageFileFilter.addType(".png");
        imageFileFilter.addType(".gif");
        imageFileFilter.addType(".jpg");
        imageFileFilterList.add( new JFileFilter("PNG file", ".png") );
        imageFileFilterList.add( new JFileFilter("GIF file", ".gif") );
        imageFileFilterList.add( new JFileFilter("JPEG file", ".jpg") );
        // Set taskbar / app jframe icon
        // TODO convert this to resource/property
        Image image = new ImageIcon(getClass().getResource("/wheelencodergenerator/resources/windows/WheelEncoderGenerator.png")).getImage();
        this.setIconImage(image);
        // Initial "load" of new encoder
        newEncoder();
        System.out.println("Done with View initialization...");

        com.botthoughts.Debug.println(Thread.currentThread().getName());
        try {
            updateChecker = new UpdateChecker();
            updateChecker.setURL(versionUrl);
            updateChecker.setVersion(getVersion());
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    // Here, we can safely update the GUI
                    // because we'll be called from the
                    // event dispatch thread
                    com.botthoughts.Debug.println(Thread.currentThread().getName());
                    CheckThreadViolationRepaintManager.setCurrentManager(new CheckThreadViolationRepaintManager());
                    promptUpdate(QUIET);
                }
            });
        } catch (MalformedURLException ex) {
            Logger.getLogger(WheelEncoderGeneratorView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Generic registration with the Mac OS X application menu
    // Checks the platform, then attempts to register with the Apple EAWT
    // See OSXAdapter.java to see how this is done without directly referencing any Apple APIs
    private void registerForMacOSXEvents() {
        if (MAC_OS_X) {
            try {
                // Generate and register the OSXAdapter, passing it a hash of all the methods we wish to
                // use as delegates for various com.apple.eawt.ApplicationListener methods
                OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("quit", (Class[])null));
                OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("about", (Class[])null));
                //OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("preferences", (Class[])null));
                //OSXAdapter.setFileHandler(this, getClass().getDeclaredMethod("loadImageFile", new Class[] { String.class }));
            } catch (Exception e) {
                System.err.println("Error while loading the OSXAdapter: " + e.getMessage());
            }
        }
    }


    private class DiameterInputVerifier extends InputVerifier {
        @Override
        public boolean verify(JComponent input) {
            boolean outcome = false;
            JTextField textField = (JTextField) input;
            try {
                int i = Integer.parseInt(textField.getText());
                if (i > 0) {
                    outcome = true;
                } else {
                    outcome = false;
                }
            } catch (Exception e) {
                outcome = false;
            }

            if (outcome == false)
                input.setForeground(Color.red);
            else
                input.setForeground(Color.black);

            guiErrorController(outcome);

            return outcome;
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
        java.awt.GridBagConstraints gridBagConstraints;

        codeButtonGroup = new javax.swing.ButtonGroup();
        toolBar = new javax.swing.JToolBar();
        newButton = new javax.swing.JButton();
        openButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        saveAsButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();
        printButton = new javax.swing.JButton();
        mainPanel = new javax.swing.JPanel();
        encoderPanel = new wheelencodergenerator.EncoderPanel();
        controlPanel = new javax.swing.JPanel();
        encoderTabbedPane = new javax.swing.JTabbedPane();
        standardPanel = new javax.swing.JPanel();
        resolutionLabel1 = new javax.swing.JLabel();
        resolutionSpinner = new javax.swing.JSpinner();
        quadratureCheckBox = new javax.swing.JCheckBox();
        indexCheckBox = new javax.swing.JCheckBox();
        absolutePanel = new javax.swing.JPanel();
        grayCodeRadioButton = new javax.swing.JRadioButton();
        binaryCodeRadioButton = new javax.swing.JRadioButton();
        resolutionLabel2 = new javax.swing.JLabel();
        absoluteResolutionComboBox = new javax.swing.JComboBox();
        diameterPanel = new javax.swing.JPanel();
        innerDiameterLabel = new javax.swing.JLabel();
        innerDiameter = new javax.swing.JTextField();
        outerDiameterLabel = new javax.swing.JLabel();
        outerDiameter = new javax.swing.JTextField();
        mmButton = new javax.swing.JRadioButton();
        inchButton = new javax.swing.JRadioButton();
        otherPanel = new javax.swing.JPanel();
        invertCheckBox = new javax.swing.JCheckBox();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newMenuItem = new javax.swing.JMenuItem();
        openMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        exportMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        printMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        helpMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        updateMenuItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        reportMenuItem = new javax.swing.JMenuItem();
        donateMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(600, 500));
        setName("Form"); // NOI18N
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setMaximumSize(new java.awt.Dimension(9999, 65));
        toolBar.setMinimumSize(new java.awt.Dimension(200, 65));
        toolBar.setName("toolBar"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(wheelencodergenerator.WheelEncoderGeneratorApp.class).getContext().getActionMap(MainFrame.class, this);
        newButton.setAction(actionMap.get("newEncoder")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(wheelencodergenerator.WheelEncoderGeneratorApp.class).getContext().getResourceMap(MainFrame.class);
        newButton.setIcon(resourceMap.getIcon("newButton.icon")); // NOI18N
        newButton.setText(resourceMap.getString("newButton.text")); // NOI18N
        newButton.setToolTipText(resourceMap.getString("newButton.toolTipText")); // NOI18N
        newButton.setBorderPainted(false);
        newButton.setFocusable(false);
        newButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        newButton.setMaximumSize(new java.awt.Dimension(50, 50));
        newButton.setMinimumSize(new java.awt.Dimension(32, 50));
        newButton.setName("newButton"); // NOI18N
        newButton.setPreferredSize(new java.awt.Dimension(32, 50));
        newButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(newButton);

        openButton.setAction(actionMap.get("openEncoder")); // NOI18N
        openButton.setIcon(resourceMap.getIcon("openButton.icon")); // NOI18N
        openButton.setText(resourceMap.getString("openButton.text")); // NOI18N
        openButton.setToolTipText(resourceMap.getString("openButton.toolTipText")); // NOI18N
        openButton.setBorderPainted(false);
        openButton.setFocusable(false);
        openButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        openButton.setMaximumSize(new java.awt.Dimension(50, 50));
        openButton.setMinimumSize(new java.awt.Dimension(32, 50));
        openButton.setName("openButton"); // NOI18N
        openButton.setPreferredSize(new java.awt.Dimension(32, 50));
        openButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(openButton);

        saveButton.setAction(actionMap.get("saveEncoder")); // NOI18N
        saveButton.setIcon(resourceMap.getIcon("saveButton.icon")); // NOI18N
        saveButton.setText(resourceMap.getString("saveButton.text")); // NOI18N
        saveButton.setToolTipText(resourceMap.getString("saveButton.toolTipText")); // NOI18N
        saveButton.setBorderPainted(false);
        saveButton.setFocusable(false);
        saveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveButton.setMaximumSize(new java.awt.Dimension(50, 50));
        saveButton.setMinimumSize(new java.awt.Dimension(32, 50));
        saveButton.setName("saveButton"); // NOI18N
        saveButton.setPreferredSize(new java.awt.Dimension(32, 50));
        saveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(saveButton);

        saveAsButton.setAction(actionMap.get("saveEncoderAs")); // NOI18N
        saveAsButton.setIcon(resourceMap.getIcon("saveAsButton.icon")); // NOI18N
        saveAsButton.setText(resourceMap.getString("saveAsButton.text")); // NOI18N
        saveAsButton.setToolTipText(resourceMap.getString("saveAsButton.toolTipText")); // NOI18N
        saveAsButton.setBorderPainted(false);
        saveAsButton.setFocusable(false);
        saveAsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveAsButton.setMaximumSize(new java.awt.Dimension(55, 50));
        saveAsButton.setMinimumSize(new java.awt.Dimension(32, 50));
        saveAsButton.setName("saveAsButton"); // NOI18N
        saveAsButton.setPreferredSize(new java.awt.Dimension(32, 50));
        saveAsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(saveAsButton);

        exportButton.setAction(actionMap.get("exportEncoder")); // NOI18N
        exportButton.setIcon(resourceMap.getIcon("exportButton.icon")); // NOI18N
        exportButton.setText(resourceMap.getString("exportButton.text")); // NOI18N
        exportButton.setToolTipText(resourceMap.getString("exportButton.toolTipText")); // NOI18N
        exportButton.setBorderPainted(false);
        exportButton.setFocusable(false);
        exportButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        exportButton.setMaximumSize(new java.awt.Dimension(50, 50));
        exportButton.setMinimumSize(new java.awt.Dimension(32, 50));
        exportButton.setName("exportButton"); // NOI18N
        exportButton.setPreferredSize(new java.awt.Dimension(32, 50));
        exportButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(exportButton);

        printButton.setAction(actionMap.get("printEncoder")); // NOI18N
        printButton.setIcon(resourceMap.getIcon("printButton.icon")); // NOI18N
        printButton.setText(resourceMap.getString("printButton.text")); // NOI18N
        printButton.setToolTipText(resourceMap.getString("printButton.toolTipText")); // NOI18N
        printButton.setBorderPainted(false);
        printButton.setFocusable(false);
        printButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        printButton.setMaximumSize(new java.awt.Dimension(50, 50));
        printButton.setMinimumSize(new java.awt.Dimension(32, 50));
        printButton.setName("printButton"); // NOI18N
        printButton.setPreferredSize(new java.awt.Dimension(32, 50));
        printButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(printButton);

        mainPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED, resourceMap.getColor("mainPanel.border.highlightOuterColor"), resourceMap.getColor("mainPanel.border.highlightInnerColor"), resourceMap.getColor("mainPanel.border.shadowOuterColor"), resourceMap.getColor("mainPanel.border.shadowInnerColor"))); // NOI18N
        mainPanel.setMaximumSize(new java.awt.Dimension(9999, 9999));
        mainPanel.setMinimumSize(new java.awt.Dimension(580, 370));
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setPreferredSize(new java.awt.Dimension(580, 370));

        encoderPanel.setBackground(resourceMap.getColor("encoderPanel.background")); // NOI18N
        encoderPanel.setToolTipText(resourceMap.getString("encoderPanel.toolTipText")); // NOI18N
        encoderPanel.setMinimumSize(new java.awt.Dimension(350, 350));
        encoderPanel.setName("encoderPanel"); // NOI18N
        encoderPanel.setPreferredSize(new java.awt.Dimension(350, 350));
        encoderPanel.setWheelEncoder(null);
        encoderPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                encoderPanelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout encoderPanelLayout = new javax.swing.GroupLayout(encoderPanel);
        encoderPanel.setLayout(encoderPanelLayout);
        encoderPanelLayout.setHorizontalGroup(
            encoderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 350, Short.MAX_VALUE)
        );
        encoderPanelLayout.setVerticalGroup(
            encoderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 414, Short.MAX_VALUE)
        );

        controlPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("controlPanel.border.title"))); // NOI18N
        controlPanel.setMaximumSize(new java.awt.Dimension(260, 350));
        controlPanel.setMinimumSize(new java.awt.Dimension(170, 350));
        controlPanel.setName("controlPanel"); // NOI18N
        controlPanel.setLayout(new java.awt.GridBagLayout());

        encoderTabbedPane.setToolTipText(resourceMap.getString("encoderTabbedPane.toolTipText")); // NOI18N
        encoderTabbedPane.setName("encoderTabbedPane"); // NOI18N
        encoderTabbedPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                encoderTabbedPaneMouseClicked(evt);
            }
        });

        standardPanel.setMaximumSize(new java.awt.Dimension(131, 96));
        standardPanel.setName("standardPanel"); // NOI18N
        standardPanel.setLayout(new java.awt.GridBagLayout());

        resolutionLabel1.setText(resourceMap.getString("resolutionLabel1.text")); // NOI18N
        resolutionLabel1.setName("resolutionLabel1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        standardPanel.add(resolutionLabel1, gridBagConstraints);

        resolutionSpinner.setModel((SpinnerModel) resolutionSpinnerModel);
        resolutionSpinner.setToolTipText(resourceMap.getString("resolutionSpinner.toolTipText")); // NOI18N
        resolutionSpinner.setMaximumSize(new java.awt.Dimension(37, 20));
        resolutionSpinner.setMinimumSize(new java.awt.Dimension(37, 20));
        resolutionSpinner.setName("resolutionSpinner"); // NOI18N
        resolutionSpinner.setPreferredSize(new java.awt.Dimension(37, 20));
        resolutionSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                resolutionSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 10);
        standardPanel.add(resolutionSpinner, gridBagConstraints);

        quadratureCheckBox.setText(resourceMap.getString("quadratureCheckBox.text")); // NOI18N
        quadratureCheckBox.setToolTipText(resourceMap.getString("quadratureCheckBox.toolTipText")); // NOI18N
        quadratureCheckBox.setName("quadratureCheckBox"); // NOI18N
        quadratureCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                quadratureCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        standardPanel.add(quadratureCheckBox, gridBagConstraints);

        indexCheckBox.setText(resourceMap.getString("indexCheckBox.text")); // NOI18N
        indexCheckBox.setToolTipText(resourceMap.getString("indexCheckBox.toolTipText")); // NOI18N
        indexCheckBox.setName("indexCheckBox"); // NOI18N
        indexCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                indexCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        standardPanel.add(indexCheckBox, gridBagConstraints);

        encoderTabbedPane.addTab(resourceMap.getString("standardPanel.TabConstraints.tabTitle"), null, standardPanel, resourceMap.getString("standardPanel.TabConstraints.tabToolTip")); // NOI18N

        absolutePanel.setName("absolutePanel"); // NOI18N
        absolutePanel.setLayout(new java.awt.GridBagLayout());

        codeButtonGroup.add(grayCodeRadioButton);
        grayCodeRadioButton.setSelected(true);
        grayCodeRadioButton.setText(resourceMap.getString("grayCodeRadioButton.text")); // NOI18N
        grayCodeRadioButton.setToolTipText(resourceMap.getString("grayCodeRadioButton.toolTipText")); // NOI18N
        grayCodeRadioButton.setName("grayCodeRadioButton"); // NOI18N
        grayCodeRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                grayCodeRadioButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipadx = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        absolutePanel.add(grayCodeRadioButton, gridBagConstraints);

        codeButtonGroup.add(binaryCodeRadioButton);
        binaryCodeRadioButton.setText(resourceMap.getString("binaryCodeRadioButton.text")); // NOI18N
        binaryCodeRadioButton.setToolTipText(resourceMap.getString("binaryCodeRadioButton.toolTipText")); // NOI18N
        binaryCodeRadioButton.setName("binaryCodeRadioButton"); // NOI18N
        binaryCodeRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                binaryCodeRadioButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 0);
        absolutePanel.add(binaryCodeRadioButton, gridBagConstraints);

        resolutionLabel2.setText(resourceMap.getString("resolutionLabel2.text")); // NOI18N
        resolutionLabel2.setName("resolutionLabel2"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        absolutePanel.add(resolutionLabel2, gridBagConstraints);

        absoluteResolutionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024", "2048", "4096" }));
        absoluteResolutionComboBox.setSelectedIndex(3);
        absoluteResolutionComboBox.setToolTipText(resourceMap.getString("absoluteResolutionComboBox.toolTipText")); // NOI18N
        absoluteResolutionComboBox.setMaximumSize(new java.awt.Dimension(60, 20));
        absoluteResolutionComboBox.setMinimumSize(new java.awt.Dimension(60, 20));
        absoluteResolutionComboBox.setName("absoluteResolutionComboBox"); // NOI18N
        absoluteResolutionComboBox.setPreferredSize(new java.awt.Dimension(60, 20));
        absoluteResolutionComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                absoluteResolutionComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 6, 10, 15);
        absolutePanel.add(absoluteResolutionComboBox, gridBagConstraints);

        encoderTabbedPane.addTab(resourceMap.getString("absolutePanel.TabConstraints.tabTitle"), null, absolutePanel, resourceMap.getString("absolutePanel.TabConstraints.tabToolTip")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        controlPanel.add(encoderTabbedPane, gridBagConstraints);

        diameterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("diameterPanel.border.title"))); // NOI18N
        diameterPanel.setName("diameterPanel"); // NOI18N
        diameterPanel.setLayout(new java.awt.GridBagLayout());

        innerDiameterLabel.setText(resourceMap.getString("innerDiameterLabel.text")); // NOI18N
        innerDiameterLabel.setName("innerDiameterLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        diameterPanel.add(innerDiameterLabel, gridBagConstraints);

        innerDiameter.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        innerDiameter.setText(resourceMap.getString("innerDiameter.text")); // NOI18N
        innerDiameter.setToolTipText(resourceMap.getString("innerDiameter.toolTipText")); // NOI18N
        innerDiameter.setInputVerifier(numVerifier);
        innerDiameter.setMinimumSize(null);
        innerDiameter.setName("innerDiameter"); // NOI18N
        innerDiameter.setPreferredSize(null);
        innerDiameter.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                innerDiameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 30;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        diameterPanel.add(innerDiameter, gridBagConstraints);

        outerDiameterLabel.setText(resourceMap.getString("outerDiameterLabel.text")); // NOI18N
        outerDiameterLabel.setName("outerDiameterLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 0);
        diameterPanel.add(outerDiameterLabel, gridBagConstraints);

        outerDiameter.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        outerDiameter.setText(resourceMap.getString("outerDiameter.text")); // NOI18N
        outerDiameter.setToolTipText(resourceMap.getString("outerDiameter.toolTipText")); // NOI18N
        outerDiameter.setInputVerifier(numVerifier);
        outerDiameter.setMinimumSize(null);
        outerDiameter.setName("outerDiameter"); // NOI18N
        outerDiameter.setPreferredSize(null);
        outerDiameter.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                outerDiameterFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 30;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        diameterPanel.add(outerDiameter, gridBagConstraints);

        mmButton.setSelected(true);
        mmButton.setText(resourceMap.getString("mmButton.text")); // NOI18N
        mmButton.setToolTipText(resourceMap.getString("mmButton.toolTipText")); // NOI18N
        mmButton.setName("mmButton"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 10, 0);
        diameterPanel.add(mmButton, gridBagConstraints);

        inchButton.setText(resourceMap.getString("inchButton.text")); // NOI18N
        inchButton.setToolTipText(resourceMap.getString("inchButton.toolTipText")); // NOI18N
        inchButton.setEnabled(false);
        inchButton.setName("inchButton"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 10, 0);
        diameterPanel.add(inchButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        controlPanel.add(diameterPanel, gridBagConstraints);

        otherPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("otherPanel.border.title"))); // NOI18N
        otherPanel.setName("otherPanel"); // NOI18N
        otherPanel.setPreferredSize(new java.awt.Dimension(204, 80));
        otherPanel.setLayout(new java.awt.GridBagLayout());

        invertCheckBox.setText(resourceMap.getString("invertCheckBox.text")); // NOI18N
        invertCheckBox.setActionCommand(resourceMap.getString("invertCheckBox.actionCommand")); // NOI18N
        invertCheckBox.setName("invertCheckBox"); // NOI18N
        invertCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                invertCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        otherPanel.add(invertCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        controlPanel.add(otherPanel, gridBagConstraints);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(encoderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(8, 8, 8)
                .addComponent(controlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(encoderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE))
                    .addComponent(controlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        menuBar.setName("menuBar"); // NOI18N
        menuBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                menuBarMouseClicked(evt);
            }
        });

        fileMenu.setAction(actionMap.get("newEncoder")); // NOI18N
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        newMenuItem.setAction(actionMap.get("newEncoder")); // NOI18N
        newMenuItem.setText(resourceMap.getString("newMenuItem.text")); // NOI18N
        newMenuItem.setName("newMenuItem"); // NOI18N
        fileMenu.add(newMenuItem);
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,MENU_MASK));

        openMenuItem.setAction(actionMap.get("openEncoder")); // NOI18N
        openMenuItem.setText(resourceMap.getString("openMenuItem.text")); // NOI18N
        openMenuItem.setName("openMenuItem"); // NOI18N
        fileMenu.add(openMenuItem);
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,MENU_MASK));

        jSeparator3.setName("jSeparator3"); // NOI18N
        fileMenu.add(jSeparator3);

        saveMenuItem.setAction(actionMap.get("saveEncoder")); // NOI18N
        saveMenuItem.setText(resourceMap.getString("saveMenuItem.text")); // NOI18N
        saveMenuItem.setName("saveMenuItem"); // NOI18N
        fileMenu.add(saveMenuItem);
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,MENU_MASK));

        saveAsMenuItem.setAction(actionMap.get("saveEncoderAs")); // NOI18N
        saveAsMenuItem.setText(resourceMap.getString("saveAsMenuItem.text")); // NOI18N
        saveAsMenuItem.setName("saveAsMenuItem"); // NOI18N
        fileMenu.add(saveAsMenuItem);

        exportMenuItem.setAction(actionMap.get("exportEncoder")); // NOI18N
        exportMenuItem.setText(resourceMap.getString("exportMenuItem.text")); // NOI18N
        exportMenuItem.setName("exportMenuItem"); // NOI18N
        fileMenu.add(exportMenuItem);

        jSeparator1.setName("jSeparator1"); // NOI18N
        fileMenu.add(jSeparator1);

        printMenuItem.setAction(actionMap.get("printEncoder")); // NOI18N
        printMenuItem.setText(resourceMap.getString("printMenuItem.text")); // NOI18N
        printMenuItem.setName("printMenuItem"); // NOI18N
        fileMenu.add(printMenuItem);
        printMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,MENU_MASK));

        jSeparator2.setName("jSeparator2"); // NOI18N
        if (!MAC_OS_X) {
            fileMenu.add(jSeparator2);

            exitMenuItem.setText(resourceMap.getString("exitMenuItem.text")); // NOI18N
            exitMenuItem.setName("exitMenuItem"); // NOI18N
            exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    exitMenuItemActionPerformed(evt);
                }
            });
            fileMenu.add(exitMenuItem);
        }

        // OSX doesn't use menu icons or mnemonics
        // This is kind of klugey but unset icons and
        // set mnemonics here
        if (MAC_OS_X) {
            newMenuItem.setIcon(null);
            openMenuItem.setIcon(null);
            saveMenuItem.setIcon(null);
            saveAsMenuItem.setIcon(null);
            exportMenuItem.setIcon(null);
            printMenuItem.setIcon(null);
        } else {
            fileMenu.setMnemonic(KeyEvent.VK_F);
            newMenuItem.setMnemonic(KeyEvent.VK_N);
            openMenuItem.setMnemonic(KeyEvent.VK_O);
            saveMenuItem.setMnemonic(KeyEvent.VK_S);
            saveAsMenuItem.setMnemonic(KeyEvent.VK_A);
            exportMenuItem.setMnemonic(KeyEvent.VK_E);
            printMenuItem.setMnemonic(KeyEvent.VK_P);
            exitMenuItem.setMnemonic(KeyEvent.VK_X);
        }
        menuBar.add(fileMenu);

        helpMenu.setMnemonic(KeyEvent.VK_H);
        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        helpMenuItem.setText(resourceMap.getString("helpMenuItem.text")); // NOI18N
        helpMenuItem.setName("helpMenuItem"); // NOI18N
        helpMenu.add(helpMenuItem);
        if (!MAC_OS_X) {
            helpMenuItem.setMnemonic(KeyEvent.VK_T);
        } else {
            helpMenuItem.setText("WheelEncoderGenerator Help");
        }
        helpMenuItem.addActionListener(helpHandler);

        jSeparator4.setName("jSeparator4"); // NOI18N
        if (!MAC_OS_X) {
            helpMenu.add(jSeparator4);
        }

        aboutMenuItem.setAction(actionMap.get("about")); // NOI18N
        aboutMenuItem.setMnemonic(KeyEvent.VK_A);
        aboutMenuItem.setText(resourceMap.getString("aboutMenuItem.text")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        if (!MAC_OS_X) {
            aboutMenuItem.setMnemonic(KeyEvent.VK_A);
            helpMenu.add(aboutMenuItem);
        }

        updateMenuItem.setText(resourceMap.getString("updateMenuItem.text")); // NOI18N
        updateMenuItem.setName("updateMenuItem"); // NOI18N
        updateMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(updateMenuItem);

        jSeparator5.setName("jSeparator5"); // NOI18N
        helpMenu.add(jSeparator5);

        reportMenuItem.setText(resourceMap.getString("reportMenuItem.text")); // NOI18N
        reportMenuItem.setName("reportMenuItem"); // NOI18N
        reportMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reportMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(reportMenuItem);

        donateMenuItem.setText(resourceMap.getString("donateMenuItem.text")); // NOI18N
        donateMenuItem.setName("donateMenuItem"); // NOI18N
        donateMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                donateMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(donateMenuItem);

        if(!MAC_OS_X) {
            helpMenu.setMnemonic(KeyEvent.VK_H);
        }
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(265, 265, 265))
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        // This action handler is only called by non-Mac Exit menu item
        if (promptSaveFirst()) {
            this.setVisible(false);
            if (parentView != null) {
                parentView.getFrame().dispose();
            }
        }
}//GEN-LAST:event_exitMenuItemActionPerformed

    private void updateMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateMenuItemActionPerformed
        promptUpdate(VERBOSE);
}//GEN-LAST:event_updateMenuItemActionPerformed

    private void reportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reportMenuItemActionPerformed
        try {
            if (Desktop.isDesktopSupported()) {
                URI uri = URI.create(issueUrl);
                Desktop.getDesktop().browse(uri);
            } else {
                JOptionPane.showMessageDialog(null, "To report a problem, visit the Issues URL:\n" + issueUrl,
                        "Oops", JOptionPane.OK_OPTION);
            }
        } catch (IOException ex) {
            Logger.getLogger(WheelEncoderGeneratorView.class.getName()).log(Level.SEVERE, null, ex);
        }
}//GEN-LAST:event_reportMenuItemActionPerformed

    private void menuBarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_menuBarMouseClicked
        showPreview();
}//GEN-LAST:event_menuBarMouseClicked

    private void encoderPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_encoderPanelMouseClicked
        showPreview();
}//GEN-LAST:event_encoderPanelMouseClicked

    private void resolutionSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_resolutionSpinnerStateChanged
        showPreview();
}//GEN-LAST:event_resolutionSpinnerStateChanged

    private void quadratureCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_quadratureCheckBoxItemStateChanged
        encoder.setQuadratureTrack(evt.getStateChange() == ItemEvent.SELECTED);
        showPreview();
}//GEN-LAST:event_quadratureCheckBoxItemStateChanged

    private void indexCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_indexCheckBoxItemStateChanged
        encoder.setIndexTrack(evt.getStateChange() == ItemEvent.SELECTED);
        showPreview();
}//GEN-LAST:event_indexCheckBoxItemStateChanged

    private void grayCodeRadioButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grayCodeRadioButtonMouseReleased
        showPreview();
}//GEN-LAST:event_grayCodeRadioButtonMouseReleased

    private void binaryCodeRadioButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_binaryCodeRadioButtonMouseReleased
        showPreview();
}//GEN-LAST:event_binaryCodeRadioButtonMouseReleased

    private void absoluteResolutionComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_absoluteResolutionComboBoxItemStateChanged
        showPreview();
}//GEN-LAST:event_absoluteResolutionComboBoxItemStateChanged

    private void encoderTabbedPaneMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_encoderTabbedPaneMouseClicked
        showPreview();
}//GEN-LAST:event_encoderTabbedPaneMouseClicked

    private void innerDiameterFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_innerDiameterFocusLost
        showPreview();
}//GEN-LAST:event_innerDiameterFocusLost

    private void outerDiameterFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_outerDiameterFocusLost
        showPreview();
}//GEN-LAST:event_outerDiameterFocusLost

    private void invertCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_invertCheckBoxItemStateChanged
        showPreview();
}//GEN-LAST:event_invertCheckBoxItemStateChanged

    private void donateMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_donateMenuItemActionPerformed
        try {
            if (Desktop.isDesktopSupported()) {
                URI uri = URI.create(donateUrl);
                Desktop.getDesktop().browse(uri);
            } else {
                JOptionPane.showMessageDialog(null, "To update, visit the download URL:\n" + donateUrl,
                        "Oops", JOptionPane.OK_OPTION);
            }
        } catch (IOException ex) {
            Logger.getLogger(WheelEncoderGeneratorView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_donateMenuItemActionPerformed

    // Ensure that the aspect ratio of the encoderPanel is within about 5% of square
    // Otherwise, correct the width and height equally by half the error
    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        Dimension mySize = this.getSize();
        Dimension eSize = encoderPanel.getSize();
        int delta = eSize.height - eSize.width;
        if (delta/mySize.getWidth() < -0.05) { // too wide
            mySize.width += delta/2; // delta is negative
            mySize.height -= delta/2;  // delta is negative
        } else if (delta/mySize.getWidth() > 0.05) { // too tall
            mySize.height -= delta/2;  // delta is positive
            mySize.width += delta/2;  // delta is positive
        }
        this.setSize(mySize);
    }//GEN-LAST:event_formComponentResized


    /** Set up the GUI to reflect the settings in the WheelEncoder object
     * Used for loading or opening new encoder.
     */
    private void setWheelEncoder(WheelEncoder e)
    {
        encoder = e;
        // Setup all the interface stuff based on contents of the
        // encoder object
        innerDiameter.setText(Integer.toString(encoder.getInnerDiameter()));
        outerDiameter.setText(Integer.toString(encoder.getOuterDiameter()));
        if (encoder.getType() == WheelEncoder.STANDARD) {
            resolutionSpinner.getModel().setValue(encoder.getResolution());
            quadratureCheckBox.setSelected(encoder.hasQuadratureTrack());
            indexCheckBox.setSelected(encoder.hasIndexTrack());
            encoderTabbedPane.setSelectedIndex(encoderTabbedPane.indexOfTab("Incremental"));
        }
        else if (encoder.getType() == WheelEncoder.ABSOLUTE) {
            encoderTabbedPane.setSelectedIndex(encoderTabbedPane.indexOfTab("Absolute"));
            if (encoder.getNumbering() == WheelEncoder.GRAY) {
                grayCodeRadioButton.setSelected(true);
            }
            else if (encoder.getNumbering() == WheelEncoder.BINARY) {
                binaryCodeRadioButton.setSelected(true);
            }
            absoluteResolutionComboBox.setSelectedIndex(encoder.getResolution()-1);
        }
    }


     /** Sets the encoderFile attribute and the titlebar
      *
      */
    private void setEncoderFile(File file)
    {
        String theTitle;
        encoderFile = file;
        if (file == null || file.getName().equals(NEW_FILE))
            theTitle = "Untitled.weg";
        else
            theTitle = file.getName();
        if (!MAC_OS_X)
            theTitle += " - " + appTitle;
        this.setTitle(theTitle);
    }

    /** returns the encoder filename
     *
     * @return current encoder filename
     */
    private String getFilename()
    {
        String filename = "Untitled";
        if (encoderFile != null) {
            filename = encoderFile.getName();
        }
        return filename;
    }

   /**
    * find the helpset file and create a HelpSet object
    */
    private HelpSet getHelpSet(String helpsetfile) {
        HelpSet myHS = null;
        ClassLoader cl = this.getClass().getClassLoader();
        try {
            URL hsURL = HelpSet.findHelpSet(cl, helpsetfile);
            myHS = new HelpSet(null, hsURL);
        } catch(Exception ee) {
            System.out.println("HelpSet: "+ee.getMessage());
            System.out.println("HelpSet: "+ helpsetfile + " not found");
        }
        return myHS;
    }

    public final String getVersion() {
        // TODO fix the resourceMap stuff
        return appVersion;
    }


    private boolean errorCheck()
    {
        boolean result=true;

        try {
            if ( Integer.parseInt(innerDiameter.getText()) >= Integer.parseInt(outerDiameter.getText()) ) {
                // TODO use warning icons
                outerDiameter.setForeground(Color.red);
                innerDiameter.setForeground(Color.red);
                result = false;
            } else {
                outerDiameter.setForeground(Color.black);
                innerDiameter.setForeground(Color.black);
            }
            // Is resolution even (ok), or odd (not ok) ?
            int i = Integer.parseInt(resolutionSpinner.getModel().getValue().toString());
            if ( (i % 2) > 0 ) {
                i++; // just fix it (note that we can never get Maximum+1 so we can get away with increment
                resolutionSpinner.getModel().setValue(i);
            }
        } catch (NumberFormatException e) {
            result = false;
            // Already covered by an InputVerifier, but what the heck.
            JOptionPane.showMessageDialog(this,
                    "Error parsing numeric input", "Error",
                    JOptionPane.ERROR_MESSAGE );
        }

        // Disable functionality (print, etc) if something is jacked up
        guiErrorController(result);

        return result;
    }


    /* guiErrorController
     *
     * If there's an error this can be used to disable a bunch of interface
     * elements (save, save as, etc.  Or re-enable if error is gone.
     */
    private void guiErrorController(boolean result)
    {
        printMenuItem.setEnabled(result);
        printButton.setEnabled(result);
        saveAsMenuItem.setEnabled(result);
        saveAsButton.setEnabled(result);
        saveMenuItem.setEnabled(result);
        saveButton.setEnabled(result);
        exportMenuItem.setEnabled(result);
        exportButton.setEnabled(result);
        newMenuItem.setEnabled(result);
        newButton.setEnabled(result);
        openMenuItem.setEnabled(result);
        openButton.setEnabled(result);
    }



    public void showPreview()
    {
        if (errorCheck()) {
            encoder.setInnerDiameter(Integer.parseInt(innerDiameter.getText()));
            encoder.setOuterDiameter(Integer.parseInt(outerDiameter.getText()));
            encoder.setInverted(invertCheckBox.isSelected());

            // Absolute Encoder
            if (encoderTabbedPane.getSelectedIndex() == encoderTabbedPane.indexOfTab("Absolute")) {
                encoder.setType(WheelEncoder.ABSOLUTE);
                if (grayCodeRadioButton.isSelected() == true)
                    encoder.setNumbering(WheelEncoder.GRAY);
                else if (binaryCodeRadioButton.isSelected() == true)
                    encoder.setNumbering(WheelEncoder.BINARY);

                // ComboBox menu is set up so that # of tracks corresponds to selected index + 1
                encoder.setResolution(absoluteResolutionComboBox.getSelectedIndex()+1);
                //System.out.println("Track count: " + Integer.toString(encoder.getResolution()) + "\n");
            }
            else if (encoderTabbedPane.getSelectedIndex() == encoderTabbedPane.indexOfTab("Incremental")) {
                encoder.setType(WheelEncoder.STANDARD);
                encoder.setResolution(Integer.parseInt(resolutionSpinner.getModel().getValue().toString()));
            }
            encoderPanel.repaint();

            // Since showPreview() gets called anytime there's a change to the
            // encoder--load, save, modify, etc.--why not enable/disable the save
            // menu items here?
            if (encoder.isChanged()) {
                saveMenuItem.setEnabled(true);
                saveButton.setEnabled(true);
            } else {
                saveMenuItem.setEnabled(false);
                saveButton.setEnabled(false);
            }
        }

    }


    /** prompts whether to save an unsaved file before loading new one or quitting
     *
     * if file isn't changed:
     *   -> true
     * If file changed, prompt to save, discard, or cancel
     *   Save -> result of doSave()
     *   Discard -> true
     *   No -> false
     */
    private boolean promptSaveFirst()
    {
        boolean result=true;

        System.out.println("promptSaveFirst(): enter");
        com.botthoughts.Debug.println(Thread.currentThread().getName());

        if (encoder != null) { // null if first time through
            if (encoder.isChanged()) {
                int response = JOptionPane.showConfirmDialog(this,
                    "The encoder " + getFilename() + " has changed. Save the changes?", "Save?",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE );
                if (response == JOptionPane.YES_OPTION) {
                    if (encoderFile.getName().equals(NEW_FILE)) {
                        File newFile = promptFileSave(encoderFile, wegFileFilter);
                        if (newFile != null)
                            setEncoderFile(newFile);
                    }
                    // By now we should have encoderFile set to something useful
                    // so the result of this operation should set the result for quit()
                    // so that if save succeeds, then we quit, but if save fails, then
                    // we don't quit.
                    result = doSave(encoderFile);
                } else if (response == JOptionPane.CANCEL_OPTION) {
                    result = false;
                } else if (response == JOptionPane.NO_OPTION) {
                    result = true;
                } else {
                    result = false;
                }
            }
        }
        System.out.println("promptSaveFirst() -- return "+Boolean.toString(result));
        return result;
    }

    
    /** check for updated version and prompt user if so
     *
     */
    private void promptUpdate(boolean verbose) {
        if (updateChecker.checkUpdate()) {
            Object[] options = {"Download", "Skip"};
            JOptionPane pane = new JOptionPane();
            pane.setOptions(options);
            pane.setInitialSelectionValue(options[0]);
            pane.setOptionType(JOptionPane.YES_NO_OPTION);
            pane.setMessage("An updated version (" +
                    updateChecker.getCurrentVersion() +
                    ") of this software\nis available. Download?");
            pane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
            JDialog dialog = pane.createDialog(null, "Update Available");
            dialog.setVisible(true);
            Object selectedValue = pane.getValue();
            if (selectedValue.equals(options[0])) {
                // Launch your default web browser with ...
                try {
                    if (Desktop.isDesktopSupported()) {
                        URI uri = URI.create(downloadUrl);
                        Desktop.getDesktop().browse(uri);
                    } else {
                        JOptionPane.showMessageDialog(null, "To update, visit the download URL:\n" + downloadUrl,
                                "Oops", JOptionPane.OK_OPTION);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(WheelEncoderGeneratorView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else if (verbose) {
            JOptionPane.showMessageDialog(null, "Wheel Encoder Generator is up to date!", "Updated", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    /* promptFileSave()
     *
     * prompts to save a file using defaultFile as the initially selected file
     * which is nice because it suggests a correct file extension, hopefully,
     * at least in the case of an untitled document.
     */
    private File promptFileSave(File defaultFile, JFileFilter ff) {
        File file = null;

        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(ff);

        if (defaultFile == null || defaultFile.getName().equals(NEW_FILE)) {
            fc.setSelectedFile(new File("Untitled.weg"));
        } else {
            fc.setSelectedFile(defaultFile);
        }

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile().getAbsoluteFile();
            System.out.println("promptFileSave(): Selected File: "+file.getName());
        }

        return file;
    }

    /* doSave()
     *
     * Saves the encoder to the specified file
     * Wraps catches encoder.save() exception and displays dialog
     * If by some chance the file is null, hopefully encoder.save()
     * will throw an exception
     */
    private boolean doSave(File file)
    {
        boolean outcome = true;

        try {
            encoder.save(file);
            showPreview();
        }
        catch (IOException e) {
            outcome = false;
            JOptionPane.showMessageDialog(this,
                "Error saving file", "File Save Error: "+e.getMessage(),
                JOptionPane.ERROR_MESSAGE );
        }

        return outcome;
    }

    private boolean doOpen()
    {
        boolean outcome = false;
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(wegFileFilter);
        int option = fc.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                WheelEncoder enc = new WheelEncoder(file);
                setEncoderFile(file);
                setWheelEncoder(enc);
                encoderPanel.setWheelEncoder(encoder);
                this.setTitle(encoderFile.getName() + " - " + appTitle);
                showPreview();
                outcome = true;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Error reading file", "File Read Error",
                    JOptionPane.ERROR_MESSAGE );
                outcome = false;
            }
        } else {
            outcome = false;
        }
        return outcome;
    }

    /*
     * Set file to NEW_FILE to indicate to other routines that it is new
     */
    @Action
    public final void newEncoder() {
        if (promptSaveFirst()) {
            setWheelEncoder(new WheelEncoder());
            encoderPanel.setWheelEncoder(encoder);
            showPreview();
            setEncoderFile(new File(NEW_FILE));
        }
    }

    @Action
    public void saveEncoder() {
        if (encoderFile.getName().equals(NEW_FILE)) {
            File newFile = promptFileSave(encoderFile, wegFileFilter);
            if (newFile != null && doSave(newFile))
                setEncoderFile(newFile);
        } else {
            doSave(encoderFile);
        }
    }

    @Action
    public void saveEncoderAs() {
        File newFile = promptFileSave(encoderFile, wegFileFilter);
        if (newFile != null) {
            if (doSave(newFile)) {
                setEncoderFile(newFile);
            }
        }
    }

    @Action
    public void openEncoder() {
        if (promptSaveFirst())
            doOpen();
    }

    @Action
    public void printEncoder() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(encoderPanel);
        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
            } catch (PrinterException e) {
                // The job did not successfully complete
            }
        }

    }

    @Action
    public void exportEncoder() {
        int option = ImageExportChooser.showDialog(this);
        if (option == ImageExportChooser.APPROVE_OPTION) {
            try {
                File f = exporter.getSelectedFile();
                int response = JOptionPane.YES_OPTION;
                if (f.exists()) {
                    response = JOptionPane.showConfirmDialog(this,
                        "File "+f.getName()+" exists. Replace?", "File exists",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE );
                }
                if (response == JOptionPane.YES_OPTION) {
                    encoderPanel.export(ImageExportChooser.getSelectedFile(), ImageExportChooser.getSelectedFileType(), ImageExportChooser.getSelectedResolution());
                }
            } catch (IOException ex) {
                Logger.getLogger(WheelEncoderGeneratorView.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("exportEncoder(): CANCEL");
        }
    }

    @Action
    public void preferences() {
    }

    @Action
    public void about() {
        System.out.println("about() -- enter");
        if (aboutBox == null) {
//            JFrame mainFrame = WheelEncoderGeneratorApp.getApplication().getMainFrame();
            aboutBox = new WheelEncoderGeneratorAboutBox(this);
            aboutBox.setLocationRelativeTo(this);
            aboutBox.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
        WheelEncoderGeneratorApp.getApplication().show(aboutBox);
        System.out.println("about() -- exit");
    }

    /* Handles OSX quit menu as well as window close (cross platform)
     *
     * Returns:
     * true if ok to proceed quitting
     * false if cancelling quit
     *
     * Calls promptSaveFirst() to check if file needs saving,
     * returns boolean true if ok to proceed; false otherwise
     */
    public boolean quit()
    {
        return promptSaveFirst();
    }

    @Action
    public void help() {
    }

    
    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel absolutePanel;
    private javax.swing.JComboBox absoluteResolutionComboBox;
    private javax.swing.JRadioButton binaryCodeRadioButton;
    private javax.swing.ButtonGroup codeButtonGroup;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JPanel diameterPanel;
    private javax.swing.JMenuItem donateMenuItem;
    private wheelencodergenerator.EncoderPanel encoderPanel;
    private javax.swing.JTabbedPane encoderTabbedPane;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JButton exportButton;
    private javax.swing.JMenuItem exportMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JRadioButton grayCodeRadioButton;
    private javax.swing.JMenuItem helpMenuItem;
    private javax.swing.JRadioButton inchButton;
    private javax.swing.JCheckBox indexCheckBox;
    private javax.swing.JTextField innerDiameter;
    private javax.swing.JLabel innerDiameterLabel;
    private javax.swing.JCheckBox invertCheckBox;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JRadioButton mmButton;
    private javax.swing.JButton newButton;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JButton openButton;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JPanel otherPanel;
    private javax.swing.JTextField outerDiameter;
    private javax.swing.JLabel outerDiameterLabel;
    private javax.swing.JButton printButton;
    private javax.swing.JMenuItem printMenuItem;
    private javax.swing.JCheckBox quadratureCheckBox;
    private javax.swing.JMenuItem reportMenuItem;
    private javax.swing.JLabel resolutionLabel1;
    private javax.swing.JLabel resolutionLabel2;
    private javax.swing.JSpinner resolutionSpinner;
    private javax.swing.JButton saveAsButton;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JButton saveButton;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JPanel standardPanel;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JMenuItem updateMenuItem;
    // End of variables declaration//GEN-END:variables

}
