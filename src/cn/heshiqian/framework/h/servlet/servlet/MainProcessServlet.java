package cn.heshiqian.framework.h.servlet.servlet;

import cn.heshiqian.framework.h.cflog.core.CFLog;
import cn.heshiqian.framework.h.servlet.classs.ClassManage;
import cn.heshiqian.framework.h.servlet.database.FrameworkMemoryStorage;
import cn.heshiqian.framework.h.servlet.startup.ContextScanner;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class MainProcessServlet extends HttpServlet {

    private static CFLog cfLog=new CFLog(MainProcessServlet.class);

    private static ServletReqHandler servletReqHandler;

    @Override
    public void init(ServletConfig config) throws ServletException {
        if(!FrameworkMemoryStorage.isBeforeScan){
            ContextScanner.newInstance();
            ContextScanner.saveContext(config.getServletContext());
            cfLog.info("上下文已存储！-->"+ContextScanner.getContext().toString());

            String classesPackagePath = ContextScanner.getContext().getInitParameter("classesPackagePath");
            String staticFilePath = ContextScanner.getContext().getInitParameter("staticFilePath");

            cfLog.info("配置信息：classesPackagePath --> "+classesPackagePath);
            cfLog.info("配置信息：staticFilePath --> "+staticFilePath);

            FrameworkMemoryStorage.classesPackagePath=classesPackagePath;
            FrameworkMemoryStorage.staticFilePath=staticFilePath;

            ClassManage.newInstance();
            ClassManage.doScan(FrameworkMemoryStorage.classesPackagePath);
        }

        if(servletReqHandler==null){
            servletReqHandler=new ServletReqHandler(MainProcessServlet.class);
            cfLog.info("Handler已生成！内存地址："+servletReqHandler.toString());
        }else {
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
        servletReqHandler.PostHandler(request,response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        servletReqHandler.GetHandler(request,response);
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }


}
