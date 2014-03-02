/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package galaxyreader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * A class containing methods for reading bytes from a FileChannel.
 *
 * @author joulupunikki
 */
public class GalaxyReader {

    /**
     * Creates a galaxy and prints it. For debugging purposes.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Path path = FileSystems.getDefault().getPath(args[0]);
        try (FileChannel fc = (FileChannel.open(path))) {

            System.out.println("before galaxy");
            Galaxy galaxy = new Galaxy(fc);
            System.out.println("after galaxy");
            int[][] hex_buf = galaxy.getBuffer();
//            Gui.start(args, hex_buf);
//            galaxy.print();
        } catch (IOException e) {
            System.err.println(e);
            System.out.println(e);
        }
    }

    /**
     * Reads a byte. Byte order is little endian.
     *
     * @param fc the FileChannel to be read.
     * @param index the position from which the byte is read.
     * @return the byte which was read.
     * @throws IOException
     */
    public static byte readByte(FileChannel fc, long index) throws IOException {
        byte r = -1;
        int nread;
        ByteBuffer bf = ByteBuffer.allocate(1);
        bf.order(ByteOrder.LITTLE_ENDIAN);
        fc.position(index);
        do {
            nread = fc.read(bf);
        } while (nread != -1 && bf.hasRemaining());
        bf.rewind();
        r = bf.get();
        return r;
    }

    /**
     * Reads a short, that is two bytes. Byte order is little endian.
     *
     * @param fc the FileChannel to be read.
     * @param index the position from which the short is read.
     * @return the short which was read.
     * @throws IOException
     */
    public static short readShort(FileChannel fc, long index) throws IOException {
        short r = -1;
        int nread;
        ByteBuffer bf = ByteBuffer.allocate(2);
        bf.order(ByteOrder.LITTLE_ENDIAN);
        fc.position(index);
        do {
            nread = fc.read(bf);
        } while (nread != -1 && bf.hasRemaining());
        bf.rewind();
        r = bf.getShort();
        return r;
    }

    /**
     * Reads an int, that is four bytes. Byte order is little endian.
     *
     * @param fc the FileChannel to be read.
     * @param index the position from which the int is read.
     * @return the int which was read.
     * @throws IOException
     */
    public static int readInt(FileChannel fc, long index) throws IOException {
        int r = -1;
        int nread;
        ByteBuffer bf = ByteBuffer.allocate(4);
        bf.order(ByteOrder.LITTLE_ENDIAN);
        fc.position(index);
        do {
            nread = fc.read(bf);
        } while (nread != -1 && bf.hasRemaining());
        bf.rewind();
        r = bf.getInt();
        return r;
    }
}
