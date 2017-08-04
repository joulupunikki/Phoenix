package com.ontalsoft.flc.lib.chunks;

import com.ontalsoft.flc.lib.FLCChunk;
import com.ontalsoft.flc.lib.FLCFile;
import com.ontalsoft.flc.util.BinaryReader;

public class FLCChunkUnknown extends FLCChunk{

	public FLCChunkUnknown(FLCFile flcFile){
		super(flcFile);
	}
	
	@Override
	protected void readChunk(BinaryReader reader){
		//do nothing since we're skipping it
	}
}
