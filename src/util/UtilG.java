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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.WritableRaster;
import javax.swing.JDialog;

/**
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
}
