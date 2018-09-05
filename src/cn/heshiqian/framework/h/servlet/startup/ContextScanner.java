package cn.heshiqian.framework.h.servlet.startup;

import cn.heshiqian.framework.h.cflog.core.CFLog;

import cn.heshiqian.framework.h.servlet.database.FrameworkMemoryStorage;
import sun.net.util.IPAddressUtil;
import sun.security.x509.IPAddressName;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;
import javax.servlet.ServletContext;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

public final class ContextScanner {

    private static CFLog cfLog=new CFLog(ContextScanner.class);
    private static ServletContext context;
    private static ContextScanner contextScanner;

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
