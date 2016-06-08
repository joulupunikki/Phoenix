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
package gui;

import dat.UnitType;
import galaxyreader.Structure;
import galaxyreader.Unit;
import game.Game;
import game.Message;
import game.PBEM;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import org.apache.commons.cli.CommandLine;
import phoenix.Phoenix;
import phoenix.RobotTester;
import state.MM1;
import state.PW;
import state.SU;
import state.State;
import static state.State.saveMainGameState;
import state.WS;
import state.Wiz;
import util.C;
import util.Comp;
import util.FN;
import util.G;
import util.StackIterator;
import util.Util;
import util.UtilG;
import util.WindowSize;

/**
 * Root GUI object.
 *
 * @author joulupunikki
 */
public class Gui extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_WINDOW_WIDTH = 640;
    private static final int DEFAULT_WINDOW_HEIGHT = 480;
    private static Gui gui = null;
    private static CommandLine args;

    public static int[][] getUnitIconsDark() {
        return unit_icons_dark;
    }
    //holds the space map
    private SpaceMap space_map;
    //holds the planet map
    private PlanetMap planet_map;
    //holds the planet map background and components
    private PlanetWindow planet_window;
    private JButton launch_button;
    private JButton space_button;
    private ButtonIcon launch_button_enabled;
    private ButtonIcon launch_button_disabled;
    private GlobeWindow globe_window;
    private GalaxyWindow galaxy_window;
    //holds the starmap background and components
    private SpaceWindow space_window;
    private GalacticMap galactic_map;           // gal minimap on space window
    private GalacticMap galactic_map_cw;        // gal minimap on combat window
    private GalacticMap galactic_map_uiw;        // gal minimap on unit info window
    private GalacticMap galactic_map_ciw;        // gal minimap on city info window

    private GlobeMap globe_map;                 // global minimap on planet window
    private GlobeMap globe_map_cw;              // global minimap on combat window
    private GlobeMap globe_map_uiw;              // global minimap on unit info window
    private GlobeMap globe_map_ciw;              // global minimap on city info window

    //holds the unit info window/stack window
    private UnitInfoWindow unit_info_window;
    private MainMenu main_menu;
    private MainMenu.W1 main_menu1;
    private CombatWindow combat_window;
    private AgoraWindow agora_window;
    private HouseWindow house_window;
    private DiplomacyWindow diplomacy_window;
    private BuildPanel build_panel;
    private AgoraAutobuyPanel agora_autobuy_panel;
    private CityInfoWindow city_info_window;
    private JDialog build_window;
    // panel showing research options
    private TechPanel tech_panel;
    // window holding TechPanel
    private JDialog tech_window;
    private TechDBPanel tech_db_panel; // panel showing tech database
    private JDialog tech_db_window;   // window holding tech database
    private Manowitz manowitz_panel;
    private JDialog manowitz_window;
    // resource info panel
    private ResourcePanel resource_panel;
    private OptionsPanel options_panel;
    private JDialog resource_window;
    // for reading messages
    private Messages messages_window;
    // build city panel
    private BuildCityPanel build_city_panel;
    private JDialog build_city_window;
    private DiplomacySelectorPanel diplomacy_selector;
    private CombatStrategyPanel strategy_selector;
    private ResolveContract resolve_contract_dialog;
    private XPlayerScreen x_player_screen;
    private PBEMGui pbem_gui;
    private ByzantiumIIWindow byzantium_ii_window;
    //holds the planet map display and star map display and unit info window in a CardLayout    
    private JPanel main_windows;
    //menubar
    private JPanel menubar_holder;
    private JMenuBar menubar;
    private JMenuBar no_menubar;
    private JMenu file_menu; //file menu
    private JMenuItem menu_exit;
    private JMenuItem menu_load;
    private JMenuItem menu_save;
    private JMenuItem menu_restart;
    private JMenuItem menu_save_as;
    private JMenuItem menu_options;
    private JMenu orders_menu; //orders menu
    private JMenuItem menu_build;
    private JMenuItem menu_research;
    private JMenuItem menu_build_city;
    private JMenuItem menu_build_road;
    private JMenuItem menu_raze_city;

    private JPopupMenu stack_menu;
    private JMenu messages_menu; // messages menu
    private JMenuItem menu_send_message;
    private JMenuItem menu_read_messages;
    private JMenuItem menu_read_contracts;
    private JMenu archives_menu; //archives menu
    private JMenuItem menu_vol1;
    private JMenuItem menu_vol2;
    private JMenuItem menu_vol3;
    private JMenuItem menu_vol4;
    private JMenuItem menu_vol5;
    private JMenu wizard_menu; //wizard mode menu
    private JMenuItem menu_all_tech;
    private JMenuItem menu_all_resources;
    private JMenuItem menu_create_unit;
    private JMenuItem menu_randomize_rng;
    private JMenuItem menu_do_ai_turn;
    private JMenuItem toggle_ai;
    private JMenuItem menu_omniscience;
    private JMenuItem house_menu; // house menu
    private JMenuItem diplomacy_menu; // diplomacy_selector menu
    private JMenuItem byzantium_ii_menu; // bydantium II menu
    private Resource resources;
    //stack display window
    private JDialog stack_window;
    private static WindowSize ws;
    //reference to Game object
    private Game game;
