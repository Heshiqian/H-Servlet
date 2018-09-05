package cn.heshiqian.framework.h.servlet.classs;

import cn.heshiqian.framework.h.cflog.core.CFLog;

import cn.heshiqian.framework.h.servlet.annotation.RequestUrl;
import cn.heshiqian.framework.h.servlet.exception.NotExistInitClassException;

import java.lang.reflect.Method;
import java.util.ArrayList;

public final class ClassPool {

    private static CFLog cfLog=new CFLog(ClassPool.class);
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
            throw new NotExistInitClassException("在类池中不存在传入的类！");
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
