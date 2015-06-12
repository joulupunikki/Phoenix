/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phoenix;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * WARNING: generates OS-level input events. Abnormal termination may lead to 
 * non-standard event combinations which may cause OS to become unresponsive to
 * input. Also requires special privileges on some platforms to generate OS-level
 * events.
  * 
 * Thread subclass to generate keyboard/mouse input events to test high level
 * Phoenix functionality.
 * 
 * @author doa
 */
public class RobotTester extends Thread {

    private static final int AUTO_DELAY = 40;
    private static final int START_DELAY = 500;
    private static final int WAIT_DELAY = 200;
    private static volatile boolean wait_for_program = false;
    Robot robot = null;
    BufferedReader input_log_buf = null;
    int[] prev_event = {-1, -1, -1, -1, -1};

    public RobotTester(String file_name) {
        Path input_log_file = FileSystems.getDefault().getPath(file_name);
        boolean fail = true;
        try {
            this.robot = new Robot();
            input_log_buf = Files.newBufferedReader(input_log_file);
            fail = false;
        } catch (NoSuchFileException ex) {
            System.out.println("Input event log file \"" + file_name + "\" not found.");
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (AWTException ex) {
            System.out.println(""
                    + "AWTException creating class Robot instance. Some platforms like X-Windows\n"
                    + "require special settings enabled to create Robots. Exiting.");
        } finally {
            if (fail) {
                System.exit(1);
            }
        }
    }

    public static void setWaitState(boolean state) {
        wait_for_program = state;
    }

    public void run() {

        robot.setAutoDelay(AUTO_DELAY);
        robot.setAutoWaitForIdle(true);
        String str = null;
        try {
            for (str = input_log_buf.readLine(); str != null; str = input_log_buf.readLine()) {
                if (!str.startsWith("#")) {
                    createEvent(str);
                } else {
                    //System.out.println(str);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("RobotTester finished. You may resume input.");
        System.exit(0);
    }

    private void waitForProgram() {
        /*
         We may have situations when wait is temporarily disabled, but no input
         should be accepted, like the following sequence:
         Gui.saveGame();
         Gui.toMainMenu();
         so we set up extra wait to pass over those
         */
        boolean extra_wait = false;
        if (RobotTester.wait_for_program) {
            System.out.println("Main program busy, waiting ...");
        }
        while (RobotTester.wait_for_program || extra_wait) {
            extra_wait = false;
            if (RobotTester.wait_for_program) {
                extra_wait = true;
            }
            try {
                Thread.sleep(WAIT_DELAY);
            } catch (InterruptedException ex) {
            }
        }
    }

    private void createEvent(String str) {
        String[] tokens = str.split("\\s");
        int[] d = new int[tokens.length];
        int[] p = prev_event;
        prev_event = d;
        d[0] = (int) (Long.parseLong(tokens[0]) % Integer.MAX_VALUE);
        int loop = tokens.length;
        if (loop == 4) {
            loop--; // skip key symbol
        }
        for (int i = 1; i < loop; i++) {
            d[i] = Integer.parseInt(tokens[i]);
        }
        int time = d[0];
        int id = d[1];
        int button = d[2];
        int x = -1;
        int y = -1;
        if (tokens.length == 5) { // mouse event
            x = d[3];
            y = d[4];

            // determine button
            switch (button) {
                case MouseEvent.BUTTON1:
                    button = InputEvent.BUTTON1_DOWN_MASK;
                    break;
                case MouseEvent.BUTTON2:
                    button = InputEvent.BUTTON2_DOWN_MASK;
                    break;
                case MouseEvent.BUTTON3:
                    button = InputEvent.BUTTON3_DOWN_MASK;
                    break;
                default:
                    /*
                     just button presses for now
                     */
                    return;
            }
        }
        // delay between events, - AUTO_DELAY
        int delay = p[0];
        if (delay > -1) {
            delay = time - delay - AUTO_DELAY;
        } else {
            delay = START_DELAY;
        }
        if (delay > 0) {
            System.out.println("Delay(ms): " + delay);
            robot.delay(delay);
        }
        // move mouse if necessary
        if (tokens.length == 5 && (x != p[3] || y != p[4])) {
            System.out.println("Move: " + x + "," + y);
            robot.mouseMove(x, y);
        }
        // button/key/wheel by event ID
        switch (id) {
            /*
             Do not wait for releases, this could lead to problems
             */
            case MouseEvent.MOUSE_RELEASED:
            case KeyEvent.KEY_RELEASED:
                break;
            default:
                waitForProgram();
        }
        switch (id) {
            case MouseEvent.MOUSE_WHEEL:
                System.out.println("MouseWheel: " + button);
                robot.mouseWheel(button);
                break;
            case MouseEvent.MOUSE_PRESSED:
                System.out.println("MousePress: " + button);
                robot.mousePress(button);
                break;
            case MouseEvent.MOUSE_RELEASED:
                System.out.println("MouseRelease: " + button);
                robot.mouseRelease(button);
                break;
            case KeyEvent.KEY_PRESSED:
                System.out.println("KeyPress: " + button);
                robot.keyPress(button);
                break;
            case KeyEvent.KEY_RELEASED:
                System.out.println("KeyRelease: " + button);
                robot.keyRelease(button);
                break;
            default:
        }
    }
}
