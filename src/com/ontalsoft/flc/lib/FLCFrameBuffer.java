package com.ontalsoft.flc.lib;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.ontalsoft.flc.lib.chunks.FLCChunkByteRun;
import com.ontalsoft.flc.lib.chunks.FLCChunkColor256;
import com.ontalsoft.flc.lib.chunks.FLCChunkDeltaFLC;
import com.ontalsoft.flc.util.BinaryStreamReader;

public class FLCFrameBuffer{

	private FLCFile flcFile;
	public FLCColor[] framebuffer;
	
	public FLCFrameBuffer(FLCFile flcFile){
		this.flcFile = flcFile;
		framebuffer = new FLCColor[flcFile.getWidth() * flcFile.getHeight()];
	}
	
	public FLCColor[] getFramebufferCopy(){
		FLCColor[] result = new FLCColor[framebuffer.length];
		System.arraycopy(framebuffer, 0, result, 0, framebuffer.length);
		return result;
	}
	
	@SuppressWarnings("incomplete-switch")
	public void updateFromFLCChunk(FLCChunk chunk, FLCChunkColor256 paletteChunk){
		if(!chunk.getType().equals(ChunkType.FRAME_TYPE)){
			return;
		}
		
		int subChunksSize = chunk.getSubChunks().size();
		for(int chnkIdx = 0; chnkIdx < subChunksSize; chnkIdx++){
			switch(chunk.getSubChunks().get(chnkIdx).getType()){
			case BYTE_RUN:
				FLCChunkByteRun brun = (FLCChunkByteRun)chunk.getSubChunks().get(chnkIdx);
				for(int i = 0; i < brun.getPixelData().length; i++){
					framebuffer[i] = paletteChunk.getColors()[brun.getPixelData()[i]];
				}
				break;
			case DELTA_FLC:
				FLCChunkDeltaFLC deltaflc = (FLCChunkDeltaFLC)chunk.getSubChunks().get(chnkIdx);
				try(BinaryStreamReader reader = new BinaryStreamReader(new ByteArrayInputStream(deltaflc.getPayload()))){
					short lineCount = reader.readUInt16();
					int curLine = 0;
					
					for(int lineIdx = 0; lineIdx < lineCount; lineIdx++){
						Short lineSkipCount = null;
						Short packetCount = null;
						
						while(packetCount == null){
							short opcode = reader.readInt16();
							if((opcode & (1 << 15)) > 0){
								if((opcode & (1 << 14)) > 0){
									lineSkipCount = (short)-opcode;
								}
							}else{
								if(!((opcode & (1 << 14)) > 0)){
									packetCount = opcode;
								}
							}
						}
						
						if(packetCount > 100){
							return;
						}
						if(lineSkipCount != null){
							curLine += lineSkipCount.intValue();
						}
						
						int curColumn = 0;
						for(int pcktIdx = 0; pcktIdx < packetCount; pcktIdx++){
							int colSkipCount = reader.readByte() & 0xFF;
							byte rleByteCount = reader.readByte();
							
							curColumn += colSkipCount;
							if(rleByteCount > 0){
								for(int i = 0; i < rleByteCount; i++){
									short val = reader.readUInt16();
									int val1 = ((byte)(val >> 8)) & 0xFF;
									int val2 = (byte)val & 0xFF;
									
									framebuffer[(curColumn + curLine * flcFile.getWidth()) + 0] = paletteChunk.getColors()[val2];
									framebuffer[(curColumn + curLine * flcFile.getWidth()) + 1] = paletteChunk.getColors()[val1];
									curColumn += 2;
								}
							}else if(rleByteCount < 0){
								byte cpyCount = (byte)-rleByteCount;
								short val = reader.readUInt16();
								int val1 = ((byte)(val >> 8)) & 0xFF;
								int val2 = (byte)val & 0xFF;
								
								for(int i = 0; i < cpyCount; i++){
									framebuffer[(curColumn + curLine * flcFile.getWidth()) + 0] = paletteChunk.getColors()[val2];
									framebuffer[(curColumn + curLine * flcFile.getWidth()) + 1] = paletteChunk.getColors()[val1];
									curColumn += 2;
								}
							}
						}
						curLine++;
					}
				}catch(IOException e){
					e.printStackTrace(System.out);
				}
				break;
			}
		}
	}
}
