package cn.heshiqian.framework.h.servlet.servlet;

import cn.heshiqian.framework.h.cflog.core.CFLog;
import cn.heshiqian.framework.h.servlet.classs.ClassManage;
import cn.heshiqian.framework.h.servlet.database.FrameworkMemoryStorage;
import cn.heshiqian.framework.h.servlet.exception.NotExistInitParameterException;
import cn.heshiqian.framework.h.servlet.startup.ContextScanner;
import cn.heshiqian.framework.h.servlet.tools.Tool;
import cn.heshiqian.framework.h.servlet.view.ViewHandler;
import com.sun.deploy.util.ArrayUtil;
import org.apache.commons.collections.ListUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public final class MainProcessServlet extends HttpServlet {

    private static CFLog cfLog = new CFLog(MainProcessServlet.class);

    private static ServletReqHandler servletReqHandler;
    private ArrayList<String> fileList;

    @Override
    public void init(ServletConfig config) throws ServletException {
        if (!FrameworkMemoryStorage.isBeforeScan) {
            ContextScanner.newInstance();
            ContextScanner.saveContext(config.getServletContext());
            cfLog.info("上下文已存储！-->" + ContextScanner.getContext().toString());
            ContextScanner.prepare();

            cfLog.info("本地地址：" + FrameworkMemoryStorage.allLocalIpAddress.toString());

            String classesPackagePath = ContextScanner.getContext().getInitParameter("classesPackagePath");
            String staticFilePath = ContextScanner.getContext().getInitParameter("staticFilePath");
            boolean staticFileLog =Boolean.valueOf(ContextScanner.getContext().getInitParameter("staticFileLog"));

            cfLog.info("配置信息：classesPackagePath --> " + classesPackagePath);
            cfLog.info("配置信息：staticFilePath --> " + staticFilePath);

            //2018年8月9日13:23:58
            //添加配置文件缺少的问题，避免框架启动失败
            if (classesPackagePath == null || classesPackagePath.equals("")) {
                cfLog.err("缺少上下文配置信息：classesPackagePath\n" +
                        "请在web.xml下配置<context-param>标签\n" +
                        "其中<param-name>为classesPackagePath\n" +
                        "<param-value>值为包名(最大一层即可)");

                throw new NotExistInitParameterException("缺少上下文配置信息：classesPackagePath");
            }
            if (staticFilePath == null || staticFilePath.equals("")) {
                cfLog.err("缺少上下文配置信息：staticFilePath\n" +
                        "请在web.xml下配置<context-param>标签\n" +
                        "其中<param-name>为'staticFilePath'\n" +
                        "<param-value>值为静态文件所在路径(最大一层即可，若需整个目录直接填写'/'即可)");

                throw new NotExistInitParameterException("缺少上下文配置信息：staticFilePath");
            }

            FrameworkMemoryStorage.classesPackagePath = classesPackagePath;
            FrameworkMemoryStorage.staticFilePath = staticFilePath;
            FrameworkMemoryStorage.staticFileDir = ContextScanner.getContext().getRealPath(staticFilePath);
            FrameworkMemoryStorage.staticFileLogSwitch=staticFileLog;

            cfLog.info("设置的静态目录：" + FrameworkMemoryStorage.staticFileDir);

            fileList = Tool.listAllFile(FrameworkMemoryStorage.staticFileDir);
            cfLog.info("扫描到的静态文件：" + fileList.size() + "个");
            for (int i = 0; i < fileList.size(); i++) {
                String one = fileList.get(i);
                String two = one.replace(FrameworkMemoryStorage.staticFileDir, "/");
                String tre = two.replace("\\", "/");
                fileList.remove(i);
                fileList.add(i, tre);
            }
            int mark = 1;
            System.out.println("文件列表如下：");
            for (int i = 0; i < fileList.size(); i++, mark++) {
                System.out.print(fileList.get(i));
                if (mark % 10 == 0)
                    System.out.println();
                else
                    if (mark == fileList.size())
                        System.out.println();
                    else
                        System.out.print(",");
            }

            ClassManage.newInstance();
            ClassManage.doScan(FrameworkMemoryStorage.classesPackagePath);
        }

        if (servletReqHandler == null) {
            servletReqHandler = new ServletReqHandler(MainProcessServlet.class);
            cfLog.info("Handler已生成！内存地址：" + servletReqHandler.toString());
        } else {
            cfLog.war("Handler已被生成过！这里重新执行了初始化");
        }
        super.init(config);
    }

    @Override
    public void destroy() {
        cfLog.war("主处理Servlet正在执行销毁");
        ClassManage.Bridge.stopLifeRecycle();
        super.destroy();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        servletReqHandler.PostHandler(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //todo 这里应该吧所有的静态文件过滤掉
        String temp = request.getRequestURL().toString();
        temp=temp.substring(temp.indexOf("/", 10),temp.length());
        for(String s : fileList){
            if(s.equals(temp)){
                //todo 只用浏览器发送的接受值不足以判断所有静态文件，还需要看后缀名，再说啦！~
                String contentType = request.getHeader("Accept").split(",")[0];
                ViewHandler.reSendStaticFile(response,temp,contentType,FrameworkMemoryStorage.staticFileLogSwitch);
                return;
            }
        }

        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        servletReqHandler.GetHandler(request, response);
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }


}
