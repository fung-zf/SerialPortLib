package com.z.helper;


import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import com.z.SerialPort;
import com.z.bean.ComData;
import com.z.util.MyFunc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;


/**
 * 串口操作方法
 */
public abstract class SerialHelper {
    private Context context;
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private Thread mReadThread;
    private SendThread mSendThread;
    private String sPort = "/dev/s3c2410_serial0";
    private int iBaudRate = 115200;
    private boolean _isOpen = false;
    private byte[] _bLoopData = new byte[]{0x30};
    private int iDelay = 200;
    private int mDataType = 1;

    /**
     *
     * @param context
     * @param mDataType 读取串口后，返回的数据类型
     */
    public SerialHelper(Context context , int mDataType) {
        this.mDataType = mDataType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.sPort = "/dev/ttyMT2";
            this.iBaudRate = 115200;
        } else {
            this.sPort = "/dev/ttyMT1";
            this.iBaudRate = 115200;
        }
        this.context = context;
        this.mReadThread = new Thread(mDataType == 1 ?  new ReadCompleteData() : new ReadAllData() );
        this.mSendThread = new SendThread();
        this.mSendThread.setSuspendFlag();
    }

    /**
     * @param context
     * @param sPort : 串口号
     * @param sBaudRate ： 波特率
     * @param mDataType ： 读取串口后，返回的数据类型
     */
    public SerialHelper(Context context , String sPort, String sBaudRate , int mDataType) {
        this.context = context;
        this.mDataType = mDataType;
        this.sPort = sPort;
        this.iBaudRate = Integer.getInteger(sBaudRate);
        this.mReadThread = new Thread(mDataType == 1 ?  new ReadCompleteData() : new ReadAllData() );
        this.mSendThread = new SendThread();
        this.mSendThread.setSuspendFlag();
    }


    /**
     * 调用JNI，初始化、并打开串口，启动读取线程、发送线程
     * @throws SecurityException
     * @throws IOException
     * @throws InvalidParameterException
     */
    public void open() throws SecurityException, IOException, InvalidParameterException {
        mSerialPort = new SerialPort(new File(sPort), iBaudRate, 0);
        mOutputStream = mSerialPort.getOutputStream();
        mInputStream = mSerialPort.getInputStream();
        mReadThread.start();
        mSendThread.start();
        _isOpen = true;




    }


    /**
     * 关闭读串口线程，调用JNI关闭串口
     */
    public void close() {

        if (mReadThread != null){
            mReadThread.interrupt();
        }

        if (mSendThread != null){
            mSendThread.interrupt();
        }

        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }

        _isOpen = false;
    }

    //----------------------------------------------------
    public void send(byte[] bOutArray) {
        try {
            mOutputStream.write(bOutArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //----------------------------------------------------
    public void sendHex(String sHex) {
        byte[] bOutArray = MyFunc.HexToByteArr(sHex);
        send(bOutArray);
    }

    //----------------------------------------------------
    public void sendTxt(String sTxt) {
        byte[] bOutArray = sTxt.getBytes();
        send(bOutArray);
    }


    /**
     * 读取全部数据
     */
    private class ReadAllData implements Runnable{
        @Override
        public void run() {
            while(_isOpen) {
                try
                {
                    Log.d("SerialPort", "Read com thread : " + mSendThread.getName());
                    if (mInputStream == null) return;
                    byte[] buffer=new byte[512];
                    int size = mInputStream.read(buffer);
                    if (size > 0){
                        ComData comRecData = new ComData(sPort,buffer,size);
                        onDataReceived(comRecData);
                    }
                    try
                    {
                        Thread.sleep(iDelay);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                } catch (Throwable e)
                {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }


    /**
     * 读取完整数据
     */
    private class ReadCompleteData implements Runnable{
        @Override
        public void run() {
            int currentLength = 0; //未处理的数据长度
            byte[] buffers = new byte[1024];
            byte[] buffer = new byte[512];
            while (_isOpen) {
                try {
                    Log.d("SerialPort", "Read com thread : " + mSendThread.getName());

                    if (mInputStream == null) return;
                    int size = mInputStream.read(buffer);
                    //此处有可能存在数组溢出
                    System.arraycopy(buffer, 0, buffers, currentLength, size);  //arraycopy(源数组, 原数组的起始位置, 目标数组, 目标数组的起始位置, 要复制的元素数目);
                    currentLength += size;
                    while (currentLength >= 3) {
                        if (buffers[0] != 0x4B || buffers[1] != 0x59) {
                            System.arraycopy(buffers, 1, buffers, 0, currentLength - 1);
                            currentLength -= 1;
                            continue;
                        } else {
                            int factPackLen = (int) buffers[2] + 4;  //整个包的长度
                            if (currentLength < factPackLen) {
                                break;
                            } else {
                                //异或校验
                                byte temp = buffers[2];
                                for (int i = 3; i < factPackLen - 1; i++) {
                                    temp ^= buffers[i];
                                }
                                //校验通过的逻辑处理
                                if (temp == buffers[factPackLen - 1]) {
                                    ComData ComRecData = new ComData(sPort, buffers, factPackLen);
                                    onDataReceived(ComRecData);
                                }

                                //校验通过与否都移除整条数据包
                                System.arraycopy(buffers, factPackLen, buffers, 0, currentLength - factPackLen);
                                currentLength -= (factPackLen);
                            }
                        }
                    }

                    try {
                        Thread.sleep(iDelay);//延时50ms
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //----------------------------------------------------
    private class SendThread extends Thread {
        public boolean suspendFlag = true;// 控制线程的执行

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {

                Log.d("SerialPort", "Send com thread : " + mSendThread.getName());

                synchronized (this) {
                    while (suspendFlag) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                send(getbLoopData());
                try {
                    Thread.sleep(iDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        //线程暂停
        public void setSuspendFlag() {
            this.suspendFlag = true;
        }

        //唤醒线程
        public synchronized void setResume() {
            this.suspendFlag = false;
            notify();
        }
    }


    //----------------------------------------------------
    public int getBaudRate() {
        return iBaudRate;
    }

    public boolean setBaudRate(int iBaud) {
        if (_isOpen) {
            return false;
        } else {
            iBaudRate = iBaud;
            return true;
        }
    }

    public boolean setBaudRate(String sBaud) {
        int iBaud = Integer.parseInt(sBaud);
        return setBaudRate(iBaud);
    }

    //----------------------------------------------------
    public String getPort() {
        return sPort;
    }

    public boolean setPort(String sPort) {
        if (_isOpen) {
            return false;
        } else {
            this.sPort = sPort;
            return true;
        }
    }

    //----------------------------------------------------
    public boolean isOpen() {
        return _isOpen;
    }

    //----------------------------------------------------
    public byte[] getbLoopData() {
        return _bLoopData;
    }

    //----------------------------------------------------
    public void setbLoopData(byte[] bLoopData) {
        this._bLoopData = bLoopData;
    }

    //----------------------------------------------------
    public void setTxtLoopData(String sTxt) {
        this._bLoopData = sTxt.getBytes();
    }

    //----------------------------------------------------
    public void setHexLoopData(String sHex) {
        this._bLoopData = MyFunc.HexToByteArr(sHex);
    }

    //----------------------------------------------------
    public int getiDelay() {
        return iDelay;
    }

    //----------------------------------------------------
    public void setiDelay(int iDelay) {
        this.iDelay = iDelay;
    }

    //----------------------------------------------------
    public void startSend() {
        if (mSendThread != null) {
            mSendThread.setResume();
        }
    }
    //----------------------------------------------------
    public void stopSend() {
        if (mSendThread != null) {
            mSendThread.setSuspendFlag();
        }
    }
    //----------------------回调方法------------------------------
    protected abstract void onDataReceived(ComData ComRecData);



    //----------------------------------------------------串口发送
    public void sendPortData(SerialHelper ComPort, String sOut) {
        if (ComPort != null && isOpen()) {
            sendHex(sOut);
        }
    }

    //----------------------------------------------------关闭串口
    public void CloseComPort(SerialHelper ComPort) {
        if (ComPort != null) {
            stopSend();
            close();
        }
    }

    //----------------------------------------------------打开串口
    public void OpenComPort(SerialHelper ComPort) {
        try {
            open();
        } catch (SecurityException e) {
            ShowMessage("打开串口失败:没有串口读/写权限!");
        } catch (InvalidParameterException e) {
            ShowMessage("打开串口失败:参数错误!");
        } catch (IOException e) {
            ShowMessage("打开串口失败:未知错误!");
        }
    }

    //------------------------------------------显示消息
    private void ShowMessage(String sMsg) {
        Toast.makeText(context, sMsg, Toast.LENGTH_SHORT).show();
    }


}