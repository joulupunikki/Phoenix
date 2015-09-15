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
package util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.plaf.metal.DefaultMetalTheme;

/**
 * Graphics related utilities.
 * <p>
 * Methods for getting children of JComponents named
 * <pre> XXX getDescendantsOfType(YYY) </pre> are from
 * <a href=https://tips4java.wordpress.com/2008/11/13/swing-utils/>SwingUtils</a>
 * by Darryl Burke.
  * 
 *
 * @author joulupunikki
 */
public class UtilG {

    public static int[] scaleColorsToDark(double r, byte[][] rgb_data) {
        int[] ret_val = new int[rgb_data[0].length];
        double[][] rgb_val = new double[rgb_data[0].length][rgb_data.length];
        for (int i = 0; i < rgb_data[0].length; i++) {
            for (int j = 0; j < rgb_data.length; j++) {
                rgb_val[i][j] = (rgb_data[2 - j][i] & 0xff); // / 255;

            }

        }

        for (int i = 0; i < rgb_val.length; i++) {
            double[] a = rgb_val[i];

            double[] d = new double[3];
            System.arraycopy(a, 0, d, 0, a.length);
            vecMulScalar(d, r);

            int index = 0;
            double shortest = Double.POSITIVE_INFINITY;
            double distance = Double.POSITIVE_INFINITY;
            for (int j = 0; j < rgb_val.length; j++) {
                distance = distBetwPoints(d, rgb_val[j]);
                if (distance < shortest) {
                    shortest = distance;
                    index = j;
                }
            }
            ret_val[i] = index;

        }
        return ret_val;
    }

    public static void writeStruct(int x, int y, int[] pixel_data,
            int[][] structures, int city_no, WritableRaster wr, WindowSize ws) {

        int dx = 0;
        int dy = 0;

        for (int i = 0; i < C.STRUCT_BIN_HEIGHT; i++) {
            for (int j = 0; j < C.STRUCT_BIN_WIDTH; j++) {

                dx = x + j;
                dy = y + i;

                writePixel(dx, dy, i * C.STRUCT_BIN_WIDTH + j,
                        pixel_data, structures, city_no, wr, ws);

            }
        }
    }

    public static void writePixel(int x, int y, int t_idx, int[] pixel_data, int[][] structures,
            int city_no, WritableRaster wr, WindowSize ws) {

        pixel_data[0] = structures[city_no][t_idx];

        if (pixel_data[0] != 0) {
            // if double window size scale image
            if (ws.is_double) {
                wr.setPixel(2 * x, 2 * y, pixel_data);
                wr.setPixel(2 * x + 1, 2 * y, pixel_data);
                wr.setPixel(2 * x, 2 * y + 1, pixel_data);
                wr.setPixel(2 * x + 1, 2 * y + 1, pixel_data);
            } else {
                wr.setPixel(x, y, pixel_data);
            }
        }
    }

    /**
     * Multiplies vector v with scalar c
     *
     * @param v
     * @param c
     */
    public static void vecMulScalar(double[] v, double c) {
        for (int i = 0; i < v.length; i++) {
            v[i] = c * v[i];
        }
    }

