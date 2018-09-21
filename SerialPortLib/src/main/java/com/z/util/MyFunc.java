package com.z.util;

import java.nio.ByteBuffer;

/**
 *数据转换工具
 */
public class MyFunc {

	/** 
	  * 字节转换为浮点 
	  * @param b 字节（至少4个字节） 
	  * @param index 开始位置 
	  * @return 
	  */
	public static float fourbyte2float(byte[] b,int index){
		byte[] a = {b[index],b[index+1],b[index+2],b[index+3]};
		ByteBuffer buf=ByteBuffer.allocateDirect(4); //无额外内存的直接缓存
		//buf=buf.order(ByteOrder.LITTLE_ENDIAN);//默认大端，小端用这行
		buf.put(a);
		buf.rewind();
		float f2=buf.getFloat();
		return f2;
	}

	//温湿度大气压
	public static int byte4Toint(byte[] b, int index) {
		byte[] by = new byte[4];
		by[0] = b[index+3];
		by[1] = b[index+2];
		by[2]=  b[index+1];
		by[3] = b[index];
		int l;
		l = by[ 0];
		l &= 0xff;
		l |= ((long) by[ 1] << 8);
		l &= 0xffff;
		l |= ((long) by[2] << 16);
		l &= 0xffffff;
		l |= ((long) by[3] << 24);
		l &= 0xffffffff;
		return l;
	}

	//-------------------------------------------------------
	// 判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
    static public int isOdd(int num)
	{
		return num & 0x1;
	}
    //-------------------------------------------------------
    static public int HexToInt(String inHex)//Hex字符串转int
    {
    	return Integer.parseInt(inHex, 16);
    }
    //-------------------------------------------------------
    static public byte HexToByte(String inHex)//Hex字符串转byte
    {
    	return (byte)Integer.parseInt(inHex,16);
    }
    //-------------------------------------------------------
    static public String Byte2Hex(Byte inByte)//1字节转2个Hex字符
    {
    	return String.format("%02x", inByte).toUpperCase();
    }
    //-------------------------------------------------------
	public static String ByteArrToHex(byte[] inBytArr)//字节数组转转hex字符串
	{
		StringBuilder strBuilder=new StringBuilder();
		int j=inBytArr.length;
		for (int i = 0; i < j; i++)
		{
			strBuilder.append(Byte2Hex(inBytArr[i]));
			strBuilder.append(" ");
		}
		return strBuilder.toString(); 
	}
  //-------------------------------------------------------
    static public String ByteArrToHex(byte[] inBytArr,int offset,int byteCount)//字节数组转转hex字符串，可选长度
	{
    	StringBuilder strBuilder=new StringBuilder();
		int j=byteCount;
		for (int i = offset; i < j; i++)
		{
			strBuilder.append(Byte2Hex(inBytArr[i]));
		}
		return strBuilder.toString();
	}
	//-------------------------------------------------------
	//转hex字符串转字节数组
    static public byte[] HexToByteArr(String inHex)//hex字符串转字节数组
	{
		int hexlen = inHex.length();
		byte[] result;
		if (isOdd(hexlen)==1)
		{//奇数
			hexlen++;
			result = new byte[(hexlen/2)];
			inHex="0"+inHex;
		}else {//偶数
			result = new byte[(hexlen/2)];
		}
	    int j=0;
		for (int i = 0; i < hexlen; i+=2)
		{
			result[j]=HexToByte(inHex.substring(i,i+2));
			j++;
		}
	    return result; 
	}
}