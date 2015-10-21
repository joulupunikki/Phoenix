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
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import org.apache.commons.io.FileUtils;
import util.C;
import util.FN;
import util.Util;

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
 * <p>
 * Notes on running:
 * <p>
 * As the events created by the RobotTester will not have identical timing with
 * the originals, and OS-level/JVM interference will cause the game to have
 * slightly different response times between executions with identical inputs,
 * the tests have an unavoidable statistical characteristic: a test may fail
 * from time to time with no apparent reason. False positives, however, should
 * have astronomically low chance of occurring. Example: a timing difference may
 * cause a double click to register as two single clicks and depending on the
 * game state the test may fail right there (note however that this specific
 * thing has been mitigated by setting the programmed inter-event delay between
 * double click mouse events to well below the JVM double click threshold.)
 * <p>
 * (I had consistent problems with this, but it has completely disappeared as of
 * late, so ultimately I do not know what the issue was, something transient ?)
 * <strike>If, on the test machine, some object has the focus when a Robot test
 * is started then the first mouse click will be consumed by giving focus to the
 * Phoenix window. Currently, this is not considered in RobotTester code and the
 * Robot test will most likely report a failure on the first event in such
 * cases. PROPOSED FIX: insert a dummy click on Phoenix window as first event in
 * every Robot test, or otherwise ensure that Phoenix window has focus.</strike>
 * <p>
 * Questionable Implementation Features:
 * <p>
 * Mouse drag with open JMenu is raw, all events are logged and repeated. Thus,
 * with all the processing and Robot.autodelay(), the drag is slooooow. And the
 * log is clogged with drag events. This has to be done cause I could not find a
 * way to detect mouse pointer moving from an open JMenu to another JMenu during
 * a drag.
 * <p>
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
    private static final int MAX_DELAY;

    static {
        AUTO_DELAY = Integer.parseInt(Gui.getMainArgs().getOptionValue(C.OPT_AUTO_DELAY, "40"));
        MAX_DELAY = Integer.parseInt(Gui.getMainArgs().getOptionValue(C.OPT_MAX_DELAY, "1000000"));
    }
    private static final int START_DELAY = 500;
    private static final int WAIT_DELAY = 200;
    private static final int MAX_CREATE_WAIT = 5000;
    private static final int MAX_XY_DEV = 1;
    private static final int READ_AHEAD = 2;
    private static final int D_CLICK_BUFFER = 300;
    private static final int D_CLICK_THRESHOLD = 400;
    private static final int D_CLICK_DIVISOR = D_CLICK_THRESHOLD / (D_CLICK_THRESHOLD - D_CLICK_BUFFER);
    static final String RANDOM_SEED_PREFIX = "# random seed=";
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
    private static volatile boolean delayed_logging = false;

    /**
     * @return the random_seed
     */
    public static long getRandomSeed() {
        return random_seed;
    }
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
    private File state_file = null;
    private LinkedList<String> log_buffer = new LinkedList<>();
    private int[] prev_event = {-1, -1, -1, -1, -1, -1, -1};
    private int halt_on_event_nr = 0;
    private Gui gui;
    private static long random_seed;

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
    public static void startRobotTester(String file_name, String state_file, Gui gui, int x, int y) {
        if (robot_tester != null) {
            System.out.println("May not restart RobotTester, it is a singleton.");
            System.exit(1);
        }
        robot_tester = new RobotTester(file_name, state_file, gui, x, y);
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
    private RobotTester(String file_name, String state_file, Gui gui, int x, int y) {
        this.dx = x;
        this.dy = y;
        this.gui = gui;
        latest_event = null;
        if (!init(file_name, state_file)) {
            System.exit(1);
        }
//        Path input_log_file = FileSystems.getDefault().getPath(file_name);
//        this.state_file = FileUtils.getFile(state_file);
//        this.state_file.exists();
//        boolean fail = true;
//        try {
//            this.robot = new Robot();
//            input_log_buf = Files.newBufferedReader(input_log_file);
//            fail = false;
//        } catch (NoSuchFileException ex) {
//            System.out.println("Input event log file \"" + file_name + "\" not found.");
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        } catch (AWTException ex) {
//            System.out.println(""
//                    + "AWTException creating class Robot instance. Some platforms like X-Windows\n"
//                    + "require special settings enabled to create Robots. Exiting.");
//        } finally {
//            if (fail) {
//                System.exit(1);
//            }
//        }
    }

    /**
     * Try to init RobotTester: determine state file if any, create Robot, open
     * input log file; if successful return true, else print cause of failure
     * and return false;
     *
     * @param file_name
     * @param state_file
     * @return
     */
    private boolean init(String file_name, String state_file) {
        if (Gui.getMainArgs().hasOption(C.OPT_ROBOT_STOP)) {
            halt_on_event_nr = Integer.parseInt(Gui.getMainArgs().getOptionValue(C.OPT_ROBOT_STOP));
        }
        if (state_file != null) {
            this.state_file = FileUtils.getFile(state_file);
            if (!this.state_file.exists()) {
                System.out.println("Game state record file \"" + state_file + "\" not found.");
                return false;
            }
        }
        Path input_log_file = FileSystems.getDefault().getPath(file_name);
        boolean success = false;
        try {
            this.robot = new Robot();
            input_log_buf = Files.newBufferedReader(input_log_file);

            success = true;
        } catch (NoSuchFileException ex) {
            System.out.println("Input event log file \"" + file_name + "\" not found.");
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (AWTException ex) {
            System.out.println(""
                    + "AWTException creating class Robot instance. Some platforms like X-Windows\n"
                    + "require special settings enabled to create Robots. Exiting.");
        }
        return success;
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

    private void flushLogMessages() {
        for (String tmp : log_buffer) {
            System.out.println(tmp);
        }
        log_buffer.clear();
    }

    static boolean isDelayedLogging() {
        return delayed_logging;
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
    @Override
    public void run() {
        setDClickMaxDelay();
        start_time = System.currentTimeMillis();
        boolean all_events_replayed = false;
        boolean game_end_states_match = false;
        boolean test_completed_succesfully = false;
        robot.setAutoDelay(AUTO_DELAY);
        robot.setAutoWaitForIdle(true);
        // event buffer: to ensure double clicks, read ahead one event
        // to see if a double click is expected, and inform createEvent()
        // if so; otherwise read behind one event (if any) and if previous event
        // was a double click and current event is a single click inform
        // createEvent()
        LinkedList<String> buffer = new LinkedList<>();
        try {
            final int NEXT_EVENT_IDX = 2;
            final int MIN_BUFFER = READ_AHEAD + 1;
            // states
            final int FILL_BUFFER = 2; // <3 events buffered
            final int BUFFER_FULL = 3; // >=3 events buffered
            final int FINISHING = 4; // null event was read
            final int FINISHING2 = 5;
            int state = FILL_BUFFER;
            int click_count = 1;
            while (!stop) {
                click_count = 1;
                assert (buffer.size() <= 4); // invariant
                switch (state) {
                    case FILL_BUFFER:
                        buffer.addFirst(input_log_buf.readLine());
                        if (buffer.getFirst() == null) {
                            if (buffer.size() > 2) {
                                state = FINISHING;
                            } else if (buffer.size() > 1) {
                                state = FINISHING2;
                            } else {
                                stop = true;
                            }
                        } else if (buffer.getFirst().startsWith("#")) {
                            String tmp = buffer.removeFirst();
                            if (tmp.startsWith(RANDOM_SEED_PREFIX)) {
                                random_seed = Long.parseLong(tmp.replaceFirst(RANDOM_SEED_PREFIX, ""));
                                System.out.println(RANDOM_SEED_PREFIX + random_seed);
                                gui.getGame().getRandom().setSeed(random_seed);
                            }
                        } else if (buffer.size() > READ_AHEAD) {
                            state = BUFFER_FULL;
                        }
                        break;
                    case BUFFER_FULL:
                        String[] tokens = buffer.get(1).split(S_SPACE, CLICK_COUNT_IDX + 2);
                        if (Integer.parseInt(tokens[CLICK_COUNT_IDX]) > 1) {
                            System.out.println("2Click read ahead 1");
                            click_count = 2;
                        } else {
                            tokens = buffer.getFirst().split(S_SPACE, CLICK_COUNT_IDX + 2);
                            if (Gui.getMainArgs().hasOption(C.OPT_CLEAN_UP_2CLICK)
                                    && Integer.parseInt(tokens[CLICK_COUNT_IDX]) > 1) {
                                System.out.println("2Click read ahead 2");
                                click_count = -2;

                            } else if (buffer.size() > MIN_BUFFER) {
                                tokens = buffer.getLast().split(S_SPACE, CLICK_COUNT_IDX + 2);
                                if (Integer.parseInt(tokens[CLICK_COUNT_IDX]) > 1) {
                                    tokens = buffer.get(NEXT_EVENT_IDX).split(S_SPACE, CLICK_COUNT_IDX + 2);
                                    if (Integer.parseInt(tokens[CLICK_COUNT_IDX]) == 1) {
                                        System.out.println("2Click read behind");
                                        click_count = -1;
                                    }
                                }
                            }
                        }
                        if (buffer.size() > MIN_BUFFER) {
                            buffer.removeLast();
                        }
                        createEvent(buffer.get(NEXT_EVENT_IDX), click_count);
                        state = FILL_BUFFER;
                        break;
                    case FINISHING:
                        //stop = true;
                        state = FINISHING2;
                        createEvent(buffer.get(NEXT_EVENT_IDX), click_count);
                        break;
                    case FINISHING2:
                        // FIXME ? without AutoWaitForIdle set to false here
                        // the last "robot.mouseRelease(button);" call in
                        // "robottester.createEvent()" may hang resulting in a
                        // otherwise succesfull test failing
                        robot.setAutoWaitForIdle(false);
                        stop = true;
                        createEvent(buffer.get(1), click_count);
                        test_completed_succesfully = true;
                        break;
                    default:
                        throw new AssertionError();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (halt_on_event_nr > 0) {
            System.out.println("Halting RobotTester on event number " + halt_on_event_nr);
            System.out.println("WARNING: os/jvm mouse/keyboard input may be left in unresponsive state.");
            robot_tester = null;
            return;
        }
        flushLogMessages();
        Phoenix.flushLogMessages();
        long stop_time = System.currentTimeMillis();
        System.out.println("Current time : " + Date.from(Instant.now()).toString()
                + "\nTest time : " + (stop_time - start_time) + "ms\n"
                + "Mouse/key press/release/wheel events created : " + created_count
        );
        if (this.state_file != null) {
            try {
                long check_time = System.currentTimeMillis();
                System.out.println("State file: " + this.state_file.getName());
                Util.recordState(FN.S_GAME_STATE_RECORD_FILE, gui.getGame());
                long expect = FileUtils.checksumCRC32(this.state_file);
                long result = FileUtils.checksumCRC32(FileUtils.getFile(FN.S_GAME_STATE_RECORD_FILE));
                System.out.println("Expected end state  CRC32 = " + expect);
                System.out.println("Resultant end state CRC32 = " + result);
                System.out.println("Check time : " + (System.currentTimeMillis() - check_time));
                if (expect != result) {
                    test_completed_succesfully = false;
                    System.out.println("------ Game end states do not match !!! ----");
                }
            } catch (IOException ex) {
                test_completed_succesfully = false;
                System.out.println("IOException when checking Game end states");
            }
        }
        int ret_val = 0;
        if (test_completed_succesfully) {
            System.out.println("+++++++++++++++++ Robot Test success +++++++++++++++++++++++++++++++++");
        } else {
            System.out.println("----------------- Robot Test failure !!! -----------------------------");
            ret_val = 1;
        }
        System.out.println("RobotTester finished. You may resume input on test machine.");
        System.exit(ret_val);
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
        if (halt_on_event_nr == Math.abs(d[NUMBER_IDX])) {
            stop = true;
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
        if (Math.abs(int_vals[NUMBER_IDX]) != Math.abs(d[NUMBER_IDX])
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

    private void delayedPrint(String msg) {
        if (delayed_logging) {
            log_buffer.addLast(msg);
        } else {
            System.out.println(msg);
        }
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

    private void setDelay(int time, int id, boolean real_time, int double_click, int[] p) {

        // delay between events, - AUTO_DELAY
        int delay = p[TIME_IDX];
        if (delay > -1) { // -1 for first event
            delay = time - delay - AUTO_DELAY;
        } else {
            delay = START_DELAY;
        }
        // ensure multi clicks will be registered as such
        int tmp = delay;
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
        } else if (double_click < 0) { // ensure not too many multi clicks
            RobotTester.delayed_logging = false;
            flushLogMessages();
            if (double_click == -2) { // prepare for imminent double click
                // so that future doubleclick event sequence will not be
                // stretched in time by a sudden gc() or non-essential IO
                // activity
                System.out.println("Cleanup for 2Click.");
                System.gc();
                RobotTester.delayed_logging = true;
                current_time = (int) (System.currentTimeMillis() - start_time);
            }
            delay = current_time - last_press_time;
            if (delay <= d_click_max) {
                delay = d_click_max + 1;
            }
        }
        // do delay if positive time
        if (delay > 0) {
            // for real time
            if (real_time && id != MouseEvent.MOUSE_RELEASED) {
                System.out.println("Real time delay");
                delay = tmp;
            } else if (delay > MAX_DELAY) {
                delay = MAX_DELAY;
            }
            delayedPrint("Delay(ms): " + delay);
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
        setDelay(time, id, (number < 0), double_click, p);
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
                delayedPrint("#C " + created_count + " MousePress: " + button);
                //to make sure possible double clicks will be reconstructed properly
                last_press_time = (int) ((System.currentTimeMillis() - start_time));
                //in case of abnormal termination
                last_created_event_id = id;
                last_created_event_button = button;
                robot.mousePress(button);
                break;
            case MouseEvent.MOUSE_RELEASED:
                delayedPrint("#C " + created_count + " MouseRelease: " + button);
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
