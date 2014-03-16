/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package galaxyreader;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;

/**
 * A class representing a jump gate object. Contains coordinates of the jump
 * gate's end points.
 *
 * @author joulupunikki
 */
public class JumpGate implements Serializable {

    private int x1;
    private int y1;
    private int x2;
    private int y2;
    int flags; // UINT
    int planet_1_index;
    int planet_2_index;

    /**
     * Creates a jump gate object. Reads in coordinates of the jump gate's end
     * points.
     *
     * @param fc the FileChannel which contains the file from which the data is
     * read.
     * @param count the Counter containing the position from which the data is
     * read.
     * @throws IOException
     */
    public JumpGate(FileChannel fc, Counter count) throws IOException {
        count.getSet(-2);
        x1 = GalaxyReader.readShort(fc, count.getSet(2));
        y1 = GalaxyReader.readShort(fc, count.getSet(2));
        x2 = GalaxyReader.readShort(fc, count.getSet(2));
        y2 = GalaxyReader.readShort(fc, count.getSet(2));
        flags = GalaxyReader.readInt(fc, count.getSet(4));
        planet_1_index = 0;
        planet_2_index = 0;
//        planet_1_index = GalaxyReader.readInt(fc, count.getSet(4));
//        planet_2_index = GalaxyReader.readInt(fc, count.getSet(4));

    }

    /**
     * Prints a jump gate object. Prints the coordinates of the end points,
     * flags and planet indices. For debugging purposes.
     */
    public void print() {
        System.out.println("x1:" + x1);
        System.out.println("y1:" + y1);
        System.out.println("x2:" + x2);
        System.out.println("y2:" + y2);
        System.out.println("flags:" + flags);
        System.out.println("planet_1_index:" + planet_1_index);
        System.out.println("planet_2_index:" + planet_2_index);
    }

    /**
     * @return the x1
     */
    public int getX1() {
        return x1;
    }

    /**
     * @param x1 the x1 to set
     */
    public void setX1(int x1) {
        this.x1 = x1;
    }

    /**
     * @return the y1
     */
    public int getY1() {
        return y1;
    }

    /**
     * @param y1 the y1 to set
     */
    public void setY1(int y1) {
        this.y1 = y1;
    }

    /**
     * @return the x2
     */
    public int getX2() {
        return x2;
    }

    /**
     * @param x2 the x2 to set
     */
    public void setX2(int x2) {
        this.x2 = x2;
    }

    /**
     * @return the y2
     */
    public int getY2() {
        return y2;
    }

    /**
     * @param y2 the y2 to set
     */
    public void setY2(int y2) {
        this.y2 = y2;
    }
}
