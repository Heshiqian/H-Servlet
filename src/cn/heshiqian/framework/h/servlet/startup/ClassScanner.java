package cn.heshiqian.framework.h.servlet.startup;

import cn.heshiqian.framework.h.cflog.core.*;

import cn.heshiqian.framework.h.servlet.annotation.Mapping;
import cn.heshiqian.framework.h.servlet.annotation.RequestUrl;
import cn.heshiqian.framework.h.servlet.tools.Tool;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ClassScanner {

    private static ClassScanner scanner;
    private static Logger cfLog=CFLog.logger(ClassScanner.class);
    private static List<Class<?>> scanClasses = new ArrayList<>();

    private ClassScanner() {
    }

    public static Class search(String url){
        for(Class c:scanClasses){
            Method[] declaredMethods = c.getDeclaredMethods();
            for(Method m:declaredMethods){
                RequestUrl requestUrl = m.getAnnotation(RequestUrl.class);
                if(requestUrl!=null){
                    if(requestUrl.value().equals(url)){
                        return c;
                    }
                }
            }
        }
        return null;
    }

    public static List<Class<?>> getScanClasses() {
        return scanClasses;
    }

    public static ClassScanner getInstance(String packageName){
        if(scanner==null){
            scanner=new ClassScanner();
            scanner.startScan(packageName);
            scanner.filterClass();
        }else
            scanner.reScan(packageName);
        return scanner;
    }


    /**
     * 这里是执行重扫的方法，正常情况不需要使用
     * @param packageName
     * @deprecated
     */
    public static void reScan(String packageName){
        //如果重扫了，清空之前扫过的
        scanClasses.clear();
        scanner.startScan(packageName);
    }

    private void filterClass(){
//        for(Class c:scanClasses){
//            Mapping mapping = (Mapping) c.getAnnotation(Mapping.class);
//            if(mapping==null)
//                scanClasses.remove(c);
//            else
//                cfLog.info("Mapping : "+c.getTypeName());
//        }
        Iterator<Class<?>> iterator = scanClasses.iterator();
        while (iterator.hasNext()){
            Class<?> next = iterator.next();
            Mapping mapping = next.getAnnotation(Mapping.class);
            if(mapping==null)
                iterator.remove();
            else{
                cfLog.info("Service Class : "+next.getTypeName());
                Method[] declaredMethods = next.getDeclaredMethods();
                for(Method m:declaredMethods){
                    RequestUrl annotation = m.getAnnotation(RequestUrl.class);
                    if(annotation!=null)
                        cfLog.info("Mapping : "+annotation.value()+" "+ Tool.requestMethodCodeVName(annotation.method()));
                }
            }
        }
    }

    /**
     * 扫描开始，在指定的包名下进行，摘自网络
     * @param pn 包名
     */
    private void startScan(String pn){
        boolean recursive = true;

        String pack = pn;
        String packDir = pack.replace('.', '/');
        Enumeration<URL> dirs;

        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packDir);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if("file".equals(protocol)){
                    String decode = URLDecoder.decode(url.getFile(), "utf-8");
                    addFileClass(pack,decode,recursive,scanClasses);
                } else if ("jar".equals(protocol)) {
                    JarFile jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry jarEntry = entries.nextElement();
                        String name = jarEntry.getName();
                        if (name.charAt(0) == '/') {
                            name = name.substring(1);
                        }

                        if (name.startsWith(packDir)) {
                            int idx = name.lastIndexOf('/');
                            if (idx != -1) {
                                pack = name.substring(0, idx).replace('/', '.');
                            }
                            if ((idx != -1) || recursive) {
                                // 如果是一个.class文件 而且不是目录
                                if (name.endsWith(".class") && !jarEntry.isDirectory()) {
                                    // 去掉后面的".class" 获取真正的类名
                                    String className = name.substring(pn.length() + 1, name.length() - 6);
                                    try {
                                        // 添加到scanClasses
                                        scanClasses.add(Class.forName(pn + '.' + className));
                                        cfLog.info("扫描到类："+scanClasses.get(scanClasses.size()-1).toString());
                                    } catch (ClassNotFoundException e) {
                                        //找不到的不添加也不提示
//                                        cfLog.err(e.getMessage());
                                    }
                                }
                            }
                        }

                    }
                }

            }
        } catch (IOException e) {
            cfLog.err(e.getMessage());
        }
    }

    /**
     * 对于文件类型的扫描，摘自网络
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param scanClasses
     */
    private void addFileClass(String packageName, String packagePath, boolean recursive, List<Class<?>> scanClasses) {

        File file = new File(packagePath);
        if(!file.exists()||!file.isDirectory()){
            cfLog.info("Don't exist path --> "+packagePath);
            return;
        }

        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return (recursive && pathname.isDirectory()) || (pathname.getName().endsWith(".class"));
            }
        });

        for(File f:files){

            if(f.isDirectory()){
                addFileClass(packageName+"."+f.getName(),f.getAbsolutePath(),recursive,scanClasses);
            }else {
                String classname = f.getName().substring(0, f.getName().length() - 6);
                try {
                    scanClasses.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + classname));
                    cfLog.info("扫描到类："+scanClasses.get(scanClasses.size()-1).toString());
                } catch (ClassNotFoundException e) {
                    cfLog.err("Not Found Class : "+packageName + '.' + classname+", And Error : "+e.getMessage());
                }
            }
        }

    }

}