//    private static Game game_s;
    //efs 256-color pallette
    private byte[][] pallette;
    private static IndexColorModel color_index;
    private static int[][] unit_icons;
    private static int[][] unit_icons_dark;
    private static int[][][] hex_tiles;
    private static int[][][] structures;
    private Timer stack_move_timer;
    private boolean stop_stack = false;
    private boolean stack_moving = false;
    private boolean animation_blink = true;
    private int stack_move_counter;
    private State state;
    private int color_cycle_count;
    private Color[] color_cycle_colors;
    private Color color_cycle_color;
    private Unit drag_unit;
    private Point drag_point;
    private Unit info_unit;

    private boolean loadsave_win_up;
    private JDialog loadsave_dialog;
    private CityDialog city_dialog;
    private CargoPanel.Win cargo_win;
    private boolean load_succesfull; // true iff load game ok
    private JMenuItem menu_group_finder;
    private JMenuItem menu_city_info;



    public Gui() throws HeadlessException {
        //this.setUndecorated(true); // Ubuntu unity, see https://github.com/joulupunikki/Phoenix/issues/51
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        CommandLine args = Gui.args;
        pallette = Util.loadPallette(FN.S_EFS_PAL);
        color_index = loadICM();
        unit_icons = Util.loadSquares(FN.S_EFSUNIT_BIN, 92, 32 * 32);
        // set resolution, needed when building Gui elements.
        if (args.hasOption(C.OPT_DOUBLE_RES)) {
            ws = new WindowSize(true);
        } else {
            ws = new WindowSize(false);
        }
        resources = new Resource(this);
        unit_icons_dark = UtilG.makeDarkUnitIcons(resources.getColorScaler(), unit_icons);
        // load galaxy
        Phoenix.addBootMsg("\nLoading galaxy file ...");
        String galaxy_file_name = FN.S_GALAXY_GAL;
        if (args.hasOption(C.OPT_NAMED_GALAXY)) {
            galaxy_file_name = args.getOptionValue(C.OPT_NAMED_GALAXY);
        }
        Util.foundOrExit(galaxy_file_name);
        // create game object
        game = new Game(galaxy_file_name, 14);
        Phoenix.addBootMsg(" done.\nInitializing GUI ...");
        game.init(resources);
        // set fonts after WindowSize has been initialized
        UIManager.put("OptionPane.messageFont", ws.font_large);
        UIManager.put("Button.font", ws.font_large);
        UIManager.put("Label.font", ws.font_large);
        UIManager.put("TextField.font", ws.font_large);
        UtilG.setUpUtilG(this);
        // set up PBEM
        pbem_gui = new PBEMGui(game);
        pbem_gui.getDATAHashes();
        this.setSize(ws.main_window_width, ws.main_window_height);
        loadHexTiles();
        loadStructureTiles();
        Comp.setGame(game);
        /*
         * build Gui
         */
        setUpMenubar();

        setUpFileMenu();
        setUpOrdersMenu();
        setUpMessagesMenu();
        setUpArchivesMenu();
        setUpHouseMenu();
        setUpDiplomacyMenu();
        setUpByzantiumIIMenu();
        if (args.hasOption("wizardmode")) { //game.getEfs_ini().wizard_mode) {
            setUpWizardModeMenu();
        }
        /*
         * build gui windows
         */
        diplomacy_selector = DiplomacySelectorPanel.getWindow(this);
        strategy_selector = CombatStrategyPanel.getWindow(this);
        setUpBuildWindow();
        agora_autobuy_panel = AgoraAutobuyPanel.getWindow(this);
        setUpTechWindow();
        setUpTechDBWindow();
        setUpManowitzWindow();
        setUpResourceWindow();
        setUpBuildCityWindow();
        cargo_win = CargoPanel.getCargoWin(this);
        setUpPlanetWindow();
        setUpSpaceButton();
        setUpLaunchButton();
        setUpSpaceWindow();
        setUpUnitInfoWindow();
        setUpMainMenus();
        setUpCombatWindow();
        city_info_window = CityInfoWindow.getCityInfoWindow(this);
        setUpMiniMaps();
        setUpXPlayerScreen();
        setUpMessagesWindow();
        setUpByzantiumIIWindow();
        setUpGalaxyWindow();
        setUpGlobeWindow();
        agora_window = AgoraWindow.getAgoraWindow(this);
        house_window = HouseWindow.getHouseWindow(this);
        diplomacy_window = DiplomacyWindow.getWindow(this);
        resolve_contract_dialog = ResolveContract.getWindow(this);
        options_panel = OptionsPanel.getWindow(this);
        /*
         * collect main windows into card layout
         */
        assembleMainWindows();
        setUpStackMenu();
        setUpCityDialog3(game, ws);


        // initialize game state
        this.setCursor(resources.getCursor(C.S_CURSOR_SCEPTOR));
        Phoenix.addBootMsg(" done.");
        state = MM1.get();
        
        this.pack();
        Phoenix.closeBootFrame();
        Phoenix.startInputEventLogging(args);
        this.setVisible(true);

        // BUGFIX ? without this, the menubar menus will shift slightly when the
        // menubar is first switched using setMenus
        main_menu1.add(menubar_holder);
        setMenus(C.S_PLANET_MAP);

        // set up stack blink and jump route color cycling
        setUpAnimation();

    }

    private void setUpMenubar() {
        /*
         *set up menubar
         */
        menubar = new JMenuBar();
        menubar.setBackground(Color.BLACK);
        menubar.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        menubar.setBounds(0, 0, ws.main_window_width, 18);
        menubar_holder = new JPanel(null);
        menubar_holder.setBounds(0, 0, ws.main_window_width, 18);
        menubar_holder.add(menubar);
    }

    private void setUpAnimation() {
        /*
         * set animation timer
         */
        int delay = 400; //milliseconds
        ActionListener timer_listener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                animation_blink = !animation_blink;
                if (!stack_moving) {
                    main_windows.repaint();
                }
            }
        };
        Timer anim_timer = new Timer(delay, timer_listener);
        anim_timer.start();

        /*
        * set stack movement timer and listener,
         */
        int move_delay = 30;
        ActionListener stack_move_listener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                state.stackMoveEvent();
            }
        };
        stack_move_timer = new Timer(move_delay, stack_move_listener);

        setColorCycle();
        color_cycle_count = 0;
        color_cycle_color = color_cycle_colors[0];
        int cycle_delay = 200; //milliseconds
        ActionListener cycle_listener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {

                if (!stack_moving) {
                    main_windows.repaint();
                }
                color_cycle_count++;
                if (color_cycle_count == 5) {
                    color_cycle_count = -2;
                }
                if (color_cycle_count >= 0) {
                    color_cycle_color = color_cycle_colors[color_cycle_count];
                }
            }
        };
        Timer cycle_timer = new Timer(cycle_delay, cycle_listener);
        cycle_timer.start();
    }

    private void assembleMainWindows() {
        main_windows = new JPanel(new CardLayout());
        main_windows.add(main_menu1, C.S_MAIN_MENU1);
        main_windows.add(main_menu, C.S_MAIN_MENU);
        main_windows.add(messages_window, C.S_MESSAGES);
        main_windows.add(x_player_screen, C.S_X_PLAYER_SCREEN);
        main_windows.add(planet_window, C.S_PLANET_MAP);
        main_windows.add(space_window, C.S_STAR_MAP);
        main_windows.add(unit_info_window, C.S_UNIT_INFO);
        main_windows.add(combat_window, C.S_COMBAT_WINDOW);
        main_windows.add(byzantium_ii_window, C.S_BYZANTIUM_II_WINDOW);
        main_windows.add(agora_window, C.S_AGORA_WINDOW);
        main_windows.add(house_window, C.S_HOUSE_WINDOW);
        main_windows.add(diplomacy_window, C.S_DIPLOMACY_WINDOW);
        main_windows.add(galaxy_window, C.S_GALAXY_WINDOW);
        main_windows.add(globe_window, C.S_GLOBE_WINDOW);
        main_windows.add(city_info_window, C.S_CITY_INFO_WINDOW);

        this.getContentPane().add(main_windows, BorderLayout.CENTER);
    }

    private void setUpByzantiumIIWindow() {
        byzantium_ii_window = new ByzantiumIIWindow(this);
        byzantium_ii_window.setLayout(null);
        byzantium_ii_window.setPreferredSize(new Dimension(ws.main_window_width,
                ws.main_window_height));
        byzantium_ii_window.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                state.clickOnByzantiumIIWindow(e);
            }
        });
    }

    private void setUpMessagesWindow() {
        messages_window = new Messages(this);
        messages_window.setLayout(null);
        messages_window.setPreferredSize(new Dimension(ws.main_window_width,
                ws.main_window_height));
    }

    private void setUpXPlayerScreen() {
        x_player_screen = new XPlayerScreen(this);
        x_player_screen.setLayout(null);
        x_player_screen.setPreferredSize(new Dimension(ws.main_window_width,
                ws.main_window_height));
    }

    private void setUpCombatWindow() {
        combat_window = new CombatWindow(this);
        combat_window.setLayout(null);
        combat_window.setPreferredSize(new Dimension(ws.main_window_width,
                ws.main_window_height));
    }

    private void setUpMainMenus() {
        main_menu1 = new MainMenu.W1(this);
        main_menu1.setLayout(null);
        main_menu1.setPreferredSize(new Dimension(ws.main_window_width,
                ws.main_window_height));

        main_menu = new MainMenu(this);
        main_menu.setLayout(null);
        main_menu.setPreferredSize(new Dimension(ws.main_window_width,
                ws.main_window_height));
//        main_menu.setUpWindow();
    }

    private void setUpUnitInfoWindow() {
        /*
         * create unit info window/stack window
         */
        unit_info_window = new UnitInfoWindow(this);
        unit_info_window.setLayout(null);
        unit_info_window.setPreferredSize(new Dimension(ws.main_window_width,
                ws.main_window_height));
        unit_info_window.setUpWindow();
    }

    public void setUnitInfoWindowMode(boolean mode) {
        unit_info_window.setMode(mode);
        if (mode) {
            unit_info_window.saveSelectedStack();
        }
    }

    private void setUpGlobeWindow() {
        /*
         * create star map display
         */
        globe_window = new GlobeWindow(this);
        globe_window.setLayout(null);
        globe_window.setPreferredSize(new Dimension(ws.main_window_width,
                ws.main_window_height));

        globe_window.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
//                clickOnPlanetMap(e);
                state.clickOnWindow(e);
            }
        });

        globe_window.addMouseWheelListener(new MouseAdapter() {
            public void mouseWheelMoved(MouseWheelEvent e) {
//                handleWheelMove(e);
                state.wheelRotated(e);
            }
        });
    }

    private void setUpGalaxyWindow() {
        /*
         * create star map display
         */
        galaxy_window = new GalaxyWindow(this);
        galaxy_window.setLayout(null);
        galaxy_window.setPreferredSize(new Dimension(ws.main_window_width,
                ws.main_window_height));

        galaxy_window.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
//                clickOnPlanetMap(e);
                state.clickOnWindow(e);
            }
        });

        galaxy_window.addMouseWheelListener(new MouseAdapter() {
            public void mouseWheelMoved(MouseWheelEvent e) {
//                handleWheelMove(e);
                state.wheelRotated(e);
            }
        });
    }

    private void setUpSpaceWindow() {
        /*
         * create star map display
         */
        space_window = new SpaceWindow(this);
        space_window.setLayout(null);
        space_window.setPreferredSize(new Dimension(ws.main_window_width,
                ws.main_window_height));

        space_window.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
//                clickOnPlanetMap(e);
                state.clickOnSpaceWindow(e);
            }
        });

        space_map = new SpaceMap(this);

        space_window.add(space_map);

        space_map.setBounds(ws.space_map_x_pos, ws.space_map_y_pos,
                ws.space_map_width, ws.space_map_height);

        SpaceMap.setUpMouse(this, space_map);
//        space_map.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
////                clickOnSpaceMap(e);
//                state.clickOnMainMap(e);
//            }
//        });

        space_map.addMouseWheelListener(new MouseAdapter() {
            public void mouseWheelMoved(MouseWheelEvent e) {
//                handleWheelMove(e);
                state.wheelRotated(e);
            }
        });
    }

    private void setUpPlanetWindow() {
        /*
         * create planet map display
         */
        planet_window = new PlanetWindow(this);
        planet_window.setLayout(null);
        planet_window.setPreferredSize(new Dimension(ws.main_window_width,
                ws.main_window_height));
//        this.add(planet_window);

        planet_window.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
//                clickOnPlanetMap(e);
                state.clickOnPlanetWindow(e);
            }
        });

        planet_map = new PlanetMap(this);
        planet_window.add(planet_map);
        planet_map.setBounds(ws.planet_map_x_offset, ws.planet_map_y_offset,
                ws.planet_map_width, ws.planet_map_height);
        SpaceMap.setUpMouse(this, planet_map);
