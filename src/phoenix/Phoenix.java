/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package phoenix;

import gui.Gui;
import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.io.File;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import util.C;
import util.FN;
import util.Util;

/**
 *
 * @author joulupunikki
 */
public class Phoenix {

    /**
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
        // log mouse events
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            public void eventDispatched(AWTEvent event) {
                System.out.println(System.currentTimeMillis() + " eventDispatched: " + event);
            }
        }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
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
