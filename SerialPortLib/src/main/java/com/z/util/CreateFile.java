package com.z.util;



import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;


public class CreateFile {

    public void Createfile(Context mContext ,String str) {
        String content = "\n" + str;
        String fileName = "send.txt";
        String filePath = "/sdcard/串口调试助手";
        File file = new File(filePath);
        {
            if (!file.exists()) {
                file.mkdirs();
            }
            if (file.exists() && file.canWrite()) {
                File newFile = new File(file.getAbsolutePath() + "/" + fileName);
                 RandomAccessFile fos = null;
                try {

                    newFile.createNewFile();
                    if (newFile.exists() && newFile.canWrite()) {
                        fos = new RandomAccessFile(newFile, "rw");
                        //光标移到原始文件最后，再执行写入
                        fos.seek(newFile.length());
                        fos.write(content.getBytes());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, "写入失败！", Toast.LENGTH_SHORT).show();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
