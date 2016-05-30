/*
 * Copyright (C) 2016 joulupunikki joulupunikki@gmail.communist.invalid.
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
package util;

import gui.Gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import phoenix.Phoenix;

/**
 * When a System.exit(x) with x != 0 condition would occur this JFrame will be
 * shown before exit.
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class CrashReporter {

    private static boolean frame_up = false;
    private static String trace_string = "";
    private static final JTextArea text_area = new JTextArea();
    private static JFrame frame = null;
    public static void showCrashReport(Throwable e) {
        removeEventListeners();
        addToReport(e, text_area);
        if (!frame_up) {
            frame_up = true;
            Gui.disposeGUI();
            createAndShowReport(e);

        }
    }

    public static void removeEventListeners() {
        AWTEventListener[] listeners = Toolkit.getDefaultToolkit().getAWTEventListeners(Phoenix.ROBOTTESTER_INPUT_EVENT_MASK);
        for (AWTEventListener listener : listeners) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
            System.out.println(" listener " + listener.toString() + " removed.");
        }
    }

    private static void createAndShowReport(Throwable e) {
        //Create and set up the window.
        frame = new JFrame("Phoenix has terminated abnormally.");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Create a text area.
        e.printStackTrace();
        text_area.setFont(new Font("Serif", Font.ITALIC, 12));
        text_area.setLineWrap(true);
        text_area.setWrapStyleWord(true);
        JScrollPane scroll_pane = new JScrollPane(text_area);
        scroll_pane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll_pane.setPreferredSize(new Dimension(800, 480));
        scroll_pane.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Stack trace"),
                                BorderFactory.createEmptyBorder(5, 5, 5, 5)),
                        scroll_pane.getBorder()));
        frame.getContentPane().add(scroll_pane, BorderLayout.CENTER);
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    private static void addToReport(Throwable e, JTextArea text_area) {
        if (frame_up) {
            trace_string += "\n\n\n";
        }
        trace_string += e.toString();
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            trace_string += "\nat " + stackTraceElement.toString();
        }
        text_area.setText(trace_string);
    }

}