//        planet_map.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
////                clickOnPlanetMap(e);
//                state.clickOnMainMap(e);
//            }
//        });

        planet_map.addMouseWheelListener(new MouseAdapter() {
            public void mouseWheelMoved(MouseWheelEvent e) {
//                handleWheelMove(e);
                state.wheelRotated(e);
            }
        });
    }

    private void setUpBuildWindow() {
        build_window = new JDialog(this, true);
        build_window.setLayout(null);
        build_window.setPreferredSize(new Dimension(ws.planet_map_width + 50,
                ws.planet_map_height));
        //System.out.println("this.getX() = " + this.getX());
        build_window.setBounds(this.getX() + ws.planet_map_x_offset,
                this.getY() + ws.planet_map_y_offset,
                ws.planet_map_width + 50, ws.planet_map_height);
        build_window.setDefaultCloseOperation(
                JDialog.DO_NOTHING_ON_CLOSE);
        build_window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                build_panel.clearSelection();
                build_panel.zeroLists();
                build_window.setVisible(false);
            }
        });
        build_panel = new BuildPanel(this);
        build_panel.setLayout(null);
        build_window.add(build_panel);
        build_panel.setBounds(0, 0,
                ws.planet_map_width, ws.planet_map_height);
        build_window.add(build_panel);
        build_window.pack();
    }


    private void setUpFileMenu() {
        file_menu = new JMenu("File");
        menu_exit = new JMenuItem("Exit");
        menu_load = new JMenuItem("Load");
        menu_save = new JMenuItem("Save");
        menu_save_as = new JMenuItem("Save As");
        menu_restart = new JMenuItem("Restart");
        menu_options = new JMenuItem("Options");
//        menubar.setBackground(Color.DARK_GRAY);
//        file_menu.setBackground(Color.DARK_GRAY);
//        menu_exit.setBackground(Color.DARK_GRAY);
//        menubar.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
//
//        file_menu.setForeground(C.COLOR_GOLD);
//        menu_exit.setForeground(C.COLOR_GOLD);
        menu_exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int ret_val = 0;
                int robot_wait = 10000;
                if (RobotTester.isRobotTest()) {
                    System.out.println("Phoenix: RobotTester detected, waiting " + robot_wait + " ms for test shutdown");
                    try {
                        Thread.sleep(robot_wait);
                    } catch (InterruptedException ex) {
                    }
                    System.out.println("Phoenix: RobotTester not finished in " + robot_wait + " ms, exiting ...");
                    ret_val = 1;
                } else {
                    Util.recordState(FN.S_GAME_STATE_RECORD_FILE, game);
                }
                System.exit(ret_val);
            }
        });
        menu_load.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadGame();

            }
        });
        menu_save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showInfoWindow("Not implemented yet, use \"Save As\"");
            }
        });
        menu_save_as.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveGame();

            }
        });
        menu_options.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                options_panel.setWindowVisiblity(true);
            }
        });
        menu_restart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                toMainMenu();

            }
        });

        file_menu.add(menu_save);
        file_menu.add(menu_save_as);
        file_menu.add(menu_load);
        file_menu.add(menu_restart);
        file_menu.add(menu_options);
        file_menu.add(menu_exit);

        menubar.add(file_menu);
    }

    public static CommandLine getMainArgs() {
        return args;
    }

    /**
     * Open Manowitz volume vol
     *
     * @param vol
     */
    public void openManowitzVol(int vol) {
        manowitz_panel.pressContents(vol);
        setDialogSize(manowitz_window, ws.manowitz_window_x_offset,
                ws.manowitz_window_y_offset,
                ws.manowitz_window_w, ws.manowitz_window_h);
        manowitz_window.setVisible(true);
    }

    /**
     * Close (hide) Manowitz window.
     */
    public void closeManowitz() {
        manowitz_window.setVisible(false);
    }

    private void setUpWizardModeMenu() {
        wizard_menu = new JMenu("WIZARD MODE");
        menu_all_tech = new JMenuItem("Get all techs.");
        menu_all_tech.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        menu_all_tech.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean[] techs = game.getFaction(game.getTurn()).getResearch().techs;
                for (int i = 0; i < techs.length; i++) {
                    techs[i] = true;
                }
                game.getFaction(game.getTurn()).getResearch().setCanBuild(game.getUnitTypes());
            }
        });
        menu_all_resources = new JMenuItem("Get all resources.");
        menu_all_resources.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        menu_all_resources.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                game.getAllResources();
            }
        });
        menu_create_unit = new JMenuItem("Create units.");
        menu_create_unit.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        menu_create_unit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!(getCurrentState() instanceof PW)) {
                    return;
                }
                saveMainGameState();
                gui.setCurrentState(Wiz.get());
            }
        });
        menu_randomize_rng = new JMenuItem("Randomize RNG.");
        menu_randomize_rng.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        menu_randomize_rng.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                game.getRandom().setSeed(System.currentTimeMillis());
            }
        });
        menu_do_ai_turn = new JMenuItem("Do AI turn.");
        menu_do_ai_turn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        menu_do_ai_turn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                game.doAITurn();
            }
        });
        toggle_ai = new JMenuItem("Toggle AI. " + !game.getHumanControl()[C.SYMBIOT]);
        toggle_ai.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        toggle_ai.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                game.setFactionPlayer(C.SYMBIOT, !game.getHumanControl()[C.SYMBIOT]);
                toggle_ai.setText("Toggle AI. " + !game.getHumanControl()[C.SYMBIOT]);
            }
        });
        menu_omniscience = new JMenuItem("Omniscience.");
        menu_omniscience.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        menu_omniscience.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                game.omniscience();
            }
        });
        wizard_menu.add(menu_all_tech);
        wizard_menu.add(menu_all_resources);
        wizard_menu.add(menu_create_unit);
        wizard_menu.add(menu_randomize_rng);
        wizard_menu.add(menu_do_ai_turn);
        wizard_menu.add(toggle_ai);
        wizard_menu.add(menu_omniscience);
//        wizard_menu.add(show_all);

        menubar.add(wizard_menu);
    }

    private void setUpOrdersMenu() {
        orders_menu = new JMenu("Orders");
        menu_build = new JMenuItem("Build units");

        menu_build.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showBuildWindow(e, -1, null);
            }
        });

        menu_research = new JMenuItem("Research");

        menu_research.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                showTechWindow();
            }
        });

        menu_build_city = new JMenuItem("Build City");
        menu_build_city.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_B, 0));
        menu_build_city.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                showBuildCityWindow();
            }
        });

        menu_build_road = new JMenuItem("Build Road");
        menu_build_road.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_R, 0));
        menu_build_road.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                state.buildRoad();
            }
        });

        menu_raze_city = new JMenuItem("Raze City");

        menu_raze_city.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                state.razeCity();
            }
        });
        orders_menu.add(menu_build);
        orders_menu.add(menu_research);
        orders_menu.add(menu_build_city);
        orders_menu.add(menu_build_road);
        orders_menu.add(menu_raze_city);

        menubar.add(orders_menu);
    }

    private void setUpArchivesMenu() {
        archives_menu = new JMenu("Archives");
        archives_menu.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        menu_vol1 = new JMenuItem("Volume 1: The Known Worlds");
        menu_vol1.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        menu_vol1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openManowitzVol(1);
            }
        });
        menu_vol2 = new JMenuItem("Volume 2: Microbiology");
        menu_vol2.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        menu_vol2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openManowitzVol(2);
            }
        });
        menu_vol3 = new JMenuItem("Volume 3: Physics");
        menu_vol3.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        menu_vol3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openManowitzVol(3);
            }
        });
        menu_vol4 = new JMenuItem("Volume 4: Psycho-Social");
        menu_vol4.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        menu_vol4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openManowitzVol(4);
            }
        });
        menu_vol5 = new JMenuItem("Volume 5: Military Units");
        menu_vol5.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        menu_vol5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openManowitzVol(5);
            }
        });
        menu_group_finder = new JMenuItem("Group Finder");
        menu_group_finder.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        menu_group_finder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SU.showGroupFinder();
//                C.MoveType[] move_types = C.MoveType.values();
//                int[] unit_manifest = new int[move_types.length];
//                for (Unit unit : game.getUnits()) {
//                    if (unit.owner == game.getTurn()) {
//                        ++unit_manifest[unit.move_type.ordinal()];
//                    }
//                }
//                String s_units = "";
//                for (int i = 0; i < unit_manifest.length; i++) {
//                    s_units += move_types[i] + " " + unit_manifest[i] + "\n";
//                }
//                showInfoWindow(s_units);
            }
        });
        menu_city_info = new JMenuItem("City Info");
        menu_city_info.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        menu_city_info.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                city_info_window.enterCityInfoWindow();
                SU.showCityInfoWindow();
            }
        });
        archives_menu.add(menu_vol1);
//        archives_menu.addSeparator();
        archives_menu.add(menu_vol2);
        archives_menu.add(menu_vol3);
        archives_menu.add(menu_vol4);
//        archives_menu.addSeparator();
        archives_menu.add(menu_vol5);
        archives_menu.add(menu_group_finder);
        archives_menu.add(menu_city_info);
        
        menubar.add(archives_menu);
    }

    private void setUpHouseMenu() {
        house_menu = new JMenuItem("House");
        house_menu.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        house_menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SU.showHouseWindow();
                house_window.enterHouseWindow();
            }
        });
        menubar.add(house_menu);
        
//        house_menu.setSize(55, 17);
//        house_menu.setBounds(0, 0, 55, 17);
        
    }

    private void setUpDiplomacyMenu() {
        diplomacy_menu = new JMenuItem("Diplomacy");
        diplomacy_menu.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        diplomacy_menu.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                diplomacy_selector.setWindowVisiblity(true);
            }
        });
        menubar.add(diplomacy_menu);

    }

    private void setUpByzantiumIIMenu() {
        byzantium_ii_menu = new JMenuItem("Byzantium II");
        byzantium_ii_menu.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        byzantium_ii_menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                byzantium_ii_window.enterWindow();
                SU.showByzantiumIIWindow();
            }
        });
        menubar.add(byzantium_ii_menu);
    }

    private void setUpMessagesMenu() {
        messages_menu = new JMenu("Messages");
        menu_send_message = new JMenuItem("Send Message");
        menu_send_message.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        messages_menu.add(menu_send_message);

        menu_read_messages = new JMenuItem("Read Messages");
        menu_read_messages.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        menu_read_messages.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
                messages_window.addMessageData();

                SU.showMessagesWindow();
            }
        });
        messages_menu.add(menu_read_messages);

        menu_read_contracts = new JMenuItem("Read Contracts");
        menu_read_contracts.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        messages_menu.add(menu_read_contracts);

        menubar.add(messages_menu);
    }

    public void showCargoWin(boolean show) {
        if (show) {
            setDialogSize(cargo_win, ws.cpw_x, ws.cpw_y, ws.cpw_w, ws.cpw_h);

        }
        cargo_win.setVisible(show);
    }

    public void initCargoWin(Unit u_from, Unit u_to, List<Unit> stack) {
        cargo_win.init(u_from, u_to, stack);
    }

    public void hideTechDBWindow() {
        tech_db_window.setVisible(false);
    }

    public void hideTechWindow() {
        tech_window.setVisible(false);
    }

    public void showTechWindow() {
        showTechWindow(-1, -1, null, null);
    }

    /**
     * When donor < 0, this will prime tech window for setting lab research targets.
     * When donor >= 0, this will prime tech window for selecting swapped techs
     * in contracts.
     *
     * @param donor
     * @param recipient
     * @param selected
     */
    public void showTechWindow(int donor, int recipient, List<Integer> in_contract, int[] selected) {
        tech_panel.setTechData(donor, recipient, in_contract);
        tech_panel.setSwapTech(selected);
        tech_panel.setLabsCost();
        tech_panel.setRPAvailable();
        tech_panel.setLabResearches();
//        tech_window.setBounds(this.getX() + ws.tech_window_x_offset,
//                this.getY() + ws.tech_window_y_offset,
//                ws.tech_window_w, ws.tech_window_h);
        setDialogSize(tech_window, ws.tech_window_x_offset,
                ws.tech_window_y_offset, ws.tech_window_w, ws.tech_window_h);
        tech_window.setVisible(true);
    }

    public void showBuildCityWindow() {
        if (build_city_panel.initPanel()) {
            setDialogSize(build_city_window, 0, 0,
                    ws.main_window_width, ws.main_window_height);
            build_city_window.setVisible(true);
        }
    }

    public void hideBuildCityWindow() {
        build_city_panel.zeroUnit();
        build_city_window.setVisible(false);
    }

    private void setUpBuildCityWindow() {
//        JDialog build_city_window;
//        JPanel build_city_panel;
        build_city_window = new JDialog(this, true);
        build_city_window.setLayout(null);
        build_city_window.setDefaultCloseOperation(
                JDialog.DO_NOTHING_ON_CLOSE);
        build_city_window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                hideBuildCityWindow();
            }
        });
        build_city_panel = new BuildCityPanel(this);
        build_city_panel.setLayout(null);
        build_city_window.add(build_city_panel);
        build_city_panel.setBounds(0, 0,
                ws.main_window_width, ws.main_window_height);
        build_city_window.setUndecorated(true);
        build_city_window.pack();
        setDialogSize(build_city_window, 0,
                0,
                ws.main_window_width, ws.main_window_height);
    }

    public void showResourceWindow(int resource) {
        resource_panel.setText(resource);
        setDialogSize(resource_window, ws.rw_x_offset, ws.rw_y_offset,
                ws.rw_width, ws.rw_height);
        resource_window.setVisible(true);
    }

    public void hideResourceWindow() {
        resource_window.setVisible(false);
    }

    private void setUpResourceWindow() {
//        JDialog resource_window;
//        JPanel resource_panel;
        resource_window = new JDialog(this, true);
        resource_window.setLayout(null);
        resource_window.setDefaultCloseOperation(
                JDialog.DO_NOTHING_ON_CLOSE);
        resource_window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {

                resource_window.setVisible(false);
            }
        });
        resource_panel = new ResourcePanel(this);
        resource_panel.setLayout(null);
        resource_window.add(resource_panel);
        resource_panel.setBounds(0, 0,
                ws.rw_width, ws.rw_height);
