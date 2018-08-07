package cn.heshiqian.framework.h.servlet.servlet;


import cn.heshiqian.framework.h.cflog.core.CFLog;
import cn.heshiqian.framework.h.servlet.annotation.*;
import cn.heshiqian.framework.h.servlet.classs.ClassManage;
import cn.heshiqian.framework.h.servlet.classs.ClassPool;
import cn.heshiqian.framework.h.servlet.database.HServlet;
import cn.heshiqian.framework.h.servlet.startup.ClassScanner;
import cn.heshiqian.framework.h.servlet.tools.HttpHelper;
import cn.heshiqian.framework.h.servlet.pojo.RequestMethod;
import cn.heshiqian.framework.h.servlet.view.ViewHandler;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;

public final class CenterHandle {

    private CFLog cfLog = new CFLog(CenterHandle.class);
    private static ViewHandler viewHandler = new ViewHandler();

    public void distributor(int methodCode, String url, HttpServletRequest request, HttpServletResponse response, Cookie[] cookies, HashMap<String, String> keyMap) {
        Class cclass = ClassManage.checkClassWasInit(url);
        if (cclass == null) {
            HttpHelper.sendErr(response, "没有此接口!");
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
            errMsg += "<span style=\"font-size:16px;color:#6c1003;font-weight:bold\">" + e + "</span><br>";
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (StackTraceElement s : stackTrace) {
                errMsg += "<span style=\"font-size:12px;color:#333\">类：" + s.getClassName() + "中，方法：" + s.getMethodName() + "，第：" + s.getLineNumber() + "行</span><br>";
            }
            HttpHelper.sendErr(response, errMsg);
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
                                cfLog.war("方法中参数：" + p.getName() + "没有实际注解，框架没有处理，默认传入null占位！请自行判断！");
                            }
                        }
                        //执行结果，交由视图Handler处理
                        Object invokeResult = m.invoke(service, objs.toArray());
                        //todo 执行结果，交由视图Handler处理
                        NullReturn nullReturn = m.getAnnotation(NullReturn.class);
                        if (nullReturn != null) {
                            //方法注解了空返回，不交由视图Handler处理，直接返回空
                            if (invokeResult != null) {
                                HttpHelper.sendErr(response, "函数：" + c.getTypeName() + "." + m.getName() + "()有返回值，但是你注解了@NullReturn，请去除，此注解将忽略所有返回值！");
                            }
                            return;
                        }
                        MapToFile mapToFile = m.getAnnotation(MapToFile.class);
                        ResponseBody responseBody = m.getAnnotation(ResponseBody.class);
                        if (mapToFile != null && responseBody != null) {
                            HttpHelper.sendErr(response, "@MapToFile与@ResponseBody在返回中，只能选择其中一种！请修改：" + c.getTypeName() + "中" + m.getName() + "方法！");
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
                                    cfLog.err("请求url中不含有key为：" + param.key() + "的值！");
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
                                cfLog.war("方法中参数：" + p.getName() + "没有实际注解，框架没有处理，默认传入null占位！请自行判断！");
                            }
                        }
                        //执行结果，交由视图Handler处理
                        Object invokeResult = m.invoke(service, objs.toArray());
                        //todo 执行结果，交由视图Handler处理
                        NullReturn nullReturn = m.getAnnotation(NullReturn.class);
                        if (nullReturn != null) {
                            //方法注解了空返回，不交由视图Handler处理，直接返回空
                            if (invokeResult != null) {
                                HttpHelper.sendErr(response, "函数：" + c.getTypeName() + "." + m.getName() + "()有返回值，但是你注解了@NullReturn，请去除，此注解将忽略所有返回值！");
                            }
                            return;
                        }
                        MapToFile mapToFile = m.getAnnotation(MapToFile.class);
                        ResponseBody responseBody = m.getAnnotation(ResponseBody.class);
                        if (mapToFile != null && responseBody != null) {
                            HttpHelper.sendErr(response, "@MapToFile与@ResponseBody在返回中，只能选择其中一种！请修改：" + c.getTypeName() + "中" + m.getName() + "方法！");
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
        switch (code){
            case RequestMethod.GET:
                HttpHelper.sendErr(response, "请求方式错误！此方法请求方式应为GET");
                break;
            case RequestMethod.POST:
                HttpHelper.sendErr(response, "请求方式错误！此方法请求方式应为POST");
                break;
        }
    }



}





