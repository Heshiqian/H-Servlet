package cn.heshiqian.framework.h.servlet.database;

import cn.heshiqian.framework.s.xconf.pojo.XConfTree;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Arrays;

public final class FrameworkMemoryStorage {

    public static boolean isBeforeScan = false;
    public static ServletContext context=null;
    public static String staticFilePath="/";
    public static String classesPackagePath="";
    public static String staticFileDir="";
    public static String ServerPort="8080";
    public static String[] ServerIps={};
    public static ArrayList<String> allLocalIpAddress=null;
    public static boolean staticFileLogSwitch=false;
    public static ArrayList<String> fileList;
    public static int filterType=HServlet.FILER_TYPE_AUTO;
    public static String filterCustomContent="";
    public static XConfTree mainConfigure=null;
    public static boolean enableRequestErrorTip=false;
    public static boolean disabledNullReturnWaring=true;
    public static boolean enableFileUpload=false;
    public static String ServerDomain="";
}