//        resource_window.add(resource_panel);
        resource_window.pack();
        setDialogSize(resource_window, ws.rw_x_offset, ws.rw_y_offset,
                ws.rw_width, ws.rw_height);
    }

    private void setUpTechWindow() {
//        JDialog tech_window;
//        JPanel tech_panel;
        tech_window = new JDialog(this, true);
//        tech_window.setUndecorated(true);
        tech_window.setLayout(null);
        tech_window.setPreferredSize(new Dimension(ws.tech_window_w,
                ws.tech_window_h));
        //System.out.println("this.getX() = " + this.getX());
        tech_window.setBounds(this.getX() + ws.tech_window_x_offset,
                this.getY() + ws.tech_window_y_offset,
                ws.tech_window_w, ws.tech_window_h);
//        tech_window.setSize(ws.tech_window_w, ws.build_window_height);
        tech_window.setDefaultCloseOperation(
                JDialog.DO_NOTHING_ON_CLOSE);
        tech_window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {

                tech_window.setVisible(false);
            }
        });
        tech_panel = new TechPanel(this);
        tech_panel.setLayout(null);
        tech_window.add(tech_panel);
        tech_panel.setBounds(0, 0,
                ws.tech_window_w, ws.tech_window_h);
        tech_window.add(tech_panel);
        tech_window.pack();
//        setDialogSize(tech_window, this.getX() + ws.tech_window_x_offset,
//                this.getY() + ws.tech_window_y_offset,
//                ws.tech_window_w, ws.tech_window_h);
    }

    public void showTechDBWindow() {
        tech_db_panel.setTechDBPanel();
        setDialogSize(tech_db_window, ws.tech_window_x_offset,
                ws.tech_window_y_offset,
                ws.tech_window_w, ws.tech_window_h);
        tech_db_window.setVisible(true);
    }

    private void setUpTechDBWindow() {
//        JDialog tech_window;
//        JPanel tech_panel;
        tech_db_window = new JDialog(this, true);
        tech_db_window.setLayout(null);
        tech_db_window.setPreferredSize(new Dimension(ws.tech_window_w,
                ws.tech_window_h));
        //System.out.println("this.getX() = " + this.getX());
        tech_db_window.setBounds(this.getX() + ws.tech_window_x_offset,
                this.getY() + ws.tech_window_y_offset,
                ws.tech_window_w, ws.tech_window_h);
        tech_db_window.setDefaultCloseOperation(
                JDialog.DO_NOTHING_ON_CLOSE);
        tech_db_window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {

                tech_db_window.setVisible(false);
            }
        });
        tech_db_panel = new TechDBPanel(this);
        tech_db_panel.setLayout(null);
        tech_db_window.add(tech_db_panel);
        tech_db_panel.setBounds(0, 0,
                ws.tech_window_w, ws.tech_window_h);
        tech_db_window.add(tech_db_panel);
        tech_db_window.pack();
        setDialogSize(tech_db_window, ws.tech_window_x_offset,
                ws.tech_window_y_offset,
                ws.tech_window_w, ws.tech_window_h);
    }

    public void showManowitz(int vol, int chapter) {
        if (!manowitz_panel.findChapter(vol, chapter)) {
            showInfoWindow("No Manowitz file for this tech.");
        } else {
            manowitz_panel.setNrChapters(vol);
            manowitz_panel.setChapter(vol, chapter);
            manowitz_panel.setState();
            setDialogSize(manowitz_window, ws.manowitz_window_x_offset,
                    ws.manowitz_window_y_offset,
                    ws.manowitz_window_w, ws.manowitz_window_h);
            manowitz_window.setVisible(true);
//        setDialogSize(manowitz_window, this.getX() + ws.manowitz_window_x_offset,
//                this.getY() + ws.manowitz_window_y_offset,
//                ws.manowitz_window_w, ws.manowitz_window_h);
        }
    }

    private void setUpManowitzWindow() {
//        JDialog tech_window;
//        JPanel tech_panel;
        manowitz_window = new JDialog(this, true);
//        manowitz_window.setUndecorated(true);
//        manowitz_window.setLayout(null);

//        manowitz_window.setPreferredSize(new Dimension(ws.manowitz_window_w,
//                ws.manowitz_window_h));
//        System.out.println("this.getX() = " + this.getX());
//        manowitz_window.setBounds(this.getX() + ws.manowitz_window_x_offset,
//                this.getY() + ws.manowitz_window_y_offset,
//                ws.manowitz_window_w, ws.manowitz_window_h);
        manowitz_window.setDefaultCloseOperation(
                JDialog.DO_NOTHING_ON_CLOSE);
        manowitz_window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {

                manowitz_window.setVisible(false);
            }
        });
        manowitz_panel = new Manowitz(this);
        manowitz_panel.setLayout(null);
        manowitz_window.add(manowitz_panel);
        manowitz_panel.setBounds(0, 0,
                ws.manowitz_window_w, ws.manowitz_window_h);
//        manowitz_window.add(manowitz_panel);
        manowitz_window.setUndecorated(true);
        manowitz_window.pack();
//        manowitz_window.setSize(ws.manowitz_window_w, ws.manowitz_window_h);
        setDialogSize(manowitz_window, ws.manowitz_window_x_offset,
                ws.manowitz_window_y_offset,
                ws.manowitz_window_w, ws.manowitz_window_h);
    }

    private void setUpMiniMaps() {
        galactic_map = new GalacticMap(this, game, ws, true);
        space_window.add(galactic_map);
        galactic_map.setBounds(ws.galactic_map_x_pos, ws.galactic_map_y_pos,
                ws.galactic_map_width, ws.galactic_map_height);
        galactic_map.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                state.clickOnGalacticMap(e);
            }
        });

        globe_map = new GlobeMap(this, game, ws, true);
        planet_window.add(globe_map);
        globe_map.setBounds(ws.globe_map_x_pos, ws.globe_map_y_pos,
                ws.globe_map_width, ws.globe_map_height);
        globe_map.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                state.clickOnGlobeMap(e);
            }
        });

        galactic_map_cw = new GalacticMap(this, game, ws, false);
        combat_window.add(galactic_map_cw);
        galactic_map_cw.setBounds(ws.cw_gm_x, ws.cw_gm_y,
                ws.cw_gm_w, ws.cw_gm_h);

        globe_map_cw = new GlobeMap(this, game, ws, false);
        combat_window.add(globe_map_cw);
        globe_map_cw.setBounds(ws.cw_glm_x, ws.cw_glm_y,
                ws.cw_glm_w, ws.cw_glm_h);

        galactic_map_uiw = new GalacticMap(this, game, ws, true);
        unit_info_window.add(galactic_map_uiw);
        galactic_map_uiw.setBounds(ws.group_finder.get(G.GF.GAL_MAP_X), ws.group_finder.get(G.GF.GAL_MAP_Y),
                ws.cw_gm_w, ws.cw_gm_h);

        globe_map_uiw = new GlobeMap(this, game, ws, true);
        unit_info_window.add(globe_map_uiw);
        globe_map_uiw.setBounds(ws.group_finder.get(G.GF.PLAN_MAP_X), ws.group_finder.get(G.GF.PLAN_MAP_Y),
                ws.cw_glm_w, ws.cw_glm_h);

        galactic_map_ciw = new GalacticMap(this, game, ws, true);
        city_info_window.add(galactic_map_ciw);
        galactic_map_ciw.setBounds(ws.group_finder.get(G.GF.GAL_MAP_X), ws.group_finder.get(G.GF.GAL_MAP_Y),
                ws.cw_gm_w, ws.cw_gm_h);

        globe_map_ciw = new GlobeMap(this, game, ws, true);
        city_info_window.add(globe_map_ciw);
        globe_map_ciw.setBounds(ws.group_finder.get(G.GF.PLAN_MAP_X), ws.group_finder.get(G.GF.PLAN_MAP_Y),
                ws.cw_glm_w, ws.cw_glm_h);
    }

    public void setDialogSize(JDialog dialog, int x, int y, int w, int h) {
        Insets insets_p = this.getInsets();
        Insets insets_d = dialog.getInsets();

        Dimension d_pane = dialog.getContentPane().getSize();
        Dimension d_window = dialog.getSize();
        int w_dec_thickness = insets_p.left + insets_p.right;
        int h_dec_thickness = insets_p.top + insets_p.bottom;
        System.out.println("w/h_dec_thickness = " + w_dec_thickness + "," + h_dec_thickness);
        dialog.setBounds(this.getX() + x + insets_p.left, this.getY() + y + insets_p.top, w + w_dec_thickness, h + h_dec_thickness);
    }