    /**
     * Subtracts vector b from vector a
     *
     * @param a
     * @param b
     * @return
     */
    public static double[] vecSub(double[] a, double[] b) {
        double[] d = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            d[i] = a[i] - b[i];
        }
        return d;
    }

    /**
     * Adds vector b to vector a
     *
     * @param a
     * @param b
     * @return
     */
    public static double[] vecAdd(double[] a, double[] b) {
        double[] d = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            d[i] = a[i] + b[i];
        }
        return d;
    }

    /**
     * Calculates distance of point p to unit line (x=y=z)
     *
     * @param p
     * @return
     */
    public static double distPointUnitLine(double[] p) {
        double d = Math.pow(p[0] - p[1], 2)
                + Math.pow(p[1] - p[2], 2)
                + Math.pow(p[2] - p[0], 2);

        d = d / 3;
        d = Math.sqrt(d);
        return d;
    }

    public static double[] closestPointPandUL2(double[] p) {
        double[] ret_val = new double[p.length];
        double d = 0;
        for (int i = 0; i < ret_val.length; i++) {
            d += p[i];
        }
        d = d / 3;
        for (int i = 0; i < ret_val.length; i++) {
            ret_val[i] = d;
        }
        return ret_val;
    }

    /**
     * Closest point on unit line (x=y=z) to point p.
     *
     * @param p
     * @return
     */
    public static double[] closestPointPandUL(double[] p) {
        double[] q = new double[p.length];
        double a = 3;
        double b = -2 * (p[0] + p[1] + p[2]);
        double c = -distPointUnitLine(p);
        System.out.println("c = " + c);

        for (int i = 0; i < q.length; i++) {
            c += Math.pow(p[i], 2);
        }
        double[] r = solveQuadratic(a, b, c);
        double[] x0 = {r[0], r[0], r[0]};
        double[] x1 = {r[1], r[1], r[1]};
        System.out.print("x0 = ");
        for (int j = 0; j < x0.length; j++) {
            System.out.print(x0[j] + " ");

        }
        System.out.println("");
        System.out.print("x1 = ");
        for (int j = 0; j < x1.length; j++) {
            System.out.print(x1[j] + " ");

        }
        System.out.println("");
        System.exit(0);
        if (0 < (distBetwPoints(p, x0) - distBetwPoints(p, x1))) {
            q = x0;
        } else {
            q = x1;
        }

        return q;
    }

    /**
     * Calculates distance between points p and q
     *
     * @param p
     * @param q
     * @return
     */
    public static double distBetwPoints(double[] p, double[] q) {
        double d = 0;
        for (int i = 0; i < q.length; i++) {
            d += Math.pow(p[i] - q[i], 2);
        }
        return Math.sqrt(d);
    }

    /**
     * Solves quadratic ax^2 + bx + c = 0
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    public static double[] solveQuadratic(double a, double b, double c) {
        double[] p = new double[2];

        double d = Math.sqrt(Math.pow(b, 2) - 4 * a * c);
        p[0] = (-b + d) / (2 * a);
        p[1] = (-b - d) / (2 * a);

        return p;
    }

    public static void setDialogLocation(JDialog dialog, Component component) {
        Dimension d_size = dialog.getSize();
        Dimension c_size = component.getSize();
        dialog.setLocation((c_size.width - d_size.width) / 2, (c_size.height - d_size.height) / 2);
    }

    public static void drawFrameRect(Graphics g, int x, int y, int w, int h) {
        g.setColor(C.COLOR_GOLD_DARK);
        g.drawLine(x - 1, y + h, x + w, y + h);
        g.drawLine(x + w, y - 1, x + w, y + h);
        g.setColor(C.COLOR_GOLD_BRIGHT);
        g.drawLine(x - 1, y - 1, x + w, y - 1);
        g.drawLine(x - 1, y - 1, x - 1, y + h);
    }

    public static void drawStringGrad(Graphics2D g2d, String s, Font f, int x, int y) {
        drawStringGrad(g2d, s, f, x, y, 0);
    }

    public static void drawStringGrad(Graphics2D g2d, String s, Font f, int x, int y, int border) {
        //gradient font test
//            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//                    RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(f);
        int line_height = f.getSize();
        GradientPaint gp = new GradientPaint(0, y - line_height + 1, C.COLOR_GOLD_BRIGHT, 0, y, C.COLOR_GOLD_DARK, true);
        if (border > 0) {
            drawStringBorder(g2d, s, Color.BLACK, x, y, border);
        }
        g2d.setPaint(gp);
        g2d.drawString(s, x, y);
    }

    public static void drawStringBorder(Graphics g, String s, Color c, int x, int y, int w) {
        g.setColor(c);
        for (; w > 0; w--) {
            g.drawString(s, x - w, y);
            g.drawString(s, x, y - w);
            g.drawString(s, x + w, y);
            g.drawString(s, x, y + w);
        }

    }

    /**
     * Convenience method for searching below <code>container</code> in the
     * component hierarchy and return nested components that are instances of
     * class <code>clazz</code> it finds. Returns an empty list if no such
     * components exist in the container.
     * <P>
     * Invoking this method with a class parameter of JComponent.class will
     * return all nested components.
     * <P>
     * This method invokes getDescendantsOfType(clazz, container, true)
     * <p>
     * Note: This method should be called under AWT tree lock.
     *
     * @param <T>
     * @param clazz the class of components whose instances are to be found.
     * @param container the container at which to begin the search
     * @return the List of components
     */
    public static <T extends JComponent> List<T> getDescendantsOfType(
            Class<T> clazz, Container container) {
        return getDescendantsOfType(clazz, container, true);
    }

    /**
     * Convenience method for searching below <code>container</code> in the
     * component hierarchy and return nested components that are instances of
     * class <code>clazz</code> it finds. Returns an empty list if no such
     * components exist in the container.
     * <P>
     * Invoking this method with a class parameter of JComponent.class will
     * return all nested components.
     * <p>
     * Note: This method should be called under AWT tree lock.
     *
     * @param <T>
     * @param clazz the class of components whose instances are to be found.
     * @param container the container at which to begin the search
     * @param nested true to list components nested within another listed
     * component, false otherwise
     * @return the List of components
     */
    public static <T extends JComponent> List<T> getDescendantsOfType(
            Class<T> clazz, Container container, boolean nested) {
        List<T> tList = new ArrayList<>();
        for (Component component : container.getComponents()) {
            if (clazz.isAssignableFrom(component.getClass())) {
                tList.add(clazz.cast(component));
            }
            if (nested || !clazz.isAssignableFrom(component.getClass())) {
                tList.addAll(UtilG.<T>getDescendantsOfType(clazz, (Container) component, nested));
         }
      }
      return tList;
    }

    public static void setJComponentChildrenToDark(JComponent chooser) {
        List<JComponent> c_list;
        //tried this with 1 ms delay
        Util.DeadLockGuard guard = Util.getDeadLockGuard(1, chooser);
        guard.start();
        synchronized (chooser.getTreeLock()) {
            c_list = UtilG.getDescendantsOfType(JComponent.class, chooser);
        }
        guard.stopGuard();
        System.out.println("# of " + chooser.getClass().getCanonicalName() + " children : " + c_list.size());
        for (JComponent jc : c_list) {
            jc.setBackground(Color.BLACK);
            if (jc instanceof JPanel) {
                jc.setBackground(Color.DARK_GRAY);
            }
            jc.setForeground(C.COLOR_GOLD);
        }
    }

    public static void setUIDefaults() {
        UIManager.put("OptionPane.background", Color.DARK_GRAY);
        UIManager.put("OptionPane.foreground", C.COLOR_GOLD);
        UIManager.put("Panel.background", Color.DARK_GRAY);
        UIManager.put("Panel.foreground", C.COLOR_GOLD);
        UIManager.put("OptionPane.messageForeground", C.COLOR_GOLD);
        UIManager.put("Button.background", Color.BLACK);
        UIManager.put("Button.foreground", C.COLOR_GOLD);
        UIManager.put("Button.border", new BorderUIResource(new LineBorder(C.COLOR_GOLD)));
        UIManager.put("Dialog.background", Color.DARK_GRAY);
        UIManager.put("Dialog.foreground", C.COLOR_GOLD);
        UIManager.put("ProgressBar.foreground", C.COLOR_GOLD);
        UIManager.put("ProgressBar.background", Color.DARK_GRAY);
        UIManager.put("MenuItem.background", Color.DARK_GRAY);
        UIManager.put("MenuItem.foreground", C.COLOR_GOLD);
        UIManager.put("MenuItem.border", new BorderUIResource(new LineBorder(Color.DARK_GRAY, 0)));
        UIManager.put("Menu.background", Color.DARK_GRAY);
        UIManager.put("Menu.foreground", C.COLOR_GOLD);
        UIManager.put("Menu.border", new BorderUIResource(new LineBorder(Color.DARK_GRAY, 0)));
        UIManager.put("TextArea.background", Color.BLACK);
        UIManager.put("TextArea.foreground", C.COLOR_GOLD);
        UIManager.put("TextField.border", new BorderUIResource(new LineBorder(Color.DARK_GRAY, 0)));
        UIManager.put("TextField.background", Color.BLACK);
        UIManager.put("TextField.foreground", C.COLOR_GOLD);
        UIManager.put("Label.foreground", C.COLOR_GOLD);
//        UIManager.put("Slider.foreground", C.COLOR_GOLD);
//        UIManager.put("Slider.background", C.COLOR_GOLD);
//        UIManager.put("Slider.focus", C.COLOR_GOLD);
//        UIManager.put("Slider.highlight", C.COLOR_GOLD);
//        UIManager.put("Slider.thumb", C.COLOR_GOLD);
//        UIManager.put("Slider.background", C.COLOR_GOLD);
        UIManager.put("Slider.foreground", C.COLOR_GOLD);
        UIManager.put("Slider.focus", C.COLOR_GOLD);
        UIManager.put("Slider.highlight", C.COLOR_GOLD);
        UIManager.put("Slider.shadow", C.COLOR_GOLD);
        UIManager.put("Slider.background", C.COLOR_GOLD);
    }

    public static class DarkTheme extends DefaultMetalTheme {

        public String getName() {
            return "DarkTheme";
        }

        private final ColorUIResource primary1 = new ColorUIResource(255, 255, 0);
        private final ColorUIResource primary2 = new ColorUIResource(0, 255, 255);
        private final ColorUIResource primary3 = new ColorUIResource(255, 0, 255);
        private final ColorUIResource menuSelectedBackground = new ColorUIResource(C.COLOR_GOLD);
        private final ColorUIResource menuSelectedForeground = new ColorUIResource(getBlack());
        private final ColorUIResource separatorBackground = new ColorUIResource(C.COLOR_GOLD_DARK);
        private final ColorUIResource separatorForeground = new ColorUIResource(getBlack());
        protected ColorUIResource getPrimary1() {
            return primary1;
        }

        protected ColorUIResource getPrimary2() {
            return primary2;
        }

        protected ColorUIResource getPrimary3() {
            return primary3;
        }

        @Override
        public ColorUIResource getMenuSelectedBackground() {
            return menuSelectedBackground; //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ColorUIResource getMenuSelectedForeground() {
            return menuSelectedForeground; //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ColorUIResource getSeparatorBackground() {
            return separatorBackground; //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ColorUIResource getSeparatorForeground() {
            return separatorForeground; //To change body of generated methods, choose Tools | Templates.
        }


    }

    /**
     * http://www.coderanch.com/t/338457/GUI/java/JSlider-knob-color
     */
    public static class CustomSliderUI extends BasicSliderUI {

        Color thumb_color;
        GradientPaint gp; // will be set just in time for painting

        public CustomSliderUI(JSlider s, Color c) {
            super(s);
            thumb_color = c;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            recalculateIfInsetsChanged();
            recalculateIfOrientationChanged();
            Rectangle clip = g.getClipBounds();

            if (slider.getPaintTrack() && clip.intersects(trackRect)) {
                paintTrack(g);
            }
            if (slider.getPaintTicks() && clip.intersects(tickRect)) {
                paintTicks(g);
            }
            if (slider.getPaintLabels() && clip.intersects(labelRect)) {
                paintLabels(g);
            }
            if (slider.hasFocus() && clip.intersects(focusRect)) {
                paintFocus(g);
            }
            if (clip.intersects(thumbRect)) {
                Color tmp = slider.getBackground();
                slider.setBackground(thumb_color);
                paintThumb(g);
                slider.setBackground(tmp);
            }
        }

        @Override
        public void paintThumb(Graphics g) {
            Rectangle knobBounds = thumbRect;
            int w = knobBounds.width;
            int h = knobBounds.height;
            if (gp == null) {
                int y = knobBounds.y;
                gp = new GradientPaint(0, y, C.COLOR_GOLD_BRIGHT, 0, y - h + 1, C.COLOR_GOLD_DARK, true);
            }
            g.translate(knobBounds.x, knobBounds.y);

            if (slider.isEnabled()) {
                g.setColor(slider.getBackground());
            } else {
                g.setColor(slider.getBackground().darker());
            }

            Boolean paintThumbArrowShape
                    = (Boolean) slider.getClientProperty("Slider.paintThumbArrowShape");

            if ((!slider.getPaintTicks() && paintThumbArrowShape == null)
                    || paintThumbArrowShape == Boolean.FALSE) {

                // "plain" version
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);

                g.setColor(Color.black);
                g.drawLine(0, h - 1, w - 1, h - 1);
                g.drawLine(w - 1, 0, w - 1, h - 1);

                g.setColor(getHighlightColor());
                g.drawLine(0, 0, 0, h - 2);
                g.drawLine(1, 0, w - 2, 0);

                g.setColor(getShadowColor());
                g.drawLine(1, h - 2, w - 2, h - 2);
                g.drawLine(w - 2, 1, w - 2, h - 3);
            } else if (slider.getOrientation() == JSlider.HORIZONTAL) {
                int cw = w / 2;
                g.fillRect(1, 1, w - 3, h - 1 - cw);
                Polygon p = new Polygon();
                p.addPoint(1, h - cw);
                p.addPoint(cw - 1, h - 1);
                p.addPoint(w - 2, h - 1 - cw);
                g.fillPolygon(p);

                g.setColor(getHighlightColor());
                g.drawLine(0, 0, w - 2, 0);
                g.drawLine(0, 1, 0, h - 1 - cw);
                g.drawLine(0, h - cw, cw - 1, h - 1);

                g.setColor(Color.black);
                g.drawLine(w - 1, 0, w - 1, h - 2 - cw);
                g.drawLine(w - 1, h - 1 - cw, w - 1 - cw, h - 1);

                g.setColor(getShadowColor());
                g.drawLine(w - 2, 1, w - 2, h - 2 - cw);
                g.drawLine(w - 2, h - 1 - cw, w - 1 - cw, h - 2);
//            } else {  // vertical
//                int cw = h / 2;
//                if (BasicGraphicsUtils.isLeftToRight(slider)) {
//                    g.fillRect(1, 1, w - 1 - cw, h - 3);
//                    Polygon p = new Polygon();
//                    p.addPoint(w - cw - 1, 0);
//                    p.addPoint(w - 1, cw);
//                    p.addPoint(w - 1 - cw, h - 2);
//                    g.fillPolygon(p);
//
//                    g.setColor(highlightColor);
//                    g.drawLine(0, 0, 0, h - 2);                  // left
//                    g.drawLine(1, 0, w - 1 - cw, 0);                 // top
//                    g.drawLine(w - cw - 1, 0, w - 1, cw);              // top slant
//
//                    g.setColor(Color.black);
//                    g.drawLine(0, h - 1, w - 2 - cw, h - 1);             // bottom
//                    g.drawLine(w - 1 - cw, h - 1, w - 1, h - 1 - cw);        // bottom slant
//
//                    g.setColor(shadowColor);
//                    g.drawLine(1, h - 2, w - 2 - cw, h - 2);         // bottom
//                    g.drawLine(w - 1 - cw, h - 2, w - 2, h - cw - 1);     // bottom slant
//                } else {
//                    g.fillRect(5, 1, w - 1 - cw, h - 3);
//                    Polygon p = new Polygon();
//                    p.addPoint(cw, 0);
//                    p.addPoint(0, cw);
//                    p.addPoint(cw, h - 2);
//                    g.fillPolygon(p);
//
//                    g.setColor(highlightColor);
//                    g.drawLine(cw - 1, 0, w - 2, 0);             // top
//                    g.drawLine(0, cw, cw, 0);                // top slant
//
//                    g.setColor(Color.black);
//                    g.drawLine(0, h - 1 - cw, cw, h - 1);         // bottom slant
//                    g.drawLine(cw, h - 1, w - 1, h - 1);           // bottom
//
//                    g.setColor(shadowColor);
//                    g.drawLine(cw, h - 2, w - 2, h - 2);         // bottom
//                    g.drawLine(w - 1, 1, w - 1, h - 2);          // right
//                }
            }

            g.translate(-knobBounds.x, -knobBounds.y);
        }
    }
}
