package cn.heshiqian.framework.h.servlet.classs;

import cn.heshiqian.framework.h.cflog.core.*;

import cn.heshiqian.framework.h.servlet.database.HServlet;
import cn.heshiqian.framework.h.servlet.startup.ClassScanner;
import cn.heshiqian.framework.h.servlet.tools.HttpHelper;

import java.lang.reflect.Parameter;
import java.util.*;

public final class ClassManage {

    private static ClassManage classManage;
    private static ClassScanner scanner;
    private static LifeRecycle lifeRecycle = new LifeRecycle();
    private static Logger cfLog=CFLog.logger(ClassManage.class);

    private ClassManage() {
    }

    public static ClassManage newInstance() {
        if (classManage == null) {
            classManage = new ClassManage();
            lifeRecycle.autoRun();
            return classManage;
        } else {
            return classManage;
        }
    }

    public static void doScan(String packageName) {
        if (scanner == null)
            scanner = ClassScanner.getInstance(packageName);
        else
            ClassScanner.reScan(packageName);
    }

    public static Class checkClassWasInit(String url){
        Class search = ClassScanner.search(url);
        if(search==null){
            cfLog.war(HServlet.CLASS_INFO_1);
            return null;
        }
        if(!ClassManage.Bridge.checkClassIsInit(search)){
            cfLog.war(HServlet.CLASS_INFO_2);
            cfLog.info(HServlet.CLASS_INFO_3+search.getTypeName());
            ClassManage.Bridge.newClass(search);
            return search;
        }else
            return search;
    }

//    public static Class _search(String url){
//        return ClassPool.search(url);
//    }

    public static class Bridge {

        public static boolean checkClassIsInit(Class c) {
            boolean init = ClassPool.isInit(c);
            if (init)
                lifeRecycle.addCount(c.getTypeName());
            else
                lifeRecycle.assertRemove(c.getTypeName());
            return init;
        }

        public static void newClass(Class c) {
            try {
                Object o = c.newInstance();
                ClassPool.newClass(o);
                lifeRecycle.addToManage(c.getTypeName(), 1);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        public static void stopLifeRecycle() {
            lifeRecycle.stopRun();
        }

    }


    private static class LifeRecycle {

        private Logger cfLog=CFLog.logger(LifeRecycle.class);

        private HashMap<String, Integer> map = new HashMap<>();
        private TimerTask timerTask;
        private static Timer timer;

        public LifeRecycle() {
            if (timerTask == null) {
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        synchronized (map) {
                            Iterator<String> keys = map.keySet().iterator();
                            while (keys.hasNext()) {
                                String oneClass = keys.next();
                                int count = map.get(oneClass);
                                if (count < 0) {
                                    lifeRecycle.assertRemove(oneClass);
                                    cfLog.print(oneClass + HServlet.CLASS_LONG_TIME_TO_USE);
                                } else {
                                    if (count < -999)
                                        count = -1;
                                    map.replace(oneClass, --count);
                                }
                            }
                        }
                    }
                };
            }
            if (timer == null)
                timer = new Timer();
        }

        public void addToManage(String className, int times) {
            synchronized (map) {
                map.put(className, times);
            }
        }

        public void addCount(String className) {
            //最多让这个类计数5次，总计可生存10分钟，若12分钟时还没使用会被回收
            synchronized (map) {
                int con = map.get(className);
                if(con>=5)
                    map.put(className,5);
                else
                    map.put(className,con+1);
            }
        }

        public void assertRemove(String className) {
            synchronized (map) {
                if (map.containsKey(className)) {
                    map.remove(className);
                    ClassPool.recycleClass(className);
                }
            }
        }

        public void stopRun() {
            if (timer != null)
                timer.cancel();
            timer=null;
            map.clear();
        }

        public void autoRun() {
            timer.schedule(timerTask, 1000 * 5, 1000 * 60 * 2);
        }

    }

}
