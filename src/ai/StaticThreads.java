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
package ai;

import galaxyreader.Planet;
import game.Hex;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.swing.SwingWorker;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.C;
import util.FN;

/**
 * Handles intensive static AI support structure calculations in a parallel
 * thread. These can take a dozen seconds to calculate and should not delay
 * start of a human player game.
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class StaticThreads {
    private static final Logger logger = LogManager.getLogger(StaticThreads.class);
    private static volatile boolean static_done;
    private static class StaticAIWorker extends SwingWorker<Void, Void> {
        private List<Planet> planets;
        public StaticAIWorker(List<Planet> planets) {
            this.planets = planets;
        }

        @Override
        public Void doInBackground() {
            logger.debug("StaticAIWorker started");
            long start = System.currentTimeMillis();
            CRC32 crc32 = new CRC32();
            ByteBuffer byte_buf = packStaticAIInitializationVector();
            crc32.update(byte_buf);
            File static_ai_file = new File(FN.STATIC_AI_SAVE_BIN);
            boolean read_failed = false;
            try (ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(FileUtils.openInputStream(static_ai_file)))) {
                long initv_crc32 = ois.readLong();
                long save_crc32 = ois.readLong();

                if (crc32.getValue() == initv_crc32 && readSavedStaticAIData(ois)) {
                    if (checksumStaticAIData() != save_crc32) {
                        throw new IOException(FN.STATIC_AI_SAVE_BIN + " save checksum mismatch");
                    }
                }
            } catch (IOException e) {
                read_failed = true;
                for (Planet planet : planets) {
                    planet.planet_grid.parallelSetAIDataStructures(planet);
                }
            }
            if (read_failed) {
                try (ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(FileUtils.openOutputStream(static_ai_file)))) {
                    oos.writeLong(crc32.getValue());
                    oos.writeLong(checksumStaticAIData());
                    for (Planet planet : planets) {
                        oos.writeObject(planet.planet_grid.getIntraContHexDist());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(FN.STATIC_AI_SAVE_BIN + " write failed");
                }
            }
            logger.debug("StaticAIWorker finished in " + ((double) (System.currentTimeMillis() - start)) / 1000 + "s");
            static_done = true;
            return null;
        }

        private ByteBuffer packStaticAIInitializationVector() {
            ByteBuffer byte_buf = ByteBuffer.allocate(2 * planets.size() * C.PLANET_MAP_WIDTH * C.PLANET_MAP_COLUMNS);
            for (Planet planet : planets) {
                for (Hex[] row : planet.planet_grid.getMapArray()) {
                    for (Hex hex : row) {
                        byte_buf.putShort(hex.getTerrFlags());
                    }
                }
            }
            return byte_buf;
        }

        private long checksumStaticAIData() {
            CRC32 crc32 = new CRC32();
            for (Planet planet : planets) {
                for (byte[] row : planet.planet_grid.getIntraContHexDist()) {
                    crc32.update(row);
                }
            }
            return crc32.getValue();
        }

        @Override
        public void done() {       
        }

        private boolean readSavedStaticAIData(ObjectInputStream ois) throws IOException {
            for (Planet planet : planets) {
                try {
                    planet.planet_grid.setIntraContHexDist((byte[][]) ois.readObject());
                } catch (ClassNotFoundException ex) {
                    throw new IOException(null, ex);
                }
            }
            return true;
        }
    }

    public static void dispatchStaticAIWorker(List<Planet> planets) {
        static_done = false;
        (new StaticAIWorker(planets)).execute();
    }

    /**
     * Game AI execution threads should confirm this before proceeding.
     *
     * @return
     */
    public static boolean isStaticDone() {
        return static_done;
    }
}
