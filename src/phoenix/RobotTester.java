/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phoenix;

import gui.Gui;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import util.C;

/**
 * WARNING: generates OS-level input events. Abnormal termination may lead to
 * non-standard event combinations which may cause OS to become unresponsive to
 * input. It is strongly suggested that these tests are NOT run on mission
 * critical production environments as random input may result. It is best to
 * run the tests on a special test environment (dedicated computer or a virtual
 * machine) where adverse effects from random input have less chance of causing
 * permanent disruption. While a test is running the test is sensitive to input
 * and the test machine should run undisturbed.
 * <p>
 * Thread subclass to generate keyboard/mouse input events to test high level
 * Phoenix functionality. Events are generated from an event log file which is
 * generated during normal Phoenix/EFS operation/play. This class is a
 * singleton, start with startRobotTester().
 *
 * Questionable Implementation Features:
 *
 * Mouse drag with open JMenu is raw, all events are logged and repeated. Thus,
 * with all the processing and Robot.autodelay(), the drag is slooooow. And the
 * log is clogged with drag events. This has to be done cause I could not find a
 * way to detect mouse pointer moving from an open JMenu to another JMenu during
 * a drag.
 *
 * "private synchronized void dispacthedEventD(String event)" is called from
 * event dispatch thread, which probably should not have to wait for user level
 * program execution. This is, however, only as a part of automated robot
 * testing and during non anomalous execution the notifying actions should be
 * completed promptly. Also, having one JVM instance stall is much less serious
 * side effect than the operating system mouse/keyboard input malfunctioning
 * because of non-standard Robot created input events.
 *
 * @author joulupunikki <joulupunikki@gmail.com>
 */
public class RobotTester extends Thread {

    private static final int AUTO_DELAY;

    static {
        AUTO_DELAY = Integer.parseInt(Gui.getMainArgs().getOptionValue(C.OPT_AUTO_DELAY, "40"));
    }
    private static final int START_DELAY = 500;
    private static final int WAIT_DELAY = 200;
    private static final int MAX_CREATE_WAIT = 5000;
    private static final int MAX_XY_DEV = 1;
    private static final int READ_AHEAD = 1;
    private static final int D_CLICK_BUFFER = 300;
    private static final int D_CLICK_THRESHOLD = 400;
    private static final int D_CLICK_DIVISOR = D_CLICK_THRESHOLD / (D_CLICK_THRESHOLD - D_CLICK_BUFFER);
    static final String S_SOURCE_SEP = " #";
    static final String S_SPACE = " ";
    static final int NUMBER_IDX = 0;
    static final int TIME_IDX = 1;
    static final int ID_IDX = 2;
    static final int BUTTON_IDX = 3;
    static final int CLICK_COUNT_IDX = 4;
    static final int X_IDX = 5;
    static final int Y_IDX = 6;
    private static RobotTester robot_tester = null;
    private static volatile boolean wait_for_program = false;
    private static String latest_event = null;
    private static volatile boolean stop = false;
    private Robot robot = null;
    private int dx = -1;
    private int dy = -1;
    private int last_created_event_id = -1;
    private int last_created_event_button = -1;
    private int created_count = 0;
    private long start_time;
    private int last_press_time;
    private int last_release_time;
    private int d_click_buffer;
    private int d_click_max;
    private BufferedReader input_log_buf = null;
    private int[] prev_event = {-1, -1, -1, -1, -1, -1, -1};

    private RobotTester() { // prevent creation of singleton
    }

    /**
     * WARNING: generates OS-level input events. Abnormal termination may lead
     * to non-standard event combinations which may cause OS to become
     * unresponsive to input.
     * <p>
     * Create a RobotTester which reads mouse/keyboard input events from the log
     * file file_name and generates OS level mouse/keyboard input events using a
     * java Robot object. RobotTester is a singleton and this method may be
     * called only once.
     *
     * @param file_name name of log file with test event sequence.
     * @param x coordinate of Gui window.
     * @param y coordinate of Gui window.
     */
    public static void startRobotTester(String file_name, int x, int y) {
        if (robot_tester != null) {
            System.out.println("May not restart RobotTester, it is a singleton.");
            System.exit(1);
        }
        robot_tester = new RobotTester(file_name, x, y);
        robot_tester.start();
    }

