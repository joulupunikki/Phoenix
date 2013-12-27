/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

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

}
