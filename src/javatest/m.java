package javatest;

import cn.heshiqian.framework.h.cflog.core.CFLog;

import java.io.File;

public class m {

    static CFLog cfLog=new CFLog(m.class);

    public static void main(String[] args) {

        cfLog.info("这是一段测试语言");
        cfLog.war("这是一段测试语言");
        cfLog.err("这是一段测试语言");
        cfLog.print("这是一段测试语言");

    }
}
