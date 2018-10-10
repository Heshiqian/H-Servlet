package cn.heshiqian.framework.h.servlet.startup;

import cn.heshiqian.framework.h.cflog.core.*;

import cn.heshiqian.framework.h.servlet.database.FrameworkMemoryStorage;
import cn.heshiqian.framework.h.servlet.database.HServlet;
import cn.heshiqian.framework.h.servlet.exception.FileUploadConfigureException;
import cn.heshiqian.framework.h.servlet.exception.NotExistInitParameterException;
import cn.heshiqian.framework.h.servlet.file.FileFactory;
import cn.heshiqian.framework.h.servlet.tools.Tool;
import cn.heshiqian.framework.s.xconf.XConf;
import cn.heshiqian.framework.s.xconf.pojo.XConfTree;
import sun.net.util.IPAddressUtil;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;
import javax.servlet.ServletContext;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

public final class ContextScanner {

    private static Logger cfLog=CFLog.logger(ContextScanner.class);
    private static ServletContext context;
    private static ContextScanner contextScanner;
    private static ArrayList<String> fileList;

    private ContextScanner(){
    }

    public static ContextScanner newInstance(){
        if(contextScanner==null){
            contextScanner=new ContextScanner();
            return contextScanner;
        }else{
            return contextScanner;
        }
    }

    public static void saveContext(ServletContext context){
        contextScanner.setContext(context);
        FrameworkMemoryStorage.context=context;
    }