//    public int getXOffset() {
//        Dimension d = this.getContentPane().getSize();
//        d.width
//    }
    /**
     * If planet != -1 set planet selected and city selected in Build Panel with
     * planet and city.
     *
     * @param e not used
     * @param planet
     * @param city
     */
    public void showBuildWindow(ActionEvent e, int planet, Structure city) {
        build_window.setBounds(this.getX() + ws.planet_map_x_offset,
                this.getY() + ws.planet_map_y_offset,
                ws.build_window_width, ws.build_window_height);
        build_panel.setPlanets();
        if (planet != -1) {
            build_panel.planetSelected(null, planet);
            build_panel.setSelectedPlanet(planet);
            build_panel.setSelectedCity(city);
            build_panel.citySelected(null);
        }
        build_window.setVisible(true);
    }

    public void showAgoraAutobuyDialog(int[] resources, int planet, int type, int t_lvl, boolean[] response) {
        agora_autobuy_panel.enterDialog(resources, planet, type, t_lvl, response);
    }

    public void showStackMenu(MouseEvent e) {
        stack_menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void setUpStackMenu() {
        stack_menu = new JPopupMenu("Select");
        JMenuItem select_all = new JMenuItem("Select all");
        JMenuItem select_combat = new JMenuItem(" -combat");
        JMenuItem select_bombard = new JMenuItem(" -bombard");
        JMenuItem select_noncombat = new JMenuItem(" -noncombat");
//        JMenuItem select_attack = new JMenuItem(" attack");
        JMenuItem select_transport = new JMenuItem(" -transport");

        stack_menu.add(select_all);
        stack_menu.add(select_combat);
        stack_menu.add(select_bombard);
        stack_menu.add(select_noncombat);
//        stack_menu.add(select_attack);
        stack_menu.add(select_transport);
        select_all.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectUnits(C.S_ALL);
            }
        });
        select_combat.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectUnits(C.S_COMBAT);
            }
        });
        select_bombard.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectUnits(C.S_BOMBARD);
            }
        });
        select_noncombat.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectUnits(C.S_NONCOMBAT);
            }
        });
//        select_attack.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                selectUnits("attack");
//            }
//        });
        select_transport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectUnits(C.S_TRANSPORT);
            }
        });
    }

    public void selectUnits(String sel) {
        List<Unit> stack = game.getSelectedStack();

        StackIterator iterator = new StackIterator(stack);
        Unit u = iterator.next();

        while (u != null) {
            switch (sel) {
                case C.S_ALL:
                    if (u.move_points > 0) {
                        u.setSelected(true);
                    } else {
                        u.setSelected(false);
                    }
                    break;
                case C.S_COMBAT:
                    if (u.move_points > 0 && UnitType.isAttackCapable(u)) {
                        u.setSelected(true);
                    } else {
                        u.setSelected(false);
                    }
                    if (u.carrier != null) {
                        u.setSelected(u.carrier.isSelected());
                    }
                    break;
                case C.S_BOMBARD:
                    if (u.move_points > 0 && u.type_data.ranged_sp_str > 0) {
                        u.setSelected(true);
                    } else {
                        u.setSelected(false);
                    }
                    break;
                case C.S_NONCOMBAT:
                    if (u.move_points > 0 && !UnitType.isAttackCapable(u)) {
                        u.setSelected(true);
                    } else {
                        u.setSelected(false);
                    }
                    if (u.carrier != null) {
                        u.setSelected(u.carrier.isSelected());
                    }
                    break;
                case C.S_TRANSPORT:
                    if (u.move_points > 0 && u.type_data.cargo > 0) {
                        u.setSelected(true);
                    } else {
                        u.setSelected(false);
                    }
                    if (u.carrier != null) {
                        u.setSelected(u.carrier.isSelected());
                    }
                    break;
                default:
                    throw new AssertionError();
            }
            u = iterator.next();
        }
        // to update build city menu item
        SU.setStateUpKeep(state);
    }

    public Resource getResources() {
        return resources;
    }

    public BuildPanel getBuildPanel() {
        return build_panel;
    }

    public JDialog getBuildWindow() {
        return build_window;
    }

    public void setLoadSaveWinUp(boolean state) {
        loadsave_win_up = state;
    }

    public boolean getLoadSaveWinUp() {
        return loadsave_win_up;
    }

    public void cancelLoadSaveDialog() {
        loadsave_dialog.dispose();
    }

    public void showCityDialog(int planet, Structure city) {
//        city_dialog.setBounds(this.getX() + ws.city_window_x,
//                this.getY() + ws.city_window_y,
//                ws.city_window_w, ws.city_window_h);
        setDialogSize(city_dialog, ws.city_window_x, ws.city_window_y,
                ws.city_window_w, ws.city_window_h);
        city_dialog.setPlanet(planet);
        city_dialog.setCity(city);
        city_dialog.setVisible(true);
    }

    /**
     * @return the byzantium_ii_window
     */
    public ByzantiumIIWindow getByzantium_ii_window() {
        return byzantium_ii_window;
    }

    /**
     * @return the house_window
     */
    public HouseWindow getHouseWindow() {
        return house_window;
    }

    /**
     * @return the diplomacy_window
     */
    public DiplomacyWindow getDiplomacyWindow() {
        return diplomacy_window;
    }

    /**
     * @return the color_cycle_count
     */
    public int getColorCycleCount() {
        return color_cycle_count;
    }

    public GalaxyWindow getGalaxyWindow() {
        return galaxy_window;
    }

    public GlobeWindow getGlobeWindow() {
        return globe_window;
    }

    public UnitInfoWindow getUnitInfoWindow() {
        return unit_info_window;
    }

    public CityInfoWindow getCityInfoWindow() {
        return city_info_window;
    }

    private class CityDialog extends JDialog {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private int planet;
        private Structure city;
        private CityPanel city_panel;
        private Game game;
        private WindowSize ws;

        public CityDialog(Frame owner, String title, boolean modal, Game game, WindowSize ws) {
            super(owner, title, modal);
            this.game = game;
            this.ws = ws;
            this.setUndecorated(true);
            setUp();
        }

        public void setUp() {
            city_panel = new CityPanel();
            city_panel.setLayout(null);
            city_panel.setBounds(0, 0, ws.build_window_width, ws.build_window_height);
            city_panel.setBackground(Color.DARK_GRAY);
            city_panel.setForeground(Color.DARK_GRAY);
            this.add(city_panel);
//        dialog.setVisible(true);
        }

        public void setGame(Game game) {
            this.game = game;
        }

        /**
         * @return the planet
         */
        public int getPlanet() {
            return planet;
        }

        /**
         * @param planet the planet to set
         */
        public void setPlanet(int planet) {
            this.planet = planet;
        }

        /**
         * @return the city
         */
        public Structure getCity() {
            return city;
        }

        /**
         * @param city the city to set
         */
        public void setCity(Structure city) {
            this.city = city;
            city_panel.setCity(city);
        }
    }

    private void setUpCityDialog3(Game game, WindowSize ws) {
        city_dialog = new CityDialog(this, null, true, game, ws);
        city_dialog.setBounds(this.getX() + ws.city_window_x,
                this.getY() + ws.city_window_y,
                ws.city_window_w, ws.city_window_h);
        city_dialog.setDefaultCloseOperation(
                JDialog.DO_NOTHING_ON_CLOSE);
        city_dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                ;
            }
        });

//        city_dialog.add(city_panel);
        city_dialog.pack();
//        dialog.setVisible(true);
    }

    private class CityPanel extends JPanel {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        JButton build;
        JButton exit;
        Structure city;
        BufferedImage bi;
        public CityPanel() {
            this.bi = Util.loadImage(FN.S_BG0_PCX, ws.is_double, pallette, 640, 480);
            setUpPanel();
        }

        /**
         *
         * @param city the value of city
         */
        public void setCity(Structure city) {
            this.city = city;
        }

        public void paintComponent(Graphics g) {
            System.out.println("Paint");
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(this.bi, null, 0, 0);
            UtilG.drawCityArea(g, game, ws, ws.city_panel_city_area_x, ws.city_panel_city_area_x, city);
        }

        public void setUpPanel() {
            build = new JButton("Build");
            build.setFont(ws.font_default);
            build.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
            this.add(build);
            build.setBounds(ws.city_build_button_x_offset, ws.city_build_button_y_offset,
                    ws.city_build_button_w, ws.city_build_button_h);
            build.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    city_dialog.setVisible(false);
                    showBuildWindow(null, city_dialog.getPlanet(), city_dialog.getCity());
                }
            });

            exit = new JButton("Exit");
            exit.setFont(ws.font_default);
            exit.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
            this.add(exit);
            exit.setBounds(ws.city_exit_button_x_offset, ws.city_exit_button_y_offset,
                    ws.city_exit_button_w, ws.city_exit_button_h);
            exit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    city_dialog.setVisible(false);
                }
            });
        }
    }

