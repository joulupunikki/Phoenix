/*
 * Copyright (C) 2015 joulupunikki joulupunikki@gmail.communist.invalid.
 *
 *  Disclaimer of Warranties and Limitation of Liability.
 *
 *     The creators and distributors offer this software as-is and
 *     as-available, and make no representations or warranties of any
 *     kind concerning this software, whether express, implied, statutory,
 *     or other. This includes, without limitation, warranties of title,
 *     merchantability, fitness for a particular purpose, non-infringement,
 *     absence of latent or other defects, accuracy, or the presence or
 *     absence of errors, whether or not known or discoverable.
 *
 *     To the extent possible, in no event will the creators or distributors
 *     be liable on any legal theory (including, without limitation,
 *     negligence) or otherwise for any direct, special, indirect,
 *     incidental, consequential, punitive, exemplary, or other losses,
 *     costs, expenses, or damages arising out of the use of this software,
 *     even if the creators or distributors have been advised of the
 *     possibility of such losses, costs, expenses, or damages.
 *
 *     The disclaimer of warranties and limitation of liability provided
 *     above shall be interpreted in a manner that, to the extent possible,
 *     most closely approximates an absolute disclaimer and waiver of
 *     all liability.
 *
 */
package phoenix;

import gui.Gui;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.LinkedList;
import javax.swing.AbstractButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import util.C;
import util.CrashReporter;
import util.FN;
import util.FileNameCapitalizer;
import util.Util;
import util.UtilG;

/**
 * Main entry point of Phoenix, clone/remake/patch/replacement of the EFS.EXE in
 * the game Emperor of the Fading Suns.
 *
 * @author joulupunikki
 */
public class Phoenix {
//    public static final Logger logger = LogManager.getLogger();
    public static final long start_time;
    private static int event_number = 0;
    private static LinkedList<String> log_buffer = new LinkedList<>();
    //true iff a JMenu is open
    private static boolean log_mouse_move = false;
    private static String last_jmenu = null;
    private static Gui gui = null;
    private static PrintWriter input_log_writer;
    private static JFrame boot_frame = null;
    private static JTextArea boot_text = null; 
    private static String boot_string = null;
    /**
     * true iff Phoenix is doing an arbitrarily long task with user input
     * dependent length (such as stack moving with animation, which can be
     * stopped by user input at any point along the movement path) and wait
     * times should not be culled. Will be set by Phoenix event handlers which
     * start such tasks. Will be reset by after MousePressed event logged in an
     * AWTEventListener which is set in Phoenix.main.
     */
    private static boolean is_real_time = false;

    static {
        start_time = System.nanoTime();
    }

