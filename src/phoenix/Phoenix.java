/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package phoenix;

import gui.Gui;
import java.awt.AWTEvent;
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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import util.C;
import util.FN;
import util.Util;

/**
 * Main entry point of Phoenix, clone/remake/patch/replacement of the EFS.EXE
 * in the game Emperor of the Fading Suns.
 * 
 * @author joulupunikki
 */
public class Phoenix {

    /**
     * Main entry point. Parse commandline options, roll logs, log all uncaught
     * Throwables, log input events, start GUI.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // parse options
        CommandLine cli_opts = parseCLI(args);
        // roll logs
        File log_file = new File(FN.S_LOG_FILE);
        File old_log = new File(FN.S_LOG_FILE + ".1");
        if (old_log.exists()) {
            old_log.delete();
        }
        if (log_file.exists()) {
            log_file.renameTo(old_log);
        }
        // log all errors and exceptions
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Util.logEx(t, e);
            }
        });
        // log input events
        String file_name = "input.log";
        Path input_log_file = FileSystems.getDefault().getPath(file_name);
        BufferedWriter event_log_buf = null;
        try {
            event_log_buf = Files.newBufferedWriter(input_log_file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException ex) {
            System.out.println("Unable to open input event log file \"" + file_name + "\"");
            System.exit(1);
        }
        final PrintWriter input_log_writer = new PrintWriter(event_log_buf, true);
        input_log_writer.println("# input logging started at " + (new Date()).toString());
        input_log_writer.println("# fields (mouse event): time(ms) eventID button/wheel screenX screenY");
        input_log_writer.println("# fields (key event): time(ms) eventID keycode keychar");
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            public void eventDispatched(AWTEvent event) {
                if (event instanceof MouseWheelEvent) { // check this first since ME is super of MWE
                    MouseWheelEvent me = (MouseWheelEvent) event;
                    input_log_writer.println(System.currentTimeMillis() + " " + me.getID() + " " + me.getWheelRotation() + " " + me.getXOnScreen() + " " + me.getYOnScreen());
                } else if (event instanceof MouseEvent) {
                    MouseEvent me = (MouseEvent) event;
                    input_log_writer.println(System.currentTimeMillis() + " " + me.getID() + " " + me.getButton() + " " + me.getXOnScreen() + " " + me.getYOnScreen());
                } else if (event instanceof KeyEvent) {
                    KeyEvent ke = (KeyEvent) event;
                    String key_char = "" + ke.getKeyChar();
                    if (ke.getExtendedKeyCode() == KeyEvent.VK_ENTER) {
                        key_char = "<enter>";
                    }
                    input_log_writer.println(System.currentTimeMillis() + " " + ke.getID() + " " + ke.getExtendedKeyCode() + " " + key_char);
                }
                //input_log_writer.println(event.toString());
            }
        }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                input_log_writer.println("# input logging stopped due to jvm shutdown at " + (new Date()).toString());
                input_log_writer.close();
            }
        });
        // start GUI
        Gui.execute(cli_opts);
    }
    
    private static CommandLine parseCLI(String[] args) {
        CommandLine ret_val = null;
        Options opts = new Options();
        opts.addOption(C.OPT_DOUBLE_RES, "Double resolution (1280x960)");
        opts.addOption(C.OPT_NAMED_GALAXY, true, "Name of galaxy file");
        opts.addOption(C.OPT_HELP, "Print help");
        HelpFormatter formatter = new HelpFormatter(); 
        DefaultParser parser = new DefaultParser();
        try {
            ret_val = parser.parse(opts, args);
        } catch (ParseException ex) {
            System.out.println("Error parsing arguments: " + ex.getMessage());
            System.exit(0);
        }
        if (ret_val.hasOption(C.OPT_HELP)) {
            formatter.printHelp("java -jar -Xss32m Phoenix\n       phoenix.sh\n       phoenix.bat", opts);
            System.exit(0);
        }
        return ret_val;
    }
}
