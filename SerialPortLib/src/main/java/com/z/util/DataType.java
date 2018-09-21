package com.z.util;

/**
 * 串口返回的数据类型
 */
public  class DataType {

    /**
     * 读取就返回 ， 不做任何校验。
     */
    public static int DATA_NO_PARSE = 0 ;


    /**
     * 按照协议，返回通过异或校验的完整数据包。
     */
    public static int DATA_OK_PARSE = 1;

}