    /**
     * WARNING: generates OS-level input events. Abnormal termination may lead
     * to non-standard event combinations which may cause OS to become
     * unresponsive to input.
     * <p>
     * Create a RobotTester which reads mouse/keyboard input events from the log
     * file file_name and generates OS level mouse/keyboard input events using a
     * java Robot object.
     *
     * @param file_name log file name.
     * @param x coordinate of Gui window.
     * @param y coordinate of Gui window.
     */
    private RobotTester(String file_name, int x, int y) {
        this.dx = x;
        this.dy = y;
        latest_event = null;
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

    /**
     * Tell RobotTester to wait (suspend event generation) or continue (resume
     * event generation.) Used when Phoenix may be unresponsive for an arbitrary
     * time, like when saving/loading/restarting.
     *
     * @param state true iff RobotTester must wait.
     */
    public static void setWaitState(boolean state) {
        wait_for_program = state;
    }

    public static boolean isRobotTest() {
        boolean ret_val = false;
        if (robot_tester != null) {
            ret_val = true;
        }
        return ret_val;
    }

    static void dispatchedEvent(String event) {
        if (robot_tester != null) {
            robot_tester.dispacthedEventD(event);
        }
    }

    private synchronized void dispacthedEventD(String event) {
        long start = System.currentTimeMillis();
        while (latest_event != null) {
            System.out.println("EventListener waiting...");
            long time_left = MAX_CREATE_WAIT + start - System.currentTimeMillis();
            if (time_left < 1) {
                break;
            }
            try {
                wait(time_left);
            } catch (InterruptedException ex) {
                // resume wait
            }
        }
        latest_event = event;
        notifyAll();
    }

    private synchronized String consumeEvent() {
        long start = System.currentTimeMillis();
        while (latest_event == null) {
            long time_left = MAX_CREATE_WAIT + start - System.currentTimeMillis();
            if (time_left < 1) {
                break;
            }
            try {
                wait(time_left);
            } catch (InterruptedException ex) {
                // resume wait
            }
        }
        String str = latest_event;
        latest_event = null;
        notifyAll();
        return str;
    }

    /**
     * WARNING: generates OS-level input events. Abnormal termination may lead
     * to non-standard event combinations which may cause OS to become
     * unresponsive to input.
     * <p>
     * Loop an input event log file through line by line, generating OS level
     * input events, in an attempt to automatically recreate a previous
     */
    public void run() {
        setDClickMaxDelay();
        start_time = System.currentTimeMillis();
        robot.setAutoDelay(AUTO_DELAY);
        robot.setAutoWaitForIdle(true);
        // event buffer: to ensure double clicks, read ahead one event
        // to see if a double click is expected, and inform createEvent()
        // if so; otherwise read behind one event (if any) and if previous event
        // was a double click and current event is a single click inform
        // createEvent()
        LinkedList<String> buffer = new LinkedList<>();
        try {
            final int NEXT_EVENT_IDX = 1;
            // states
            final int FILL_BUFFER = 2; // <2 events buffered
            final int BUFFER_FULL = 3; // >=2 events buffered
            final int FINISHING = 4; // null event was read
            //final int FINISHING2 = 5;
            int state = FILL_BUFFER;
            int click_count = 1;
            while (!stop) {
                click_count = 1;
                assert (buffer.size() <= 3); // invariant
                switch (state) {
                    case FILL_BUFFER:
                        buffer.addFirst(input_log_buf.readLine());
                        if (buffer.getFirst() == null) {
                            if (buffer.size() > 1) {
                                state = FINISHING;
                            } else {
                                stop = true;
                            }
                        } else if (buffer.getFirst().startsWith("#")) {
                            buffer.removeFirst();
                        } else if (buffer.size() > READ_AHEAD) {
                            state = BUFFER_FULL;
                        }
                        break;
                    case BUFFER_FULL:
                        String[] tokens = buffer.getFirst().split(S_SPACE, CLICK_COUNT_IDX + 2);
                        if (Integer.parseInt(tokens[CLICK_COUNT_IDX]) > 1) {
                            System.out.println("2Click read ahead");
                            click_count = 2;
                        } else {
                            if (buffer.size() > 2) {
                                tokens = buffer.getLast().split(S_SPACE, CLICK_COUNT_IDX + 2);
                                if (Integer.parseInt(tokens[CLICK_COUNT_IDX]) > 1) {
                                    tokens = buffer.get(1).split(S_SPACE, CLICK_COUNT_IDX + 2);
                                    if (Integer.parseInt(tokens[CLICK_COUNT_IDX]) == 1) {
                                        System.out.println("2Click read behind");
                                        click_count = -1;
                                    }
                                }
                            }
                        }
                        if (buffer.size() > 2) {
                            buffer.removeLast();
                        }
                        createEvent(buffer.get(NEXT_EVENT_IDX), click_count);
                        state = FILL_BUFFER;
                        break;
                    case FINISHING:
                        stop = true;
//                        state = FINISHING2;
                        createEvent(buffer.get(NEXT_EVENT_IDX), click_count);
//                        break;
//                    case FINISHING2:
//                        stop = true;
//                        createEvent(buffer.get(1), click_count);
                        break;
                    default:
                        throw new AssertionError();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        long stop_time = System.currentTimeMillis();
        System.out.println("Current time : " + Date.from(Instant.now()).toString()
                + "\nTest time : " + (stop_time - start_time) + "ms\n"
                + "Mouse/key press/release/wheel events created : " + created_count
        );
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

    private void assertCreated(String str, int[] d, String source) {
        //String[] tokens = str.split(" ", ID_IDX + 1);
        String expected = str;
        String oldest = consumeEvent();
        boolean fail = true;
        if (oldest == null) {
            System.out.println("Event not queued in " + MAX_CREATE_WAIT + "ms. Stopping test.");
        } else if (!compEvents(oldest, d, source)) {
            System.out.println("Error, dispatched event mismatch:\n"
                    + "Dispatched = " + oldest + "\n"
                    + "Expected   = " + expected + "\n"
                    + "Stopping test."
            );
        } else {
            fail = false;
        }

        if (fail) {
            // signal release of a pressed button/key
            switch (last_created_event_id) {
                case MouseEvent.MOUSE_PRESSED:
                    robot.mouseRelease(last_created_event_button);
                    break;
                case KeyEvent.KEY_PRESSED:
                    robot.keyRelease(last_created_event_button);
                    break;
                default:
                    break;
            }
            stop = true;
            return;
        }
        last_created_event_id = -1;
    }

    private boolean compEvents(String oldest, int[] d, String source) {
        boolean success = true;
        String[] tokens_all = oldest.split(S_SOURCE_SEP, 2); // split source name
        String[] tokens = tokens_all[0].split(S_SPACE);
//        int count = tokens.length - 1;
//        if (count == CLICK_COUNT_IDX + 1) {
//            count--; // skip key name
//        }
        int count = tokens.length;
        int[] int_vals = new int[count];
        for (int i = 0; i < count; i++) {
            int_vals[i] = Integer.parseInt(tokens[i]);
        }
        // int values requiring strict match
        if (int_vals[NUMBER_IDX] != d[NUMBER_IDX]
                || int_vals[ID_IDX] != d[ID_IDX]
                || int_vals[BUTTON_IDX] != d[BUTTON_IDX]
                || int_vals[CLICK_COUNT_IDX] != d[CLICK_COUNT_IDX]) {
            System.out.println("ID mismatch.");
            success = false;
        }
        // int values requiring approximate match
        if (d[X_IDX] - MAX_XY_DEV > int_vals[X_IDX]
                || d[X_IDX] + MAX_XY_DEV < int_vals[X_IDX]
                || d[Y_IDX] - MAX_XY_DEV > int_vals[Y_IDX]
                || d[Y_IDX] + MAX_XY_DEV < int_vals[Y_IDX]) {
            System.out.println("Coordinate mismatch.");
            success = false;
        }
        // event source name (+ text if any)
        if (!source.equals(tokens_all[1])) {
            System.out.println("Source mismatch.");
            success = false;
        }
        return success;
    }

    private void setDClickMaxDelay() {
        d_click_max = (Integer) Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
        System.out.println("Double click JVM max threshold: " + d_click_max + "ms");
        if (d_click_max < D_CLICK_THRESHOLD) {
            d_click_buffer = d_click_max / D_CLICK_DIVISOR;
        } else {
            d_click_buffer = d_click_max - D_CLICK_BUFFER;
        }
        d_click_buffer /= 2;
        System.out.println("Double click autodelay max interval: " + d_click_buffer + "ms");
        System.out.println("Robot autodelay: " + AUTO_DELAY + "ms");
    }

    private void setDelay(int time, int id, int double_click, int[] p) {

        // delay between events, - AUTO_DELAY
        int delay = p[TIME_IDX];
        if (delay > -1) { // -1 for first event
            delay = time - delay - AUTO_DELAY;
        } else {
            delay = START_DELAY;
        }
        // ensure multi clicks will be registered as such
        int current_time = (int) (System.currentTimeMillis() - start_time);
        if (double_click > 1) {
            switch (id) {
                case MouseEvent.MOUSE_PRESSED:
                    delay = current_time - last_press_time - AUTO_DELAY;
                    break;
                case MouseEvent.MOUSE_RELEASED:
                    delay = current_time - last_release_time - AUTO_DELAY;
                    break;
                default:
                    // should never arrive here ...
                    throw new AssertionError();
            }
            if (delay > d_click_buffer) {
                delay = 2 * d_click_buffer - delay;
            }
        } else if (double_click == -1) { // ensure not too many multi clicks
            delay = current_time - last_press_time;
            if (delay <= d_click_max) {
                delay = d_click_max + 1;
            }
        }
        // do delay if positive time
        if (delay > 0) {
            System.out.println("Delay(ms): " + delay);
            robot.delay(delay);
        }
    }

    private void createEvent(String str, int double_click) {

        String[] tokens_all = str.split(S_SOURCE_SEP, 2); // split source name
        String[] tokens = tokens_all[0].split(S_SPACE);
        int loop = tokens.length;
        int[] d = new int[loop];
        int[] p = prev_event;
        prev_event = d;
        for (int i = 0; i < loop; i++) {
            d[i] = Integer.parseInt(tokens[i]);
        }
        int number = d[NUMBER_IDX];
        int time = d[TIME_IDX];
        int id = d[ID_IDX];
        int button = d[BUTTON_IDX];
        int clicks = d[CLICK_COUNT_IDX];
        int x = d[X_IDX] + dx;
        int y = d[Y_IDX] + dy;
        // determine button for Robot
        if (id == MouseEvent.MOUSE_PRESSED || id == MouseEvent.MOUSE_RELEASED) {
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
                    break;
            }
        }
        setDelay(time, id, double_click, p);
        // implicit mouse motion
        if (id != MouseEvent.MOUSE_DRAGGED && id != MouseEvent.MOUSE_MOVED
                && (x != p[X_IDX] || y != p[Y_IDX])) {
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
        created_count++; // -- in switch default
        switch (id) {
            case MouseEvent.MOUSE_DRAGGED:
                System.out.println("#C " + created_count + " MouseDrag: " + x + "," + y);
                robot.mouseMove(x, y);
                break;
            case MouseEvent.MOUSE_MOVED:
                System.out.println("#C " + created_count + " MouseMove: " + x + "," + y);
                robot.mouseMove(x, y);
                break;
            case MouseEvent.MOUSE_WHEEL:
                System.out.println("#C " + created_count + " MouseWheel: " + button);
                robot.mouseWheel(button);
                break;
            case MouseEvent.MOUSE_PRESSED:
                System.out.println("#C " + created_count + " MousePress: " + button);
                //to make sure possible double clicks will be reconstructed properly
                last_press_time = (int) ((System.currentTimeMillis() - start_time));
                //in case of abnormal termination
                last_created_event_id = id;
                last_created_event_button = button;
                robot.mousePress(button);
                break;
            case MouseEvent.MOUSE_RELEASED:
                System.out.println("#C " + created_count + " MouseRelease: " + button);
                // to make sure possible double clicks will be reconstructed properly
                last_release_time = (int) ((System.currentTimeMillis() - start_time));
                robot.mouseRelease(button);
                break;
            case KeyEvent.KEY_PRESSED:
                System.out.println("#C " + created_count + " KeyPress: " + button);
                //in case of abnormal termination
                last_created_event_id = id;
                last_created_event_button = button;
                robot.keyPress(button);
                break;
            case KeyEvent.KEY_RELEASED:
                System.out.println("#C" + created_count + " KeyRelease: " + button);
                robot.keyRelease(button);
                break;
            default:
                created_count--;
                break;
        }
        assertCreated(str, d, tokens_all[1]);
    }
}
