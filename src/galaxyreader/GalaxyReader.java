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
