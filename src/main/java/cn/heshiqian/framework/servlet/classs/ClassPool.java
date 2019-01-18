package cn.heshiqian.framework.servlet.classs;

import cn.heshiqian.framework.h.cflog.core.*;
import cn.heshiqian.framework.servlet.database.HServlet;
import cn.heshiqian.framework.servlet.exception.NotExistInitClassException;

import java.util.ArrayList;

public final class ClassPool {

    private static Logger cfLog=CFLog.logger(ClassPool.class);
    private static ArrayList<Object> initClasses=new ArrayList<>();


    public static boolean isInit(Class c){
        for(Object o:initClasses){
            if (o.getClass().getTypeName().equals(c.getTypeName()))
                return true;
        }
        return false;
    }


    public static void newClass(Object o){
        synchronized (initClasses){
            initClasses.add(o);
        }
    }

    /**
     * 获取到指定类的实例化对象
     * @param c 类
     * @return 类对象
     */
    public static Object getClass(Class c){
        synchronized (initClasses){
            for(Object o:initClasses){
                if (o.getClass().getTypeName().equals(c.getTypeName()))
                    return o;
            }
            throw new NotExistInitClassException(HServlet.CLASS_POOL_ERROR);
        }
    }

    public static void recycleClass(String className){
        synchronized (initClasses){
            for(Object o:initClasses){
                if (o.getClass().getTypeName().equals(className)){
                    initClasses.remove(o);
                    break;
                }
            }
        }
    }

//    public static Class search(String url){
//        for(Object o:initClasses){
//            Class<?> aClass = o.getClass();
//            RequestMethod[] declaredMethods = aClass.getDeclaredMethods();
//            for(RequestMethod m:declaredMethods){
//                RequestUrl annotation = m.getAnnotation(RequestUrl.class);
//                if(annotation!=null){
//                    if (annotation.value().equals(url)){
//                        return aClass;
//                    }
//                }
//            }
//        }
//        cfLog.err("没有找到此类！对应Url："+url);
//        return null;
//    }
}
