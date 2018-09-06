package cn.heshiqian.framework.h.servlet.servlet;

import cn.heshiqian.framework.h.cflog.core.CFLog;

import cn.heshiqian.framework.h.servlet.classs.ClassManage;
import cn.heshiqian.framework.h.servlet.database.FrameworkMemoryStorage;
import cn.heshiqian.framework.h.servlet.exception.NotExistInitParameterException;
import cn.heshiqian.framework.h.servlet.handler.ServletReqHandler;
import cn.heshiqian.framework.h.servlet.handler.StaticFileHandle;
import cn.heshiqian.framework.h.servlet.startup.ContextScanner;
import cn.heshiqian.framework.h.servlet.tools.Tool;
import cn.heshiqian.framework.h.servlet.view.ViewHandler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

public final class MainProcessServlet extends HttpServlet {

    private static CFLog cfLog=new CFLog(MainProcessServlet.class);

    private static ServletReqHandler servletReqHandler;

    @Override
    public void init(ServletConfig config) throws ServletException {
        cfLog.war("框架开始启动");
        if (!FrameworkMemoryStorage.isBeforeScan) {
            ContextScanner.newInstance();
            ContextScanner.saveContext(config.getServletContext());
            cfLog.info("上下文已存储！-->" + ContextScanner.getContext().toString());
            ContextScanner.prepare();

            cfLog.info("本地地址：" + FrameworkMemoryStorage.allLocalIpAddress.toString());

            ClassManage.newInstance();
            ClassManage.doScan(FrameworkMemoryStorage.classesPackagePath);
        }

        StaticFileHandle.newInstance();
        StaticFileHandle.prepare(FrameworkMemoryStorage.fileList);

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
        if(StaticFileHandle.filter(request,response)) return;
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        servletReqHandler.PostHandler(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(StaticFileHandle.filter(request,response)) return;
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        servletReqHandler.GetHandler(request, response);
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }


}
