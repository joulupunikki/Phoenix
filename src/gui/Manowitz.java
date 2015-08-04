/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import dat.Tech;
import game.Game;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import util.C;
import util.FN;
import util.Util;
import util.WindowSize;

/**
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class Manowitz extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Gui gui;
    private Game game;
    private WindowSize ws;

    private byte[][] pallette;
    private IndexColorModel color_index;

    private JTextArea right_page;
    private JTextArea left_page;

    // Show contents button
    private JButton contents;
    // Previous page/spread button
    private JButton prev;
    // Next page/spread button
    private JButton next;
    // Exit/Close Book button
    private JButton close;

    // Manowitz fitting lines of the current chapter
    private List<String> lines;

    private int volume;
    // current chapter, if 0 then we are at index/contents
    private int chapter;
    // how many chapters there are listed in the current volume
    private int chapters;
    // spread/double page number of current chapter
    private int spread;
//    // contents is displayed
//    private final int CONTENTS = 1;
//    // a chapter is displayed
//    private final int CHAPTER = 2;
//    // the state we are in eg. CONTENTS, CHAPTER
//    private int state;

    // for tuning button locations
//    private int x_offset;
//    private Manowitz self_ref;
    public Manowitz(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
        game = gui.getGame();

        // Manowitz has unique pallette
        File file_obj = new File(FN.S_BOOK5H_PCX);
        pallette = Util.getPalletteFromPCX(FN.S_BOOK5H_PCX, (int) file_obj.length());
        color_index = new IndexColorModel(8, 256, pallette[2], pallette[1], pallette[0], 256);

        addLeftPage();
        addRightPage();
//        setUpButtonListener();
        setUpPageListeners();
        setUpContentsButton();
        setUpPrevButton();
        setUpNextButton();
        setUpCloseButton();
//        setUpPosSliders();

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage bi = Util.loadImage(FN.S_BOOK5H_PCX, ws.is_double, pallette, 640, 480);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);
    }

//    public void setUpPosSliders() {
//        //Create the slider
//        JSlider x_slider = new JSlider(JSlider.VERTICAL,
//                -30, 30, 0);
//        x_slider.addChangeListener(new ChangeListener() {
//            public void stateChanged(ChangeEvent e) {
//                JSlider source = (JSlider) e.getSource();
//                if (!source.getValueIsAdjusting()) {
//                    int value = (int) source.getValue();
//
////                    x_offset = ws.manowitz_prev_x_offset + value;
//                    x_offset = ws.manowitz_close_x_offset + value;
//                    close.setBounds(x_offset, ws.manowitz_close_y_offset,
//                            ws.manowitz_close_w, ws.manowitz_close_h);
//                    System.out.println("x_offset = " + x_offset);
//                }
//            }
//        });
//        x_slider.setMajorTickSpacing(10);
//        x_slider.setMinorTickSpacing(1);
//        x_slider.setPaintTicks(true);
//        this.add(x_slider);
//        x_slider.setBounds(100, 100, 100, 200);
//        x_slider.setPaintLabels(true);
//    }
    public void setGame(Game game) {
        this.game = game;
    }

    public void setUpCloseButton() {

//        JButton close;
        ButtonIcon close_enabled = new ButtonIcon(ws.manowitz_close_w, ws.manowitz_close_h, FN.S_CLOSE_BIN, 0, color_index, ws);

        int file_offset = 3;
        ButtonIcon close_disabled = new ButtonIcon(ws.manowitz_close_w, ws.manowitz_close_h, FN.S_CLOSE_BIN, file_offset, color_index, ws);
        file_offset = 2;
        ButtonIcon close_pressed = new ButtonIcon(ws.manowitz_close_w, ws.manowitz_close_h, FN.S_CLOSE_BIN, file_offset, color_index, ws);

        close = new JButton();
        close.setBorder(null);
        close.setIcon(close_enabled);
        close.setDisabledIcon(close_disabled);
        close.setPressedIcon(close_pressed);
        this.add(close);
        close.setBounds(ws.manowitz_close_x_offset, ws.manowitz_close_y_offset,
                ws.manowitz_close_w, ws.manowitz_close_h);
        close.setEnabled(true);
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.closeManowitz();
            }
        });
    }

    public void setUpNextButton() {

//        JButton next;
        ButtonIcon next_enabled = new ButtonIcon(ws.manowitz_next_w, ws.manowitz_next_h, FN.S_NEXT_BIN, 0, color_index, ws);

        int file_offset = 3;
        ButtonIcon next_disabled = new ButtonIcon(ws.manowitz_next_w, ws.manowitz_next_h, FN.S_NEXT_BIN, file_offset, color_index, ws);
        file_offset = 2;
        ButtonIcon next_pressed = new ButtonIcon(ws.manowitz_next_w, ws.manowitz_next_h, FN.S_NEXT_BIN, file_offset, color_index, ws);

        next = new JButton();
        next.setBorder(null);
        next.setIcon(next_enabled);
        next.setDisabledIcon(next_disabled);
        next.setPressedIcon(next_pressed);
        this.add(next);
        next.setBounds(ws.manowitz_next_x_offset, ws.manowitz_next_y_offset,
                ws.manowitz_next_w, ws.manowitz_next_h);
        next.setEnabled(true);
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pressNext();
            }
        });
    }

    public void setUpPrevButton() {

//        JButton prev;
        ButtonIcon previous_enabled = new ButtonIcon(ws.manowitz_prev_w, ws.manowitz_prev_h, FN.S_PREV_BIN, 0, color_index, ws);

        int file_offset = 3;
        ButtonIcon previous_disabled = new ButtonIcon(ws.manowitz_prev_w, ws.manowitz_prev_h, FN.S_PREV_BIN, file_offset, color_index, ws);
        file_offset = 2;
        ButtonIcon previous_pressed = new ButtonIcon(ws.manowitz_prev_w, ws.manowitz_prev_h, FN.S_PREV_BIN, file_offset, color_index, ws);

        prev = new JButton();
        prev.setBorder(null);
        prev.setIcon(previous_enabled);
        prev.setDisabledIcon(previous_disabled);
        prev.setPressedIcon(previous_pressed);
        this.add(prev);
        prev.setBounds(ws.manowitz_prev_x_offset, ws.manowitz_prev_y_offset,
                ws.manowitz_prev_w, ws.manowitz_prev_h);
        prev.setEnabled(true);
        prev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pressPrev();
            }
        });
    }

    public void setUpContentsButton() {

        ButtonIcon contents_enabled = new ButtonIcon(ws.manowitz_contents_w, ws.manowitz_contents_h, FN.S_CONTENTS_BIN, 0, color_index, ws);

        int file_offset = 3;
        ButtonIcon contents_disabled = new ButtonIcon(ws.manowitz_contents_w, ws.manowitz_contents_h, FN.S_CONTENTS_BIN, file_offset, color_index, ws);
        file_offset = 2;
        ButtonIcon contents_pressed = new ButtonIcon(ws.manowitz_contents_w, ws.manowitz_contents_h, FN.S_CONTENTS_BIN, file_offset, color_index, ws);

        contents = new JButton();
        contents.setBorder(null);
        contents.setIcon(contents_enabled);
        contents.setDisabledIcon(contents_disabled);
        contents.setPressedIcon(contents_pressed);
        this.add(contents);
        contents.setBounds(ws.manowitz_contents_x_offset, ws.manowitz_contents_y_offset,
                ws.manowitz_contents_w, ws.manowitz_contents_h);
        contents.setEnabled(true);
        contents.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pressContents(-1);
            }
        });
    }

    public void setUpPageListeners() {
        left_page.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (chapter != 0) {
                    return;
                }
                int row = selectRow(e);
                if (row > chapters) {
                    return;
                }
                setChapter(volume, row);
                setState();
            }
        });
        right_page.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                FontMetrics fm = right_page.getFontMetrics(right_page.getFont());

                Rectangle r = right_page.getBounds();
                int rows = r.height / fm.getHeight();
                if (chapter != 0) {
                    return;
                }
                int row = selectRow(e) + rows;
                if (row > chapters) {
                    return;
                }
                setChapter(volume, row);
                setState();
            }
        });
    }

    public int selectRow(MouseEvent e) {
        int selected_row = -1;
        Point p = e.getPoint();
        System.out.println("Manowitz left page(x,y): " + p.x + ", " + p.y);
        FontMetrics fm = right_page.getFontMetrics(right_page.getFont());
        selected_row = p.y / fm.getHeight() + 1;
        return selected_row;

    }

    public void setUpButtonListener() {
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
//                clickOnPlanetMap(e);
                Point p = e.getPoint();
                System.out.println("Manowitz (x,y): " + p.x + ", " + p.y);

                if (p.x > ws.manowitz_contents_x_offset
                        && p.x < ws.manowitz_contents_x_offset + ws.manowitz_contents_w
                        && p.y > ws.manowitz_contents_y_offset
                        && p.y < ws.manowitz_contents_y_offset + ws.manowitz_contents_h) {
                    System.out.println("Contents");
                    pressContents(-1);
                }
                if (p.x > ws.manowitz_prev_x_offset
                        && p.x < ws.manowitz_prev_x_offset + ws.manowitz_prev_w
                        && p.y > ws.manowitz_prev_y_offset
                        && p.y < ws.manowitz_prev_y_offset + ws.manowitz_prev_h) {
                    System.out.println("Prev");
                    pressPrev();
                }
                if (p.x > ws.manowitz_next_x_offset
                        && p.x < ws.manowitz_next_x_offset + ws.manowitz_next_w
                        && p.y > ws.manowitz_next_y_offset
                        && p.y < ws.manowitz_next_y_offset + ws.manowitz_next_h) {
                    System.out.println("Next");
                    pressNext();
                }

                if (p.x > ws.manowitz_close_x_offset
                        && p.x < ws.manowitz_close_x_offset + ws.manowitz_close_w
                        && p.y > ws.manowitz_close_y_offset
                        && p.y < ws.manowitz_close_y_offset + ws.manowitz_close_h) {
                    System.out.println("Close");
                    gui.closeManowitz();
                }

            }
        });
    }

    /**
     * Go back one spread, or if on spread 1 of current chapter, skip to
     * beginning of previous chapter.
     */
    public void pressPrev() {
        if (spread > 1) {
            spread--;
            setSpread(spread);
        } else {
            File prev_file = new File(getChapterName(this.volume, chapter - 1));
            if (prev_file.exists()) {
                setChapter(volume, chapter - 1);
            } else if (chapter == 1) {
                pressContents(-1);
            }
        }
        setState();
    }

    /**
     * Go forward one spread, or if on last spread of current chapter, skip to
     * next chapter.
     */
    public void pressNext() {
        FontMetrics fm = right_page.getFontMetrics(right_page.getFont());

        Rectangle r = right_page.getBounds();
        int rows = r.height / fm.getHeight();
        if (spread < (lines.size() - 1) / (2 * rows) + 1) {
            spread++;
            setSpread(spread);
        } else {
            File next_file = new File(getChapterName(this.volume, chapter + 1));
            if (next_file.exists()) {
                setChapter(volume, chapter + 1);
            }
        }
        setState();
    }

    /**
     * Set the state ie which buttons are enabled. Called after a button press
     * is processed except for close button.
     */
    public void setState() {
        FontMetrics fm = right_page.getFontMetrics(right_page.getFont());

        Rectangle r = right_page.getBounds();
        int rows = r.height / fm.getHeight();
        // set next button
        File next_file = new File(getChapterName(this.volume, chapter + 1));
        if ((spread < (lines.size() - 1) / (2 * rows) + 1)
                || (next_file.exists() && (chapter + 1 <= chapters))) {
            next.setEnabled(true);
        } else {
            next.setEnabled(false);
        }
        // set previous button
        if (spread > 1 || chapter > 0) {
            prev.setEnabled(true);
        } else {
            prev.setEnabled(false);
        }
        // set contents button
        if (chapter != 0) {
            contents.setEnabled(true);
        } else {
            contents.setEnabled(false);
        }
    }

    /**
     * Show index of volume vol or current volume if vol == -1.
     *
     * @param vol
     */
    public void pressContents(int vol) {
//        /*
//         Test code
//         */
//        System.out.println("Returning from Manowitz.pressContents");
//        if (vol >= -1) {
//            return;
//        }
//        /*
//         /Test code
//         */
        if (vol != -1) {
            this.volume = vol;
        }
        String file_name = FN.S_VOLUME + volume + FN.S__TXT;
        String text = Util.readText(file_name);

        // parse contents into lines of chapter names
        lines = new ArrayList<>();
        String word = "";
        boolean is_valid = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != '\n') {
                // only count lines with non-space characters
                if (c >= '0' && c <= 'z') {
                    is_valid = true;
                }
                word += c;
            } else {
                word += '\n';
                if (is_valid) {
                    lines.add(word);
                }
                is_valid = false;
                word = "";
            }
        }
        setSpread(1);
        chapters = lines.size();
        chapter = 0;
        setState();
    }

    /**
     * Set number of chapters to be number of lines in MANOWITZ/VOLUMEvol.TXT.
     *
     * @param vol
     */
    public void setNrChapters(int vol) {

        this.volume = vol;

        String file_name = FN.S_VOLUME + volume + FN.S__TXT;
        String text = Util.readText(file_name);

        // parse contents into lines of chapter names
        lines = new ArrayList<>();
        String word = "";
        boolean is_valid = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != '\n') {
                // only count lines with non-space characters
                if (c >= '0' && c <= 'z') {
                    is_valid = true;
                }
                word += c;
            } else {
                word += '\n';
                if (is_valid) {
                    lines.add(word);
                }
                is_valid = false;
                word = "";
            }
        }
        chapters = lines.size();
    }

    public boolean findChapter(int vol, int chapter) {
        boolean ret_val = false;
        File file = new File(getChapterName(vol, chapter));
        if (file.exists()) {
            ret_val = true;
        }
        return ret_val;
    }

    public String getChapterName(int vol, int chapter) {
        String sample = "V1CHP001.TXT";
        String manowitz = "MANOWITZ";
        String separ = System.getProperty("file.separator");
        String v = "V";
        String chp = "CHP";
        String zero = "0";
        String zerozero = "00";
        String txt = ".TXT";
        String file_name = manowitz + separ + v + vol + chp;
        if (chapter < 10) {
            file_name += zerozero;
        } else {
            file_name += zero;
        }
        file_name += chapter + txt;
        return file_name;
    }

    public void setChapter(int vol, int chapter) {
        this.volume = vol;
        this.chapter = chapter;
        // read chapter text
        String text = "";
        String file_name = getChapterName(this.volume, chapter);
        text = Util.readText(file_name);

        //skip possible image reference on first line
        Pattern ref_pat = Pattern.compile("<<[^(>>)]*>>");
        Matcher m = ref_pat.matcher(text);
        if (m.find()) {
            Pattern lf = Pattern.compile("\n");
            m = lf.matcher(text);
            m.find();
            String tmp = text.substring(m.end());
            text = tmp;
        }

        // for techs, insert names of prerequisite techs
        // do not skip wolfen and conclusion of vol 5 in vanilla EFS
        if (vol > 1) { // && (vol != 5 || (chapter != 25 && chapter != 47))) {
            String preqs = getPrerequisiteTechString(vol, chapter);
            System.out.println("preqs = " + preqs);
            text += preqs;
        }
        //break text into words, spaces and newlines
        List<String> words = new ArrayList<>();
        String word = "";
//        Pattern ref_pat = Pattern.compile("<<(bkil)|(BKIL)[0-9]+\\.(pcx)|(PCX)>>");

        boolean after_ref = false;
        boolean image_ref = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
//            //skip possible image reference on first line
//            if (!after_ref) {
//                Matcher m = ref_pat.matcher(word);
//                if (m.find()) {
//                    image_ref = true;
//                    word = "";
//                }
//                if (image_ref == true && c != '\n') {
//                    continue;
//                } else if (image_ref == true && c == '\n') {
//                    after_ref = true;
//                    continue;
//                } else if (c == '\n') {
//                    after_ref = true;
//                }
//            }

            if (c != ' ' && c != '\n' && c != '\r') {
                word += c;
            } else if (c == '\r') {
                System.out.println("CR");
            } else {

                words.add(word);
                System.out.println("word = " + word);
                word = "" + c;
                words.add(word);
                System.out.println("word = " + word);
                word = "";
            }
        }
        if (word != null && !word.equalsIgnoreCase("")) {
            words.add(word);
        }
        // join words into lines that just fit on the manowitz pages
        FontMetrics fm = right_page.getFontMetrics(right_page.getFont());
        lines = new ArrayList<>();
        Dimension d = right_page.getPreferredSize();
        Rectangle r = right_page.getBounds();
        String line = "";
        for (int i = 0; i < words.size(); i++) {
            word = words.get(i);
//            if (word.equalsIgnoreCase(text)) {
            if (!word.equalsIgnoreCase("\n")) {
                if (fm.stringWidth(line + word) <= d.width) {
                    if (!word.equalsIgnoreCase(" ") || !line.equalsIgnoreCase("")) {
                        line += word;
                    }
                } else {
                    lines.add(line);
                    System.out.println("line = " + line);
                    line = "";
//                    if (word != " ") {
                    line += word;
//                    }
                }
            } //            else {
            //                word = " ";
            //                while (fm.stringWidth(line + word) <= d.width) {
            //                    line += word;
            //                }
            //                lines.add(line);
            //                System.out.println("line = " + line);
            //                line = "";
            //            }
            else {
                if (fm.stringWidth(line + word) <= d.width) {
                    line += word;
                    lines.add(line);
                    System.out.println("line = " + line);
                    line = "";

                } else {
                    lines.add(line);
                    System.out.println("line = " + line);
                    line = word;
                    lines.add(line);
                    System.out.println("line = " + line);
                    line = "";
                }
            }
//            }
        }
        if (!line.equalsIgnoreCase("")) {
            lines.add(line);
        }

        setSpread(1);

    }

    /**
     * Sets the left and right pages of manowitz with the text contained in
     * lines.
     *
     */
    public void setSpread(int spread) {
        this.spread = spread;
        FontMetrics fm = right_page.getFontMetrics(right_page.getFont());

        Rectangle r = right_page.getBounds();
        int rows = r.height / fm.getHeight();
        System.out.println("r.height = " + r.height);
        System.out.println("fm.getHeight() = " + fm.getHeight());
        System.out.println("rows = " + rows);
        String left_text = "";
        int i = (spread - 1) * rows * 2;
        for (int j = 0; i < lines.size() && j < rows; i++, j++) {
            left_text += lines.get(i);
        }
        System.out.println("left_text = " + left_text);
        String right_text = "";
        for (int j = 0; i < lines.size() && j < rows; i++, j++) {
            right_text += lines.get(i);
        }
        System.out.println("right_text = " + right_text);
        left_page.setText(left_text);
        right_page.setText(right_text);
    }

