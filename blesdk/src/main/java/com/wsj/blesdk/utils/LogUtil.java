package com.wsj.blesdk.utils;

import android.util.Log;


/**
 * 自定义Log
 * Created by WSJ on 2016/10/18.
 */

public class LogUtil {
    /**
     * 允许输出的log日志等级
     * 当出正式版时,把mLogLevel的值改为 LOG_LEVEL_NONE,
     * 就不会输出任何的Log日志了.
     */
    private static boolean logFlag = true;
    /**
     * 以级别为 d 的形式输出LOG,输出debug调试信息
     */
    public static void d(String tag, String msg)
    {
        if (logFlag)
            Log.d(tag, msg);
    }
    /**
     * 以级别为 i 的形式输出LOG,一般提示性的消息information
     */
    public static void i(String tag, String msg)
    {
        if (logFlag)
            Log.i(tag, msg);
    }
    /**
     * 以级别为 w 的形式输出LOG,显示warning警告，一般是需要我们注意优化Android代码
     */
    public static void w(String tag, String msg)
    {
        if (logFlag)
            Log.w(tag, msg);
    }
    /**
     * 以级别为 e 的形式输出LOG ，红色的错误信息，查看错误源的关键
     */
    public static void e(String tag, String msg)
    {
        if (logFlag)
            Log.e(tag, msg);
    }
    /**
     * 以级别为 v 的形式输出LOG ，verbose啰嗦的意思
     *
     */
    public static void v(String tag, String msg)
    {
        if (logFlag)
            Log.v(tag, msg);
    }
    public static void logErrorLine(){
        if (!logFlag)
            return;
        /**
         * 在调用栈中 : [getStackTrace],[logFunc],[调用用logFunc 的方法.] , ...
         * */
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[3];
        LogUtil.e("BLX_PKCS"," 文件名 : " + e.getFileName() + " 当前行数 : " + e.getLineNumber());

    }
    public static void logLine(){
        if (!logFlag)
            return;
        /**
         * 在调用栈中 : [getStackTrace],[logFunc],[调用用logFunc 的方法.] , ...
         * */
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[3];
        LogUtil.e("BLX_PKCS"," 文件名 : " + e.getFileName() + " 当前行数 : " + e.getLineNumber());

    }
    private static void logFuncBase(String tag){
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[4];
        LogUtil.i(tag,">>> " + e.getMethodName());
    }
    public static void logFunc(String tag){
        if (!logFlag)
            return;
        logFuncBase(tag);
    }

}
