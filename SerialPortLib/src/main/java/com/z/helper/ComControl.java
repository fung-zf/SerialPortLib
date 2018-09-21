package com.z.helper;
import android.util.Log;
import java.io.IOException;
import java.security.InvalidParameterException;

/**
 * 串口操作
 */

public class ComControl {

    //----------------------------------------------------串口发送
    public static void sendPortData(SerialHelper ComPort, String sOut) {
        if (ComPort != null && ComPort.isOpen()) {
            ComPort.sendHex(sOut);
        }
    }

    //----------------------------------------------------关闭串口
    public static void CloseComPort(SerialHelper ComPort) {
        if (ComPort != null) {
            ComPort.stopSend();
            ComPort.close();
        }
    }

    //----------------------------------------------------打开串口
    public static void OpenComPort(SerialHelper ComPort) {
        try {
            ComPort.open();
        } catch (SecurityException e) {
            ShowMessage("打开串口失败:没有串口读/写权限!");
        } catch (InvalidParameterException e) {
            ShowMessage("打开串口失败:参数错误!");
        } catch (IOException e) {
            ShowMessage("打开串口失败:未知错误!");
        }
    }

    //------------------------------------------显示消息
    public  static void ShowMessage(String sMsg) {
        Log.d("SerialPort", "ShowMessage: " + sMsg);
    }


}
