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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import javax.swing.Icon;
import util.C;
import util.Util;
import util.UtilG;
import util.WindowSize;

/**
 * Given size, file name, and image position in file constructs an icon usable
 * with a button. Given negative image position will construct an icon with the
 * text of icon_file String on black backround.
 *
 * @author joulupunikki
 */
public class ButtonIcon implements Icon {

    private int width;
    private int height;
    BufferedImage icon_image;

    public ButtonIcon(int width, int height, String icon_file, int pos_index, IndexColorModel color_index, WindowSize ws) {
        this.width = width;
        this.height = height;
        int raw_width = width;
        int raw_height = height;
        if (ws.is_double) {
            raw_width /= 2;
            raw_height /= 2;
        }
        int[] pixel_data = new int[1];
        icon_image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, color_index);
        WritableRaster wr = icon_image.getRaster();
        int[] icon_data = null;
        if (pos_index < 0) {
            Graphics2D g = (Graphics2D) icon_image.getGraphics();
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);
            UtilG.drawStringTextured(g, icon_file, ws.font_menu, 2 * ws.d, 4 * height / 6, 0, false);
            return;
        }
        if (icon_file != null) {
            icon_data = Util.loadSquare(icon_file, pos_index * raw_width * raw_height, raw_width * raw_height);
        }
        for (int i = 0; i < raw_width; i++) {
            for (int j = 0; j < raw_height; j++) {
                if (icon_file != null) {
                    pixel_data[0] = icon_data[i + j * raw_width];
                } else {
                    pixel_data[0] = C.INDEX_COLOR_EFS_BLACK;
                }
                if (ws.is_double) {
                    wr.setPixel(2 * i, 2 * j, pixel_data);
                    wr.setPixel(2 * i + 1, 2 * j, pixel_data);
                    wr.setPixel(2 * i, 2 * j + 1, pixel_data);
                    wr.setPixel(2 * i + 1, 2 * j + 1, pixel_data);
                } else {
                    wr.setPixel(i, j, pixel_data);
                }

            }
        }
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.drawImage(icon_image, null, 0, 0);
        g2d.dispose();
    }

    public int getIconWidth() {
        return width;
    }

    public int getIconHeight() {
        return height;
    }
}
