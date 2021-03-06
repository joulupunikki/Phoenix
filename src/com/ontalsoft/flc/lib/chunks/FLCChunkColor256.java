// Generated by delombok at Sat Jul 01 03:46:04 EEST 2017
package com.ontalsoft.flc.lib.chunks;

import java.io.IOException;
import com.ontalsoft.flc.lib.FLCChunk;
import com.ontalsoft.flc.lib.FLCColor;
import com.ontalsoft.flc.lib.FLCFile;
import com.ontalsoft.flc.util.BinaryReader;

public class FLCChunkColor256 extends FLCChunk {
	private FLCColor[] colors;

	public FLCChunkColor256(FLCFile flcFile) {
		super(flcFile);
		colors = new FLCColor[256];
	}

	@Override
	protected void readChunk(BinaryReader reader) throws IOException {
		short nrPackets = reader.readUInt16();
		for (int i = 0; i < nrPackets; i++) {
			reader.skipBytes(1); // skip packet count byte
			int copyCount = reader.readUByte();
			if (copyCount == 0) {
				byte[] rgbData = reader.readBytes(256 * 3);
				for (int j = 0; j < 256; j++) {
					colors[j] = new FLCColor(rgbData[j * 3 + 0], rgbData[j * 3 + 1], rgbData[j * 3 + 2]);
				}
			}
		}
	}

	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public FLCColor[] getColors() {
		return this.colors;
	}
}
