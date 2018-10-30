package cn.heshiqian.framework.h.servlet.handler;


import cn.heshiqian.framework.h.cflog.core.*;
import cn.heshiqian.framework.h.servlet.annotation.*;
import cn.heshiqian.framework.h.servlet.annotation.upload.FileMapping;
import cn.heshiqian.framework.h.servlet.classs.ClassManage;
import cn.heshiqian.framework.h.servlet.classs.ClassPool;
import cn.heshiqian.framework.h.servlet.database.FrameworkMemoryStorage;
import cn.heshiqian.framework.h.servlet.database.HServlet;
import cn.heshiqian.framework.h.servlet.exception.FileUploadConfigureException;
import cn.heshiqian.framework.h.servlet.factory.FileAcceptFactory;
import cn.heshiqian.framework.h.servlet.factory.FileExtraProcessor;
import cn.heshiqian.framework.h.servlet.factory.SessionFactory;
import cn.heshiqian.framework.h.servlet.file.FileFactory;
import cn.heshiqian.framework.h.servlet.pojo.RequestMethod;
import cn.heshiqian.framework.h.servlet.startup.ClassScanner;
import cn.heshiqian.framework.h.servlet.tools.HttpHelper;
import cn.heshiqian.framework.h.servlet.view.ViewHandler;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("all")
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
                            GetParam getParam = p.getAnnotation(GetParam.class);
                            if(getParam!=null){
                                objs.add(keyMap.get(getParam.key()));
                            } else if (cookiess != null) {
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
                        if(!m.isAccessible())
                            m.setAccessible(true);
                        Object invokeResult = m.invoke(service, objs.toArray());
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
                            Session session = p.getAnnotation(Session.class);
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
                            } else if(session !=null){
                              //这个变量是要Session
                                if(p.getType().getTypeName().trim().equals(SessionFactory.class.getTypeName())){
                                    SessionFactory sessionFactory = new SessionFactory(request, response);
                                    objs.add(sessionFactory);
                                }else {
                                    cfLog.err("参数："+p.getName()+"不是类SessionFactory的变量！");
                                    objs.add(null);
                                }
                            } else {
                                objs.add(null);
                                cfLog.war(HServlet.HANDLE_DISPATCHER_INFO_1 + p.getName() + HServlet.HANDLE_DISPATCHER_INFO_2);
                            }
                        }
                        //执行结果，交由视图Handler处理
                        if(!m.isAccessible())
                            m.setAccessible(true);
                        Object invokeResult = m.invoke(service, objs.toArray());
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


    public void fileHandle(HttpServletRequest request, HttpServletResponse response, String requestURL) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        Class aClass = ClassManage.checkClassWasInit(requestURL);
        if (aClass == null) {
            HttpHelper.sendErr(response, HServlet.HANDLE_CENTER_NO_INTERFACE);
            return;
        }
        Object fileService = ClassPool.getClass(aClass);
        Method[] declaredMethods = aClass.getDeclaredMethods();
        for(Method m:declaredMethods){
            RequestUrl re = m.getAnnotation(RequestUrl.class);
            if(re!=null&&re.value().equals(requestURL)){
                FileMapping fileMapping = m.getAnnotation(FileMapping.class);
                if(fileMapping==null){
                    cfLog.war("你在Service :"+aClass.getTypeName()+"中，方法:"+m.getName()+" 请求URL:"+requestURL+"上缺少注释@FileMapping，即将按默认POST请求处理");
                    postReqResProcess_(aClass,requestURL,request,response,request.getCookies(),null);
                    return;
                }
                //生成一个Factory
                Class<FileFactory> fileFactoryClass = FileFactory.class;
                FileFactory fileFactory = fileFactoryClass.newInstance();
                //注入需求值
                Field req = fileFactoryClass.getDeclaredField("request");
                Field res = fileFactoryClass.getDeclaredField("response");
                Field factory = fileFactoryClass.getDeclaredField("fileAcceptFactory");
                req.setAccessible(true);
                res.setAccessible(true);
                factory.setAccessible(true);
                req.set(fileFactory,request);
                res.set(fileFactory,response);
                //生成唯一对应的AccpetFactory
                Class<FileAcceptFactory> fileAcceptFactoryClass = FileAcceptFactory.class;
                FileAcceptFactory fileAcceptFactory = fileAcceptFactoryClass.newInstance();
                factory.set(fileFactory,fileAcceptFactory);
                Parameter[] parameters = m.getParameters();
                if (parameters.length==0||parameters.length>1){
                    cfLog.err("方法:"+m.getName()+"中只允许存在FileFactory唯一变量");
                    throw new IllegalArgumentException("方法:"+m.getName()+"中只允许存在FileFactory唯一变量");
                }
                if(!parameters[0].getType().getTypeName().equals(FileFactory.class.getTypeName())){
                    cfLog.err("方法:"+m.getName()+"中只允许存在FileFactory唯一变量");
                    throw new IllegalArgumentException("方法:"+m.getName()+"中只允许存在FileFactory唯一变量");
                }
                Object invoke = m.invoke(fileService, fileFactory);
                ResponseBody responseBody = m.getAnnotation(ResponseBody.class);
                if(responseBody!=null)
                    viewHandler.analyzeResBody(invoke,response,request.getCookies());
                else
                    viewHandler.analysis(invoke,response,request.getCookies());
            }
        }
    }

}





