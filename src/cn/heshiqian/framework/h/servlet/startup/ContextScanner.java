package cn.heshiqian.framework.h.servlet.startup;

import cn.heshiqian.framework.h.cflog.core.CFLog;

import cn.heshiqian.framework.h.servlet.database.FrameworkMemoryStorage;
import cn.heshiqian.framework.h.servlet.exception.NotExistInitParameterException;
import cn.heshiqian.framework.h.servlet.tools.Tool;
import cn.heshiqian.framework.s.xconf.XConf;
import cn.heshiqian.framework.s.xconf.pojo.XConfTree;
import sun.net.util.IPAddressUtil;
import sun.security.x509.IPAddressName;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;
import javax.servlet.ServletContext;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

public final class ContextScanner {

    private static CFLog cfLog=new CFLog(ContextScanner.class);
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
            FrameworkMemoryStorage.ServerPort=port;
            FrameworkMemoryStorage.ServerIps=IPV4Address;
            cfLog.print("本机IP："+Arrays.toString(IPV4Address));
            cfLog.print("服务器Port："+port);

        } catch (SocketException e) {
            e.printStackTrace();
            cfLog.err("在解析本地地址时出现错误！详见系统报错！");
        }

        //对服务器可本地访问的所有IP进行拼接
        ArrayList<String> tempList=new ArrayList<>();
        for(String s:IPV4Address){
            tempList.add(s+":"+FrameworkMemoryStorage.ServerPort);
        }
        FrameworkMemoryStorage.allLocalIpAddress=tempList;

        //2018年9月6日10:11:23
        //对conf文件进行解析，代替原来在MainServlet里面的代码

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
