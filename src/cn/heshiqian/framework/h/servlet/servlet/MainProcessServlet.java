package cn.heshiqian.framework.h.servlet.servlet;

import cn.heshiqian.framework.h.cflog.core.*;

import cn.heshiqian.framework.h.servlet.classs.ClassManage;
import cn.heshiqian.framework.h.servlet.database.FrameworkMemoryStorage;
import cn.heshiqian.framework.h.servlet.database.HServlet;
import cn.heshiqian.framework.h.servlet.exception.NotExistInitParameterException;
import cn.heshiqian.framework.h.servlet.handler.ServletReqHandler;
import cn.heshiqian.framework.h.servlet.handler.StaticFileHandle;
import cn.heshiqian.framework.h.servlet.startup.ContextScanner;
import cn.heshiqian.framework.h.servlet.tools.HttpHelper;
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

    private static Logger cfLog=CFLog.logger(MainProcessServlet.class);

    private static ServletReqHandler servletReqHandler;

    @Override
    public void init(ServletConfig config) throws ServletException {
        cfLog.war(HServlet.HS_START_INFO_1);
        if (!FrameworkMemoryStorage.isBeforeScan) {
            ContextScanner.newInstance();
            ContextScanner.saveContext(config.getServletContext());
            cfLog.info( HServlet.HS_START_INFO_2 + ContextScanner.getContext().toString());
            ContextScanner.prepare();

            cfLog.info( HServlet.HS_START_INFO_3 + FrameworkMemoryStorage.allLocalIpAddress.toString());

            ClassManage.newInstance();
            ClassManage.doScan(FrameworkMemoryStorage.classesPackagePath);
        }

        StaticFileHandle.newInstance();
        StaticFileHandle.prepare(FrameworkMemoryStorage.fileList);

        if (servletReqHandler == null) {
            servletReqHandler = new ServletReqHandler(MainProcessServlet.class);
            cfLog.info(HServlet.HS_START_INFO_4 + servletReqHandler.toString());
        } else {
            cfLog.war(HServlet.HS_START_INFO_5);
        }

        cfLog.print("框架启动完毕");

        super.init(config);
    }

    @Override
    public void destroy() {
        cfLog.war(HServlet.HS_START_INFO_6);
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
        req.setCharacterEncoding("utf-8");
        resp.setCharacterEncoding("utf-8");
        servletReqHandler.DeleteHandler(req, resp);
    }

    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doHead(req, resp);
    }

    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doOptions(req, resp);
    }

    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(400);
    }
}
