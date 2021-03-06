// Generated by delombok at Sat Jul 01 03:46:04 EEST 2017
package com.ontalsoft.flc.lib;

import java.io.File;
import java.io.IOException;
import com.ontalsoft.flc.lib.chunks.FLCChunkColor256;
import com.ontalsoft.flc.util.BinaryReader;

public class FLCFile {
	private boolean shouldLoop;
	private boolean pauseAfterFirstFrame;
	private FLCHeader header;
	private File inputFile;
	private BinaryReader reader;
	private FLCChunkColor256 currentPalette;
	private FLCFrameBuffer currentFrame;

	public int getWidth() {
		return header.getWidth();
	}

	public int getHeight() {
		return header.getHeight();
	}

	public FLCFile(File inputFile) {
		this.inputFile = inputFile;
		header = null;
		shouldLoop = true;
		pauseAfterFirstFrame = false;
		currentFrame = null;
		currentPalette = null;
	}

	public boolean open() throws Exception {
		reader = new BinaryReader(inputFile);
		header = FLCHeader.readFromStream(reader);
		if (header.getType() != (short) 44818) {
			throw new Exception("Can only open FLC videos (Type 0xAF12)");
		}
		currentFrame = new FLCFrameBuffer(this);
		return true;
	}

	public FLCColor[] getFramebufferCopy() {
		return currentFrame.getFramebufferCopy();
	}

	public FLCChunk readNextChunk() throws IOException {
		FLCChunk frm = FLCChunk.createChunk(reader, this);
		FLCChunkColor256 newPalette = (FLCChunkColor256) frm.getChunkByType(ChunkType.COLOR_256);
		if (newPalette != null) {
			currentPalette = newPalette;
		}
		currentFrame.updateFromFLCChunk(frm, currentPalette);
		return frm;
	}

	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public boolean isShouldLoop() {
		return this.shouldLoop;
	}

	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public boolean isPauseAfterFirstFrame() {
		return this.pauseAfterFirstFrame;
	}

	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public FLCHeader getHeader() {
		return this.header;
	}

	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public BinaryReader getReader() {
		return this.reader;
	}
}