//    public void setUpCityDialog2() {
//        JDialog city_dialog;
//        CityPanel city_panel;
//                city_dialog = new JDialog(this, true);
//        city_dialog.setLayout(null);
//        city_dialog.setPreferredSize(new Dimension(ws.planet_map_width + 50,
//                ws.planet_map_height));
//        System.out.println("this.getX() = " + this.getX());
//        city_dialog.setBounds(this.getX() + ws.planet_map_x_offset,
//                this.getY() + ws.planet_map_y_offset,
//                ws.planet_map_width + 50, ws.planet_map_height);
//        city_dialog.setDefaultCloseOperation(
//                JDialog.DO_NOTHING_ON_CLOSE);
//        city_dialog.addWindowListener(new WindowAdapter() {
//            public void windowClosing(WindowEvent we) {
//                city_panel.clearSelection();
//                city_panel.zeroLists();
//                city_dialog.setVisible(false);
//            }
//        });
//        city_panel = new BuildPanel(this);
//        city_panel.setLayout(null);
//        city_dialog.add(city_panel);
//        city_panel.setBounds(0, 0,
//                ws.planet_map_width, ws.planet_map_height);
//        city_dialog.add(city_panel);
//        city_dialog.pack();
//    }
    public JDialog showProgressBar(String text) {
        JDialog dialog = new JDialog(this, text, true);
        int p_width = this.getWidth();
        int p_height = this.getHeight();

        int width = p_width / 3;
        //System.out.println("width = " + width);
        int heigth = p_height / 5;
        //System.out.println("heigth = " + heigth);
        int x = (p_width - width) / 2;
        int y = (p_height - heigth) / 2;
        dialog.setBounds(this.getX() + x, this.getY() + y, width, heigth);
        dialog.setDefaultCloseOperation(
                JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                ;
            }
        });
        JProgressBar progress_bar = new JProgressBar();
        progress_bar.setSize(width, heigth);
        progress_bar.setIndeterminate(true);
        dialog.add(progress_bar);
        dialog.pack();
