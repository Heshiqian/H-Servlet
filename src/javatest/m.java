package javatest;

import cn.heshiqian.framework.h.cflog.core.*;

import cn.heshiqian.framework.h.servlet.tools.Tool;

import java.io.File;
import java.util.ArrayList;

public class m {

    static Logger cfLog=CFLog.logger(m.class);

    public static void main(String[] args) {

//        cfLog.info("这是一段测试语言");
//        cfLog.war("这是一段测试语言");
//        cfLog.err("这是一段测试语言");
//        cfLog.print("这是一段测试语言");


//        ArrayList<String> strings = Tool.listAllFile("G:\\IDEA_Project\\H-Servlet\\libs");
//        System.out.println(strings.toString());
        File file = new File("D:\\IDEAP\\H-Serlvet");
        String s = Tool.FileFinder.find(file, "test.html");
        System.out.println(s);
    }
}
