/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package phoenix;

import gui.Gui;
import java.io.File;
import util.C;
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

        File log_file = new File(C.S_LOG_FILE);
        File old_log = new File(C.S_LOG_FILE + ".1");
        if (old_log.exists()) {
            old_log.delete();
        }
        if (log_file.exists()) {
            log_file.renameTo(old_log);
        }
        
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Util.logEx(t, e);
            }
        });

        Gui.execute(args);// TODO code application logic here
    }
}
