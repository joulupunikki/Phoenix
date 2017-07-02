package com.ontalsoft.flc.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.GregorianCalendar;

public class Util{

	public static String byteArrayToHexString(byte[] a){
		StringBuilder sb = new StringBuilder(a.length * 2);
		for(byte b : a){
			sb.append(String.format("%02x", b).toUpperCase());
		}
		return sb.toString();
	}
	
	/**
	 * Coverts old MS-DOS decimal format to Java Date.<br/>
	 * <i>This was pain =S</i>
	 * @param t - MS-DOS decimal format
	 * @return
	 */
	public static String msDosDecimalDateToString(int t){
		Date date = new GregorianCalendar(
				1980 + ((t & 0xfe000000) >> 25), 
				((t & 0x1e00000) >> 21) - 1, 
				((t & 0x1f0000) >> 16), 
				((t & 0xf800) >> 11), 
				((t & 0x7e0) >> 5), 
				2 * (t & 0x1f)).getTime();
		return date.toString();
	}
	
	public static byte[] int32ToByteArray(int i, boolean bigEndianOrder){
		return ByteBuffer.allocate(4).order(bigEndianOrder ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).putInt(i).array();
	}
	
	public static byte[] int16ToByteArray(short i, boolean bigEndianOrder){
		return ByteBuffer.allocate(2).order(bigEndianOrder ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).putShort(i).array();
	}
}