    public static void closeBootFrame() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
        }
        boot_frame.dispose();
    }
    
    public static void addBootMsg(String s) {
        System.out.println(s);
        boot_string += s;
        boot_text.setText(boot_string);
        boot_text.paintImmediately(BOOT_FRAME_X, BOOT_FRAME_Y, BOOT_FRAME_W, BOOT_FRAME_H);
    }
    
    /**
     * Main entry point of Phoenix.
     * <p>
     * parse commandline options <br>
     * log all uncaught Throwables <br>
     * log input events <br>
     * start GUI (the Phoenix proper)
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {        
        boot_string = "Phoenix started.\n" + "OS: "
                + System.getProperty("os.arch", "No arch info.") + " "
                + System.getProperty("os.name", "No name info.") + " "
                + System.getProperty("os.version", "No version info.") + "\n"
                + "System: "
                + "available cores " + Runtime.getRuntime().availableProcessors()
        ;
        System.out.println(boot_string);
        boot_frame = new JFrame("Phoenix boot GUI");
        
        boot_text = new JTextArea();
        boot_text.setText(boot_string);
        boot_text.setForeground(C.COLOR_GOLD);
        boot_text.setBackground(Color.BLACK);
        boot_frame.getContentPane().add(boot_text, BorderLayout.CENTER);
        boot_frame.pack();
        boot_frame.setVisible(true);
        boot_frame.setBounds(BOOT_FRAME_X, BOOT_FRAME_Y, BOOT_FRAME_W, BOOT_FRAME_H);
        setLAF();
//        logger.debug("Test log4j logging");

        // parse options
        CommandLine cli_opts = parseCLI(args);
        // wait if requested
        if (cli_opts.hasOption(C.OPT_WAIT_BEFORE_START)) {
            System.out.print("Press enter to start Phoenix");
            try {
                System.in.read();
            } catch (IOException ex) {

            }

        }
        //
        if (cli_opts.hasOption(C.OPT_CAPITALIZE_FILE_NAMES) && !FileNameCapitalizer.run()) {
            addBootMsg("\nFailed to ensure all filenames are uppercase.");
            addBootMsg("\nPhoenix halted.");
            CrashReporter.removeEventListeners();

            return;
        }
        // log all errors and exceptions
        rollLogs(FN.S_LOG_FILE);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println("Uncaught exception.");
                Util.logEx(t, e);
                CrashReporter.showCrashReport(e);
            }
        });
        // start GUI
        Gui.execute(cli_opts);
    }

    public static boolean startInputEventLogging(CommandLine cli_opts) {
        // log input events
        String file_name = FN.S_DIST_PREFIX + "input.log";
        rollLogs(file_name);
        Path input_log_file = FileSystems.getDefault().getPath(file_name);
        BufferedWriter event_log_buf = null;
        try {
            event_log_buf = Files.newBufferedWriter(input_log_file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException ex) {
            System.out.println("Unable to open input event log file \"" + file_name + "\"");
            Util.logEx(null, ex);
            CrashReporter.showCrashReport(ex);
            return true;
        }
        input_log_writer = new PrintWriter(event_log_buf, true);
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            //WORKAROUND JDK-6778087 : getLocationOnScreen() always returns (0, 0) for mouse wheel events, on Windows
            private Point prev_xy = new Point(-1, -1);
            private boolean prefix = true;
            @Override
            public void eventDispatched(AWTEvent event) {
                //System.out.println("#####Event : " + event.paramString());
                if (prefix) {
                    prefix = false;
                    Long random_seed = Phoenix.start_time;
                    if (cli_opts.hasOption(C.OPT_RANDOM_SEED)) {
                        random_seed = Long.getLong(cli_opts.getOptionValue(C.OPT_RANDOM_SEED));
                    }
                    if (RobotTester.isRobotTest()) {
                        random_seed = RobotTester.getRandomSeed();
                    }
                    input_log_writer.println(RobotTester.RANDOM_SEED_PREFIX + random_seed);
                    input_log_writer.println("# input logging started at " + (new Date()).toString());
                    input_log_writer.println("# fields (#8): nr time(ms) eventID button/wheel/key clicks screenX screenY source[=text]");
                }
                int id = event.getID();
                // On windows certain events always return 0,0 as their getLocationOnScreen()
                // This causes problems with robottester,  so we skip here everything
                // except that which may go into input.log
                switch (id) {
                    case MouseEvent.MOUSE_DRAGGED:
                    case MouseEvent.MOUSE_MOVED:
                    case MouseEvent.MOUSE_PRESSED:
                    case MouseEvent.MOUSE_RELEASED:
                    case MouseEvent.MOUSE_WHEEL:
                    case KeyEvent.KEY_PRESSED:
                    case KeyEvent.KEY_RELEASED:
                        break;
                    default:
                        return;
                }
                String details = "" + id;
                if (event instanceof MouseWheelEvent) { // check this first since ME is super of MWE
                    // Wheel Events seem to propagate beyond the original source
                    // multiple dispatches confuse RobotTester so return here if
                    // source container is not the first below mouse pointer
                    Container co = (Container) event.getSource();
                    if (co.getMousePosition(false) == null) {
                        return;
                    }
                    MouseWheelEvent me = (MouseWheelEvent) event;
                    details += " " + me.getWheelRotation() + " -1 "
                            + getCoordinates(me);
                    //System.out.println("MWE at : " + getCoordinates(me));
                } else if (event instanceof MouseEvent) {
                    MouseEvent me = (MouseEvent) event;
                    details += " " + me.getButton() + " " + me.getClickCount()
                            + " " + getCoordinates(me);
                } else if (event instanceof KeyEvent) {
                    KeyEvent ke = (KeyEvent) event;
                    details += " " + ke.getExtendedKeyCode() + " -1 -1 -1";
//                    int r = ke.getKeyCode();
//                    String key_char = "<non-unicode>";
//                    if (r <= KeyEvent.VK_ALT && r >= KeyEvent.VK_SHIFT) {
//                        key_char = "<alt/ctrl/shift>";
//                    } else {
//                        String tmp = Character.getName(ke.getKeyChar());
//                        if (tmp != null) {
//                            key_char = tmp.replace(' ', '_');
//                        }
//                    }
//                    details += " " + ke.getExtendedKeyCode() + " " + key_char;
                } else {
                    return;
                }
                String event_src = "";
                Object s = event.getSource();

                event_src += s.getClass().getCanonicalName();
                if (s instanceof AbstractButton) {
                    event_src += "=" + ((AbstractButton) s).getText();
                }
                details = details + RobotTester.S_SOURCE_SEP + event_src;
                //System.out.println("id: " + id);
                switch (id) {
                    // Mouse button/move events need special treatment
                    // with respect to JMenus.
                    case MouseEvent.MOUSE_DRAGGED:
//                        C.p("DRAG MOUSE " + (gui.getJMenuBar().findComponentAt(((MouseEvent) event).getLocationOnScreen())));
//                        C.p("DRAG MOUSE " + (((MouseEvent) event).getLocationOnScreen()));
                        if (log_mouse_move) {
                            logDispatched(details, event);
                        }
                    case MouseEvent.MOUSE_MOVED:
                        // if a JMenu is open and mouse cursor moves to a different
                        // JMenu, make note of the menu change
                        if (log_mouse_move && event_src.startsWith("javax.swing.JMenu=") && !last_jmenu.equals(event_src)) {
                            last_jmenu = event_src;
                            logDispatched(details, event);
                        }
                        break;
                    case MouseEvent.MOUSE_PRESSED:
                        // if on a JMenu, toggle mouse move logging
                        if (s instanceof JMenu) {
                            log_mouse_move = !log_mouse_move;
                            last_jmenu = event_src;
                        } else {
                            log_mouse_move = false;
                        }
                        logDispatched(details, event);
                        is_real_time = false;
                        break;
                    case MouseEvent.MOUSE_RELEASED:
                        // if not on a JMenu, disable mouse move logging
                        if (!(s instanceof javax.swing.JMenu)) {
                            log_mouse_move = false;
                        }
                        logDispatched(details, event);
                        break;
                    case MouseEvent.MOUSE_WHEEL:
                        log_mouse_move = false;
                    case KeyEvent.KEY_PRESSED:
                    case KeyEvent.KEY_RELEASED:
                        logDispatched(details, event);
                        break;
                    default:
                        break;
                }
            }

            private void logDispatched(String details, AWTEvent event) {
                int number = ++event_number;
                int time = (int) ((System.nanoTime() - start_time) / 1_000_000);
                if (is_real_time) {
                    number *= -1;
                }
                String logged = number + " " + time + " " + details;
                // logging delayed during 2click sequences
                if (RobotTester.isDelayedLogging()) {
                    log_buffer.addLast(logged);
                } else {
                    flushLogMessages();
                    input_log_writer.println(logged);
                }
                RobotTester.dispatchedEvent(logged);

                //System.out.println("#D " + number + " " + event);
            }

            private String getCoordinates(MouseEvent me) {
                Point p = Gui.getOrigin();
                //WORKAROUND JDK-6778087 : getLocationOnScreen() always returns (0, 0) for mouse wheel events, on Windows
                if (me.getID() != MouseEvent.MOUSE_WHEEL) {
                    //System.out.println(me);
                    //System.out.println("Non-Wheel event");
                    prev_xy = me.getLocationOnScreen();
                }
                return ((prev_xy.x - p.x) + " "
                        + (prev_xy.y - p.y));
            }
        }, ROBOTTESTER_INPUT_EVENT_MASK);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                input_log_writer.println("# input logging stopped due to jvm shutdown at " + (new Date()).toString());
                input_log_writer.close();
            }
        });
        return false;
    }
    private static final int BOOT_FRAME_H = 400;
    private static final int BOOT_FRAME_W = 400;
    private static final int BOOT_FRAME_Y = 0;
    private static final int BOOT_FRAME_X = 0;
    public static final long ROBOTTESTER_INPUT_EVENT_MASK = AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK;

    private static void setLAF() {
        try {
            // Set cross-platform Java L&F (also called "Metal")
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
            //MetalLookAndFeel.setCurrentTheme(new DarkTheme());
            UtilG.setUIDefaults();
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            System.out.println("setLAF failed");
        }
    }

    private static CommandLine parseCLI(String[] args) {
        CommandLine ret_val = null;
        Options opts = new Options();
        opts.addOption(C.OPT_DOUBLE_RES, "Double resolution (1280x960)");
        opts.addOption(C.OPT_NAMED_GALAXY, true, "Name of galaxy file");
        opts.addOption(C.OPT_HELP, "Print help");
        opts.addOption(null, C.OPT_ROBOT_TEST, true, "(WARNING: EXPERTS ONLY) Run Robot test with file");
        opts.addOption(null, C.OPT_WAIT_BEFORE_START, false, "Wait for enter before initiliazing");
        opts.addOption(null, C.OPT_ECONOMY_PRINT, false, "Printout economy details at start of turn");
        opts.addOption(null, C.OPT_AUTO_DELAY, true, "Set Robot test auto delay in ms");
        opts.addOption(null, C.OPT_CLEAN_UP_2CLICK, false, "Delay IO and do gc() before 2click");
        opts.addOption(null, C.OPT_GAME_STATE_FILE, true, "Game state record file to check against");
        opts.addOption(null, C.OPT_MAX_DELAY, true, "Maximum event delay (in ms) during Robot testing");
        opts.addOption(null, C.OPT_WIZARD_MODE, false, "Activate wizard mode");
        opts.addOption(null, C.OPT_ROBOT_STOP, true, "Execute Robot test for this number of events then stop "
                + "Robot and leave game as is");
        opts.addOption(null, C.OPT_RANDOM_SEED, true, "Set argument as random seed");
        opts.addOption(null, C.OPT_ENABLE_AI, false, "Enable AI");
        opts.addOption(null, C.OPT_AI_TEST, false, "Do AI test run");
        HelpFormatter formatter = new HelpFormatter();
        DefaultParser parser = new DefaultParser();
        try {
            ret_val = parser.parse(opts, args);
        } catch (ParseException ex) {
            System.out.println("Error parsing arguments: " + ex.getMessage());
            System.exit(0);
        }
        if (ret_val.hasOption(C.OPT_HELP)) {
            formatter.printHelp("java -jar -Xss32m Phoenix.jar\n       phoenix.sh\n       phoenix.bat"
                    + "\n\nLong options are for debugging", opts);
            System.exit(0);
        }
        return ret_val;
    }

    private static void rollLogs(String name) {
        // roll logs
        final int MAX_BACKUP = 5;
        for (int i = MAX_BACKUP; i > 0; i--) {
            File log_file = new File(name + "." + (i - 1));
            if (i == 1) {
                log_file = new File(name);
            }
            File old_log = new File(name + "." + i);
            if (old_log.exists()) {
                old_log.delete();
            }
            if (log_file.exists()) {
                log_file.renameTo(old_log);
            }
        }
    }

    public static void setGui(Gui g) {
        gui = g;
    }

    public static void setRealTime() {
        Phoenix.is_real_time = true;
    }

    static void flushLogMessages() {
        for (String tmp : log_buffer) {
            input_log_writer.println(tmp);
        }
        log_buffer.clear();
    }
}