//        dialog.setVisible(true);
        return dialog;
    }

    public void loadGame() {
        // to set attributes of chooser components google "Darryl SwingUtils"
        // or http://tips4java.wordpress.com/2008/11/13/swing-utils/
        JFileChooser chooser = new JFileChooser();
        UtilG.setJComponentChildrenToDark(chooser);
        String path_name = FN.S_SAVE_PATH;
        chooser.setCurrentDirectory(new File(path_name));
        int returnVal = chooser.showOpenDialog(this);
        //System.out.println("path_name = " + path_name);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            RobotTester.setWaitState(true); // tell Robot tester we are unresposive
            String load_name = chooser.getCurrentDirectory().getAbsolutePath()
                    + System.getProperty("file.separator")
                    + chooser.getSelectedFile().getName();

            //System.out.println("load_name = " + load_name);
//            JDialog load_dialog = showProgressBar("Loading game");
            loadsave_dialog = showProgressBar("Loading game");
//            Cursor cursor = this.getCursor();
//            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            new LoadWorker(this, load_name).execute();

            loadsave_dialog.setVisible(true);

            if (!load_succesfull) {
                RobotTester.setWaitState(false); // tell Robot tester we are online
                return;
            }

            SU.setWindow(C.S_X_PLAYER_SCREEN);
            setGameReferences();
            game.setPath(null);
            game.setJumpPath(null);
            PBEM pbem = game.getEfs_ini().pbem;
            RobotTester.setWaitState(false); // tell Robot tester we are online
            if (pbem.pbem) {

                if (pbem.end_turn) {
                    pbem.end_turn = false;
                    game.endTurn();
                    pbem_gui.testDATAHashes(this);
                }
                if (pbem.password_revocation) {  // if password revocation sequence
                    int password_turn = pbem_gui.passwordTurn(game); // get next confirmation giver
                    if (pbem_gui.testPasswd(password_turn, this, game) == PBEMGui.PASSWORD_OK) { // test password
                        pbem_gui.setRevokeConfirm(password_turn); // set confirmation password
                        if (pbem_gui.testConfirmPasswdsSet(game)) { // if all confirmations given
                            pbem_gui.revokePassword(); // zero password of revoked player
                            if (!pbem.revocation_action) { // if action == set to computer control
                                game.setFactionPlayer(pbem.revoked_player, false); // set to computer control
                                game.endTurn(); // process turn
                                pbem_gui.zeroRevocationConfirm(); // zero revocation confirmation passwords
                                pbem.revoked_player = -1; // set revoked player to -1
                            }
                            pbem.password_revocation = false; // cancel password revocation flag
                        }
                        saveGame();
                        toMainMenu();
                        return;
                    } else {
                        toMainMenu();
                        return;
                    }
                }
                // if game.first year && no_passwd ask for a new password
                // else if no_passwd && all revocation confirm passwords set ask
                // for a new password && zero all revocation confirm passwords
                // else ask for password if fail return to main menu
                // if password revocation save & return to main menu
                if (game.getYear() == C.STARTING_YEAR
                        && pbem.passwd_hashes[game.getTurn()] == null) {
                    pbem_gui.getPasswd(game.getTurn(), this);
                } else if (pbem.passwd_hashes[game.getTurn()] == null
                        && pbem_gui.testConfirmPasswdsSet(game)) {
                    pbem_gui.getPasswd(game.getTurn(), this);
                    pbem_gui.zeroRevocationConfirm();
                    pbem.revoked_player = -1;
                } else {
                    int reply = pbem_gui.testPasswd(game.getTurn(), this, game);
                    if (reply == PBEMGui.PASSWORD_FAIL) {
                        return;
                    } else if (reply == PBEMGui.PASSWORD_REVOKE) {
                        saveGame();
                        toMainMenu();
                        return;
                    }
                }
            }
            Point p = game.getSelectedPoint();
            if (p == null) {
                SU.selectNextUnmovedUnit();

            } else {
                //System.out.println("selected stack");
                List<Unit> stack = game.getSelectedStack();
                SU.centerMapOnUnit(stack.get(0));
            }
//                System.exit(0);

//            this.setCursor(cursor);
        }
    }


    public void saveGame() {
        // to set attributes of chooser components google "Darryl SwingUtils"
        // or http://tips4java.wordpress.com/2008/11/13/swing-utils/
        JFileChooser chooser = new JFileChooser();
        UtilG.setJComponentChildrenToDark(chooser);
//        FileNameExtensionFilter filter = new FileNameExtensionFilter(
//                "JPG & GIF Images", "jpg", "gif");
//        chooser.setFileFilter(filter);
        String path_name = FN.S_SAVE_PATH;
        chooser.setCurrentDirectory(new File(path_name));

        int returnVal = chooser.showSaveDialog(this);
        //System.out.println("path_name = " + path_name);
//        System.exit(0);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            RobotTester.setWaitState(true); // tell Robot tester we are unresponsive
            final String save_name = chooser.getCurrentDirectory().getAbsolutePath()
                    + System.getProperty("file.separator")
                    + chooser.getSelectedFile().getName();

            //System.out.println("save_name = " + save_name);
//            FileOutputStream f;
//            ObjectOutput s;
            loadsave_dialog = showProgressBar("Saving game");
            Cursor cursor = this.getCursor();
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            new SaveWorker(this, save_name).execute();

            loadsave_dialog.setVisible(true);

            if (game.getEfs_ini().pbem.pbem && game.getEfs_ini().pbem.end_turn) {
                toMainMenu();
            }
            this.setCursor(cursor);
            RobotTester.setWaitState(false); // tell Robot tester we are online
        }
    }

    private class LoadWorker extends SwingWorker<Void, Void> {

        private Gui gui;
        private String load_name;

        public LoadWorker(Gui gui, String load_name) {
            this.gui = gui;
            this.load_name = load_name;
        }

        public Void doInBackground() {
            while (!loadsave_dialog.isVisible()) {
                try {
                    Thread.sleep(200);
                } catch (Exception e) {
                    ;
                }

            }
//            try {
//                Thread.sleep(5000);
//            } catch (Exception e) {
//                ;
//            }
            load_succesfull = true; // assume true
            try (FileInputStream in = new FileInputStream(load_name);
                    GZIPInputStream gis = new GZIPInputStream(in);
                    ObjectInputStream s = new ObjectInputStream(gis)) {
                Game tmp = (Game) s.readObject();
                game = tmp;
                //System.out.println("after read object");
//                space_map.setGame(game);
//                planet_map.setGame(game);
//                planet_window.setGame(game);
//                space_window.setGame(game);
//                unit_info_window.setGame(game);
//                main_menu.setGame(game);
//                combat_window.setGame(game);
//                galactic_map.setGame(game);
//                globe_map.setGame(game);
//                build_panel.setGame(game);
//                tech_panel.setGame(game);
//                tech_db_panel.setGame(game);
//                manowitz_panel.setGame(game);
//                resource_panel.setGame(game);
//                build_city_panel.setGame(game);
//                State.setGameRef(game);
//                Comp.setGame(game);

            } catch (Throwable ex) {
                load_succesfull = false;
                Util.logEx(null, ex, "Load game failed");
                showInfoWindow("Load failed");
            }
            return null;
        }

        public void done() {
            gui.cancelLoadSaveDialog();
        }
    }

    private class SaveWorker extends SwingWorker<Void, Void> {

        private Gui gui;
        private String save_name;

        public SaveWorker(Gui gui, String save_name) {
            this.gui = gui;
            this.save_name = save_name;
        }

        public Void doInBackground() {
            while (!loadsave_dialog.isVisible()) {
                try {
                    Thread.sleep(200);
                } catch (Exception e) {
                    ;
                }

            }
            try (FileOutputStream f = new FileOutputStream(save_name);
                    GZIPOutputStream gos = new GZIPOutputStream(f);
                    ObjectOutputStream s = new ObjectOutputStream(gos)) {

                s.writeObject(game);
                s.flush();
                //System.out.println("after flush");
            } catch (Throwable ex) {
                Util.logEx(null, ex, "Save game failed");
                System.out.println(ex);
                showInfoWindow("Save failed");
            }
            return null;
        }

        public void done() {
            gui.cancelLoadSaveDialog();
        }
    }

    public void toMainMenu() {
        RobotTester.setWaitState(true); // tell Robot tester we are unresponsive
        setCurrentState(WS.get());
        if (args.hasOption(C.OPT_NAMED_GALAXY)) {
            game = new Game(args.getOptionValue(C.OPT_NAMED_GALAXY), 14);
            game.init(resources);
        } else {
            game = new Game(FN.S_GALAXY_GAL, 14);
            game.init(resources);
        }
        setGameReferences();
        initGui();
        pbem_gui.getDATAHashes();
        SU.setWindow(C.S_MAIN_MENU1);
        setCurrentState(MM1.get());
        RobotTester.setWaitState(false); // tell Robot tester we are online
    }

    /**
     * Initializes various aspects of gui. Called when ever game is initialized
     * beyond initialization at game startup.
     */
    private void initGui() {
        main_menu.initMainMenu();
    }

    /**
     * Sets game references for various gui and other objects. Called when ever
     * game is loaded or initialized beyond initialization at game startup.
     */
    private void setGameReferences() {
        space_map.setGame(game);
        planet_map.setGame(game);
        planet_window.setGame(game);
        space_window.setGame(game);
        unit_info_window.setGame(game);
        main_menu.setGame(game);
        combat_window.setGame(game);
        galactic_map.setGame(game);
        globe_map.setGame(game);
        galactic_map_cw.setGame(game);
        globe_map_cw.setGame(game);
        galactic_map_uiw.setGame(game);
        globe_map_uiw.setGame(game);
        build_panel.setGame(game);
        agora_autobuy_panel.setGame(game);
        tech_panel.setGame(game);
        tech_db_panel.setGame(game);
        manowitz_panel.setGame(game);
        resource_panel.setGame(game);
        build_city_panel.setGame(game);
        messages_window.setGame(game);
        pbem_gui.setPBEMRef(game);
        city_dialog.setGame(game);
        cargo_win.setGame(game);
        byzantium_ii_window.setGame(game);
        agora_window.setGame(game);
        house_window.setGame(game);
        diplomacy_selector.setGame(game);
        diplomacy_window.setGame(game);
        resolve_contract_dialog.setGame(game);
        galaxy_window.setGame(game);
        globe_window.setGame(game);
        State.setGameRef(game);
        Comp.setGame(game);
        game.initAI(false);
//        game.AIAfterLoad();
        game.setPath(null);
        game.setJumpPath(null);
    }

    /**
     * Displays a String message in a simple window. The message will have
     * automatic line breaks inserted.
     *
     * @param message
     */
    public void showInfoWindow(String message) {
        if (message == null) {
            message = "<null/not implemented yet>";
        }
        String s = setLineBreaks(message, (Font) UIManager.get("OptionPane.messageFont"));
        JOptionPane pane = new UtilG.PhoenixJOptionPane(s, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, null, null);
        JDialog dialog = pane.createDialog(this, null);
        // reposition to conform with existing tests
        Rectangle r = dialog.getBounds();
        dialog.setBounds(r.x, r.y - 9, r.width, r.height);
        // \reposition
        dialog.setVisible(true);
    }

    public void showDiplomacySelectorWindow() {
        diplomacy_selector.setWindowVisiblity(true);
    }

    public void showCombatStrategySelectorDialog(CombatStrategyPanel.Strategy[] strategy) {
        strategy_selector.setResponseArray(strategy);
        strategy_selector.setWindowVisiblity(true);
    }

    public String setLineBreaks(String text, Font font) {
        List<String> words = new ArrayList<>();
        String word = "";
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != ' ' && c != '\n' && c != '\r') {
                word += c;
            } else if (c == '\r') {
                //System.out.println("CR");
            } else {

                words.add(word);
                //System.out.println("word = " + word);
                word = "" + c;
                words.add(word);
                //System.out.println("word = " + word);
                word = "";
            }
        }
        if (word != null && !word.equalsIgnoreCase("")) {
            words.add(word);
        }
        // join words into lines that just fit on 4/5 of main window
        FontMetrics fm = this.getFontMetrics(font);
        ArrayList<String> lines = new ArrayList<>();
        int width = ws.main_window_width * 4 / 5;
        String line = "";
        for (int i = 0; i < words.size(); i++) {
            word = words.get(i);
            if (!word.equalsIgnoreCase("\n")) {
                if (fm.stringWidth(line + word) <= width) {
                    if (!word.equalsIgnoreCase(" ") || !line.equalsIgnoreCase("")) {
                        line += word;
                    }
                } else {
                    lines.add(line);
                    //System.out.println("line = " + line);
                    line = "";
                    line += word;
                }
            } else {
                if (fm.stringWidth(line + word) <= width) {
                    line += word;
                    lines.add(line);
                    //System.out.println("line = " + line);
                    line = "";

                } else {
                    lines.add(line);
                    //System.out.println("line = " + line);
                    line = word;
                    lines.add(line);
                    //System.out.println("line = " + line);
                    line = "";
                }
            }
        }
        if (!line.equalsIgnoreCase("")) {
            lines.add(line);
        }

        String ret_val = "";
        boolean linefeed = false;
        for (String string : lines) {
            if (linefeed) {
                ret_val += "\n";
            }
            ret_val += string;
            linefeed = true;
        }
        return ret_val;

    }

    public boolean showConfirmWindow(String message) {
        String s = setLineBreaks(message, (Font) UIManager.get("OptionPane.messageFont"));
        JOptionPane pane = new UtilG.PhoenixJOptionPane(s, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION, null, null, JOptionPane.NO_OPTION);
        JDialog dialog = pane.createDialog(this, null);
        // reposition to conform with existing tests
        Rectangle r = dialog.getBounds();
        dialog.setBounds(r.x, r.y - 9, r.width, r.height);
        // \reposition
        dialog.setVisible(true);
        int reply = JOptionPane.NO_OPTION;
        Object val = pane.getValue();
        if (val != null) {
            reply = (Integer) val;
        }
        return reply == JOptionPane.YES_OPTION;
    }

    /**
     * Show attack confirm window if hex has non-hostile units.
     *
     * @param my_faction
     * @param target_stack
     * @return
     */
    public boolean showAttackConfirmWindow(int my_faction, List<Unit> target_stack) {
        String msg = ". Shall we attack anyway?";
        boolean spotted = false;
        for (Unit u : target_stack) {
            if (u.spotted[my_faction]) {
                spotted = true;
                break;
            }
        }
        if (spotted) {
            msg = "My Lord, we are not at war with " + Util.factionNameDisplay(target_stack.get(0).owner)
                    + msg;
        } else {
            msg = "Unspotted units from " + Util.factionNameDisplay(target_stack.get(0).owner)
                    + " are here. We are not at war with them" + msg;
        }
        return showConfirmWindow(msg);
    }

    public void setMouseCursor(String cursor) {
        this.setCursor(resources.getCursor(cursor));
    }

    public Cursor createMouseCursor(String cursor_file, String cursor_name, boolean bullseye) {

        int wt;
        int ht;
        if (ws.is_double) {
            wt = 64;
            ht = 64;
        } else {
            wt = 32;
            ht = 32;
        }

        BufferedImage bi = new BufferedImage(wt, ht, BufferedImage.TYPE_BYTE_INDEXED, color_index);
        WritableRaster wr = bi.getRaster();
        int[] cursor_img = Util.loadSquare(cursor_file, 0, 32 * 32);
        if (ws.is_double) {
            cursor_img = Util.scale2XImage(cursor_img, 32 * 32, 32);
        }
        wr.setPixels(0, 0, wt, ht, cursor_img);
        BufferedImage alpha_img = new BufferedImage(wt, ht, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = alpha_img.createGraphics();
        g2.drawImage(bi, 0, 0, null);
        g2.dispose();
        WritableRaster alpha_wr = alpha_img.getRaster();
        int[] trans_color = new int[4];

        alpha_wr.getPixel(0, ht - 1, trans_color);

        trans_color[3] = 0;
        int[] pixel = new int[4];
        for (int i = 0; i < ht; i++) {
            for (int j = 0; j < wt; j++) {
                alpha_wr.getPixel(i, j, pixel);
                if (pixel[0] == trans_color[0]
                        && pixel[1] == trans_color[1]
                        && pixel[2] == trans_color[2]) {
                    alpha_wr.setPixel(i, j, trans_color);
                }

            }

        }

        Point focus = new Point(0, 0);
        if (bullseye) {
            focus.x = wt / 2;
            focus.y = ht / 2;
        }
        return Toolkit.getDefaultToolkit().createCustomCursor(alpha_img, focus, cursor_name);

    }

    public void enableBuildCityMenuItem(boolean enabled) {
        menu_build_city.setEnabled(enabled);
    }

    public void enableBuildRoadMenuItem(boolean enabled) {
        menu_build_road.setEnabled(enabled);
    }

    public void enableRazeCityMenuItem(boolean enabled) {
        menu_raze_city.setEnabled(enabled);
    }

    public void enableLaunchButton(boolean enabled) {
        launch_button.setEnabled(enabled);
    }

    private void setUpLaunchButton() {

        launch_button_enabled = new ButtonIcon(ws.launch_button_width, ws.launch_button_height, FN.S_EFSBUT_BIN[3], 0, color_index, ws);

        int file_offset = 3;
        launch_button_disabled = new ButtonIcon(ws.launch_button_width, ws.launch_button_height, FN.S_EFSBUT_BIN[3], file_offset, color_index, ws);
        file_offset = 2;
        ButtonIcon launch_button_pressed = new ButtonIcon(ws.launch_button_width, ws.launch_button_height, FN.S_EFSBUT_BIN[3], file_offset, color_index, ws);

        launch_button = new JButton();
        launch_button.setBorder(null);
        launch_button.setIcon(launch_button_enabled);
        launch_button.setDisabledIcon(launch_button_disabled);
        launch_button.setPressedIcon(launch_button_pressed);
        planet_window.add(launch_button);
        launch_button.setBounds(ws.launch_button_x_offset, ws.launch_button_y_offset,
                ws.launch_button_width, ws.launch_button_height);
        launch_button.setEnabled(false);
        launch_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                state.pressLaunchButton();
            }
        });
    }

    private void setUpSpaceButton() {

        ButtonIcon space_button_enabled = new ButtonIcon(ws.space_button_width, ws.space_button_height, FN.S_EFSBUT_BIN[2], 0, color_index, ws);

//        int file_offset = 3;
//        launch_button_disabled = new ButtonIcon(ws.launch_button_width, ws.launch_button_height, FN.S_EFSBUT_BIN[2], file_offset, color_index, ws);
        int file_offset = 2;
        ButtonIcon space_button_pressed = new ButtonIcon(ws.space_button_width, ws.space_button_height, FN.S_EFSBUT_BIN[2], file_offset, color_index, ws);

        space_button = new JButton();
        space_button.setBorder(null);
        space_button.setIcon(space_button_enabled);
//        space_button.setDisabledIcon(launch_button_disabled);
        space_button.setPressedIcon(space_button_pressed);
        planet_window.add(space_button);
        space_button.setBounds(ws.space_button_x_offset, ws.space_button_y_offset,
                ws.space_button_width, ws.space_button_height);
//        launch_button.setEnabled(false);
        space_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                state.pressSpaceButton();
            }
        });
    }

    public final void setMenus(String new_parent) {
        Container old_parent = menubar_holder.getParent();
        if (old_parent != null) {
            old_parent.remove(menubar_holder);
        }
        if (new_parent == null) {
            return;
        } else if (new_parent.equals(C.S_PLANET_MAP)) {
            planet_window.add(menubar_holder);
        } else if (new_parent.equals(C.S_STAR_MAP)) {
            space_window.add(menubar_holder);
        } else {
            throw new AssertionError("Wrong window " + new_parent);
        }
    }

    public void setDragUnit(Unit u, Point p) {
        drag_unit = u;
        drag_point = p;
    }

    public void setDragPoint(Point p) {
        drag_point = p;
    }

    public Unit getDragUnit() {
        return drag_unit;
    }

    public Point getDragPoint() {
        return drag_point;
    }

    public void showTooManyUnits() {
        showInfoWindow("Too many units in the destination area.");
    }

    public Color getColorCycleColor() {
        return color_cycle_color;
    }

    public void setColorCycle() {
        color_cycle_colors = new Color[5];
        for (int i = 0; i < color_cycle_colors.length; i++) {
            int cycle = 20 * i;
            color_cycle_colors[color_cycle_colors.length - i - 1] = new Color(60 + cycle, 80 + cycle, 160 + cycle);

        }
    }

    public void startStackMove() {
        Phoenix.setRealTime();
        setStack_move_counter(0);
        setStack_moving(true);
        setStop_stack(false);
        getStack_move_timer().start();
    }

    public void stopStackMove() {
        setStop_stack(true);
    }

    public int getStackMoveCounter() {
        return getStack_move_counter();
    }

    public boolean getStackMove() {
        return isStack_moving();
    }

    public boolean getAnimationBlink() {
        return animation_blink;
    }

    public void setDefaultUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Util.logEx(t, e);
                showInfoWindow("Uncaught exception has occurred and details have"
                        + " been written to phoenixlog.txt. This indicates a bug."
                        + " Please email a short description of where you were in"
                        + " the game and what you were doing and contents of"
                        + " phoenixlog.txt to joulupunikki@gmail.com and put \"PHOENIX BUG\""
                        + " in the subject line. You may continue playing the game"
                        + " but the game is possibly in an inconsistent state.");
            }
        });
    }

    public void setStateReferences() {
        State.setReferences(this, game, ws);
    }

    public static IndexColorModel getICM() {
        return color_index;
    }

    public IndexColorModel loadICM() {

        return new IndexColorModel(8, 256, pallette[2], pallette[1], pallette[0], 256);
    }

    public static int[][] getUnitIcons() {
        return unit_icons;
    }

    public static void loadHexTiles() {
        hex_tiles = new int[C.TILE_SETS][][];
        for (int i = 0; i < C.TILE_SETS; i++) {
            hex_tiles[i] = Util.loadHexTiles(FN.S_EFSTILE + i + FN.S__BIN, 134);

        }
    }

    public static void loadStructureTiles() {
        structures = new int[C.TILE_SETS][][];
        for (int i = 0; i < C.TILE_SETS; i++) {
            structures[i] = Util.loadHexTiles(FN.S_STRUCT + i + FN.S__BIN, 32);

        }
    }

    public static int[][] getHexTiles(int tile_set) {
        return hex_tiles[tile_set];
    }

    public static int[][] getStructureTiles(int tile_set) {
        return structures[tile_set];
    }

    public static WindowSize getWindowSize() {
        return ws;
    }

    public Game getGame() {
        return game;
    }

