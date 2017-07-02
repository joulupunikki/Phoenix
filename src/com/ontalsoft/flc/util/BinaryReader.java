package com.ontalsoft.flc.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This class provides basic functionality similar to its C# couterpart,
 * simulating basic System.IO.BinaryReader operations used in this project.
 * 
 * @author Akaine
 */
public class BinaryReader extends RandomAccessFile{

	public BinaryReader(File file) throws FileNotFoundException{
		super(file, "r");
	}
	
	/**
	 * Reads signed 4 bytes int at the current cursor position.
	 * @return
	 * @throws IOException
	 */
	public int readInt32() throws IOException{
		byte[] bytes = new byte[4];
		this.read(bytes);
		return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}
	
	/**
	 * Reads unsigned 4 bytes int (long) at the current cursor position.
	 * @return
	 * @throws IOException
	 */
	public long readUInt32() throws IOException{
		return this.readInt32() & 0xFFFFFFFFL;
	}
	
	/**
	 * Reads signed 2 bytes int (short) at the current cursor position.
	 * @return
	 * @throws IOException
	 */
	public short readInt16() throws IOException{
		byte[] bytes = new byte[2];
		this.read(bytes);
		return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
	}
	
	/**
	 * Reads unsigned 2 byte int (short) at the current cursor position.
	 * @return
	 * @throws IOException
	 */
	public short readUInt16() throws IOException{
		return (short)(this.readInt16() & 0xFFFF);
	}
	
	/**
	 * Reads unsigned byte at the current cursor position.<br/> 
	 * <i>The only way to simulate the "unsigned" byte behaviour in Java is by using an int after (i & 0xFF) operation.</i>
	 * @return
	 * @throws IOException
	 */
	public int readUByte() throws IOException{
		return this.readUnsignedByte();
	}
	
	
	/**
	 * Reads signed byte at the current cursor position.<br/>
	 * <i>In Java byte is always signed (-127 to 127).</i>
	 * @return
	 * @throws IOException
	 */
	public byte readSByte() throws IOException{
		return this.readByte();
	}
	
	/**
	 * Reads array of bytes of specified length.<br/>
	 * @return
	 * @throws IOException
	 */
	public byte[] readBytes(int num) throws IOException{
		byte[] bytes = new byte[num];
		this.read(bytes);
		return bytes;
	}
}