//    /*
//     * Finds those currently researchable technologies which are prerequisites
//     * to current chapter (technology) or technologies leading towards current
//     * chapter.
//     */
    /**
     * Find prerequisite technologies for current chapter (technology). If preqs
     * == null return "".
     *
     * @param vol
     * @param chapter
     * @return
     */
    public String getPrerequisiteTechString(int vol, int chapter) {
        String ret_val = null;
        LinkedList<Tech> preqs = findPrerequisiteTechs(vol, chapter);
        if (preqs == null) {
            return "";
        }
        if (preqs.size() == 0) {
            ret_val = "\nThis technology is known.";
        } else if (preqs.size() == 1) {
            int tech_id = preqs.getFirst().idx;
            int researched = game.getFaction(game.getTurn()).getResearch().researched;
            if (tech_id == researched) {
                ret_val = "\nWe are currently researching this technology.";
            } else {
                ret_val = "\nWe can research this technology.";
            }
        } else {
            ret_val = "\nResearching ";
            Tech t = preqs.pop();
            ret_val += t.name;
            while (preqs.size() > 2) {
                t = preqs.pop();
                ret_val += ", " + t.name;
            }
            if (preqs.size() > 1) {
                t = preqs.pop();
                ret_val += " and " + t.name;
            }
            ret_val += " will lead us to this technology.";
        }
        return ret_val;

    }

    /**
     * Finds all the prerequisite techs for the tech listed in (vol, chapter) of
     * Manowitz. Returns null if vol,chapter pair is not found among techs.
     *
     * @param vol
     * @param chapter
     * @return
     */
    public LinkedList<Tech> findPrerequisiteTechs(int vol, int chapter) {
        Tech[] techs = game.getGameResources().getTech();
        boolean[] owned = game.getFaction(game.getTurn()).getResearch().techs;
        Tech tech = null;
        /*
         In hyperion applied tech and viper militia have the same (vol,chapter)
         values. To properly show preqs for viper militia techs need to be
         traversed in reverse order.
         */
//        for (Tech t : techs) {
        for (int i = techs.length - 1; i > -1; i--) {
            Tech t = techs[i];
            if (t.stats[C.TECH_VOL] == vol && t.stats[C.TECH_CH] == chapter) {
                tech = t;
                break;
            }
        }
        if (tech == null) {
            return null;
        }
        Set<Tech> included = new HashSet<>();
        LinkedList<Tech> preqs = new LinkedList<>();
        LinkedList<Tech> list = new LinkedList<>();
        list.push(tech);
        while (!list.isEmpty()) {
            Tech t = list.pollLast();
            if (!owned[t.idx]) {
                if (included.add(t)) {
                    preqs.addFirst(t);
                }
                if (t.stats[C.TECH0] < 800) {
                    list.push(techs[t.stats[C.TECH0]]);
                    list.push(techs[t.stats[C.TECH1]]);
                    list.push(techs[t.stats[C.TECH2]]);
                }
            }
        }
        // make it so that researchable techs are first in list
        for (Tech t : included) {
            if (t.stats[C.TECH0] >= 800
                    || (owned[t.stats[C.TECH0]]
                    && owned[t.stats[C.TECH1]]
                    && owned[t.stats[C.TECH2]])) {
                preqs.remove(t);
                preqs.addFirst(t);
            }
        }
        return preqs;
    }

    public void addLeftPage() {
        left_page = new JTextArea();
        left_page.setEditable(false);
//        JScrollPane tech_info_scroller = new JScrollPane(tech_info);
//        this.add(tech_info_scroller);
//        tech_info_scroller.setBounds(ws.tech_info_x_offset, ws.tech_info_y_offset,
//                ws.tech_info_w, ws.tech_info_h);
        this.add(left_page);
        left_page.setFont(ws.font_default);
        left_page.setForeground(C.COLOR_MANOWITZ_TEXT);
        left_page.setOpaque(false);
        left_page.setBounds(ws.left_page_x_offset, ws.left_page_y_offset,
                ws.left_page_w, ws.left_page_h);
        left_page.setLineWrap(true);
        left_page.setWrapStyleWord(true);
    }

    public void addRightPage() {
        right_page = new JTextArea();
        right_page.setEditable(false);
//        JScrollPane tech_info_scroller = new JScrollPane(tech_info);
//        this.add(tech_info_scroller);
//        tech_info_scroller.setBounds(ws.tech_info_x_offset, ws.tech_info_y_offset,
//                ws.tech_info_w, ws.tech_info_h);
        this.add(right_page);
        right_page.setFont(ws.font_default);
        right_page.setForeground(C.COLOR_MANOWITZ_TEXT);
        right_page.setOpaque(false);
        right_page.setBounds(ws.right_page_x_offset, ws.left_page_y_offset,
                ws.left_page_w, ws.left_page_h);
        right_page.setLineWrap(true);
        right_page.setWrapStyleWord(true);
    }
}