//    public static Game getGameS() {
//        return game_s;
//    }
//    
    public byte[][] getPallette() {
        return pallette;
    }

    public Messages getMessages() {
        return messages_window;
    }

    public JPanel getMainWindows() {
        return main_windows;
    }

    public AgoraWindow getAgoraWindow() {
        return agora_window;
    }

    public JPanel getPlanetWindow() {
        return planet_window;
    }

    public JPanel getPlanetMap() {
        return planet_map;
    }

    public JPanel getSpaceWindow() {
        return space_window;
    }

    public CombatWindow getCombatWindow() {
        return combat_window;
    }

    public void setCurrentState(State s) {
        System.out.println("State -> " + s.getClass().getName());
        state = s;
        SU.setStateUpKeep(s);
//        state_ref.setState(s);
    }

    public State getCurrentState() {
        return state;
//        return state_ref.getState();
    }

    public void setUpMainMenu() {
        main_menu.setDefaultHumanControl();
    }

    void setUpCustomSwingActions() {
        CustomSwingActions.setUpActions(gui);
        CustomSwingActions.setUpKeyBindings(planet_window);
        CustomSwingActions.setUpKeyBindings(space_window);
    }

    private static void createAndShowGUI() {
        gui = new Gui();
        gui.setUpCustomSwingActions();
        gui.setStateReferences();
//        gui.setDefaultUncaughtExceptionHandler();
        gui.setUpMainMenu();
        Rectangle r = gui.getBounds();
        System.out.println("JFrame bounds : " + r);
        System.out.println("Location on screen : " + gui.getLocationOnScreen());
        //gui.setBounds(0, 24, r.width, r.height);
        Phoenix.setGui(gui);
//        throw new AssertionError(); // for testing exception handler
        double init_time = ((System.nanoTime() - Phoenix.start_time)) / 1_000_000 / 1e3;
        System.out.println("Startup time = " + init_time + "s");
        System.out.println("Phoenix ready.");
        Long random_seed = Phoenix.start_time;   
        if (args.hasOption(C.OPT_RANDOM_SEED)) {
            System.out.println(args.getOptionValue(C.OPT_RANDOM_SEED));
            random_seed = Long.parseLong(args.getOptionValue(C.OPT_RANDOM_SEED)); // Fix #65
        }
        System.out.println("Random seed = " + random_seed);
        gui.getGame().getRandom().setSeed(random_seed);
        if (args.hasOption(C.OPT_ROBOT_TEST)) {
            System.out.println(""
                    + "Starting Robot test. Please leave test machine undisturbed until test is\n"
                    + "finished. If Robot test terminates abnormally, test machine OS may be left\n"
                    + "in unresponsive state.");
            RobotTester.startRobotTester(args.getOptionValue(C.OPT_ROBOT_TEST), args.getOptionValue(C.OPT_GAME_STATE_FILE), gui, gui.getX(), gui.getY());
        } else if (getMainArgs().hasOption(C.OPT_AI_TEST)) {// do automated AI testing
            gui.game.setFactionPlayer(C.HOUSE1, true);
            gui.game.setFactionPlayer(C.SYMBIOT, false);
            gui.game.setFactionPlayer(C.STIGMATA, true);
            gui.game.beginGame();
            SU.selectNextUnmovedUnit();
            gui.game.endTurn();
        }
    }

    public static void execute(CommandLine args) {
        Gui.args = args;
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    public static void disposeGUI() {
        if (gui != null) {
            gui.dispose();
        }
    }

    /**
     * @return the stack_move_counter
     */
    public int getStack_move_counter() {
        return stack_move_counter;
    }

    /**
     * @param stack_move_counter the stack_move_counter to set
     */
    public void setStack_move_counter(int stack_move_counter) {
        this.stack_move_counter = stack_move_counter;
    }

    /**
     * @return the stop_stack
     */
    public boolean isStop_stack() {
        return stop_stack;
    }

    /**
     * @param stop_stack the stop_stack to set
     */
    public void setStop_stack(boolean stop_stack) {
        this.stop_stack = stop_stack;
    }

    /**
     * @return the stack_moving
     */
    public boolean isStack_moving() {
        return stack_moving;
    }

    /**
     * @param stack_moving the stack_moving to set
     */
    public void setStack_moving(boolean stack_moving) {
        this.stack_moving = stack_moving;
    }

    /**
     * @return the stack_move_timer
     */
    public Timer getStack_move_timer() {
        return stack_move_timer;
    }

    /**
     * @param stack_move_timer the stack_move_timer to set
     */
    public void setStack_move_timer(Timer stack_move_timer) {
        this.stack_move_timer = stack_move_timer;
    }

    /**
     * @return the info_unit
     */
    public Unit getInfo_unit() {
        return info_unit;
    }

    /**
     * @param info_unit the info_unit to set
     */
    public void setInfo_unit(Unit info_unit) {
        this.info_unit = info_unit;
    }

    public PBEMGui getPBEMGui() {
        return pbem_gui;
    }

    public static Point getOrigin() {
        Point p = new Point();
        if (gui != null) {
            p = gui.getLocationOnScreen();

        }
        return p;
    }

    public void showResolveContractDialog(Message msg) {
        resolve_contract_dialog.enterDialog(msg);
    }

    public EnumMap getGuiOpt() {
        return options_panel.getGuiOpt();
    }
}