    public static void prepare(){
        String[] IPV4Address={};

        //2018年9月6日10:11:23
        //对conf文件进行解析，代替原来在MainServlet里面的代码
        //2018年9月11日11:20:07
        //换个位置

        //对于类路径的配置还是使用web.xml
        String classesPackagePath = ContextScanner.getContext().getInitParameter("classesPackagePath");

        //获取到根目录
        String home = context.getRealPath("/");
        System.out.println(home);
        File file = new File(home);

        //找到conf文件
        String path = Tool.FileFinder.find(file, "configuration.conf");
        System.out.println(path);
        XConfTree confTree = XConf.read(path);
        FrameworkMemoryStorage.mainConfigure=confTree;

        //用NetworkInterface获取本地所有网络地址
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()){
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()){
                    InetAddress inetAddress = inetAddresses.nextElement();
                    String hostAddress = inetAddress.getHostAddress();
                    if(IPAddressUtil.isIPv4LiteralAddress(hostAddress)){
                        //保存为一个IPV4的本机地址
                        IPV4Address = Arrays.copyOf(IPV4Address, IPV4Address.length + 1);
                        IPV4Address[IPV4Address.length-1]=hostAddress;
                    }
                }
            }
            //手动加个localhost
            IPV4Address = Arrays.copyOf(IPV4Address, IPV4Address.length + 1);
            IPV4Address[IPV4Address.length-1]="localhost";

            //摘自网络，取目前运行的Tomcat端口
            //https://blog.csdn.net/weixin_38015582/article/details/78532363
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> objs = null;
            try {
                objs = mbs.queryNames(new ObjectName("*:type=Connector,*"),
                        Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
            } catch (MalformedObjectNameException e) {
                e.printStackTrace();
            }
            String port = null;
            for (Iterator<ObjectName> i = objs.iterator(); i.hasNext();) {
                ObjectName obj = i.next();
                obj.getCanonicalName();
                port = obj.getKeyProperty("port");
            }

            //存起来
            FrameworkMemoryStorage.ServerDomain = FrameworkMemoryStorage.mainConfigure.getRootByName("server").getLeafByName("domain").getValue();
            FrameworkMemoryStorage.ServerPort=port;
            FrameworkMemoryStorage.ServerIps=IPV4Address;
            cfLog.print("本机IP："+Arrays.toString(IPV4Address));
            cfLog.print("服务器Port："+port);
            if(FrameworkMemoryStorage.ServerDomain.equals("")){
                cfLog.print("没有配置域名解析，不配置域名在使用访问时可能造成不能解析！");
            }
        } catch (SocketException e) {
            e.printStackTrace();
            cfLog.err("在解析本地地址时出现错误！详见系统报错！");
        }

        //对服务器可本地访问的所有IP进行拼接
        if(!(FrameworkMemoryStorage.ServerPort.equals("80")||FrameworkMemoryStorage.ServerPort.equals("443"))){
            ArrayList<String> tempList=new ArrayList<>();
            for(String s:IPV4Address){
                tempList.add(s+":"+FrameworkMemoryStorage.ServerPort);
            }
            if(!FrameworkMemoryStorage.ServerDomain.equals(""))
                tempList.add(FrameworkMemoryStorage.ServerDomain+":"+FrameworkMemoryStorage.ServerPort);
            FrameworkMemoryStorage.allLocalIpAddress=tempList;
        }else {
            FrameworkMemoryStorage.allLocalIpAddress=new ArrayList<>();
            FrameworkMemoryStorage.allLocalIpAddress.addAll(Arrays.asList(IPV4Address));
            FrameworkMemoryStorage.allLocalIpAddress.add(FrameworkMemoryStorage.ServerDomain);
        }


        //获取配置
        String staticFilePath = confTree.getRootByName("server").getLeafByName("staticFilePath").getValue();
        boolean staticFileLog = Boolean.valueOf(confTree.getRootByName("server").getLeafByName("enableStaticFileLog").getValue());
        cfLog.info("配置信息：classesPackagePath --> " + classesPackagePath);
        cfLog.info("配置信息：staticFilePath --> " + staticFilePath);
        if (classesPackagePath == null || classesPackagePath.equals("")) {
            cfLog.err("缺少上下文配置信息：classesPackagePath\n" +
                    "请在web.xml下配置<context-param>标签\n" +
                    "其中<param-name>为classesPackagePath\n" +
                    "<param-value>值为包名(最大一层即可)");
            throw new NotExistInitParameterException("缺少上下文配置信息：classesPackagePath");
        }
        if (staticFilePath == null || staticFilePath.equals("")) {
            cfLog.err("缺少上下文配置信息：staticFilePath\n" +
                    "请在"+path+"下配置[server]节点\n" +
                    "其中'staticFilePath'值为空或未填写\n" +
                    "'staticFilePath'值为静态文件所在路径(最大一层即可，例如：'staticFilePath=/'");
            throw new NotExistInitParameterException("缺少上下文配置信息：staticFilePath");
        }
        FrameworkMemoryStorage.classesPackagePath = classesPackagePath;
        FrameworkMemoryStorage.staticFilePath = staticFilePath;
        FrameworkMemoryStorage.staticFileDir = ContextScanner.getContext().getRealPath(staticFilePath);
        FrameworkMemoryStorage.staticFileLogSwitch=staticFileLog;
        FrameworkMemoryStorage.enableRequestErrorTip = Boolean.valueOf(FrameworkMemoryStorage.mainConfigure.getRootByName("server").getLeafByName("enableRequestErrorTip").getValue());
        FrameworkMemoryStorage.disabledNullReturnWaring = Boolean.valueOf(FrameworkMemoryStorage.mainConfigure.getRootByName("server").getLeafByName("disabledNullReturnWaring").getValue());
        FrameworkMemoryStorage.enableFileUpload = Boolean.valueOf(FrameworkMemoryStorage.mainConfigure.getRootByName("server").getLeafByName("enableFileUpload").getValue());

        if(FrameworkMemoryStorage.enableFileUpload){
            cfLog.info("已开启文件上传服务");
            File up = new File(context.getRealPath("/WEB-INF") + "\\upload");
            if(!up.exists())
                up.mkdirs();
            cfLog.info("文件存放地址："+up.getAbsolutePath());
            FrameworkMemoryStorage.uploadFileSaveF=up;
        }


