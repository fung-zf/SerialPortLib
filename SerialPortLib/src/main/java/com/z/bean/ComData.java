package com.z.bean;

public class ComData {
    public byte[] recData=null;//数据包
    public Long recTime;//获取时刻
    public String recComPort;//串口号
    public int recDataType=0;//数据包类型
    public ComData(String port,byte[] buffer,int size)
    {
        recComPort=port;
        recData=new byte[size];
        for(int i=0;i<size;i++)
        {
            recData[i]=buffer[i];
        }
        recTime=System.currentTimeMillis();
        if(recData.length>9)
        {
            recDataType=recData[9];
        }
    }
}
