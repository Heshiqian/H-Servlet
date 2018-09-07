package cn.heshiqian.framework.h.servlet.handler;


import cn.heshiqian.framework.h.cflog.core.*;
import cn.heshiqian.framework.h.servlet.annotation.*;
import cn.heshiqian.framework.h.servlet.classs.ClassManage;
import cn.heshiqian.framework.h.servlet.classs.ClassPool;
import cn.heshiqian.framework.h.servlet.database.FrameworkMemoryStorage;
import cn.heshiqian.framework.h.servlet.database.HServlet;
import cn.heshiqian.framework.h.servlet.pojo.RequestMethod;
import cn.heshiqian.framework.h.servlet.tools.HttpHelper;
import cn.heshiqian.framework.h.servlet.view.ViewHandler;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;

public final class CenterHandle {

    private Logger cfLog=CFLog.logger(CenterHandle.class);
    private static ViewHandler viewHandler = new ViewHandler();

    public void distributor(int methodCode, String url, HttpServletRequest request, HttpServletResponse response, Cookie[] cookies, HashMap<String, String> keyMap) {
        Class cclass = ClassManage.checkClassWasInit(url);
        if (cclass == null) {
            HttpHelper.sendErr(response, HServlet.HANDLE_CENTER_NO_INTERFACE);
            return;
        }
        try {
            switch (methodCode) {
                case RequestMethod.GET:
                    //已存在此类，继续操作
                    getReqResProcess_(cclass, url, request, response, cookies, keyMap);
                    break;
                case RequestMethod.POST:
                    //已存在此类，继续操作
                    postReqResProcess_(cclass, url, request, response, cookies, keyMap);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            String errMsg = "";
            errMsg += HServlet.HANDLE_EXCEPTION_PART_1 + e + HServlet.HANDLE_EXCEPTION_PART_2;
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (StackTraceElement s : stackTrace) {
                errMsg += HServlet.HANDLE_EXCEPTION_PART_3 + s.getClassName() + HServlet.HANDLE_EXCEPTION_PART_4 + s.getMethodName() + HServlet.HANDLE_EXCEPTION_PART_5 + s.getLineNumber() + HServlet.HANDLE_EXCEPTION_PART_6;
            }
            HttpHelper.sendErr(response, errMsg);
            e.printStackTrace();
        }
    }

    private void postReqResProcess_(Class c, String url, HttpServletRequest request, HttpServletResponse response, Cookie[] cookies, HashMap<String, String> keyMap) throws InvocationTargetException, IllegalAccessException {
        String oldJSON = keyMap.get(HServlet.SYS_CONSTANT_KEY);

        Object service = ClassPool.getClass(c);
        Method[] methods = service.getClass().getDeclaredMethods();

        for (java.lang.reflect.Method m : methods) {
            RequestUrl requestUrl = m.getAnnotation(RequestUrl.class);
            if (requestUrl != null) {
                if (requestUrl.value().equals(url)) {
                    if (requestUrl.method() == RequestMethod.POST) {
                        ArrayList<Object> objs = new ArrayList<>();
                        Parameter[] parameters = m.getParameters();
                        for (Parameter p : parameters) {
                            Cookies cookiess = p.getAnnotation(Cookies.class);
                            JSONString jsonString = p.getAnnotation(JSONString.class);
                            if (cookiess != null) {
                                //这个变量为Cookie
                                objs.add(cookies);
                            } else if (jsonString != null) {
                                objs.add(oldJSON);
                            } else {
                                objs.add(null);
                                cfLog.war(HServlet.HANDLE_DISPATCHER_INFO_1 + p.getName() + HServlet.HANDLE_DISPATCHER_INFO_2);
                            }
                        }
                        //执行结果，交由视图Handler处理
                        Object invokeResult = m.invoke(service, objs.toArray());
                        //todo 执行结果，交由视图Handler处理
                        NullReturn nullReturn = m.getAnnotation(NullReturn.class);
                        if (nullReturn != null) {
                            //方法注解了空返回，不交由视图Handler处理，直接返回空
                            if (invokeResult != null) {
                                if(FrameworkMemoryStorage.disabledNullReturnWaring){
                                    HttpHelper.sendCustomTitle(response,"text","");
                                    return;
                                }
                                HttpHelper.sendErr(response, HServlet.HANDLE_DISPATCHER_INFO_3 + c.getTypeName() + "." + m.getName() + HServlet.HANDLE_DISPATCHER_INFO_4);
                            }
                            return;
                        }
                        MapToFile mapToFile = m.getAnnotation(MapToFile.class);
                        ResponseBody responseBody = m.getAnnotation(ResponseBody.class);
                        if (mapToFile != null && responseBody != null) {
                            HttpHelper.sendErr(response, HServlet.HANDLE_DISPATCHER_INFO_5 + c.getTypeName() + HServlet.HANDLE_DISPATCHER_INFO_5_1 + m.getName() + HServlet.HANDLE_DISPATCHER_INFO_5_2);
                        }

                        if (mapToFile != null) {
                            viewHandler.analysis(invokeResult, response, cookies, true);
                            return;
                        }
                        if (mapToFile == null && responseBody == null) {
                            viewHandler.analysis(invokeResult, response, cookies, false);
                            return;
                        }
                        if (mapToFile == null && responseBody != null) {
                            viewHandler.analyzeResBody(invokeResult, response, cookies);
                            return;
                        }

                    } else {
                        callMethodErr(response,requestUrl.method());
                        return;
                    }
                }
            }
        }

    }

    private void getReqResProcess_(Class c, String url, HttpServletRequest request, HttpServletResponse response, Cookie[] cookies, HashMap<String, String> keyMap) throws InvocationTargetException, IllegalAccessException {
        Object service = ClassPool.getClass(c);
        java.lang.reflect.Method[] methods = service.getClass().getDeclaredMethods();
        for (java.lang.reflect.Method m : methods) {
            RequestUrl requestUrl = m.getAnnotation(RequestUrl.class);
            if (requestUrl != null) {
                if (requestUrl.value().equals(url)) {
                    if (requestUrl.method() == RequestMethod.GET) {
                        ArrayList<Object> objs = new ArrayList<>();
                        Parameter[] parameters = m.getParameters();
                        for (Parameter p : parameters) {
                            Cookies cookiess = p.getAnnotation(Cookies.class);
                            GetParam param = p.getAnnotation(GetParam.class);
                            TextString textString = p.getAnnotation(TextString.class);
                            if (cookiess != null) {
                                //这个变量为Cookie
                                objs.add(cookies);
                            } else if (param != null) {
                                //这个变量为变量key
                                String val = keyMap.get(param.key());
                                if (val == null)
                                    cfLog.err( HServlet.HANDLE_DISPATCHER_INFO_6+ param.key() + HServlet.HANDLE_DISPATCHER_INFO_6_1);
                                objs.add(val);
                            } else if (textString != null) {
                                //这个变量不做处理，原文发送
                                String fullPath = request.getRequestURI();
                                String queryString = request.getQueryString();
                                if (queryString != null)
                                    fullPath += "?" + queryString;
                                objs.add(fullPath);
                            } else {
                                objs.add(null);
                                cfLog.war(HServlet.HANDLE_DISPATCHER_INFO_1 + p.getName() + HServlet.HANDLE_DISPATCHER_INFO_2);
                            }
                        }
                        //执行结果，交由视图Handler处理
                        Object invokeResult = m.invoke(service, objs.toArray());
                        //todo 执行结果，交由视图Handler处理
                        NullReturn nullReturn = m.getAnnotation(NullReturn.class);
                        if (nullReturn != null) {
                            //方法注解了空返回，不交由视图Handler处理，直接返回空
                            if (invokeResult != null) {
                                if(FrameworkMemoryStorage.disabledNullReturnWaring){
                                    HttpHelper.sendCustomTitle(response,"text","");
                                    return;
                                }
                                HttpHelper.sendErr(response, HServlet.HANDLE_DISPATCHER_INFO_3 + c.getTypeName() + "." + m.getName() + HServlet.HANDLE_DISPATCHER_INFO_4);
                            }
                            return;
                        }
                        MapToFile mapToFile = m.getAnnotation(MapToFile.class);
                        ResponseBody responseBody = m.getAnnotation(ResponseBody.class);
                        if (mapToFile != null && responseBody != null) {
                            HttpHelper.sendErr(response, HServlet.HANDLE_DISPATCHER_INFO_5 + c.getTypeName() + HServlet.HANDLE_DISPATCHER_INFO_5_1 + m.getName() + HServlet.HANDLE_DISPATCHER_INFO_5_2);
                        }

                        if (mapToFile != null) {
                            viewHandler.analysis(invokeResult, response, cookies, true);
                            return;
                        }
                        if (mapToFile == null && responseBody == null) {
                            viewHandler.analysis(invokeResult, response, cookies, false);
                            return;
                        }
                        if (mapToFile == null && responseBody != null) {
                            viewHandler.analyzeResBody(invokeResult, response, cookies);
                            return;
                        }

                    } else {
                        callMethodErr(response,requestUrl.method());
                        return;
                    }
                }
            }
        }
    }


    private void callMethodErr(HttpServletResponse response,int code){
        if (!FrameworkMemoryStorage.enableRequestErrorTip) {
            try {
                ViewHandler.reSendErrorCode(response,405);
            } catch (IOException e) {
            }
            return;
        }
        switch (code){
            case RequestMethod.GET:
                HttpHelper.sendErr(response, HServlet.HANDLE_REQUEST_METHOD_ERROR_GET);
                break;
            case RequestMethod.POST:
                HttpHelper.sendErr(response, HServlet.HANDLE_REQUEST_METHOD_ERROR_POST);
                break;
        }
    }



}