/*
        if(FrameworkMemoryStorage.fileUploadHandlerClass.equals("")&&FrameworkMemoryStorage.enableFileUpload){
            String uUrl = FrameworkMemoryStorage.mainConfigure.getRootByName("upload").getLeafByName("uploadURL").getValue();
            uUrl = uUrl.replace("\\", "/");
            if (uUrl.indexOf("/")!=0)
                uUrl="/"+uUrl;
            FrameworkMemoryStorage.fileUploadURL=uUrl;
            if (uUrl.equals("")){
                FrameworkMemoryStorage.fileUploadURL="/upload";
                cfLog.war("你没有配置路径，默认使用："+FrameworkMemoryStorage.fileUploadURL);
            }
            cfLog.war("配置的文件上传路径："+uUrl);
            String handleClassName = FrameworkMemoryStorage.mainConfigure.getRootByName("upload").getLeafByName("uploadProcessHandle").getValue();
            if(handleClassName==null||handleClassName.trim().equals("")){
                cfLog.err("服务类初始化失败！");
                throw new FileUploadConfigureException("配置的文件上传处理类为空");
            }
            FrameworkMemoryStorage.fileUploadHandlerClass=handleClassName;
            try {
                Class<?> handleClass = Thread.currentThread().getContextClassLoader().loadClass(FrameworkMemoryStorage.fileUploadHandlerClass);
                if(handleClass.getSuperclass().getTypeName().equals(FileUploadProcessFactory.class.getTypeName()))
                    FrameworkMemoryStorage.fileUploadClassInstance = handleClass.newInstance();
                else {
                    FileUploadConfigureException ee = new FileUploadConfigureException("配置的类不是FileUploadProcessFactory的子类！");
                    cfLog.err(ee.toString());
                    cfLog.err("服务类初始化失败！");
                    throw new IllegalStateException(ee);
                }
            } catch (ClassNotFoundException e) {
                FileUploadConfigureException ee = new FileUploadConfigureException("配置的类无法找到", e);
                cfLog.err(ee.toString());
                cfLog.err("服务类初始化失败！");
                throw new IllegalStateException(ee);
            } catch (IllegalAccessException e) {
                FileUploadConfigureException ee = new FileUploadConfigureException("配置的类无法访问，可能构造函数为私有", e);
                cfLog.err(ee.toString());
                cfLog.err("服务类初始化失败！");
                throw new IllegalStateException(ee);
            } catch (InstantiationException e) {
                FileUploadConfigureException ee = new FileUploadConfigureException("配置的类可能是个接口或抽象类，无法实例化", e);
                cfLog.err(ee.toString());
                cfLog.err("服务类初始化失败！");
                throw new IllegalStateException(ee);
            }
        }
*/
        //静态配置
        String filterType = confTree.getRootByName("static").getLeafByName("filterType").getValue();
        cfLog.info("静态代理模式："+filterType);
        if("custom".equals(filterType)) {
            FrameworkMemoryStorage.filterType = HServlet.FILER_TYPE_CUSTOM;
            try {
                FrameworkMemoryStorage.filterCustomContent = confTree.getRootByName("static").getLeafByName("filterList").getValue();
            }catch (RuntimeException e){
                cfLog.err("缺少'filterList'键值！现在配置的模式为'自定义'，请配置'filterList'！已自动切换为'自动'模式！");
                FrameworkMemoryStorage.filterType = HServlet.FILER_TYPE_AUTO;
            }
        }
        if("off".equals(filterType)) {
            FrameworkMemoryStorage.filterType = HServlet.FILER_TYPE_OFF;
        }

        //对静态文件的扫描
        cfLog.info("设置的静态目录：" + FrameworkMemoryStorage.staticFileDir);

        fileList = Tool.listAllFile(FrameworkMemoryStorage.staticFileDir);
        cfLog.info("扫描到的静态文件：" + fileList.size() + "个");
        for (int i = 0; i < fileList.size(); i++) {
            String one = fileList.get(i);
            String two = one.replace(FrameworkMemoryStorage.staticFileDir, "/");
            String tre = two.replace("\\", "/");
            fileList.remove(i);
            fileList.add(i, tre);
        }
        int mark = 1;
        System.out.println("文件列表如下：");
        for (int i = 0; i < fileList.size(); i++, mark++) {
            System.out.print(fileList.get(i));
            if (mark % 10 == 0)
                System.out.println();
            else
            if (mark == fileList.size())
                System.out.println();
            else
                System.out.print(",");
        }
        FrameworkMemoryStorage.fileList=fileList;

    }

    public static ServletContext getContext(){
        return contextScanner.gContext();
    }

    private void setContext(ServletContext context) {
        ContextScanner.context = context;
    }
    private ServletContext gContext() {
        return context;
    }
}
