/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import game.Game;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
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

    private JTable tech_table;
    private JTextArea right_page;
    private JTextArea left_page;

    private JButton tech_db;
    private JButton exit;

    public Manowitz(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
        game = gui.getGame();

        addLeftPage();
        addRightPage();
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

    public void setChapter(int vol, int chapter) {
        // read chapter text
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

        //break text into words, spaces and newlines
        String text = Util.readText(file_name);
        List<String> words = new ArrayList<>();
        String word = "";
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
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
        // join words into lines that just fit on the manowitz pages
        FontMetrics fm = right_page.getFontMetrics(right_page.getFont());
        List<String> lines = new ArrayList<>();
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
//                word = " ";
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
//        FontMetrics fm = right_page.getFontMetrics(right_page.getFont());
//        List<String> lines = new ArrayList<>();
//        Dimension d = right_page.getPreferredSize();
//        Rectangle r = right_page.getBounds();
//        String line = "";
//        for (int i = 0; i < text.length(); i++) {
//            char c = text.charAt(i);
//            if (c != '\n') {
//                if (fm.stringWidth(line + c) <= d.width) {
//                    line += c;
//                } else {
//                    lines.add(line);
//                    System.out.println("line = " + line);
//                    line = "" + c;
//                }
//            } else {
//                lines.add(line);
//                System.out.println("line = " + line);
//                line = "";
//            }
//        }

        // put lines into strings and set the pages
        int rows = r.height / fm.getHeight();
        System.out.println("r.height = " + r.height);
        System.out.println("fm.getHeight() = " + fm.getHeight());
        System.out.println("rows = " + rows);
        String left_text = "";
        int i = 0;
        for (; i < lines.size() && i < rows; i++) {
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
