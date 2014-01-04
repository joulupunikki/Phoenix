/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package phoenix;

import gui.Gui;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
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

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Util.logEx(t, e);
            }
        });

        Gui.execute(args);// TODO code application logic here
    }
}
