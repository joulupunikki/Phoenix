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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import util.C;
import util.Util;
import util.WindowSize;

/**
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class Manowitz extends JPanel {

    private Gui gui;
    private Game game;
    private WindowSize ws;

    private JTextArea right_page;
    private JTextArea left_page;

    // Manowitz fitting lines of the current chapter
    private List<String> lines;

    private int volume;
    private int chapter;
    // how many chapters there are listed in the current volume
    private int chapters;
    // spread/double page number of current chapter
    private int spread;
    // contents is displayed
    private final int CONTENTS = 1;
    // a chapter is displayed
    private final int CHAPTER = 2;
    // the state we are in eg. CONTENTS, CHAPTER
    private int state;

    public Manowitz(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
        game = gui.getGame();

        addLeftPage();
        addRightPage();
        setUpButtonListener();
        setUpPageListeners();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        String file = "MANOWITZ" + System.getProperty("file.separator") + "BOOK5H.PCX";
        File file_obj = new File(file);

        byte[][] pallette = Util.getPalletteFromPCX(file, (int) file_obj.length());
        BufferedImage bi = Util.loadImage(file, ws.is_double, pallette, 640, 480);

        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);

    }

    public void setUpPageListeners() {
        left_page.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (state != CONTENTS) {
                    return;
                }
                int row = selectRow(e);
                if (row > chapters) {
                    return;
                }
                setChapter(volume, row);
            }
        });
        right_page.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                FontMetrics fm = right_page.getFontMetrics(right_page.getFont());

                Rectangle r = right_page.getBounds();
                int rows = r.height / fm.getHeight();
                if (state != CONTENTS) {
                    return;
                }
                int row = selectRow(e) + rows;
                if (row > chapters) {
                    return;
                }
                setChapter(volume, row);
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
     * previous chapter.
     */
    public void pressPrev() {
        if (spread > 1) {
            spread--;
            setSpread(spread);
        } else {
            File prev_file = new File(getChapterName(chapter - 1));
            if (prev_file.exists()) {
                setChapter(volume, chapter - 1);
            }
        }
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
            File next_file = new File(getChapterName(chapter + 1));
            if (next_file.exists()) {
                setChapter(volume, chapter + 1);
            }
        }

    }

    /**
     * Show index of volume vol or current volume if vol == -1.
     *
     * @param vol
     */
    public void pressContents(int vol) {
        if (vol != -1) {
            this.volume = vol;
        }
        String file_name = "MANOWITZ" + System.getProperty("file.separator")
                + "VOLUME" + volume + ".TXT";
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
        state = CONTENTS;
        chapter = 0;
    }

    public String getChapterName(int chapter) {
        String sample = "V1CHP001.TXT";
        String manowitz = "MANOWITZ";
        String separ = System.getProperty("file.separator");
        String v = "V";
        String chp = "CHP";
        String zero = "0";
        String zerozero = "00";
        String txt = ".TXT";
        String file_name = manowitz + separ + v + this.volume + chp;
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
        String file_name = getChapterName(chapter);
        text = Util.readText(file_name);

        // for techs, insert names of prerequisite techs
        if (vol > 1) {
            String preqs = getPrerequisiteTechString(vol, chapter);
            System.out.println("preqs = " + preqs);
            text += preqs;
        }
        //break text into words, spaces and newlines
        List<String> words = new ArrayList<>();
        String word = "";
        Pattern ref_pat = Pattern.compile("<<(bkil)|(BKIL)[0-9]+\\.(pcx)|(PCX)>>");

        boolean after_ref = false;
        boolean image_ref = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            //skip possible image reference on first line
            if (!after_ref) {
                Matcher m = ref_pat.matcher(word);
                if (m.find()) {
                    image_ref = true;
                    word = "";
                }
                if (image_ref == true && c != '\n') {
                    continue;
                } else if (image_ref == true && c == '\n') {
                    after_ref = true;
                    continue;
                } else if (c == '\n') {
                    after_ref = true;
                }
            }

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
        state = CHAPTER;
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

    /*
     * Finds those currently researchable technologies which are prerequisites
     * to current chapter (technology) or technologies leading towards current
     * chapter.
     */
    /**
     *
     * @param vol
     * @param chapter
     * @return
     */
    public String getPrerequisiteTechString(int vol, int chapter) {
        String ret_val = null;
        LinkedList<Tech> preqs = findPrerequisiteTechs(vol, chapter);
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
     * Finds all the prerequisite techs for the tech listed in vol, chapter of
     * Manowitz.
     *
     * @param vol
     * @param chapter
     * @return
     */
    public LinkedList<Tech> findPrerequisiteTechs(int vol, int chapter) {
        Tech[] techs = game.getResources().getTech();
        boolean[] owned = game.getFaction(game.getTurn()).getResearch().techs;
        Tech tech = null;
        for (Tech t : techs) {
            if (t.stats[C.TECH_VOL] == vol && t.stats[C.TECH_CH] == chapter) {
                tech = t;
                break;
            }
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
