package cn.heshiqian.framework.h.servlet.database;

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
}
