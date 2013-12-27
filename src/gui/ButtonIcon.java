/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import javax.swing.Icon;
import util.Util;
import util.WindowSize;

/**
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
        int[] icon_data = Util.loadSquare(icon_file, pos_index * raw_width * raw_height, raw_width * raw_height);
        for (int i = 0; i < raw_width; i++) {
            for (int j = 0; j < raw_height; j++) {
                pixel_data[0] = icon_data[i + j * raw_width];
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