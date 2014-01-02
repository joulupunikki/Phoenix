/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package phoenix;

import gui.Gui;
import java.io.FileWriter;
import java.io.PrintWriter;
import util.C;

/**
 *
 * @author joulupunikki
 */
public class Phoenix {



    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                logEx(t, e);
            }
        });

        Gui.execute(args);// TODO code application logic here
    }

    public static void logEx(Thread t, Throwable e) {

        try (
            FileWriter log_stream = new FileWriter(C.S_LOG_FILE, true);
            PrintWriter log = new PrintWriter(log_stream)){
            log.println("***** Begin Stack Trace *****");
            if (t != null) {
                log.println(t.toString());
                System.out.println(t.toString());
            }
            e.printStackTrace(log);
            log.println("***** End Stack Trace *****");
            log.flush();           
            e.printStackTrace(System.out);
        } catch (Exception ex) {
        }

    }
}
